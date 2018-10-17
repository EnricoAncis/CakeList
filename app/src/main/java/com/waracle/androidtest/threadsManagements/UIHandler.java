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

public class UIHandler extends Handler {
    private static final String TAG = UIHandler.class.getSimpleName();

    private ImageLoaderHandler mImageLoaderHandler = null;
    private Map<Integer, ImageView> mImageViewMap;

    public void initializedUIHandler(){
        mImageViewMap = new Hashtable<Integer, ImageView>();
    }

    public void setImageLoaderHandler(ImageLoaderHandler imageLoaderHandler) {
        mImageLoaderHandler = imageLoaderHandler;
    }


    public void setImageView(ImageView imageView, String imageUrl, int pos) {
        mImageViewMap.put(pos, imageView);

        Message msg = mImageLoaderHandler.obtainMessage(GOT_IMAGEURL);
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URL_KEY, imageUrl);
        bundle.putInt(IMAGE_POSITION_KEY, pos);
        msg.setData(bundle);
        mImageLoaderHandler.sendMessage(msg);
        Log.e(TAG, "--> SEND image url: " + imageUrl);

    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case IMAGE_DOWNLOADED:
                Log.e(TAG, "--> GOT BACK image");
                Bundle imageBundle = msg.getData();

                int imagePos = imageBundle.getInt(IMAGE_POSITION_KEY);
                String imageUrl = imageBundle.getString(IMAGE_URL_KEY);

                if(mImageViewMap.containsKey(imagePos) &&
                        StaticTolls.simpleCache.containsKey(imageUrl)){

                    Bitmap imageBitmap =  StaticTolls.simpleCache.get(imageUrl);
                    ImageView imgView = mImageViewMap.get(imagePos);
                    imgView.setImageBitmap(imageBitmap);
                    Log.e(TAG, "--> SETTED image in imageview ");
                }
                break;
        }
    }

}

