package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
public class ActivityMicrosoftBandSettings extends PreferenceActivity {
    private static final String TAG = ActivityMicrosoftBandSettings.class.getSimpleName();
    MicrosoftBandPlatforms microsoftBandPlatforms;
    public final int ADD_DEVICE = 1;
    MyBlueTooth myBlueTooth;
    ProgressDialog progressDialog;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoftband_settings);
        addPreferencesFromResource(R.xml.pref_microsoftband_general);
        initializeBluetoothConnection();
        if (!myBlueTooth.isEnabled()) {
            myBlueTooth.enable();
        } else
            microsoftBandPlatforms = new MicrosoftBandPlatforms(ActivityMicrosoftBandSettings.this);


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("background"));
        if(getActionBar()!=null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    void initializeBluetoothConnection() {
        myBlueTooth = new MyBlueTooth(this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                microsoftBandPlatforms = new MicrosoftBandPlatforms(ActivityMicrosoftBandSettings.this);
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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.dismiss();
            finish();
        }
    };

    @Override
    protected void onResume() {
        if (myBlueTooth.isEnabled()) {
            microsoftBandPlatforms.addOthers();
            enablePage();
        }
        else disablePage();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        myBlueTooth.close();
        if (microsoftBandPlatforms != null)
            microsoftBandPlatforms.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void clearPreferenceScreenMicrosoftBand() {
        ((PreferenceCategory) findPreference("microsoftband_configured")).removeAll();
        ((PreferenceCategory) findPreference("microsoftband_available")).removeAll();
    }

    private synchronized void setupPreferenceScreenMicrosoftBand() {
        clearPreferenceScreenMicrosoftBand();
        final ArrayList<MicrosoftBandPlatform> microsoftBandPlatforms = this.microsoftBandPlatforms.getMicrosoftBandPlatform();
        for (int i = 0; i < microsoftBandPlatforms.size(); i++) {
            Log.d(TAG, "i=" + i + " " + microsoftBandPlatforms.get(i).getPlatformId() + " " + microsoftBandPlatforms.get(i).enabled);
            final int finalI = i;
            Preference preference = new Preference(ActivityMicrosoftBandSettings.this);
            preference.setKey(microsoftBandPlatforms.get(finalI).getPlatformId());
            preference.setOnPreferenceClickListener(microsoftBandListener());
            preference.setTitle(microsoftBandPlatforms.get(finalI).getPlatformName());
            preference.setSummary("ERROR: BandOff/PairProblem");
            PreferenceCategory preferenceCategory = ((PreferenceCategory) findPreference("microsoftband_configured"));
            if (preferenceCategory.findPreference(preference.getKey()) != null) {
                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
            }
            preferenceCategory = (PreferenceCategory) findPreference("microsoftband_available");
            if (preferenceCategory.findPreference(preference.getKey()) != null) {
                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
            }
            Log.d(TAG, " enabled=" + microsoftBandPlatforms.get(finalI).enabled);

            if (microsoftBandPlatforms.get(finalI).enabled) {
                ((PreferenceCategory) findPreference("microsoftband_configured")).addPreference(preference);
                Log.d(TAG, microsoftBandPlatforms.get(finalI).getPlatformId() + "add to microsoftband_configured");
            } else {
                ((PreferenceCategory) findPreference("microsoftband_available")).addPreference(preference);
                Log.d(TAG, microsoftBandPlatforms.get(finalI).getPlatformId() + "add to microsoftband_available");
            }
            microsoftBandPlatforms.get(i).connect(new BandCallBack() {
                @Override
                public void onBandConnected() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updatePreference(microsoftBandPlatforms.get(finalI));
                            microsoftBandPlatforms.get(finalI).disconnect();
                        }
                    });
                }
            });
        }
    }

    void updatePreference(MicrosoftBandPlatform microsoftBandPlatform) {
        Preference preference = findPreference(microsoftBandPlatform.getPlatformId());
        preference.setTitle(microsoftBandPlatform.getPlatformName());
        if (microsoftBandPlatform.getLocation() != null)
            preference.setSummary(microsoftBandPlatform.getLocation().toLowerCase().replace("_", " "));
        else preference.setSummary("");
    }

    private Preference.OnPreferenceClickListener microsoftBandListener() {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isNew;
                final String platformId = preference.getKey();
                final Intent intent = new Intent(ActivityMicrosoftBandSettings.this, ActivityMicrosoftBandPlatformSettings.class);
                MicrosoftBandPlatform microsoftBandPlatform = microsoftBandPlatforms.getMicrosoftBandPlatform(platformId);
                setSharedPreference(microsoftBandPlatform);

                Log.d(TAG, "platformId=" + platformId);
                isNew = microsoftBandPlatform.enabled;
                Log.d(TAG, platformId + " " + microsoftBandPlatform.enabled);
                if (isNew) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ActivityMicrosoftBandSettings.this).create();
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
                                    microsoftBandPlatforms.deleteMicrosoftBandPlatform(platformId);
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

    void setSharedPreference(MicrosoftBandPlatform microsoftBandPlatform) {
        MySharedPreference mySharedPreference = MySharedPreference.getInstance(getBaseContext());
        mySharedPreference.setSharedPreferencesString("platformId", microsoftBandPlatform.getPlatformId());
        mySharedPreference.setSharedPreferencesString("platformName", microsoftBandPlatform.getPlatformName());
        mySharedPreference.setSharedPreferencesString("location", microsoftBandPlatform.getLocation());
        mySharedPreference.setSharedPreferencesBoolean("enabled", microsoftBandPlatform.enabled);
        mySharedPreference.setSharedPreferencesString("version",microsoftBandPlatform.versionHardware);

        for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
            String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
            boolean enabled = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).isEnabled();
            mySharedPreference.setSharedPreferencesBoolean(dataSourceType, enabled);
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                String frequency = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getFrequency();
                mySharedPreference.setSharedPreferencesString(dataSourceType + "_frequency", frequency + " Hz");
            }
        }
    }

    void getSharedPreference() {
        MySharedPreference mySharedPreference = MySharedPreference.getInstance(getBaseContext());
        String platformId = mySharedPreference.getSharedPreferenceString("platformId");
        MicrosoftBandPlatform microsoftBandPlatform = microsoftBandPlatforms.getMicrosoftBandPlatform(platformId);
        microsoftBandPlatform.setLocation(mySharedPreference.getSharedPreferenceString("location"));
        microsoftBandPlatform.enabled = true;

        for (int i = 0; i < microsoftBandPlatform.getMicrosoftBandDataSource().size(); i++) {
            String dataSourceType = microsoftBandPlatform.getMicrosoftBandDataSource().get(i).getDataSourceType();
            microsoftBandPlatform.getMicrosoftBandDataSource().get(i).setEnabled(mySharedPreference.getSharedPreferenceBoolean(dataSourceType));
            if (dataSourceType.equals(DataSourceType.ACCELEROMETER) || dataSourceType.equals(DataSourceType.GYROSCOPE)) {
                String frequencyStr = mySharedPreference.getSharedPreferenceString(dataSourceType + "_frequency");
                String frequency = frequencyStr.substring(0, frequencyStr.length() - 3);
                microsoftBandPlatform.getMicrosoftBandDataSource().get(i).setFrequency(frequency);
            }
        }
    }

    void enablePage() {
        Log.d(TAG,"enable page");
        setupPreferenceScreenBluetooth(true);
        setupPreferenceScreenMicrosoftBand();
        setSaveButton(true);
        setCancelButton();
    }

    void disablePage() {
        if (microsoftBandPlatforms != null) microsoftBandPlatforms.unregister();
        microsoftBandPlatforms=null;
        setupPreferenceScreenBluetooth(false);
        clearPreferenceScreenMicrosoftBand();
        setSaveButton(false);
        setCancelButton();
    }


    @SuppressWarnings("deprecation")
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_DEVICE) {
            if (resultCode == RESULT_OK) {
                getSharedPreference();
                Log.d(TAG,"result add");
                setupPreferenceScreenMicrosoftBand();
            }
        }
    }


    private void updateMicrosoftBand() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMicrosoftBandSettings.this);
        int count = 0;
        for (int i = 0; i < microsoftBandPlatforms.getMicrosoftBandPlatform().size(); i++)
            if (microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).enabled) count++;
        if (count == 0)
            finish();
        else
            builder.setMessage("Configure MicrosoftBand (Change Background & Add Tile)?").setPositiveButton("Yes", dialogChangeBackgroundListener)
                    .setNegativeButton("No", dialogChangeBackgroundListener).show();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Intent intent = new Intent(ActivityMicrosoftBandSettings.this, ServiceMicrosoftBands.class);
                    stopService(intent);
                    saveConfigurationFile();
                    intent = new Intent(ActivityMicrosoftBandSettings.this, ServiceMicrosoftBands.class);
                    startService(intent);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(getBaseContext(), "Configuration file is not saved.", Toast.LENGTH_LONG).show();
                    finish();
                    break;
            }
        }
    };
    DialogInterface.OnClickListener dialogChangeBackgroundListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    for (int i = 0; i < microsoftBandPlatforms.getMicrosoftBandPlatform().size(); i++) {
                        if (!microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).enabled)
                            continue;
                        final String location = microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).getLocation();
                        if (location == null) continue;
                        progressDialog = new ProgressDialog(ActivityMicrosoftBandSettings.this);
                        progressDialog.setMessage("Updating Background theme ... (" + location.toLowerCase().replace("_", " ") + ")");
                        progressDialog.show();
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                microsoftBandPlatforms.getMicrosoftBandPlatform().get(finalI).configureMicrosoftBand(location);
                            }
                        });
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    finish();
                    break;
            }
        }
    };

    void saveConfigurationFile() {
        try {
            microsoftBandPlatforms.writeDataSourceToFile();
            updateMicrosoftBand();

            Toast.makeText(getBaseContext(), "Configuration file is saved.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "!!!Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setSaveButton(boolean bluetoothEnabled) {
        final Button button = (Button) findViewById(R.id.button_settings_save);
        button.setEnabled(bluetoothEnabled);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Apps.isServiceRunning(ActivityMicrosoftBandSettings.this, Constants.SERVICE_NAME)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMicrosoftBandSettings.this);
                    builder.setMessage("Save configuration file and restart the MicrosoftBand Service?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    saveConfigurationFile();
                }
            }
        });
    }

    private void setCancelButton() {
        final Button button = (Button) findViewById(R.id.button_settings_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (microsoftBandPlatforms != null) {
                    microsoftBandPlatforms.unregister();
                    microsoftBandPlatforms = null;
                }
                finish();
            }
        });
    }
}
