package org.md2k.microsoftband;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.utilities.UI.UIShow;

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
    public boolean isMSBandConnected =false;
    boolean isDataKitConnected =false;

    @Override
    public void onCreate() {
        isDataKitConnected=false;
        super.onCreate();
        if(!readSettings())
            UIShow.ErrorDialog(ServiceMicrosoftBands.this, "Configuration File", "Configuration file for MicrosoftBand doesn't exist. Please click Settings");
        else if(!connectDataKit())
            UIShow.ErrorDialog(ServiceMicrosoftBands.this, "DataKit", "DataKit is not available. Please Install DataKit");
    }
    private boolean readSettings(){
        microsoftBandPlatforms = new MicrosoftBandPlatforms(ServiceMicrosoftBands.this);
        return microsoftBandPlatforms.size(true) != 0;
    }


    void initializeBluetoothConnection() {
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                isMSBandConnected=true;
                microsoftBandPlatforms.register();
            }

            @Override
            public void onDisconnected() {
                isMSBandConnected=false;
                microsoftBandPlatforms.unregister();
            }
        });
        if (myBlueTooth.isEnabled()) {
            isMSBandConnected=true;
            microsoftBandPlatforms.register();
        } else {
            myBlueTooth.enable();
        }
    }

    boolean connectDataKit() {
        isDataKitConnected =false;
        DataKitHandler dataKitHandler = DataKitHandler.getInstance(ServiceMicrosoftBands.this);
        return dataKitHandler.connectDataKit(new OnConnectionListener() {
            @Override
            public void onConnected() {
                isDataKitConnected =true;
                initializeBluetoothConnection();
            }
        });
    }

    @Override
    public void onDestroy() {

        if (isMSBandConnected) {
            microsoftBandPlatforms.unregister();
        }
        if(isDataKitConnected)
            DataKitHandler.getInstance(ServiceMicrosoftBands.this).disconnect();
        if(myBlueTooth!=null) {
            myBlueTooth.close();
            myBlueTooth=null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
