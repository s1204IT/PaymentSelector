package me.s1204.payment.selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

public class ClearActivity extends Activity {

    private void confirmClearList() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.clear_list_confirm_message)
                .setPositiveButton(android.R.string.yes, (d, s) -> clearList())
                .setNegativeButton(android.R.string.no, (d, s) -> finish())
                .show();
    }

    // 選択済みアプリ一覧を空にする
    private void clearList() {
        PaymentSelector.savePackageListToPrefs(this, new String[]{});
        finishAndRemoveTask();
        Toast.makeText(this, R.string.app_list_cleared, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        confirmClearList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }

}
