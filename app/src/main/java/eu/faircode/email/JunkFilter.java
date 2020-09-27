package eu.faircode.email;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;

public class JunkFilter {
    private static final Classifier<String, String> bayes = new BayesClassifier<>();

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "junk");

    static void classify(Context context, String html, String folderType) {
        if (EntityFolder.isOutgoing(folderType) ||
                EntityFolder.ARCHIVE.equals(folderType))
            return;

        final boolean junk = EntityFolder.JUNK.equals(folderType);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String text = HtmlHelper.getText(context, html);
                    List<String> words = Arrays.asList(text.split("[^\\p{L}\\p{N}'`]+"));

                    Classification<String, String> classification = bayes.classify(words);
                    Log.i("MMM folder=" + folderType + " category=" + (classification == null ? null : classification.getCategory()));

                    bayes.learn(junk ? "junk" : "ham", words);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static void save(Context context) {
        final File file = new File(context.getFilesDir(), "junk.filter");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static void load(Context context) {
        final File file = new File(context.getFilesDir(), "junk.filter");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (!file.exists())
                    return;
                try (FileInputStream fis = new FileInputStream(file)) {
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }
}
