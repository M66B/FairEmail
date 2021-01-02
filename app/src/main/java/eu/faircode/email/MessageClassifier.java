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
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageClassifier {
    private static Map<String, Integer> classMessages = new HashMap<>();
    private static Map<String, Map<String, Integer>> wordClassFrequency = new HashMap<>();

    private static final double COMMON_WORD_FACTOR = 0.75;
    private static final double CHANCE_THRESHOLD = 2.0;

    static String classify(EntityMessage message, boolean added, Context context) {
        DB db = DB.getInstance(context);

        if (!message.content)
            throw new IllegalArgumentException("Message without content");

        EntityFolder folder = db.folder().getFolder(message.folder);
        if (folder == null)
            return null;

        EntityAccount account = db.account().getAccount(folder.account);
        if (account == null)
            return null;

        if (!EntityFolder.INBOX.equals(folder.type) &&
                !EntityFolder.JUNK.equals(folder.type) &&
                !EntityFolder.USER.equals(folder.type) &&
                !(EntityFolder.ARCHIVE.equals(folder.type) && !account.isGmail()))
            return null;

        File file = message.getFile(context);
        String text;
        try {
            text = HtmlHelper.getFullText(file);
        } catch (IOException ex) {
            Log.w(ex);
            text = null;
        }

        if (TextUtils.isEmpty(text))
            return null;

        String classified = classify(folder.name, text, added);

        Integer m = classMessages.get(folder.name);
        if (added) {
            m = (m == null ? 1 : m + 1);
            classMessages.put(folder.name, m);
        } else {
            if (m != null)
                classMessages.put(folder.name, m - 1);
        }

        return classified;
    }

    static String classify(String classify, String text, boolean added) {
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

                Map<String, Integer> classFrequency = wordClassFrequency.get(word);
                if (!added) {
                    Integer c = (classFrequency == null ? null : classFrequency.get(classify));
                    if (c != null)
                        classFrequency.put(classify, c - 1);
                    continue;
                }

                if (classFrequency == null) {
                    classFrequency = new HashMap<>();
                    wordClassFrequency.put(word, classFrequency);
                }

                // Filter classes of common occurring words
                List<String> applyClasses = new ArrayList<>(classFrequency.keySet());
                for (String class1 : classFrequency.keySet())
                    for (String class2 : classFrequency.keySet())
                        if (!class1.equals(class2)) {
                            double percentage1 = (double) classFrequency.get(class1) / classMessages.get(class1);
                            double percentage2 = (double) classFrequency.get(class2) / classMessages.get(class2);
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
            Log.i("Classifier " + c +
                    " frequency=" + stat.totalFrequency + "/" + maxFrequency +
                    " matched=" + stat.matchedWords + "/" + maxMatchedWords);
            chances.add(c);
        }

        if (chances.size() <= 1)
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
