package ru.androidtools.system_app_manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.format.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.androidtools.system_app_manager.model.AppInfo;


/**
 * Created by dev on 18.12.16.
 */

public class GetAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {
    private static final String TAG = "GetAppsTask";
    private Context context;
    private AsyncResponse delegate = null;

    public interface AsyncResponse {
        void onTaskComplete(List<AppInfo> result);
    }

    public GetAppsTask(Context context) {
        this.context = context;
    }

    public void setAsyncResponce(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected List<AppInfo> doInBackground(Void... params) {
        final PackageManager pm = context.getPackageManager();

        List<AppInfo> apps = new ArrayList<>();

        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : packages) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(
                        appInfo.packageName, 0);
                File file = new File(appInfo.publicSourceDir);
                String size = formateFileSize(context, file.length());

                //  Log.d("TAG", appInfo.packageName + " " + hash512);
                apps.add(new AppInfo(applicationLabel(context, appInfo), appInfo.packageName, appInfo.sourceDir, appInfo.publicSourceDir, packageInfo.versionName, packageInfo.versionCode, isSystemPackage(packageInfo), size, appInfo.dataDir, appInfo.nativeLibraryDir));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return apps;
    }


    @Override
    protected void onPostExecute(List<AppInfo> result) {
        super.onPostExecute(result);
        if (result != null && delegate != null)
            delegate.onTaskComplete(result);
    }

    private String formateFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private String applicationLabel(Context con, ApplicationInfo packageInfo) {
        PackageManager p = con.getPackageManager();
        return p.getApplicationLabel(packageInfo).toString();
    }
}