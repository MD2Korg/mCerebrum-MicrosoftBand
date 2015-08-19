package org.md2k.microsoftband;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandTheme;
import com.microsoft.band.ConnectionState;

import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.utilities.Report.Log;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

public abstract class Device implements Serializable{
    private static final String TAG = Device.class.getSimpleName();
    protected Context context;
    protected String platformId;
    protected String platformName;
    protected String platformType;
    protected boolean enabled;
    protected BandClient bandClient;
    protected boolean running;
    protected boolean wait=false;

    Device(Context context, String platformId) {
        this.context = context;
        this.platformId = platformId;
        platformType = PlatformType.MICROSOFT_BAND;
        this.enabled = false;
        platformName = findPlatformName();
        running=true;
    }

    public static BandInfo[] findBandInfo() {
        return BandClientManager.getInstance().getPairedBands();
    }

    String findPlatformName() {
        BandInfo[] mPairBands = BandClientManager.getInstance().getPairedBands();
        for (BandInfo bandInfo : mPairBands) {
            if (bandInfo.getMacAddress().equals(platformId)) return bandInfo.getName();
        }
        return null;
    }

    public String getPlatformId() {
        return platformId;
    }

    public String getPlatformName() {
        return platformName;
    }

    BandInfo findBandInfo(String platformId) {
        BandInfo[] mPairBands = BandClientManager.getInstance().getPairedBands();
        for (BandInfo bandInfo : mPairBands) {
            if (bandInfo.getMacAddress().equals(platformId)) return bandInfo;
        }
        return null;
    }

    private boolean connectDevice() {
        Log.d(TAG, platformId + " connectDevice...");
        Log.d(TAG, "platformId=" + platformId + " PlatformName=" + platformName + " enabled=" + enabled + " bandclient=" + bandClient);
        if (bandClient != null) {
            Log.d(TAG, "platformId=" + platformId + " PlatformName=" + platformName + " enabled=" + enabled + " bandclient=" + bandClient + "status=" + bandClient.getConnectionState().name());
            if (bandClient.getConnectionState() == ConnectionState.CONNECTED) return true;
            else bandClient = null;
        }
        try {
            BandInfo bandInfo = findBandInfo(platformId);
            if (bandInfo == null) return false;
            platformName = bandInfo.getName();
            Log.d(TAG, platformId + " after bandinfo..." + bandInfo.getName());
            bandClient = BandClientManager.getInstance().create(context, bandInfo);
            Log.d(TAG, bandInfo.getName() + " connect().. bandClient=" + bandClient);
            ConnectionState state = bandClient.connect().await(1, TimeUnit.SECONDS);

            Log.d(TAG, "After connection await()---->  platformId=" + platformId + " PlatformName=" + platformName + " enabled=" + enabled + " bandclient=" + bandClient + "status=" + bandClient.getConnectionState().name());
            Log.d(TAG, platformId + " ...connectDevice");
            return ConnectionState.CONNECTED == state;
        } catch (InterruptedException | BandException e) {
            bandClient = null;
            Log.e(TAG, e.getMessage());
            Log.d(TAG, platformId + " exception1");
            Log.d(TAG, platformId + " ...connectDevice");
            return false;
        } catch (TimeoutException e) {
            bandClient = null;
            Log.e(TAG, e.getMessage());
            Log.d(TAG, platformId + " exception2");
            Log.d(TAG, platformId + " ...connectDevice");
            return false;
        }
    }
    Runnable connectRunnable=new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "connect run()...");
            while(running) {
                boolean res = connectDevice();
                Log.d(TAG, "connect run() status=" + res);
                if (res) {
                    Log.d(TAG, "connect run() status= CONNECTED");
                    bandCallBack.onBandConnected();
                    break;
                } else {
                    Log.d(TAG, "connect run() status=NOTCONNECTED post delayed()");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Sleep Error");
                    }
                }
            }
            Log.d(TAG, "...connect run()");
        }
    };
    Thread runThread;
    BandCallBack bandCallBack;

    public void connect(final BandCallBack bandCallBack) {
        Log.d(TAG, "connect...");
        this.bandCallBack=bandCallBack;
        runThread=new Thread(connectRunnable);
        runThread.start();
        Log.d(TAG, "...connect");
    }
    public void stopConnection(){
        running=false;
        runThread.interrupt();
    }


    public synchronized void disconnect() {

        Log.d(TAG, "disconnect...");
        while(wait);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run()...");
                if (bandClient == null) return;
                try {
                    if (bandClient.isConnected())
                        bandClient.disconnect().await(1, TimeUnit.SECONDS);
                    bandClient = null;
                } catch (Exception e) {
                    bandClient = null;
                    Log.d(TAG, "disconnect() .. Exception: e=" + e.getMessage());
                    Log.e(TAG, e.getMessage());
                }
                Log.d(TAG, "...run()");
            }
        }).start();
        Log.d(TAG,"...disconnect");
    }

    public synchronized void changeBackGround(final String wrist) {
        final Thread background = new Thread(new Runnable() {
            private Bitmap getBitmap(String wrist) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                if (wrist.equals("LEFT_WRIST")) {
                    return BitmapFactory.decodeResource(context.getResources(), R.raw.left, options);
                } else if (wrist.equals("RIGHT_WRIST")) {
                    return BitmapFactory.decodeResource(context.getResources(), R.raw.right, options);
                } else return null;
            }

            private BandTheme getTheme(String wrist) {
                if (wrist.equals(Constants.LEFT_WRIST))
                    return new BandTheme(0x39bf6f, 0x41ce7a, 0x35aa65, 0x939982, 0x33a361, 0x2c8454);
                else if (wrist.equals(Constants.RIGHT_WRIST))
                    return new BandTheme(0x3366cc, 0x3a78dd, 0x3165ba, 0x8997ab, 0x3a78dd, 0x2b5aa5);
                else return null;
            }

            @Override
            public void run() {
                final Bitmap image = getBitmap(wrist);
                final BandTheme bandTheme = getTheme(wrist);
                if (image == null || bandTheme == null) return;
                connect(new BandCallBack() {
                    @Override
                    public void onBandConnected() {
                        try {
                            wait=true;
                            bandClient.getPersonalizationManager().setMeTileImage(image).await();
                            bandClient.getPersonalizationManager().setTheme(bandTheme).await();
                            wait=false;

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (BandException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        background.start();
    }
}
