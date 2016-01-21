package org.md2k.microsoftband.sensors;

import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;

import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;

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
public class AmbientTemperature extends Sensor {
    AmbientTemperature() {
        super(DataSourceType.AMBIENT_TEMPERATURE,"1 Hz",2);
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Ambient Temperature");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "degree celsius");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Current temperature in degrees Celsius");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDouble.class.getName());
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        return dataSourceBuilder;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Ambient Temperature", "Current temperature in degrees Celsius", "degree celsius", frequency, double.class.getName(), "-20", "20"));
        return dataDescriptors;
    }
    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack){
        registerDataSource(context, platform);
        this.callBack = callBack;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bandClient.getSensorManager().registerBarometerEventListener(mAmbientTemperatureEventListener);
                } catch (BandException e) {
                    e.printStackTrace();
                }
            }

        });
        background.start();
    }

    private BandBarometerEventListener mAmbientTemperatureEventListener = new BandBarometerEventListener() {
        @Override
        public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
            DataTypeDouble dataTypeDouble = new DataTypeDouble(DateTime.getDateTime(), bandBarometerEvent.getTemperature());
            sendData(dataTypeDouble);
            callBack.onReceivedData(dataTypeDouble);
        }
    };

    public void unregister(Context context, final BandClient bandClient) {
        if (!enabled) return;
        unregisterDataSource(context);
        if (bandClient == null) return;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bandClient.getSensorManager().unregisterBarometerEventListener(mAmbientTemperatureEventListener);
                } catch (BandIOException e) {
                    e.printStackTrace();
                }
            }
        });
        background.start();
    }

}
