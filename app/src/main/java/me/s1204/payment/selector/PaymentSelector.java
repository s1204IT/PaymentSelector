package me.s1204.payment.selector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PaymentSelector extends Activity {

    /**
     * アプリ起動時の処理
     * @since v1.0.0
     * @see #setPackage(int, String, String)
     * @author Syuugo
     * @noinspection SpellCheckingInspection*/
    private void refresh() {
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
        // VポイントPay
        setPackage(
                R.id.v_point_pay,
                "com.smbc_card.vpoint",
                ".ui.splash.SplashActivity"
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
