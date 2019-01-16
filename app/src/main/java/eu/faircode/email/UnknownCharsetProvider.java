package eu.faircode.email;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

public class UnknownCharsetProvider extends CharsetProvider {
    @Override
    public Iterator<Charset> charsets() {
        return Collections.emptyIterator();
    }

    @Override
    public Charset charsetForName(String name) {
        // x-binaryenc
        // UseInqueCodePage
        // none
        // unknown-8bit
        // https://javaee.github.io/javamail/FAQ#unsupen
        Log.w("Unknown charset=" + name);
        return Charset.forName("US-ASCII");
    }
}
