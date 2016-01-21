package org.md2k.microsoftband.sensors;

import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.SampleRate;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.microsoftband.CallBack;
import org.md2k.utilities.datakit.DataKitHandler;

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
public abstract class Sensor {
    Context context;
    String dataSourceType;
    String frequency;
    CallBack callBack;
    boolean enabled;
    int version;
    DataSourceClient dataSourceClient;


    Sensor(String dataSourceType,String frequency,int version){
        this.dataSourceType=dataSourceType;
        this.frequency=frequency;
        this.enabled=false;
        this.version=version;
    }
    public boolean equals(String dataSourceType){
        if(this.dataSourceType.equals(dataSourceType)) return true;
        return false;
    }
    public void setEnabled(boolean enable){
        enabled=enable;
    }
    public boolean isEnabled(){
        return enabled;
    }
    public String getDataSourceType(){
        return dataSourceType;
    }
    HashMap<String, String> createDataDescriptor(String name, String description, String unit, String frequency, String dataType, String minValue, String maxValue) {
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, name);
        dataDescriptor.put(METADATA.DESCRIPTION, description);
        dataDescriptor.put(METADATA.MIN_VALUE, minValue);
        dataDescriptor.put(METADATA.MAX_VALUE, maxValue);
        dataDescriptor.put(METADATA.UNIT, unit);
        dataDescriptor.put(METADATA.FREQUENCY, frequency);
        dataDescriptor.put(METADATA.DATA_TYPE, dataType);
        return dataDescriptor;
    }
    public abstract void register(Context context,BandClient bandClient, Platform platform,CallBack callBack);
    public abstract void unregister(Context context, BandClient bandClient);
    public abstract DataSourceBuilder createDataSourceBuilder(Platform Platform);

    public String getFrequency() {
        return frequency;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public int getVersion() {
        return version;
    }
    public void setFrequency(String frequency){
        this.frequency=frequency;
    }
    public void registerDataSource(Context context, Platform platform){
        this.context=context;
        dataSourceClient= DataKitHandler.getInstance(context).register(createDataSourceBuilder(platform));
    }
    public void unregisterDataSource(Context context){
        DataKitHandler.getInstance(context).unregister(dataSourceClient);
    }
    public void sendData(DataType dataType){
        DataKitHandler.getInstance(context).insert(dataSourceClient,dataType);
    }
}
