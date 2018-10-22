package com.waracle.androidtest.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.waracle.androidtest.cacheManagement.ImageCache;
import com.waracle.androidtest.unused.ImageLoader;
import com.waracle.androidtest.R;
import com.waracle.androidtest.threadsManagements.ImageLoaderHandler;
import com.waracle.androidtest.threadsManagements.UIHandler;
import com.waracle.androidtest.utils.StaticTolls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyAdapterViewHolder>  {
    private static final String TAG = MyAdapter.class.getSimpleName();

    JSONArray mCakesData = null;
    ImageLoader mImageLoader;
    UIHandler mUIHandler;

    public MyAdapter() {
        mImageLoader = new ImageLoader();
    }


    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item it
     *                  can use this viewType integer to provide a different layout.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MyAdapter.MyAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MyAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder    The holder which should be updated to represent the
     *                  contents of the item at the given position in the data set.
     * @param position  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MyAdapter.MyAdapterViewHolder holder, int position) {
            try {
                JSONObject object = (JSONObject) mCakesData.getJSONObject(position);
                holder.mTitleView.setText(object.getString("title"));
                holder.mDescView.setText(object.getString("desc"));
                holder.mImageView.setImageBitmap(null);
                String imageUrl = object.getString("image");

                /*
                 * Also now if images not in the cache has to be downloaded and save in
                 * ImageLoaderHandler , but this time it's used Lru Cache Android Class
                 * */
                Bitmap imageBitmap = (Bitmap)ImageCache.getInstance().getLru().get(imageUrl);
                if(imageBitmap != null){
                    //Bitmap imageBitmap = StaticTolls.simpleCache.get(imageUrl);
                    holder.mImageView.setImageBitmap(imageBitmap);
                }
                else{
                    Log.e(TAG, "--> SET imageView");
                    mUIHandler.setImageView(holder.mImageView, imageUrl, position);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getStackTrace().toString());
            }


    }

    /**
     * This method simply returns the number of items to display. It is used behind and
     * it's the corrispondent in ListView
     *
     * @return The number of items available in our cakes list
     */
    @Override
    public int getItemCount() {
        int index = 0;
        if (mCakesData != null){
            index = mCakesData.length();
        }
        return index;
    }

    /**
     * Cache of the children views for a cakes list item.
     */
    public class MyAdapterViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mImageView;
        public final TextView mTitleView;
        public final TextView mDescView;

        public MyAdapterViewHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.image);
            mTitleView = (TextView) itemView.findViewById(R.id.title);
            mDescView = (TextView) itemView.findViewById(R.id.desc);
        }
    }

    /**
     * This method is used to set the cakes data on MyAdapter if we've already
     * created one. This is handy when it's gotten new data from the web but doesn't want to create a
     * new MyAdapter to display it.
     *
     * @param cakeData The new cakes data to be displayed.
     */
    public void setItems(JSONArray cakeData) {
        mCakesData = cakeData;
        notifyDataSetChanged();
    }

    /**
     * This method sets the UIHandlet to the MyAdapter to let it to set images in the Imageviews.
     *
     * @param uiHandler The UIHandler.
     */
    public void setUIHandler(UIHandler uiHandler) {
        mUIHandler = uiHandler;
    }

    /**
     * This is the method to set the random image in the mCoverImage
     *
     * @param coverImageView Imageview of the landscape mode
     * */
    public void setCoverImage(ImageView coverImageView){

        //Here it's seted the cakes data in a static variable to the static method in easy way
        StaticTolls.setCakesData(mCakesData);
        coverImageView.setImageBitmap(null);

        if(mCakesData != null){
            //setRandomImage(coverImageView);
            //If the cache is populated the random cake image is get from it
            if(ImageCache.getInstance().getLru().size() > 0){
                //Here it's obtained a random image key
                String randomKey = StaticTolls.getRandomCacheKey();
                Bitmap imageBitmap = (Bitmap) ImageCache.getInstance().getLru().snapshot().get(randomKey);
                coverImageView.setImageBitmap(imageBitmap);
            }
            else{
                //Here it's obtained a random image url to download it from the web
                String imageUrl = StaticTolls.getRandomImageUrl();
                mUIHandler.setImageView(coverImageView, imageUrl, -1); //Now It's not neede the
                //position because setImageView
                //is not workinking for the
                //Rectclerview
            }

        }
    }
}
