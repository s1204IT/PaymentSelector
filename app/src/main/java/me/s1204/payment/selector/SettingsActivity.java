package me.s1204.payment.selector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //TODO: 画面外でチェック状態解除＆順序変更
        showAppListDialog();
    }

    private void showAppListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("アプリを選択");

        // プログラムでListViewを作成
        ListView appListView = new ListView(this);
        final List<AppInfo> appInfoList = getInstalledAppInfoList(); // アプリ情報のリストを取得
        loadSelectedApps(this, appInfoList); // 選択状態をロード
        AppListAdapter adapter = new AppListAdapter(this, appInfoList); // アダプターを作成
        appListView.setAdapter(adapter); // ListViewにAdapterを設定

        builder.setView(appListView);

        // OKボタンの設定
        builder.setPositiveButton("保存", (dialog, which) -> {
            saveSelectedApps(this, appInfoList);

            finish();
        });

        // キャンセルボタンの設定
        builder.setNegativeButton("キャンセル", (dialog, which) -> {
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
        if (!selectedAppsString.isEmpty()) {
            String[] selectedAppPackages = selectedAppsString.split(",");
            for (AppInfo appInfo : appInfoList) {
                appInfo.isChecked = false; // 初期化
                for (String selectedPackage : selectedAppPackages) {
                    //空白を除去し、大文字小文字を区別せずに比較
                    if (appInfo.packageName.trim().equalsIgnoreCase(selectedPackage.trim())) {
                        appInfo.isChecked = true;
                        break;
                    }
                }
            }
        } else {
            // SharedPreferencesに値がない場合、すべてのチェックを外す
            for (AppInfo appInfo : appInfoList) {
                appInfo.isChecked = false;
            }
        }
    }


    @SuppressLint("ApplySharedPref")
    private void saveSelectedApps(Context context, List<AppInfo> appInfoList) {
        List<String> selectedAppPackages = new ArrayList<>();
        for (AppInfo appInfo : appInfoList) {
            if (appInfo.isChecked) {
                selectedAppPackages.add(appInfo.packageName);
            }
        }
        String appListString = TextUtils.join(",", selectedAppPackages);

        // SharedPreferencesに保存
        SharedPreferences prefs = getSharedPreferences(PaymentSelector.PREF_APP_LIST, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // チェックボックスが1つも選択されていない場合、SharedPreferencesをクリアする
        if (selectedAppPackages.isEmpty()) {
            editor.clear();
            // PaymentSelector の packageList も空にする
            PaymentSelector.savePackageListToPrefs(context, new String[]{});
            Toast.makeText(this, "選択されたアプリはありません", Toast.LENGTH_SHORT).show();

        } else {
            editor.putString(PaymentSelector.PREF_APP_LIST, appListString);
            //PaymentSelectorのアクティビティを更新
            PaymentSelector.savePackageListToPrefs(context, appListString.split(","));

            // アプリを再起動

            startActivity(Objects.requireNonNull(getBaseContext().getPackageManager().
                            getLaunchIntentForPackage(getBaseContext().getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        editor.commit();
        finish();// SharedPreferencesを保存後、SettingsActivityを閉じる
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
        /** @noinspection FieldCanBeLocal, unused , FieldMayBeFinal */
        private LayoutInflater inflater;
        /** @noinspection FieldMayBeFinal*/
        private List<AppInfo> mAppList; // AppInfoのリストを保持

        public AppListAdapter(Context context, List<AppInfo> appList) {
            super(context, 0, appList); // XMLレイアウトを使わないのでresourceIdは0でOK
            inflater = LayoutInflater.from(context);
            mAppList = appList;
        }


        /** @noinspection NullableProblems*/
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
                // ScaleTypeを設定
                appIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                appIconView.setId(View.generateViewId());
                itemLayout.addView(appIconView);


                TextView appNameTextView = new TextView(getContext());
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                appNameTextView.setLayoutParams(textViewParams);
                appNameTextView.setTextSize(16);
                appNameTextView.setTypeface(null, android.graphics.Typeface.BOLD); // 太字にする
                appNameTextView.setId(View.generateViewId()); // IDを生成
                itemLayout.addView(appNameTextView);

                CheckBox checkBox = new CheckBox(getContext());
                itemLayout.addView(checkBox);
                checkBox.setId(View.generateViewId()); // IDを生成

                convertView = itemLayout;

                holder = new ViewHolder();
                holder.appIconView = appIconView;
                holder.appNameTextView = appNameTextView;
                holder.checkBox = checkBox;

                convertView.setTag(holder);

                //convertView が null の場合にのみ、setOnCheckedChangeListener() メソッドを呼び出す
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    AppInfo appInfo = mAppList.get(position);
                    appInfo.isChecked = isChecked;

                });


            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo appInfo = mAppList.get(position);
            holder.appNameTextView.setText(appInfo.appName);
            holder.checkBox.setChecked(appInfo.isChecked);
            PackageManager pm = getContext().getPackageManager();
            Drawable appIcon;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
                appIcon = applicationInfo.loadIcon(pm);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

            // 画面密度を取得
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            int densityDpi = metrics.densityDpi; // 密度 DPI (例: 160, 240, 320, 480)

            // 密度に基づいてアイコンサイズを調整
            float iconSizeDp;

            //noinspection EnhancedSwitchMigration
            switch (densityDpi) {
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

            int iconSizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, iconSizeDp, getContext().getResources().getDisplayMetrics());
            // appIcon.setBounds(0, 0, iconSizePx, iconSizePx);

            //アイコンサイズ指定
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

}