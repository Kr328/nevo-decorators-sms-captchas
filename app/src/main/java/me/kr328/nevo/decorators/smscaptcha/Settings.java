package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;

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

    @Override
    protected void defaultValue(Context context) {

    }

    @Override
    protected void readFromTrayPreferences(TrayPreferences preferences) {
        setCaptchaHideOnLocked(preferences.getBoolean(SETTING_CAPTCHA_HIDE_ON_LOCKED, isCaptchaHideOnLocked()));
        setCaptchaOverrideDefaultAction(preferences.getBoolean(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION ,isCaptchaOverrideDefaultAction()));
        setCaptchaPostCopyAction(preferences.getInt(SETTING_CAPTCHA_POST_COPY_ACTION ,getCaptchaPostCopyAction()));
        setCaptchaUseDefaultPattern(preferences.getBoolean(SETTING_CAPTCHA_USE_DEFAULT_PATTERN ,true));
        setCaptchaIdentifyPattern(preferences.getString(SETTING_CAPTCHA_IDENTIFY_PATTERN, getCaptchaIdentifyPattern()));
        setCaptchaParsePattern(preferences.getString(SETTING_CAPTCHA_PARSE_PATTERN, getCaptchaParsePattern()));
        setSubscribeIdentifyPattern(preferences.getString(SETTING_SUBSCRIBE_IDENTIFY_PATTERN, getSubscribeIdentifyPattern()));
        setSubscribePriority(preferences.getInt(SETTING_SUBSCRIBE_PRIORITY, getSubscribePriority()));
    }

    @Override
    protected void onSettingsChanged(String key, String value) {

    }

    public boolean isCaptchaHideOnLocked() {
        return captchaHideOnLocked;
    }

    public void setCaptchaHideOnLocked(boolean captchaHideOnLocked) {
        this.captchaHideOnLocked = captchaHideOnLocked;
    }

    public String getCaptchaIdentifyPattern() {
        return captchaIdentifyPattern;
    }

    public void setCaptchaIdentifyPattern(String captchaIdentifyPattern) {
        this.captchaIdentifyPattern = captchaIdentifyPattern;
    }

    public String getCaptchaParsePattern() {
        return captchaParsePattern;
    }

    public void setCaptchaParsePattern(String captchaParsePattern) {
        this.captchaParsePattern = captchaParsePattern;
    }

    public String getSubscribeIdentifyPattern() {
        return subscribeIdentifyPattern;
    }

    public void setSubscribeIdentifyPattern(String subscribeIdentifyPattern) {
        this.subscribeIdentifyPattern = subscribeIdentifyPattern;
    }

    public int getSubscribePriority() {
        return subscribePriority;
    }

    public void setSubscribePriority(int subscribePriority) {
        this.subscribePriority = subscribePriority;
    }

    public boolean isCaptchaOverrideDefaultAction() {
        return captchaOverrideDefaultAction;
    }

    public void setCaptchaOverrideDefaultAction(boolean captchaOverrideDefaultAction) {
        this.captchaOverrideDefaultAction = captchaOverrideDefaultAction;
    }

    public int getCaptchaPostCopyAction() {
        return captchaPostCopyAction;
    }

    public void setCaptchaPostCopyAction(int captchaPostCopyAction) {
        this.captchaPostCopyAction = captchaPostCopyAction;
    }

    public boolean isCaptchaUseDefaultPattern() {
        return captchaUseDefaultPattern;
    }

    public void setCaptchaUseDefaultPattern(boolean captchaUseDefaultPattern) {
        this.captchaUseDefaultPattern = captchaUseDefaultPattern;
    }
}
