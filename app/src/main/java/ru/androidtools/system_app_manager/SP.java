package ru.androidtools.system_app_manager;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class SP {
    private final static String PREF_FILE = "ProfileActivity";
    public final static String SHOW_UNINSTALL_DIALOG = "SHOW_UNINSTALL_DIALOG";

    public static final String ROOT_ACTIVE = "root_active";
    public static final String FIRST_ROOT_IMPORT = "first_root_import";
    private static SharedPreferences mSettings;

    private static SharedPreferences getSettings(Context context) {
        if (mSettings == null)
            mSettings = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        return mSettings;
    }

    static void setString(Context context, String key, String value) {


        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setBoolean(Context context, String key, Boolean value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }


    static String getString(Context context, String key, String defValue) {
        return getSettings(context).getString(key, defValue);
    }


    static int getInt(Context context, String key, int defValue) {
        return getSettings(context).getInt(key, defValue);
    }


    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getSettings(context).getBoolean(key, defValue);
    }
}
