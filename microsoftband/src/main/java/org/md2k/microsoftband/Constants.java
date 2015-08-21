package org.md2k.microsoftband;

import android.os.Environment;

import java.io.File;

/**
 * Created by smh on 5/2/2015.
 */
public class Constants {
    public static final String DIRECTORY = Environment.getExternalStorageDirectory() + File.separator + "mCerebrum" + File.separator + "config";
    public static final String FILENAME = "config_microsoftband.json";
    public static final String DIR_FILENAME = DIRECTORY + File.separator + FILENAME;
    public static final String LEFT_WRIST="LEFT_WRIST";
    public static final String RIGHT_WRIST="RIGHT_WRIST";
}
