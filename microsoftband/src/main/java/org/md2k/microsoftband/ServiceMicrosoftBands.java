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
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.notification.NotificationManager;
import org.md2k.utilities.Report.LogStorage;
import org.md2k.utilities.UI.AlertDialogs;
import org.md2k.utilities.permission.PermissionInfo;

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
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    stopSelf();
                } else {
                    load();
                }
            }
        });
    }

    void load() {
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        setBluetooth();
    }

    void setBluetooth() {
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                connectDataKit();
            }

            @Override
            public void onDisconnected() {
                clear();
                myBlueTooth.enable();
            }
        });
        if (myBlueTooth.isEnabled())
            connectDataKit();
        else {
            myBlueTooth.enable();
        }
    }

    private synchronized boolean readSettings() {
        microsoftBands = new MicrosoftBands(getApplicationContext());
        return microsoftBands.size(true) != 0;
    }

    private BroadcastReceiver mMessageReceiverStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service" + ", msg=" + intent.getStringExtra("type"));
            clear();
            stopSelf();
        }
    };
    private BroadcastReceiver mMessageReceiverRestart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_restart_service");
            String deviceId = intent.getStringExtra("deviceid");
            microsoftBands.unregister(deviceId);
            microsoftBands.disconnect(deviceId);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            microsoftBands.register(deviceId);
        }
    };

    private void connectDataKit() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverStop,
                new IntentFilter(Constants.INTENT_STOP));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverRestart,
                new IntentFilter(Constants.INTENT_RESTART));
        if (readSettings()) {
            dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
            try {
                dataKitAPI.connect(new OnConnectionListener() {
                    @Override
                    public void onConnected() {
                        try {
                            microsoftBands.register();
                            notificationManager = new NotificationManager(ServiceMicrosoftBands.this, microsoftBands.find());
                            notificationManager.start();
                        } catch (Exception e) {
                            Intent intent = new Intent(Constants.INTENT_STOP);
                            intent.putExtra("type", "ServiceMicrosoftBands.java...register error after connection");
                            LocalBroadcastManager.getInstance(ServiceMicrosoftBands.this).sendBroadcast(intent);
                        }
                    }
                });
            } catch (DataKitException e) {
                Log.d(TAG, "onException...");
                Intent intent = new Intent(Constants.INTENT_STOP);
                intent.putExtra("type", "ServiceMicrosoftBands.java...Connection Error");
                LocalBroadcastManager.getInstance(ServiceMicrosoftBands.this).sendBroadcast(intent);
            }
        } else {
            showAlertDialogConfiguration(this);
            stopSelf();
        }


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        clear();
        if (myBlueTooth != null) {
            myBlueTooth.close();
            myBlueTooth = null;
        }
        org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_stop");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    synchronized void clear() {
        Log.d(TAG, "disconnectDataKit()...");
        Log.d(TAG, "disconnectDataKit()...microsoftBands=" + microsoftBands);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiverStop);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiverRestart);
        if (microsoftBands != null) {
            microsoftBands.unregister();
            microsoftBands.disconnect();
            microsoftBands = null;
        }
        if (notificationManager != null) {
            notificationManager.stop();
            notificationManager = null;
        }
        if (dataKitAPI != null) {
            dataKitAPI.disconnect();
            dataKitAPI = null;
        }
    }

    void showAlertDialogConfiguration(final Context context) {
        AlertDialogs.AlertDialog(this, "Error: MicrosoftBand Settings", "Please configure Microsoft Band", R.drawable.ic_error_red_50dp, "Settings", "Cancel", null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialog.BUTTON_POSITIVE) {
                    Intent intent = new Intent(context, ActivityMicrosoftBandSettings.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }
}
