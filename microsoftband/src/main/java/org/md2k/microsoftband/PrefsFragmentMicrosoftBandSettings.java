package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Apps;
import org.md2k.utilities.Report.Log;

import java.io.IOException;
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
public class PrefsFragmentMicrosoftBandSettings extends PreferenceFragment {
    private static final String TAG = PrefsFragmentMicrosoftBandSettings.class.getSimpleName();
    MicrosoftBands microsoftBands;
    public final int ADD_DEVICE = 1;
    MyBlueTooth myBlueTooth;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_microsoftband_general);
        initializeBluetoothConnection();
        if (!myBlueTooth.isEnabled()) {
            myBlueTooth.enable();
        } else
            microsoftBands = new MicrosoftBands(getActivity().getApplicationContext());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("background"));
    }

    void initializeBluetoothConnection() {
        myBlueTooth = new MyBlueTooth(getActivity(), new BlueToothCallBack() {
            @Override
            public void onConnected() {
                microsoftBands = new MicrosoftBands(getActivity().getApplicationContext());
                enablePage();
            }

            @Override
            public void onDisconnected() {
                disablePage();
            }
        });
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.dismiss();
            getActivity().finish();
        }
    };

    @Override
    public void onResume() {
        if (myBlueTooth.isEnabled()) {
            microsoftBands.addOthers();
            enablePage();
        } else disablePage();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        myBlueTooth.close();
        if (microsoftBands != null)
            microsoftBands.disconnect();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void clearPreferenceScreenMicrosoftBand() {
        ((PreferenceCategory) findPreference("microsoftband_configured")).removeAll();
        ((PreferenceCategory) findPreference("microsoftband_available")).removeAll();
    }

    private synchronized void setupPreferenceScreenMicrosoftBand() {
        clearPreferenceScreenMicrosoftBand();
        final ArrayList<MicrosoftBand> microsoftBands = this.microsoftBands.find();
        for (int i = 0; i < microsoftBands.size(); i++) {
            Log.d(TAG, "i=" + i + " " + microsoftBands.get(i).getDeviceId() + " " + microsoftBands.get(i).enabled);
            final int finalI = i;
            Preference preference = new Preference(getActivity());
            preference.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_watch_teal_48dp));
            preference.setKey(microsoftBands.get(finalI).getDeviceId());
            preference.setOnPreferenceClickListener(microsoftBandListener());
            preference.setTitle(microsoftBands.get(finalI).getPlatformName());
            preference.setSummary("ERROR: BandOff/PairProblem");
//            PreferenceCategory preferenceCategory = ((PreferenceCategory) findPreference("microsoftband_configured"));
//            if (preferenceCategory.findPreference(preference.getKey()) != null) {
//                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
            //           }
//            preferenceCategory = (PreferenceCategory) findPreference("microsoftband_available");
//            if (preferenceCategory.findPreference(preference.getKey()) != null) {
//                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
//            }
            Log.d(TAG, " enabled=" + microsoftBands.get(finalI).enabled);

            if (microsoftBands.get(finalI).enabled) {
                ((PreferenceCategory) findPreference("microsoftband_configured")).addPreference(preference);
                Log.d(TAG, microsoftBands.get(finalI).getDeviceId() + "add to microsoftband_configured");
            } else {
                ((PreferenceCategory) findPreference("microsoftband_available")).addPreference(preference);
                Log.d(TAG, microsoftBands.get(finalI).getDeviceId() + "add to microsoftband_available");
            }
            microsoftBands.get(i).connect(new BandCallBack() {
                @Override
                public void onBandConnected() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updatePreference(microsoftBands.get(finalI));
                            microsoftBands.get(finalI).disconnect();
                        }
                    });
                }
            });
        }
    }

    void updatePreference(MicrosoftBand microsoftBand) {
        Preference preference = findPreference(microsoftBand.getDeviceId());
        preference.setTitle(microsoftBand.getPlatformName());
        preference.setSummary(getLocationSummary(microsoftBand));
    }
    private Preference.OnPreferenceClickListener microsoftBandListener() {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isNew;
                final String deviceId = preference.getKey();
                final Intent intent = new Intent(getActivity(), ActivityMicrosoftBandPlatformSettings.class);
                MicrosoftBand microsoftBand = microsoftBands.find(deviceId);
                setSharedPreference(microsoftBand);

                Log.d(TAG, "deviceId=" + deviceId);
                isNew = microsoftBand.enabled;
                Log.d(TAG, deviceId + " " + microsoftBand.enabled);
                if (isNew) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Edit/Delete Selected Device");
                    alertDialog.setMessage("Edit/Delete Device (" + preference.getTitle() + ")?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    microsoftBands.deleteMicrosoftBandPlatform(deviceId);
                                    setupPreferenceScreenMicrosoftBand();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Edit",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivityForResult(intent, ADD_DEVICE);
                                }
                            });

                    alertDialog.show();
                } else {
                    startActivityForResult(intent, ADD_DEVICE);
                }
                return false;
            }
        };
    }

    void setSharedPreference(MicrosoftBand microsoftBand) {
        MySharedPreference mySharedPreference = MySharedPreference.getInstance(getActivity());
        mySharedPreference.setSharedPreferencesString("deviceId", microsoftBand.getDeviceId());
        mySharedPreference.setSharedPreferencesString("platformName", microsoftBand.getPlatformName());
        mySharedPreference.setSharedPreferencesString("platformId", microsoftBand.getPlatformId());
        mySharedPreference.setSharedPreferencesBoolean("enabled", microsoftBand.enabled);
        mySharedPreference.setSharedPreferencesString("version", microsoftBand.versionHardware);

        for (int i = 0; i < microsoftBand.getSensors().size(); i++) {
            String dataSourceType = microsoftBand.getSensors().get(i).getDataSourceType();
            boolean enabled = microsoftBand.getSensors().get(i).isEnabled();
            mySharedPreference.setSharedPreferencesBoolean(dataSourceType, enabled);
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                String frequency = microsoftBand.getSensors().get(i).getFrequency();
                mySharedPreference.setSharedPreferencesString(dataSourceType + "_frequency", frequency);
            }
        }
    }

    void getSharedPreference() {
        MySharedPreference mySharedPreference = MySharedPreference.getInstance(getActivity());
        String deviceId = mySharedPreference.getSharedPreferenceString("deviceId");
        MicrosoftBand microsoftBand = microsoftBands.find(deviceId);
        microsoftBand.platformId = mySharedPreference.getSharedPreferenceString("platformId");
        microsoftBand.enabled = true;

        for (int i = 0; i < microsoftBand.getSensors().size(); i++) {
            String dataSourceType = microsoftBand.getSensors().get(i).getDataSourceType();
            microsoftBand.getSensors().get(i).setEnabled(mySharedPreference.getSharedPreferenceBoolean(dataSourceType));
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                String frequency = mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency");
                microsoftBand.getSensors().get(i).setFrequency(frequency);
            }
        }
    }

    void enablePage() {
        Log.d(TAG, "enable page");
        setupPreferenceScreenBluetooth(true);
        setupPreferenceScreenMicrosoftBand();
        setSaveButton(true);
        setCancelButton();
    }

    void disablePage() {
        if (microsoftBands != null) microsoftBands.unregister();
        microsoftBands = null;
        setupPreferenceScreenBluetooth(false);
        clearPreferenceScreenMicrosoftBand();
        setSaveButton(false);
        setCancelButton();
    }

    public void setupPreferenceScreenBluetooth(final boolean bluetoothEnabled) {
        Preference preference = findPreference("bluetooth_onoff");
        if (bluetoothEnabled) preference.setSummary("ON");
        else preference.setSummary("OFF (click to turn on)");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!bluetoothEnabled) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
                return true;
            }
        });
        preference = findPreference("bluetooth_pair");
        preference.setEnabled(bluetoothEnabled);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_DEVICE) {
            if (resultCode == getActivity().RESULT_OK) {
                getSharedPreference();
                Log.d(TAG, "result add");
                setupPreferenceScreenMicrosoftBand();
            }
        }
    }


    private void updateMicrosoftBand() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int count = 0;
        for (int i = 0; i < microsoftBands.find().size(); i++)
            if (microsoftBands.find().get(i).enabled) count++;
        if (count == 0)
            getActivity().finish();
        else
            builder.setMessage("Configure MicrosoftBand (Change Background)?").setPositiveButton("Yes", dialogChangeBackgroundListener)
                    .setNegativeButton("No", dialogChangeBackgroundListener).show();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Intent intent = new Intent(getActivity(), ServiceMicrosoftBands.class);
                    getActivity().stopService(intent);
                    saveConfigurationFile();
                    intent = new Intent(getActivity(), ServiceMicrosoftBands.class);
                    getActivity().startService(intent);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(getActivity(), "Configuration file is not saved.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    break;
            }
        }
    };
    DialogInterface.OnClickListener dialogChangeBackgroundListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    for (int i = 0; i < microsoftBands.find().size(); i++) {
                        if (!microsoftBands.find().get(i).enabled)
                            continue;
                        final String location = microsoftBands.find().get(i).getPlatformId();
                        if (location == null) continue;
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Updating Background theme ... (" + location.toLowerCase().replace("_", " ") + ")");
                        progressDialog.show();
                        final int finalI = i;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                microsoftBands.find().get(finalI).configureMicrosoftBand(location);
                            }
                        });
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    getActivity().finish();
                    break;
            }
        }
    };
    String getLocationSummary(MicrosoftBand microsoftBand){
        String summary;
        if (microsoftBand.getPlatformId() != null)
            summary = microsoftBand.getPlatformId().toLowerCase().replace("_", " ");
        else summary = "";
        if (Constants.LEFT_WRIST.equals(microsoftBand.getPlatformId()))
            summary = "Left Wrist";
        else if (Constants.RIGHT_WRIST.equals(microsoftBand.getPlatformId()))
            summary = "Right Wrist";
        return summary;
    }

    void saveConfigurationFile() {
        try {
            microsoftBands.writeDataSourceToFile();
            updateMicrosoftBand();

            Toast.makeText(getActivity(), "Configuration file is saved.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "!!!Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            getActivity().finish();
        }
    }

    private void setSaveButton(boolean bluetoothEnabled) {
        final Button button = (Button) getActivity().findViewById(R.id.button_1);
        button.setText("Save");
        button.setEnabled(bluetoothEnabled);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Apps.isServiceRunning(getActivity(), Constants.SERVICE_NAME)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Save configuration file and restart the MicrosoftBand Service?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    saveConfigurationFile();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    private void setCancelButton() {
        final Button button = (Button) getActivity().findViewById(R.id.button_2);
        button.setText("Cancel");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (microsoftBands != null) {
                    microsoftBands.unregister();
                    microsoftBands = null;
                }
                getActivity().finish();
            }
        });
    }
}
