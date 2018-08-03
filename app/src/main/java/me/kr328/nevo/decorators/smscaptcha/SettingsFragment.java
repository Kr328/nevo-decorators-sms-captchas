package me.kr328.nevo.decorators.smscaptcha;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import java.util.Objects;

import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;


public class SettingsFragment extends PreferenceFragmentCompat {
    public final static String TAG = SettingsFragment.class.getSimpleName();

    public final static String KEY_HIDE_IN_LAUNCHER = "setting_hide_in_launcher";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.main_setting);
        getPreferenceScreen().setEnabled(false);
        getPreferenceScreen().setIconSpaceReserved(false);

        mPluginEnabled       = (CheckBoxPreference) findPreference(Settings.SETTING_PLUGIN_ENABLED);
        mHideOnLocked        = (CheckBoxPreference) findPreference(Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED);
        mCheckCaptchaPattern = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_CHECK_PATTERN);
        mParseCaptchaPattern = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_PARSE_PATTERN);
        mHideInLauncher      = (CheckBoxPreference) findPreference(KEY_HIDE_IN_LAUNCHER);

        mPluginEnabled.setOnPreferenceChangeListener(this::onPreferenceChange);
        mHideOnLocked.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCheckCaptchaPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mParseCaptchaPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mHideInLauncher.setOnPreferenceChangeListener(this::onPreferenceChange);

        new Thread(this::loadSettingsAndUpdateViews).start();
    }

    private boolean onPreferenceChange(Preference preference, Object o) {
        String key = preference.getKey();

        switch ( key ) {
            case Settings.SETTING_PLUGIN_ENABLED :
                mSettings.pluginEnabled = (boolean) o;
                mAppPreferences.put(key ,mSettings.pluginEnabled);
                break;
            case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED :
                mSettings.captchaHideOnLocked = (boolean) o;
                mAppPreferences.put(key ,mSettings.captchaHideOnLocked);
                break;
            case Settings.SETTING_CAPTCHA_CHECK_PATTERN :
                mSettings.captchaCheckPattern = (String) o;
                mAppPreferences.put(key ,mSettings.captchaCheckPattern);
                if ( !PatternUtils.checkPatternValid((String) o) ) {
                    Toast.makeText(getActivity() ,R.string.setting_pattern_invalid ,Toast.LENGTH_LONG).show();
                    return false;
                }
                break;
            case Settings.SETTING_CAPTCHA_PARSE_PATTERN :
                mSettings.captchaParsePattern = (String) o;
                mAppPreferences.put(key ,mSettings.captchaParsePattern);
                if ( !PatternUtils.checkPatternValid((String) o) ) {
                    Toast.makeText(getActivity() ,R.string.setting_pattern_invalid ,Toast.LENGTH_LONG).show();
                    return false;
                }
                break;
            case KEY_HIDE_IN_LAUNCHER:
                new Thread(() -> updateMainActivityEnabled(! (Boolean) o)).start();
                break;
        }

        Log.i(TAG ,"Updating " + key);

        return true;
    }

    private void loadSettingsAndUpdateViews() {
        mAppPreferences = new AppPreferences(Objects.requireNonNull(getActivity()));
        mSettings = new Settings(true ,true ,getString(R.string.default_value_check_captcha_pattern) ,getString(R.string.default_value_parse_captcha_pattern))
                .readFromTrayPreference(mAppPreferences);
        getActivity().runOnUiThread(this::updateViews);
    }

    private void updateViews() {
        mPluginEnabled.setChecked(mSettings.pluginEnabled);
        mHideOnLocked.setChecked(mSettings.captchaHideOnLocked);
        mCheckCaptchaPattern.setText(mSettings.captchaCheckPattern);
        mParseCaptchaPattern.setText(mSettings.captchaParsePattern);

        getPreferenceScreen().setEnabled(true);
    }

    private void updateMainActivityEnabled(boolean enabled) {
        Objects.requireNonNull(getActivity()).
                getPackageManager().
                setComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity"),
                        enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED ,
                        PackageManager.DONT_KILL_APP);
    }

    private CheckBoxPreference mPluginEnabled;
    private CheckBoxPreference mHideOnLocked;
    private EditTextPreference mCheckCaptchaPattern;
    private EditTextPreference mParseCaptchaPattern;
    private CheckBoxPreference mHideInLauncher;

    private AppPreferences mAppPreferences;
    private Settings mSettings;
}
