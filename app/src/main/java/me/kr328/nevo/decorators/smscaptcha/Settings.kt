package me.kr328.nevo.decorators.smscaptcha

import net.grandcentrix.tray.TrayPreferences

data class Settings(var isCaptchaHideOnLocked: Boolean = false,
                    var isCaptchaOverrideDefaultAction: Boolean = false,
                    var isCaptchaUseDefaultPattern: Boolean = false,
                    var captchaIdentifyPattern: String = "",
                    var captchaParsePattern: String = "") {

    fun applyFromTrayPreference(preferences: TrayPreferences): Settings {
        isCaptchaHideOnLocked = preferences.getBoolean(SETTING_CAPTCHA_HIDE_ON_LOCKED, isCaptchaHideOnLocked)
        isCaptchaOverrideDefaultAction = preferences.getBoolean(SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION, isCaptchaOverrideDefaultAction)
        isCaptchaUseDefaultPattern = preferences.getBoolean(SETTING_CAPTCHA_USE_DEFAULT_PATTERN, true)
        captchaIdentifyPattern = preferences.getString(SETTING_CAPTCHA_IDENTIFY_PATTERN, captchaIdentifyPattern)!!
        captchaParsePattern = preferences.getString(SETTING_CAPTCHA_PARSE_PATTERN, captchaParsePattern)!!
        return this
    }

    companion object {
        const val SETTING_CAPTCHA_HIDE_ON_LOCKED = "setting_captcha_hide_on_locked"
        const val SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION = "setting_captcha_override_default_action"
        const val SETTING_CAPTCHA_USE_DEFAULT_PATTERN = "setting_captcha_use_default_pattern"
        const val SETTING_CAPTCHA_IDENTIFY_PATTERN = "setting_captcha_identify_pattern"
        const val SETTING_CAPTCHA_PARSE_PATTERN = "setting_captcha_parse_pattern"
    }
}