package me.chon.downloader.util;

import android.util.Log;

import me.chon.downloader.BuildConfig;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class Trace {
    public final static String TAG = "Trace";
    public final static boolean DEBUG = true;

    public static void e(String message) {
        e(TAG,message);
    }

    public static void e(String tag,String message) {
        if (DEBUG) {
            Log.e(tag,message);
        }
    }

}
