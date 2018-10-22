package com.waracle.androidtest;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.waracle.androidtest.adapters.MyAdapter;
import com.waracle.androidtest.threadsManagements.ImageLoaderHandler;
import com.waracle.androidtest.threadsManagements.JosonLoader;
import com.waracle.androidtest.threadsManagements.UIHandler;
import com.waracle.androidtest.utils.StaticTolls;

import org.json.JSONArray;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONArray> {

    /**
     * In this test is required to not use thirdpart Libraries, to handle the REST request I would have used
     * Retrofit (Volley as second choice).
     * Without libraries I implement an AsyncTaskLoader to retrieve data from the web with REST way.
     * The  AsyncTaskLoader permits to download the data ina a separete thread different from the mainthread.
     * This is mandatory to not throw a NetworkOnMainThreadException
     * I choose AsyncTaskLoader instead of a simply Asynctask because the first one, with its ID, is threadsafe in case
     * of the caller activity is killed by a configuration changes event, for example. So when the caller Activity dieds
     * does not stay appended any AsyncTasks that's looks for their caller.
     * */

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

    private static final int CAKE_LOADER_ID = 0;

    private static LinearLayoutManager mLayoutManager;
    private static LoaderManager.LoaderCallbacks<JSONArray> mCallback;

    private static MyAdapter mAdapter;

    private ImageLoaderHandler mImageLoaderHandler;
    private UIHandler mUIHandler;
    private HandlerThread mHtHandler;

    private ImageView mCoverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);

        //StaticTolls.simpleCache = new Hashtable<String, Bitmap>();

        /*
         * From MainActivity, I have implemented the LoaderCallbacks interface with the type of
         * Joson array. (implements LoaderCallbacks<JSONArray>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify to MainActivity it will do so through this callback.
         */
        mCallback = (LoaderManager.LoaderCallbacks<JSONArray>) MainActivity.this;

        //Instantiate and return a new Loader for the given ID.
        int loaderId = CAKE_LOADER_ID;
        Bundle bundleForLoader = null;
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, mCallback);

        /*
         * As said above in this test is required to not use thirdpart Libaries. To handle the web download
         * of images I would have use Picasso.
         * To do this work without libraries I've impementd a HandlerThead and two handler to support it.
         * One of these Handler runs on the Mainthread to let to update UI layout
         * I need a HandlerThead to realize a queue of message of request for each image to retrieve
         * that run on a separated thread out of the Mainthread
         */
        mHtHandler = new HandlerThread("ImageDownloaderThread");
        mHtHandler.start();
        mUIHandler = new UIHandler();
        mUIHandler.initializedUIHandler();
        //HandlerThead is passed to a handler to manage the message queue
        mImageLoaderHandler = new ImageLoaderHandler(mHtHandler.getLooper());
        mImageLoaderHandler.setUIHandler(mUIHandler);
        /*
         * UIthread receives the result images from mImageLoaderHandler to set them on the respective
         * Imageviews on the Mainthread to update layout
         */
        mUIHandler.setImageLoaderHandler(mImageLoaderHandler);

        /*
         * Supporting the ratation, it has been diversify the layout for the landascape mode, in this case
         * it's present an ImaheView that shows a random image from cakes that have one and getting it
         * from the cache, if cache is not empty.
         * mCoverImage is this ImagegeView
         * */
        mCoverImage = (ImageView) findViewById(R.id.cover_image);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //When the Activity is destroyed it's needed to stop the HandlerThread
        mHtHandler.quit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //Here I implement the refresh action
            invalidateData();
            getSupportLoaderManager().restartLoader(CAKE_LOADER_ID, null, this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param bundle Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<JSONArray> onCreateLoader(int id, @Nullable Bundle bundle) {

        return new JosonLoader(MainActivity.this, JSON_URL);
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param jsonArray The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<JSONArray> loader, JSONArray jsonArray) {
        //StaticTolls.simpleCache.clear();
        mAdapter.setItems(jsonArray);
        mAdapter.setUIHandler(mUIHandler);
        //Downloaded the cakes data it's called the method to set the mCoverImage
        setCoverImage();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<JSONArray> loader) {
        /*
         * I'm not using this method, but it's ever required to Override
         * it to implement the LoaderCallbacks<JSONArray> interface
         */
    }

    /**
     * Fragment is responsible for loading in some JSON and
     * then displaying a list of cakes with images.
     * Fix any crashes
     * Improve any performance issues
     * Use good coding practices to make code more secure
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        /*
         * I have replaced Listfragment with a RecyclerView beacuse the lastone is more
         * flexyble when it's needed to customize the list and it's more powerful for better and smoother
         * animations above all in case of a lot of items in in the list
         * */
        private RecyclerView mRecyclerView;

        public PlaceholderFragment() { /**/ }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_cakes);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mRecyclerView.setLayoutManager(mLayoutManager);
            /*
             * Use this setting to improve performance if you know that changes in content do not
             * change the child layout size in the RecyclerView
             */
            mRecyclerView.setHasFixedSize(true);

            mAdapter = new MyAdapter();
            /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mRecyclerView.setAdapter(mAdapter);
        }

    }
    /**
     * This refresh cakes data mUIHandler is reinitialized,
     * Items data erased in MyAdapter
     * */
    private void invalidateData() {
        //StaticTolls.simpleCache.clear();
        mUIHandler.initializedUIHandler();
        mAdapter.setItems(null);
    }

    /**
     * (Portrait mode: mCoverImage is null)
     * (Landscape mode: mCoverImage is not null)
     * */
    public void setCoverImage(){
        if(mCoverImage != null){
            //mCoverImage is seted in the MyAdapter tha has all it's needed
            mAdapter.setCoverImage(mCoverImage);
        }
    }
}
