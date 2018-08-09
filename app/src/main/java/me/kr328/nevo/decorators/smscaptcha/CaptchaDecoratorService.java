package me.kr328.nevo.decorators.smscaptcha;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;
import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.TrayItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import me.kr328.nevo.decorators.smscaptcha.utils.CaptchaUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;

public class CaptchaDecoratorService extends NevoDecoratorService {
    public static final String TAG = CaptchaDecoratorService.class.getSimpleName();
    public static final String[] TARGET_PACKAGES = new String[]{"com.android.messaging", "com.google.android.apps.messaging", "com.android.mms"};

    public static final String INTENT_ACTION_COPY_CAPTCHA = Global.PREFIX_INTENT_ACTION + ".captcha.copy";
    public static final String INTENT_EXTRA_NOTIFICATION_KEY = Global.PREFIX_INTENT_EXTRA + ".captcha.notification.key";
    public static final String INTENT_EXTRA_CAPTCHA = Global.PREFIX_INTENT_EXTRA + ".captcha.value";

    public static final String NOTIFICATION_CHANNEL_CAPTCHA_NORMAL = "notification_channel_captcha_normal";
    public static final String NOTIFICATION_CHANNEL_CAPTCHA_SILENT = "notification_channel_captcha_silent";

    public static final String NOTIFICATION_EXTRA_RECAST = Global.PREFIX_NOTIFICATION_EXTRA + ".captcha.notification.recast";

    private Settings mSettings;
    private CaptchaUtils mCaptchaUtils;
    private BroadcastReceiver mCopyCaptchaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                String code = intent.getStringExtra(INTENT_EXTRA_CAPTCHA);
                String key = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);

                clipboardManager.setPrimaryClip(ClipData.newPlainText("SmsCaptcha", code));
                Toast.makeText(context, getString(R.string.captcha_service_toast_copied_format, code), Toast.LENGTH_LONG).show();
                cancelNotification(key);
            }
        }
    };
    private TreeSet<String> appliedKeys = new TreeSet<>();
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
        Bundle extras = notification.extras;
        boolean recast = extras.getBoolean(NOTIFICATION_EXTRA_RECAST, false);
        CharSequence message = extras.getCharSequence(Notification.EXTRA_TEXT);
        String[] captchas = mCaptchaUtils.findSmsCaptchas(message);

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
        appliedKeys.add(evolving.getKey());

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    private void applyKeyguardLocked(Notification notification, String key, CharSequence text, String[] captchas) {
        Notification.Action[] actions = new Notification.Action[1];
        String captcha = captchas[0];
        Icon icon = Icon.createWithResource(this, R.drawable.ic_notification_action_copy);
        Intent intent = new Intent(INTENT_ACTION_COPY_CAPTCHA).putExtra(INTENT_EXTRA_CAPTCHA, captcha).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        actions[0] = new Notification.Action.Builder(icon, getString(R.string.captcha_service_notification_locked_action_copy_code), pendingIntent).build();

        NotificationCompat.MessagingStyle originalStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        NotificationCompat.MessagingStyle appliedStyle  = null;
        if ( originalStyle != null ) {
            appliedStyle = new NotificationCompat.MessagingStyle(originalStyle.getUser());
            appliedStyle.setConversationTitle(originalStyle.getConversationTitle());
            appliedStyle.setGroupConversation(originalStyle.isGroupConversation());

            for ( NotificationCompat.MessagingStyle.Message message : originalStyle.getMessages() )
                appliedStyle.addMessage(CaptchaUtils.replaceCaptchaWithChar(message.getText(), captchas, '*') ,message.getTimestamp() ,message.getPerson());

            appliedStyle.addCompatExtras(notification.extras);
        }
        else {
            notification.extras.remove(Notification.EXTRA_TEMPLATE);
            notification.extras.putCharSequence(Notification.EXTRA_TEXT, CaptchaUtils.replaceCaptchaWithChar(text ,captchas ,'*'));
        }

        notification.actions = actions;

        Log.i(TAG , "originalStyle == null: " + String.valueOf(originalStyle == null));
    }

    private void applyKeyguardUnlocked(Notification notification, String key, CharSequence message, String[] captchas) {
        Notification.Action[] actions = new Notification.Action[captchas.length];

        for (int i = 0; i < captchas.length; i++) {
            String captcha = captchas[i];
            Icon icon = Icon.createWithResource(this, R.drawable.ic_notification_action_copy);
            Intent intent = new Intent(INTENT_ACTION_COPY_CAPTCHA).putExtra(INTENT_EXTRA_CAPTCHA, captcha).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (key + i).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            actions[i] = new Notification.Action.Builder(icon, getString(R.string.captcha_service_notification_unlocked_action_copy_code_format, captcha), pendingIntent).build();
        }

        notification.actions = actions;
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
        for (String key : appliedKeys)
            recastNotification(key, fillInExtras);
        appliedKeys.clear();
    }

    private void registerReceivers() {
        registerReceiver(mKeyguardReceiver, new IntentFilter() {{
            addAction(Intent.ACTION_USER_PRESENT);
            addAction(Intent.ACTION_SCREEN_OFF);
        }});
        registerReceiver(mCopyCaptchaReceiver, new IntentFilter(INTENT_ACTION_COPY_CAPTCHA));
    }

    private void unregisterReceivers() {
        unregisterReceiver(mCopyCaptchaReceiver);
        unregisterReceiver(mKeyguardReceiver);
    }

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        appliedKeys.remove(key);
    }

    @Override
    protected void onConnected() {
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
