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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;
import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import java.util.ArrayList;
import java.util.TreeSet;

public class SmsCaptchaDecoratorService extends NevoDecoratorService {
    public static final String   TAG = SmsCaptchaDecoratorService.class.getSimpleName();
    public static final String[] TARGET_PACKAGES = new String[] {"com.android.messaging" ,"com.google.android.apps.messaging" ,"com.android.mms"};

    public static final String   INTENT_EXTRA_ACTION_COPY_CODE = TAG + ".notification.action.copy.code";
    public static final String   INTENT_EXTRA_NOTIFICATION_KEY = TAG + ".notification.key";
    public static final String   INTENT_EXTRA_COPY_CODE_INTENT_ACTION = TAG + ".action.copy.code";
    public static final int      INTENT_EXTRA_COPY_CODE_INTENT_ACTION_ID_BASE = 233;

    public static final String   NOTIFICATION_CHANNEL_NORMAL = "apply_notification_channel_normal";
    public static final String   NOTIFICATION_CHANNEL_SILENT = "apply_notification_channel_silent";

    public static final String   NOTIFICATION_EXTRA_RECAST = TAG + ".notification.recast";

    @Override
    protected void apply(MutableStatusBarNotification evolving) {
        MutableNotification notification = evolving.getNotification();
        Bundle extras = notification.extras;
        boolean recast = extras.getBoolean(NOTIFICATION_EXTRA_RECAST);
        CharSequence message = extras.getCharSequence(Notification.EXTRA_TEXT);
        String[] captchas = Utils.findSmsCaptchas(message);

        if ( captchas.length == 0 ) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notification.setChannelId(recast ? NOTIFICATION_CHANNEL_SILENT : NOTIFICATION_CHANNEL_NORMAL);
        else {
            if (recast) {
                notification.priority = Notification.PRIORITY_LOW;
                notification.sound = Uri.EMPTY;
            }
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if ( keyguardManager != null && keyguardManager.isKeyguardLocked() )
            applyKeyguardLocked(notification ,evolving.getKey() ,message ,captchas);
        else
            applyKeyguardUnlocked(notification ,evolving.getKey() ,message ,captchas);

        notification.publicVersion = null;
        notification.visibility = Notification.VISIBILITY_PUBLIC;

        appliedKeys.add(evolving.getKey());

        Log.i(TAG ,"Applied " + evolving.getKey());
    }

    private void applyKeyguardLocked(Notification notification , String key , CharSequence message , String[] captchas) {
        for ( String captcha : captchas ) {
            message = message.toString().replace(captcha , Utils.repeat('*' ,captcha.length()));
        }

        notification.extras.remove(Notification.EXTRA_TEMPLATE);
        notification.extras.putCharSequence(Notification.EXTRA_TEXT ,message);

        Notification.Action[] actions = new Notification.Action[1];
        String captcha = captchas[0];
        Icon icon = Icon.createWithResource(this, R.drawable.ic_notification_action_copy);
        Intent intent = new Intent(INTENT_EXTRA_COPY_CODE_INTENT_ACTION).putExtra(INTENT_EXTRA_ACTION_COPY_CODE, captcha).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, INTENT_EXTRA_COPY_CODE_INTENT_ACTION_ID_BASE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        actions[0] = new Notification.Action.Builder(icon, getString(R.string.main_service_notification_locked_action_copy_code), pendingIntent).build();

        notification.actions = actions;
    }

    private void applyKeyguardUnlocked(Notification notification , String key , CharSequence message , String[] captchas) {
        Notification.Action[] actions = new Notification.Action[captchas.length];

        for ( int i = 0 ; i < captchas.length ; i++ ) {
            String captcha = captchas[i];
            Icon icon = Icon.createWithResource(this, R.drawable.ic_notification_action_copy);
            Intent intent = new Intent(INTENT_EXTRA_COPY_CODE_INTENT_ACTION).putExtra(INTENT_EXTRA_ACTION_COPY_CODE, captcha).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, INTENT_EXTRA_COPY_CODE_INTENT_ACTION_ID_BASE + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            actions[i] = new Notification.Action.Builder(icon, getString(R.string.main_service_notification_unlocked_action_copy_code_format,captcha), pendingIntent).build();
        }

        notification.actions = actions;
        notification.extras.remove(Notification.EXTRA_TEMPLATE);
    }

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        appliedKeys.remove(key);
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            NotificationChannel channelNormal = new NotificationChannel(NOTIFICATION_CHANNEL_NORMAL ,getString(R.string.main_service_notification_channel_name) ,NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channelSilent = new NotificationChannel(NOTIFICATION_CHANNEL_SILENT ,getString(R.string.main_service_notification_channel_name) ,NotificationManager.IMPORTANCE_LOW);

            ArrayList<NotificationChannel> notificationChannels = new ArrayList<>();
            notificationChannels.add(channelNormal);
            notificationChannels.add(channelSilent);

            for ( String packageName : TARGET_PACKAGES )
                createNotificationChannels(packageName ,notificationChannels);
        }

        registerReceiver(mCopyCaptchaReceiver, new IntentFilter(INTENT_EXTRA_COPY_CODE_INTENT_ACTION));
        registerReceiver(mKeyguardLockedReceiver ,new IntentFilter(){{addAction(Intent.ACTION_USER_PRESENT);addAction(Intent.ACTION_SCREEN_OFF);}});
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mCopyCaptchaReceiver);
    }

    private BroadcastReceiver mCopyCaptchaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                String code = intent.getStringExtra(INTENT_EXTRA_ACTION_COPY_CODE);
                String key  = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);

                clipboardManager.setPrimaryClip(ClipData.newPlainText("SmsCaptcha", code));
                Toast.makeText(context ,getString(R.string.main_service_toast_copied_format ,code) ,Toast.LENGTH_LONG).show();
                cancelNotification(key);
            }
        }
    };

    private BroadcastReceiver mKeyguardLockedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle fillBundle = new Bundle();
            fillBundle.putBoolean(NOTIFICATION_EXTRA_RECAST ,true);

            for ( String key : appliedKeys )
                recastNotification(key ,fillBundle);
            Log.i(TAG ,intent.getAction());
        }
    };

    private TreeSet<String> appliedKeys = new TreeSet<>();
}
