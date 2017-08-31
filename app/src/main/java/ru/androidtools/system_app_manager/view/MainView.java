package ru.androidtools.system_app_manager.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import java.util.List;

import ru.androidtools.system_app_manager.AppsAdapter;
import ru.androidtools.system_app_manager.GetIconThread;
import ru.androidtools.system_app_manager.model.AppInfo;

/**
 * Created by Nikita on 28.08.2017.
 */

public interface MainView {
    void onDownloadApps(List<AppInfo> mAppsList, GetIconThread<AppsAdapter.AppViewHolder> getIconThread, int currentList);

    PackageManager getPackageManagerFromActivity();

    int getMemoryClassFromActivity();

    void onToastReady(String message);

    void onGoneFooter();

    void onVisibleFooter();

    void onSelectionChanged(List<AppInfo> selectedList);

    void onSelectionClear();

    void onPrepareDeleteDialog(List<AppInfo> selectedList);

    void onUpdatedList(AppInfo ai);

    void onSetToolbarTitle(String title);

    void onReadyActivityStart(Intent intent);

    void onReadyActivityStartForResult(Intent intent);

    String getStringFromResources(int id);

    boolean isPackageInstalled(String packageName);

    View getViewForDialog(int layout);

    void saveCheckedUninstallDialog();
}
