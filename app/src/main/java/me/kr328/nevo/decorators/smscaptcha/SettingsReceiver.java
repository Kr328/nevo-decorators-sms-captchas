package me.kr328.nevo.decorators.smscaptcha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SettingsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startActivity(new Intent(context ,SettingsActivity.class));
    }
}
