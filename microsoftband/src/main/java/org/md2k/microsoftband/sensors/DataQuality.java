package org.md2k.microsoftband.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.band.BandClient;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;
import org.md2k.microsoftband.Constants;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.DATA_QUALITY;

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
public class DataQuality extends Sensor {
    public static final long PERIOD = 3000;
    public static final long RESTART = 15000;
    private static final String TAG = DataQuality.class.getSimpleName();
    private long lastReceivedTimestamp;
    private Handler handler;
    private double lastBandContact;
    private Runnable runnableGetStatus = new Runnable() {
        @Override
        public void run() {
            int dataQuality;
            if (DateTime.getDateTime() - lastReceivedTimestamp > PERIOD)
                dataQuality = DATA_QUALITY.BAND_OFF;
            else if (lastBandContact != 0)
                dataQuality = DATA_QUALITY.NOT_WORN;
            else
                dataQuality = DATA_QUALITY.GOOD;

            DataTypeInt dataTypeInt = new DataTypeInt(DateTime.getDateTime(), dataQuality);
            Log.d(TAG, "DataQuality = " + dataQuality);
            sendDataStatus(dataTypeInt);

            callBack.onReceivedData(dataTypeInt);
            handler.postDelayed(runnableGetStatus, PERIOD);
        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastReceivedTimestamp = intent.getLongExtra("timestamp", 0);
            if (DataSourceType.BAND_CONTACT.equals(intent.getStringExtra("datasourcetype")))
                lastBandContact = ((DataTypeDoubleArray) intent.getParcelableExtra("data")).getSample()[0];
        }
    };

    DataQuality() {
        super(DataSourceType.DATA_QUALITY, String.valueOf(1.0/(PERIOD/1000))+" Hz", 1);
        lastReceivedTimestamp = 0;
        lastBandContact = DATA_QUALITY.BAND_OFF;
        handler = new Handler();
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        Log.d(TAG, "platform=" + platform);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setId(DataSourceType.BAND_CONTACT);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "enum");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the Data Quality of microsoft band. Values="+DATA_QUALITY.METADATA_STR);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeInt.class.getName());
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        return dataSourceBuilder;
    }

    private ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Measures the data quality", "measures the data quality of microsoft band",DATA_QUALITY.METADATA_STR, frequency, double.class.getName(), "0", "6"));
        return dataDescriptors;
    }


    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack) {
        registerDataSource(context, platform);
        this.callBack = callBack;
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.INTENT_RECEIVED_DATA));
        handler.post(runnableGetStatus);
    }

    public void unregister(Context context, final BandClient bandClient) {
        if (!enabled) return;
        handler.removeCallbacks(runnableGetStatus);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        unregisterDataSource(context);
    }
    public void sendDataStatus(DataType dataType){
        try {
            DataKitAPI.getInstance(context).insert(dataSourceClient, dataType);
        } catch (DataKitException e) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.INTENT_STOP));
        }
    }

}
