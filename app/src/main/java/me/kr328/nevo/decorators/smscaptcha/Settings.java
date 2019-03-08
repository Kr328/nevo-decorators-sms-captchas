package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;
import android.content.Context;

import net.grandcentrix.tray.TrayPreferences;

public class Settings extends SettingsBase {
    public final static String SETTING_CAPTCHA_HIDE_ON_LOCKED = "setting_captcha_hide_on_locked";
    public final static String SETTING_CAPTCHA_IDENTIFY_PATTERN = "setting_captcha_identify_pattern";
    public final static String SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION = "setting_captcha_override_default_action";
    public final static String SETTING_CAPTCHA_USE_DEFAULT_PATTERN = "setting_captcha_use_default_pattern";
    public final static String SETTING_CAPTCHA_PARSE_PATTERN = "setting_captcha_parse_pattern";

    private boolean captchaHideOnLocked;
    private boolean captchaOverrideDefaultAction;
    private boolean captchaUseDefaultPattern;
    private String captchaIdentifyPattern;
    private String captchaParsePattern;

    public Settings(MainApplication application) {
        super(application);
    }

    public static Settings fromApplication(Application application) {
        if (application instanceof MainApplication)
            return ((MainApplication) application).getSettings();
        throw new IllegalArgumentException("");
    }

    @Override
    protected void defaultValue(Context context) {
        captchaHideOnLocked = true;
        captchaOverrideDefaultAction = false;
        captchaUseDefaultPattern = true;
        captchaIdentifyPattern = "";
        captchaParsePattern = "";
    }

    @Override
    protected void readFromTrayPreferences(TrayPreferences preferences) {
        captchaHideOnLocked = preferences.getBoolean(SETTING_CAPTCHA_HIDE_ON_LOCKED, captchaHideOnLocked);
        captchaOverrideDefaultAction = preferences.getBoolean(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION, captchaOverrideDefaultAction);
        captchaUseDefaultPattern = preferences.getBoolean(SETTING_CAPTCHA_USE_DEFAULT_PATTERN, captchaUseDefaultPattern);
        captchaIdentifyPattern = preferences.getString(SETTING_CAPTCHA_IDENTIFY_PATTERN, captchaIdentifyPattern);
        captchaParsePattern = preferences.getString(SETTING_CAPTCHA_PARSE_PATTERN, captchaParsePattern);
    }

    @Override
    protected void onSettingsChanged(String key, String value) {
        switch (key) {
            case SETTING_CAPTCHA_HIDE_ON_LOCKED:
                this.captchaHideOnLocked = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION:
                this.captchaOverrideDefaultAction = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_USE_DEFAULT_PATTERN:
                this.captchaUseDefaultPattern = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_IDENTIFY_PATTERN:
                this.captchaIdentifyPattern = value;
                break;
            case SETTING_CAPTCHA_PARSE_PATTERN:
                this.captchaParsePattern = value;
                break;
        }
    }

    public boolean isCaptchaHideOnLocked() {
        return captchaHideOnLocked;
    }

    public void setCaptchaHideOnLocked(boolean captchaHideOnLocked) {
        getPreference().put(SETTING_CAPTCHA_HIDE_ON_LOCKED, captchaHideOnLocked);
    }

    public String getCaptchaIdentifyPattern() {
        return captchaIdentifyPattern;
    }

    public void setCaptchaIdentifyPattern(String captchaIdentifyPattern) {
        getPreference().put(SETTING_CAPTCHA_IDENTIFY_PATTERN, captchaIdentifyPattern);
    }

    public String getCaptchaParsePattern() {
        return captchaParsePattern;
    }

    public void setCaptchaParsePattern(String captchaParsePattern) {
        getPreference().put(SETTING_CAPTCHA_PARSE_PATTERN, captchaParsePattern);
    }

    public boolean isCaptchaOverrideDefaultAction() {
        return captchaOverrideDefaultAction;
    }

    public void setCaptchaOverrideDefaultAction(boolean captchaOverrideDefaultAction) {
        getPreference().put(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION, captchaOverrideDefaultAction);
    }

    public boolean isCaptchaUseDefaultPattern() {
        return captchaUseDefaultPattern;
    }

    public void setCaptchaUseDefaultPattern(boolean captchaUseDefaultPattern) {
        getPreference().put(SETTING_CAPTCHA_USE_DEFAULT_PATTERN, captchaUseDefaultPattern);
    }
}
