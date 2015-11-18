package org.md2k.microsoftband;

import android.content.Context;
import android.content.Intent;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.microsoft.band.sensors.MotionType;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.UVIndexLevel;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDouble;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeFloat;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeIntArray;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.datakit.DataKitHandler;


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
public class MicrosoftBandDataSource {
    private static final String TAG = MicrosoftBandDataSource.class.getSimpleName();
    Context context;
//    private String dataSourceType;
//    private double frequency;
    private boolean enabled;
    CallBack callBack;
    DataKitHandler dataKitHandler;
    DataSourceClient dataSourceClient;
    MSBandSensors.MSBandSensor msBandSensor;

    public MicrosoftBandDataSource(Context context, MSBandSensors.MSBandSensor msBandSensor, boolean enabled) {
        this.context = context;
        this.msBandSensor=msBandSensor;
        if (msBandSensor.dataSourceType.equals(DataSourceType.ACCELEROMETER) || msBandSensor.dataSourceType.equals(DataSourceType.GYROSCOPE))
            msBandSensor.frequency = "31";
        this.enabled = enabled;
    }

    public String getDataSourceType() {
        return msBandSensor.dataSourceType;
    }

    public void show() {
        Log.d(TAG, "datasourcetype=" + msBandSensor.dataSourceType + " frequency=" + msBandSensor.frequency + " enabled=" + enabled);
    }

    public String getFrequency() {
        return msBandSensor.frequency;
    }

