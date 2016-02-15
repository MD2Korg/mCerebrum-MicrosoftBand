package org.md2k.microsoftband.sensors;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.source.METADATA;
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
public class Sensors {
    private static final String TAG =Sensors.class.getSimpleName() ;
    Context context;
    ArrayList<Sensor> sensors;
    HashMap<String, Integer> hm = new HashMap<>();
    long starttimestamp = 0;
    int countOff=0;
    Platform platform;
    BandClient bandClient;
    public Sensors(Context context, Platform platform){
        this.context=context;
        this.platform=platform;
        sensors=new ArrayList<>();
        sensors.add(new Status());
        sensors.add(new Accelerometer());
        sensors.add(new Gyroscope());
        sensors.add(new AirPressure());
        sensors.add(new AmbientLight());
        sensors.add(new AmbientTemperature());
        sensors.add(new BandContact());
        sensors.add(new CaloryBurn());
        sensors.add(new Distance());
        sensors.add(new GalvanicSkinResponse());
        sensors.add(new HeartRate());
        sensors.add(new MotionType());
        sensors.add(new Pace());
        sensors.add(new RRInterval());
        sensors.add(new SkinTemperature());
        sensors.add(new Speed());
        sensors.add(new StepCount());
        sensors.add(new UltraVioletRadiation());
        sensors.add(new Altimeter());
        }
    public ArrayList<Sensor> getSensors(){
        return sensors;
    }
    public void setEnable(String dataSourceType,boolean enable){
        for(int i=0;i<sensors.size();i++){
            if(sensors.get(i).getDataSourceType().equals(dataSourceType)) {
                sensors.get(i).setEnabled(enable);
                break;
            }
        }
    }
    public void register(final BandClient bandClient, final Platform platform) {
        this.bandClient=bandClient;
        this.platform=platform;
        hm.clear();
        starttimestamp = DateTime.getDateTime();

        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).isEnabled()) {
                final int finalI = i;
                Log.d(TAG, "sensor=" + sensors.get(i).dataSourceType+" freq="+sensors.get(i).frequency);
                sensors.get(i).register(context,bandClient, platform, new CallBack() {
                    @Override
                    public void onReceivedData(DataType data) {
                        String dataSourceType = sensors.get(finalI).getDataSourceType();
                        Intent intent = new Intent("microsoftBand");
                        intent.putExtra("operation", "data");
                        if (!hm.containsKey(dataSourceType)) {
                            hm.put(dataSourceType, 0);
                        }
                        hm.put(dataSourceType, hm.get(dataSourceType) + 1);
                        intent.putExtra("count", hm.get(dataSourceType));
                        intent.putExtra("timestamp", data.getDateTime());
                        intent.putExtra("starttimestamp", starttimestamp);
                        intent.putExtra("data",(Parcelable) data);
                        intent.putExtra("datasourcetype", dataSourceType);
                        intent.putExtra("deviceid", Sensors.this.platform.getMetadata().get(METADATA.DEVICE_ID));
                        intent.putExtra("platformid", Sensors.this.platform.getId());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        if(dataSourceType.equals(DataSourceType.STATUS)){
                            int status[]=((DataTypeIntArray) data).getSample();
                            if(status[0]== DATA_QUALITY.BAND_OFF){
                                countOff+=Status.PERIOD;
                            }
                            else countOff=0;
                            if(countOff>Status.RESTART){
                                Intent intentRestart = new Intent("microsoftband_restart");
                                intent.putExtra("platformid",platform.getId());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intentRestart);
                            }
                        }
                    }
                });
            }
        }

    }
    public void unregister(BandClient bandClient) throws BandIOException {
        if(!bandClient.isConnected()) return;
        for(int i=0;i<sensors.size();i++)
            sensors.get(i).unregister(context, bandClient);
    }
}
