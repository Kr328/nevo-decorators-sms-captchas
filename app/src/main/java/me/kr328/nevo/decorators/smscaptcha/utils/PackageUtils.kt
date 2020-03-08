package me.kr328.nevo.decorators.smscaptcha.utils

import android.content.Context
import android.content.pm.PackageManager

object PackageUtils {
    fun hasPackageInstalled(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }
}