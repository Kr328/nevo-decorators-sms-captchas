@file:Suppress("DEPRECATION")

package me.kr328.nevo.decorators.smscaptcha.compat

import android.app.Notification
import com.oasisfeng.nevo.sdk.MutableNotification

val NOTIFICATION_PRIORITY_LOW_COMPAT: Int
    get() = Notification.PRIORITY_LOW
val NOTIFICATION_PRIORITY_HIGH_COMPAT: Int
    get() = Notification.PRIORITY_HIGH

var MutableNotification.priorityCompat: Int
    get() = this.priority
    set(value) {
        this.priority = value
    }