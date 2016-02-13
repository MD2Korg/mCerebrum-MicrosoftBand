package org.md2k.microsoftband.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.band.BandClient;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DATA_QUALITY;

import java.util.ArrayList;
import java.util.HashMap;

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
public class Status extends Sensor {
    private static final String TAG = Status.class.getSimpleName();
    Handler handler;
    long lastReceivedTimestamp;
    double lastBandContact;
    public static final long PERIOD=1000;
    public static final long RESTART=30000;


    Status() {
        super(DataSourceType.STATUS, "1 Hz", 1);
        lastReceivedTimestamp = 0;
        lastBandContact = -2;
        handler = new Handler();
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        Log.d(TAG, "platform=" + platform);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "enum");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the connection status of microsoft band");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeIntArray.class.getName());
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        return dataSourceBuilder;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Connection Status", "measures the connection status of microsoft band", "meter/second^2)", frequency, double.class.getName(), "0", "4"));
        return dataDescriptors;
    }

    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack) {
        registerDataSource(context, platform);
        this.callBack = callBack;
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter("microsoftBand"));
        handler.post(getStatus);
    }

    Runnable getStatus = new Runnable() {
        @Override
        public void run() {
            int status[] = new int[1];
            if (DateTime.getDateTime() - lastReceivedTimestamp > PERIOD)
                status[0] = DATA_QUALITY.BAND_OFF;
            else if (lastBandContact != 0)
                status[0] = DATA_QUALITY.NOT_WORN;
            else
                status[0] = DATA_QUALITY.GOOD;

            DataTypeIntArray dataTypeIntArray = new DataTypeIntArray(DateTime.getDateTime(), status);
            sendDataStatus(dataTypeIntArray);

            callBack.onReceivedData(dataTypeIntArray);
            handler.postDelayed(getStatus, PERIOD);
        }
    };

    public void unregister(Context context, final BandClient bandClient) {
        if (!enabled) return;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        handler.removeCallbacks(getStatus);
        unregisterDataSource(context);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastReceivedTimestamp = intent.getLongExtra("timestamp", 0);
            if (DataSourceType.BAND_CONTACT.equals(intent.getStringExtra("datasourcetype")))
                lastBandContact = ((DataTypeDoubleArray) intent.getSerializableExtra("data")).getSample()[0];
        }
    };

}
