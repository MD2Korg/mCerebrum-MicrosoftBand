package org.md2k.microsoftband.sensors;

import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;

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
public class Gyroscope extends Sensor {
    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            double[] samples = new double[3];
            samples[0] = event.getAngularVelocityX();
            samples[1] = event.getAngularVelocityY();
            samples[2] = event.getAngularVelocityZ();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendData(dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
        }
    };

    Gyroscope() {
        super(DataSourceType.GYROSCOPE, "31", 1);
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Gyroscope");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "degrees/second");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Provides X, Y, and Z angular velocity in degrees per second (°/sec) units");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDoubleArray.class.getName());
        return dataSourceBuilder;
    }

    private ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Gyroscope X", "Angular velocity around the x-axis of the Band in degrees/sec", "degrees/second", frequency, double.class.getName(), "-800", "800"));
        dataDescriptors.add(createDataDescriptor("Gyroscope Y", "Angular velocity around the y-axis of the Band in degrees/sec", "degrees/second", frequency, double.class.getName(), "-800", "800"));
        dataDescriptors.add(createDataDescriptor("Gyroscope Z", "Angular velocity around the z-axis of the Band in degrees/sec", "degrees/second", frequency, double.class.getName(), "-800", "800"));
        return dataDescriptors;
    }

    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack) {
        registerDataSource(context, platform);
        this.callBack = callBack;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    switch (frequency) {
                        case "8":
                            bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS128);
                            break;
                        case "31":
                            bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS32);
                            break;
                        case "62":
                            bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS16);
                            break;
                        default:
                            break;
                    }
                } catch (BandException e) {
                    e.printStackTrace();
                }
            }

        });
        background.start();
    }

    public void unregister(Context context, final BandClient bandClient) {
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bandClient.getSensorManager().unregisterGyroscopeEventListeners();
                } catch (Exception e) {
                }
            }
        });
        background.start();
        unregisterDataSource(context);
    }

}
