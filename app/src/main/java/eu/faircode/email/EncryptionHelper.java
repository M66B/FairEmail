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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public class EncryptionHelper {
    static {
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

