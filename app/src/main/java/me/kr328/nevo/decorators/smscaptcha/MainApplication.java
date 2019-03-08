package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;

public class MainApplication extends Application {
    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();

        settings = new Settings(this);
    }

    public Settings getSettings() {
        return settings;
    }
}
