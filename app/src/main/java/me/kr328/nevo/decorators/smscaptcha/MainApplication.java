package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainApplication extends Application {
    private BroadcastReceiver userUnlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();


    }

    public Settings getSettings() {
        return settings;
    }

    private Settings settings;
}
