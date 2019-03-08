package me.kr328.nevo.decorators.smscaptcha;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AttachApplicationActivity extends AppCompatActivity {
    private final static int ATTACH_REQUEST_CODE = 214;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView contentView = new ListView(this);

        contentView.setOnItemClickListener((parent, view, position, id) -> {
            String packageName = parent.getAdapter().getItem(position).toString();

            Intent attachIntent =
                    new Intent(Global.NEVOLUTION_ACTION_ACTIVATE_DECORATOR)
                            .putExtra(Global.NEVOLUTION_EXTRA_DECORATOR_COMPONENT,
                                    ComponentName.createRelative(this, CaptchaDecoratorService.class.getName()))
                            .putExtra(Global.NEVOLUTION_EXTRA_TARGET_PACKAGE, packageName);

            startActivityForResult(attachIntent, ATTACH_REQUEST_CODE);
        });

        setContentView(contentView);

        new Thread(() -> {
            ApplicationSelectorAdapter adapter =
                    new ApplicationSelectorAdapter(getPackageManager()
                            .getInstalledApplications(0));

            runOnUiThread(() -> contentView.setAdapter(adapter));
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ApplicationSelectorAdapter extends BaseAdapter {
        private List<ApplicationInfoCache> applicationInfos;

        ApplicationSelectorAdapter(List<ApplicationInfo> applicationInfos) {
            this.applicationInfos = applicationInfos.parallelStream()
                    .map(applicationInfo -> new ApplicationInfoCache() {{
                        label = applicationInfo.loadLabel(getPackageManager()).toString();
                        packageName = applicationInfo.packageName;
                        icon = applicationInfo.loadIcon(getPackageManager());
                    }})
                    .sorted(Comparator.comparing(c -> c.label))
                    .collect(Collectors.toList());
        }

        @Override
        public int getCount() {
            return applicationInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return applicationInfos.get(position).packageName;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater
                        .from(AttachApplicationActivity.this)
                        .inflate(R.layout.application_item, parent, false);
            }

            ApplicationInfoCache currentApp = applicationInfos.get(position);

            ImageView appIconView = convertView.findViewById(R.id.app_icon);
            TextView appNameView = convertView.findViewById(R.id.app_name);
            TextView appPackageView = convertView.findViewById(R.id.app_package);

            appIconView.setImageDrawable(currentApp.icon);
            appNameView.setText(currentApp.label);
            appPackageView.setText(currentApp.packageName);

            return convertView;
        }

        class ApplicationInfoCache {
            String label;
            String packageName;
            Drawable icon;
        }
    }
}
