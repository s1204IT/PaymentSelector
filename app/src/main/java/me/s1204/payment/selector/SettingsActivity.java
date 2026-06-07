package me.s1204.payment.selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        showAppListDialog();
    }

    private void showAppListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_apps_title);

        ListView appListView = new ListView(this);
        final List<AppInfo> appInfoList = getInstalledAppInfoList(); // アプリ情報のリストを取得
        loadSelectedApps(this, appInfoList); // 選択状態をロード
        AppListAdapter adapter = new AppListAdapter(this, appInfoList);
        appListView.setAdapter(adapter);

        builder.setView(appListView);

        builder.setPositiveButton(R.string.next, (dialog, which) -> {
            List<AppInfo> selectedApps = new ArrayList<>();
            for (AppInfo appInfo : appInfoList) {
                if (appInfo.isChecked) {
                    selectedApps.add(appInfo);
                }
            }

            if (selectedApps.isEmpty()) {
                clearSelectedApps(this);
            } else {
                // 選択したアプリの順序を設定する画面を表示
                showAppOrderDialog(selectedApps);
            }
        });
        
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // インストールされているアプリの情報を取得
    private List<AppInfo> getInstalledAppInfoList() {
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = resolveInfo.loadLabel(packageManager).toString();
            appInfoList.add(new AppInfo(appName, packageName, false));
        }

        // アプリ名をアルファベット順にソート
        appInfoList.sort((app1, app2) -> app1.appName.compareToIgnoreCase(app2.appName));

        return appInfoList;
    }

    // SharedPreferences から以前に選択されたアプリを読み込む
    private void loadSelectedApps(Context context, List<AppInfo> appInfoList) {
        SharedPreferences prefs = context.getSharedPreferences(PaymentSelector.PREF_APP_LIST, MODE_PRIVATE);
        String selectedAppsString = prefs.getString(PaymentSelector.PREF_APP_LIST, "");
        if (selectedAppsString.isEmpty()) {
            return;
        }

        String[] selectedAppPackages = selectedAppsString.split(",");
        for (AppInfo appInfo : appInfoList) {
            for (String selectedPackage : selectedAppPackages) {
                // 前後の空白を除去し、大文字小文字を区別せずに比較
                if (appInfo.packageName.trim().equalsIgnoreCase(selectedPackage.trim())) {
                    appInfo.isChecked = true;
                    break;
                }
            }
        }
    }


    // SharedPreferences をクリア
    private void clearSelectedApps(Context context) {
        SharedPreferences prefs = getSharedPreferences(PaymentSelector.PREF_APP_LIST, MODE_PRIVATE);
        prefs.edit().clear().commit();

        // PaymentSelector の packageList も空にする
        PaymentSelector.savePackageListToPrefs(context, new String[]{});
        Toast.makeText(this, R.string.no_apps_selected, Toast.LENGTH_SHORT).show();

        finish();
    }


    // 並び替え後のアプリの順序を保存
    private void savePackageOrder(Context context, List<AppInfo> orderedApps) {
        List<String> orderedPackages = new ArrayList<>();
        for (AppInfo appInfo : orderedApps) {
            orderedPackages.add(appInfo.packageName);
        }
        String appListString = TextUtils.join(",", orderedPackages);

        // SharedPreferences に保存
        SharedPreferences prefs = getSharedPreferences(PaymentSelector.PREF_APP_LIST, MODE_PRIVATE);
        prefs.edit().putString(PaymentSelector.PREF_APP_LIST, appListString).commit();

        // アクティビティを更新
        PaymentSelector.savePackageListToPrefs(context, orderedPackages.toArray(new String[0]));

        finish();
    }

    // 選択したアプリの並び順を編集するダイアログを表示
    private void showAppOrderDialog(List<AppInfo> selectedApps) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_order_title);

        // プログラムでListViewを作成
        ListView appOrderListView = new ListView(this);
        AppOrderAdapter adapter = new AppOrderAdapter(this, selectedApps);
        appOrderListView.setAdapter(adapter);

        builder.setView(appOrderListView);

        // 保存ボタンの設定
        builder.setPositiveButton(R.string.save, (dialog, which) -> savePackageOrder(this, selectedApps));

        // キャンセルボタンの設定
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 画面密度に応じたアプリアイコンの表示サイズ(px)を計算
    private static int computeIconSizePx(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // 密度に基づいてアイコンサイズを調整
        float iconSizeDp;

        //noinspection EnhancedSwitchMigration
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:      // ldpi (120dpi)
                iconSizeDp = 24f;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:   // mdpi (160dpi)
                iconSizeDp = 36f;
                break;
            case DisplayMetrics.DENSITY_HIGH:     // hdpi (240dpi)
                iconSizeDp = 48f;
                break;
            case DisplayMetrics.DENSITY_XHIGH:    // xhdpi (320dpi)
                iconSizeDp = 72f;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:   // xxhdpi (480dpi)
                iconSizeDp = 96f;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:  // xxxhdpi (640dpi)
                iconSizeDp = 144f;
                break;
            default:
                iconSizeDp = 48f;
                break;
        }

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, iconSizeDp, metrics);
    }

    static class AppInfo {
        String appName;
        String packageName;
        boolean isChecked;

        public AppInfo(String appName, String packageName, boolean isChecked) {
            this.appName = appName;
            this.packageName = packageName;
            this.isChecked = isChecked;
        }
    }

    static class AppListAdapter extends ArrayAdapter<AppInfo> {
        private final List<AppInfo> mAppList; // AppInfo のリストを保持

        public AppListAdapter(Context context, List<AppInfo> appList) {
            super(context, 0, appList);
            mAppList = appList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                // リストアイテムのレイアウトをプログラムで作成
                LinearLayout itemLayout = new LinearLayout(getContext());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(8, 8, 8, 8);

                ImageView appIconView = new ImageView(getContext());
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                appIconView.setLayoutParams(iconParams);
                // ScaleType を設定
                appIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                appIconView.setId(View.generateViewId());
                itemLayout.addView(appIconView);


                TextView appNameTextView = new TextView(getContext());
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                appNameTextView.setLayoutParams(textViewParams);
                appNameTextView.setTextSize(16);
                appNameTextView.setTypeface(null, Typeface.BOLD);
                appNameTextView.setId(View.generateViewId());
                itemLayout.addView(appNameTextView);

                CheckBox checkBox = new CheckBox(getContext());
                itemLayout.addView(checkBox);
                checkBox.setId(View.generateViewId());

                convertView = itemLayout;

                holder = new ViewHolder();
                holder.appIconView = appIconView;
                holder.appNameTextView = appNameTextView;
                holder.checkBox = checkBox;

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = mAppList.get(position);
            holder.appNameTextView.setText(appInfo.appName);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(appInfo.isChecked);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> appInfo.isChecked = isChecked);

            PackageManager pm = getContext().getPackageManager();
            Drawable appIcon;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
                appIcon = applicationInfo.loadIcon(pm);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

            // アイコンサイズ指定
            int iconSizePx = computeIconSizePx(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            holder.appIconView.setLayoutParams(layoutParams);
            holder.appIconView.setImageDrawable(appIcon);

            return convertView;
        }

        static class ViewHolder {
            ImageView appIconView;
            TextView appNameTextView;
            CheckBox checkBox;
        }
    }


    static class AppOrderAdapter extends ArrayAdapter<AppInfo> {
        private final LayoutInflater inflater;
        private final List<AppInfo> mAppList; // AppInfo のリスト

        public AppOrderAdapter(Context context, List<AppInfo> appList) {
            super(context, 0, appList);
            inflater = LayoutInflater.from(context);
            mAppList = appList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sequence, parent, false);

                holder = new ViewHolder();
                holder.appIconView = convertView.findViewById(R.id.app_icon);
                holder.appNameTextView = convertView.findViewById(R.id.app_name);
                holder.upButton = convertView.findViewById(R.id.up_button);
                holder.downButton = convertView.findViewById(R.id.down_button);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = mAppList.get(position);
            holder.appNameTextView.setText(appInfo.appName);

            PackageManager pm = getContext().getPackageManager();
            Drawable appIcon = null;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
                appIcon = applicationInfo.loadIcon(pm);
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            int iconSizePx = computeIconSizePx(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            holder.appIconView.setLayoutParams(layoutParams);
            holder.appIconView.setImageDrawable(appIcon);

            // 上下ボタンの表示状態を現在のリスト内の位置に応じて切り替え
            int currentPosition = mAppList.indexOf(appInfo);
            holder.upButton.setEnabled(currentPosition > 0);
            holder.downButton.setEnabled(currentPosition < mAppList.size() - 1);

            // 入れ替え
            holder.upButton.setOnClickListener(v -> {
                int pos = mAppList.indexOf(appInfo);
                if (pos > 0) {
                    Collections.swap(mAppList, pos, pos - 1);
                    notifyDataSetChanged();
                }
            });
            holder.downButton.setOnClickListener(v -> {
                int pos = mAppList.indexOf(appInfo);
                if (pos < mAppList.size() - 1) {
                    Collections.swap(mAppList, pos, pos + 1);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }

        static class ViewHolder {
            ImageView appIconView;
            TextView appNameTextView;
            ImageButton upButton;
            ImageButton downButton;
        }
    }

}