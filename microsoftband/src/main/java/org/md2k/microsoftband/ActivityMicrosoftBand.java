package org.md2k.microsoftband;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
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
public class ActivityMicrosoftBand extends Activity {
    private static final String TAG = ActivityMicrosoftBand.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoftband);
        Log.d(TAG, "OnCreate()");
        setupButtonSettings();
    }
    private void setupButtonSettings() {
        final Button button_settings = (Button) findViewById(R.id.button_settings);
        button_settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMicrosoftBand.this, ActivityMicrosoftBandSettings.class);
                startActivity(intent);
            }
        });
    }


/*    HashMap<String, TextView> hm = new HashMap<>();
    long starttimestamp = 0;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("operation").equals("data")) {
                TextView tv;
                LinearLayout ll = (LinearLayout) findViewById(R.id.LL_data);
                int count = intent.getIntExtra("count", 0);
                String platformId = intent.getStringExtra("platformId");
                platformId = platformId.substring(0, 2);
                String dataSourceType = intent.getStringExtra("dataSourceType");
                long curtimestamp = intent.getLongExtra("timestamp", 0);
                if (starttimestamp == 0) starttimestamp = curtimestamp;
                double expected = 31.25 * (curtimestamp - starttimestamp) / 1000;
                double missing = 100.0 * (expected - count) / expected;
                String key = platformId + "_" + dataSourceType;
                if (hm.containsKey(key)) {
                    tv = hm.get(key);
                } else {
                    tv = new TextView(getApplicationContext());
                    hm.put(key, tv);
                    ll.addView(tv);
                }
                tv.setText(key + " " + String.format("%.1f%% count=%d", missing, count));

                final TextView v = (TextView) findViewById(R.id.textView_all);
                v.setText("Time: " + String.format("%.1f Minute (%.0f Second)", ((double) curtimestamp - (double) starttimestamp) / (1000.0 * 60.0), ((double) curtimestamp - (double) starttimestamp) / (1000.0)));
            }
            else if(intent.getStringExtra("operation").equals("connection")){
                Log.d(TAG,"Broadcast msg onReceive()...connection...");
                   setButtons();
            }
        }
    };
    */
    TableRow createDefaultRow(){
        TableRow row = new TableRow(this);
        TextView tvSensor = new TextView(this);tvSensor.setText("sensor");tvSensor.setTypeface(null, Typeface.BOLD);tvSensor.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvCount = new TextView(this);tvCount.setText("count");tvCount.setTypeface(null, Typeface.BOLD);tvCount.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvFreq = new TextView(this);tvFreq.setText("freq.");tvFreq.setTypeface(null, Typeface.BOLD);tvFreq.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView tvSample = new TextView(this);tvSample.setText("samples");tvSample.setTypeface(null, Typeface.BOLD);tvSample.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        row.addView(tvSensor);
        row.addView(tvCount);
        row.addView(tvFreq);
        row.addView(tvSample);
        return row;
    }

    void prepareTable() {
        ArrayList<MicrosoftBandPlatform> microsoftBandPlatforms=MicrosoftBandPlatforms.getInstance(ActivityMicrosoftBand.this).getMicrosoftBandPlatform();
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.removeAllViews();
        ll.addView(createDefaultRow());
        for (int i = 0; i < microsoftBandPlatforms.size(); i++) {
            if (!microsoftBandPlatforms.get(i).enabled) continue;
            for(int j=0;j<microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().size();j++){
                if(!microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).isEnabled()) continue;
                String id = microsoftBandPlatforms.get(i).platformId+":"+microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).getDataSourceType();
                String sensorname=microsoftBandPlatforms.get(i).platformName.substring(0,4)+":"+microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).getDataSourceType().toLowerCase();
                TableRow row = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);
                TextView tvSensor = new TextView(this);
                tvSensor.setText(sensorname);
                TextView tvCount = new TextView(this);
                tvCount.setText("0");
                hm.put(id + "_count", tvCount);
                TextView tvFreq = new TextView(this);
                tvFreq.setText("0");
                hm.put(id + "_freq", tvFreq);
                TextView tvSample = new TextView(this);
                tvSample.setText("0");
                hm.put(id + "_sample", tvSample);
                row.addView(tvSensor);
                row.addView(tvCount);
                row.addView(tvFreq);
                row.addView(tvSample);
                row.setBackgroundResource(R.drawable.border);
                ll.addView(row);
            }
        }

    }

    void showActiveSensors() {
        TextView textView = (TextView) findViewById(R.id.configuration_info);
        String str = "";
        int count = 0;
        ArrayList<MicrosoftBandPlatform> microsoftBandPlatforms=MicrosoftBandPlatforms.getInstance(ActivityMicrosoftBand.this).getMicrosoftBandPlatform();
        for (int i = 0; i < microsoftBandPlatforms.size(); i++) {
            if (!microsoftBandPlatforms.get(i).enabled) continue;
            for(int j=0;j<microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().size();j++){
                if(!microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).isEnabled()) continue;
                if (count % 3 == 0 && count != 0) str = str + "\n";
                else if (count != 0) str = str + "   ";
                String platformName=microsoftBandPlatforms.get(i).platformName;
                String dataSourceType=microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).getDataSourceType();
                double freq=microsoftBandPlatforms.get(i).getMicrosoftBandDataSource().get(j).getFrequency();
                str=str+platformName.substring(0,2)+":"+dataSourceType.toLowerCase();
                if(freq!=0) str+="(freq="+String.format("%.1f",freq)+")";
                count++;
            }
        }
        textView.setText(str);
    }

    void serviceStatus() {
        TextView textView = (TextView) findViewById(R.id.service_info);
        if (ServiceMicrosoftBands.isRunning) textView.setText("Running");
        else textView.setText("Not Running");
    }

    private void setupButtonService() {
        final Button buttonStopService = (Button) findViewById(R.id.button_stopservice);
        final Button buttonStartService = (Button) findViewById(R.id.button_startservice);
        buttonStartService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ServiceMicrosoftBands.isRunning) {
                    starttimestamp = 0;
                    Intent intent = new Intent(ActivityMicrosoftBand.this, ServiceMicrosoftBands.class);
                    startService(intent);
                    TextView textView = (TextView) findViewById(R.id.service_info);
                    textView.setText("Running");
                }
            }
        });
        buttonStopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ServiceMicrosoftBands.isRunning) {
                    Intent intent = new Intent(ActivityMicrosoftBand.this, ServiceMicrosoftBands.class);
                    stopService(intent);
                    TextView textView = (TextView) findViewById(R.id.service_info);
                    textView.setText("Not Running");
                }
            }
        });
    }

    long starttimestamp = 0;
    HashMap<String, TextView> hm = new HashMap<>();
    void updateServiceStatus(){
        TextView textView = (TextView) findViewById(R.id.service_info);
        if (starttimestamp == 0) starttimestamp = DateTime.getDateTime();
        double minutes = ((double) (DateTime.getDateTime() - starttimestamp) / (1000 * 60));
        textView.setText("Running (" + String.format("%.2f", minutes) + " minutes)");
    }

    void updateTable(Intent intent){
        String sampleStr = "";
        String dataSourceType=intent.getStringExtra("datasourcetype");
        String platformId=intent.getStringExtra("platformid");

        String id=platformId+":"+dataSourceType;
        int count = intent.getIntExtra("count", 0);
        hm.get(id+"_count").setText(String.valueOf(count));

        double time = (intent.getLongExtra("timestamp", 0) - intent.getLongExtra("starttimestamp", 0)) / 1000.0;
        double freq = (double) count / time;
        hm.get(id+"_freq").setText(String.format("%.1f",freq));


        DataType data = (DataType) intent.getSerializableExtra("data");
        if (data instanceof DataTypeFloat) {
            sampleStr = String.format("%.1f", ((DataTypeFloat) data).getSample());
        } else if (data instanceof DataTypeFloatArray) {
            float[] sample = ((DataTypeFloatArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if(i%3==0 && i!=0) sampleStr+="\n";
                sampleStr = sampleStr + String.format("%.1f", sample[i]);
            }
        } else if (data instanceof DataTypeDoubleArray) {
            double[] sample = ((DataTypeDoubleArray) data).getSample();
            for (int i = 0; i < sample.length; i++) {
                if (i != 0) sampleStr += ",";
                if(i%3==0 && i!=0) sampleStr+="\n";
                sampleStr = sampleStr + String.format("%.1f", sample[i]);
            }
        }
        hm.get(id+"_sample").setText(sampleStr);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateServiceStatus();
            updateTable(intent);

        }
    };

    @Override
    public void onResume(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("microsoftBand"));
        serviceStatus();
        showActiveSensors();
        prepareTable();
        setupButtonService();

        super.onResume();
    }
    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()...");
        super.onDestroy();
        Log.d(TAG, "...onDestroy()");

    }
}
