package com.kurukurupapa.appsharehelper.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.helper.ClipboardIntentHelper;
import com.kurukurupapa.appsharehelper.service.IntentService;

/**
 * クリップボード共有アクティビティ。
 * 基本的には、受信アクティビティ（RecvActivity）と同様の動作ですが、共有元がクリップボードです。
 */
public class ClipboardSendActivity extends RecvActivity {
    private static final String TAG = ClipboardSendActivity.class.getSimpleName();

    @Override
    protected void setupActionBar() {
        getActionBar().setTitle(R.string.title_activity_clipboard_send);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(R.drawable.ic_launcher);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * インテント受信サービスを設定します。
     * @param intent アクティビティで取得するインテント
     */
    protected void setupIntentService(Intent intent, Bundle savedInstanceState) {
        Intent newIntent = new ClipboardIntentHelper(this).createIntent();
        mIntentService.setIntent(newIntent);
        mIntentService.setSrcAppInfo(getPackageName(), getString(R.string.label_clipboard));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called.");

        // クリップボード内容の取得
        setupIntentService(null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Called");
    }

}
