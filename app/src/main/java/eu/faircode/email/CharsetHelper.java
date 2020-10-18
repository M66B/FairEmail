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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class CharsetHelper {
    private static final int MAX_SAMPLE_SIZE = 8192;

    static {
        System.loadLibrary("compact_enc_det");
    }

    private static native String jni_detect(byte[] chars);

    static boolean isUTF8(String text) {
        // Get extended ASCII characters
        byte[] octets = text.getBytes(StandardCharsets.ISO_8859_1);

        int bytes;
        for (int i = 0; i < octets.length; i++) {
            if ((octets[i] & 0b10000000) == 0b00000000)
                bytes = 1;
            else if ((octets[i] & 0b11100000) == 0b11000000)
                bytes = 2;
            else if ((octets[i] & 0b11110000) == 0b11100000)
                bytes = 3;
            else if ((octets[i] & 0b11111000) == 0b11110000)
                bytes = 4;
            else if ((octets[i] & 0b11111100) == 0b11111000)
                bytes = 5;
            else if ((octets[i] & 0b11111110) == 0b11111100)
                bytes = 6;
            else
                return false;

            if (i + bytes > octets.length)
                return false;

            while (--bytes > 0)
                if ((octets[++i] & 0b11000000) != 0b10000000)
                    return false;
        }

        return true;
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
            String detected = jni_detect(sample);
            if ("US-ASCII".equals(detected) ||
                    "ISO-8859-1".equals(detected) ||
                    "UTF-8".equals(detected))
                Log.w("compact_enc_det result=" + detected);
            else
                // ISO-2022-JP, ISO-8859-2, windows-1250, windows-1257
                Log.e("compact_enc_det result=" + detected);

            return Charset.forName(detected);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }
}
