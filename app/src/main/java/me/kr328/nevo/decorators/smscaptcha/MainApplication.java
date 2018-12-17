package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        settings = new Settings(this);
    }

    public Settings getSettings() {
        return settings;
    }

    private Settings settings;
}
