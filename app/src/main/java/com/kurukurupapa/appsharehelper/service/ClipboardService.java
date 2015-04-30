package com.kurukurupapa.appsharehelper.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.kurukurupapa.appsharehelper.activity.RecvActivity;

import org.apache.commons.lang3.StringUtils;

/**
 * クリップボードを操作するサービスクラスです。
 */
public class ClipboardService {
    /**
     * コンテキスト
     */
    private Context mContext;

    /**
     * コンストラクタ
     *
     * @param context
     */
    public ClipboardService(Context context) {
        mContext = context;
    }

    /**
     * クリップボードに対象データが存在するか判定します。
     *
     * @return インテント
     */
    public boolean isValidClipData() {
        // 少々安直な方法
        return createIntent() != null;
    }

    /**
     * 受信アクティビティ起動インテントを作成します。
     *
     * @return インテント
     */
    public Intent createIntent() {
        // クリップボードの内容を取得します。
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clipData = cm.getPrimaryClip();
        return createIntent(clipData);
    }

    protected Intent createIntent(ClipData clipData) {
        if (clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                String html = getHtmlText(item);
                CharSequence text = item.getText();
                if (StringUtils.isNotEmpty(html) && StringUtils.isNotEmpty(text)) {
                    return createRecvActivityIntent(html, text);
                }
            }
            if (StringUtils.isNotEmpty(item.getText())) {
                return createRecvActivityIntent(item.getText());
            }
            // URI,Intentは対象外とします。
        }

        // インテント作成不可
        return null;
    }

    private Intent createRecvActivityIntent(CharSequence clipboardHtml, CharSequence clipboardText) {
        Intent intent = createRecvActivityIntent();
        intent.setType(ClipDescription.MIMETYPE_TEXT_HTML);
        intent.putExtra(Intent.EXTRA_HTML_TEXT, clipboardHtml);
        intent.putExtra(Intent.EXTRA_TEXT, clipboardText);
        return intent;
    }

    private Intent createRecvActivityIntent(CharSequence clipboardText) {
        Intent intent = createRecvActivityIntent();
        intent.setType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        intent.putExtra(Intent.EXTRA_TEXT, clipboardText);
        return intent;
    }

    private Intent createRecvActivityIntent(Uri clipboardUri) {
        Intent intent = createRecvActivityIntent();
        intent.setType(ClipDescription.MIMETYPE_TEXT_URILIST);
        intent.putExtra(Intent.EXTRA_ORIGINATING_URI, clipboardUri);
        return intent;
    }

    private Intent createRecvActivityIntent(Intent clipboardIntent) {
        Intent intent = createRecvActivityIntent();
        intent.setType(ClipDescription.MIMETYPE_TEXT_INTENT);
        intent.putExtra(Intent.EXTRA_INTENT, clipboardIntent);
        return intent;
    }

    private Intent createRecvActivityIntent() {
        Intent intent = new Intent(mContext, RecvActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(IntentService.EXTRA_MY_APP, true);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private String getHtmlText(ClipData.Item item) {
        return item.getHtmlText();
    }

}
