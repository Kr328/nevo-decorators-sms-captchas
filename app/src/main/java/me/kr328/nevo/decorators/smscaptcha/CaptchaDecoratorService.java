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
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserManager;
import android.service.notification.StatusBarNotification;
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
import java.util.regex.Pattern;

import me.kr328.nevo.decorators.smscaptcha.utils.CaptchaUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.MessageUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.PackageUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;

public class CaptchaDecoratorService extends BaseSmsDecoratorService {
    public static final String TAG = CaptchaDecoratorService.class.getSimpleName();
    public static final String[] TARGET_PACKAGES = new String[]{"com.android.messaging", "com.google.android.apps.messaging", "com.android.mms" ,"com.sonyericsson.conversations" ,"com.moez.QKSMS"};

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
        MutableNotification notification    = evolving.getNotification();
        Bundle              extras          = notification.extras;
        boolean             recast          = extras.getBoolean(NOTIFICATION_EXTRA_RECAST, false);
        NotificationUtils.Messages messages = NotificationUtils.parseMessages(notification);
        String[]            captchas        = mCaptchaUtils.findSmsCaptchas(messages.texts);

        if (captchas.length == 0)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notification.setChannelId(recast ? NOTIFICATION_CHANNEL_CAPTCHA_SILENT : NOTIFICATION_CHANNEL_CAPTCHA_NORMAL);
        else
            notification.priority = recast ? Notification.PRIORITY_LOW : Notification.PRIORITY_HIGH;

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (mSettings.isCaptchaHideOnLocked() && keyguardManager != null && keyguardManager.isKeyguardLocked())
            applyKeyguardLocked(notification, evolving.getKey(), messages, captchas);
        else
            applyKeyguardUnlocked(notification, evolving.getKey(), messages, captchas);

        notification.flags     |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.visibility = Notification.VISIBILITY_PUBLIC;

        extras.putBoolean(Global.NOTIFICATION_EXTRA_APPLIED, true);
        mAppliedKeys.add(evolving.getKey());

        if ( !recast )
            sendBroadcast(new Intent(Global.INTENT_CAPTCHA_NOTIFICATION_SHOW).
                putExtra(Global.INTENT_NOTIFICATION_KEY ,evolving.getKey()).
                putExtra(Global.INTENT_NOTIFICATION_CAPTCHA ,captchas[0]));

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    @Override
    protected void onNotificationRemoved(StatusBarNotification notification, int reason) {
        super.onNotificationRemoved(notification, reason);

        sendBroadcast(new Intent(Global.INTENT_CAPTCHA_NOTIFICATION_CANCEL).putExtra(Global.INTENT_NOTIFICATION_KEY ,notification.getKey()));
    }

    private void applyKeyguardLocked(Notification notification, String key, NotificationUtils.Messages messages , String[] captchas) {
        Notification.Action[] actions = new Notification.Action[] {
                createNonIconAction(key ,getString(R.string.captcha_service_notification_locked_action_copy_code) ,new CaptchaMessage(messages ,captchas[0]))
        };

        NotificationUtils.replaceMessages(notification ,text -> CaptchaUtils.replaceCaptchaWithChar(text ,captchas ,'*'));

        if ( mSettings.isCaptchaOverrideDefaultAction() )
            replaceActions(notification ,key ,actions);
        else
            appendActions(notification ,key ,actions);
    }

    private void applyKeyguardUnlocked(Notification notification, String key, NotificationUtils.Messages messages, String[] captchas) {
        Notification.Action[] actions = Arrays.stream(captchas).
                map(captcha -> createNonIconAction(key ,getString(R.string.captcha_service_notification_unlocked_action_copy_code_format ,captcha) ,new CaptchaMessage(messages ,captcha))).
                toArray(Notification.Action[]::new);

        NotificationUtils.rebuildMessageStyle(notification);

        if ( mSettings.isCaptchaOverrideDefaultAction() )
            replaceActions(notification ,key ,actions);
        else
            appendActions(notification ,key ,actions);
    }

