package com.waracle.androidtest.threadsManagements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.waracle.androidtest.utils.StaticTools;
import com.waracle.androidtest.utils.StreamUtils;
import com.waracle.androidtest.cacheManagement.ImageCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.waracle.androidtest.utils.Constants.GOT_IMAGEURL;
import static com.waracle.androidtest.utils.Constants.IMAGE_DOWNLOADED;
import static com.waracle.androidtest.utils.Constants.IMAGE_POSITION_KEY;
import static com.waracle.androidtest.utils.Constants.IMAGE_URL_KEY;

/**
 * ImageLoaderHandler receive of the Handlerthread looper to run on a separate thread from the Mainthread
 * and manages the Messageed queue to download in async way the images
 * */
public class ImageLoaderHandler extends Handler {
    private static final String TAG = ImageLoaderHandler.class.getSimpleName();

    private Handler mUIHandler;

    /**
     * The costructor refers to its super creator and passes the looper
     *
     * @param looper Handlerthread looper
     */
    public ImageLoaderHandler(Looper looper) {
        super(looper);
    }

    /**
     * This sets UIHandler to post Messages to its
     *
     * @param uiHandler UIHandler
     * */
    public void setUIHandler(Handler uiHandler){
        mUIHandler = uiHandler;
    }

    /**
     * This Overridden method is needed to handle message requests from UIHandler with its params
     * */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case GOT_IMAGEURL:  //Messagge waited

                //Retrieve data from the message request
                Bundle imageBundle = msg.getData();

                //retrieve item position
                int imagePos = imageBundle.getInt(IMAGE_POSITION_KEY);

                //retrieve image url
                String imageUrl = imageBundle.getString(IMAGE_URL_KEY);

                Log.e(TAG, "--> GOT image url: " + imageUrl);

                //Call method to do the http request for the image
                loadImage(imageUrl, imagePos);

                break;
        }
    }

    /**
     * This method does the http request for the images, it saves them in StaticTools.simpleCache and
     * send back the Message to the UIHandler
     *
     * @param imageUrl image url to download
     * @param position Recyclerview position
     * */
    private void loadImage(String imageUrl, int position){
        if(imageUrl != null){
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                // Can you think of a way to make the entire
                // HTTP more efficient using HTTP headers??

                /*
                 * It can be used the ETags from the  HTTP headers to check if the image has been updated
                 * or expired.
                 * In case of the update image, it can be download it, in case of image no update or expired,
                 * it can be used the copy of it from the cache
                 *
                 * A tool to help in this work can be Retrofit and OkHttp
                 * */
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();

                try {
                    // Read data from workstation
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    // Read the error from the workstation
                    Log.e(TAG, e.getMessage());
                    inputStream = connection.getErrorStream();
                }

                /*
                 * StreamUtils.readUnknownFully is useless because it retrieves the Byte array to decode
                 * the image, but in the ImageLoaderHandler
                 * It can be use directly BitmapFactory.decodeStream to obtain the image bitmap from the inputStream.
                */
               /* byte[] imageBytes = StreamUtils.readUnknownFully(inputStream);
                Bitmap imageBitmap = StaticTools.convertToBitmap(imageBytes);*/
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);

                // Can you think of a way to improve loading of b   itmaps
                // that have already been loaded previously??

                /*
                 * Now it's used LruCache Android Class to have a real cache for the images
                 */
                if( imageBitmap!= null){
                    //StaticTools.simpleCache.put(imageUrl, imageBitmap);
                    //if the images is not null it's saved in the cache
                    ImageCache.getInstance().getLru().put(imageUrl, imageBitmap);
                }

                //Message preparation
                Message msg = mUIHandler.obtainMessage(IMAGE_DOWNLOADED);
                Bundle bundle = new Bundle();
                bundle.putInt(IMAGE_POSITION_KEY, position);
                bundle.putString(IMAGE_URL_KEY, imageUrl);
                msg.setData(bundle);
                //Message posts to UIHandler
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

