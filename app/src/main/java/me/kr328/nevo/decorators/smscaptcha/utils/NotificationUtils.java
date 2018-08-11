package me.kr328.nevo.decorators.smscaptcha.utils;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import java.util.function.Function;

public class NotificationUtils {
    public static String[] getMessages(Notification notification) {
        NotificationCompat.MessagingStyle style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        if ( style != null ) {
            return style.getMessages().
                    stream().
                    map(NotificationCompat.MessagingStyle.Message::getText).
                    toArray(String[]::new);
        }
        else {
            return new String[]{notification.extras.getString(Notification.EXTRA_TEXT)};
        }
    }

    public static void setMessages(Notification notification ,String[] messages) {
        NotificationCompat.MessagingStyle originalStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
        NotificationCompat.MessagingStyle appliedStyle  = null;
        if ( originalStyle != null ) {
            appliedStyle = new NotificationCompat.MessagingStyle(originalStyle.getUser());
            if ( messages.length == originalStyle.getMessages().size() ) {
                int i = 0;
                for (NotificationCompat.MessagingStyle.Message message : originalStyle.getMessages()) {
                    appliedStyle.addMessage(messages[i++] ,message.getTimestamp() ,message.getPerson());
                }
            }
            else { // messages.length != originalStyle.getMessages().size()
                NotificationCompat.MessagingStyle.Message message = originalStyle.getMessages().get(0);
                for ( String msg : messages )
                    appliedStyle.addMessage(msg ,message.getTimestamp() ,message.getPerson());
            }
        }
        else { // originalStyle == null
            notification.extras.remove(Notification.EXTRA_TEMPLATE);
            notification.extras.putCharSequence(Notification.EXTRA_TEXT ,messages[0]);
        }
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

    }
}
