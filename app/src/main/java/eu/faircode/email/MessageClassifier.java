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

import androidx.annotation.NonNull;
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
    private static final Map<Long, List<String>> accountMsgIds = new HashMap<>();
    private static final Map<Long, Map<String, Integer>> classMessages = new HashMap<>();
    private static final Map<Long, Map<String, Map<String, Frequency>>> wordClassFrequency = new HashMap<>();

    static void classify(EntityMessage message, EntityFolder folder, EntityFolder target, Context context) {
        try {
            if (!isEnabled(context))
                return;

            if (!canClassify(folder.type))
                return;

            if (target != null && !canClassify(target.type))
                return;

            long start = new Date().getTime();

            // Build text to classify
            List<String> texts = getTexts(message, context);
            if (texts.size() == 0)
                return;

            // Load data if needed
            load(context);

            // Initialize account if needed
            if (!accountMsgIds.containsKey(folder.account))
                accountMsgIds.put(folder.account, new ArrayList<>());
            if (!classMessages.containsKey(folder.account))
                classMessages.put(folder.account, new HashMap<>());
            if (!wordClassFrequency.containsKey(folder.account))
                wordClassFrequency.put(folder.account, new HashMap<>());

            // Classify texts
            String classified = classify(folder.account, folder.name, texts, target == null, context);

            long elapsed = new Date().getTime() - start;
            EntityLog.log(context, "Classifier" +
                    " folder=" + folder.name +
                    " message=" + message.id +
                    "@" + new Date(message.received) +
                    ":" + message.subject +
                    " class=" + classified +
                    " re=" + message.auto_classified +
                    " elapsed=" + elapsed);

            // Auto classify message
            if (classified != null &&
                    !classified.equals(folder.name) &&
                    !TextUtils.isEmpty(message.msgid) &&
                    !accountMsgIds.get(folder.account).contains(message.msgid) &&
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

                    accountMsgIds.get(folder.account).add(message.msgid);
                } finally {
                    db.endTransaction();
                }
            }

            dirty = true;
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @NonNull
    private static List<String> getTexts(@NonNull EntityMessage message, @NonNull Context context) throws IOException {
        List<String> texts = new ArrayList<>();

        File file = message.getFile(context);
        if (!file.exists())
            return texts;

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
            String name = ((InternetAddress) address).getPersonal();
            if (!TextUtils.isEmpty(email))
                texts.add(email);
            if (!TextUtils.isEmpty(name))
                texts.add(name);
        }

        if (message.subject != null)
            texts.add(message.subject);

        String text = HtmlHelper.getFullText(file);
        texts.add(text);

        return texts;
    }

    private static String classify(long account, @NonNull String currentClass, @NonNull List<String> texts, boolean added, @NonNull Context context) {
        State state = new State();

        Log.i("Classifier texts=" + texts.size());
        for (String text : texts) {
            // First word
            processWord(account, added, null, state);

            // Process words
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                java.text.BreakIterator boundary = java.text.BreakIterator.getWordInstance();
                boundary.setText(text);
                int start = boundary.first();
                for (int end = boundary.next(); end != java.text.BreakIterator.DONE; end = boundary.next()) {
                    String word = text.substring(start, end);
                    processWord(account, added, word, state);
                    start = end;
                }
            } else {
                // The ICU break iterator works better for Chinese texts
                android.icu.text.BreakIterator boundary = android.icu.text.BreakIterator.getWordInstance();
                boundary.setText(text);
                int start = boundary.first();
                for (int end = boundary.next(); end != android.icu.text.BreakIterator.DONE; end = boundary.next()) {
                    String word = text.substring(start, end);
                    processWord(account, added, word, state);
                    start = end;
                }
            }
        }

        // final word
        processWord(account, added, null, state);

        int maxMessages = 0;
        for (String clazz : classMessages.get(account).keySet()) {
            int count = classMessages.get(account).get(clazz);
            if (count > maxMessages)
                maxMessages = count;
        }

        updateFrequencies(account, currentClass, added, state);

        if (maxMessages == 0) {
            Log.i("Classifier no messages account=" + account);
            return null;
        }

        if (!added)
            return null;

        // Calculate chance per class
        DB db = DB.getInstance(context);
        int words = state.words.size() - texts.size() - 1;
        List<Chance> chances = new ArrayList<>();
        for (String clazz : state.classStats.keySet()) {
            EntityFolder folder = db.folder().getFolderByName(account, clazz);
            if (folder == null) {
                Log.w("Classifier no folder class=" + account + ":" + clazz);
                continue;
            }

            Stat stat = state.classStats.get(clazz);

            double chance = stat.totalFrequency / maxMessages / words;
            Chance c = new Chance(clazz, chance);
            chances.add(c);
            EntityLog.log(context, "Classifier " + c +
                    " frequency=" + (Math.round(stat.totalFrequency * 100.0) / 100.0) + "/" + maxMessages + " msgs" +
                    " matched=" + stat.matchedWords + "/" + words + " words" +
                    " text=" + TextUtils.join(", ", stat.words));
        }

        if (BuildConfig.DEBUG)
            Log.i("Classifier words=" + state.words.size() + " " + TextUtils.join(", ", state.words));

        if (chances.size() <= 1)
            return null;

        // Sort classes by chance
        Collections.sort(chances, new Comparator<Chance>() {
            @Override
            public int compare(Chance c1, Chance c2) {
                return -c1.chance.compareTo(c2.chance);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double class_min_chance = prefs.getInt("class_min_probability", 50) / 100.0;
        double class_min_difference = prefs.getInt("class_min_difference", 50) / 100.0;

        // Select best class
        String classification = null;
        double c0 = chances.get(0).chance;
        double c1 = chances.get(1).chance;
        double threshold = c0 * (1.0 - class_min_difference);
        if (c0 > class_min_chance && c1 < threshold)
            classification = chances.get(0).clazz;

        Log.i("Classifier current=" + currentClass +
                " c0=" + Math.round(c0 * 100 * 100) / 100.0 + ">" + Math.round(class_min_chance * 100) + "%" +
                " c1=" + Math.round(c1 * 100 * 100) / 100.0 + "<" + Math.round(threshold * 100 * 100) / 100.0 + "%" +
                " (" + Math.round(class_min_difference * 100) + "%)" +
                " classified=" + classification);

        return classification;
    }

    private static void processWord(long account, boolean added, String word, State state) {
        if (word != null) {
            word = word.trim().toLowerCase();
            if (word.length() < 2 || word.matches(".*\\d.*"))
                return;
        }

        if (word != null ||
                state.words.size() == 0 ||
                state.words.get(state.words.size() - 1) != null)
            state.words.add(word);

        if (!added)
            return;

        if (state.words.size() < 3)
            return;

        String before = state.words.get(state.words.size() - 3);
        String current = state.words.get(state.words.size() - 2);
        String after = state.words.get(state.words.size() - 1);

        if (current == null)
            return;

        Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(current);
        if (classFrequency == null)
            return;

        for (String clazz : classFrequency.keySet()) {
            Frequency frequency = classFrequency.get(clazz);
            if (frequency.count <= 0)
                continue;

            Stat stat = state.classStats.get(clazz);
            if (stat == null) {
                stat = new Stat();
                state.classStats.put(clazz, stat);
            }

            int c = (frequency.count - frequency.duplicates);
            Integer b = (before == null ? null : frequency.before.get(before));
            Integer a = (after == null ? null : frequency.after.get(after));
            double f = (c +
                    (b == null ? 2 * c : 2.0 * b / frequency.count * c) +
                    (a == null ? 2 * c : 2.0 * a / frequency.count * c)) / 5.0;
            //Log.i("Classifier " +
            //        before + "/" + b + "/" + frequency.before.get(before) + " " +
            //        after + "/" + a + "/" + frequency.after.get(after) + " " +
            //        current + "/" + c + "=" + frequency.count + "-" + frequency.duplicates +
            //        " f=" + f);

            stat.totalFrequency += f;
            stat.matchedWords++;

            if (BuildConfig.DEBUG)
                stat.words.add(current + "=" + f);
        }
    }

    private static void updateFrequencies(long account, @NonNull String currentClass, boolean added, @NonNull State state) {
        Integer m = classMessages.get(account).get(currentClass);
        m = (m == null ? 0 : m) + (added ? 1 : -1);
        if (m <= 0)
            classMessages.get(account).remove(currentClass);
        else
            classMessages.get(account).put(currentClass, m);
        Log.i("Classifier " + currentClass + "=" + m + " msgs");

        for (int i = 1; i < state.words.size() - 1; i++) {
            String before = state.words.get(i - 1);
            String current = state.words.get(i);
            String after = state.words.get(i + 1);

            if (current == null)
                continue;

            Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(current);
            if (added) {
                if (classFrequency == null) {
                    classFrequency = new HashMap<>();
                    wordClassFrequency.get(account).put(current, classFrequency);
                }
                Frequency c = classFrequency.get(currentClass);
                if (c == null) {
                    c = new Frequency();
                    classFrequency.put(currentClass, c);
                }
                c.add(before, after, 1, state.words.indexOf(current) < i);
            } else {
                Frequency c = (classFrequency == null ? null : classFrequency.get(currentClass));
                if (c != null)
                    c.add(before, after, -1, state.words.indexOf(current) < i);
            }
        }
    }

    static synchronized void save(@NonNull Context context) throws JSONException, IOException {
        if (!dirty)
            return;

        File file = getFile(context);
        Helper.writeText(file, toJson().toString(2));

        dirty = false;
        Log.i("Classifier data saved");
    }

    private static synchronized void load(@NonNull Context context) throws IOException, JSONException {
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

    static synchronized void cleanup(@NonNull Context context) {
        try {
            load(context);

            DB db = DB.getInstance(context);
            for (Long account : accountMsgIds.keySet()) {
                List<String> msgids = accountMsgIds.get(account);
                Log.i("Classifier cleanup account=" + account + " count=" + msgids.size());
                for (String msgid : new ArrayList<>(msgids)) {
                    List<EntityMessage> messages = db.message().getMessagesByMsgId(account, msgid);
                    if (messages != null && messages.size() == 0) {
                        Log.i("Classifier removing msgid=" + msgid);
                        msgids.remove(msgid);
                        dirty = true;
                    }
                }
            }

            if (dirty)
                save(context);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static synchronized void clear(@NonNull Context context) {
        accountMsgIds.clear();
        wordClassFrequency.clear();
        dirty = true;
        Log.i("Classifier data cleared");
    }

    static boolean isEnabled(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("classification", false);
    }

    static boolean canClassify(@NonNull String folderType) {
        return EntityFolder.INBOX.equals(folderType) ||
                EntityFolder.JUNK.equals(folderType) ||
                EntityFolder.USER.equals(folderType);
    }

    static File getFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "classifier.json");
    }

    @NonNull
    static JSONObject toJson() throws JSONException {
        JSONArray jmessages = new JSONArray();
        for (Long account : classMessages.keySet())
            for (String clazz : classMessages.get(account).keySet()) {
                JSONObject jmessage = new JSONObject();
                jmessage.put("account", account);
                jmessage.put("class", clazz);
                jmessage.put("count", classMessages.get(account).get(clazz));
                jmessages.put(jmessage);
            }

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
                    jword.put("count", f.count);
                    jword.put("dup", f.duplicates);
                    jword.put("before", from(f.before));
                    jword.put("after", from(f.after));
                    jwords.put(jword);
                }
            }

        JSONArray jclassified = new JSONArray();
        for (Long account : accountMsgIds.keySet()) {
            JSONObject jaccount = new JSONObject();
            jaccount.put("account", account);
            jaccount.put("messages", from(accountMsgIds.get(account)));
            jclassified.put(jaccount);
        }

        JSONObject jroot = new JSONObject();
        jroot.put("version", 2);
        jroot.put("messages", jmessages);
        jroot.put("words", jwords);
        jroot.put("classified", jclassified);

        return jroot;
    }

    @NonNull
    private static JSONArray from(@NonNull List<String> list) throws JSONException {
        JSONArray jlist = new JSONArray();
        for (String item : list)
            jlist.put(item);
        return jlist;
    }

    @NonNull
    private static JSONObject from(@NonNull Map<String, Integer> map) throws JSONException {
        JSONObject jmap = new JSONObject();
        for (String key : map.keySet())
            jmap.put(key, map.get(key));
        return jmap;
    }

    static void fromJson(@NonNull JSONObject jroot) throws JSONException {
        int version = jroot.optInt("version");
        if (version < 2)
            return;

        JSONArray jmessages = jroot.getJSONArray("messages");
        for (int m = 0; m < jmessages.length(); m++) {
            JSONObject jmessage = (JSONObject) jmessages.get(m);
            long account = jmessage.getLong("account");
            if (!classMessages.containsKey(account))
                classMessages.put(account, new HashMap<>());
            String clazz = jmessage.getString("class");
            int count = jmessage.getInt("count");
            classMessages.get(account).put(clazz, count);
        }

        JSONArray jwords = jroot.getJSONArray("words");
        for (int w = 0; w < jwords.length(); w++) {
            JSONObject jword = (JSONObject) jwords.get(w);
            long account = jword.getLong("account");
            if (!wordClassFrequency.containsKey(account))
                wordClassFrequency.put(account, new HashMap<>());
            if (jword.has("word")) {
                String word = jword.getString("word");
                Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(word);
                if (classFrequency == null) {
                    classFrequency = new HashMap<>();
                    wordClassFrequency.get(account).put(word, classFrequency);
                }
                Frequency f = new Frequency();
                f.count = jword.getInt("count");
                f.duplicates = jword.optInt("dup");
                if (jword.has("before"))
                    f.before = from(jword.getJSONObject("before"));
                if (jword.has("after"))
                    f.after = from(jword.getJSONObject("after"));
                classFrequency.put(jword.getString("class"), f);
            } else
                Log.w("No words account=" + account);
        }

        JSONArray jclassified = jroot.getJSONArray("classified");
        for (int a = 0; a < jclassified.length(); a++) {
            JSONObject jaccount = jclassified.getJSONObject(a);
            long account = jaccount.getLong("account");
            List<String> ids = accountMsgIds.get(account);
            if (ids == null) {
                ids = new ArrayList<>();
                accountMsgIds.put(account, ids);
            }
            JSONArray jids = jaccount.getJSONArray("messages");
            for (int h = 0; h < jids.length(); h++)
                ids.add(jids.getString(h));
        }
    }

    @NonNull
    private static Map<String, Integer> from(@NonNull JSONObject jmap) throws JSONException {
        Map<String, Integer> result = new HashMap<>(jmap.length());
        Iterator<String> iterator = jmap.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            result.put(key, jmap.getInt(key));
        }
        return result;
    }

    private static class State {
        private final List<String> words = new ArrayList<>();
        private final Map<String, Stat> classStats = new HashMap<>();
    }

    private static class Frequency {
        private int count = 0;
        private int duplicates = 0;
        private Map<String, Integer> before = new HashMap<>();
        private Map<String, Integer> after = new HashMap<>();

        private void add(String b, String a, int c, boolean duplicate) {
            if (count + c < 0)
                return;

            count += c;

            if (duplicate)
                duplicates += c;

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
        private int matchedWords = 0;
        private double totalFrequency = 0;
        private final List<String> words = new ArrayList<>();
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
