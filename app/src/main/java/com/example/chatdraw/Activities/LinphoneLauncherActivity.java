package com.example.chatdraw.Activities;

/*
LinphoneLauncherActivity.java
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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.chatdraw.R;

/** Creates LinphoneService and wait until Core is ready to start main Activity */
public class LinphoneLauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (LinphoneService.isReady()) {
            onServiceReady();
            Log.v("TICKET","2");
        } else {
            startService(
                    new Intent().setClass(LinphoneLauncherActivity.this, LinphoneService.class));
            new ServiceWaitThread().start();
            Log.v("TICKET","1");
        }
    }

    private void onServiceReady() {
        final Class<? extends Activity> classToStart;
        Log.v("TICKET","5");
        classToStart = MainActivity.class;

        LinphoneUtils.dispatchOnUIThreadAfter(
                new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(LinphoneLauncherActivity.this, classToStart);
                        if (getIntent() != null && getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.setAction(getIntent().getAction());
                        intent.setType(getIntent().getType());
                        startActivity(intent);
                    }
                },
                100);
    }

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    Log.v("TICKET","3");
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            Log.v("TICKET","4");
            LinphoneUtils.dispatchOnUIThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            onServiceReady();
                        }
                    });
        }
    }
}
