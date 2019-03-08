package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class PackageUtils {
    public static long getPackageVersionCode(Context context ,String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName ,0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                return packageInfo.getLongVersionCode();
            else
                return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
