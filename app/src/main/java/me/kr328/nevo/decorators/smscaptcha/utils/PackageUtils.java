package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtils {
    public static boolean hasPackageInstalled(Context context ,String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName ,0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        return true;
    }
}
