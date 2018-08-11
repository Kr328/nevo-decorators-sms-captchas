package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastUtils {
    public BroadcastUtils(Context context) {

    }

    public void registerBroadcastListener() {

    }

    public interface BroadcastListener {
        void onReceiver(Intent intent);
    }
}
