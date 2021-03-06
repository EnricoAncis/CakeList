package com.waracle.androidtest.threadsManagements;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.waracle.androidtest.utils.StreamUtils;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This is the AsyncTaskLoader explained in the MainActivity
 * */
public class JosonLoader extends AsyncTaskLoader<JSONArray> {
    private  final String TAG = JosonLoader.class.getSimpleName();


    JSONArray mCakesData = null;
    String mLsonUrl;

    /**
     * This is the costructor with the referment to its super creator to which is passed the Context.
     * Here is inizialied mLsonUrl with the url to download the JsonArray
     *
     * @param context Activity contex
     * @param jsonUrl url form which download the JsonArray
     * */
    public JosonLoader(@NonNull Context context, String jsonUrl) {
        super(context);
        mLsonUrl = jsonUrl;
    }

    /**
     * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
     */
    @Override
    protected void onStartLoading() {
        if (mCakesData != null) {
            deliverResult(mCakesData);
        } else {
            forceLoad();
        }
    }

    /**
     * This is the method of the AsyncTaskLoader that will load and parse the JSON data
     * from gist.githubusercontent in the background.
     *
     * @return Cakes data from gist.githubusercontent as an json array.
     *         null if an error occurs
     */
    @Override
    public JSONArray loadInBackground() {

        JSONArray jsonResult = null;
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(mLsonUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???

            /*
             * It can be used the ETags from the HTTP headers to check if the image has been updated
             * or expired.
             * In case of the update image it can be download, in case of image no update or expired
             * It can be used the copy of it from the cache
             *
             * */

            // Also, Do you trust any utils thrown your way????

            /*
             * A tool to help in this work can be Retrofit and OkHttp
             * */

            byte[] bytes = StreamUtils.readUnknownFully(in);

            // Read in charset of HTTP content.
            String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, charset);

            // Read string as JSON.
            jsonResult = new JSONArray(jsonText);

        }
        catch (Exception e){
            Log.e(TAG,  e.getMessage());
        }
        finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            return jsonResult;
        }

    }

    /**
     * Sends the result of the load to the registered listener.
     *
     * @param data The result of the load
     */
    public void deliverResult(JSONArray data) {
        mCakesData = data;
        super.deliverResult(data);
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public  String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }
}


