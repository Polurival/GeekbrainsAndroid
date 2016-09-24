package com.github.polurival.widgetapp;

import android.util.Log;

/**
 * Created by Polurival
 * on 24.09.2016.
 */
public class AppLog {
    private static final String APP_TAG = "GPSWidget";

    public static int logString(String message) {
        return Log.i(APP_TAG, message);
    }
}
