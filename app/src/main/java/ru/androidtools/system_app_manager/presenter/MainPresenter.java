package ru.androidtools.system_app_manager.presenter;

import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;

import ru.androidtools.system_app_manager.GetAppsTask;
import ru.androidtools.system_app_manager.model.AppInfo;
import ru.androidtools.system_app_manager.view.MainView;

/**
 * Created by Nikita on 28.08.2017.
 */

public interface MainPresenter {
    void detachView();

    void attachView(MainView mainView);

    void downloadApps(GetAppsTask mGetAppsTask);

    void changeList(int change);

    void clearQueue();

    void deleteApp();

    void addToSelected(AppInfo ai);

    void clearSelection();

    void updateAppList();

    void prepareRootDialog(AlertDialog.Builder builder);

    void onWikiClick();

    void onMarketClick();

    void onSettingsClick();

    void onShareClick(ShareCompat.IntentBuilder intentBuilder);

    void onSendClick(ShareCompat.IntentBuilder intentBuilder);

    void showUninstallDialog(AlertDialog.Builder builder);
}