    private void loadSettings() {
        if (Objects.requireNonNull(getSystemService(UserManager.class)).isUserUnlocked()) {
            AppPreferences mAppPreferences = new AppPreferences(this);
            mSettings = Settings.defaultValueFromContext(this).readFromTrayPreference(mAppPreferences);
            mAppPreferences.registerOnTrayPreferenceChangeListener(this::onSettingsChanged);
        } else {
            mSettings = Settings.defaultValueFromContext(createDeviceProtectedStorageContext());
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelNormal = new NotificationChannel(NOTIFICATION_CHANNEL_CAPTCHA_NORMAL, getString(R.string.captcha_service_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channelSilent = new NotificationChannel(NOTIFICATION_CHANNEL_CAPTCHA_SILENT, getString(R.string.captcha_service_notification_channel_name), NotificationManager.IMPORTANCE_LOW);

            ArrayList<NotificationChannel> notificationChannels = new ArrayList<>();
            notificationChannels.add(channelNormal);
            notificationChannels.add(channelSilent);

            for (String packageName : TARGET_PACKAGES)
                if (PackageUtils.hasPackageInstalled(this ,packageName))
                    createNotificationChannels(packageName, notificationChannels);
        }
    }

    private void initCaptchaUtils() {
        mCaptchaUtils = new CaptchaUtils(mSettings.isCaptchaUseDefaultPattern() ,
                PatternUtils.compilePattern(mSettings.getCaptchaIdentifyPattern(), "" ,Pattern.CASE_INSENSITIVE),
                PatternUtils.compilePattern(mSettings.getCaptchaParsePattern(), "" ,Pattern.CASE_INSENSITIVE));

        Log.d(TAG ,"CaptchaUtils " + mSettings.isCaptchaUseDefaultPattern() + " " + mSettings.getCaptchaIdentifyPattern() + " " + mSettings.getCaptchaParsePattern());
    }

    private void recastAllNotifications(Bundle fillInExtras) {
        for (String key : mAppliedKeys)
            recastNotification(key, fillInExtras);
        mAppliedKeys.clear();
    }

    private void copyCaptcha(String captcha , NotificationUtils.Messages messages) {
        ((ClipboardManager) Objects.requireNonNull(getSystemService(Context.CLIPBOARD_SERVICE))).
                setPrimaryClip(ClipData.newPlainText("SmsCaptcha", captcha));

        switch (mSettings.getCaptchaPostCopyAction()) {
            case Settings.POST_ACTION_DELETE :
                Arrays.stream(messages.texts).forEach(t -> MessageUtils.delete(this , t));
                break;
            case Settings.POST_ACTION_MARK_AS_READ :
                Arrays.stream(messages.texts).forEach(t -> MessageUtils.markAsRead(this , t));
                break;
        }

        Toast.makeText(this, getString(R.string.captcha_service_toast_copied_format, captcha), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserUnlocked() {
        this.loadSettings();
    }

    @Override
    public void onActionClicked(String key ,Parcelable cookies) {
        CaptchaMessage captchaMessage = (CaptchaMessage) cookies;

        if ( captchaMessage == null )
            return;

        copyCaptcha(captchaMessage.captcha ,captchaMessage.messages);
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
                    break;
                case Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN :
                    mSettings.setCaptchaUseDefaultPattern(Boolean.parseBoolean(item.value()));
                    initCaptchaUtils();
                    break;
                case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED :
                    mSettings.setCaptchaHideOnLocked(Boolean.parseBoolean(item.value()));
                    break;
                case Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION :
                    mSettings.setCaptchaOverrideDefaultAction(Boolean.parseBoolean(item.value()));
                    break;
                case Settings.SETTING_CAPTCHA_POST_COPY_ACTION :
                    mSettings.setCaptchaPostCopyAction(Integer.parseInt(Objects.requireNonNull(item.value())));
                    break;

            }

            Log.i(TAG ,"Settings Updated " + item.key() + "=" + item.value());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();
    }

    static class CaptchaMessage implements Parcelable {
        NotificationUtils.Messages messages;
        String                     captcha;

        CaptchaMessage(NotificationUtils.Messages messages, String captcha) {
            this.messages = messages;
            this.captcha  = captcha;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(messages.texts.length);

            for (CharSequence s : messages.texts) dest.writeString(s.toString());

            dest.writeString(captcha);
        }

        public static final Creator<CaptchaMessage> CREATOR = new Creator<CaptchaMessage>() {
            @Override
            public CaptchaMessage createFromParcel(Parcel source) {
                NotificationUtils.Messages messages = new NotificationUtils.Messages();
                messages.texts = new String[source.readInt()];

                for ( int i = 0 ; i < messages.texts.length ; i++ ) messages.texts[i] = source.readString();

                return new CaptchaMessage(messages ,source.readString());
            }

            @Override
            public CaptchaMessage[] newArray(int size) {
                return new CaptchaMessage[size];
            }
        };
    }
}

