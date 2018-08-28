package me.kr328.nevo.decorators.smscaptcha;

import android.app.Application;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        this.moveDatabaseFrom(createDeviceProtectedStorageContext() ,"tray");
    }
}
