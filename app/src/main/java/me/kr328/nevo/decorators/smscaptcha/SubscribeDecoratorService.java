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
import android.os.Parcelable;
import android.os.UserManager;
import android.util.Log;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;
import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.TrayItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class SubscribeDecoratorService extends BaseSmsDecoratorService {
    public final static String TAG = SubscribeDecoratorService.class.getSimpleName();
    public final static String[] TARGET_PACKAGES = new String[]{"com.android.messaging", "com.google.android.apps.messaging", "com.android.mms" ,"com.sonyericsson.conversations"};

    public final static String NOTIFICATION_CHANNEL_SUBSCRIBE_DEFAULT = "notification_channel_subscribe_default";

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

        appendActions(notification ,evolving.getKey() ,new Notification.Action[0]);

        extras.putBoolean(Global.NOTIFICATION_EXTRA_APPLIED, true);

        Log.i(TAG, "Applied " + evolving.getKey());
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        loadSettings();
        createNotificationChannels();
    }

    @Override
    public void onUserUnlocked() {
        this.loadSettings();
    }

    @Override
    public void onActionClicked(String key ,Parcelable cookies) {

    }

    public void loadSettings() {
        if ( Objects.requireNonNull(getSystemService(UserManager.class)).isUserUnlocked() ) {
            AppPreferences mAppPreference = new AppPreferences(this);
            mSettings = Settings.defaultValueFromContext(this).readFromTrayPreference(mAppPreference);

            mAppPreference.registerOnTrayPreferenceChangeListener(this::onSettingsChanged);
        }
        else {
            mSettings = Settings.defaultValueFromContext(createDeviceProtectedStorageContext());
        }
    }

    private void onSettingsChanged(Collection<TrayItem> trayItems) {
        for (TrayItem item : trayItems) {
            switch (item.key()) {
                case Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN:
                    mSettings.setSubscribeIdentifyPattern(item.value());
                    break;
                case Settings.SETTING_SUBSCRIBE_PRIORITY:
                    mSettings.setSubscribePriority(Integer.parseInt(Objects.requireNonNull(item.value())));
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
