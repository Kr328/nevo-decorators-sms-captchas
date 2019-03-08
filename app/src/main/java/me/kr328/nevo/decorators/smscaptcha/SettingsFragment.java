package me.kr328.nevo.decorators.smscaptcha;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import me.kr328.nevo.decorators.smscaptcha.utils.PatternUtils;


public class SettingsFragment extends PreferenceFragmentCompat {
    public final static String TAG = SettingsFragment.class.getSimpleName();

    public final static int    REQUEST_DISPLAY_OVER_OTHER_CODE = 21;

    public final static String KEY_HIDE_IN_LAUNCHER = "setting_hide_in_launcher";
    public final static String KEY_ENABLE_AUTO_FILL = "setting_captcha_enable_auto_fill";

    private CheckBoxPreference mCaptchaHideOnLocked;
    private CheckBoxPreference mCaptchaOverrideDefaultAction;
    private CheckBoxPreference mCaptchaUseDefaultPattern;
    private Preference         mCaptchaEnableAutoFill;
    private EditTextPreference mCaptchaIdentifyPattern;
    private EditTextPreference mCaptchaParsePattern;
    private CheckBoxPreference mHideInLauncher;

    private Settings           mSettings;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.main_setting);
        getPreferenceScreen().setEnabled(false);

        mCaptchaHideOnLocked           = findPreference(Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED);
        mCaptchaOverrideDefaultAction  = findPreference(Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION);
        mCaptchaUseDefaultPattern      = findPreference(Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN);
        mCaptchaEnableAutoFill         = findPreference(KEY_ENABLE_AUTO_FILL);
        mCaptchaIdentifyPattern        = findPreference(Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN);
        mCaptchaParsePattern           = findPreference(Settings.SETTING_CAPTCHA_PARSE_PATTERN);
        mHideInLauncher                = findPreference(KEY_HIDE_IN_LAUNCHER);

        mCaptchaHideOnLocked.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaOverrideDefaultAction.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaUseDefaultPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaEnableAutoFill.setOnPreferenceClickListener(this::onAutoFillClicked);
        mCaptchaIdentifyPattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mCaptchaParsePattern.setOnPreferenceChangeListener(this::onPreferenceChange);
        mHideInLauncher.setOnPreferenceChangeListener(this::onPreferenceChange);

        new Thread(this::loadSettingsAndUpdateViews).start();
    }

    private boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();

        switch (key) {
            case Settings.SETTING_CAPTCHA_HIDE_ON_LOCKED:
                mSettings.setCaptchaHideOnLocked((Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_OVERRIDE_DEFAULT_ACTION :
                mSettings.setCaptchaOverrideDefaultAction((Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_USE_DEFAULT_PATTERN :
                mSettings.setCaptchaUseDefaultPattern((Boolean) value);
                break;
            case Settings.SETTING_CAPTCHA_IDENTIFY_PATTERN:
                if (checkPatternInvalidAndMakeToast((String) value)) return false;
                mSettings.setCaptchaIdentifyPattern((String) value);
                break;
            case Settings.SETTING_CAPTCHA_PARSE_PATTERN:
                if (checkPatternInvalidAndMakeToast((String) value)) return false;
                mSettings.setCaptchaParsePattern((String) value);
                break;
            case KEY_HIDE_IN_LAUNCHER:
                new Thread(() -> updateMainActivityEnabled(!(Boolean)value)).start();
                break;
            case KEY_ENABLE_AUTO_FILL :

        }

        return true;
    }

    private boolean onAutoFillClicked(Preference preference) {
        new AlertDialog.Builder(requireContext()).
                setTitle(R.string.auto_fill_tips_title).
                setMessage(R.string.auto_fill_tips_content).
                setPositiveButton(R.string.auto_fill_tips_ok, ((dia, which) -> startAccessibility())).
                show();

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
        mSettings = Settings.fromApplication(requireActivity().getApplication());
        requireActivity().runOnUiThread(this::updateViews);

        boolean isActivityHidden = requireActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity")) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        requireActivity().runOnUiThread(() -> mHideInLauncher.setChecked(isActivityHidden));
    }

    private void updateViews() {
        mCaptchaHideOnLocked.setChecked(mSettings.isCaptchaHideOnLocked());
        mCaptchaOverrideDefaultAction.setChecked(mSettings.isCaptchaOverrideDefaultAction());
        mCaptchaUseDefaultPattern.setChecked(mSettings.isCaptchaUseDefaultPattern());
        mCaptchaIdentifyPattern.setText(mSettings.getCaptchaIdentifyPattern());
        mCaptchaParsePattern.setText(mSettings.getCaptchaParsePattern());

        getPreferenceScreen().setEnabled(true);
    }

    private void updateMainActivityEnabled(boolean enabled) {
        Objects.requireNonNull(getActivity()).
                getPackageManager().
                setComponentEnabledSetting(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".MainActivity"),
                        enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }

    private void startAccessibility() {
        if ( !android.provider.Settings.canDrawOverlays(requireContext()) ) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION) ,REQUEST_DISPLAY_OVER_OTHER_CODE);
            Toast.makeText(requireContext() ,R.string.auto_fill_tips_enable_toast ,Toast.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(requireContext() ,R.string.auto_fill_tips_enable_toast ,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_DISPLAY_OVER_OTHER_CODE :
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(requireContext() ,R.string.auto_fill_tips_enable_toast ,Toast.LENGTH_LONG).show();
                break;
        }
    }
}
