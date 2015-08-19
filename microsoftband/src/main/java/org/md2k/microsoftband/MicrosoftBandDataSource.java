package org.md2k.microsoftband;

import android.content.Context;
import android.content.Intent;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.MotionType;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.UVIndexLevel;

import org.md2k.datakitapi.DataKitApi;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeFloatArray;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
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
public class MicrosoftBandDataSource {
    private static final String TAG = MicrosoftBandDataSource.class.getSimpleName();
    Context context;
    private String dataSourceType;
    private double frequency;
    private boolean enabled;
    CallBack callBack;
    DataSourceClient dataSourceClient;
    DataKitApi mDataKitApi;

    public MicrosoftBandDataSource(Context context, String dataSourceType, boolean enabled) {
        this.context = context;
        this.dataSourceType = dataSourceType;
        if(dataSourceType.equals(DataSourceType.ACCELEROMETER)|| dataSourceType.equals(DataSourceType.GYROSCOPE))
            frequency=31.25;
        this.enabled = enabled;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public String toString() {
        return "datasourcetype=" + dataSourceType + " frequency=" + frequency + " enabled=" + enabled;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void set(DataSource dataSource) {
        dataSourceType = dataSource.getType();
        frequency = Double.parseDouble(dataSource.getMetadata().get("frequency"));
        enabled = true;
    }

    public DataSourceBuilder createDataSourceBuilder() {
        if (enabled == false) return null;
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setId(null).setType(dataSourceType);
        if (dataSourceType.equals(DataSourceType.CALORY_BURN)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("Get the total number of kilocalories burned since the band was last factory reset");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "kilocalories");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.ACCELEROMETER)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("Float values represent the band's axes acceleration in units of standard gravity");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "g units");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", String.valueOf(frequency));

        } else if (dataSourceType.equals(DataSourceType.GYROSCOPE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("Float values represent the angular velocity around the band's axes in degrees/second");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "degrees/second");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", String.valueOf(frequency));
        } else if (dataSourceType.equals(DataSourceType.BAND_CONTACT)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("Current contact state of the band.. Contact state: -1: Unknown, 0: Not_Worn, 1: Worn");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "-1:unknown,0:not_worn,1:worn");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.STEP_COUNT)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("The total number of steps taken since the band was last factory reset");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "steps");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.SKIN_TEMPERATURE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("Float value representing the skin temperature of the person wearing the Band in degrees Celsius");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "degree celsius");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.ULTRA_VIOLET_RADIATION)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("the UV index level as calculated by the band. 0:NONE, 1: LOW, 2: MEDIUM, 3: HIGH, 4: VERY_HIGH");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "0:NONE, 1: LOW, 2: MEDIUM, 3: HIGH, 4: VERY_HIGH");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.DISTANCE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("he total distance traveled since the band was last factory reset in cm");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "centimeter");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.SPEED)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("a float value representing the current speed the band is moving at in cm/s");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "centimeter/second");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.PACE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("a float value representing the current pace of the band in ms/m");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "millisecond/meter");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.MOTION_TYPE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("the current MotionType of the band. IDLE,JOGGING,RUNNING,UNKNOWN,WALKING");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "MotionType: IDLE, JOGGING, RUNNING, UNKNOWN, WALKING");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        } else if (dataSourceType.equals(DataSourceType.HEART_RATE)) {
            dataSourceBuilder = dataSourceBuilder.setDescription("the current heart rate as read by the Band in beats per minute. Confidence=0.0 (acquiring), 1.0 (locked)");
            dataSourceBuilder = dataSourceBuilder.setMetadata("unit", "beats/minute");
            dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", "0");
        }
        return dataSourceBuilder;
    }

    private void sendMessage(DataType data) {
        mDataKitApi.insert(dataSourceClient, data);
        callBack.onReceivedData(data);
    }

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            float[] samples=new float[3];
            samples[0]=event.getAccelerationX();
            samples[1]=event.getAccelerationY();
            samples[2]=event.getAccelerationZ();
            DataTypeFloatArray dataTypeFloatArray=new DataTypeFloatArray(DateTime.getDateTime(),samples);
            sendMessage(dataTypeFloatArray);
        }
    };
    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            float[] samples=new float[3];
            samples[0]=event.getAngularVelocityX();
            samples[1]=event.getAngularVelocityY();
            samples[2]=event.getAngularVelocityZ();
            DataTypeFloatArray dataTypeFloatArray=new DataTypeFloatArray(DateTime.getDateTime(),samples);
            sendMessage(dataTypeFloatArray);
        }
    };
    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent event) {
            DataTypeLong dataTypeLong = new DataTypeLong(DateTime.getDateTime(), event.getTotalDistance());
            sendMessage(dataTypeLong);
        }
    };
    private BandDistanceEventListener mSpeedEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent event) {
            DataTypeFloat dataTypeFloat = new DataTypeFloat(DateTime.getDateTime(), event.getSpeed());
            sendMessage(dataTypeFloat);
        }
    };
    private BandDistanceEventListener mMotionTypeEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent event) {
            String motionType = "";
            if (event.getMotionType().equals(MotionType.IDLE)) motionType = "IDLE";
            else if (event.getMotionType().equals(MotionType.UNKNOWN)) motionType = "UNKNOWN";
            else if (event.getMotionType().equals(MotionType.JOGGING)) motionType = "JOGGING";
            else if (event.getMotionType().equals(MotionType.WALKING)) motionType = "WALKING";
            else if (event.getMotionType().equals(MotionType.RUNNING)) motionType = "RUNNING";
            DataTypeString dataTypeString = new DataTypeString(DateTime.getDateTime(), motionType);
            sendMessage(dataTypeString);
        }
    };
    private BandDistanceEventListener mPaceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent event) {
            DataTypeFloat dataTypeFloat = new DataTypeFloat(DateTime.getDateTime(), event.getPace());
            sendMessage(dataTypeFloat);
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            DataTypeInt dataTypeInt = new DataTypeInt(DateTime.getDateTime(), event.getHeartRate());
            //TODO: Heart rate quality
/*            if (event.getQuality().equals(HeartRateQuality.ACQUIRING))
                dataTypeInt.setConfidence(0.0F);
            else
                dataTypeInt.setConfidence(1.0F);

*/            sendMessage(dataTypeInt);

        }
    };

    private BandContactEventListener mContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent event) {
            int state = -1;
            if (event.getContactState() == BandContactState.UNKNOWN)
                state = -1;
            else if (event.getContactState() == BandContactState.NOT_WORN)
                state = 0;
            else if (event.getContactState() == BandContactState.WORN)
                state = 1;
            DataTypeInt dataTypeInt = new DataTypeInt(DateTime.getDateTime(), state);
            sendMessage(dataTypeInt);
        }
    };

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent event) {
            DataTypeFloat dataTypeFloat = new DataTypeFloat(DateTime.getDateTime(), event.getTemperature());
            sendMessage(dataTypeFloat);
        }
    };

    private BandUVEventListener mUltravioletEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(final BandUVEvent event) {
            int uv = 0;
            if (event.getUVIndexLevel() == UVIndexLevel.NONE) uv = 0;
            else if (event.getUVIndexLevel() == UVIndexLevel.LOW) uv = 1;
            else if (event.getUVIndexLevel() == UVIndexLevel.MEDIUM) uv = 2;
            else if (event.getUVIndexLevel() == UVIndexLevel.HIGH) uv = 3;
            else if (event.getUVIndexLevel() == UVIndexLevel.VERY_HIGH) uv = 4;
            DataTypeInt dataTypeInt = new DataTypeInt(DateTime.getDateTime(), uv);
            sendMessage(dataTypeInt);
        }
    };

    private BandPedometerEventListener mPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(final BandPedometerEvent event) {
            DataTypeLong dataTypeLong = new DataTypeLong(DateTime.getDateTime(), event.getTotalSteps());
            sendMessage(dataTypeLong);
        }
    };
    private BandCaloriesEventListener mCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(final BandCaloriesEvent event) {
            DataTypeLong dataTypeLong = new DataTypeLong(DateTime.getDateTime(), event.getCalories());
            sendMessage(dataTypeLong);
        }
    };

    public boolean register(DataKitApi dataKitApi, final Platform platform, final BandClient bandClient, final CallBack newcallBack) {
        mDataKitApi = dataKitApi;
        DataSourceBuilder dataSourceBuilder = createDataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        DataSource dataSource = dataSourceBuilder.build();

        dataSourceClient = dataKitApi.register(dataSource).await();

        callBack = newcallBack;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Connect: band=" + bandClient.toString() + " datasourcetype=" + dataSourceType);
                try {
                    registerSensor(bandClient);
                } catch (BandException e) {
                    e.printStackTrace();
                }
            }

        });
        background.start();

        return true;
    }

    private void registerSensor(BandClient bandClient) throws BandException {
        Log.d(TAG, "BandClient=" + bandClient);
        if (bandClient == null) return;
        int freq = 0;
        if (frequency != 0) {
            freq = (int) (1000.0 / frequency + 0.01);
            Log.d(TAG, "frequency=" + freq + " millis=" + freq);
        }
        if (DataSourceType.ACCELEROMETER.equals(dataSourceType)) {
            switch (freq) {
                case 16:
                    bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                    break;
                case 32:
                    bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS32);
                    break;
                case 128:
                    bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                    break;
            }
        } else if (DataSourceType.GYROSCOPE.equals(dataSourceType)) {
            switch (freq) {
                case 16:
                    bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS16);
                    break;
                case 32:
                    bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS32);
                    break;
                case 128:
                    bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS128);
                    break;
            }
        } else if (DataSourceType.STEP_COUNT.equals(dataSourceType)) {
            bandClient.getSensorManager().registerPedometerEventListener(mPedometerEventListener);
        } else if (DataSourceType.DISTANCE.equals(dataSourceType)) {
            bandClient.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
        } else if (DataSourceType.MOTION_TYPE.equals(dataSourceType)) {
            bandClient.getSensorManager().registerDistanceEventListener(mMotionTypeEventListener);
        } else if (DataSourceType.SPEED.equals(dataSourceType)) {
            bandClient.getSensorManager().registerDistanceEventListener(mSpeedEventListener);
        } else if (DataSourceType.PACE.equals(dataSourceType)) {
            bandClient.getSensorManager().registerDistanceEventListener(mPaceEventListener);
        } else if (DataSourceType.BAND_CONTACT.equals(dataSourceType))
            bandClient.getSensorManager().registerContactEventListener(mContactEventListener);
        else if (DataSourceType.CALORY_BURN.equals(dataSourceType))
            bandClient.getSensorManager().registerCaloriesEventListener(mCaloriesEventListener);
        else if (DataSourceType.HEART_RATE.equals(dataSourceType)) {
            // check current user heart rate consent
            Log.d(TAG, "userconsent=" + bandClient.getSensorManager().getCurrentHeartRateConsent().name());
            if (bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                // user has not consented, request it
                // the calling class is both an Activity and implements
                // HeartRateConsentListener
                Log.d(TAG, "userconsent=" + bandClient.getSensorManager().getCurrentHeartRateConsent().name() + " inside...");

                Intent intent = new Intent(context, HRConsentActivity.class);
                HRConsentActivity.bandClient = bandClient;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                ((Activity)context).startActivity(intent);
                context.startActivity(intent);

            }
            if (bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                Log.d(TAG, "userconsent=" + bandClient.getSensorManager().getCurrentHeartRateConsent().name() + " here...");

                bandClient.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                Log.d(TAG, "HR consent-> success");
            }
        } else if (DataSourceType.ULTRA_VIOLET_RADIATION.equals(dataSourceType))
            bandClient.getSensorManager().registerUVEventListener(mUltravioletEventListener);
        else if (DataSourceType.SKIN_TEMPERATURE.equals(dataSourceType))
            bandClient.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
    }

    private void unregisterSensor(BandClient bandClient) {
        try {
            if (DataSourceType.ACCELEROMETER.equals(dataSourceType))
                bandClient.getSensorManager().unregisterAccelerometerEventListeners();
            else if (DataSourceType.GYROSCOPE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterGyroscopeEventListeners();
            else if (DataSourceType.HEART_RATE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterHeartRateEventListeners();
            else if (DataSourceType.CALORY_BURN.equals(dataSourceType))
                bandClient.getSensorManager().unregisterCaloriesEventListeners();
            else if (DataSourceType.SKIN_TEMPERATURE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterSkinTemperatureEventListeners();
            else if (DataSourceType.ULTRA_VIOLET_RADIATION.equals(dataSourceType))
                bandClient.getSensorManager().unregisterUVEventListeners();
            else if (DataSourceType.BAND_CONTACT.equals(dataSourceType))
                bandClient.getSensorManager().unregisterContactEventListeners();
            else if (DataSourceType.DISTANCE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterDistanceEventListener(mDistanceEventListener);
            else if (DataSourceType.SPEED.equals(dataSourceType))
                bandClient.getSensorManager().unregisterDistanceEventListener(mSpeedEventListener);
            else if (DataSourceType.MOTION_TYPE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterDistanceEventListener(mMotionTypeEventListener);
            else if (DataSourceType.PACE.equals(dataSourceType))
                bandClient.getSensorManager().unregisterDistanceEventListener(mPaceEventListener);
            else if (DataSourceType.STEP_COUNT.equals(dataSourceType))
                bandClient.getSensorManager().unregisterPedometerEventListeners();

        } catch (BandIOException e) {
        }
    }

    public void unregister(final BandClient bandClient) {
        Log.d(TAG, "Unregister: " + dataSourceType);
        if(!enabled) return;
        if(bandClient==null) return;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                unregisterSensor(bandClient);
            }
        });
        background.start();
    }
}
