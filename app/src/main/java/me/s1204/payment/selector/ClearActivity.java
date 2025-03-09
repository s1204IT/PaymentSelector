package me.s1204.payment.selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

public class ClearActivity extends Activity {

    /**
     * 構成のクリア
     * @since v1.1.0
     * @see #clearList()
     * @author Syuugo
     */
    private void checkUserAcception() {
        new AlertDialog.Builder(this)
                .setTitle("アプリ一覧のリセット")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("設定したアプリ一覧をリセットしますか？")
                .setPositiveButton(android.R.string.yes, (d, s) -> clearList())
                .setNegativeButton(android.R.string.no, (d, s) -> finish())
                .show();
    }

    /**
     * @since v1.1.0
     * @see #checkUserAcception()
     * @author Syuugo
     */
    private void clearList() {
        PaymentSelector.savePackageListToPrefs(this, new String[]{});
        finishAndRemoveTask();
        Toast.makeText(this, "アプリ一覧をクリアしました", Toast.LENGTH_SHORT).show();
    }

    /// @see #checkUserAcception()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserAcception();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }

}
