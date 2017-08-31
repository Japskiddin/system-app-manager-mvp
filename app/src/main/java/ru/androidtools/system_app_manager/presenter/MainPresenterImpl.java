package ru.androidtools.system_app_manager.presenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import ru.androidtools.system_app_manager.AppsAdapter;
import ru.androidtools.system_app_manager.GetAppsTask;
import ru.androidtools.system_app_manager.GetIconThread;
import ru.androidtools.system_app_manager.IconCache;
import ru.androidtools.system_app_manager.R;
import ru.androidtools.system_app_manager.model.AppInfo;
import ru.androidtools.system_app_manager.model.AppsInteractor;
import ru.androidtools.system_app_manager.view.MainView;

/**
 * Created by Nikita on 28.08.2017.
 */

public class MainPresenterImpl implements MainPresenter {
    private MainView mainView;
    private AppsInteractor appsInteractor;
    private List<AppInfo> mAppsList;
    private GetIconThread<AppsAdapter.AppViewHolder> mGetIconThread;
    private int currentList = R.string.menu_installed;
    private List<AppInfo> mSelectedList;

    public MainPresenterImpl(MainView mainView, AppsInteractor appsInteractor) {
        this.mainView = mainView;
        this.appsInteractor = appsInteractor;
        mAppsList = new ArrayList<>();
        mSelectedList = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        mGetIconThread.quit();
        mainView = null;
    }

    @Override
    public void onStop() {
        mGetIconThread.clearQueue();
    }

    @Override
    public void downloadApps(GetAppsTask mGetAppTask) {
        mainView.onSetToolbarTitle(mainView.getStringFromResources(R.string.loading));
        setIconTask();
        mGetAppTask.setAsyncResponce(new GetAppsTask.AsyncResponse() {
            @Override
            public void onTaskComplete(List<AppInfo> result) {
                mAppsList = result;
                initAppsAdapter();
            }
        });
        mGetAppTask.execute();
    }

    @Override
    public void changeList(int change) {
        currentList = change;
        initAppsAdapter();
    }

    @Override
    public void addToSelected(AppInfo ai) {
        Log.d("TAG", "addToSelected");
        if (!mSelectedList.contains(ai)) {
            mSelectedList.add(ai);
        } else {
            mSelectedList.remove(ai);
        }
        mainView.onSelectionChanged(mSelectedList);
        if (mSelectedList.isEmpty()) {
            mainView.onGoneFooter();
        } else {
            mainView.onVisibleFooter();
        }
    }

    @Override
    public void clearSelection() {
        mSelectedList.clear();
        mainView.onSelectionClear();
    }

    @Override
    public void deleteApp() {
        Log.d("TAG", "deleteApp");
        if (currentList == R.string.menu_installed) {
            for (AppInfo ai : mSelectedList) {
                Uri packageURI = Uri.parse("package:" + ai.packageName);
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                mainView.onReadyActivityStartForResult(uninstallIntent);
                mAppsList.remove(ai);
            }
        } else {
            mainView.onSetToolbarTitle(mainView.getStringFromResources(R.string.deleting));
            mainView.onPrepareDeleteDialog(mSelectedList);
        }
    }

    @Override
    public void updateAppList() {
        Log.d("TAG", "updateAppList");
        for (AppInfo ai : mSelectedList) {
            if (!mainView.isPackageInstalled(ai.packageName)) {
                mainView.onUpdatedList(ai);
            }
        }
        List<AppInfo> list = appsInteractor.filterApps(currentList, mAppsList);
        mainView.onSetToolbarTitle(mainView.getStringFromResources(currentList) + " (" + list.size() + ")");
        clearSelection();
    }

    @Override
    public void prepareRootDialog(AlertDialog.Builder builder) {
        builder.setMessage(R.string.system_app_no_root_access).setTitle(R.string.common_warning);
        builder.setPositiveButton(R.string.root_help_title, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onWikiClick();
            }
        });
        builder.setNegativeButton(R.string.common_i_know, null);
        builder.create().show();
    }

    @Override
    public void onWikiClick() {
        Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(mainView.getStringFromResources(R.string.wiki_url)));
        if (intentBrowser.resolveActivity(mainView.getPackageManagerFromActivity()) != null) {
            mainView.onReadyActivityStart(intentBrowser);
        } else {
            mainView.onToastReady(mainView.getStringFromResources(R.string.error_missing_browser));
        }
    }

    @Override
    public void onMarketClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Android Tools (ru)"));
        if (intent.resolveActivity(mainView.getPackageManagerFromActivity()) != null) {
            mainView.onReadyActivityStart(intent);
        } else {
            mainView.onToastReady(mainView.getStringFromResources(R.string.error_missing_market));
        }
    }

    @Override
    public void onSettingsClick() {
        mainView.onToastReady(mainView.getStringFromResources(R.string.settings_empty));
    }

    @Override
    public void onShareClick(ShareCompat.IntentBuilder intentBuilder) {
        intentBuilder.setText(mainView.getStringFromResources(R.string.send_message) + " " + mainView.getStringFromResources(R.string.share_text));
        intentBuilder.setType("text/plain"); // most general text sharing MIME type
        intentBuilder.setChooserTitle(R.string.share_title);
        intentBuilder.startChooser();
    }

    @Override
    public void onSendClick(ShareCompat.IntentBuilder intentBuilder) {
        intentBuilder.setType("message/rfc822");
        intentBuilder.setSubject(mainView.getStringFromResources(R.string.app_name));
        intentBuilder.setText(mainView.getStringFromResources(R.string.send_message) + " " + mainView.getStringFromResources(R.string.share_text));
        intentBuilder.setChooserTitle(R.string.send_title);
        intentBuilder.startChooser();
    }

    @Override
    public void showUninstallDialog(AlertDialog.Builder builder) {
        View dialogView = mainView.getViewForDialog(R.layout.uninstall_dialog);
        builder.setView(dialogView);
        CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mainView.saveCheckedUninstallDialog();
                }
            }
        });
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    private void initAppsAdapter() {
        List<AppInfo> list = appsInteractor.filterApps(currentList, mAppsList);
        mainView.onSetToolbarTitle(mainView.getStringFromResources(currentList) + " (" + list.size() + ")");
        mainView.onDownloadApps(list, mGetIconThread, currentList);
    }

    private void setIconTask() {
        int memClass = mainView.getMemoryClassFromActivity();
        int cacheSize = 1024 * 1024 * memClass / 8;
        IconCache iconCache = new IconCache(cacheSize);

        Handler responseHandler = new Handler();
        mGetIconThread = new GetIconThread<>(responseHandler, mainView.getPackageManagerFromActivity(), iconCache);
        mGetIconThread.setIconDownloadListener(
                new GetIconThread.GetIconThreadListener<AppsAdapter.AppViewHolder>() {
                    @Override
                    public void onIconDownloaded(AppsAdapter.AppViewHolder target, Drawable icon) {
                        target.bindDrawable(icon);
                    }
                });
        mGetIconThread.start();
        mGetIconThread.getLooper();
    }
}
