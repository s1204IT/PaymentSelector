package me.s1204.payment.selector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    /**
     * アプリ一覧に設定されているアプリの合計数を返す
     * @return 設定されているアプリの合計数
     * @since v1.1.0
     * @author Syuugo
     */
    protected static int countSelectedItem() {
        //TODO: showAppList() の実装後にこちらも修正
        return 1;
    }

    /**
     * アプリ一覧に表示するアプリの設定
     * @since v1.1.0
     * @author Syuugo
     */
    private void showAppList() {
        //TODO: AlertDialog で既存アプリをリスト化し、チェックボックスで選択後、閉じるボタンで finish()
        Toast.makeText(this, SettingsActivity.class.getName(), Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    /// @see #showAppList()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAppList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }

}
