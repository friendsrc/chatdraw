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

/** Handles Linphone's Core lifecycle */
public class LinphoneManager {
    private final String mBasePath;
    private final Context mContext;
    private Core mCore;

    private boolean mExited;

    public LinphoneManager(Context c) {
        Log.v("TICKET","6");
        mExited = false;
        mContext = c;
        mBasePath = c.getFilesDir().getAbsolutePath();
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
