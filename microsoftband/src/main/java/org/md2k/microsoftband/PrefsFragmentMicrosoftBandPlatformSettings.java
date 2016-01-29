package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PrefsFragmentMicrosoftBandPlatformSettings extends PreferenceFragment {
    public static final String TAG = PrefsFragmentMicrosoftBandPlatformSettings.class.getSimpleName();
    String deviceId="";
    MySharedPreference mySharedPreference;
    ArrayList<DataSource> dataSourcesDefault;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId=getActivity().getIntent().getStringExtra("deviceId");
        readDefaultDataSources();
        createMySharedPreference();
        addPreferencesFromResource(R.xml.pref_microsoftband_platform);
        addPreferenceScreenSensors();
        updatePreferenceScreen();
        setAddButton();
        setCancelButton();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    int getBandVersion(){
        String version=mySharedPreference.getSharedPreferenceString("version");
        int versionInt=1;
        if(version==null)
            return 0;
        else {
            versionInt = Integer.valueOf(version);
            if (versionInt <= 19) return 1;
            else return 2;
        }
    }


    void updatePreferenceScreen() {
        int versionFirmwareInt=getBandVersion();
        findPreference("platformName").setSummary(mySharedPreference.getSharedPreferenceString("platformName"));
        findPreference("deviceId").setSummary(mySharedPreference.getSharedPreferenceString("deviceId"));
        findPreference("platformId").setSummary(getLocationSummary(mySharedPreference.getSharedPreferenceString("platformId")));
        ListPreference lpLocation=(ListPreference)findPreference("platformId");
        lpLocation.setValue(mySharedPreference.getSharedPreferenceString("platformId"));
        MicrosoftBand microsoftBand=new MicrosoftBand(getActivity().getApplicationContext(),null,null);

        for (int i = 0; i < microsoftBand.getSensors().size(); i++) {
            String dataSourceType = microsoftBand.getSensors().get(i).getDataSourceType();
            ((SwitchPreference) findPreference(dataSourceType)).setChecked(mySharedPreference.getSharedPreferenceBoolean(dataSourceType));
            if(microsoftBand.getSensors().get(i).getVersion()>versionFirmwareInt)
                findPreference(dataSourceType).setEnabled(false);
            else
                findPreference(dataSourceType).setEnabled(true);
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                findPreference(dataSourceType).setSummary(mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency"));
                findPreference(dataSourceType).setDefaultValue(mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency"));
            }
        }
    }
    private void createMySharedPreference(){
        mySharedPreference = MySharedPreference.getInstance(getActivity());
        mySharedPreference.setListener(onSharedPreferenceChangeListener);
    }


    private void addPreferenceScreenSensors() {
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("dataSourceType");
        Log.d(TAG, "Preference category: " + preferenceCategory);
        preferenceCategory.removeAll();
        int versionHardwareInt=getBandVersion();
        MicrosoftBand microsoftBand=new MicrosoftBand(getActivity().getApplicationContext(),null,null);
        for (int i = 0; i <microsoftBand.getSensors().size(); i++) {
            final String dataSourceType = microsoftBand.getSensors().get(i).getDataSourceType();
            SwitchPreference switchPreference = new SwitchPreference(getActivity());
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
                        String[] string = getResources().getStringArray(R.array.frequency_entries);//{"8 Hz", "31 Hz", "62 Hz"};
                        String curFreq=mySharedPreference.getSharedPreferenceString(dataSourceType+"_frequency");
                        int curIndex=1;
                        for(int ii=0;ii<string.length;ii++)
                            if(curFreq.equals(string[ii])) curIndex=ii;
                        AlertDialogFrequency(getActivity(), string, preference.getKey() + "_frequency",curIndex);
                        return false;
                    }
                });
            }
            if(microsoftBand.getSensors().get(i).getVersion()>versionHardwareInt)
                switchPreference.setEnabled(false);
            else
                switchPreference.setEnabled(true);
            preferenceCategory.addPreference(switchPreference);
        }
        if(dataSourcesDefault!=null)
            preferenceCategory.setEnabled(false);
        else preferenceCategory.setEnabled(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container,savedInstanceState);
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    private void setAddButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_1);
        button.setText("Save");
        final String deviceId = mySharedPreference.getSharedPreferenceString("deviceId");
        if (mySharedPreference.getSharedPreferenceBoolean("enabled")) {
            button.setText("Update");
        } else {
            button.setText("Add");
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean enabled;
                MicrosoftBand microsoftBand=new MicrosoftBand(getActivity().getApplicationContext(),null,null);
                final String location = mySharedPreference.getSharedPreferenceString("platformId");
                if (deviceId == null || deviceId.equals("")) {
                    Toast.makeText(getActivity(), "!!! Device ID is missing !!!", Toast.LENGTH_LONG).show();
                } else if (location == null || location.equals("")) {
                    Toast.makeText(getActivity(), "!!! Location is missing !!!", Toast.LENGTH_LONG).show();
                } else {
                    enabled=false;
                    for(int i=0;i< microsoftBand.getSensors().size();i++)
                        if(mySharedPreference.getSharedPreferenceBoolean(microsoftBand.getSensors().get(i).getDataSourceType()))
                            enabled=true;
                    mySharedPreference.setSharedPreferencesBoolean("enabled",enabled);
                    if(!enabled){
                        Toast.makeText(getActivity(), "!!! No Sensor is enabled !!!", Toast.LENGTH_LONG).show();
                    }else {
                        Intent returnIntent = new Intent();
                        getActivity().setResult(getActivity().RESULT_OK, returnIntent);
                        getActivity().finish();
                    }
                }
            }
        });
    }

    private void setCancelButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_2);
        button.setText("Cancel");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                getActivity().setResult(getActivity().RESULT_CANCELED, returnIntent);
                getActivity().finish();
            }
        });
    }
    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "platformId":
                    findPreference("platformId").setSummary(getLocationSummary(mySharedPreference.getSharedPreferenceString("platformId")));
                    ListPreference lpLocation=(ListPreference)findPreference("platformId");
                    lpLocation.setValue(mySharedPreference.getSharedPreferenceString("platformId"));
                    updateDefaultConfiguration();
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
    String getLocationSummary(String curSummary){
        String summary;
        if (curSummary != null)
            summary = curSummary.toLowerCase().replace("_", " ");
        else summary = "";
        if (Constants.LEFT_WRIST.equals(curSummary))
            summary = "Left Wrist";
        else if (Constants.RIGHT_WRIST.equals(curSummary))
            summary = "Right Wrist";
        return summary;
    }

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
    void readDefaultDataSources(){
        try {
            dataSourcesDefault=Configuration.readDefault();
        } catch (FileNotFoundException e) {
            dataSourcesDefault=null;
        }
    }
    void updateDefaultConfiguration(){
        if(dataSourcesDefault==null) return;
        String curPlatformId=mySharedPreference.getSharedPreferenceString("platformId");
        for(int i=0;i<dataSourcesDefault.size();i++){
            if(dataSourcesDefault.get(i).getPlatform().getId()!=null && !dataSourcesDefault.get(i).getPlatform().getId().equals(curPlatformId))
                continue;
            SwitchPreference switchPreference= (SwitchPreference) findPreference(dataSourcesDefault.get(i).getType());
            switchPreference.setChecked(true);
            mySharedPreference.setSharedPreferencesBoolean(dataSourcesDefault.get(i).getType(),true);
        }
    }
}
