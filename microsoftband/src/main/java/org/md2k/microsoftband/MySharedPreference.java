package org.md2k.microsoftband;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

/**
 * Created by smh on 5/31/2015.
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
