package com.waracle.androidtest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.waracle.androidtest.cacheManagement.ImageCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StaticTools {
    private static final String TAG = StaticTools.class.getSimpleName();

    /*
     * Static variables and methos that che be called from anywhere in the project
     */

    private static JSONArray mCakesData = null;
    /**
     * Set the cakes data
     *
     * @param cakesData cakes data as a JSONArray
     * */
    public static void setCakesData(JSONArray cakesData){
        mCakesData = cakesData;
    }

    public static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * This returns a random image url starting froem a random index
     * */
    public static String getRandomImageUrl(){
        String imageUrl = "";
        /*int min = 0;
        int max = mCakesData.length() - 1;
        int randomIndex = min + (int) (Math.random() * ((max - min) + 1));*/

        int randomIndex = getRandomIndex(mCakesData.length() - 1);
        JSONObject cake = null;
        try {
            cake = (JSONObject) mCakesData.getJSONObject(randomIndex);
            imageUrl = cake.getString("image");
        } catch (JSONException e) {
            Log.e(TAG,  e.getStackTrace().toString());
        }

        return imageUrl;
    }

    /**
     * This returns a random cache image key froem a random index
     * */
    public static String getRandomCacheKey(){
        String key = "";

        /*int min = 0;
        int max = ImageCache.getInstance().getLru().size() - 1;
        int randomIndex = min + (int) (Math.random() * ((max - min) + 1));*/

        int randomIndex = getRandomIndex(ImageCache.getInstance().getLru().size() - 1);

        Object[] keys = ImageCache.getInstance().getLru().snapshot().keySet().toArray();
        key = (String) keys[randomIndex];

        return key;
    }

    /**
     * This calocalte a random integer between 0 and the max value passes through Math.random.
     * An easy way to have random values
     *
     * @param max up rang limit
     * */
    private static int getRandomIndex(int max){
        int min = 0;
        int randomIndex = min + (int) (Math.random() * ((max - min) + 1));

        return randomIndex;
    }
}
