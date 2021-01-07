package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class MessageClassifier {
    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final Map<Long, Map<String, Map<String, Frequency>>> wordClassFrequency = new HashMap<>();

    private static final double CHANCE_MINIMUM = 0.20;
    private static final double CHANCE_THRESHOLD = 2.0;

    static void classify(EntityMessage message, EntityFolder folder, EntityFolder target, Context context) {
        try {
            if (!isEnabled(context))
                return;

            if (!canClassify(folder.type))
                return;

            if (target != null && !canClassify(target.type))
                return;

            File file = message.getFile(context);
            if (!file.exists())
                return;

            long start = new Date().getTime();

            // Build text to classify
            StringBuilder sb = new StringBuilder();

            List<Address> addresses = new ArrayList<>();
            if (message.from != null)
                addresses.addAll(Arrays.asList(message.from));
            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
            if (message.bcc != null)
                addresses.addAll(Arrays.asList(message.bcc));
            if (message.reply != null)
                addresses.addAll(Arrays.asList(message.reply));

            for (Address address : addresses) {
                String email = ((InternetAddress) address).getAddress();
                String name = ((InternetAddress) address).getAddress();
                if (!TextUtils.isEmpty(email)) {
                    sb.append(email).append('\n');
                    int at = email.indexOf('@');
                    String domain = (at < 0 ? null : email.substring(at + 1));
                    if (!TextUtils.isEmpty(domain))
                        sb.append(domain).append('\n');
                }
                if (!TextUtils.isEmpty(name))
                    sb.append(name).append('\n');
            }

            if (message.subject != null)
                sb.append(message.subject).append('\n');

            sb.append(HtmlHelper.getFullText(file));

            if (sb.length() == 0)
                return;

            // Load data if needed
            load(context);

            // Initialize data if needed
            if (!wordClassFrequency.containsKey(folder.account))
                wordClassFrequency.put(folder.account, new HashMap<>());

            // Classify text
            String classified = classify(folder.account, folder.name, sb.toString(), target == null, context);

            long elapsed = new Date().getTime() - start;
            EntityLog.log(context, "Classifier" +
                    " folder=" + folder.name +
                    " message=" + message.id +
                    "@" + new Date(message.received) +
                    ":" + message.subject +
                    " class=" + classified +
                    " re=" + message.auto_classified +
                    " elapsed=" + elapsed);

            dirty = true;

            // Auto classify
            if (classified != null &&
                    !classified.equals(folder.name) &&
                    !message.auto_classified &&
                    !EntityFolder.JUNK.equals(folder.type)) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder dest = db.folder().getFolderByName(folder.account, classified);
                    if (dest != null && dest.auto_classify) {
                        EntityOperation.queue(context, message, EntityOperation.MOVE, dest.id, false, true);
                        message.ui_hide = true;
                    }

                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static String classify(long account, String currentClass, String text, boolean added, Context context) {
        int maxMessages = 0;
        for (String word : wordClassFrequency.get(account).keySet()) {
            for (String clazz : wordClassFrequency.get(account).get(word).keySet()) {
                int count = wordClassFrequency.get(account).get(word).get(clazz).count;
                if (count > maxMessages)
                    maxMessages = count;
            }
        }

        State state = new State();
        process(account, currentClass, added, null, state);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            java.text.BreakIterator boundary = java.text.BreakIterator.getWordInstance();
            boundary.setText(text);
            int start = boundary.first();
            for (int end = boundary.next(); end != java.text.BreakIterator.DONE; end = boundary.next()) {
                String word = text.substring(start, end);
                process(account, currentClass, added, word, state);
                start = end;
            }
        } else {
            // The ICU break iterator works better for Chinese texts
            android.icu.text.BreakIterator boundary = android.icu.text.BreakIterator.getWordInstance();
            boundary.setText(text);
            int start = boundary.first();
            for (int end = boundary.next(); end != android.icu.text.BreakIterator.DONE; end = boundary.next()) {
                String word = text.substring(start, end);
                process(account, currentClass, added, word, state);
                start = end;
            }
        }

        process(account, currentClass, added, null, state);

        if (!added)
            return null;

        if (maxMessages == 0) {
            Log.e("Classifier no messages account=" + account);
        }

        DB db = DB.getInstance(context);
        List<Chance> chances = new ArrayList<>();
        for (String clazz : state.classStats.keySet()) {
            EntityFolder folder = db.folder().getFolderByName(account, clazz);
            if (folder == null) {
                Log.w("Classifier no folder class=" + account + ":" + clazz);
                continue;
            }

            Stat stat = state.classStats.get(clazz);

            double chance = stat.totalFrequency / maxMessages / state.words.size();
            Chance c = new Chance(clazz, chance);
            chances.add(c);
            EntityLog.log(context, "Classifier " + c +
                    " frequency=" + (Math.round(stat.totalFrequency * 100.0) / 100.0) + "/" + maxMessages + " msgs" +
                    " matched=" + stat.matchedWords + "/" + state.words.size() + " words" +
                    " text=" + TextUtils.join(", ", stat.words));
        }

        if (BuildConfig.DEBUG)
            Log.i("Classifier words=" + TextUtils.join(", ", state.words));

        if (chances.size() <= 1)
            return null;

        Collections.sort(chances, new Comparator<Chance>() {
            @Override
            public int compare(Chance c1, Chance c2) {
                return -c1.chance.compareTo(c2.chance);
            }
        });

        String classification = null;
        if (chances.get(0).chance > CHANCE_MINIMUM &&
                chances.get(0).chance / chances.get(1).chance >= CHANCE_THRESHOLD)
            classification = chances.get(0).clazz;

        Log.i("Classifier current=" + currentClass + " classified=" + classification);

        return classification;
    }

    private static void process(long account, String currentClass, boolean added, String word, State state) {
        if (word != null) {
            word = word.trim().toLowerCase();

            if (word.length() < 2 ||
                    state.words.contains(word) ||
                    word.matches(".*\\d.*"))
                return;
        }

        state.words.add(word);

        if (state.words.size() < 3)
            return;

        String before = state.words.get(state.words.size() - 3);
        String current = state.words.get(state.words.size() - 2);
        String after = state.words.get(state.words.size() - 1);

        Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(current);
        if (added) {
            if (classFrequency == null) {
                classFrequency = new HashMap<>();
                wordClassFrequency.get(account).put(current, classFrequency);
            }

            for (String clazz : classFrequency.keySet()) {
                Frequency frequency = classFrequency.get(clazz);
                if (frequency.count > 0) {
                    Stat stat = state.classStats.get(clazz);
                    if (stat == null) {
                        stat = new Stat();
                        state.classStats.put(clazz, stat);
                    }

                    int c = frequency.count;
                    Integer b = (before == null ? null : frequency.before.get(before));
                    Integer a = (after == null ? null : frequency.after.get(after));
                    double f = ((b == null ? 0 : b) + c + (a == null ? 0 : a)) / 3.0;
                    stat.totalFrequency += f;

                    stat.matchedWords++;
                    if (stat.matchedWords > state.maxMatchedWords)
                        state.maxMatchedWords = stat.matchedWords;

                    if (BuildConfig.DEBUG)
                        stat.words.add(current);
                }
            }

            Frequency c = classFrequency.get(currentClass);
            if (c == null)
                c = new Frequency();
            c.add(before, after, 1);
            classFrequency.put(currentClass, c);
        } else {
            Frequency c = (classFrequency == null ? null : classFrequency.get(currentClass));
            if (c != null)
                c.add(before, after, -1);
        }
    }

    static synchronized void save(Context context) throws JSONException, IOException {
        if (!dirty)
            return;

        File file = getFile(context);
        Helper.writeText(file, toJson().toString(2));

        dirty = false;
        Log.i("Classifier data saved");
    }

    private static synchronized void load(Context context) throws IOException, JSONException {
        if (loaded || dirty)
            return;

        wordClassFrequency.clear();

        File file = getFile(context);
        if (file.exists()) {
            String json = Helper.readText(file);
            fromJson(new JSONObject(json));
        }

        loaded = true;
        Log.i("Classifier data loaded");
    }

    static synchronized void clear(Context context) {
        wordClassFrequency.clear();
        dirty = true;
        Log.i("Classifier data cleared");
    }

    static boolean isEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("classification", false);
    }

    static boolean canClassify(String folderType) {
        return EntityFolder.INBOX.equals(folderType) ||
                EntityFolder.JUNK.equals(folderType) ||
                EntityFolder.USER.equals(folderType);
    }

    static File getFile(Context context) {
        return new File(context.getFilesDir(), "classifier.json");
    }

    static JSONObject toJson() throws JSONException {
        JSONArray jwords = new JSONArray();
        for (Long account : wordClassFrequency.keySet())
            for (String word : wordClassFrequency.get(account).keySet()) {
                Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(word);
                for (String clazz : classFrequency.keySet()) {
                    Frequency f = classFrequency.get(clazz);
                    JSONObject jword = new JSONObject();
                    jword.put("account", account);
                    jword.put("word", word);
                    jword.put("class", clazz);
                    jword.put("frequency", f.count);
                    jword.put("before", from(f.before));
                    jword.put("after", from(f.after));
                    jwords.put(jword);
                }
            }

        JSONObject jroot = new JSONObject();
        jroot.put("words", jwords);

        return jroot;
    }

    private static JSONObject from(Map<String, Integer> map) throws JSONException {
        JSONObject jmap = new JSONObject();
        for (String key : map.keySet())
            jmap.put(key, map.get(key));
        return jmap;
    }

    static void fromJson(JSONObject jroot) throws JSONException {
        JSONArray jwords = jroot.getJSONArray("words");
        for (int w = 0; w < jwords.length(); w++) {
            JSONObject jword = (JSONObject) jwords.get(w);
            long account = jword.getLong("account");
            if (!wordClassFrequency.containsKey(account))
                wordClassFrequency.put(account, new HashMap<>());
            String word = jword.getString("word");
            Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(word);
            if (classFrequency == null) {
                classFrequency = new HashMap<>();
                wordClassFrequency.get(account).put(word, classFrequency);
            }
            Frequency f = new Frequency();
            f.count = jword.getInt("frequency");
            if (jword.has("before"))
                f.before = from(jword.getJSONObject("before"));
            if (jword.has("after"))
                f.after = from(jword.getJSONObject("after"));
            classFrequency.put(jword.getString("class"), f);
        }
    }

    private static Map<String, Integer> from(JSONObject jmap) throws JSONException {
        Map<String, Integer> result = new HashMap<>(jmap.length());
        Iterator<String> iterator = jmap.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            result.put(key, jmap.getInt(key));
        }
        return result;
    }

    private static class State {
        private int maxMatchedWords = 0;
        private List<String> words = new ArrayList<>();
        private Map<String, Stat> classStats = new HashMap<>();
    }

    private static class Frequency {
        private int count = 0;
        private Map<String, Integer> before = new HashMap<>();
        private Map<String, Integer> after = new HashMap<>();

        private void add(String b, String a, int c) {
            if (count + c < 0)
                return;

            count += c;

            if (b != null) {
                Integer x = before.get(b);
                before.put(b, (x == null ? 0 : x) + c);
            }

            if (a != null) {
                Integer x = after.get(a);
                after.put(a, (x == null ? 0 : x) + c);
            }
        }
    }

    private static class Stat {
        int matchedWords = 0;
        double totalFrequency = 0;
        List<String> words = new ArrayList<>();
    }

    private static class Chance {
        private String clazz;
        private Double chance;

        private Chance(String clazz, Double chance) {
            this.clazz = clazz;
            this.chance = chance;
        }

        @NotNull
        @Override
        public String toString() {
            return clazz + "=" + Math.round(chance * 100.0 * 100.0) / 100.0 + "%";
        }
    }
}
