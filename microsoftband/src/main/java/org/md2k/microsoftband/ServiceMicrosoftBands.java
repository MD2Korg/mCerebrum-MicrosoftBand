package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.microsoftband.notification.NotificationManager;
import org.md2k.utilities.UI.AlertDialogs;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ServiceMicrosoftBands extends Service {
    private static final String TAG = ServiceMicrosoftBands.class.getSimpleName();
    private MyBlueTooth myBlueTooth = null;
    private MicrosoftBands microsoftBands;
    private DataKitAPI dataKitAPI = null;
    private NotificationManager notificationManager;
    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverStop,
                new IntentFilter(Constants.INTENT_STOP));
        if (readSettings())
            setBluetooth();
        else {
            showAlertDialogConfiguration(this);
            stopSelf();
        }
    }

    void setBluetooth() {
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                connectDataKit();
            }
            @Override
            public void onDisconnected() {
                stopSelf();
            }
        });
        if (myBlueTooth.isEnabled())
            connectDataKit();
        else {
            myBlueTooth.enable();
        }
    }

    private boolean readSettings() {
        microsoftBands = new MicrosoftBands(getApplicationContext());
        return microsoftBands.size(true) != 0;
    }

    private BroadcastReceiver mMessageReceiverStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };

    private void connectDataKit() {
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    microsoftBands.register();
                    notificationManager = new NotificationManager(ServiceMicrosoftBands.this, microsoftBands.find());
                    notificationManager.start();
                }
            });
        } catch (DataKitException e) {
            Log.d(TAG, "onException...");
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.INTENT_STOP));
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiverStop);
        clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void clear() {
        Log.d(TAG, "disconnectDataKit()...");
        Log.d(TAG, "disconnectDataKit()...microsoftBands=" + microsoftBands);

        if (microsoftBands != null)
            microsoftBands.unregister();
        if (notificationManager != null) notificationManager.clear();
        if (dataKitAPI != null) {
            dataKitAPI.disconnect();
        }
        if (myBlueTooth != null)
            myBlueTooth.close();
    }

    void showAlertDialogConfiguration(final Context context){
        AlertDialogs.AlertDialog(this, "Error: MicrosoftBand Settings", "Please configure Microsoft Band", R.drawable.ic_error_red_50dp, "Settings", "Cancel", null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which== AlertDialog.BUTTON_POSITIVE){
                    Intent intent = new Intent(context, ActivityMicrosoftBandSettings.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }
}