    public void setFrequency(String frequency) {
        msBandSensor.frequency = frequency;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void set(DataSource dataSource) {
        msBandSensor.dataSourceType = dataSource.getType();
        msBandSensor.frequency = dataSource.getMetadata().get("frequency");
        enabled = true;
    }

    public DataSourceBuilder getDataSourceBuilder() {
        if (!enabled) return null;
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setId(null).setType(msBandSensor.dataSourceType);
        dataSourceBuilder = dataSourceBuilder.setDescription(msBandSensor.description);
        dataSourceBuilder = dataSourceBuilder.setMetadata("unit", msBandSensor.unit);
        dataSourceBuilder = dataSourceBuilder.setMetadata("frequency", msBandSensor.frequency);
        return dataSourceBuilder;
    }

    private void sendMessage(DataType data) {
        Log.d(TAG,dataSourceClient.getDataSource().getPlatform().getId()+" "+dataSourceClient.getDataSource().getType());
        dataKitHandler.insert(dataSourceClient, data);
        callBack.onReceivedData(data);
    }

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            double[] samples = new double[3];
            samples[0] = event.getAccelerationX() * 9.81;
            samples[1] = event.getAccelerationY() * 9.81;
            samples[2] = event.getAccelerationZ() * 9.81;
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendMessage(dataTypeDoubleArray);
        }
    };
    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            double[] samples = new double[3];
            samples[0] = event.getAngularVelocityX();
            samples[1] = event.getAngularVelocityY();
            samples[2] = event.getAngularVelocityZ();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendMessage(dataTypeDoubleArray);
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
            int samples[]=new int[2];
            samples[0]=event.getHeartRate();
            if(event.getQuality()== HeartRateQuality.ACQUIRING)
                samples[1]=1;
            else if(event.getQuality()==HeartRateQuality.LOCKED)
                samples[1]=0;
            DataTypeIntArray dataTypeIntArray = new DataTypeIntArray(DateTime.getDateTime(), samples);
            sendMessage(dataTypeIntArray);
        }
    };
    private BandRRIntervalEventListener mRRIntervalEventListener=new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(BandRRIntervalEvent bandRRIntervalEvent) {
            DataTypeDouble dataTypeDouble = new DataTypeDouble(DateTime.getDateTime(), bandRRIntervalEvent.getInterval());
            sendMessage(dataTypeDouble);
        }
    };

    private BandContactEventListener mBandContactEventListener = new BandContactEventListener() {
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
    private BandGsrEventListener mGSREventListener=new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(BandGsrEvent bandGsrEvent) {
            DataTypeInt dataTypeInt=new DataTypeInt(DateTime.getDateTime(),bandGsrEvent.getResistance());
            sendMessage(dataTypeInt);
        }
    };
    private BandBarometerEventListener mAirPressureEventListener=new BandBarometerEventListener() {
        @Override
        public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
            DataTypeDouble dataTypeDouble=new DataTypeDouble(DateTime.getDateTime(),bandBarometerEvent.getAirPressure());
            sendMessage(dataTypeDouble);
        }
    };
    private BandBarometerEventListener mAmbientTemperatureEventListener=new BandBarometerEventListener() {
        @Override
        public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
            DataTypeDouble dataTypeDouble=new DataTypeDouble(DateTime.getDateTime(),bandBarometerEvent.getTemperature());
            sendMessage(dataTypeDouble);
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

    private BandAltimeterEventListener mAltimeterListener = new BandAltimeterEventListener() {
        @Override
        public void onBandAltimeterChanged(BandAltimeterEvent bandAltimeterEvent) {
            double samples[] = new double[9];
            samples[0] = bandAltimeterEvent.getFlightsAscended();
            samples[1]=bandAltimeterEvent.getFlightsDescended();
            samples[2]=bandAltimeterEvent.getRate();
            samples[3]=bandAltimeterEvent.getSteppingGain();
            samples[4]=bandAltimeterEvent.getSteppingLoss();
            samples[5]=bandAltimeterEvent.getStepsAscended();
            samples[6]=bandAltimeterEvent.getStepsDescended();
            samples[7]=bandAltimeterEvent.getTotalGain();
            samples[8]=bandAltimeterEvent.getTotalLoss();
            DataTypeDoubleArray dataTypeDoubleArray = new DataTypeDoubleArray(DateTime.getDateTime(), samples);
            sendMessage(dataTypeDoubleArray);
        }
    };
    private BandAmbientLightEventListener mAmbientLightEventListener=new BandAmbientLightEventListener() {
        @Override
        public void onBandAmbientLightChanged(BandAmbientLightEvent bandAmbientLightEvent) {
            DataTypeInt dataTypeInt=new DataTypeInt(DateTime.getDateTime(),bandAmbientLightEvent.getBrightness());
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

    public boolean register(final Platform platform, final BandClient bandClient, final CallBack newcallBack) {

        dataKitHandler = DataKitHandler.getInstance(context);
        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
        dataSourceBuilder = dataSourceBuilder.setPlatform(platform);
        dataSourceClient = dataKitHandler.register(dataSourceBuilder);
        Log.d(TAG, dataSourceClient.getDataSource().getPlatform().getId() + " " + dataSourceClient.getDataSource().getType() + " " + dataSourceClient.getDs_id());

        callBack = newcallBack;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
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
        Log.d(TAG,"registerSensor: bandClient="+bandClient+" msBandSensor="+ msBandSensor.dataSourceType+" frequency="+msBandSensor.frequency);
        if (bandClient == null) return;
        switch (msBandSensor.dataSourceType) {
            case DataSourceType.ACCELEROMETER:
                switch (msBandSensor.frequency) {
                    case "8":
                        bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                        break;
                    case "31":
                        bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS32);
                        break;
                    case "62":
                        bandClient.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                        break;
                }
                break;
            case DataSourceType.GYROSCOPE:
                switch (msBandSensor.frequency) {
                    case "8":
                        bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS128);
                        break;
                    case "31":
                        bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS32);
                        break;
                    case "62":
                        bandClient.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS16);
                        break;
                }
                break;

            case DataSourceType.STEP_COUNT:
                bandClient.getSensorManager().registerPedometerEventListener(mPedometerEventListener);
                break;
            case DataSourceType.DISTANCE:
                bandClient.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
                break;
            case DataSourceType.MOTION_TYPE:
                bandClient.getSensorManager().registerDistanceEventListener(mMotionTypeEventListener);
                break;
            case DataSourceType.SPEED:
                bandClient.getSensorManager().registerDistanceEventListener(mSpeedEventListener);
                break;
            case DataSourceType.PACE:
                bandClient.getSensorManager().registerDistanceEventListener(mPaceEventListener);
                break;
            case DataSourceType.BAND_CONTACT:
                bandClient.getSensorManager().registerContactEventListener(mBandContactEventListener);
                break;
            case DataSourceType.CALORY_BURN:
                bandClient.getSensorManager().registerCaloriesEventListener(mCaloriesEventListener);
                break;
            case DataSourceType.HEART_RATE:
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                    Intent intent = new Intent(context, HRConsentActivity.class);
                    HRConsentActivity.bandClient = bandClient;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                    bandClient.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                }
                break;
            case DataSourceType.ULTRA_VIOLET_RADIATION:
                bandClient.getSensorManager().registerUVEventListener(mUltravioletEventListener);
                break;
            case DataSourceType.SKIN_TEMPERATURE:
                bandClient.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                break;
            case DataSourceType.GALVANIC_SKIN_RESPONSE:
                bandClient.getSensorManager().registerGsrEventListener(mGSREventListener);
                break;
            case DataSourceType.AMBIENT_TEMPERATURE:
                bandClient.getSensorManager().registerBarometerEventListener(mAmbientTemperatureEventListener);
                break;
            case DataSourceType.AIR_PRESSURE:
                bandClient.getSensorManager().registerBarometerEventListener(mAirPressureEventListener);
                break;
            case DataSourceType.AMBIENT_LIGHT:
                bandClient.getSensorManager().registerAmbientLightEventListener(mAmbientLightEventListener);
                break;
            case DataSourceType.RR_INTERVAL:
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                    Intent intent = new Intent(context, HRConsentActivity.class);
                    HRConsentActivity.bandClient = bandClient;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                    bandClient.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                }
                break;
            case DataSourceType.ALTIMETER:
                bandClient.getSensorManager().registerAltimeterEventListener(mAltimeterListener);
                break;

        }

    }

    private void unregisterSensor(BandClient bandClient) {
        try {
            switch (msBandSensor.dataSourceType) {
                case DataSourceType.ACCELEROMETER:
                    bandClient.getSensorManager().unregisterAccelerometerEventListeners();
                    break;
                case DataSourceType.GYROSCOPE:
                    bandClient.getSensorManager().unregisterGyroscopeEventListeners();
                    break;

                case DataSourceType.STEP_COUNT:
                    bandClient.getSensorManager().unregisterPedometerEventListeners();
                    break;
                case DataSourceType.DISTANCE:
                    bandClient.getSensorManager().unregisterDistanceEventListener(mDistanceEventListener);
                    break;
                case DataSourceType.MOTION_TYPE:
                    bandClient.getSensorManager().unregisterDistanceEventListener(mMotionTypeEventListener);
                    break;
                case DataSourceType.SPEED:
                    bandClient.getSensorManager().unregisterDistanceEventListener(mSpeedEventListener);
                    break;
                case DataSourceType.PACE:
                    bandClient.getSensorManager().unregisterDistanceEventListener(mPaceEventListener);
                    break;
                case DataSourceType.BAND_CONTACT:
                    bandClient.getSensorManager().unregisterContactEventListeners();
                    break;
                case DataSourceType.CALORY_BURN:
                    bandClient.getSensorManager().unregisterCaloriesEventListeners();
                    break;
                case DataSourceType.HEART_RATE:
                    bandClient.getSensorManager().unregisterHeartRateEventListeners();
                    break;
                case DataSourceType.ULTRA_VIOLET_RADIATION:
                    bandClient.getSensorManager().unregisterUVEventListeners();
                    break;
                case DataSourceType.SKIN_TEMPERATURE:
                    bandClient.getSensorManager().unregisterSkinTemperatureEventListeners();
                    break;
                case DataSourceType.GALVANIC_SKIN_RESPONSE:
                    bandClient.getSensorManager().unregisterGsrEventListeners();
                    break;
                case DataSourceType.AMBIENT_TEMPERATURE:
                    bandClient.getSensorManager().unregisterBarometerEventListener(mAmbientTemperatureEventListener);
                    break;
                case DataSourceType.AIR_PRESSURE:
                    bandClient.getSensorManager().unregisterBarometerEventListener(mAirPressureEventListener);
                    break;
                case DataSourceType.AMBIENT_LIGHT:
                    bandClient.getSensorManager().unregisterAmbientLightEventListener(mAmbientLightEventListener);
                    break;
                case DataSourceType.RR_INTERVAL:
                    bandClient.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
                    break;
                case DataSourceType.ALTIMETER:
                    bandClient.getSensorManager().unregisterAltimeterEventListener(mAltimeterListener);
                    break;
            }
        } catch (BandIOException e) {
        }
    }

    public void unregister(final BandClient bandClient) {
        Log.d(TAG, "Unregister: " + msBandSensor.dataSourceType);
        if (!enabled) return;
        if (bandClient == null) return;
        final Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                unregisterSensor(bandClient);
            }
        });
        background.start();
    }
}
