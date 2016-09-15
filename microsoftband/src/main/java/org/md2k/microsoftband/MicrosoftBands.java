package org.md2k.microsoftband;

import android.content.Context;
import android.widget.Toast;

import com.microsoft.band.BandInfo;

import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.utilities.Report.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;

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
public class MicrosoftBands {
    private static final String TAG = MicrosoftBands.class.getSimpleName();
    private Context context;
    private ArrayList<MicrosoftBand> microsoftBands;
    public MicrosoftBands(Context context) {
        Log.d(TAG,"Constructor()...");
        this.context=context;
        microsoftBands =new ArrayList<>();
        readDataSourceFromFile();
        addOthers();
    }
    public int size(){
        return microsoftBands.size();
    }
    public int size(boolean enabled){
        int count=0;
        for(int i=0;i< microsoftBands.size();i++)
            if(microsoftBands.get(i).enabled==enabled)
                count++;
        return count;
    }

    public void addOthers() {
        BandInfo[] bandInfos=Device.findBandInfo();
        for (BandInfo bandInfo : bandInfos) {
            String deviceId = bandInfo.getMacAddress();
            if(find(deviceId)==null)
                microsoftBands.add(new MicrosoftBand(context, null,deviceId));
        }
    }
    public MicrosoftBand find(String deviceId){
        for (int i = 0; i < microsoftBands.size(); i++)
            if (microsoftBands.get(i).equals(deviceId))
                return microsoftBands.get(i);
        return null;
    }

    public void readDataSourceFromFile(){
        try {
            Log.d(TAG, "readDataSourceFromFile()...");
            ArrayList<DataSource> dataSources = Configuration.getDataSources();
            if(dataSources==null) throw new FileNotFoundException();
            Log.d(TAG, "readDataSourceFromFile() .. datasource_size=" + dataSources.size());
            for (int i = 0; i < dataSources.size(); i++) {
                String deviceId = dataSources.get(i).getPlatform().getMetadata().get(METADATA.DEVICE_ID);
                String platformId = dataSources.get(i).getPlatform().getId();
                MicrosoftBand microsoftBand = find(deviceId);
                if (microsoftBand == null) {
                    microsoftBand = new MicrosoftBand(context, platformId,deviceId);
                    microsoftBands.add(microsoftBand);
                }
                microsoftBand.setEnabled(true);
                microsoftBand.setEnabled(dataSources.get(i), true);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context,"Microsoft Band is not configured",Toast.LENGTH_LONG).show();
        }
        Log.d(TAG,"...readDataSourceFromFile()");
    }
    public void deleteMicrosoftBandPlatform(String deviceId) {
        MicrosoftBand microsoftBand=find(deviceId);
        if(microsoftBand==null) return;
        microsoftBand.enabled=false;
        microsoftBand.platformId=null;
        microsoftBand.resetDataSource();
    }
    public ArrayList<MicrosoftBand> find() {
        return microsoftBands;
    }
    public void writeDataSourceToFile() throws IOException {
        ArrayList<DataSource> dataSources = new ArrayList<>();
        if (microsoftBands == null) throw new NullPointerException();
        if (microsoftBands.size() == 0) throw new EmptyStackException();
        for (int i = 0; i < microsoftBands.size(); i++) {
            if(!microsoftBands.get(i).enabled) continue;
            Platform platform= microsoftBands.get(i).getPlatform();
            for(int j=0;j< microsoftBands.get(i).getSensors().size();j++) {
                if(!microsoftBands.get(i).getSensors().get(j).isEnabled()) continue;
                DataSourceBuilder dataSourceBuilder= microsoftBands.get(i).getSensors().get(j).createDataSourceBuilder(microsoftBands.get(i).getPlatform());
                if(dataSourceBuilder==null) continue;
                dataSourceBuilder=dataSourceBuilder.setPlatform(platform);
                DataSource dataSource = dataSourceBuilder.build();
                dataSources.add(dataSource);
            }
        }
        if(dataSources.size()==0) Toast.makeText(context,"Error: MicrosoftBand is not configured propoerly...",Toast.LENGTH_SHORT).show();
        Configuration.write(dataSources);
    }

    public void register() {
        Log.d(TAG, "register...");
        for(int i=0;i< microsoftBands.size();i++) {
            microsoftBands.get(i).register();
        }
    }
    public void unregister() {
        for(int i=0;i< microsoftBands.size();i++) {
            microsoftBands.get(i).unregister();
        }
    }

    public void unregister(String deviceId) {
        for (int i = 0; i < microsoftBands.size(); i++)
            if (microsoftBands.get(i).getPlatform().getMetadata().get(METADATA.DEVICE_ID).equals(deviceId))
                microsoftBands.get(i).unregister();
    }

    public void register(String deviceId) {
        for (int i = 0; i < microsoftBands.size(); i++)
            if (microsoftBands.get(i).getPlatform().getMetadata().get(METADATA.DEVICE_ID).equals(deviceId))
                microsoftBands.get(i).register();
    }

    public void disconnect(){
        for(int i=0;i< microsoftBands.size();i++) {
                microsoftBands.get(i).disconnect();
        }
    }

    public void disconnect(String deviceId) {
        for (int i = 0; i < microsoftBands.size(); i++) {
            if (microsoftBands.get(i).getPlatform().getMetadata().get(METADATA.DEVICE_ID).equals(deviceId))
                microsoftBands.get(i).disconnect();
        }
    }

}
