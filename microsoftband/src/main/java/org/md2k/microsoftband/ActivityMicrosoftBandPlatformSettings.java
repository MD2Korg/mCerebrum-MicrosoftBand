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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

@SuppressWarnings("deprecation")
public class ActivityMicrosoftBandPlatformSettings extends PreferenceActivity {
    public static final String TAG = ActivityMicrosoftBandPlatformSettings.class.getSimpleName();
    String platformId="";
    MySharedPreference mySharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        platformId=getIntent().getStringExtra("platformId");
        createMySharedPreference();
        setContentView(R.layout.activity_microsoftband_platform_settings);
        addPreferencesFromResource(R.xml.pref_microsoftband_platform);
        addPreferenceScreenSensors();
        updatePreferenceScreen();
        setAddButton();
        setCancelButton();
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void updatePreferenceScreen() {
        findPreference("platformName").setSummary(mySharedPreference.getSharedPreferenceString("platformName"));
        findPreference("platformId").setSummary(mySharedPreference.getSharedPreferenceString("platformId"));
        findPreference("location").setSummary(mySharedPreference.getSharedPreferenceString("location").toLowerCase().replace("_"," "));
        ListPreference lpLocation=(ListPreference)findPreference("location");
        lpLocation.setValue(mySharedPreference.getSharedPreferenceString("location"));

        for (int i = 0; i < MicrosoftBandPlatform.DATASOURCETYPE.length; i++) {
            String dataSourceType = MicrosoftBandPlatform.DATASOURCETYPE[i];
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
        for (int i = 0; i < MicrosoftBandPlatform.DATASOURCETYPE.length; i++) {
            final String dataSourceType = MicrosoftBandPlatform.DATASOURCETYPE[i];
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
        if (mySharedPreference.getSharedPreferenceBoolean("enabled")) {
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
                    enabled=false;
                    for(int i=0;i<MicrosoftBandPlatform.DATASOURCETYPE.length;i++)
                        if(mySharedPreference.getSharedPreferenceBoolean(MicrosoftBandPlatform.DATASOURCETYPE[i]))
                            enabled=true;
                    mySharedPreference.setSharedPreferencesBoolean("enabled",enabled);
                    if(!enabled){
                        Toast.makeText(getBaseContext(), "!!! No Sensor is enabled !!!", Toast.LENGTH_LONG).show();
                    }else {
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
                    findPreference("location").setSummary(mySharedPreference.getSharedPreferenceString("location").toLowerCase().replace("_"," "));
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
}
