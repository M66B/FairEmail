package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

public class JsonStream extends JsonWriter {

    private final ObjectJsonStreamer objectJsonStreamer;

    public interface Streamable {
        void toStream(@NonNull JsonStream stream) throws IOException;
    }

    private final Writer out;

    /**
     * Constructs a JSONStream
     *
     * @param out the writer
     */
    public JsonStream(@NonNull Writer out) {
        super(out);
        setSerializeNulls(false);
        this.out = out;
        objectJsonStreamer = new ObjectJsonStreamer();
    }

    JsonStream(@NonNull JsonStream stream, @NonNull ObjectJsonStreamer streamer) {
        super(stream.out);
        setSerializeNulls(stream.getSerializeNulls());
        this.out = stream.out;
        this.objectJsonStreamer = streamer;
    }

    // Allow chaining name().value()
    @NonNull
    public JsonStream name(@Nullable String name) throws IOException {
        super.name(name);
        return this;
    }

    /**
     * Serialises an arbitrary object as JSON, handling primitive types as well as
     * Collections, Maps, and arrays.
     */
    public void value(@Nullable Object object, boolean shouldRedactKeys) throws IOException {
        if (object instanceof Streamable) {
            ((Streamable) object).toStream(this);
        } else {
            objectJsonStreamer.objectToStream(object, this, shouldRedactKeys);
        }
    }

    /**
     * Serialises an arbitrary object as JSON, handling primitive types as well as
     * Collections, Maps, and arrays.
     */
    public void value(@Nullable Object object) throws IOException {
        if (object instanceof File) {
            value((File) object);
        } else {
            value(object, false);
        }
    }

    /**
     * Writes a File (its content) into the stream
     */
    public void value(@NonNull File file) throws IOException {
        if (file == null || file.length() <= 0) {
            return;
        }

        super.flush();
        beforeValue(); // add comma if in array

        // Copy the file contents onto the stream
        Reader input = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            input = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            IOUtils.copy(input, out);
        } finally {
            IOUtils.closeQuietly(input);
        }

        out.flush();
    }
}
