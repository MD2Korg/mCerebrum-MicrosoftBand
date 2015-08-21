package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

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
@SuppressWarnings("deprecation")
public class ActivityMicrosoftBandPlatformSettings extends PreferenceActivity {
/*    public static final String TAG = ActivityMicrosoftBandPlatformSettings.class.getSimpleName();
    String platformId="";
    MySharedPreference mySharedPreference;
    MicrosoftBandPlatform microsoftBandPlatform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        platformId=getIntent().getStringExtra("platformId");
        createMySharedPreference();
        getMicrosoftBandPlatform();
        setContentView(R.layout.activity_microsoftband_platform_settings);
        addPreferencesFromResource(R.xml.pref_microsoftband_platform);
        addPreferenceScreenSensors();
        prepareMySharedPreference();
        updatePreferenceScreen();
        setAddButton();
        setCancelButton();
    }
    private void getMicrosoftBandPlatform() {
        microsoftBandPlatform = MicrosoftBandPlatforms.getInstance(getBaseContext()).find(platformId);
    }
    void prepareMySharedPreference() {
        mySharedPreference.setSharedPreferencesString("platformId", microsoftBandPlatform.getPlatformId());
        mySharedPreference.setSharedPreferencesString("platformName", microsoftBandPlatform.getPlatformName());
        mySharedPreference.setSharedPreferencesString("location", microsoftBandPlatform.getLocation());
        for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
            String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
            boolean enabled = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).isEnabled();
            mySharedPreference.setSharedPreferencesBoolean(dataSourceType, enabled);
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                double frequency = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getFrequency();
                mySharedPreference.setSharedPreferencesString(dataSourceType + "_frequency", String.valueOf(frequency) + " Hz");
            }
        }
    }

    void updatePreferenceScreen() {
        findPreference("platformName").setSummary(mySharedPreference.getSharedPreferenceString("platformName"));
        findPreference("platformName").setEnabled(false);
        findPreference("platformId").setSummary(mySharedPreference.getSharedPreferenceString("platformId"));
        findPreference("platformId").setEnabled(false);
        findPreference("location").setSummary(mySharedPreference.getSharedPreferenceString("location"));
        ListPreference lpLocation=(ListPreference)findPreference("location");
        lpLocation.setValue(mySharedPreference.getSharedPreferenceString("location"));

        for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
            String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
            ((SwitchPreference) findPreference(dataSourceType)).setChecked(mySharedPreference.getSharedPreferenceBoolean(dataSourceType));
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                findPreference(dataSourceType).setSummary(mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency"));
                findPreference(dataSourceType).setDefaultValue(mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency"));
            }
        }
    }
    private void createMySharedPreference(){
        mySharedPreference = MySharedPreference.getInstance(getBaseContext());
        mySharedPreference.setListener(onSharedPreferenceChangeListener);
    }


    private void addPreferenceScreenSensors() {
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
        Log.d(TAG, "Preference category: " + preferenceCategory);
        preferenceCategory.removeAll();
        for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
            final String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
            SwitchPreference switchPreference = new SwitchPreference(this);
            switchPreference.setKey(dataSourceType);
            String title=dataSourceType;
            title=title.replace("_"," ");
            title=title.substring(0,1).toUpperCase() + title.substring(1).toLowerCase();
            switchPreference.setTitle(title);
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SwitchPreference switchPreference = (SwitchPreference) preference;
                        switchPreference.setChecked(!switchPreference.isChecked());
                        String[] string = {"7.125 Hz", "31.25 Hz", "62.5 Hz"};
                        String curFreq=mySharedPreference.getSharedPreferenceString(dataSourceType+"_frequency");
                        int curIndex=1;
                        for(int ii=0;ii<string.length;ii++)
                            if(curFreq.equals(string[ii])) curIndex=ii;
                        AlertDialogFrequency(ActivityMicrosoftBandPlatformSettings.this, string, preference.getKey() + "_frequency",curIndex);
                        return false;
                    }
                });
            }
            preferenceCategory.addPreference(switchPreference);
        }
    }

    private void setAddButton() {
        final Button button = (Button) findViewById(R.id.button_settings_platform_add);
        final String platformId = mySharedPreference.getSharedPreferenceString("platformId");
        if (MicrosoftBandPlatforms.getInstance(getBaseContext()).find(platformId).enabled) {
            button.setText("Update");
        } else {
            button.setText("Add");
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean enabled=false;
                final String location = mySharedPreference.getSharedPreferenceString("location");
                Log.d(TAG, "platformId=" + platformId + " Location=" + location);
                if (platformId == null || platformId.equals("")) {
                    Toast.makeText(getBaseContext(), "!!! Device ID is missing !!!", Toast.LENGTH_LONG).show();
                } else if (location == null || location.equals("")) {
                    Toast.makeText(getBaseContext(), "!!! Location is missing !!!", Toast.LENGTH_LONG).show();
                } else {
                    MicrosoftBandPlatform microsoftBandPlatform = MicrosoftBandPlatforms.getInstance(getBaseContext()).find(platformId);
                    microsoftBandPlatform.setLocation(mySharedPreference.getSharedPreferenceString("location"));
                    for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
                        String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
                        enabled=(enabled|(mySharedPreference.getSharedPreferenceBoolean(dataSourceType)));
                        microsoftBandPlatform.getMicrosoftBandDataSource().get(i).setEnabled(mySharedPreference.getSharedPreferenceBoolean(dataSourceType));
                        if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                            String frequencyStr=mySharedPreference.getSharedPreferenceString(dataSourceType+"_frequency");
                            double frequency=Double.valueOf(frequencyStr.substring(0,frequencyStr.length()-3));
                                microsoftBandPlatform.getMicrosoftBandDataSource().get(i).setFrequency(frequency);
                        }
                    }
                    if(!enabled){
                        Toast.makeText(getBaseContext(), "!!! No Sensor is enabled !!!", Toast.LENGTH_LONG).show();
                    }else {
                        microsoftBandPlatform.enabled=true;
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });
    }

    private void setCancelButton() {
        final Button button = (Button) findViewById(R.id.button_settings_platform_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }
    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "location":
                    findPreference("location").setSummary(mySharedPreference.getSharedPreferenceString("location"));
                    ListPreference lpLocation=(ListPreference)findPreference("location");
                    lpLocation.setValue(mySharedPreference.getSharedPreferenceString("location"));
                    break;
                case DataSourceType.ACCELEROMETER + "_frequency":
                    findPreference(DataSourceType.ACCELEROMETER).setSummary(mySharedPreference.getSharedPreferenceString(key));
                    break;
                case DataSourceType.GYROSCOPE + "_frequency":
                    findPreference(DataSourceType.GYROSCOPE).setSummary(mySharedPreference.getSharedPreferenceString(key));
                    break;
            }
        }
    };

    public void AlertDialogFrequency(final Context context, String[] string, final String key, final int curIndex) {
        Log.d(TAG, "Context=" + context);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Select Frequency");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.select_dialog_singlechoice);
        for (String aString : string) arrayAdapter.add(aString);
        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setSingleChoiceItems(arrayAdapter,curIndex,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        MySharedPreference.getInstance(context).setSharedPreferencesString(key, strName);
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }
*/}
