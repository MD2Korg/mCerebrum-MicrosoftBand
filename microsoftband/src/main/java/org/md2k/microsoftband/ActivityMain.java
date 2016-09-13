package org.md2k.microsoftband;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Apps;
import org.md2k.utilities.UI.ActivityAbout;
import org.md2k.utilities.UI.ActivityCopyright;
import org.md2k.utilities.permission.PermissionInfo;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

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
public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    private HashMap<String, TextView> hashMapData = new HashMap<>();
    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            {
                long time = Apps.serviceRunningTime(ActivityMain.this, Constants.SERVICE_NAME);
                if (time < 0) {
                    ((Button) findViewById(R.id.button_app_status)).setText("START");
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_off));

                } else {
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_on));
                    ((Button) findViewById(R.id.button_app_status)).setText(DateTime.convertTimestampToTimeStr(time));

                }
                mHandler.postDelayed(this, 1000);
            }
        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTable(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit, new Crashlytics());

        setContentView(R.layout.activity_main);
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    load();
                }
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void load() {
        final Button buttonService = (Button) findViewById(R.id.button_app_status);
        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ServiceMicrosoftBands.class);
                if (Apps.isServiceRunning(getBaseContext(), Constants.SERVICE_NAME)) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                intent = new Intent(this, ActivityMicrosoftBandSettings.class);
                startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(this, ActivityAbout.class);
                try {
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_CODE, String.valueOf(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_NAME, this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                break;
            case R.id.action_copyright:
                intent = new Intent(this, ActivityCopyright.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private TableRow createDefaultRow() {
        TableRow row = new TableRow(this);
        TextView tvSensor = new TextView(this);
        tvSensor.setText("sensor");
        tvSensor.setTypeface(null, Typeface.BOLD);
        tvSensor.setTextColor(getResources().getColor(R.color.teal_a700));
        TextView tvCount = new TextView(this);
        tvCount.setText("count");
        tvCount.setTypeface(null, Typeface.BOLD);
        tvCount.setTextColor(getResources().getColor(R.color.teal_a700));
        TextView tvFreq = new TextView(this);
        tvFreq.setText("freq.");
        tvFreq.setTypeface(null, Typeface.BOLD);
        tvFreq.setTextColor(getResources().getColor(R.color.teal_a700));
        TextView tvSample = new TextView(this);
        tvSample.setText("samples");
        tvSample.setTypeface(null, Typeface.BOLD);
        tvSample.setTextColor(getResources().getColor(R.color.teal_a700));
        row.addView(tvSensor);
        row.addView(tvCount);
        row.addView(tvFreq);
        row.addView(tvSample);
        return row;
    }

    private void prepareTable(ArrayList<MicrosoftBand> microsoftBands) {
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.removeAllViews();
        ll.addView(createDefaultRow());
        for (int i = 0; i < microsoftBands.size(); i++) {
            if (!microsoftBands.get(i).enabled) continue;
            for (int j = 0; j < microsoftBands.get(i).getSensors().size(); j++) {
                if (!microsoftBands.get(i).getSensors().get(j).isEnabled())
                    continue;
                String id = microsoftBands.get(i).platformId + ":" + microsoftBands.get(i).getSensors().get(j).getDataSourceType();
                TableRow row = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);
                TextView tvSensor = new TextView(this);
                String sname = microsoftBands.get(i).getPlatform().getType().toLowerCase()+"("+microsoftBands.get(i).getPlatform().getId().substring(0,1) + ")\n" + microsoftBands.get(i).getSensors().get(j).getDataSourceType().toLowerCase();
                tvSensor.setText(sname);
                TextView tvCount = new TextView(this);
                tvCount.setText("0");
                hashMapData.put(id + "_count", tvCount);
                TextView tvFreq = new TextView(this);
                tvFreq.setText("0");
                hashMapData.put(id + "_freq", tvFreq);
                TextView tvSample = new TextView(this);
                tvSample.setText("0");
                hashMapData.put(id + "_sample", tvSample);
                row.addView(tvSensor);
                row.addView(tvCount);
                row.addView(tvFreq);
                row.addView(tvSample);
                row.setBackgroundResource(R.drawable.border);
                ll.addView(row);
            }
        }
    }

    private void updateTable(Intent intent) {
        try {
            String sampleStr = "";
            String dataSourceType = intent.getStringExtra("datasourcetype");
            String platformId = intent.getStringExtra("platformid");

            String id = platformId + ":" + dataSourceType;
            int count = intent.getIntExtra("count", 0);
            if(hashMapData.containsKey(id+"_count"))
                hashMapData.get(id + "_count").setText(String.valueOf(count));

            double time = (intent.getLongExtra("timestamp", 0) - intent.getLongExtra("starttimestamp", 0)) / 1000.0;
            double freq = (double) count / time;
            if(hashMapData.containsKey(id+"_freq"))
                hashMapData.get(id + "_freq").setText(String.format("%.1f", freq));


            DataType data = intent.getParcelableExtra("data");
            if (data instanceof DataTypeFloat) {
                sampleStr = String.format("%.1f", ((DataTypeFloat) data).getSample());
            } else if (data instanceof DataTypeFloatArray) {
                float[] sample = ((DataTypeFloatArray) data).getSample();
                for (int i = 0; i < sample.length; i++) {
                    if (i != 0) sampleStr += ",";
                    if (i % 3 == 0 && i != 0) sampleStr += "\n";
                    sampleStr = sampleStr + String.format("%.1f", sample[i]);
                }
            } else if (data instanceof DataTypeDouble) {
                sampleStr = String.format("%.1f", ((DataTypeDouble) data).getSample());
            } else if (data instanceof DataTypeDoubleArray) {
                double[] sample = ((DataTypeDoubleArray) data).getSample();
                for (int i = 0; i < sample.length; i++) {
                    if (i != 0) sampleStr += ",";
                    if (i % 3 == 0 && i != 0) sampleStr += "\n";
                    sampleStr = sampleStr + String.format("%.1f", sample[i]);
                }
            } else if (data instanceof DataTypeInt) {
                sampleStr = String.format("%d", ((DataTypeInt) data).getSample());
            } else if (data instanceof DataTypeIntArray) {
                int[] sample = ((DataTypeIntArray) data).getSample();
                for (int i = 0; i < sample.length; i++) {
                    if (i != 0) sampleStr += ",";
                    if (i % 3 == 0 && i != 0) sampleStr += "\n";
                    sampleStr = sampleStr + String.format("%d", sample[i]);
                }
            }
            if(hashMapData.containsKey(id+"_sample"))
                hashMapData.get(id + "_sample").setText(sampleStr);
        } catch (Exception e) {
            Log.d("MicrosoftBand", "Exception is UI table update");
            Log.d("MicrosoftBand", e.getStackTrace().toString());
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.INTENT_RECEIVED_DATA));
        ArrayList<MicrosoftBand> microsoftBands = new MicrosoftBands(getApplicationContext()).find();
        prepareTable(microsoftBands);
        mHandler.post(runnable);
        super.onResume();
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        super.onDestroy();
        Log.d(TAG, "...onDestroy()");
    }
}
