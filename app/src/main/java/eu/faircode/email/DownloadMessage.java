package eu.faircode.email;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import java.io.File;

class DownloadMessage {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long size;
    @NonNull
    public Boolean content = false;
    public String warning; // persistent

    File getFile(Context context) {
        File dir = new File(context.getFilesDir(), "messages");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id.toString());
    }
}
