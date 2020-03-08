package me.kr328.nevo.decorators.smscaptcha.utils

import android.app.Notification
import androidx.core.app.NotificationCompat

object NotificationUtils {
    fun parseMessages(notification: Notification): List<String> {
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)

        return if (style != null) {
            style.messages.map { it.text.toString() }
        } else {
            val message = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            if (message != null) listOf(message.toString()) else emptyList()
        }
    }

    fun replaceMessages(notification: Notification, replacer: (String) -> String) {
        val originalStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        if (originalStyle != null) {
            originalStyle.messages.replaceAll {
                NotificationCompat.MessagingStyle.Message(replacer(it.text.toString()), it.timestamp, it.person)
            }
            originalStyle.addCompatExtras(notification.extras)
        } else {
            notification.extras.remove(Notification.EXTRA_TEMPLATE)
            notification.extras.putCharSequence(Notification.EXTRA_TEXT,
                    replacer(notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()))
        }
    }

    fun rebuildMessageStyle(notification: Notification) {
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        if (style != null) style.addCompatExtras(notification.extras) else notification.extras.remove(Notification.EXTRA_TEMPLATE)
    }
}