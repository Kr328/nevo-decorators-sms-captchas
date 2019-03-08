package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class MessageUtils {
    public static final String TAG = MessageUtils.class.getSimpleName();
    public static final Uri URI_SMS_INBOX = Uri.parse("content://sms");

    public static void markAsRead(Context context, CharSequence text) {
        ContentValues values = new ContentValues();
        values.put("read", true);
        context.getContentResolver().
                update(URI_SMS_INBOX, values, "body=?", new String[]{text.toString()});
    }

    public static void delete(Context context, CharSequence text) {
        context.getContentResolver().
                delete(URI_SMS_INBOX, "body=?", new String[]{text.toString()});
        Log.i(TAG, "Fucking Android " + text);
    }

    private static String processAddress(String address) {
        return address.replaceAll("[()\\s]", "");
    }
}
