package org.md2k.microsoftband;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;

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
public class MicrosoftBandPlatform extends Device {
    private static final String TAG = MicrosoftBandPlatform.class.getSimpleName();
    private String location;
    boolean isConnected=false;
    private ArrayList<MicrosoftBandDataSource> microsoftBandDataSources;
    public static final String[] DATASOURCETYPE = {
            DataSourceType.ACCELEROMETER,
            DataSourceType.GYROSCOPE,
            DataSourceType.DISTANCE,
            DataSourceType.SPEED,
            DataSourceType.PACE,
            DataSourceType.MOTION_TYPE,
            DataSourceType.HEART_RATE,
            DataSourceType.STEP_COUNT,
            DataSourceType.SKIN_TEMPERATURE,
            DataSourceType.ULTRA_VIOLET_RADIATION,
            DataSourceType.BAND_CONTACT,
            DataSourceType.CALORY_BURN
    };

    public void show() {
        for (int i = 0; i < microsoftBandDataSources.size(); i++) {
            Log.d(TAG, "PlatformId=" + platformId + " platformName=" + platformName + " Location=" + location);
            microsoftBandDataSources.get(i).show();
        }
    }

    public void resetDataSource() {
        microsoftBandDataSources = new ArrayList<>();
        for (String dataSourceType : DATASOURCETYPE)
            microsoftBandDataSources.add(new MicrosoftBandDataSource(context, dataSourceType, false));

    }

    MicrosoftBandPlatform(Context context, String platformId, String location) {
        super(context, platformId);
        this.location = location;
        resetDataSource();
    }

    public ArrayList<MicrosoftBandDataSource> getMicrosoftBandDataSource() {
        return microsoftBandDataSources;
    }

    public void enable(DataSource dataSource) {
        for (int i = 0; i < microsoftBandDataSources.size(); i++) {
            if (microsoftBandDataSources.get(i).getDataSourceType().equals(dataSource.getType())) {
                microsoftBandDataSources.get(i).set(dataSource);
                enabled = true;
            }
        }
    }

    public boolean equals(String platformId) {
        return this.platformId.equals(platformId);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public Platform getPlatform() {
        return new PlatformBuilder().setId(platformId).setType(platformType).setMetadata("location", location).setMetadata("name", platformName).build();
    }

    HashMap<String, Integer> hm = new HashMap<>();
    long starttimestamp = 0;

    public void register() {
        if (!enabled) return;
        hm.clear();
        starttimestamp = DateTime.getDateTime();
        connect(new BandCallBack() {
            @Override
            public void onBandConnected() {
                for (int i = 0; i < microsoftBandDataSources.size(); i++) {
                    if (microsoftBandDataSources.get(i).isEnabled()) {
                        final int finalI = i;
                        microsoftBandDataSources.get(i).register(getPlatform(), bandClient, new CallBack() {
                            @Override
                            public void onReceivedData(DataType data) {
                                String dataSourceType = microsoftBandDataSources.get(finalI).getDataSourceType();
                                Intent intent = new Intent("microsoftBand");
                                intent.putExtra("operation", "data");
                                if (!hm.containsKey(dataSourceType)) {
                                    hm.put(dataSourceType, 0);
                                }
                                hm.put(dataSourceType, hm.get(dataSourceType) + 1);
                                intent.putExtra("count", hm.get(dataSourceType));
                                intent.putExtra("timestamp", data.getDateTime());
                                intent.putExtra("starttimestamp", starttimestamp);
                                intent.putExtra("data", data);
                                intent.putExtra("datasourcetype", dataSourceType);
                                intent.putExtra("platformid", platformId);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        });
                    }
                }

            }
        });
    }

    public void unregister() {
        if(bandClient.isConnected()) {
            for (MicrosoftBandDataSource microsoftBandDataSource : microsoftBandDataSources)
                microsoftBandDataSource.unregister(bandClient);
            disconnect();
        }
    }
}
