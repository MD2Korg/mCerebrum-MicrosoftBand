package org.md2k.microsoftband;

import android.content.Context;
import android.widget.Toast;

import com.microsoft.band.BandInfo;

import org.md2k.datakitapi.DataKitApi;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.utilities.Files;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.UI.UIShow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;

/**
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
public class MicrosoftBandPlatforms{
    private static final String TAG = MicrosoftBandPlatforms.class.getSimpleName();
    private ArrayList<MicrosoftBandPlatform> microsoftBandPlatforms;
    Context context;
    public MicrosoftBandPlatforms(Context context) {
        Log.d(TAG,"Constructor()...");
        this.context=context;
        microsoftBandPlatforms=new ArrayList<>();
        readDataSourceFromFile();
        addOthers();
        Log.d(TAG, "Constructor()... size="+microsoftBandPlatforms.size());
        Log.d(TAG, "...Constructor()");
    }
    int size(){
        return microsoftBandPlatforms.size();
    }
    public int size(boolean enabled){
        int count=0;
        for(int i=0;i<microsoftBandPlatforms.size();i++)
            if(microsoftBandPlatforms.get(i).enabled==enabled)
                count++;
        return count;
    }
    void addOthers(){
        Log.d(TAG, "addOthers...");
        BandInfo[] bandInfos=Device.findBandInfo();
        for (BandInfo bandInfo : bandInfos) {
            String platformId = bandInfo.getMacAddress();
            MicrosoftBandPlatform microsoftBandPlatform = getMicrosoftBandPlatform(platformId);
            if (microsoftBandPlatform == null) {
                Log.d(TAG,"addOthers..."+platformId);
                microsoftBandPlatform = new MicrosoftBandPlatform(context, platformId, null);
                microsoftBandPlatforms.add(microsoftBandPlatform);
            }
        }
    }

    public MicrosoftBandPlatform getMicrosoftBandPlatform(String platformId){
        for (int i = 0; i < microsoftBandPlatforms.size(); i++)
            if (microsoftBandPlatforms.get(i).equals(platformId))
                return microsoftBandPlatforms.get(i);
        return null;
    }

    public void readDataSourceFromFile(){
        try {
            Log.d(TAG, "readDataSourceFromFile()...");
            ArrayList<DataSource> dataSources = Files.readDataSourceFromFile(Constants.DIR_FILENAME);
            Log.d(TAG, "readDataSourceFromFile() .. datasource_size=" + dataSources.size());
            for (int i = 0; i < dataSources.size(); i++) {
                String platformId = dataSources.get(i).getPlatform().getId();
                String location = dataSources.get(i).getPlatform().getMetadata().get("location");
                MicrosoftBandPlatform microsoftBandPlatform = getMicrosoftBandPlatform(platformId);
                if (microsoftBandPlatform == null) {
                    microsoftBandPlatform = new MicrosoftBandPlatform(context, platformId, location);
                    microsoftBandPlatforms.add(microsoftBandPlatform);
                }
                microsoftBandPlatform.enable(dataSources.get(i));
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context,"Microsoft Band is not configured",Toast.LENGTH_LONG).show();
        }
        Log.d(TAG,"...readDataSourceFromFile()");
    }
    public void show(){
        for(int i=0;i<microsoftBandPlatforms.size();i++){
            microsoftBandPlatforms.get(i).show();
        }
    }
    public void deleteMicrosoftBandPlatform(String platformId) {
        for (int i = 0; i < microsoftBandPlatforms.size(); i++)
            if (microsoftBandPlatforms.get(i).equals(platformId)) {
                microsoftBandPlatforms.get(i).enabled=false;
                microsoftBandPlatforms.get(i).setLocation(null);
                microsoftBandPlatforms.get(i).resetDataSource();
            }
    }
    public ArrayList<MicrosoftBandPlatform> getMicrosoftBandPlatform() {
        return microsoftBandPlatforms;
    }
    public void writeDataSourceToFile() throws IOException {
        ArrayList<DataSource> dataSources = new ArrayList<>();
        if (microsoftBandPlatforms == null) throw new NullPointerException();
        if (microsoftBandPlatforms.size() == 0) throw new EmptyStackException();

        for (int i = 0; i < microsoftBandPlatforms.size(); i++) {
            Platform platform=microsoftBandPlatforms.get(i).getPlatform();
            for(int j=0;j<microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().size();j++) {
                DataSourceBuilder dataSourceBuilder=microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).getDataSourceBuilder();
                if(dataSourceBuilder==null) continue;
                dataSourceBuilder=dataSourceBuilder.setPlatform(platform);
                DataSource dataSource = dataSourceBuilder.build();
                dataSources.add(dataSource);
            }
        }
        Files.writeDataSourceToFile(Constants.DIRECTORY, Constants.FILENAME, dataSources);
    }

    public void register() {
        for(int i=0;i<microsoftBandPlatforms.size();i++) {
            microsoftBandPlatforms.get(i).register();
        }
    }
    public void unregister(){
        for(int i=0;i<microsoftBandPlatforms.size();i++) {
            microsoftBandPlatforms.get(i).unregister();
        }
    }
    public void disconnect(){
        for(int i=0;i<microsoftBandPlatforms.size();i++) {
            if (microsoftBandPlatforms.get(i).isConnected)
                microsoftBandPlatforms.get(i).disconnect();
        }

    }
}
