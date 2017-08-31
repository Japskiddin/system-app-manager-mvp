package ru.androidtools.system_app_manager.model;

import java.io.Serializable;

/**
 * Created by volodya on 06.02.2015.
 */
public class AppInfo implements Serializable {
    public AppInfo(String title, String packageName, String sourceDir, String publicSourceDir, String versionName, int versionCode, boolean isSystem, String size, String dataDir, String nativeLibraryDir) {
        this.title = title;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
        this.publicSourceDir = publicSourceDir;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.isSystem = isSystem;
        this.size = size;
        this.dataDir = dataDir;
        this.nativeLibraryDir = nativeLibraryDir;
    }

    public boolean isSystem;
    public String title;
    public String packageName;
    public String sourceDir;
    public String publicSourceDir;
    public String versionName;
    public String size;
    public int versionCode;
    public String dataDir, nativeLibraryDir;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppInfo) {
            AppInfo temp = (AppInfo) obj;
            if (this.title.equals(temp.title) && this.packageName.equals(temp.packageName))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.title.hashCode() + this.packageName.hashCode());
    }
}
