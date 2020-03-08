package me.kr328.nevo.decorators.smscaptcha

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils.checkPatternValid
import net.grandcentrix.tray.AppPreferences
import kotlin.concurrent.thread

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var mCaptchaHideOnLocked: CheckBoxPreference
    private lateinit var mCaptchaOverrideDefaultAction: CheckBoxPreference
    private lateinit var mCaptchaUseDefaultPattern: CheckBoxPreference
    private lateinit var mCaptchaIdentifyPattern: EditTextPreference
    private lateinit var mCaptchaParsePattern: EditTextPreference
    private lateinit var mAppPreferences: AppPreferences
    private lateinit var mSettings: Settings

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_setting)

        preferenceScreen.isEnabled = false

        mCaptchaHideOnLocked = findPreference(Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED)!!
        mCaptchaOverrideDefaultAction = findPreference(Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION)!!
        mCaptchaUseDefaultPattern = findPreference(Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN)!!
        mCaptchaIdentifyPattern = findPreference(Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN)!!
        mCaptchaParsePattern = findPreference(Settings.SETTING_CAPTCHA_PARSE_PATTERN)!!

        mCaptchaHideOnLocked.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener(::onPreferenceChange)
        mCaptchaOverrideDefaultAction.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener(::onPreferenceChange)
        mCaptchaIdentifyPattern.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener(::onPreferenceChange)
        mCaptchaParsePattern.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener(::onPreferenceChange)

        thread {
            loadSettingsAndUpdateViews()
        }
    }

    private fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        when (val key = preference.key) {
            Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED -> {
                mSettings.isCaptchaHideOnLocked = (value as Boolean)
                mAppPreferences.put(key, value)
            }
            Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION -> {
                mSettings.isCaptchaOverrideDefaultAction = (value as Boolean)
                mAppPreferences.put(key, value)
            }
            Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN -> {
                mSettings.isCaptchaUseDefaultPattern = (value as Boolean)
                mAppPreferences.put(key, value)
            }
            Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN -> {
                if (checkPatternInvalidAndMakeToast(value as String)) return false
                mSettings.captchaIdentifyPattern = value
                mAppPreferences.put(key, value)
            }
            Settings.SETTING_CAPTCHA_PARSE_PATTERN -> {
                if (checkPatternInvalidAndMakeToast(value as String)) return false
                mSettings.captchaParsePattern = value
                mAppPreferences.put(key, value)
            }
        }
        return true
    }

    private fun checkPatternInvalidAndMakeToast(pattern: String): Boolean {
        if (!checkPatternValid(pattern)) {
            Toast.makeText(this.activity, R.string.setting_pattern_invalid, Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    private fun loadSettingsAndUpdateViews() {
        val activity = requireActivity()

        mAppPreferences = AppPreferences(activity)
        mSettings = Settings().applyFromTrayPreference(mAppPreferences)

        activity.runOnUiThread {
            updateViews()
        }
    }

    private fun updateViews() {
        mCaptchaHideOnLocked.isChecked = mSettings.isCaptchaHideOnLocked
        mCaptchaOverrideDefaultAction.isChecked = mSettings.isCaptchaOverrideDefaultAction
        mCaptchaIdentifyPattern.text = mSettings.captchaIdentifyPattern
        mCaptchaParsePattern.text = mSettings.captchaParsePattern

        preferenceScreen.isEnabled = true
    }
}