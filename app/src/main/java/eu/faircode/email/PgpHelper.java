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

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class PgpHelper {
    private static final long CONNECT_TIMEOUT = 5000L;

    static OpenPgpServiceConnection getConnection(Context context) {
        return getConnection(context, CONNECT_TIMEOUT);
    }

    static OpenPgpServiceConnection getConnection(Context context, long timeout) {
        final String pkg = Helper.getOpenKeychainPackage(context);
        Log.i("PGP binding to " + pkg + " timeout=" + timeout);

        CountDownLatch latch = new CountDownLatch(1);
        OpenPgpServiceConnection pgpService = new OpenPgpServiceConnection(context, pkg,
                new OpenPgpServiceConnection.OnBound() {
                    @Override
                    public void onBound(IOpenPgpService2 service) {
                        Log.i("Bound to " + pkg);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.i(ex.getMessage());
                        latch.countDown();
                    }
                });
        pgpService.bindToService();

        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Log.w(ex);
        }

        Log.i("PGP bound=" + pgpService.isBound());

        return pgpService;
    }
}
