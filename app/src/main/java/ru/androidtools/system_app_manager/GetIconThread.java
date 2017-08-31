package ru.androidtools.system_app_manager;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GetIconThread<T> extends HandlerThread {
    private static final String TAG = "GetIconThread";
    private static final int MESSSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private PackageManager packageManager;
    private Handler mResponseHandler;
    private GetIconThreadListener<T> mGetIconThreadListener;
    private IconCache iconCache;

    public interface GetIconThreadListener<T> {
        void onIconDownloaded(T target, Drawable icon);
    }

    public void setIconDownloadListener(GetIconThreadListener<T> listener) {
        mGetIconThreadListener = listener;
    }

    public GetIconThread(Handler responseHandler, PackageManager packageManager, IconCache iconCache) {
        super(TAG);
        this.packageManager = packageManager;
        mResponseHandler = responseHandler;
        this.iconCache = iconCache;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Получен запрос для пакета: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        final String packageName = mRequestMap.get(target);
        final Drawable icon;

        if (packageName != null) { // во избежание key == null
            try {
                if (iconCache.getBitmapFromMemory(packageName) == null) {
                    icon = packageManager.getApplicationIcon(packageName);
                    iconCache.setBitmapToMemory(packageName, icon);
                } else {
                    icon = iconCache.getBitmapFromMemory(packageName);
                }
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRequestMap.get(target) == null || !mRequestMap.get(target).equals(packageName)) {
                            return;
                        }
                        mRequestMap.remove(target);
                        mGetIconThreadListener.onIconDownloaded(target, icon);
                    }
                });
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void queueIcon(T target, String packageName) {
        Log.i(TAG, "Получен packageName: " + packageName);
        if (packageName == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, packageName);
            mRequestHandler.obtainMessage(MESSSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSSAGE_DOWNLOAD);
    }
}






