package me.kr328.nevo.decorators.smscaptcha;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.TrayItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import me.kr328.nevo.decorators.smscaptcha.utils.CaptchaUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;

public class CaptchaDecoratorService extends BaseSmsDecoratorService {
    public static final String TAG = CaptchaDecoratorService.class.getSimpleName();
    public static final String[] TARGET_PACKAGES = new String[]{"com.android.messaging", "com.google.android.apps.messaging", "com.android.mms"};

    public static final String NOTIFICATION_CHANNEL_CAPTCHA_NORMAL = "notification_channel_captcha_normal";
    public static final String NOTIFICATION_CHANNEL_CAPTCHA_SILENT = "notification_channel_captcha_silent";

    public static final String NOTIFICATION_EXTRA_RECAST = Global.PREFIX_NOTIFICATION_EXTRA + ".captcha.notification.recast";

    private Settings mSettings;
    private CaptchaUtils mCaptchaUtils;

    private TreeSet<String> mAppliedKeys = new TreeSet<>();

    private BroadcastReceiver mKeyguardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle fillBundle = new Bundle();
            fillBundle.putBoolean(NOTIFICATION_EXTRA_RECAST, true);

            recastAllNotifications(fillBundle);
        }
    };

    @Override
    protected void apply(MutableStatusBarNotification evolving) {
        MutableNotification notification = evolving.getNotification();
        Bundle              extras       = notification.extras;
        boolean             recast       = extras.getBoolean(NOTIFICATION_EXTRA_RECAST, false);
        CharSequence        message      = extras.getCharSequence(Notification.EXTRA_TEXT);
        String[]            captchas     = mCaptchaUtils.findSmsCaptchas(message);

        if (captchas.length == 0 || extras.getBoolean(Global.NOTIFICATION_EXTRA_APPLIED, false))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notification.setChannelId(recast ? NOTIFICATION_CHANNEL_CAPTCHA_SILENT : NOTIFICATION_CHANNEL_CAPTCHA_NORMAL);
        else
            notification.priority = recast ? Notification.PRIORITY_LOW : Notification.PRIORITY_HIGH;

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (mSettings.isCaptchaHideOnLocked() && keyguardManager != null && keyguardManager.isKeyguardLocked())
            applyKeyguardLocked(notification, evolving.getKey(), message, captchas);
        else
            applyKeyguardUnlocked(notification, evolving.getKey(), message, captchas);

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.visibility = Notification.VISIBILITY_PUBLIC;

        extras.putBoolean(Global.NOTIFICATION_EXTRA_APPLIED, true);
        mAppliedKeys.add(evolving.getKey());

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    private void applyKeyguardLocked(Notification notification, String key, CharSequence message ,String[] captchas) {
        Notification.Action[] actions = new Notification.Action[] {
                createNonIconAction(key ,getString(R.string.captcha_service_notification_locked_action_copy_code) ,(intent -> copyCaptcha(captchas[0])))
        };

        NotificationUtils.replaceMessages(notification ,text -> CaptchaUtils.replaceCaptchaWithChar(text ,captchas ,'*'));

        replaceActions(notification ,key ,actions);
    }

    private void applyKeyguardUnlocked(Notification notification, String key, CharSequence text, String[] captchas) {
        Notification.Action[] actions = Arrays.stream(captchas).
                map(captcha -> createNonIconAction(key ,getString(R.string.captcha_service_notification_unlocked_action_copy_code_format ,captcha) ,intent->copyCaptcha(captcha))).
                toArray(Notification.Action[]::new);

        NotificationUtils.rebuildMessageStyle(notification);

        replaceActions(notification ,key ,actions);
    }

    private void loadSettings() {
        AppPreferences mAppPreferences = new AppPreferences(this);
        mSettings = Settings.defaultValueFromContext(this).readFromTrayPreference(mAppPreferences);

        mAppPreferences.registerOnTrayPreferenceChangeListener(this::onSettingsChanged);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelNormal = new NotificationChannel(NOTIFICATION_CHANNEL_CAPTCHA_NORMAL, getString(R.string.captcha_service_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channelSilent = new NotificationChannel(NOTIFICATION_CHANNEL_CAPTCHA_SILENT, getString(R.string.captcha_service_notification_channel_name), NotificationManager.IMPORTANCE_LOW);

            ArrayList<NotificationChannel> notificationChannels = new ArrayList<>();
            notificationChannels.add(channelNormal);
            notificationChannels.add(channelSilent);

            for (String packageName : TARGET_PACKAGES)
                createNotificationChannels(packageName, notificationChannels);
        }
    }

    private void initCaptchaUtils() {
        mCaptchaUtils = new CaptchaUtils(
                PatternUtils.compilePattern(mSettings.getCaptchaIdentifyPattern(), getString(R.string.default_value_identify_captcha_pattern)),
                PatternUtils.compilePattern(mSettings.getCaptchaParsePattern(), getString(R.string.default_value_parse_captcha_pattern)));
    }

    private void recastAllNotifications(Bundle fillInExtras) {
        for (String key : mAppliedKeys)
            recastNotification(key, fillInExtras);
        mAppliedKeys.clear();
    }

    private void copyCaptcha(String captcha) {
        ((ClipboardManager) Objects.requireNonNull(getSystemService(Context.CLIPBOARD_SERVICE))).setPrimaryClip(ClipData.newPlainText("SmsCaptcha", captcha));
        Toast.makeText(this, getString(R.string.captcha_service_toast_copied_format, captcha), Toast.LENGTH_LONG).show();
    }

    private void registerReceivers() {
        registerReceiver(mKeyguardReceiver, new IntentFilter() {{
            addAction(Intent.ACTION_USER_PRESENT);
            addAction(Intent.ACTION_SCREEN_OFF);
        }});
    }

    private void unregisterReceivers() {
        unregisterReceiver(mKeyguardReceiver);
    }

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        super.onNotificationRemoved(key ,reason);
        mAppliedKeys.remove(key);
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        loadSettings();
        createNotificationChannels();
        initCaptchaUtils();
        registerReceivers();
    }

    private void onSettingsChanged(Collection<TrayItem> trayItems) {
        for (TrayItem item : trayItems) {
            switch (item.key()) {
                case Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN:
                    mSettings.setCaptchaIdentifyPattern(item.value());
                    initCaptchaUtils();
                    break;
                case Settings.SETTING_CAPTCHA_PARSE_PATTERN:
                    mSettings.setCaptchaParsePattern(item.value());
                    initCaptchaUtils();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();
    }
}
