package me.kr328.nevo.decorators.smscaptcha;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import java.util.HashMap;

import me.kr328.nevo.decorators.smscaptcha.utils.BroadcastUtils;
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils;

public abstract class BaseSmsDecoratorService extends NevoDecoratorService {
    public static final String TAG = BaseSmsDecoratorService.class.getSimpleName();

    public static final String INTENT_ACTION_CLICKED_ACTION             = Global.PREFIX_INTENT_ACTION + ".clicked.action";
    public static final String INTENT_ACTION_PROXY_ACTION               = Global.PREFIX_INTENT_ACTION + ".proxy.action";
    public static final String INTENT_EXTRA_NOTIFICATION_KEY            = Global.PREFIX_INTENT_EXTRA  + ".notification.key";
    public static final String INTENT_EXTRA_ACTION_HASHCODE             = Global.PREFIX_INTENT_EXTRA  + ".action.hashcode";
    public static final String INTENT_EXTRA_PROXY_INTENT                = Global.PREFIX_INTENT_EXTRA  + ".proxy.intent";

    private BroadcastReceiver mOnActionClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key   = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);
            int hashcode = intent.getIntExtra(INTENT_EXTRA_ACTION_HASHCODE ,0);
            SparseArray<ActionClickedListener> listeners = actionsMap.get(key);
            ActionClickedListener listener = listeners.get(hashcode);
            if ( listener != null ) listener.onClicked(intent);
            cancelNotification(key);
        }
    };

    private BroadcastReceiver mActionProxyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PendingIntent originalIntent = intent.getParcelableExtra(INTENT_EXTRA_PROXY_INTENT);
            String        key            = intent.getStringExtra(INTENT_EXTRA_NOTIFICATION_KEY);
            try {
                cancelNotification(key);
                originalIntent.send(context ,0 ,intent.setPackage(originalIntent.getCreatorPackage()));
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG ,"Proxy failure." ,e);
            }
        }
    };

    private BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BaseSmsDecoratorService.this.onUserUnlocked();
        }
    };

    public interface ActionClickedListener {
        void onClicked(Intent intent);
    }

    @Override
    protected void onNotificationRemoved(String key, int reason) {
        super.onNotificationRemoved(key, reason);

        actionsMap.remove(key);

        Log.i(TAG , key + " Removed");
    }

    @Override
    protected void onConnected() {
        super.onConnected();

        registerReceiver(mOnActionClickedReceiver ,new IntentFilter(INTENT_ACTION_CLICKED_ACTION));
        registerReceiver(mActionProxyReceiver ,new IntentFilter(INTENT_ACTION_PROXY_ACTION));
        registerReceiver(mUserUnlockedReceiver ,new IntentFilter(Intent.ACTION_USER_UNLOCKED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mOnActionClickedReceiver);
        unregisterReceiver(mActionProxyReceiver);
        unregisterReceiver(mUserUnlockedReceiver);
    }

    public abstract void onUserUnlocked();

    protected Notification.Action createNonIconAction(String key ,String title , ActionClickedListener listener) {
        int    listenerHashcode     = listener.hashCode();
        Icon   icon                 = Icon.createWithResource(this ,R.drawable.ic_empty);
        Intent intent               = new Intent().setAction(INTENT_ACTION_CLICKED_ACTION).putExtra(INTENT_EXTRA_ACTION_HASHCODE ,listenerHashcode).putExtra(INTENT_EXTRA_NOTIFICATION_KEY ,key);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this ,listenerHashcode ,intent ,PendingIntent.FLAG_UPDATE_CURRENT);

        addActionToActionMap(key ,listenerHashcode ,listener);

        return new Notification.Action.Builder(icon ,title ,pendingIntent).build();
    }

    protected void appendActions(Notification notification ,String key , Notification.Action[] actions) {
        Notification.Action[] appliedActions = new Notification.Action[notification.actions.length + actions.length];

        for (Notification.Action action : notification.actions) {
            action.actionIntent = PendingIntent.getBroadcast(this,
                    action.actionIntent.hashCode() ,
                    new Intent().setAction(INTENT_ACTION_PROXY_ACTION).putExtra(INTENT_EXTRA_PROXY_INTENT ,action.actionIntent).putExtra(INTENT_EXTRA_NOTIFICATION_KEY ,key) ,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        System.arraycopy(notification.actions ,0 ,appliedActions ,0 ,notification.actions.length);
        System.arraycopy(actions ,0 ,appliedActions ,notification.actions.length ,actions.length);

        notification.actions = appliedActions;
    }

    protected void replaceActions(Notification notification ,String key , Notification.Action[] actions) {
        notification.actions = actions;
    }

    private void addActionToActionMap(String key ,int hashcode ,ActionClickedListener listener) {
        SparseArray<ActionClickedListener> listeners = actionsMap.getOrDefault(key ,new SparseArray<>());
        listeners.put(hashcode ,listener);
        actionsMap.put(key ,listeners);
    }

    private HashMap<String ,SparseArray<ActionClickedListener>> actionsMap = new HashMap<>();
}
