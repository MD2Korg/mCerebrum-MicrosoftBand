package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnExceptionListener;
import org.md2k.datakitapi.status.Status;
import org.md2k.microsoftband.notification.NotificationManager;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
    MyBlueTooth myBlueTooth=null;
    MicrosoftBands microsoftBands;
    DataKitAPI dataKitAPI=null;
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        setBluetoothSettingsDataKit();
    }
    private boolean readSettings(){
        microsoftBands = new MicrosoftBands(getApplicationContext());
        return microsoftBands.size(true) != 0;
    }


    void initializeBluetoothConnection() {
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                setSettingsDataKit();
            }
            @Override
            public void onDisconnected() {
                Log.d(TAG,"bluetooth disconnected...");
                clearDataKitSettingsBluetooth();
            }
        });
    }

    void connectDataKit() {
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        Log.d(TAG,"datakitapi connected="+dataKitAPI.isConnected());
        dataKitAPI.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG," datakit connected.................");
                Log.d(TAG,"datakitapi connected="+dataKitAPI.isConnected());
                microsoftBands.register();
                //TODO: Notitification manager turn on to vibrate MSBand
//                notificationManager=new NotificationManager(ServiceMicrosoftBands.this, microsoftBands.find());
                Toast.makeText(ServiceMicrosoftBands.this, "MicrosoftBand Started successfully", Toast.LENGTH_SHORT).show();
            }
        }, new OnExceptionListener() {
            @Override
            public void onException(Status status) {
                Log.d(TAG, "onException...");
                Toast.makeText(ServiceMicrosoftBands.this, "MicrosoftBand Stopped. Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        });
    }
    void disconnectDataKit(){
        Log.d(TAG,"disconnectDataKit()...");
        if(microsoftBands !=null)
            microsoftBands.unregister();
        if(dataKitAPI!=null) {
            dataKitAPI.disconnect();
            dataKitAPI.close();
        }
    }
    @Override
    public void onDestroy() {
//        if(notificationManager!=null)
//            notificationManager.clear();
        Log.d(TAG, "onDestroy()...");
        clearDataKitSettingsBluetooth();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void setBluetoothSettingsDataKit() {
        initializeBluetoothConnection();
        if (myBlueTooth.isEnabled())
            setSettingsDataKit();
        else {
            myBlueTooth.enable();
            Log.d(TAG,"bluetooth not enabled..");
//            showAlertDialogBluetooth();
            close();
        }
    }
    private void setSettingsDataKit() {
        if(readSettings())
            setDataKit();
        else {
            showAlertDialogSettings();
            Log.d(TAG,"setSettingsDataKit()...");
            close();
        }
    }
    void setDataKit(){
        connectDataKit();
    }
    private void clearDataKitSettingsBluetooth(){
        Log.d(TAG,"clearDataKitSettingsBluetooth...");
        disconnectDataKit();
        clearSettingsBluetooth();
    }
    private void clearSettingsBluetooth(){
        microsoftBands =null;
        clearBlueTooth();
    }
    private void clearBlueTooth(){
        myBlueTooth.close();
//        if(myBlueTooth.isEnabled()) myBlueTooth.disable();
//        close();
    }

    void close() {
        Log.d(TAG,"close()..");
        stopSelf();
    }

    void showAlertDialogSettings(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: Settings")
                .setIcon(R.drawable.ic_error_red_50dp)
                .setMessage("Microsoft Band is not configured.\n\n Please go to Menu -> Settings (or, click Settings below)")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ServiceMicrosoftBands.this, ActivityMicrosoftBandSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ServiceMicrosoftBands.this.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        close();
                    }
                })
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
    void showAlertDialogBluetooth(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: Bluetooth")
                .setIcon(R.drawable.ic_error_red_50dp)
                .setMessage("Please turn on Bluetooth")
                .setPositiveButton("Turn On Bluetooth", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myBlueTooth.enable();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        close();
                    }
                })
                .create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    void showAlertDialogDataKit(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: DataKit")
                .setIcon(R.drawable.ic_error_red_50dp)
                .setMessage("DataKit is not installed.\n\n Please install DataKit")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        close();
                    }
                })
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

}
