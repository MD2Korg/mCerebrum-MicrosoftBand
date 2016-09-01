package org.md2k.microsoftband.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;
import org.md2k.microsoftband.HRConsentActivity;

import java.util.ArrayList;
import java.util.HashMap;

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
public class HeartRate extends Sensor {
    Handler handler;
    boolean isRegistered;

    HeartRate() {
        super(DataSourceType.HEART_RATE, "1", 1);
        handler = new Handler();
        isRegistered = false;
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Heart Rate");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "beats/minute");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Provides the number of beats per minute; also indicates if the heart rate sensor is fully locked on to the wearer’s heart rate.");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeIntArray.class.getName());
        return dataSourceBuilder;
    }

    private ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Heart Rate", "Current heart rate as read by the Band in beats/min", "beats/minute", frequency, double.class.getName(), "0", "200"));
        dataDescriptors.add(createDataDescriptor("Quality", "Quality of the current heart rate reading", "enum [0: locked, 1: acquiring]", frequency, double.class.getName(), "0", "1"));
        return dataDescriptors;
    }

    BandClient bandClient;
    Runnable runnableStart = new Runnable() {
        @Override
        public void run() {
            try {
                if (isRegistered == true) return;
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                    Intent intent = new Intent(context, HRConsentActivity.class);
                    intent.putExtra("type", HeartRate.class.getSimpleName());
                    HRConsentActivity.bandClient = bandClient;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                    bandClient.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    isRegistered = true;
                }
            } catch (BandException e) {
                e.printStackTrace();
            }
        }
    };

    public void register(final Context context, final BandClient bandClient, Platform platform, CallBack callBack) {
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(HRConsentActivity.HRCONSENT));
        registerDataSource(context, platform);
        this.callBack = callBack;
        this.bandClient = bandClient;
        isRegistered = false;
        handler.post(runnableStart);
    }

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            double samples[] = new double[2];
            samples[0] = event.getHeartRate();
            if (event.getQuality() == HeartRateQuality.ACQUIRING)
                samples[1] = 1;
            else if (event.getQuality() == HeartRateQuality.LOCKED)
                samples[1] = 0;
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendData(dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
        }
    };

    public void unregister(Context context, final BandClient bandClient) {
        handler.removeCallbacks(runnableStart);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        isRegistered=false;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bandClient.getSensorManager().unregisterHeartRateEventListeners();
                } catch (Exception e) {
                }
            }
        });
        background.start();
        unregisterDataSource(context);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.post(runnableStart);
        }
    };
}