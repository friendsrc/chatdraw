package com.example.chatdraw.Activities;

/*
LinphoneService.java
Copyright (C) 2017 Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.linphone.core.Core;
import org.linphone.core.Factory;

public final class LinphoneService extends Service {
    private LinphoneManager mLinphoneManager;
    private static LinphoneService sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        LinphonePreferences.instance().setContext(this);
        Factory.instance().setLogCollectionPath(getFilesDir().getAbsolutePath());
        Factory.instance().setDebugMode(true, "Linphone");
        // You must provide the Android app context as createCore last param !
        Core core = Factory.instance().createCore(null, null, this);
        core.start();

        LinphonePreferences.instance().setContext(this);
        mLinphoneManager = new LinphoneManager(this);
        sInstance = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public LinphoneManager getLinphoneManager() {
        return mLinphoneManager;
    }

    // TODO IMPORTANT
    public static boolean isReady() {
        Log.v("TICKET","" + sInstance);
        return sInstance != null;
    }

    // TODO IMPORTANT
    public static LinphoneService instance() {
        if (isReady()) return sInstance;

        throw new RuntimeException("LinphoneService not instantiated yet");
    }
}
