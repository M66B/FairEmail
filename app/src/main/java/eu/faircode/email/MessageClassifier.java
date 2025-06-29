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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class MessageClassifier {
    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final Map<Long, List<String>> accountMsgIds = new HashMap<>();
    private static final Map<Long, Map<String, Integer>> classMessages = new HashMap<>();
    private static final Map<Long, Map<Integer, Map<String, Frequency>>> wordClassFrequency = new HashMap<>();
    private static final Map<String, Integer> wordIndex = new LinkedHashMap<>();

    private static final int VERSION = 4;
    private static final int MAX_WORDS = 1000;

    static synchronized void classify(EntityMessage message, EntityFolder folder, boolean added, Context context) {
        try {
            if (!isEnabled(context))
                return;

            if (!folder.auto_classify_source)
                return;

            if (message.ui_hide)
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
            String classified = classify(message, folder.name, texts, added, context);

            boolean notJunk = message.isNotJunk(context);

            long elapsed = new Date().getTime() - start;
            EntityLog.log(context, EntityLog.Type.Classification, message,
                    "Classifier" +
                            " folder=" + folder.account + ":" + folder.name + ":" + folder.type +
                            " added=" + added +
                            " message=" + message.id + "/" + !TextUtils.isEmpty(message.msgid) +
                            " keyword=" + message.hasKeyword(MessageHelper.FLAG_CLASSIFIED) +
                            " filtered=" + message.hasKeyword(MessageHelper.FLAG_FILTERED) +
                            " notJunk=" + notJunk +
                            "@" + new Date(message.received) +
                            ":" + message.subject +
                            " class=" + classified +
                            " re=" + message.auto_classified +
                            " elapsed=" + elapsed);

            // Auto classify message
            if (classified != null &&
                    !classified.equals(folder.name) &&
                    !TextUtils.isEmpty(message.msgid) &&
                    !message.hasKeyword(MessageHelper.FLAG_CLASSIFIED) &&
                    (!message.hasKeyword(MessageHelper.FLAG_FILTERED) || BuildConfig.DEBUG) &&
                    !accountMsgIds.get(folder.account).contains(message.msgid) &&
                    !EntityFolder.JUNK.equals(folder.type)) {
                boolean pro = ActivityBilling.isPro(context);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder dest = db.folder().getFolderByName(folder.account, classified);
                    if (dest != null && dest.auto_classify_target &&
                            (pro || EntityFolder.JUNK.equals(dest.type)) &&
                            (!EntityFolder.JUNK.equals(dest.type) || !notJunk)) {
                        EntityOperation.queue(context, message, EntityOperation.KEYWORD, MessageHelper.FLAG_CLASSIFIED, true);
                        EntityOperation.queue(context, message, EntityOperation.MOVE, dest.id, false, true);
                        message.ui_hide = true;
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                //if (message.ui_hide)
                //    accountMsgIds.get(folder.account).add(message.msgid);
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
        if (message.return_path != null)
            addresses.addAll(Arrays.asList(message.return_path));

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

        String text = HtmlHelper.getFullText(context, file);
        if (text != null)
            texts.add(text);

        return texts;
    }

    private static String classify(EntityMessage message, @NonNull String currentClass, @NonNull List<String> texts, boolean added, @NonNull Context context) {
        State state = new State();

        // Check classes
        DB db = DB.getInstance(context);
        for (String clazz : new ArrayList<>(classMessages.get(message.account).keySet())) {
            EntityFolder folder = db.folder().getFolderByName(message.account, clazz);
            if (folder == null || !folder.auto_classify_source) {
                EntityLog.log(context, EntityLog.Type.Classification, message,
                        "Classifier deleting folder" +
                                " class=" + message.account + ":" + clazz +
                                " exists=" + (folder != null));
                classMessages.get(message.account).remove(clazz);
                for (int word : wordClassFrequency.get(message.account).keySet())
                    wordClassFrequency.get(message.account).get(word).remove(clazz);
            }
        }

        Log.i("Classifier texts=" + texts.size());
        for (String text : texts) {
            // First word
            processWord(message.account, added, null, state);

            // Process words
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                java.text.BreakIterator boundary = java.text.BreakIterator.getWordInstance();
                boundary.setText(text);
                int start = boundary.first();
                for (int end = boundary.next(); end != java.text.BreakIterator.DONE; end = boundary.next()) {
                    String word = text.substring(start, end);
                    processWord(message.account, added, word, state);
                    if (state.words.size() >= MAX_WORDS)
                        break;
                    start = end;
                }
            } else {
                // The ICU break iterator works better for Chinese texts
                android.icu.text.BreakIterator boundary = android.icu.text.BreakIterator.getWordInstance();
                boundary.setText(text);
                int start = boundary.first();
                for (int end = boundary.next(); end != android.icu.text.BreakIterator.DONE; end = boundary.next()) {
                    String word = text.substring(start, end);
                    processWord(message.account, added, word, state);
                    if (state.words.size() >= MAX_WORDS)
                        break;
                    start = end;
                }
            }
        }

        // final word
        processWord(message.account, added, null, state);

        int maxMessages = 0;
        for (String clazz : classMessages.get(message.account).keySet()) {
            int count = classMessages.get(message.account).get(clazz);
            if (count > maxMessages)
                maxMessages = count;
        }

        updateFrequencies(message.account, currentClass, added, state);

        if (maxMessages == 0) {
            Log.i("Classifier no messages account=" + message.account);
            return null;
        }

        if (!added)
            return null;

        // Calculate chance per class
        int words = state.words.size() - texts.size() - 1;
        List<Chance> chances = new ArrayList<>();
        for (String clazz : state.classStats.keySet()) {
            Stat stat = state.classStats.get(clazz);

            double chance = stat.totalFrequency / maxMessages / words;
            Chance c = new Chance(clazz, chance);
            chances.add(c);
            EntityLog.log(context, EntityLog.Type.Classification, message,
                    "Classifier " + c +
                            " frequency=" + (Math.round(stat.totalFrequency * 100.0) / 100.0) + "/" + maxMessages + " msgs" +
                            " matched=" + stat.matchedWords + "/" + words + " words" +
                            " text=" + TextUtils.join(", ", stat.words));
        }

        if (BuildConfig.DEBUG) {
            StringBuilder sb = new StringBuilder();
            for (Integer word : state.words) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(getWord(word));
            }
            Log.i("Classifier words=" + state.words.size() + " " + sb);
        }

        // Sort classes by chance
        Collections.sort(chances, new Comparator<Chance>() {
            @Override
            public int compare(Chance c1, Chance c2) {
                return -c1.chance.compareTo(c2.chance);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double class_min_chance = prefs.getInt("class_min_probability", 15) / 100.0;
        double class_min_difference = prefs.getInt("class_min_difference", 50) / 100.0;

        // Special case: pick first best target class
        if (class_min_difference == 0) {
            for (Chance chance : chances)
                if (chance.chance > class_min_chance) {
                    EntityFolder target = db.folder().getFolderByName(message.account, chance.clazz);
                    if (target != null && target.auto_classify_target) {
                        Log.i("Classifier current=" + currentClass + " classified=" + chance.clazz);
                        return chance.clazz;
                    }
                }
            return null;
        }

        if (chances.size() <= 1)
            return null;

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

        _processWord(account, added, word == null ? null : getWordIndex(word), state);
    }

    private static void _processWord(long account, boolean added, Integer word, State state) {

        if (word != null ||
                state.words.size() == 0 ||
                state.words.get(state.words.size() - 1) != null)
            state.words.add(word);

        if (!added)
            return;

        if (state.words.size() < 3)
            return;

        Integer before = state.words.get(state.words.size() - 3);
        Integer current = state.words.get(state.words.size() - 2);
        Integer after = state.words.get(state.words.size() - 1);

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

            if (BuildConfig.DEBUG && false)
                stat.words.add(current + "=" + f);
        }
    }

    private static int getWordIndex(String word) {
        Integer index = wordIndex.get(word);
        if (index == null) {
            index = wordIndex.size();
            wordIndex.put(word, index);
        }
        return index;
    }

    private static String getWord(Integer index) {
        if (index == null)
            return "<null>";
        if (index < 0 || index >= wordIndex.size())
            return "<" + index + ">";
        return new ArrayList<>(wordIndex.keySet()).get(index);
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
            Integer before = state.words.get(i - 1);
            Integer current = state.words.get(i);
            Integer after = state.words.get(i + 1);

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

    static synchronized void save(@NonNull Context context) throws IOException {
        if (!dirty)
            return;

        long start = new Date().getTime();

        reduce();

        File file = getFile(context, false);
        File backup = getFile(context, true);
        Helper.secureDelete(backup);
        if (file.exists())
            file.renameTo(backup);

        Log.i("Classifier save " + file);
        try (JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(file)))) {
            writer.beginObject();

            Log.i("Classifier write version=" + VERSION);
            writer.name("version").value(VERSION);

            writer.name("messages");
            writer.beginArray();
            for (Long account : classMessages.keySet())
                for (String clazz : classMessages.get(account).keySet()) {
                    writer.beginObject();
                    writer.name("account").value(account);
                    writer.name("class").value(clazz);
                    writer.name("count").value(classMessages.get(account).get(clazz));
                    writer.endObject();
                }
            writer.endArray();

            writer.name("words");
            writer.beginArray();
            for (Long account : wordClassFrequency.keySet())
                for (int word : wordClassFrequency.get(account).keySet()) {
                    Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(word);
                    for (String clazz : classFrequency.keySet()) {
                        Frequency f = classFrequency.get(clazz);
                        writer.beginObject();

                        writer.name("account").value(account);
                        writer.name("word").value(word);
                        writer.name("class").value(clazz);
                        writer.name("count").value(f.count);
                        writer.name("dup").value(f.duplicates);

                        writer.name("before");
                        writer.beginObject();
                        for (int key : f.before.keySet())
                            writer.name(Integer.toString(key)).value(f.before.get(key));
                        writer.endObject();

                        writer.name("after");
                        writer.beginObject();
                        for (int key : f.after.keySet())
                            writer.name(Integer.toString(key)).value(f.after.get(key));
                        writer.endObject();

                        writer.endObject();
                    }
                }
            writer.endArray();

            writer.name("list");
            writer.beginArray();
            for (String word : wordIndex.keySet())
                writer.value(word);
            writer.endArray();

            writer.name("classified");
            writer.beginArray();
            for (Long account : accountMsgIds.keySet()) {
                writer.beginObject();
                writer.name("account").value(account);
                writer.name("messages");
                writer.beginArray();
                for (String msgid : accountMsgIds.get(account))
                    writer.value(msgid);
                writer.endArray();
                writer.endObject();
            }
            writer.endArray();

            writer.endObject();
        }

        Helper.secureDelete(backup);

        dirty = false;

        long elapsed = new Date().getTime() - start;
        Log.i("Classifier data saved elapsed=" + elapsed + " size=" + file.length());
    }

    private static synchronized void load(@NonNull Context context) {
        if (loaded || dirty)
            return;

        clear(context);
        File file = getFile(context, false);
        File backup = getFile(context, true);
        if (backup.exists())
            file = backup;
        try {
            _load(file);
        } catch (Throwable ex) {
            Log.e(ex);
            Helper.secureDelete(file);
            clear(context);
        }
    }

    private static synchronized void _load(File file) throws IOException {
        Log.i("Classifier read " + file);
        long start = new Date().getTime();
        int version = 0;
        if (file.exists())
            try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(file)))) {
                reader.beginObject();
                while (reader.hasNext())
                    switch (reader.nextName()) {
                        case "version":
                            version = reader.nextInt();
                            Log.i("Classifier read version=" + version);
                            break;

                        case "messages":
                            reader.beginArray();
                            while (reader.hasNext()) {
                                Long account = null;
                                String clazz = null;
                                Integer count = null;

                                reader.beginObject();
                                while (reader.hasNext())
                                    switch (reader.nextName()) {
                                        case "account":
                                            account = reader.nextLong();
                                            break;
                                        case "class":
                                            clazz = reader.nextString();
                                            break;
                                        case "count":
                                            count = reader.nextInt();
                                            break;
                                    }
                                reader.endObject();

                                if (account == null || clazz == null || count == null)
                                    continue;

                                if (!classMessages.containsKey(account))
                                    classMessages.put(account, new HashMap<>());
                                classMessages.get(account).put(clazz, count);
                            }
                            reader.endArray();
                            break;

                        case "words":
                            reader.beginArray();
                            while (reader.hasNext()) {
                                Long account = null;
                                Integer word = null;
                                String clazz = null;
                                Frequency f = new Frequency();

                                reader.beginObject();
                                while (reader.hasNext())
                                    switch (reader.nextName()) {
                                        case "account":
                                            account = reader.nextLong();
                                            break;
                                        case "word":
                                            if (version > 3)
                                                word = Integer.parseInt(reader.nextString());
                                            else
                                                word = getWordIndex(reader.nextString());
                                            break;
                                        case "class":
                                            clazz = reader.nextString();
                                            break;
                                        case "count":
                                            f.count = reader.nextInt();
                                            break;
                                        case "dup":
                                            f.duplicates = reader.nextInt();
                                            break;
                                        case "before":
                                            reader.beginObject();
                                            while (reader.hasNext()) {
                                                int b = (version > 3
                                                        ? Integer.parseInt(reader.nextName())
                                                        : getWordIndex(reader.nextName()));
                                                f.before.put(b, reader.nextInt());
                                            }
                                            reader.endObject();
                                            break;
                                        case "after":
                                            reader.beginObject();
                                            while (reader.hasNext()) {
                                                int a = (version > 3
                                                        ? Integer.parseInt(reader.nextName())
                                                        : getWordIndex(reader.nextName()));
                                                f.after.put(a, reader.nextInt());
                                            }
                                            reader.endObject();
                                            break;
                                    }
                                reader.endObject();

                                if (account == null || word == null || clazz == null)
                                    continue;

                                if (!wordClassFrequency.containsKey(account))
                                    wordClassFrequency.put(account, new HashMap<>());

                                Map<String, Frequency> classFrequency = wordClassFrequency.get(account).get(word);
                                if (classFrequency == null) {
                                    classFrequency = new HashMap<>();
                                    wordClassFrequency.get(account).put(word, classFrequency);
                                }

                                classFrequency.put(clazz, f);
                            }
                            reader.endArray();
                            break;

                        case "list":
                            reader.beginArray();
                            while (reader.hasNext())
                                wordIndex.put(reader.nextString(), wordIndex.size());
                            reader.endArray();
                            break;

                        case "classified":
                            reader.beginArray();
                            while (reader.hasNext()) {
                                Long account = null;
                                List<String> msgids = new ArrayList<>();

                                reader.beginObject();
                                while (reader.hasNext())
                                    switch (reader.nextName()) {
                                        case "account":
                                            account = reader.nextLong();
                                            break;
                                        case "messages":
                                            reader.beginArray();
                                            while (reader.hasNext())
                                                msgids.add(reader.nextString());
                                            reader.endArray();
                                            break;
                                    }
                                reader.endObject();

                                if (account == null)
                                    continue;

                                accountMsgIds.put(account, msgids);
                            }
                            reader.endArray();
                            break;
                    }
                reader.endObject();
            }

        reduce();

        loaded = true;
        dirty = false;

        long elapsed = new Date().getTime() - start;
        Log.i("Classifier data loaded elapsed=" + elapsed + " words=" + wordIndex.size());
    }

    private static void reduce() {
        Log.i("Classifier reduce");
        for (long account : wordClassFrequency.keySet()) {
            Map<String, Integer> max = new HashMap<>();
            Map<String, Long> total = new HashMap<>();
            Map<String, Integer> count = new HashMap<>();

            for (int word : wordClassFrequency.get(account).keySet())
                for (String clazz : wordClassFrequency.get(account).get(word).keySet()) {
                    int f = wordClassFrequency.get(account).get(word).get(clazz).count;

                    Integer m = max.get(clazz);
                    if (m == null || f > m)
                        max.put(clazz, f);

                    if (!total.containsKey(clazz))
                        total.put(clazz, 0L);
                    total.put(clazz, total.get(clazz) + f);

                    if (!count.containsKey(clazz))
                        count.put(clazz, 0);
                    count.put(clazz, count.get(clazz) + 1);
                }

            for (String clazz : max.keySet())
                Log.i("Classifier max " + account + ":" + clazz + "=" + max.get(clazz));

            int dropped = 0;
            for (int word : wordClassFrequency.get(account).keySet())
                for (String clazz : new ArrayList<>(wordClassFrequency.get(account).get(word).keySet())) {
                    long m = max.get(clazz);
                    long avg = total.get(clazz) / count.get(clazz);
                    Frequency freq = wordClassFrequency.get(account).get(word).get(clazz);
                    if (freq.count < m / 1000) {
                        dropped++;
                        wordClassFrequency.get(account).get(word).remove(clazz);
                    } else if (freq.count < avg / 2 && false) {
                        dropped++;
                        Log.i("Classifier dropping account=" + account +
                                " word=" + word + " class=" + clazz + " freq=" + freq.count + " avg=" + avg);
                    }
                }
            Log.i("Classifier dropped words=" + dropped);

            // Source 47 MB

            // max/10 = 3 MB
            // max/20 = 4.4 MB
            // max/50 = 6.5 MB
            // max/100 = 6.5 MB
            // max/200 = 11.5 MB
            // max/500 = 15 MB
            // max/1000 = 18 MB
            // max/2000 = 22 MB
            // max/5000 = 26 MB

            // avg/1 = 21.3
            // avg/2 = 25.5
            // avg/3 = 29.0
            // avg/5 = 34.6

            // ba/5  = 27.2
            // ba/10 = 29.3
            // ba/20 = 31.5

            // avg/2 + ba/20 = 10 MB
        }
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
        classMessages.clear();
        wordClassFrequency.clear();
        wordIndex.clear();
        dirty = true;
        Log.i("Classifier data cleared");
    }

    static boolean isEnabled(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("classification", false);
    }

    static File getFile(@NonNull Context context, boolean backup) {
        return new File(context.getFilesDir(),
                backup ? "classifier.backup" : "classifier.json");
    }

    static long getSize(Context context) {
        try {
            return getFile(context, false).length();
        } catch (Throwable ignored) {
            return -1L;
        }
    }

    private static class State {
        private final List<Integer> words = new ArrayList<>();
        private final Map<String, Stat> classStats = new HashMap<>();
    }

    private static class Frequency {
        private int count = 0;
        private int duplicates = 0;
        private Map<Integer, Integer> before = new HashMap<>();
        private Map<Integer, Integer> after = new HashMap<>();

        private void add(Integer b, Integer a, int c, boolean duplicate) {
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

        @NonNull
        @Override
        public String toString() {
            return clazz + "=" + Math.round(chance * 100.0 * 100.0) / 100.0 + "%";
        }
    }
}
