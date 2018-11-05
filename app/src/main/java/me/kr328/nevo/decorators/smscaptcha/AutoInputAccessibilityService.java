package me.kr328.nevo.decorators.smscaptcha;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class AutoInputAccessibilityService extends AccessibilityService {
    public final static String TAG = AutoInputAccessibilityService.class.getSimpleName();

    View     floating       = null;
    TextView pasteButton    = null;
    boolean  attached       = false;
    boolean  inputPopped    = false;
    String   currentKey     = null;
    String   currentCaptcha = null;

    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ( Global.INTENT_CAPTCHA_NOTIFICATION_SHOW.equals(intent.getAction()) ) {
                currentKey     = intent.getStringExtra(Global.INTENT_NOTIFICATION_KEY);
                currentCaptcha = intent.getStringExtra(Global.INTENT_NOTIFICATION_CAPTCHA);
            }
            else if ( Global.INTENT_CAPTCHA_NOTIFICATION_CANCEL.equals(intent.getAction()) && currentKey != null && currentKey.equals(intent.getStringExtra(Global.INTENT_NOTIFICATION_KEY)) ) {
                currentKey     = null;
                currentCaptcha = null;
            }

            updateFloating();
        }
    };

    @SuppressLint("InflateParams")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        floating    = LayoutInflater.from(this).inflate(R.layout.floating_view ,null);
        pasteButton = floating.findViewById(R.id.paste);

        pasteButton.setOnClickListener(v -> handlePasteButtonClicked());

        registerReceiver(mNotificationReceiver ,new IntentFilter(){{
            addAction(Global.INTENT_CAPTCHA_NOTIFICATION_CANCEL);
            addAction(Global.INTENT_CAPTCHA_NOTIFICATION_SHOW);}});
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotificationReceiver);

        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if ( event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null ) return;

        inputPopped = false;

        for ( AccessibilityWindowInfo info : getWindows() ) {
            if ( info.getType() != AccessibilityWindowInfo.TYPE_INPUT_METHOD ) continue;

            inputPopped = true;
        }

        updateFloating();
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG ,"Interrupted");
    }

    private void handlePasteButtonClicked() {
        if ( currentCaptcha == null ) return;

        AccessibilityNodeInfo node = findFocusedEditText(getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_INPUT));

        if ( node == null ) {
            Toast.makeText(this ,R.string.auto_fill_service_not_able_find_node ,Toast.LENGTH_LONG).show();
            Objects.requireNonNull(getSystemService(ClipboardManager.class)).setPrimaryClip(ClipData.newPlainText("captcha" ,currentCaptcha));
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE ,currentCaptcha);

        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT ,bundle);

        sendBroadcast(new Intent(Global.INTENT_CAPTCHA_NOTIFICATION_DO_CANCEL).
                putExtra(Global.INTENT_NOTIFICATION_KEY ,currentKey).
                putExtra(Global.INTENT_NOTIFICATION_CAPTCHA ,currentKey));
    }

    private AccessibilityNodeInfo findFocusedEditText(AccessibilityNodeInfo info) {
        if ( EditText.class.getName().contentEquals(info.getClassName()) && info.isFocused() )
            return info;

        for ( int i = 0 ; i < info.getChildCount() ; i++ ) {
            AccessibilityNodeInfo result = findFocusedEditText(info.getChild(i));
            if ( result != null ) return result;
        }

        return null;
    }

    private void updateFloating() {
        if ( currentCaptcha != null && inputPopped )
            showFillButton();
        else
            hideFillButton();
    }

    private void showFillButton() {
        if ( attached ) return;

        pasteButton.setText(getString(R.string.auto_fill_service_button_format ,currentCaptcha));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.gravity = Gravity.END | Gravity.BOTTOM;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width  = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.windowAnimations = android.R.style.Animation_Translucent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;


        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

        layoutParams.format = PixelFormat.TRANSPARENT;

        Objects.requireNonNull(getSystemService(WindowManager.class)).addView(floating ,layoutParams);
        attached = true;
    }

    private void hideFillButton() {
        if ( !attached ) return;

        Objects.requireNonNull(getSystemService(WindowManager.class)).removeView(floating);
        attached = false;
    }
}
