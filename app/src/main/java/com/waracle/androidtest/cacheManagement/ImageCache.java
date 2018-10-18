package com.waracle.androidtest.cacheManagement;

import android.util.LruCache;

/**
 * This Android Class ad-hoc lets implement a real cache to handle the images downloaded
 * */
public class ImageCache{
    private static final int CACHE_SIZE = 1024; //Configurable cache size

    private static ImageCache instance;
    private LruCache<Object, Object> lru;

    private ImageCache() {
        lru = new LruCache<Object, Object>(CACHE_SIZE);
    }

    public static ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    public LruCache<Object, Object> getLru() {
        return lru;
    }

}
