package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;

import java.util.ArrayList;
import java.util.HashSet;

public class Settings extends SettingsBase {
    public final static String SETTING_CAPTCHA_HIDE_ON_LOCKED            = "setting_captcha_hide_on_locked";
    public final static String SETTING_CAPTCHA_IDENTIFY_PATTERN          = "setting_captcha_identify_pattern";
    public final static String SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION   = "setting_captcha_override_default_action";
    public final static String SETTING_CAPTCHA_POST_COPY_ACTION          = "setting_captcha_post_copy_action";
    public final static String SETTING_CAPTCHA_USE_DEFAULT_PATTERN       = "setting_captcha_use_default_pattern";
    public final static String SETTING_CAPTCHA_PARSE_PATTERN             = "setting_captcha_parse_pattern";
    public final static String SETTING_SUBSCRIBE_IDENTIFY_PATTERN        = "setting_subscribe_identify_pattern";
    public final static String SETTING_SUBSCRIBE_PRIORITY                = "setting_subscribe_priority";

    public final static int POST_ACTION_NONE         = 0;
    public final static int POST_ACTION_MARK_AS_READ = 1;
    public final static int POST_ACTION_DELETE       = 2;

    private boolean captchaHideOnLocked;
    private boolean captchaOverrideDefaultAction;
    private int     captchaPostCopyAction;
    private boolean captchaUseDefaultPattern;
    private String  captchaIdentifyPattern;
    private String  captchaParsePattern;
    private String  subscribeIdentifyPattern;
    private int     subscribePriority;

    public Settings(MainApplication application) {
        super(application);
    }

    public static Settings fromApplication(Application application) {
        if ( application instanceof MainApplication )
            return ((MainApplication) application).getSettings();
        throw new IllegalArgumentException("");
    }

    @Override
    protected void defaultValue(Context context) {
        captchaHideOnLocked = true;
        captchaOverrideDefaultAction = false;
        captchaPostCopyAction = POST_ACTION_NONE;
        captchaUseDefaultPattern = true;
        captchaIdentifyPattern = "";
        captchaParsePattern = "";
        subscribeIdentifyPattern = context.getString(R.string.default_value_identify_subscribe_pattern);
        subscribePriority = Notification.PRIORITY_MIN;
    }

    @Override
    protected void readFromTrayPreferences(TrayPreferences preferences) {
        captchaHideOnLocked          = preferences.getBoolean(SETTING_CAPTCHA_HIDE_ON_LOCKED, captchaHideOnLocked);
        captchaOverrideDefaultAction = preferences.getBoolean(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION ,captchaOverrideDefaultAction);
        captchaPostCopyAction        = preferences.getInt(SETTING_CAPTCHA_POST_COPY_ACTION ,captchaPostCopyAction);
        captchaUseDefaultPattern     = preferences.getBoolean(SETTING_CAPTCHA_USE_DEFAULT_PATTERN ,captchaUseDefaultPattern);
        captchaIdentifyPattern       = preferences.getString(SETTING_CAPTCHA_IDENTIFY_PATTERN, captchaIdentifyPattern);
        captchaParsePattern          = preferences.getString(SETTING_CAPTCHA_PARSE_PATTERN, captchaParsePattern);
        subscribeIdentifyPattern     = preferences.getString(SETTING_SUBSCRIBE_IDENTIFY_PATTERN, subscribeIdentifyPattern);
        subscribePriority            = preferences.getInt(SETTING_SUBSCRIBE_PRIORITY, subscribePriority);
    }

    @Override
    protected void onSettingsChanged(String key, String value) {
        switch (key) {
            case SETTING_CAPTCHA_HIDE_ON_LOCKED :
                this.captchaHideOnLocked = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION :
                this.captchaOverrideDefaultAction = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_POST_COPY_ACTION :
                this.captchaPostCopyAction = Integer.valueOf(value);
                break;
            case SETTING_CAPTCHA_USE_DEFAULT_PATTERN :
                this.captchaUseDefaultPattern = Boolean.valueOf(value);
                break;
            case SETTING_CAPTCHA_IDENTIFY_PATTERN :
                this.captchaIdentifyPattern = value;
                break;
            case SETTING_CAPTCHA_PARSE_PATTERN :
                this.captchaParsePattern = value;
                break;
            case SETTING_SUBSCRIBE_IDENTIFY_PATTERN :
                this.subscribeIdentifyPattern = value;
                break;
            case SETTING_SUBSCRIBE_PRIORITY :
                this.subscribePriority = Integer.valueOf(value);
                break;
        }
    }

    public boolean isCaptchaHideOnLocked() {
        return captchaHideOnLocked;
    }

    public void setCaptchaHideOnLocked(boolean captchaHideOnLocked) {
        getPreference().put(SETTING_CAPTCHA_HIDE_ON_LOCKED ,captchaHideOnLocked);
    }

    public String getCaptchaIdentifyPattern() {
        return captchaIdentifyPattern;
    }

    public void setCaptchaIdentifyPattern(String captchaIdentifyPattern) {
        getPreference().put(SETTING_CAPTCHA_IDENTIFY_PATTERN ,captchaIdentifyPattern);
    }

    public String getCaptchaParsePattern() {
        return captchaParsePattern;
    }

    public void setCaptchaParsePattern(String captchaParsePattern) {
        getPreference().put(SETTING_CAPTCHA_PARSE_PATTERN ,captchaParsePattern);
    }

    public String getSubscribeIdentifyPattern() {
        return subscribeIdentifyPattern;
    }

    public void setSubscribeIdentifyPattern(String subscribeIdentifyPattern) {
        getPreference().put(SETTING_SUBSCRIBE_IDENTIFY_PATTERN ,subscribeIdentifyPattern);
    }

    public int getSubscribePriority() {
        return subscribePriority;
    }

    public void setSubscribePriority(int subscribePriority) {
        getPreference().put(SETTING_SUBSCRIBE_PRIORITY ,subscribePriority);
    }

    public boolean isCaptchaOverrideDefaultAction() {
        return captchaOverrideDefaultAction;
    }

    public void setCaptchaOverrideDefaultAction(boolean captchaOverrideDefaultAction) {
        getPreference().put(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION ,captchaOverrideDefaultAction);
    }

    public int getCaptchaPostCopyAction() {
        return captchaPostCopyAction;
    }

    public void setCaptchaPostCopyAction(int captchaPostCopyAction) {
        getPreference().put(SETTING_CAPTCHA_POST_COPY_ACTION ,captchaPostCopyAction);
    }

    public boolean isCaptchaUseDefaultPattern() {
        return captchaUseDefaultPattern;
    }

    public void setCaptchaUseDefaultPattern(boolean captchaUseDefaultPattern) {
        getPreference().put(SETTING_CAPTCHA_USE_DEFAULT_PATTERN ,captchaUseDefaultPattern);
    }
}
