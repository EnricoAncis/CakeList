package com.waracle.androidtest.threadsManagements;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.waracle.androidtest.StreamUtils;
import com.waracle.androidtest.utils.StaticTolls;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.waracle.androidtest.utils.Constants.GOT_IMAGEURL;
import static com.waracle.androidtest.utils.Constants.IMAGE_DOWNLOADED;
import static com.waracle.androidtest.utils.Constants.IMAGE_POSITION_KEY;
import static com.waracle.androidtest.utils.Constants.IMAGE_URL_KEY;

public class ImageLoaderHandler extends Handler {
    private static final String TAG = ImageLoaderHandler.class.getSimpleName();

    private Handler mUIHandler;

    public ImageLoaderHandler(Looper looper) {
        super(looper);
    }

    public void setUIHandler(Handler uiHandler){
        mUIHandler = uiHandler;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case GOT_IMAGEURL:
                Bundle imageBundle = msg.getData();
                String imageUrl = imageBundle.getString(IMAGE_URL_KEY);
                int imagePos = imageBundle.getInt(IMAGE_POSITION_KEY);
                Log.e(TAG, "--> GOT image url: " + imageUrl);
                loadImage(imageUrl, imagePos);

                break;
        }
    }

    private void loadImage(String imageUrl, int position){
        if(imageUrl != null){
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                // Can you think of a way to make the entire
                // HTTP more efficient using HTTP headers??
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();

                try {
                    // Read data from workstation
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    // Read the error from the workstation
                    Log.e(TAG, e.getMessage());
                    inputStream = connection.getErrorStream();
                }



                byte[] imageBytes = StreamUtils.readUnknownFully(inputStream);

                // Can you think of a way to improve loading of bitmaps
                // that have already been loaded previously??
                /**
                 * StaticTolls.simpleCache is a easy way to have a image cache
                 * */
                Bitmap imageBitmap = StaticTolls.convertToBitmap(imageBytes);
                if( imageBitmap!= null){
                    StaticTolls.simpleCache.put(imageUrl, imageBitmap);
                }

                Message msg = mUIHandler.obtainMessage(IMAGE_DOWNLOADED);
                Bundle bundle = new Bundle();
                bundle.putInt(IMAGE_POSITION_KEY, position);
                bundle.putString(IMAGE_URL_KEY, imageUrl);
                msg.setData(bundle);
                mUIHandler.sendMessage(msg);

                Log.e(TAG, "--> SEND BACK image");
            }
            catch ( Exception e){
                Log.e(TAG, e.getMessage());
            }
            finally {
                // Close the input stream if it exists.
                StreamUtils.close(inputStream);

                // Disconnect the connection
                connection.disconnect();
            }
        }
    }

}

