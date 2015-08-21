package org.md2k.microsoftband;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microsoftband_settings);
        addPreferencesFromResource(R.xml.pref_microsoftband_general);
        myBlueTooth = new MyBlueTooth(ActivityMicrosoftBandSettings.this, new BlueToothCallBack() {
            @Override
            public void onConnected() {
                enablePage();
            }

            @Override
            public void onDisconnected() {
                disablePage();
            }
        });
    }
    @Override
    protected void onResume() {
        if (myBlueTooth.isEnabled())
            enablePage();
        else disablePage();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        myBlueTooth.close();
        if(microsoftBandPlatforms!=null)
            microsoftBandPlatforms.unregister();
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
            final int finalI = i;
            Preference preference = new Preference(ActivityMicrosoftBandSettings.this);
            preference.setKey(microsoftBandPlatforms.get(finalI).getPlatformId());
            preference.setOnPreferenceClickListener(microsoftBandListener());
            preference.setTitle(microsoftBandPlatforms.get(finalI).getPlatformName() + " (ERROR)");
            preference.setSummary(microsoftBandPlatforms.get(finalI).getPlatformId() + "(" + microsoftBandPlatforms.get(finalI).getLocation() + ")\nERROR: BandOff/PairProblem");
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("microsoftband_configured");
            if (preferenceCategory.findPreference(preference.getKey()) != null) {
                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
            }
            preferenceCategory = (PreferenceCategory) findPreference("microsoftband_available");
            if (preferenceCategory.findPreference(preference.getKey()) != null) {
                preferenceCategory.removePreference(preferenceCategory.findPreference(preference.getKey()));
            }

            if (microsoftBandPlatforms.get(finalI).enabled) {
                ((PreferenceCategory) findPreference("microsoftband_configured")).addPreference(preference);
            } else
                ((PreferenceCategory) findPreference("microsoftband_available")).addPreference(preference);

            microsoftBandPlatforms.get(i).connect(new BandCallBack() {
                @Override
                public void onBandConnected() {
                    Message msg = new Message();
                    msg.what = finalI;
                    handler.sendMessage(msg);
                }
            });
        }
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updatePreference(MicrosoftBandPlatforms.getInstance(ActivityMicrosoftBandSettings.this).getMicrosoftBandPlatform().get(msg.what));
            super.handleMessage(msg);
        }
    };

    void updatePreference(MicrosoftBandPlatform microsoftBandPlatform) {
        Preference preference = findPreference(microsoftBandPlatform.getPlatformId());
        Log.d(TAG, "......................Connect=true name=" + microsoftBandPlatform.getPlatformName());
        preference.setTitle(microsoftBandPlatform.getPlatformName());
        preference.setSummary(microsoftBandPlatform.getPlatformId() + "(" + microsoftBandPlatform.getLocation() + ")");
    }

    private Preference.OnPreferenceClickListener microsoftBandListener() {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isNew = false;
                final String platformId = preference.getKey();
                final Intent intent = new Intent(ActivityMicrosoftBandSettings.this, ActivityMicrosoftBandPlatformSettings.class);
                intent.putExtra("platformId", platformId);
                Log.d(TAG, "platformId=" + platformId);
                isNew = !MicrosoftBandPlatforms.getInstance(ActivityMicrosoftBandSettings.this).find(platformId).enabled;
                if (!isNew) {
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

    void enablePage() {
        microsoftBandPlatforms = MicrosoftBandPlatforms.getInstance(ActivityMicrosoftBandSettings.this);
        setupPreferenceScreenBluetooth(true);
        setupPreferenceScreenMicrosoftBand();
        setSaveButton(true);
        setCancelButton();
    }

    void disablePage() {
        microsoftBandPlatforms = null;
        setupPreferenceScreenBluetooth(false);
        clearPreferenceScreenMicrosoftBand();
        setSaveButton(false);
        setCancelButton();
    }



    @SuppressWarnings("deprecation")
    public void setupPreferenceScreenBluetooth(boolean bluetoothEnabled) {
        Preference preference = findPreference("bluetooth_onoff");
        if (bluetoothEnabled) preference.setSummary("ON");
        else preference.setSummary("OFF");
        preference = findPreference("bluetooth_pair");
        preference.setEnabled(bluetoothEnabled);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.bluetooth.BluetoothSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_DEVICE) {
            if (resultCode == RESULT_OK) {
                setupPreferenceScreenMicrosoftBand();
            }
        }
    }


    private void updateBandBackGround() {
        for (int i = 0; i < microsoftBandPlatforms.getMicrosoftBandPlatform().size(); i++) {
            if (!microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).enabled) continue;
            String location = microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).getLocation();
            if (location == null) continue;
            microsoftBandPlatforms.getMicrosoftBandPlatform().get(i).changeBackGround(location);
        }
    }

    private void setSaveButton(boolean bluetoothEnabled) {
        final Button button = (Button) findViewById(R.id.button_settings_save);
        button.setEnabled(bluetoothEnabled);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    microsoftBandPlatforms.writeDataSourceToFile();
                    updateBandBackGround();

                    Toast.makeText(ActivityMicrosoftBandSettings.this, "Configuration file is saved.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(ActivityMicrosoftBandSettings.this, "!!!Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                finish();
            }
        });
    }
    private void setCancelButton() {
        final Button button = (Button) findViewById(R.id.button_settings_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
