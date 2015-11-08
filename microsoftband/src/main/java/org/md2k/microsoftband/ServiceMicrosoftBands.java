package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;

import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.utilities.datakit.DataKitHandler;

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
    MicrosoftBandPlatforms microsoftBandPlatforms;
    DataKitHandler dataKitHandler=null;

    @Override
    public void onCreate() {
        super.onCreate();
        setBluetoothSettingsDataKit();
    }
    private boolean readSettings(){
        microsoftBandPlatforms = new MicrosoftBandPlatforms(ServiceMicrosoftBands.this);
        return microsoftBandPlatforms.size(true) != 0;
    }


    void initializeBluetoothConnection() {
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                setSettingsDataKit();
            }
            @Override
            public void onDisconnected() {
                clearDataKitSettingsBluetooth();
            }
        });
    }

    boolean connectDataKit() {
        dataKitHandler = DataKitHandler.getInstance(ServiceMicrosoftBands.this);
        return dataKitHandler.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
                microsoftBandPlatforms.register();
            }
        });
    }
    void disconnectDataKit(){
        if(microsoftBandPlatforms!=null)
            microsoftBandPlatforms.unregister();
        if(dataKitHandler!=null)
            dataKitHandler.disconnect();
        dataKitHandler=null;
    }
    @Override
    public void onDestroy() {
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
//            showAlertDialogBluetooth();
            close();
        }
    }
    private void setSettingsDataKit() {
        if(readSettings())
            setDataKit();
        else {
            showAlertDialogSettings();
            close();
        }
    }
    void setDataKit(){
        if(connectDataKit())
            Toast.makeText(getApplicationContext(), "MicrosoftBand Service started Successfully", Toast.LENGTH_LONG).show();
        else {
            showAlertDialogDataKit();
            close();
        }
    }
    private void clearDataKitSettingsBluetooth(){
        disconnectDataKit();
        clearSettingsBluetooth();
    }
    private void clearSettingsBluetooth(){
        microsoftBandPlatforms=null;
        clearBlueTooth();
    }
    private void clearBlueTooth(){
        myBlueTooth.close();
//        if(myBlueTooth.isEnabled()) myBlueTooth.disable();
//        close();
    }

    void close() {
        stopSelf();
    }

    void showAlertDialogSettings(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Error: Settings")
                .setIcon(R.drawable.ic_error_outline_white_24dp)
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
                .setIcon(R.drawable.ic_error_outline_white_24dp)
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
                .setIcon(R.drawable.ic_error_outline_white_24dp)
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
