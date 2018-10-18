package com.waracle.androidtest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Map;

public class StaticTolls {
    /*
     * Static variables and methos that che be called from anywhere in the project
     */

    public static Map<String, Bitmap> simpleCache;

    public static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

}
