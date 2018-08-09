package me.kr328.nevo.decorators.smscaptcha;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;
import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.TrayItem;

import java.util.ArrayList;
import java.util.Collection;

public class SubscribeDecoratorService extends NevoDecoratorService {
    public final static String TAG = SubscribeDecoratorService.class.getSimpleName();
    public final static String[] TARGET_PACKAGES = new String[]{"com.android.messaging", "com.google.android.apps.messaging", "com.android.mms"};

    public final static String INTENT_ACTION_SEND_PENDING_INTENT_CANCEL = Global.PREFIX_INTENT_ACTION + ".send.and.cancel";
    public final static String INTENT_EXTRA_ORIGINAL_PENDING_INTENT = Global.PREFIX_INTENT_EXTRA + ".original.intent";
    public final static String NOTIFICATION_EXTRA_KEY = Global.PREFIX_NOTIFICATION_EXTRA + ".key";

    public final static String NOTIFICATION_CHANNEL_SUBSCRIBE_DEFAULT = "notification_channel_subscribe_default";
    private BroadcastReceiver mSendIntentAndCancelNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra(NOTIFICATION_EXTRA_KEY);
            PendingIntent originalIntent = intent.getParcelableExtra(INTENT_EXTRA_ORIGINAL_PENDING_INTENT);

            try {
                originalIntent.send(SubscribeDecoratorService.this ,0 ,intent.setPackage(originalIntent.getCreatorPackage()));
            } catch (PendingIntent.CanceledException e) {
                Log.i(TAG, "PendingIntent.send() ", e);
            }

            cancelNotification(key);
        }
    };
    private AppPreferences mAppPreference;
    private Settings mSettings;

    @Override
    protected void apply(MutableStatusBarNotification evolving) {
        MutableNotification notification = evolving.getNotification();
        Bundle extras = notification.extras;
        CharSequence message = extras.getCharSequence(Notification.EXTRA_TEXT);

        if (message == null || extras.getBoolean(Global.NOTIFICATION_EXTRA_APPLIED, false) || !message.toString().matches(mSettings.getSubscribeIdentifyPattern()))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notification.setChannelId(NOTIFICATION_CHANNEL_SUBSCRIBE_DEFAULT);
        else
            notification.priority = mSettings.getSubscribePriority();

        for (Notification.Action action : notification.actions) {
            PendingIntent originalIntent = action.actionIntent;
            Intent newIntent = new Intent().setAction(INTENT_ACTION_SEND_PENDING_INTENT_CANCEL).putExtra(NOTIFICATION_EXTRA_KEY, evolving.getKey()).putExtra(INTENT_EXTRA_ORIGINAL_PENDING_INTENT, originalIntent);

            action.actionIntent = PendingIntent.getBroadcast(this, originalIntent.hashCode(), newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        extras.putBoolean(Global.NOTIFICATION_EXTRA_APPLIED, true);

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    @Override
    protected void onConnected() {
        createNotificationChannels();

        mAppPreference = new AppPreferences(this);
        mSettings = Settings.defaultValueFromContext(this).readFromTrayPreference(mAppPreference);

        mAppPreference.registerOnTrayPreferenceChangeListener(this::onSettingsChanged);
        registerReceiver(mSendIntentAndCancelNotificationReceiver, new IntentFilter(INTENT_ACTION_SEND_PENDING_INTENT_CANCEL));
    }

    private void onSettingsChanged(Collection<TrayItem> trayItems) {
        for (TrayItem item : trayItems) {
            switch (item.key()) {
                case Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN:
                    mSettings.setSubscribeIdentifyPattern(item.value());
                    break;
                case Settings.SETTING_SUBSCRIBE_PRIORITY:
                    mSettings.setSubscribePriority(Integer.parseInt(item.value()));
                    break;
            }
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_SUBSCRIBE_DEFAULT, getString(R.string.subscribe_service_notification_channel_name), NotificationManager.IMPORTANCE_MIN);

        ArrayList<NotificationChannel> notificationChannels = new ArrayList<>();
        notificationChannels.add(channel);

        for (String packageName : TARGET_PACKAGES)
            createNotificationChannels(packageName, notificationChannels);
    }
}
