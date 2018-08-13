package me.kr328.nevo.decorators.smscaptcha.utils;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import java.util.Objects;
import java.util.function.Function;

public class NotificationUtils {
    public static class Messages {
        public String   address;
        public String[] text;
    }

    public static Messages parseMessages(Notification notification) {
        Messages result = new Messages();
        result.address = notification.extras.getCharSequence(Notification.EXTRA_TITLE ,"").toString();

        NotificationCompat.MessagingStyle style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        if ( style != null ) {
            result.text = style.getMessages().
                    stream().
                    map(NotificationCompat.MessagingStyle.Message::getText).
                    toArray(String[]::new);
        }
        else {
            CharSequence message = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            if ( message != null )
                result.text = new String[]{message.toString()};
            else
                result.text = new String[0];
        }

        return result;
    }

    public static void replaceMessages(Notification notification , Function<CharSequence ,CharSequence> replacer) {
        NotificationCompat.MessagingStyle originalStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        NotificationCompat.MessagingStyle appliedStyle  = null;
        if ( originalStyle != null ) {
            appliedStyle = new NotificationCompat.MessagingStyle(originalStyle.getUser());
            appliedStyle.setConversationTitle(originalStyle.getConversationTitle());
            appliedStyle.setGroupConversation(originalStyle.isGroupConversation());

            for ( NotificationCompat.MessagingStyle.Message message : originalStyle.getMessages() )
                appliedStyle.addMessage(replacer.apply(message.getText()) ,message.getTimestamp() ,message.getPerson());

            appliedStyle.addCompatExtras(notification.extras);
        }
        else {
            notification.extras.remove(Notification.EXTRA_TEMPLATE);
            notification.extras.putCharSequence(Notification.EXTRA_TEXT, replacer.apply(notification.extras.getCharSequence(Notification.EXTRA_TEXT)));
        }
    }
    public static void rebuildMessageStyle(Notification notification) {
        NotificationCompat.MessagingStyle style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        if ( style != null )
            style.addCompatExtras(notification.extras);
        else notification.extras.remove(Notification.EXTRA_TEMPLATE);
    }
}
