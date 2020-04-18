package me.kr328.nevo.decorators.smscaptcha

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification
import me.kr328.nevo.decorators.smscaptcha.compat.NOTIFICATION_PRIORITY_HIGH_COMPAT
import me.kr328.nevo.decorators.smscaptcha.compat.priorityCompat
import me.kr328.nevo.decorators.smscaptcha.utils.CaptchaUtils
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils.parseMessages
import me.kr328.nevo.decorators.smscaptcha.utils.NotificationUtils.replaceMessages
import me.kr328.nevo.decorators.smscaptcha.utils.PackageUtils.hasPackageInstalled
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils.compilePattern
import net.grandcentrix.tray.AppPreferences
import net.grandcentrix.tray.core.TrayItem
import java.lang.Exception
import java.util.*

class CaptchaDecoratorService : BaseSmsDecoratorService() {
    private lateinit var mSettings: Settings
    private lateinit var mCaptchaUtils: CaptchaUtils
    private val mAppliedMessages = mutableMapOf<String, List<String>>()
    private val mKeyguardReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            recastAllNotifications()
        }
    }

    override fun apply(evolving: MutableStatusBarNotification): Boolean {
        try {
            val notification = evolving.notification
            val extras = notification.extras
            val messages = (mAppliedMessages[evolving.key] ?: emptyList()) + parseMessages(notification)
            val captchas = mCaptchaUtils.extractSmsCaptchas(messages, mSettings.isCaptchaUseDefaultPattern)

            if (captchas.isEmpty()) return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.channelId = NOTIFICATION_CHANNEL_CAPTCHA
            } else {
                notification.priorityCompat = NOTIFICATION_PRIORITY_HIGH_COMPAT
            }

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (mSettings.isCaptchaHideOnLocked && keyguardManager.isKeyguardLocked)
                applyKeyguardLocked(notification, evolving.key, messages, captchas)
            else
                applyKeyguardUnlocked(notification, evolving.key, messages, captchas)

            notification.flags = notification.flags or Notification.FLAG_ONLY_ALERT_ONCE
            notification.visibility = Notification.VISIBILITY_PUBLIC
            extras.putBoolean(Constants.NOTIFICATION_EXTRA_APPLIED, true)
            mAppliedMessages[evolving.key] = messages

            Log.i(Constants.TAG, "Applied " + evolving.key)

            return true
        }
        catch (e: Exception) {
            Log.w(Constants.TAG, "Apply failure", e)
            return false
        }
    }

    private fun applyKeyguardLocked(notification: Notification, key: String, messages: List<String>, captchas: List<String>) {
        val actions = arrayOf(
                createNonIconAction(key, getString(R.string.captcha_service_notification_locked_action_copy_code), CaptchaMessage(messages, captchas[0]))
        )
        replaceMessages(notification) { text: String -> mCaptchaUtils.replaceCaptchaWithChar(text, captchas) }

        if (mSettings.isCaptchaOverrideDefaultAction)
            replaceActions(notification, actions)
        else
            appendActions(notification, key, actions)
    }

    private fun applyKeyguardUnlocked(notification: Notification, key: String, messages: List<String>, captchas: List<String>) {
        val actions = captchas.map {
            createNonIconAction(key, getString(R.string.captcha_service_notification_unlocked_action_copy_code_format, it), CaptchaMessage(messages, it))
        }.toTypedArray()

        if (mSettings.isCaptchaOverrideDefaultAction)
            replaceActions(notification, actions)
        else
            appendActions(notification, key, actions)
    }

    private fun loadSettings() {
        if (Objects.requireNonNull(getSystemService(UserManager::class.java)).isUserUnlocked) {
            val mAppPreferences = AppPreferences(this)
            mSettings = Settings().applyFromTrayPreference(mAppPreferences)
            mAppPreferences.registerOnTrayPreferenceChangeListener(this::onSettingsChanged)
        } else {
            mSettings = Settings()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_CAPTCHA,
                    getString(R.string.captcha_service_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH)

            for (packageName in DEFAULT_TARGET_PACKAGES)
                if (hasPackageInstalled(this, packageName)) createNotificationChannels(packageName, Process.myUserHandle(), listOf(channel))
        }
    }

    private fun initCaptchaUtils() {
        mCaptchaUtils = CaptchaUtils(this,
                compilePattern(mSettings.captchaIdentifyPattern, "", setOf(RegexOption.IGNORE_CASE)),
                compilePattern(mSettings.captchaParsePattern, "", setOf(RegexOption.IGNORE_CASE)))
    }

    private fun recastAllNotifications() {
        for (entry in mAppliedMessages) recastNotification(entry.key, null)
        mAppliedMessages.clear()
    }

    private fun copyCaptcha(captcha: String) {
        (Objects.requireNonNull(getSystemService(Context.CLIPBOARD_SERVICE)) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("SmsCaptcha", captcha))
        Toast.makeText(this, getString(R.string.captcha_service_toast_copied_format, captcha), Toast.LENGTH_LONG).show()
    }

    override fun onUserUnlocked() {
        loadSettings()
    }

    override fun onActionClicked(key: String, cookies: Parcelable?) {
        val captchaMessage = cookies as CaptchaMessage? ?: return
        copyCaptcha(captchaMessage.captcha)
    }

    private fun registerReceivers() {
        registerReceiver(mKeyguardReceiver, object : IntentFilter() {
            init {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
        })
    }

    private fun unregisterReceivers() {
        unregisterReceiver(mKeyguardReceiver)
    }

    override fun onNotificationRemoved(key: String, reason: Int): Boolean {
        super.onNotificationRemoved(key, reason)

        mAppliedMessages.remove(key)

        return false
    }

    override fun onConnected() {
        super.onConnected()

        loadSettings()
        initCaptchaUtils()
        createNotificationChannels()
        registerReceivers()
    }

    private fun onSettingsChanged(trayItems: Collection<TrayItem>) {
        for (item in trayItems) {
            when (item.key()) {
                Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN -> {
                    mSettings.captchaIdentifyPattern = item.value().orEmpty()
                    initCaptchaUtils()
                }
                Settings.SETTING_CAPTCHA_PARSE_PATTERN -> {
                    mSettings.captchaParsePattern = item.value().orEmpty()
                    initCaptchaUtils()
                }
                Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN -> {
                    mSettings.isCaptchaUseDefaultPattern = item.value()?.toBoolean() ?: true
                    initCaptchaUtils()
                }
                Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED -> mSettings.isCaptchaHideOnLocked = item.value()?.toBoolean()
                        ?: false
                Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION -> mSettings.isCaptchaOverrideDefaultAction = item.value()?.toBoolean()
                        ?: false
            }
            Log.i(Constants.TAG, "Settings Updated " + item.key() + "=" + item.value())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }

    @Keep
    data class CaptchaMessage(val messages: List<String>, val captcha: String) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.createStringArrayList() ?: throw NullPointerException(),
                parcel.readString() ?: throw NullPointerException())

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeStringList(messages)
            dest.writeString(captcha)
        }

        companion object CREATOR : Parcelable.Creator<CaptchaMessage> {
            override fun createFromParcel(parcel: Parcel): CaptchaMessage {
                return CaptchaMessage(parcel)
            }

            override fun newArray(size: Int): Array<CaptchaMessage?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        val DEFAULT_TARGET_PACKAGES = listOf(
                "com.android.messaging",
                "com.google.android.apps.messaging",
                "com.android.mms",
                "com.sonyericsson.conversations",
                "com.moez.QKSMS"
        )
        const val NOTIFICATION_CHANNEL_CAPTCHA = "notification_channel_captcha"
        const val NOTIFICATION_EXTRA_RECAST = Constants.PREFIX_NOTIFICATION_EXTRA + ".captcha.notification.recast"
    }
}