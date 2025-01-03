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
import android.os.Build;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public class EncryptionHelper {
    static {
        /*
            Caused by: java.lang.SecurityException: Incorrect signature
              at org.apache.harmony.security.utils.JarUtils.verifySignature(JarUtils.java:223)
              at java.util.jar.JarVerifier.verifyCertificate(JarVerifier.java:294)
              at java.util.jar.JarVerifier.readCertificates(JarVerifier.java:268)
              at java.util.jar.JarFile.getInputStream(JarFile.java:380)
              at libcore.net.url.JarURLConnectionImpl.getInputStream(JarURLConnectionImpl.java:222)
              at java.net.URL.openStream(URL.java:470)
              at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:444)
              at java.lang.Class.getResourceAsStream(Class.java:1334)
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            try {
                Provider[] providers = Security.getProviders();
                for (int p = 0; p < providers.length; p++)
                    if (BouncyCastleProvider.PROVIDER_NAME.equals(providers[p].getName())) {
                        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
                        Provider bc = new BouncyCastleProvider();
                        Security.insertProviderAt(bc, p + 1);
                        Log.i("Replacing security provider " + providers[p] + " at " + p + " by " + bc);
                        break;
                    }
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    static void init(Context context) {

    }
}

