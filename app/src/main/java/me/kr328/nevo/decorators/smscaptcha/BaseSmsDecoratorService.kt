package me.kr328.nevo.decorators.smscaptcha

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Parcelable
import android.util.Log
import com.oasisfeng.nevo.sdk.NevoDecoratorService
import me.kr328.nevo.decorators.smscaptcha.compat.cancelNotificationCompat

abstract class BaseSmsDecoratorService : NevoDecoratorService() {
    private val mOnActionClickedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val key = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY) ?: return
            val cookies = intent.getParcelableExtra<Parcelable>(INTENT_EXTRA_COOKIES)

            onActionClicked(key, cookies)

            cancelNotificationCompat(key)
        }
    }
    private val mActionProxyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val originalIntent = intent
                    .getParcelableExtra<PendingIntent>(INTENT_EXTRA_PROXY_INTENT) ?: return
            val key = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY) ?: return
            try {
                originalIntent.send(context, 0, intent.setPackage(originalIntent.creatorPackage))

                cancelNotificationCompat(key)
            } catch (e: CanceledException) {
                Log.e(Constants.TAG, "Proxy failure.", e)
            }
        }
    }
    private val mUserUnlockedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onUserUnlocked()
        }
    }

    override fun onConnected() {
        super.onConnected()

        registerReceiver(mOnActionClickedReceiver, IntentFilter(INTENT_ACTION_CLICKED_ACTION))
        registerReceiver(mActionProxyReceiver, IntentFilter(INTENT_ACTION_PROXY_ACTION))
        registerReceiver(mUserUnlockedReceiver, IntentFilter(Intent.ACTION_USER_UNLOCKED))
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(mOnActionClickedReceiver)
        unregisterReceiver(mActionProxyReceiver)
        unregisterReceiver(mUserUnlockedReceiver)
    }

    abstract fun onUserUnlocked()
    abstract fun onActionClicked(key: String, cookies: Parcelable?)

    protected fun createNonIconAction(key: String, title: String, cookies: Parcelable): Notification.Action {
        val icon = Icon.createWithResource(this, R.drawable.ic_empty)
        val intent = Intent().setAction(INTENT_ACTION_CLICKED_ACTION)
                .putExtra(INTENT_EXTRA_COOKIES, cookies)
                .putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key)
        val pendingIntent = PendingIntent
                .getBroadcast(this,
                        cookies.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Action.Builder(icon, title, pendingIntent).build()
    }

    protected fun appendActions(notification: Notification, key: String, actions: Array<Notification.Action>) {
        for (action in notification.actions) {
            action.actionIntent = PendingIntent.getBroadcast(this,
                    action.actionIntent.hashCode(),
                    Intent(INTENT_ACTION_PROXY_ACTION)
                            .putExtra(INTENT_EXTRA_PROXY_INTENT, action.actionIntent)
                            .putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key),
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }

        notification.actions = notification.actions + actions
    }

    protected fun replaceActions(notification: Notification, actions: Array<Notification.Action>) {
        notification.actions = actions
    }

    companion object {
        const val INTENT_ACTION_CLICKED_ACTION = Constants.PREFIX_INTENT_ACTION + ".clicked.action"
        const val INTENT_ACTION_PROXY_ACTION = Constants.PREFIX_INTENT_ACTION + ".proxy.action"
        const val INTENT_EXTRA_NOTIFICATION_KEY = Constants.PREFIX_INTENT_EXTRA + ".notification.key"
        const val INTENT_EXTRA_PROXY_INTENT = Constants.PREFIX_INTENT_EXTRA + ".proxy.intent"
        const val INTENT_EXTRA_COOKIES = Constants.PREFIX_INTENT_EXTRA + ".cookies"
    }
}