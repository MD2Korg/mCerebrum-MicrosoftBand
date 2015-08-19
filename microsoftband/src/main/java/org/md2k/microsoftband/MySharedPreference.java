package org.md2k.microsoftband;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
public class MySharedPreference {
    public static SharedPreferences sharedPreferences=null;
    private static MySharedPreference instance=null;
    public static MySharedPreference getInstance(Context context){
        if(instance==null) instance=new MySharedPreference(context);
        return instance;
    }
    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    public void clear(String key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    private MySharedPreference(Context context){
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        clear();
    }

    public void setListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener){
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
    public boolean getSharedPreferenceBoolean(String key){
        return sharedPreferences.getBoolean(key,false);
    }
    public String getSharedPreferenceString(String key){
        return sharedPreferences.getString(key, "");
    }
    public int getSharedPreferenceInt(String key){
        return sharedPreferences.getInt(key, -1);
    }
    public float getSharedPreferenceDouble(String key){
        return sharedPreferences.getFloat(key, 0.0f);
    }

    public void setSharedPreferencesString(String key, String text) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, text);
        editor.commit();
    }

    public void setSharedPreferencesBoolean(String key, boolean result) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, result);
        editor.commit();
    }

    public void setSharedPreferencesInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void setSharedPreferencesDouble(String key, double value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, (float) value);
        editor.commit();
    }

}
