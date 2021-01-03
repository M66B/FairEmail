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
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageClassifier {
    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final Map<Long, Map<String, Integer>> classMessages = new HashMap<>();
    private static final Map<Long, Map<String, Map<String, Integer>>> wordClassFrequency = new HashMap<>();

    private static final int MIN_MATCHED_WORDS = 10;
    private static final double COMMON_WORD_FACTOR = 0.75;
    private static final double CHANCE_THRESHOLD = 2.0;

    static void classify(EntityMessage message, EntityFolder target, Context context) {
        try {
            if (!isEnabled(context))
                return;

            if (target != null && !canClassify(target.type))
                return;

            DB db = DB.getInstance(context);

            EntityFolder folder = db.folder().getFolder(message.folder);
            if (folder == null)
                return;

            EntityAccount account = db.account().getAccount(folder.account);
            if (account == null)
                return;

            if (!canClassify(folder.type))
                return;

            File file = message.getFile(context);
            if (!file.exists())
                return;

            String text = HtmlHelper.getFullText(file);
            if (TextUtils.isEmpty(text))
                return;

            load(context);

            if (!classMessages.containsKey(account.id))
                classMessages.put(account.id, new HashMap<>());
            if (!wordClassFrequency.containsKey(account.id))
                wordClassFrequency.put(account.id, new HashMap<>());

            String classified = classify(account.id, folder.name, text, target == null, context);

            EntityLog.log(context, "Classifier" +
                    " folder=" + folder.name +
                    " message=" + message.id +
                    "@" + new Date(message.received) +
                    ":" + message.subject +
                    " class=" + classified);

            Integer m = classMessages.get(account.id).get(folder.name);
            if (target == null) {
                m = (m == null ? 1 : m + 1);
                classMessages.get(account.id).put(folder.name, m);
            } else {
                if (m != null && m > 0)
                    classMessages.get(account.id).put(folder.name, m - 1);
            }
            Log.i("Classifier classify=" + folder.name + " messages=" + classMessages.get(account.id).get(folder.name));

            dirty = true;

            if (classified != null) {
                EntityFolder f = db.folder().getFolderByName(account.id, classified);
                if (f != null && !f.id.equals(folder.id) && f.auto_classify) {
                    EntityOperation.queue(context, message, EntityOperation.MOVE, f.id, false);
                    message.ui_hide = true;
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static String classify(long account, String classify, String text, boolean added, Context context) {
        int maxFrequency = 0;
        int maxMatchedWords = 0;
        List<String> words = new ArrayList<>();
        Map<String, Stat> classStats = new HashMap<>();

        BreakIterator boundary = BreakIterator.getWordInstance(); // TODO ICU
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; end = boundary.next()) {
            String word = text.substring(start, end).toLowerCase();
            if (word.length() > 1 &&
                    !words.contains(word) &&
                    !word.matches(".*\\d.*")) {
                words.add(word);

                Map<String, Integer> classFrequency = wordClassFrequency.get(account).get(word);
                if (added) {
                    if (classFrequency == null) {
                        classFrequency = new HashMap<>();
                        wordClassFrequency.get(account).put(word, classFrequency);
                    }

                    // Filter classes of common occurring words
                    List<String> applyClasses = new ArrayList<>(classFrequency.keySet());
                    for (String class1 : classFrequency.keySet())
                        for (String class2 : classFrequency.keySet())
                            if (!class1.equals(class2)) {
                                double percentage1 = (double) classFrequency.get(class1) / classMessages.get(account).get(class1);
                                double percentage2 = (double) classFrequency.get(class2) / classMessages.get(account).get(class2);
                                double factor = percentage1 / percentage2;
                                if (factor > 1)
                                    factor = 1 / factor;
                                if (factor > COMMON_WORD_FACTOR) {
                                    Log.i("Classifier skip class=" + class1 + " word=" + word);
                                    applyClasses.remove(class1);
                                    break;
                                }
                            }

                    for (String clazz : applyClasses) {
                        int frequency = classFrequency.get(clazz);
                        if (frequency > maxFrequency)
                            maxFrequency = frequency;

                        Stat stat = classStats.get(clazz);
                        if (stat == null) {
                            stat = new Stat();
                            classStats.put(clazz, stat);
                        }

                        stat.matchedWords++;
                        stat.totalFrequency += frequency;

                        if (stat.matchedWords > maxMatchedWords)
                            maxMatchedWords = stat.matchedWords;
                    }

                    Integer c = classFrequency.get(classify);
                    c = (c == null ? 1 : c + 1);
                    classFrequency.put(classify, c);
                } else {
                    Integer c = (classFrequency == null ? null : classFrequency.get(classify));
                    if (c != null)
                        if (c > 0)
                            classFrequency.put(classify, c - 1);
                        else
                            classFrequency.remove(classify);
                }
            }
            start = end;
        }

        if (!added)
            return null;

        List<Chance> chances = new ArrayList<>();
        for (String clazz : classStats.keySet()) {
            Stat stat = classStats.get(clazz);
            double chance = ((double) stat.totalFrequency / maxFrequency / maxMatchedWords);
            Chance c = new Chance(clazz, chance);
            EntityLog.log(context, "Classifier " + c +
                    " frequency=" + stat.totalFrequency + "/" + maxFrequency +
                    " matched=" + stat.matchedWords + "/" + maxMatchedWords);
            chances.add(c);
        }

        if (chances.size() <= 1 || maxMatchedWords < MIN_MATCHED_WORDS)
            return null;

        Collections.sort(chances, new Comparator<Chance>() {
            @Override
            public int compare(Chance c1, Chance c2) {
                return -c1.chance.compareTo(c2.chance);
            }
        });

        String classification = null;
        if (chances.get(0).chance / chances.get(1).chance >= CHANCE_THRESHOLD)
            classification = chances.get(0).clazz;

        Log.i("Classifier classify=" + classify + " classified=" + classification);

        return classification;
    }

    static synchronized void save(Context context) throws JSONException, IOException {
        if (!dirty)
            return;
        if (!isEnabled(context))
            return;

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
        for (Long account : classMessages.keySet())
            for (String word : wordClassFrequency.get(account).keySet()) {
                Map<String, Integer> classFrequency = wordClassFrequency.get(account).get(word);
                for (String clazz : classFrequency.keySet()) {
                    JSONObject jword = new JSONObject();
                    jword.put("account", account);
                    jword.put("word", word);
                    jword.put("class", clazz);
                    jword.put("frequency", classFrequency.get(clazz));
                    jwords.put(jword);
                }
            }

        JSONObject jroot = new JSONObject();
        jroot.put("messages", jmessages);
        jroot.put("words", jwords);

        File file = getFile(context);
        Helper.writeText(file, jroot.toString(2));

        dirty = false;

        Log.i("Classifier saved");
    }

    private static synchronized void load(Context context) throws IOException, JSONException {
        if (loaded)
            return;

        if (!isEnabled(context))
            return;

        classMessages.clear();
        wordClassFrequency.clear();

        File file = getFile(context);
        if (file.exists()) {
            String json = Helper.readText(file);
            JSONObject jroot = new JSONObject(json);

            JSONArray jmessages = jroot.getJSONArray("messages");
            for (int m = 0; m < jmessages.length(); m++) {
                JSONObject jmessage = (JSONObject) jmessages.get(m);
                long account = jmessage.getLong("account");
                if (!classMessages.containsKey(account))
                    classMessages.put(account, new HashMap<>());
                classMessages.get(account).put(jmessage.getString("class"), jmessage.getInt("count"));
            }

            JSONArray jwords = jroot.getJSONArray("words");
            for (int w = 0; w < jwords.length(); w++) {
                JSONObject jword = (JSONObject) jwords.get(w);
                long account = jword.getLong("account");
                if (!wordClassFrequency.containsKey(account))
                    wordClassFrequency.put(account, new HashMap<>());
                String word = jword.getString("word");
                Map<String, Integer> classFrequency = wordClassFrequency.get(account).get(word);
                if (classFrequency == null) {
                    classFrequency = new HashMap<>();
                    wordClassFrequency.get(account).put(word, classFrequency);
                }
                classFrequency.put(jword.getString("class"), jword.getInt("frequency"));
            }
        }

        loaded = true;
        Log.i("Classifier loaded");
    }

    static boolean isEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("experiments", false);
    }

    static boolean canClassify(String folderType) {
        return EntityFolder.INBOX.equals(folderType) ||
                EntityFolder.JUNK.equals(folderType) ||
                EntityFolder.USER.equals(folderType);
    }

    static File getFile(Context context) {
        return new File(context.getFilesDir(), "classifier.json");
    }

    private static class Stat {
        int matchedWords = 0;
        int totalFrequency = 0;
    }

    private static class Chance {
        String clazz;
        Double chance;

        Chance(String clazz, Double chance) {
            this.clazz = clazz;
            this.chance = chance;
        }

        @NotNull
        @Override
        public String toString() {
            return clazz + "=" + chance;
        }
    }
}
