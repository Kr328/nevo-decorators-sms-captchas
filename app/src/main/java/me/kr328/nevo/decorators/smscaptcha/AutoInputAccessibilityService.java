package me.kr328.nevo.decorators.smscaptcha;

import android.accessibilityservice.AccessibilityService;
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

import java.util.Objects;

public class AutoInputAccessibilityService extends AccessibilityService {
    public final static String TAG = AutoInputAccessibilityService.class.getSimpleName();

    View     floating    = null;
    TextView pasteButton = null;
    boolean  attached    = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        floating    = LayoutInflater.from(this).inflate(R.layout.floating_view ,null);
        pasteButton = floating.findViewById(R.id.paste);

        pasteButton.setOnClickListener(v -> handlePasteButtonClicked());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if ( event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null ) return;

        handleWindowStateChanged();
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG ,"Interrupted");
    }

    private void handlePasteButtonClicked() {
        AccessibilityNodeInfo node = findFocusedEditText(getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_INPUT));

        if ( node == null ) return;

        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE ,"233333");

        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT ,bundle);

    }

    private AccessibilityNodeInfo findFocusedEditText(AccessibilityNodeInfo info) {
        if ( EditText.class.getName().contentEquals(info.getClassName()) && info.isFocused() ) {
            return info;
        }

        for ( int i = 0 ; i < info.getChildCount() ; i++ ) {
            AccessibilityNodeInfo result = findFocusedEditText(info.getChild(i));
            if ( result != null ) return result;
        }

        return null;
    }

    private void handleWindowStateChanged() {
        boolean isInputMethodShowed = false;

        for ( AccessibilityWindowInfo info : getWindows() ) {
            if ( info.getType() != AccessibilityWindowInfo.TYPE_INPUT_METHOD ) continue;

            isInputMethodShowed = true;
        }

        if ( isInputMethodShowed )
            handleInputMethodPopup();
        else
            handleInputMethodClosed();
    }

    private void handleInputMethodPopup() {
        if ( attached ) return;

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

        Objects.requireNonNull(getApplicationContext().getSystemService(WindowManager.class)).addView(floating ,layoutParams);
        attached = true;
    }

    private void handleInputMethodClosed() {
        if ( !attached ) return;

        Objects.requireNonNull(getApplicationContext().getSystemService(WindowManager.class)).removeView(floating);
        attached = false;
    }
}
