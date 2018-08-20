package me.kr328.nevo.decorators.smscaptcha;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import java.util.Objects;

import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;


public class SettingsFragment extends PreferenceFragmentCompat {
    public final static String TAG = SettingsFragment.class.getSimpleName();

    public final static String WEBSITE_PERMISSION_HELP = "https://kr328.github.io/nevo-decorators-sms-captchas/docs/obtain_permission";

    public final static String KEY_HIDE_IN_LAUNCHER = "setting_hide_in_launcher";

    private CheckBoxPreference mCaptchaHideOnLocked;
    private CheckBoxPreference mCaptchaOverrideDefaultAction;
    private ListPreference     mCaptchaPostCopyAction;
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

        mCaptchaHideOnLocked           = (CheckBoxPreference) findPreference(Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED);
        mCaptchaOverrideDefaultAction  = (CheckBoxPreference) findPreference(Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION);
        mCaptchaPostCopyAction         = (ListPreference)     findPreference(Settings.SETTING_CAPTCHA_POST_COPY_ACTION);
        mCaptchaIdentifyPattern        = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN);
        mCaptchaParsePattern           = (EditTextPreference) findPreference(Settings.SETTING_CAPTCHA_PARSE_PATTERN);
        mSubscribeIdentityPattern      = (EditTextPreference) findPreference(Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN);
        mSubscribePriority             = findPreference(Settings.SETTING_SUBSCRIBE_PRIORITY);
        mHideInLauncher                = (CheckBoxPreference) findPreference(KEY_HIDE_IN_LAUNCHER);

        mCaptchaHideOnLocked.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaOverrideDefaultAction.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaPostCopyAction.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaIdentifyPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaParsePattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mSubscribeIdentityPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mHideInLauncher.setOnPreferenceChangeListener(this::onPreferenceChange);
        mSubscribePriority.setOnPreferenceChangeListener(this::onPreferenceChange);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mSubscribePriority.setOnPreferenceClickListener((Preference p) -> {
                startActivity(
                        new Intent("android.settings.APP_NOTIFICATION_SETTINGS").
                                putExtra("android.provider.extra.APP_PACKAGE" , Global.NEVOLUTION_PACKAGE_NAME));
                return false;
            });
        }

        new Thread(this::loadSettingsAndUpdateViews).start();
    }

    private boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        int valueInteger = 0;

        switch (key) {
            case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED:
                mSettings.setCaptchaHideOnLocked((Boolean) value);
                mAppPreferences.put(key, (Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION :
                mSettings.setCaptchaOverrideDefaultAction((Boolean) value);
                mAppPreferences.put(key ,(Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_POST_COPY_ACTION :
                valueInteger = Integer.parseInt((String) value);
                mSettings.setCaptchaPostCopyAction(valueInteger);
                mAppPreferences.put(key ,valueInteger);
                requestPermission(valueInteger);
                break;
            case Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN:
                if (checkPatternInvalidAndMakeToast((String) value)) return false;
                mSettings.setCaptchaIdentifyPattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_CAPTCHA_PARSE_PATTERN:
                if (checkPatternInvalidAndMakeToast((String) value)) return false;
                mSettings.setCaptchaParsePattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_SUBSCRIBE_IDENTIFY_PATTERN:
                if (checkPatternInvalidAndMakeToast((String) value)) return false;
                mSettings.setSubscribeIdentifyPattern((String) value);
                mAppPreferences.put(key, (String) value);
                break;
            case Settings.SETTING_SUBSCRIBE_PRIORITY:
                valueInteger = Integer.parseInt((String) value);
                mSettings.setSubscribePriority(valueInteger);
                mAppPreferences.put(key, valueInteger);
                break;
            case KEY_HIDE_IN_LAUNCHER:
                new Thread(() -> updateMainActivityEnabled(!(Boolean)value)).start();
                break;
        }

        return true;
    }

    private boolean checkPatternInvalidAndMakeToast(String pattern) {
        if (!PatternUtils.checkPatternValid(pattern)) {
            Toast.makeText(this.getActivity() ,R.string.setting_pattern_invalid ,Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
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
        mCaptchaOverrideDefaultAction.setChecked(mSettings.isCaptchaOverrideDefaultAction());
        mCaptchaPostCopyAction.setValue(String.valueOf(mSettings.getCaptchaPostCopyAction()));
        mCaptchaIdentifyPattern.setText(mSettings.getCaptchaIdentifyPattern());
        mCaptchaParsePattern.setText(mSettings.getCaptchaParsePattern());
        mSubscribeIdentityPattern.setText(mSettings.getSubscribeIdentifyPattern());

        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O )
            ((ListPreference)mSubscribePriority).setValue(String.valueOf(mSettings.getSubscribePriority()));

        getPreferenceScreen().setEnabled(true);
    }

    private void requestPermission(int postAction) {
        if (postAction == Settings.POST_ACTION_NONE) return;

        if ( !shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
            requestPermissions(new String[]{Manifest.permission.READ_SMS} ,0);
        showPermissionTips();
    }


    private void showPermissionTips() {
        new AlertDialog.Builder(requireContext()).
                setTitle(R.string.permission_tips_title).
                setMessage(R.string.permission_tips_content).
                setNegativeButton(R.string.permission_tips_cancel ,((dialogInterface, i) -> dialogInterface.dismiss())).
                setPositiveButton(R.string.permission_tips_help ,(dialogInterface, i) -> new CustomTabsIntent.Builder().build().launchUrl(requireContext() ,Uri.parse(WEBSITE_PERMISSION_HELP))).
                create().show();
    }

    private void updateMainActivityEnabled(boolean enabled) {
        Objects.requireNonNull(getActivity()).
                getPackageManager().
                setComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity"),
                        enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }
}
