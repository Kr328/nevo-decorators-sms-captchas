package me.kr328.nevo.decorators.smscaptcha

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SettingsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startActivity(Intent(context, SettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}