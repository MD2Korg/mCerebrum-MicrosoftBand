package org.md2k.microsoftband.sensors;

import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;

import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
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
public class Altimeter extends Sensor{
    Altimeter() {
        super(DataSourceType.ALTIMETER,"1 Hz",2);
    }

    public DataSourceBuilder createDataSourceBuilder(Platform platform) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setType(dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(createDataDescriptors());
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.FREQUENCY, frequency);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Altimeter");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Provides current elevation data like total gain/loss,steps ascended/descended, flights ascended/descended, and elevation rate.");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeDoubleArray.class.getName());
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        return dataSourceBuilder;
    }

    ArrayList<HashMap<String, String>> createDataDescriptors() {
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        dataDescriptors.add(createDataDescriptor("FlightsAscended", "Number of floors ascended since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("FlightsDescended", "Number of floors ascended since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("Rate", "The current rate of ascend/descend in cm/s", "centimeter/second", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("SteppingGain", "Total elevation gained in centimeters by taking steps since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("SteppingLoss", "Total elevation lost in centimeters by taking steps since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("StepsAscended", "Total number of steps ascended since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("StepsDescended", "Total number of steps descended since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("TotalGain", "Total elevation gained in centimeters since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        dataDescriptors.add(createDataDescriptor("TotalLoss", "Total elevation loss in centimeters since the Band was last factory-reset", "count", frequency, double.class.getName(), null, null));
        return dataDescriptors;
    }
    public void register(Context context, final BandClient bandClient, Platform platform, CallBack callBack){
        registerDataSource(context, platform);
        this.callBack=callBack;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bandClient.getSensorManager().registerAltimeterEventListener(mAltimeterListener);
                } catch (BandException e) {
                    e.printStackTrace();
                }
            }

        });
        background.start();
    }
    private BandAltimeterEventListener mAltimeterListener = new BandAltimeterEventListener() {
        @Override
        public void onBandAltimeterChanged(BandAltimeterEvent bandAltimeterEvent) {
            double samples[] = new double[9];
            samples[0] = bandAltimeterEvent.getFlightsAscended();
            samples[1]=bandAltimeterEvent.getFlightsDescended();
            samples[2]=bandAltimeterEvent.getRate();
            samples[3]=bandAltimeterEvent.getSteppingGain();
            samples[4]=bandAltimeterEvent.getSteppingLoss();
            samples[5]=bandAltimeterEvent.getStepsAscended();
            samples[6]=bandAltimeterEvent.getStepsDescended();
            samples[7]=bandAltimeterEvent.getTotalGain();
            samples[8]=bandAltimeterEvent.getTotalLoss();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendData(dataTypeDoubleArray);
            callBack.onReceivedData(dataTypeDoubleArray);
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
                    bandClient.getSensorManager().unregisterAltimeterEventListener(mAltimeterListener);
                } catch (BandIOException e) {
                    e.printStackTrace();
                }
            }
        });
        background.start();
    }
}
