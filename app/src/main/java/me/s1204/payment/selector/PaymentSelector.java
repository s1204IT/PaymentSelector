package me.s1204.payment.selector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PaymentSelector extends Activity {

    // 決済アプリリスト
    private static final String[] packageList = {
            "com.google.android.apps.walletnfcrel", // Google ウォレット
            "jp.ne.paypay.android.app", // PayPay
            "jp.co.rakuten.pay", // 楽天ペイ
            "com.nttdocomo.keitai.payment", // d払い
            "jp.co.family.familymart_app", // ファミペイ
            "com.smbc_card.vpoint", // VポイントPay
            "com.lecipapp", // QUICK RIDE
            "jp.co.westjr.wester", // WESTER
            "jp.co.netbk", // 住信SBI
            "com.MinnaNoGinko.bankapp" // みんなの銀行
    };

    private static final String DOUBLE_PRESS = "function_key_config_doublepress";
    private static final String DOUBLE_PRESS_TYPE = DOUBLE_PRESS + "_type";
    private static final String DOUBLE_PRESS_VALUE = DOUBLE_PRESS + "_value";
    // LinearLayoutのインスタンス変数
    private LinearLayout appListLayout;

    /**
     * アプリ起動時の処理
     *
     * @since v1.0.0
     * @see #setPackage(String)
     * @author Syuugo
     * @noinspection SpellCheckingInspection
     */
    private void refresh() {

        // アプリ一覧にアプリが１つも無かったら設定アクティビティを立ち上げ終了
        if (!checkItemCount()) {
            startActivity(
                new Intent(Intent.ACTION_VIEW)
                        .setClassName(getPackageName(), SettingsActivity.class.getName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            finish();
            return;
        }

        // レイアウトを表示
        setContentView(R.layout.applist);

        // LinearLayout を取得
        appListLayout = findViewById(R.id.list);

        // 各パッケージに対してボタンを追加
        for (String packageName : packageList) {
            setPackage(packageName);
        }
    }

    /**
     * 決済アプリを起動
     *
     * @param packageName 起動対象のパッケージ名
     * @since v1.0.0
     * @author Syuugo
     */
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
     * @since v1.1.0
     * @see SettingsActivity#countSelectedItem()
     * @author Syuugo
     */
    private boolean checkItemCount() {
        return SettingsActivity.countSelectedItem() > 0;
    }

    /// @see #refresh()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }

}