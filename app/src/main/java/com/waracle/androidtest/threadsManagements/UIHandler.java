package com.waracle.androidtest.threadsManagements;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.waracle.androidtest.utils.StaticTolls;

import java.util.Hashtable;
import java.util.Map;

import static com.waracle.androidtest.utils.Constants.GOT_IMAGEURL;
import static com.waracle.androidtest.utils.Constants.IMAGE_DOWNLOADED;
import static com.waracle.androidtest.utils.Constants.IMAGE_POSITION_KEY;
import static com.waracle.androidtest.utils.Constants.IMAGE_URL_KEY;

/**
 * UIthread is the handler that receives downloaded images form mImageLoaderHandler and sets
 * them to the respective Imageviews on the Mainthread to update layout
 */
public class UIHandler extends Handler {
    private static final String TAG = UIHandler.class.getSimpleName();

    private ImageLoaderHandler mImageLoaderHandler = null;

    /**
     * I use an Hashtable (Recyclerview item position int as Key, ImageView from MyAdapter as Values)
     * to assign the right downloaded image to the right MyAdapter Imageview item
     * */
    private Map<Integer, ImageView> mImageViewMap;

    /**
     * This costructor id used to initialize the Hashtable
     * */
    public void initializedUIHandler(){
        mImageViewMap = new Hashtable<Integer, ImageView>();
    }

    /**
     * This method to assigns the ImageLoaderHandler from MyAdapter.
     * ImageLoaderHandler is needed to post Messages request to its
     *
     * @param imageLoaderHandler ImageLoaderHandler from MyAdapter
     * */
    public void setImageLoaderHandler(ImageLoaderHandler imageLoaderHandler) {
        mImageLoaderHandler = imageLoaderHandler;
    }

    /**
     * This sets the ImageViews from MyAdapter to the Hashmap.
     * Here is prepared and posted the Message request to the ImageLoaderHandler
     *
     * @param imageView ImageViews from MyAdapter
     * @param imageUrl image url from MyAdapter to download
     * @param pos Recyclerview item posistion from MyAdapter
     * */
    public void setImageView(ImageView imageView, String imageUrl, int pos) {
        //Position/ImageView assignement
        mImageViewMap.put(pos, imageView);

        //Message preparation
        Message msg = mImageLoaderHandler.obtainMessage(GOT_IMAGEURL);
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URL_KEY, imageUrl);
        bundle.putInt(IMAGE_POSITION_KEY, pos);
        msg.setData(bundle);

        //Message posted to the ImageLoaderHandler
        mImageLoaderHandler.sendMessage(msg);
        Log.e(TAG, "--> SEND image url: " + imageUrl);

    }
    /**
     * This Overridden method is needed to handle messages post by the ImageLoaderHandler with its results
     * */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case IMAGE_DOWNLOADED:  //Messagge waited
                Log.e(TAG, "--> GOT BACK image");

                //Retrieve data from the response
                Bundle imageBundle = msg.getData();

                //retrieve item position
                int imagePos = imageBundle.getInt(IMAGE_POSITION_KEY);
                //retrieve image url
                String imageUrl = imageBundle.getString(IMAGE_URL_KEY);

                if(mImageViewMap.containsKey(imagePos) &&
                        StaticTolls.simpleCache.containsKey(imageUrl)){

                    //Image url to retrieves the image Bitmap from the Hashmap StaticTolls.simpleCache
                    Bitmap imageBitmap =  StaticTolls.simpleCache.get(imageUrl);
                    //Item position to retrieve the right item ImageView to set right image
                    ImageView imgView = mImageViewMap.get(imagePos);
                    imgView.setImageBitmap(imageBitmap);
                    Log.e(TAG, "--> SETTED image in imageview ");
                }
                break;
        }
    }

}

