package me.kr328.nevo.decorators.smscaptcha;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.kr328.nevo.decorators.smscaptcha.utils.PackageUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWindowOreo();

        new Thread(this::detectedNevolution).start();
    }

    private void setupWindowOreo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary ,getTheme()));
    }

    private void detectedNevolution() {
        if ( !PackageUtils.hasPackageInstalled(this ,Global.NEVOLUTION_PACKAGE_NAME) ) {
            runOnUiThread(() -> new AlertDialog.Builder(this).
                    setTitle(R.string.missing_nevolution_dialog_title).
                    setMessage(R.string.missing_nevolution_dialog_message).
                    setOnDismissListener(dialog -> this.finish()).
                    setPositiveButton(R.string.missing_nevolution_dialog_button ,(dialog ,i) -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + Global.NEVOLUTION_PACKAGE_NAME)))).
                    show());
        }
        else {
            runOnUiThread(() -> setContentView(R.layout.main_layout));
        }
    }
}
