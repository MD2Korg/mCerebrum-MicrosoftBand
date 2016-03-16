package org.md2k.microsoftband.notification;

import android.content.Context;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.microsoftband.MicrosoftBand;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.Notification;

import java.util.ArrayList;

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
public class NotificationManager {
    private static final String TAG = NotificationManager.class.getSimpleName();
    private DataKitAPI dataKitAPI;
    private Context context;
    private ArrayList<MicrosoftBand> microsoftBands;
    private ArrayList<DataSourceClient> dataSourceClientArrayList;
//    private Handler handler;

    public NotificationManager(Context context, ArrayList<MicrosoftBand> microsoftBands) {
//        handler = new Handler();
        this.context = context;
        this.microsoftBands = microsoftBands;
        dataSourceClientArrayList = null;
        subscribe();
    }

    public void clear() {
        unsubscribe();
    }

    private void unsubscribe() {
        if (dataSourceClientArrayList != null)
            for (int i = 0; i < dataSourceClientArrayList.size(); i++)
                dataKitAPI.unsubscribe(dataSourceClientArrayList.get(i));
    }

    private void subscribe() {
        Log.d(TAG, "NotificationManager : subscribe()");
        dataKitAPI = DataKitAPI.getInstance(context);
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION);
        dataSourceClientArrayList = dataKitAPI.find(dataSourceBuilder);
        Log.d(TAG, "datasourceclient=" + dataSourceClientArrayList.size());
        if (dataSourceClientArrayList.size() > 0) {
            for (int i = 0; i < dataSourceClientArrayList.size(); i++) {
                Log.d(TAG, "ds_id=" + dataSourceClientArrayList.get(i).getDs_id());
                final int finalI = i;
                dataKitAPI.subscribe(dataSourceClientArrayList.get(i), new OnReceiveListener() {
                    @Override
                    public void onReceived(final DataType dataType) {
                        processMessage(dataSourceClientArrayList.get(finalI),dataType);
                    }
                });
            }
        }
    }

    private void processMessage(DataSourceClient dataSourceClient, DataType dataType) {
        DataTypeString dataTypeString = (DataTypeString) dataType;
        Log.d(TAG, "onReceived=" + dataTypeString.getSample());
        Gson gson = new Gson();
        Notification notification = gson.fromJson(dataTypeString.getSample(), Notification.class);
        if (notification.getOperation() != Notification.OPERATION.SEND) return;
        if (notification.getDataSource().getPlatform() == null) return;
        if (notification.getDataSource().getPlatform().getType() == null) return;
        if (!notification.getDataSource().getPlatform().getType().equals(PlatformType.MICROSOFT_BAND))
            return;

        if (notification.getDataSource().getPlatform().getId() != null) {
            for (int i = 0; i < microsoftBands.size(); i++) {
                if (microsoftBands.get(i).getPlatformId().equals(notification.getDataSource().getPlatform().getId())) {
                    microsoftBands.get(i).setNotification(notification);
                    microsoftBands.get(i).alarm();
                }
            }
        } else {
            for (int i = 0; i < microsoftBands.size(); i++) {
                microsoftBands.get(i).setNotification(notification);
                microsoftBands.get(i).alarm();
            }
        }
        notification.setOperation(Notification.OPERATION.DELIVER_SUCCESS);
        DataTypeString dataTypeString1=new DataTypeString(DateTime.getDateTime(),gson.toJson(notification));
        DataKitAPI.getInstance(context).insert(dataSourceClient,dataTypeString1);
    }
}
