package me.kr328.nevo.decorators.smscaptcha;

import android.app.KeyguardManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;

import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import me.kr328.nevo.decorators.smscaptcha.utils.CaptchaUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;

public class CaptchaDecoratorService extends BaseSmsDecoratorService {
    public static final String TAG = CaptchaDecoratorService.class.getSimpleName();

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

    private BroadcastReceiver mCancelNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra(Global.INTENT_NOTIFICATION_KEY);

            cancelNotification(key);
        }
    };

    @Override
    protected void apply(MutableStatusBarNotification evolving) {
        MutableNotification notification = evolving.getNotification();
        Bundle extras = notification.extras;
        boolean recast = extras.getBoolean(NOTIFICATION_EXTRA_RECAST, false);
        NotificationUtils.Messages messages = NotificationUtils.parseMessages(notification);
        String[] captchas = mCaptchaUtils.findSmsCaptchas(messages.texts);

        Log.i(TAG, "apply begin");

        Stream.of(messages).forEach((msg) -> Stream.of(msg.texts).forEach((c) -> Log.i(TAG, "Message " + c)));

        if (captchas.length == 0) {
            Log.i(TAG, "Captcha not found.");
            return;
        }

        if (mSettings.isCaptchaHideOnLocked()) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null && keyguardManager.isKeyguardLocked())
                applyKeyguardLocked(notification, evolving.getKey(), messages, captchas);
            else
                applyKeyguardUnlocked(notification, evolving.getKey(), messages, captchas);
            notification.visibility = Notification.VISIBILITY_PUBLIC;
        } else {
            applyKeyguardLocked(notification, evolving.getKey(), messages, captchas);
        }

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        mAppliedKeys.add(evolving.getKey());

        if (!recast)
            sendBroadcast(new Intent(Global.INTENT_CAPTCHA_NOTIFICATION_SHOW).
                    putExtra(Global.INTENT_NOTIFICATION_KEY, evolving.getKey()).
                    putExtra(Global.INTENT_NOTIFICATION_CAPTCHA, captchas[0]));

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    private void applyKeyguardLocked(Notification notification, String key, NotificationUtils.Messages messages, String[] captchas) {
        Notification.Action[] actions = new Notification.Action[]{
                createNonIconAction(key, getString(R.string.captcha_service_notification_locked_action_copy_code), new CaptchaMessage(messages, captchas[0]))
        };

        NotificationUtils.replaceMessages(notification, text -> CaptchaUtils.replaceCaptchaWithChar(text, captchas, '*'));

        if (mSettings.isCaptchaOverrideDefaultAction())
            replaceActions(notification, key, actions);
        else
            appendActions(notification, key, actions);
    }

    private void applyKeyguardUnlocked(Notification notification, String key, NotificationUtils.Messages messages, String[] captchas) {
        Notification.Action[] actions = Arrays.stream(captchas).
                map(captcha -> createNonIconAction(key, getString(R.string.captcha_service_notification_unlocked_action_copy_code_format, captcha), new CaptchaMessage(messages, captcha))).
                toArray(Notification.Action[]::new);

        if (mSettings.isCaptchaOverrideDefaultAction())
            replaceActions(notification, key, actions);
        else
            appendActions(notification, key, actions);
    }

    private void loadSettings() {
        mSettings = Settings.fromApplication(getApplication());
    }

    private void initCaptchaUtils() {
        mCaptchaUtils = new CaptchaUtils(mSettings.isCaptchaUseDefaultPattern(),
                PatternUtils.compilePattern(mSettings.getCaptchaIdentifyPattern(), "", Pattern.CASE_INSENSITIVE),
                PatternUtils.compilePattern(mSettings.getCaptchaParsePattern(), "", Pattern.CASE_INSENSITIVE));

        Log.d(TAG, "CaptchaUtils " + mSettings.isCaptchaUseDefaultPattern() + " " + mSettings.getCaptchaIdentifyPattern() + " " + mSettings.getCaptchaParsePattern());
    }

    private void recastAllNotifications(Bundle fillInExtras) {
        for (String key : mAppliedKeys)
            recastNotification(key, fillInExtras);
        mAppliedKeys.clear();
    }

    private void copyCaptcha(String captcha, NotificationUtils.Messages messages) {
        ((ClipboardManager) Objects.requireNonNull(getSystemService(Context.CLIPBOARD_SERVICE))).
                setPrimaryClip(ClipData.newPlainText("SmsCaptcha", captcha));

        Toast.makeText(this, getString(R.string.captcha_service_toast_copied_format, captcha), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActionClicked(String key, Parcelable cookies) {
        CaptchaMessage captchaMessage = (CaptchaMessage) cookies;

        if (captchaMessage == null)
            return;

        copyCaptcha(captchaMessage.captcha, captchaMessage.messages);
    }

    private void registerReceivers() {
        registerReceiver(mKeyguardReceiver, new IntentFilter() {{
            addAction(Intent.ACTION_USER_PRESENT);
            addAction(Intent.ACTION_SCREEN_OFF);
        }});
        registerReceiver(mCancelNotificationReceiver, new IntentFilter(Global.INTENT_CAPTCHA_NOTIFICATION_DO_CANCEL));
    }

    private void unregisterReceivers() {
        unregisterReceiver(mKeyguardReceiver);
        unregisterReceiver(mCancelNotificationReceiver);
    }

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        super.onNotificationRemoved(key, reason);

        sendBroadcast(new Intent(Global.INTENT_CAPTCHA_NOTIFICATION_CANCEL).putExtra(Global.INTENT_NOTIFICATION_KEY, key));

        mAppliedKeys.remove(key);
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        loadSettings();
        initCaptchaUtils();
        registerReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();
    }

    static class CaptchaMessage implements Parcelable {
        public static final Creator<CaptchaMessage> CREATOR = new Creator<CaptchaMessage>() {
            @Override
            public CaptchaMessage createFromParcel(Parcel source) {
                NotificationUtils.Messages messages = new NotificationUtils.Messages();
                messages.texts = new String[source.readInt()];

                for (int i = 0; i < messages.texts.length; i++)
                    messages.texts[i] = source.readString();

                return new CaptchaMessage(messages, source.readString());
            }

            @Override
            public CaptchaMessage[] newArray(int size) {
                return new CaptchaMessage[size];
            }
        };
        NotificationUtils.Messages messages;
        String captcha;

        CaptchaMessage(NotificationUtils.Messages messages, String captcha) {
            this.messages = messages;
            this.captcha = captcha;
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
    }
}

