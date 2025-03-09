package me.s1204.payment.selector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PaymentSelector extends Activity {

    protected static final String PREF_APP_LIST = "app_list";
    private  String[] packageList = {};
    private LinearLayout appListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applist);

        // LinearLayout を取得
        appListLayout = findViewById(R.id.list);
        // SharedPreferences からリストを読み込む
        loadPackageListFromPrefs();
        // refresh() を onCreate() の最後に呼び出す
        refresh();
    }

    private void refresh() {


        // アプリ一覧にアプリが１つも無かったら設定アクティビティを立ち上げ終了
        if (!checkItemCount()) {
            Log.d("PaymentSelector", "No selected apps, starting SettingsActivity");
            startActivity(
                    new Intent(Intent.ACTION_VIEW)
                            .setClassName(getPackageName(), SettingsActivity.class.getName())
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            finish();
            return;
        }

        // 既存のボタンをすべて削除
        appListLayout.removeAllViews();

        // 各パッケージに対してボタンを追加
        for (String packageName : packageList) {
            setPackage(packageName);
        }
    }

    private void setPackage(final String packageName) {
        // パッケージマネージャーを取得
        PackageManager pm = getPackageManager();
        Button button;

        try {
            // ApplicationInfo を取得
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            // アプリ名を取得
            String appName = appInfo.loadLabel(pm).toString();
            // アプリアイコンを取得
            Drawable appIcon = appInfo.loadIcon(pm);

            // 画面密度を取得
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int densityDpi = metrics.densityDpi; // 密度 DPI

            // 密度に基づいてアイコンサイズを調整
            float iconSizeDp;
            float fontSizeSp; // フォントサイズ
            switch (densityDpi) {
                case DisplayMetrics.DENSITY_LOW: // ldpi (120dpi)
                    iconSizeDp = 24f;
                    fontSizeSp = 12f;
                    break;
                case DisplayMetrics.DENSITY_MEDIUM: // mdpi (160dpi)
                    iconSizeDp = 36f;
                    fontSizeSp = 14f;
                    break;
                case DisplayMetrics.DENSITY_HIGH: // hdpi (240dpi)
                    iconSizeDp = 48f;
                    fontSizeSp = 16f;
                    break;
                case DisplayMetrics.DENSITY_XHIGH: // xhdpi (320dpi)
                    iconSizeDp = 72f;
                    fontSizeSp = 18f;
                    break;
                case DisplayMetrics.DENSITY_XXHIGH: // xxhdpi (480dpi)
                    iconSizeDp = 96f;
                    fontSizeSp = 20f;
                    break;
                case DisplayMetrics.DENSITY_XXXHIGH: // xxxhdpi (640dpi)
                    iconSizeDp = 144f;
                    fontSizeSp = 24f;
                    break;
                default:
                    iconSizeDp = 48f;
                    fontSizeSp = 16f;
                    break;
            }

            // アイコンサイズを画面密度に応じて設定
            int iconSizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, iconSizeDp, getResources().getDisplayMetrics()
            );
            appIcon.setBounds(0, 0, iconSizePx, iconSizePx);

            // ボタンを生成
            button = new Button(this);

            // アプリ名テキストを太字にする
            button.setTypeface(null, android.graphics.Typeface.BOLD);
            // フォントサイズを画面密度に応じて設定
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
            // テキストを設定
            button.setText(getString(R.string.space, appName));
            // 大文字変換を無効化
            button.setAllCaps(false);
            // アイコンを設定
            button.setCompoundDrawables(appIcon, null, null, null);
            // タグに packageName を設定（後でIntent生成に使用）
            button.setTag(packageName);
            // ボタン用 LinearLayout を追加
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Button を追加するレイアウトを指定
            button.setLayoutParams(layoutParams);
            // レイアウトに Button を追加
            appListLayout.addView(button);
        } catch (PackageManager.NameNotFoundException ignored) {
            // パッケージが存在しない場合は何もせず、ボタンも追加しない
            return;
        }
        // ボタンが押された時の処理
        button.setOnClickListener(v -> {
            try {
                // アクティビティを指定
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    // アクティビティを起動
                    startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    // アクティビティが起動できない時は何もしない(例外を発生させない)
                    Toast.makeText(PaymentSelector.this, R.string.cannot_launch, Toast.LENGTH_SHORT).show();
                }
            } catch (ActivityNotFoundException ignored) {
                // 起動出来なかった時は何もしない
            } finally {
                // このアプリを終了
                finishAndRemoveTask();
            }
        });
    }

    /**
     * リストに選択されているアプリの数の確認
     *
     * @return リストに選択されているアプリの合計数が１以上かどうか
     */
    private boolean checkItemCount() {
        return packageList != null && packageList.length > 0;
    }


    /**
     * SharedPreferences からリストを読み込む
     */
    private void loadPackageListFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_APP_LIST, MODE_PRIVATE);
        String packageListString = prefs.getString(PREF_APP_LIST, ""); // デフォルト値は空文字列

        Log.d("PaymentSelector", "Loaded package list string: " + packageListString); //SharedPreferencesの内容確認用Log

        if (!packageListString.isEmpty()) {
            packageList = packageListString.split(","); // カンマ区切りで分割
            Log.d("PaymentSelector", "Loaded package list: " + TextUtils.join(", ", packageList)); //分割後のリスト確認用Log
        } else {
            packageList = new String[]{}; // SharedPreferencesが空の場合は空のリストにする
            Log.d("PaymentSelector", "Package list is empty");
        }
    }

    /**
     * SharedPreferences にリストを保存する
     */
    public static void savePackageListToPrefs(Context context, String[] list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_APP_LIST, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 配列をカンマ区切りの文字列に変換
        String packageListString = TextUtils.join(",", list);
        editor.putString(PREF_APP_LIST, packageListString);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }
}