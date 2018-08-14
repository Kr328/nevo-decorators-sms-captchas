package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class MessageUtils {
    public static final String TAG = MessageUtils.class.getSimpleName();
    public static final Uri    URI_SMS_INBOX = Uri.parse("content://sms");

    public static void markAsRead(Context context ,String address ,String text) {
        ContentValues values = new ContentValues();
        values.put("read" ,true);
        context.getContentResolver().
                update(URI_SMS_INBOX ,values ,"address=? And body=?" ,new String[]{processAddress(address) ,text});
    }

    public static void delete(Context context ,String address ,String text) {
        context.getContentResolver().
                delete(URI_SMS_INBOX ,"address=? And body=?" ,new String[]{processAddress(address) ,text});
        Log.i(TAG ,"Fucking Android " + processAddress(address));
    }

    private static String processAddress(String address) {
        return address.replaceAll("[()\\s]" ,"");
    }
}
