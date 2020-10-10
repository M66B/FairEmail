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

import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

class CharsetHelper {
    private static UniversalDetector detector = new UniversalDetector();

    private static final int SAMPLE_SIZE = 2 * 1024;

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

    static boolean isISO8859(String text) {
        // https://en.wikipedia.org/wiki/ISO/IEC_8859-1
        int c;
        byte[] octets = text.getBytes(StandardCharsets.ISO_8859_1);
        for (byte b : octets) {
            c = b & 0xFF;
            if (c < 32)
                return false;
            if (c >= 127 && c < 160)
                return false;
        }
        return true;
    }

    static boolean isISO2022JP(String text) {
        // https://en.wikipedia.org/wiki/ISO/IEC_2022
        // https://www.sljfaq.org/afaq/encodings.html#encodings-ISO-2022-JP

        try {
            Charset.forName("ISO-2022-JP");
        } catch (UnsupportedCharsetException ex) {
            return false;
        }

        int c;
        int escapes = 0;
        boolean escaped = false;
        boolean parenthesis = false;
        boolean dollar = false;
        byte[] octets = text.getBytes(StandardCharsets.ISO_8859_1);
        for (byte b : octets) {
            c = b & 0xFF;

            if (c > 0x7F)
                return false;

            if (escaped) {
                escaped = false;
                if (c == '(')
                    parenthesis = true;
                else if (c == '$')
                    dollar = true;
            } else if (parenthesis) {
                parenthesis = false;
                if (c == 'B' || c == 'J')
                    escapes++;
            } else if (dollar) {
                dollar = false;
                if (c == '@' || c == 'B')
                    escapes++;
            } else if (c == 0x1B)
                escaped = true;

            if (escapes >= 3)
                return true;
        }

        return false;
    }

    static Charset detect(String text) {
        try {
            byte[] sample = text.getBytes(StandardCharsets.ISO_8859_1);

            detector.handleData(sample, 0, Math.min(SAMPLE_SIZE, sample.length));
            detector.dataEnd();

            String detected = detector.getDetectedCharset();
            if (detected == null)
                return null;

            return Charset.forName(detected);
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        } finally {
            detector.reset();
        }
    }
}
