package me.kr328.nevo.decorators.smscaptcha.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readFromResource(Context context , int resId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resId);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer ,0 ,buffer.length);
            inputStream.close();
            return new String(buffer ,"utf-8");
        } catch (IOException e) {
            throw new UnsupportedOperationException("Read raw file");
        }
    }
}
