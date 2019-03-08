package me.kr328.nevo.decorators.smscaptcha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserManager;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.TrayItem;

import java.util.Collection;
import java.util.Objects;

public abstract class SettingsBase {
    private MainApplication application;
    private AppPreferences preferences;

    SettingsBase(MainApplication application) {
        this.application = application;
        this.preferences = null;

        notifyLoadSettings();

        application.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyLoadSettings();
            }
        }, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
    }

    protected abstract void defaultValue(Context context);

    protected abstract void readFromTrayPreferences(TrayPreferences preferences);

    protected abstract void onSettingsChanged(String key, String value);

    protected TrayPreferences getPreference() {
        return preferences;
    }

    private void notifyLoadSettings() {
        defaultValue(application);

        if (Objects.requireNonNull(application.getSystemService(UserManager.class)).isUserUnlocked()) {
            initAppPreference();
            readFromTrayPreferences(preferences);
        } else {
            preferences = null;
        }
    }

    private synchronized void initAppPreference() {
        if (preferences != null)
            return;
        preferences = new AppPreferences(application);
        preferences.registerOnTrayPreferenceChangeListener(this::onChanged);
    }

    private void onChanged(Collection<TrayItem> items) {
        items.forEach((item) -> onSettingsChanged(item.key(), item.value()));
    }
}
