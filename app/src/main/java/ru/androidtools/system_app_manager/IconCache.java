package ru.androidtools.system_app_manager;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by Nikita on 21.08.2017.
 */

public class IconCache extends LruCache<String, Drawable> {

    public IconCache(int maxSize) {
        super(maxSize);
    }

    public Drawable getBitmapFromMemory(String key) {
        return this.get(key);
    }

    public void setBitmapToMemory(String key, Drawable drawable) {
        if (getBitmapFromMemory(key) == null) {
            this.put(key, drawable);
            Log.d("TEST", key + " добавлен в кэш");
        }
    }
}
