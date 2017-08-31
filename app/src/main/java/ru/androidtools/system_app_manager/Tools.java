package ru.androidtools.system_app_manager;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.DataOutputStream;
import java.io.File;

/**
 * Created by dev on 22.04.17.
 */

public class Tools {

    public static boolean m521a(String str) {
        try {
            if (new File(str).exists()) {
                String parent = new File(str).getParent();
                Process exec = Runtime.getRuntime().exec("su");
                DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());
                dataOutputStream.writeBytes("mount -o rw,remount /system; \n");
                dataOutputStream.writeBytes("chmod 777 " + parent + "; \n");
                dataOutputStream.writeBytes("chmod 777 " + str + "; \n");
                dataOutputStream.writeBytes("rm -r " + str + "; \n");
                dataOutputStream.writeBytes("mount -o ro,remount /system; \n");
                dataOutputStream.flush();
                dataOutputStream.close();
                exec.waitFor();

                //Toast.makeText(context, "Uninstalled", Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
        return true;
    }

    /*
       public static void uninstallSystemApp(DeleteAppItem deleteAppItem, Context context) {
           String str = context.getFilesDir() + "/busybox mount -o remount,rw /system";
           String str2 = "rm " + deleteAppItem.getCodePath();
           String str3 = "rm " + deleteAppItem.getDataDir();
           String str4 = "rm " + deleteAppItem.getNativeLibraryDir();
           String str5 = "pm uninstall " + deleteAppItem.getAppPackage();
           ShellUtils.execCommand(new String[]{str, "mount -o remount,rw /system", str2, str3, str4, str5}, true, true, 60);
       }
   */
    public static boolean isPackageInstalled(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static boolean checkRooted() {
        try {
            Process p = Runtime.getRuntime().exec("su", null, new File("/"));
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pwd\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            return false;
        }

        return true;
    }


}
