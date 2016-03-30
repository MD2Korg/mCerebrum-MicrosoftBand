package org.md2k.microsoftband.notification;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.microsoftband.MicrosoftBand;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationRequest;

import java.util.ArrayList;

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
public class NotificationManager {
    private static final String TAG = NotificationManager.class.getSimpleName();
    private DataKitAPI dataKitAPI;
    private Context context;
    private ArrayList<MicrosoftBand> microsoftBands;
    private ArrayList<DataSourceClient> dataSourceClientArrayList;
    private Handler handler;
    DataSourceClient dataSourceClientDeliver;

    public NotificationManager(Context context, ArrayList<MicrosoftBand> microsoftBands) {
        handler = new Handler();
        this.context = context;
        this.microsoftBands = microsoftBands;
        dataSourceClientArrayList = null;
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_DELIVER);
        dataSourceClientDeliver= DataKitAPI.getInstance(context).register(dataSourceBuilder);
        handler.post(runnableSubscribe);
    }

    public void clear() {
        unsubscribe();
    }

    private void unsubscribe() {
        try {
            if (dataSourceClientArrayList != null)
                for (int i = 0; i < dataSourceClientArrayList.size(); i++) {
                    dataKitAPI.unsubscribe(dataSourceClientArrayList.get(i));
                }
        }catch(Exception e){

        }
    }
    Runnable runnableSubscribe=new Runnable() {
        @Override
        public void run() {
            dataKitAPI = DataKitAPI.getInstance(context);
            Application application=new ApplicationBuilder().setId("org.md2k.notificationmanager").build();
            DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST).setApplication(application);
            dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
            Log.d(TAG, "datasourceclient=" + dataSourceClientArrayList.size());
            if(dataSourceClientArrayList.size()==0)
                handler.postDelayed(runnableSubscribe,5000);
            else subscribe();
        }
    };

    private void subscribe() {
        if (dataSourceClientArrayList.size() > 0) {
            for (int i = 0; i < dataSourceClientArrayList.size(); i++) {
                Log.d(TAG, "ds_id=" + dataSourceClientArrayList.get(i).getDs_id());
                dataKitAPI.subscribe(dataSourceClientArrayList.get(i), new OnReceiveListener() {
                    @Override
                    public void onReceived(final DataType dataType) {
                        processMessage(dataType);
                    }
                });
            }
        }
    }

    void processMessage(DataType dataType) {
        DataTypeString dataTypeString = (DataTypeString) dataType;
        Log.d(TAG, "onReceived=" + dataTypeString.getSample());
        Gson gson = new Gson();
        NotificationRequest notificationRequest = gson.fromJson(dataTypeString.getSample(), NotificationRequest.class);
        if (notificationRequest.getDatasource().getPlatform() == null) return;
        if (notificationRequest.getDatasource().getPlatform().getType() == null) return;
        if (!notificationRequest.getDatasource().getPlatform().getType().equals(PlatformType.MICROSOFT_BAND))
            return;
        Log.d(TAG, "onReceived=" + dataTypeString.getSample());
        Log.d(TAG,"microsoftbandNo="+microsoftBands.size());
        if (notificationRequest.getDatasource().getPlatform().getId() != null) {
            Log.d(TAG,"first...");
            for (int i = 0; i < microsoftBands.size(); i++) {
                if (microsoftBands.get(i).getPlatformId().equals(notificationRequest.getDatasource().getPlatform().getId())) {
                    switch(notificationRequest.getType()) {
                        case NotificationRequest.VIBRATION:
                            microsoftBands.get(i).setNotificationRequestVibration(notificationRequest);
                            microsoftBands.get(i).setDataSourceClientDeliver(dataSourceClientDeliver);
                            microsoftBands.get(i).vibrate();
                            break;
                        case NotificationRequest.MESSAGE:
                            microsoftBands.get(i).setDataSourceClientDeliver(dataSourceClientDeliver);
                            microsoftBands.get(i).setNotificationRequestMessage(notificationRequest);
                            microsoftBands.get(i).sendMessage();
                            break;
                    }
                }
            }
        } else {
            for (int i = 0; i < microsoftBands.size(); i++) {
                Log.d(TAG,"second...i="+i);
                switch(notificationRequest.getType()) {
                    case NotificationRequest.VIBRATION:
                        microsoftBands.get(i).setDataSourceClientDeliver(dataSourceClientDeliver);
                        microsoftBands.get(i).setNotificationRequestVibration(notificationRequest);
                        microsoftBands.get(i).vibrate();
                        break;
                    case NotificationRequest.MESSAGE:
                        microsoftBands.get(i).setDataSourceClientDeliver(dataSourceClientDeliver);
                        microsoftBands.get(i).setNotificationRequestMessage(notificationRequest);
                        microsoftBands.get(i).sendMessage();
                        break;
                }
            }
        }
    }
}
