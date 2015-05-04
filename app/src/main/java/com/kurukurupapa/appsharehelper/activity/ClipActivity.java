package com.kurukurupapa.appsharehelper.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Toast;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.service.IntentService;

/**
 * クリップボードアクティビティ。
 * データ共有内容をクリップボードへコピーします。
 */
public class ClipActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);

        // オブジェクト生成
        IntentService intentService = new IntentService(this);

        // データ取得
        intentService.setIntent(getIntent());
        String intentStr = intentService.getShortIntentStr();

        // クリップボードへ
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), intentStr);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);

        // トースト出力
        Toast.makeText(this, getString(R.string.msg_copy_to_clipboard), Toast.LENGTH_SHORT).show();

        // アクティビティ終了
        finish();
    }
}
