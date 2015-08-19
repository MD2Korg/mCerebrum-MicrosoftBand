package org.md2k.microsoftband;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.md2k.datakitapi.DataKitApi;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.UI.UIShow;

/**
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
    public static boolean isRunning = false;

    MicrosoftBandPlatforms microsoftBandPlatforms;
    DataKitApi dataKitApi;
    MyBlueTooth myBlueTooth;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
        connectDataKit();
    }

    void initialize() {
        microsoftBandPlatforms = null;
        isRunning = false;
        dataKitApi = new DataKitApi(getBaseContext());
        myBlueTooth = new MyBlueTooth(ServiceMicrosoftBands.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                connectDevice();
            }
        });
    }

    void connectDataKit() {
        if (!dataKitApi.connect(onConnectionListener)) {
            UIShow.ErrorDialog(ServiceMicrosoftBands.this.getApplicationContext(), "DataKit Service", "DataKit Service is not available");
            Log.e(TAG, "DataKit Service is not available");
            stopSelf();
        }
    }

    OnConnectionListener onConnectionListener = new OnConnectionListener() {
        @Override
        public void onConnected() {
            if (!myBlueTooth.isEnabled())
                myBlueTooth.enable();
            else {
                connectDevice();
            }
        }
    };
    void connectDevice() {
        microsoftBandPlatforms=MicrosoftBandPlatforms.getInstance(ServiceMicrosoftBands.this);
        microsoftBandPlatforms.register(dataKitApi);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy().. send broadcast message... isRunning=" + isRunning);
        myBlueTooth.close();
        if (microsoftBandPlatforms != null) {
            microsoftBandPlatforms.unregister();
        }
        dataKitApi.disconnect();
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
