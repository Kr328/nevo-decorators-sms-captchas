package me.kr328.nevo.decorators.smscaptcha;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Objects;

import me.kr328.nevo.decorators.smscaptcha.utils.ResourceUtils;

public class PermissionHelpActivity extends AppCompatActivity {
    public static final String COMMAND = "adb shell appops set me.kr328.nevo.decorators.smscaptcha WRITE_SMS allow";

    private TextView   mMethodText;
    private Button     mActionButton;
    private RadioGroup mMethodRadios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_help);

        mMethodText   = findViewById(R.id.method_text);
        mActionButton = findViewById(R.id.action_button);
        mMethodRadios = findViewById(R.id.permission_obtain_method);

        mActionButton.setOnClickListener(this::onActionButtonClicked);
        mMethodRadios.setOnCheckedChangeListener((radioGroup, i) -> updateViews());

        this.updateViews();
    }

    private void updateViews() {
        switch (mMethodRadios.getCheckedRadioButtonId()) {
            case R.id.obtain_by_root :
                mMethodText.setVisibility(View.GONE);
                mActionButton.setText(R.string.permission_obtain_permission);
                break;
            case R.id.obtain_by_adb :
                mMethodText.setVisibility(View.VISIBLE);
                mActionButton.setText(R.string.permission_copy_command);
                break;
        }
    }

    private void onActionButtonClicked(View view) {
        switch (mMethodRadios.getCheckedRadioButtonId()) {
            case R.id.obtain_by_root :
                Toast.makeText(this ,"未实现" ,Toast.LENGTH_LONG).show();
                break;
            case R.id.obtain_by_adb :
                copyCommand();
                break;
        }
    }

    private void copyCommand() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        Objects.requireNonNull(clipboardManager).setPrimaryClip(ClipData.newPlainText("Command" ,COMMAND));
    }
}
