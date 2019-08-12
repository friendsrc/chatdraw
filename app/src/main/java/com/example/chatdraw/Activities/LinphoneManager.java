package com.example.chatdraw.Activities;

/*
LinphoneManager.java
Copyright (C) 2018 Belledonne Communications, Grenoble, France

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

import android.content.Context;
import android.util.Log;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.tools.H264Helper;

import java.util.Timer;
import java.util.TimerTask;

/** Handles Linphone's Core lifecycle */
public class LinphoneManager {
    private final String mBasePath;
    private final Context mContext;
    private Core mCore;
    private Runnable mIterateRunnable;
    private Timer mTimer;
    private final LinphonePreferences mPrefs;
    private CoreListenerStub mCoreListener;

    private boolean mExited;

    public LinphoneManager(Context c) {
        Log.v("TICKET","6");
        mExited = false;
        mContext = c;
        mBasePath = c.getFilesDir().getAbsolutePath();
        mPrefs = LinphonePreferences.instance();

        Core core = Factory.instance().createCore(null, null, this);
        core.start();
    }

    public static synchronized LinphoneManager getInstance() {
        LinphoneManager manager = LinphoneService.instance().getLinphoneManager();
        if (manager == null) {
            throw new RuntimeException(
                    "[Manager] Linphone Manager should be created before accessed");
        }
        if (manager.mExited) {
            throw new RuntimeException(
                    "[Manager] Linphone Manager was already destroyed. "
                            + "Better use getCore and check returned value");
        }
        return manager;
    }

    public synchronized void startLibLinphone(boolean isPush) {
        try {
            mCore =
                    Factory.instance()
                            .createCore(
                                    mPrefs.getLinphoneDefaultConfig(),
                                    mPrefs.getLinphoneFactoryConfig(),
                                    mContext);
            mCore.addListener(mCoreListener);

            if (isPush) {
                org.linphone.core.tools.Log.w(
                        "[Manager] We are here because of a received push notification, enter background mode before starting the Core");
                mCore.enterBackground();
            }

            mCore.start();

            mIterateRunnable =
                    new Runnable() {
                        @Override
                        public void run() {
                            if (mCore != null) {
                                mCore.iterate();
                            }
                        }
                    };
            TimerTask lTask =
                    new TimerTask() {
                        @Override
                        public void run() {
                            LinphoneUtils.dispatchOnUIThread(mIterateRunnable);
                        }
                    };
            /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
            mTimer = new Timer("Linphone scheduler");
            mTimer.schedule(lTask, 0, 20);
        } catch (Exception e) {
            org.linphone.core.tools.Log.e(e, "[Manager] Cannot start linphone");
        }

        // H264 codec Management - set to auto mode -> MediaCodec >= android 5.0 >= OpenH264
        H264Helper.setH264Mode(H264Helper.MODE_AUTO, mCore);
    }

    // TODO IMPORTANT
    public static synchronized Core getCore() {
        if (getInstance().mExited) {
            // Can occur if the UI thread play a posted event but in the meantime the
            // LinphoneManager was destroyed
            // Ex: stop call and quickly terminate application.
            return null;
        }
        return getInstance().mCore;
    }
}
