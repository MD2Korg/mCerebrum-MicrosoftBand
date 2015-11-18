package org.md2k.microsoftband;

import android.content.Context;

import com.google.gson.Gson;

import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.datakit.DataKitHandler;

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
public class MicrosoftBandInfo {
    private static final String TAG = MicrosoftBandInfo.class.getSimpleName();
    Context context;
    DataSourceClient dataSourceClient;
    class Info{
        String VERSION_HARDWARE;
        String VERSION_FIRMWARE;
        String LOCATION;

        public Info(String VERSION_HARDWARE, String VERSION_FIRMWARE, String LOCATION) {
            this.VERSION_HARDWARE = VERSION_HARDWARE;
            this.VERSION_FIRMWARE = VERSION_FIRMWARE;
            this.LOCATION = LOCATION;
        }
    }
    MicrosoftBandInfo(Context context){
        this.context=context;
    }
    void register(Platform platform){
        DataSourceBuilder dataSourceBuilder=new DataSourceBuilder().setType(DataSourceType.INFO).setPlatform(platform);
        dataSourceClient=DataKitHandler.getInstance(context).register(dataSourceBuilder);
        Log.d(TAG,"ds_id="+dataSourceClient.getDs_id());
    }
    void updateData(String versionHardware, String versionFirmware,String location){
        Info info = new Info(versionHardware,versionFirmware,location);
        Gson gson=new Gson();
        Log.d(TAG, "info=" + gson.toJson(info));
        DataTypeString data=new DataTypeString(DateTime.getDateTime(),gson.toJson(info));
        DataKitHandler.getInstance(context).insert(dataSourceClient,data);
    }
}
