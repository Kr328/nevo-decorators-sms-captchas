package me.kr328.nevo.decorators.smscaptcha;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.kr328.nevo.decorators.smscaptcha.utils.PackageUtils;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWindowOreo();

        new Thread(this::detectNevolution).start();
    }

    private void setupWindowOreo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary ,getTheme()));
    }

    private void detectNevolution() {
        if ( PackageUtils.getPackageVersionCode(this ,Global.NEVOLUTION_PACKAGE_NAME) < 30000 ) {
            runOnUiThread(() -> new AlertDialog.Builder(this).
                    setTitle(R.string.nevolution_invalid_dialog_title).
                    setMessage(R.string.nevolution_invalid_dialog_message).
                    setOnDismissListener(dialog -> this.finish()).
                    setPositiveButton(R.string.nevolution_invalid_dialog_button,(dialog , i) -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + Global.NEVOLUTION_PACKAGE_NAME)))).
                    show());
        }
        else {
            runOnUiThread(() -> setContentView(R.layout.main_layout));
        }
    }
}
