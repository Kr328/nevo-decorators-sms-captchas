package me.kr328.nevo.decorators.smscaptcha;

import net.grandcentrix.tray.TrayPreferences;

public class Settings {
    public final static String SETTING_PLUGIN_ENABLED                  = "setting_plugin_enabled";
    public final static String SETTING_CAPTCHA_HIDE_ON_LOCKED          = "setting_captcha_hide_on_locked";
    public final static String SETTING_CAPTCHA_CHECK_PATTERN           = "setting_captcha_check_pattern";
    public final static String SETTING_CAPTCHA_PARSE_PATTERN           = "setting_captcha_parse_pattern";

    public boolean pluginEnabled;
    public boolean captchaHideOnLocked;
    public String  captchaCheckPattern;
    public String  captchaParsePattern;

    public Settings readFromTrayPreference(TrayPreferences preferences) {
        pluginEnabled       = preferences.getBoolean(SETTING_PLUGIN_ENABLED         ,pluginEnabled);
        captchaHideOnLocked = preferences.getBoolean(SETTING_CAPTCHA_HIDE_ON_LOCKED ,captchaHideOnLocked);
        captchaCheckPattern = preferences.getString(SETTING_CAPTCHA_CHECK_PATTERN   ,captchaCheckPattern);
        captchaParsePattern = preferences.getString(SETTING_CAPTCHA_PARSE_PATTERN   ,captchaParsePattern);

        return this;
    }

    public Settings readFromTrayPreference(TrayPreferences preferences ,String key) {
        switch ( key ) {
            case Settings.SETTING_PLUGIN_ENABLED :
                pluginEnabled = preferences.getBoolean(key ,pluginEnabled);
                break;
            case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED :
                captchaHideOnLocked = preferences.getBoolean(key ,captchaHideOnLocked);
                break;
            case Settings.SETTING_CAPTCHA_CHECK_PATTERN :
                captchaCheckPattern = preferences.getString(key ,captchaCheckPattern);
                break;
            case Settings.SETTING_CAPTCHA_PARSE_PATTERN :
                captchaParsePattern = preferences.getString(key ,captchaParsePattern);
                break;
        }

        return this;
    }

    public Settings writeToTrayPreference(TrayPreferences preferences) {
        preferences.put(SETTING_PLUGIN_ENABLED         ,pluginEnabled);
        preferences.put(SETTING_CAPTCHA_HIDE_ON_LOCKED ,captchaHideOnLocked);
        preferences.put(SETTING_CAPTCHA_CHECK_PATTERN  ,captchaCheckPattern);
        preferences.put(SETTING_CAPTCHA_PARSE_PATTERN  ,captchaParsePattern);

        return this;
    }

    @Override
    public String toString() {
        return  " PluginEnabled=" + pluginEnabled       +
                " HideOnLocked="  + captchaHideOnLocked +
                " CheckPattern="  + captchaCheckPattern +
                " ParsePattern="  + captchaParsePattern ;
    }

    public Settings() {
        this(true ,true ,"" ,"");
    }

    public Settings(boolean pluginEnabled ,boolean captchaHideOnLocked ,String captchaCheckPattern ,String captchaParsePattern) {
        this.pluginEnabled       = pluginEnabled;
        this.captchaHideOnLocked = captchaHideOnLocked;
        this.captchaCheckPattern = captchaCheckPattern;
        this.captchaParsePattern = captchaParsePattern;
    }
}
