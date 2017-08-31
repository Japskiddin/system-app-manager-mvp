package ru.androidtools.system_app_manager.model;

import java.util.List;

/**
 * Created by Nikita on 28.08.2017.
 */

public interface AppsInteractor {
    List<AppInfo> filterApps(int currentList, List<AppInfo> mAppsList);
}
