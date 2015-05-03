package com.kurukurupapa.appsharehelper.helper;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.kurukurupapa.appsharehelper.activity.RecvActivity;

import junit.framework.TestCase;

public class ClipboardIntentHelperTest extends AndroidTestCase {

    private ClipboardIntentHelper sut;

    public void setUp() throws Exception {
        super.setUp();

        sut = new ClipboardIntentHelper(getContext());
    }

    public void testCreateIntent_NULL() throws Exception {
        // 準備
        ClipData clipData = null;

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_TEXT形式NULL() throws Exception {
        // 準備
        ClipData clipData = ClipData.newPlainText(null, null);

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_TEXT形式ブランク() throws Exception {
        // 準備
        ClipData clipData = ClipData.newPlainText("", "");

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_TEXT形式値あり() throws Exception {
        // 準備
        ClipData clipData = ClipData.newPlainText("ラベル", "テキスト");

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(Intent.ACTION_SEND, actual.getAction());
        assertEquals("text/plain", actual.getType());
        assertEquals(null, actual.getCategories());
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_SUBJECT));
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_HTML_TEXT));
        assertEquals("テキスト", actual.getStringExtra(Intent.EXTRA_TEXT));
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_ORIGINATING_URI));
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_INTENT));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testCreateIntent_HTML形式NULL() throws Exception {
        // 準備
        ClipData clipData = ClipData.newHtmlText(null, null, null);

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testCreateIntent_HTML形式ブランク() throws Exception {
        // 準備
        ClipData clipData = ClipData.newHtmlText("", "", "");

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void testCreateIntent_HTML形式値あり() throws Exception {
        // 準備
        ClipData clipData = ClipData.newHtmlText("ラベル", "テキスト", "<p>テキスト</p>");

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(Intent.ACTION_SEND, actual.getAction());
        assertEquals("text/html", actual.getType());
        assertEquals(null, actual.getCategories());
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_SUBJECT));
        assertEquals("<p>テキスト</p>", actual.getStringExtra(Intent.EXTRA_HTML_TEXT));
        assertEquals("テキスト", actual.getStringExtra(Intent.EXTRA_TEXT));
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_ORIGINATING_URI));
        assertEquals(null, actual.getStringExtra(Intent.EXTRA_INTENT));
    }

    public void testCreateIntent_Uri形式NULL() throws Exception {
        // 準備
        ClipData clipData = ClipData.newRawUri(null, null);

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_Uri形式値あり() throws Exception {
        // 準備
        ClipData clipData = ClipData.newRawUri("ラベル", Uri.parse("https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper"));

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_Intent形式NULL() throws Exception {
        // 準備
        ClipData clipData = ClipData.newIntent(null, null);

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

    public void testCreateIntent_Intent形式値あり() throws Exception {
        // 準備
        Intent intent = new Intent(getContext(), RecvActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, "テキスト");
        intent.putExtra("dummy", true);
        ClipData clipData = ClipData.newIntent("ラベル", intent);

        // テスト実行
        Intent actual = sut.createIntent(clipData);

        // 検証
        assertEquals(null, actual);
    }

}