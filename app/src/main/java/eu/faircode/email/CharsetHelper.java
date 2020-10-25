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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class CharsetHelper {
    private static final int MAX_SAMPLE_SIZE = 8192;
    private static String CHINESE = new Locale("zh").getLanguage();
    private static final List<String> COMMON = Collections.unmodifiableList(Arrays.asList(
            "US-ASCII", "ISO-8859-1", "ISO-8859-2", "windows-1250", "windows-1252", "windows-1257", "UTF-8"
    ));

    static {
        System.loadLibrary("compact_enc_det");
    }

    private static native DetectResult jni_detect(byte[] octets);

    static boolean isUTF8(String text) {
        // Get extended ASCII characters
        byte[] octets = text.getBytes(StandardCharsets.ISO_8859_1);

        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            utf8Decoder.decode(ByteBuffer.wrap(octets));
            return true;
        } catch (CharacterCodingException ex) {
            Log.w(ex);
            return false;
        }
    }

    static Charset detect(String text) {
        try {
            byte[] octets = text.getBytes(StandardCharsets.ISO_8859_1);

            byte[] sample;
            if (octets.length < MAX_SAMPLE_SIZE)
                sample = octets;
            else {
                sample = new byte[MAX_SAMPLE_SIZE];
                System.arraycopy(octets, 0, sample, 0, MAX_SAMPLE_SIZE);
            }

            Log.i("compact_enc_det sample=" + sample.length);
            DetectResult detected = jni_detect(sample);

            if (TextUtils.isEmpty(detected.charset)) {
                Log.e("compact_enc_det result=" + detected);
                return null;
            } else if (!BuildConfig.PLAY_STORE_RELEASE &&
                    COMMON.contains(detected.charset))
                Log.w("compact_enc_det result=" + detected);
            else if ("GB18030".equals(detected.charset) &&
                    !Locale.getDefault().getLanguage().equals(CHINESE)) {
                // https://github.com/google/compact_enc_det/issues/8
                Log.e("compact_enc_det result=" + detected);
                return null;
            } else
                Log.e("compact_enc_det result=" + detected);

            return Charset.forName(detected.charset);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    private static class DetectResult {
        String charset;
        int sample_size;
        int bytes_consumed;
        boolean is_reliable;

        DetectResult(String charset, int sample_size, int bytes_consumed, boolean is_reliable) {
            this.charset = charset;
            this.sample_size = sample_size;
            this.bytes_consumed = bytes_consumed;
            this.is_reliable = is_reliable;
        }

        @Override
        public String toString() {
            return charset + " s=" + bytes_consumed + "/" + sample_size + " r=" + is_reliable;
        }
    }
}
