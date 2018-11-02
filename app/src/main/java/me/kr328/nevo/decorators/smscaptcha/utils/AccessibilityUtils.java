package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class AccessibilityUtils {
    public static boolean isAccessibilityEnabled(Context context , ComponentName componentName){
        try {
            if ( Settings.Secure.getInt(context.getContentResolver() ,Settings.Secure.ACCESSIBILITY_ENABLED) == 1 ){
                String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
                    mStringColonSplitter.setString(settingValue);
                    while (mStringColonSplitter.hasNext()) {
                        String accessibilityService = mStringColonSplitter.next();
                        Log.i(AccessibilityUtils.class.getSimpleName() ,accessibilityService);
                        if (accessibilityService.equalsIgnoreCase(componentName.toString()))
                            return true;
                    }
                }
            }
        } catch (Settings.SettingNotFoundException ignored) {}
        return false;
    }
}
