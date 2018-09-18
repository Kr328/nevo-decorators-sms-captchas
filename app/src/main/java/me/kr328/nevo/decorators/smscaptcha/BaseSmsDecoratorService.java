package me.kr328.nevo.decorators.smscaptcha;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.util.Log;

import com.oasisfeng.nevo.sdk.NevoDecoratorService;

public abstract class BaseSmsDecoratorService extends NevoDecoratorService {
    public static final String TAG = BaseSmsDecoratorService.class.getSimpleName();

    public static final String INTENT_ACTION_CLICKED_ACTION = Global.PREFIX_INTENT_ACTION + ".clicked.action";
    public static final String INTENT_ACTION_PROXY_ACTION = Global.PREFIX_INTENT_ACTION + ".proxy.action";
    public static final String INTENT_EXTRA_NOTIFICATION_KEY = Global.PREFIX_INTENT_EXTRA + ".notification.key";
    public static final String INTENT_EXTRA_PROXY_INTENT = Global.PREFIX_INTENT_EXTRA + ".proxy.intent";
    public static final String INTENT_EXTRA_COOKIES = Global.PREFIX_INTENT_EXTRA + ".cookies";

    private BroadcastReceiver mOnActionClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);
            Parcelable cookies = intent.getParcelableExtra(INTENT_EXTRA_COOKIES);

            BaseSmsDecoratorService.this.onActionClicked(cookies);

            Log.i(TAG, "Key Clicked " + key);

            cancelNotification(key);
        }
    };

    private BroadcastReceiver mActionProxyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PendingIntent originalIntent = intent.getParcelableExtra(INTENT_EXTRA_PROXY_INTENT);
            String key = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);
            try {
                cancelNotification(key);
                originalIntent.send(context, 0, intent.setPackage(originalIntent.getCreatorPackage()));
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, "Proxy failure.", e);
            }
        }
    };

    private BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BaseSmsDecoratorService.this.onUserUnlocked();
        }
    };

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        super.onNotificationRemoved(key, reason);

        Log.i(TAG, key + " Removed");
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        registerReceiver(mOnActionClickedReceiver, new IntentFilter(INTENT_ACTION_CLICKED_ACTION));
        registerReceiver(mActionProxyReceiver, new IntentFilter(INTENT_ACTION_PROXY_ACTION));
        registerReceiver(mUserUnlockedReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mOnActionClickedReceiver);
        unregisterReceiver(mActionProxyReceiver);
        unregisterReceiver(mUserUnlockedReceiver);
    }

    public abstract void onUserUnlocked();

    public abstract void onActionClicked(Parcelable cookies);

    protected Notification.Action createNonIconAction(String key, String title, Parcelable cookies) {
        Icon icon = Icon.createWithResource(this, R.drawable.ic_empty);
        Intent intent = new Intent().setAction(INTENT_ACTION_CLICKED_ACTION).putExtra(INTENT_EXTRA_COOKIES, cookies).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    protected void appendActions(Notification notification, String key, Notification.Action[] actions) {
        Notification.Action[] appliedActions = new Notification.Action[notification.actions.length + actions.length];

        for (Notification.Action action : notification.actions) {
            action.actionIntent = PendingIntent.getBroadcast(this,
                    action.actionIntent.hashCode(),
                    new Intent().setAction(INTENT_ACTION_PROXY_ACTION).putExtra(INTENT_EXTRA_PROXY_INTENT, action.actionIntent).putExtra(INTENT_EXTRA_NOTIFICATION_KEY, key),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        System.arraycopy(notification.actions, 0, appliedActions, 0, notification.actions.length);
        System.arraycopy(actions, 0, appliedActions, notification.actions.length, actions.length);

        notification.actions = appliedActions;
    }

    protected void replaceActions(Notification notification, String key, Notification.Action[] actions) {
        notification.actions = actions;
    }
}
