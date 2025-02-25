package me.s1204.payment.selector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PaymentSelector extends Activity {

    private static final String DOUBLE_PRESS = "function_key_config_doublepress";
    private static final String DOUBLE_PRESS_TYPE = DOUBLE_PRESS + "_type";
    private static final String DOUBLE_PRESS_VALUE = DOUBLE_PRESS + "_value";

    /**
     * アプリ起動時の処理
     * @since v1.0.0
     * @see #checkDoublePress()
     * @see #setPackage(int, String, String)
     * @author Syuugo
     * @noinspection SpellCheckingInspection*/
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

        // [2回押し]が有効でかつ選択されているかどうかの処理
        checkDoublePress();
        // レイアウトを表示
        setContentView(R.layout.applist);

        // Google ウォレット
        setPackage(
                R.id.google_pay,
                "com.google.android.apps.walletnfcrel",
                "com.google.commerce.tapandpay.android.wallet.WalletActivity"
        );
        // PayPay
        setPackage(
                R.id.paypay,
                "jp.ne.paypay.android.app",
                ".MainActivity"
        );
        // 楽天ペイ
        setPackage(
                R.id.rakuten_pay,
                "jp.co.rakuten.pay",
                "jp.co.rakuten.wallet.activities.StartActivity"
        );
        // d払い
        setPackage(
                R.id.d_pay,
                "com.nttdocomo.keitai.payment",
                ".presentation.scenes.splash.view.SplashActivity"
        );
        // ファミペイ
        setPackage(
                R.id.famipay,
                "jp.co.family.familymart_app",
                "jp.co.family.familymart.presentation.splash.SplashActivity"
        );
        // VポイントPay
        setPackage(
                R.id.v_point_pay,
                "com.smbc_card.vpoint",
                ".ui.splash.SplashActivity"
        );
        // QUICK RIDE
        setPackage(
                R.id.quick_ride,
                "com.lecipapp",
                ".SplashActivity"
        );
        // WESTER
        setPackage(
                R.id.wester,
                "jp.co.westjr.wester",
                ".presentation.splash.SplashActivity"
        );
        // 住信SBI
        setPackage(
                R.id.neobank,
                "jp.co.netbk",
                "jp.co.sbi_nbapp.SplashActivity"
        );
        // みんなの銀行
        setPackage(
                R.id.minna_no_ginko,
                "com.MinnaNoGinko.bankapp",
                ".MainActivity"
        );
    }

    /**
     * 決済アプリを起動
     * @param resId ボタンのリソースID
     * @param packageName 起動対象のパッケージ名
     * @param className 起動対象のクラス名
     * @since v1.0.0
     * @author Syuugo
     * @noinspection ReassignedVariable*/
    private void setPackage(final int resId, final String packageName, String className) {
        // クラス名の先頭がパッケージ名と同じ場合は連結
        if (className.startsWith(".")) className = packageName + className;
        // ClickListener にクラス名の変数を定数として引き継ぐ
        final String finalClassName = className;
        // ボタンの定数を定義
        final Button button = findViewById(resId);
        // 対象のパッケージが存在するかを確認
        try {
            // メタデータが取得可能か試みる
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ignored) {
            // パッケージが存在しない場合はボタンを非表示
            button.setVisibility(View.GONE);
        }
        // ボタンが押された時の処理
        button.setOnClickListener(v -> {
            try {
                // アクティビティを起動
                startActivity(
                        // 既定のアクティビティ
                        new Intent(Intent.ACTION_MAIN)
                                // 指定されたパッケージ名とクラス名
                                .setClassName(packageName, finalClassName)
                                // このアプリが終了されても新しいタスクを維持
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
            } catch (ActivityNotFoundException ignored) {
                // 起動出来なかった時は何もしない
            }
            // このアプリを終了
            finishAndRemoveTask();
        });
    }

    /**
     * アプリ一覧にアプリが１つ以上選択されているか
     * @since v1.1.0
     * @see SettingsActivity#countSelectedItem()
     * @author Syuugo
     */
    private boolean checkItemCount() {
        return SettingsActivity.countSelectedItem() > 0;
    }

    /**
     * Galaxy において、ダブルクリックが有効かどうかを確認
     * @since v1.1.0
     * @author Syuugo
     */
    private void checkDoublePress() {
        if (
                // Samsung 製の端末かどうか
                Settings.System.getString(getContentResolver(), "preload_fingerprint").startsWith("samsung/")
                // [2回押し]が有効かどうか
                && (!checkInt(DOUBLE_PRESS, 1)
                // [アプリを起動]が選択されているかどうか
                || !checkInt(DOUBLE_PRESS_TYPE, 2)
                // このアプリが対象に選ばれているかどうか
                || !checkString(DOUBLE_PRESS_VALUE, getPackageName() + "/" + getClass().getName()))
        ) {
            // トーストメッセージを表示
            Toast.makeText(this, "ダブルクリックの対象に選択されていません", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Global ネームスペースの整数型の値が一致するかどうかを返す
     * @param name 変数名
     * @param value 検証する値
     * @since v1.1.0
     * @author Syuugo
     * @noinspection BooleanMethodIsAlwaysInverted*/
    private boolean checkInt(String name, int value) {
        try {
            return Settings.Global.getInt(getContentResolver(), name) == value;
        } catch (Settings.SettingNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Global ネームスペースの文字列型の値が一致するかどうかを返す
     * @param name 変数名
     * @param value 検証する値
     * @since v1.1.0
     * @author Syuugo
     * @noinspection SameParameterValue*/
    private boolean checkString(String name, String value) {
        return Settings.Global.getString(getContentResolver(), name).equals(value);
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
