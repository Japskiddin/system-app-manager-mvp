package ru.androidtools.system_app_manager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.androidtools.system_app_manager.R;

/**
 * Created by Nikita on 28.08.2017.
 */

public class AppsInteractorImpl implements AppsInteractor {
    @Override
    public List<AppInfo> filterApps(int currentList, List<AppInfo> mAppsList) {
        List<AppInfo> list = new ArrayList<>();
        if (currentList == R.string.menu_installed) {
            for (AppInfo appInfo : mAppsList)
                if (!appInfo.isSystem) list.add(appInfo);
        } else {
            for (AppInfo appInfo : mAppsList)
                if (appInfo.isSystem) list.add(appInfo);
        }
        sortAppList(list);
        return list;
    }

    private static void sortAppList(List<AppInfo> apps) {
        Comparator<AppInfo> myComparator = new Comparator<AppInfo>() {
            public int compare(AppInfo obj1, AppInfo obj2) {
                return obj1.title.compareToIgnoreCase(obj2.title);
            }
        };
        Collections.sort(apps, myComparator);
    }
}
