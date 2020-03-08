package me.kr328.nevo.decorators.smscaptcha

@Suppress("MemberVisibilityCanBePrivate")
object Constants {
    const val TAG = "NevoSmsCaptchas"

    const val PREFIX = BuildConfig.APPLICATION_ID
    const val PREFIX_INTENT = "$PREFIX.intent"
    const val PREFIX_INTENT_ACTION = "$PREFIX_INTENT.action"
    const val PREFIX_INTENT_EXTRA = "$PREFIX_INTENT.extra"
    const val PREFIX_NOTIFICATION = "$PREFIX.notification"
    const val PREFIX_NOTIFICATION_EXTRA = "$PREFIX_NOTIFICATION.extra"
    const val NOTIFICATION_EXTRA_APPLIED = "$PREFIX_NOTIFICATION_EXTRA.applied"
    const val NEVOLUTION_PACKAGE_NAME = "com.oasisfeng.nevo"
}