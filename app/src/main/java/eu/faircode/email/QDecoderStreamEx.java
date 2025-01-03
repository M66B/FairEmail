package eu.faircode.email;

/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2018-2025 by Marcel Bokhorst (M66B)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.QPDecoderStream;

import java.io.IOException;
import java.io.InputStream;

public class QDecoderStreamEx extends QPDecoderStream {
    private Byte pushed = null;

    public QDecoderStreamEx(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int c = (pushed == null ? in.read() : pushed);
        pushed = null;

        if (c == '_')
            return ' ';
        else if (c == '=') {
            ba[0] = (byte) in.read();
            ba[1] = (byte) in.read();
            try {
                return ASCIIUtility.parseInt(ba, 0, 2, 16);
            } catch (NumberFormatException ex) {
                Log.w(ex);
                pushed = ba[1];
                return ba[0];
            }
        } else
            return c;
    }
}

