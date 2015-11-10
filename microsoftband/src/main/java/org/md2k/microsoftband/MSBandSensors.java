package org.md2k.microsoftband;

import org.md2k.datakitapi.source.datasource.DataSourceType;

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
public class MSBandSensors {
    static class MSBandSensor {
        int version;
        String dataSourceType;
        String description;
        String unit;
        String frequency;

        public MSBandSensor(int version, String dataSourceType, String description, String unit, String frequency) {
            this.version = version;
            this.dataSourceType = dataSourceType;
            this.description = description;
            this.unit = unit;
            this.frequency = frequency;
        }
    };
    public static ArrayList<MSBandSensor> getSensors(){
        ArrayList<MSBandSensor> msBandSensors=new ArrayList<>();
        msBandSensors.add(new MSBandSensor(1,DataSourceType.ACCELEROMETER,"represent the band's axes acceleration in units of standard gravity", "meter/second^2",""));
        msBandSensors.add(new MSBandSensor(2,DataSourceType.ALTIMETER,"0: FlightsAscended (Number of floors ascended since the Band was last factory-reset), "+
                "1: FlightsDescended (Number of floors ascended since the Band was last factory-reset), " +
                "2: Rate (The current rate of ascend/descend in cm/s), " +
                "3: SteppingGain (Total elevation gained in centimeters by taking steps since the Band was last factory-reset), " +
                "4: SteppingLoss (Total elevation lost in centimeters by taking steps since the Band was last factory-reset), " +
                "5: StepsAscended (Total number of steps ascended since the Band was last factory-reset), " +
                "6: StepsDescended (Total number of steps descended since the Band was last factory-reset), " +
                "7: TotalGain (Total elevation gained in centimeters since the Band was last factory-reset), " +
                "8: TotalLoss (Total elevation loss in centimeters since the Band was last factory-reset", "","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.AMBIENT_LIGHT,"Current ambient light in lumens per square meter (lux)", "lux","2"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.GYROSCOPE,"represent the angular velocity around the band's axes in degrees/second", "degress/second",""));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.BAND_CONTACT,"Current contact state of the band.. Contact state: -1: Unknown, 0: worn, 1: not_worn", "-1:unknown,0:worn,1:not_worn","0"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.CALORY_BURN,"Get the total number of kilocalories burned since the band was last factory rese", "kilocalories","0"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.STEP_COUNT,"The total number of steps taken since the band was last factory reset", "steps","0"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.SKIN_TEMPERATURE,"Current temperature in degrees Celsius of the person wearing the Band", "degree celsius","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.ULTRA_VIOLET_RADIATION,"the UV index level as calculated by the band. 0:NONE, 1: LOW, 2: MEDIUM, 3: HIGH, 4: VERY_HIGH", "0:NONE, 1: LOW, 2: MEDIUM, 3: HIGH, 4: VERY_HIGH","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.DISTANCE,"the total distance traveled since the band was last factory reset in cm", "centimeter","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.SPEED,"a float value representing the current speed the band is moving at in cm/s", "centimeter/second","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.PACE,"a float value representing the current pace of the band in ms/m", "millisecond/meter","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.MOTION_TYPE,"the current MotionType of the band. IDLE,JOGGING,RUNNING,UNKNOWN,WALKING", "MotionType: IDLE, JOGGING, RUNNING, UNKNOWN, WALKING","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.HEART_RATE,"the current heart rate as read by the Band in beats per minute. Confidence=0.0 (acquiring), 1.0 (locked)", "beats/minute","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.RR_INTERVAL,"Current RR interval in seconds as read by the Band", "second","0"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.GSR,"Current skin resistance in kohms of the person wearing the Band", "kohms","0.2"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.AIR_PRESSURE,"Current air pressure in hectopascals", "hectopascals","1"));
        msBandSensors.add(new MSBandSensor(1,DataSourceType.AMBIENT_TEMPERATURE,"Current temperature in degrees Celsius", "degree celcius","1"));
        return msBandSensors;

    }
}

