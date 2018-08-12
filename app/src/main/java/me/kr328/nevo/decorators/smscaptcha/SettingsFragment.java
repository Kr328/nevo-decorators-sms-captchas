package me.kr328.nevo.decorators.smscaptcha;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import net.grandcentrix.tray.AppPreferences;

import java.util.Objects;

import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;


public class SettingsFragment extends PreferenceFragmentCompat {
    public final static String TAG = SettingsFragment.class.getSimpleName();

    public final static String NEVOLUTION_PACKAGE_NAME = "com.oasisfeng.nevo";

    public final static String KEY_HIDE_IN_LAUNCHER = "setting_hide_in_launcher";
    private CheckBoxPreference mCaptchaHideOnLocked;
    private EditTextPreference mCaptchaIdentifyPattern;
    private EditTextPreference mCaptchaParsePattern;
    private EditTextPreference mSubscribeIdentityPattern;
    private Preference         mSubscribePriority;
    private CheckBoxPreference mHideInLauncher;
    private AppPreferences     mAppPreferences;
    private Settings           mSettings;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.main_setting);
        getPreferenceScreen().setEnabled(false);

        mCaptchaHideOnLocked      = (CheckBoxPreference) findPreference(Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED);
        mCaptchaIdentifyPattern   = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN);
        mCaptchaParsePattern      = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_PARSE_PATTERN);
        mSubscribeIdentityPattern = (EditTextPreference) findPreference(Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN);
        mSubscribePriority        = findPreference(Settings.SETTING_SUBSCRIBE_PRIORITY);
        mHideInLauncher           = (CheckBoxPreference) findPreference(KEY_HIDE_IN_LAUNCHER);

        mCaptchaHideOnLocked.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaIdentifyPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaParsePattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mSubscribeIdentityPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mHideInLauncher.setOnPreferenceChangeListener(this::onPreferenceChange);
        mSubscribePriority.setOnPreferenceChangeListener(this::onPreferenceChange);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mSubscribePriority.setOnPreferenceClickListener((Preference p) -> {
                startActivity(
                        new Intent("android.settings.APP_NOTIFICATION_SETTINGS").
                                putExtra("android.provider.extra.APP_PACKAGE" ,NEVOLUTION_PACKAGE_NAME));
                return false;
            });
        }

        new Thread(this::loadSettingsAndUpdateViews).start();
    }

    private boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();

        switch (key) {
            case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED:
                mSettings.setCaptchaHideOnLocked((Boolean) value);
                mAppPreferences.put(key, (Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN:
                if (!PatternUtils.checkPatternValid((String) value)) return false;
                mSettings.setCaptchaIdentifyPattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_CAPTCHA_PARSE_PATTERN:
                if (!PatternUtils.checkPatternValid((String) value)) return false;
                mSettings.setCaptchaParsePattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN:
                if (!PatternUtils.checkPatternValid((String) value)) return false;
                mSettings.setSubscribeIdentifyPattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_SUBSCRIBE_PRIORITY:
                int valueInteger = Integer.parseInt((String) value);
                mSettings.setSubscribePriority(valueInteger);
                mAppPreferences.put(key, valueInteger);
                break;
            case KEY_HIDE_IN_LAUNCHER:
                new Thread(() -> updateMainActivityEnabled(!(Boolean)value)).start();
                break;
        }

        return true;
    }

    private void loadSettingsAndUpdateViews() {
        mAppPreferences = new AppPreferences(Objects.requireNonNull(getActivity()));
        mSettings = Settings.defaultValueFromContext(getActivity()).readFromTrayPreference(mAppPreferences);
        getActivity().runOnUiThread(this::updateViews);

        boolean isActivityHidden = getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity")) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        getActivity().runOnUiThread(() -> mHideInLauncher.setChecked(isActivityHidden));
    }

    private void updateViews() {
        mCaptchaHideOnLocked.setChecked(mSettings.isCaptchaHideOnLocked());
        mCaptchaIdentifyPattern.setText(mSettings.getCaptchaIdentifyPattern());
        mCaptchaParsePattern.setText(mSettings.getCaptchaParsePattern());
        mSubscribeIdentityPattern.setText(mSettings.getSubscribeIdentifyPattern());

        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O )
            ((ListPreference)mSubscribePriority).setValue(String.valueOf(mSettings.getSubscribePriority()));

        getPreferenceScreen().setEnabled(true);
    }

    private void updateMainActivityEnabled(boolean enabled) {
        Objects.requireNonNull(getActivity()).
                getPackageManager().
                setComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity"),
                        enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }
}
