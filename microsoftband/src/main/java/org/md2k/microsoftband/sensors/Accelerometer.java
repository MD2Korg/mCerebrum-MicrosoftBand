package org.md2k.microsoftband.sensors;

import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.SampleRate;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.CallBack;
import org.md2k.utilities.Report.Log;

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
public class Accelerometer extends Sensor {
    private static final String TAG = Accelerometer.class.getSimpleName();
    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            double[] samples = new double[3];
            samples[0] = event.getAccelerationX();
            samples[1] = event.getAccelerationY();
            samples[2] = event.getAccelerationZ();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendData(dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
        }
    };

    Accelerometer() {
        super(DataSourceType.ACCELEROMETER, "31", 1);
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        Log.d(TAG,"platform="+platform);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Accelerometer");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.UNIT, "g");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "measures the acceleration applied to the device");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDoubleArray.class.getName());
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        return dataSourceBuilder;
    }

    private ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("Accelerometer X", "The x-axis acceleration of the Band in +-g (g = 9.81 m/s^2)", "g", frequency, double.class.getName(), "-3", "3"));
        dataDescriptors.add(createDataDescriptor("Accelerometer Y", "The y-axis acceleration of the Band in +-g (g = 9.81 m/s^2)", "g", frequency, double.class.getName(), "-3", "3"));
        dataDescriptors.add(createDataDescriptor("Accelerometer Z", "The z-axis acceleration of the Band in +-g (g = 9.81 m/s^2)", "g", frequency, double.class.getName(), "-3", "3"));
        return dataDescriptors;
    }

    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack) {
        if (!registerDataSource(context, platform)) return;
        this.callBack = callBack;
        Log.d(TAG, "register: ACCELEROMETER freq=" + frequency);

        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (frequency) {
                        case "8":
                            bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                            break;
                        case "31":
                            bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS32);
                            break;
                        case "62":
                            bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                            break;
                        default:
                            break;
                    }
                } catch (BandException e) {
                    Log.d(TAG," exception...............");
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
                    bandClient.getSensorManager().unregisterAccelerometerEventListeners();
                } catch (Exception e) {
                }
            }
        });
        background.start();
        unregisterDataSource(context);
    }

}
