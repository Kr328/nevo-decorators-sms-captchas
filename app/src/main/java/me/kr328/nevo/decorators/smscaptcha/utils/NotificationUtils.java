package me.kr328.nevo.decorators.smscaptcha.utils;

import android.app.Notification;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class NotificationUtils {
    private static final String MESSAGING_STYLE_KEY_TEXT = "text";

    public static Messages parseMessages(Notification notification) {
        Messages result = new Messages();

        Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages != null) {
            result.texts = Stream.of(messages)
                    .filter(m -> m instanceof Bundle)
                    .map(m -> (Bundle) m)
                    .map(b -> b.getCharSequence(MESSAGING_STYLE_KEY_TEXT))
                    .filter(Objects::nonNull)
                    .toArray(CharSequence[]::new);
        }

        if (messages == null || result.texts.length != messages.length) {
            result.texts = new CharSequence[]{notification.extras.getCharSequence(Notification.EXTRA_TEXT)};
        }

        return result;
    }

    public static void replaceMessages(Notification notification, Function<CharSequence, CharSequence> replacer) {
        Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages != null) {
            Stream.of(messages)
                    .filter(m -> m instanceof Bundle)
                    .map(m -> (Bundle) m)
                    .filter(m -> m.containsKey(MESSAGING_STYLE_KEY_TEXT))
                    .forEach(m -> m.putCharSequence(MESSAGING_STYLE_KEY_TEXT, replacer.apply(m.getCharSequence(MESSAGING_STYLE_KEY_TEXT))));
            notification.extras.putParcelableArray(Notification.EXTRA_MESSAGES ,messages.clone());
        }

        if (notification.extras.containsKey(Notification.EXTRA_TEXT))
            notification.extras.putCharSequence(Notification.EXTRA_TEXT, replacer.apply(notification.extras.getCharSequence(Notification.EXTRA_TEXT)));
    }

    public static class Messages {
        public CharSequence[] texts;
    }
}
