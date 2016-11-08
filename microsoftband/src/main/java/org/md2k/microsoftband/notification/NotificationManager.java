package org.md2k.microsoftband.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.microsoftband.Constants;
import org.md2k.microsoftband.MicrosoftBand;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationRequests;

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
    DataSourceClient dataSourceClientAcknowledge;
    private Context context;
    private ArrayList<MicrosoftBand> microsoftBands;
    private ArrayList<DataSourceClient> dataSourceClietNotificationRequests;
    private Handler handler;
    int RERUN;
    Thread t;
    Runnable runnableSubscribe = new Runnable() {
        @Override
        public void run() {
            Application application = new ApplicationBuilder().setId("org.md2k.notificationmanager").build();
            DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST).setApplication(application);
            try {
                dataSourceClietNotificationRequests = DataKitAPI.getInstance(context).find(dataSourceBuilder);
                Log.d(TAG, "datasourceclient=" + dataSourceClietNotificationRequests.size());
                if (dataSourceClietNotificationRequests.size() == 0) {
                    if (RERUN > 0) {
                        RERUN--;
                        handler.postDelayed(this, 1000);
                    } else handler.postDelayed(this, 60000);
                } else {
                    subscribe();
                }
            } catch (DataKitException e) {
                Intent intent = new Intent(Constants.INTENT_STOP);
                intent.putExtra("type", "NotificationManager.java...runnableSubscribe()");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    };

    public NotificationManager(Context context, ArrayList<MicrosoftBand> microsoftBands) {
        handler = new Handler();
        this.context = context;
        this.microsoftBands = microsoftBands;
    }

    public void start() {
        RERUN = 60;
        dataSourceClietNotificationRequests = null;
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_ACKNOWLEDGE);
        try {
            dataSourceClientAcknowledge = DataKitAPI.getInstance(context).register(dataSourceBuilder);
            handler.post(runnableSubscribe);
        } catch (DataKitException e) {
            Intent intent = new Intent(Constants.INTENT_STOP);
            intent.putExtra("type", "NotificationManager.java...start()");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public void stop() {
        handler.removeCallbacks(runnableSubscribe);
        dataSourceClientAcknowledge=null;
//        try {
//            if (dataSourceClientAcknowledge != null)
//                DataKitAPI.getInstance(context).unregister(dataSourceClientAcknowledge);
//        } catch (DataKitException e) {
//            e.printStackTrace();
//        }
        unsubscribe();
    }

    private void unsubscribe() {
        try {
            if (t != null && t.isAlive()) t.interrupt();
        } catch (Exception ignored) {
        }
        if (dataSourceClietNotificationRequests != null)
            for (int i = 0; i < dataSourceClietNotificationRequests.size(); i++) {
                try {
                    DataKitAPI.getInstance(context).unsubscribe(dataSourceClietNotificationRequests.get(i));
                } catch (Exception ignored) {
                }
            }
    }

    private void subscribe() {
        if (dataSourceClietNotificationRequests.size() > 0) {
            for (int i = 0; i < dataSourceClietNotificationRequests.size(); i++) {
                Log.d(TAG, "subscribe ... ds_id=" + dataSourceClietNotificationRequests.get(i).getDs_id());
                try {
                    DataKitAPI.getInstance(context).subscribe(dataSourceClietNotificationRequests.get(i), new OnReceiveListener() {
                        @Override
                        public void onReceived(final DataType dataType) {
                            t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        processMessage(dataType);
                                    } catch (DataKitException e) {
                                        Intent intent = new Intent(Constants.INTENT_STOP);
                                        intent.putExtra("type", "NotificationManager.java...subscribe()..after getting data");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    }
                                }
                            });
                            t.start();
                        }
                    });
                } catch (DataKitException e) {
                    Intent intent = new Intent(Constants.INTENT_STOP);
                    intent.putExtra("type", "NotificationManager.java...subscribe()");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }
        }
    }

    void processMessage(DataType dataType) throws DataKitException {
        DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataType;
        Gson gson = new Gson();
        DataKitAPI.getInstance(context).insert(dataSourceClientAcknowledge, dataTypeJSONObject);
        NotificationRequests notificationRequests = gson.fromJson(dataTypeJSONObject.getSample().toString(), NotificationRequests.class);
        for (int r = 0; r < notificationRequests.getNotification_option().size(); r++) {
            if (notificationRequests.getNotification_option().get(r).getDatasource().getPlatform() == null)
                continue;
            if (notificationRequests.getNotification_option().get(r).getDatasource().getPlatform().getType() == null)
                continue;
            if (!notificationRequests.getNotification_option().get(r).getDatasource().getPlatform().getType().equals(PlatformType.MICROSOFT_BAND))
                continue;
            Log.d(TAG, "microsoftbandNo=" + microsoftBands.size());
            if (notificationRequests.getNotification_option().get(r).getDatasource().getPlatform().getId() != null) {
                Log.d(TAG, "first...");
                for (int m = 0; m < microsoftBands.size(); m++) {
                    if (microsoftBands.get(m).getPlatformId().equals(notificationRequests.getNotification_option().get(r).getDatasource().getPlatform().getId())) {
                        switch (notificationRequests.getNotification_option().get(r).getType()) {
                            case NotificationRequest.VIBRATION:
                                microsoftBands.get(m).setNotificationRequestVibration(notificationRequests.getNotification_option().get(r));
                                microsoftBands.get(m).vibrate();
                                break;
                            case NotificationRequest.MESSAGE:
                                microsoftBands.get(m).setNotificationRequestMessage(notificationRequests.getNotification_option().get(r));
                                microsoftBands.get(m).sendMessage();
                                break;
                        }
                    }
                }
            } else {
                for (int m = 0; m < microsoftBands.size(); m++) {
                    Log.d(TAG, "second...i=" + m);
                    switch (notificationRequests.getNotification_option().get(m).getType()) {
                        case NotificationRequest.VIBRATION:
                            microsoftBands.get(m).setNotificationRequestVibration(notificationRequests.getNotification_option().get(r));
                            microsoftBands.get(m).vibrate();
                            break;
                        case NotificationRequest.MESSAGE:
                            microsoftBands.get(m).setNotificationRequestMessage(notificationRequests.getNotification_option().get(r));
                            microsoftBands.get(m).sendMessage();
                            break;
                    }
                }
            }
        }
    }
}
