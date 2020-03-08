@file:Suppress("DEPRECATION")

package me.kr328.nevo.decorators.smscaptcha.compat

import com.oasisfeng.nevo.sdk.NevoDecoratorService

fun NevoDecoratorService.cancelNotificationCompat(key: String) {
    this.cancelNotification(key)
}