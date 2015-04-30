package com.kurukurupapa.appsharehelper.service;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.kurukurupapa.appsharehelper.activity.RecvActivity;

public class IntentServiceTest extends AndroidTestCase {

    private IntentService sut;

    public IntentServiceTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();

        sut = new IntentService(getContext());
    }

    public void testGetShortIntentStr_未設定() {
        // 準備
        Intent intent = new Intent();
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみHTML形式NULL() {
        // 準備
        Intent intent = createHtmlClipDataIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみHTML形式ブランク() {
        // 準備
        Intent intent = createHtmlClipDataIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみHTML形式値あり() {
        // 準備
        Intent intent = createHtmlClipDataIntent("ラベル", "テキスト");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみTEXT形式NULL() {
        // 準備
        Intent intent = createTextClipDataIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみTEXT形式ブランク() {
        // 準備
        Intent intent = createTextClipDataIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみTEXT形式値あり() {
        // 準備
        Intent intent = createTextClipDataIntent("ラベル", "テキスト");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみURI形式NULL() {
        // Uri.parseメソッドで、NullPointerExceptionが発生するため、テスト不可。
//        // 準備
//        Intent intent = createUriClipDataIntent(null, null);
//        sut.setIntent(intent);
//
//        // テスト実行
//        String actual = sut.getShortIntentStr();
//
//        // 検証
//        String expected = "テキストデータがありません。";
//        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみURI形式ブランク() {
        // 準備
        Intent intent = createUriClipDataIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみURI形式値あり() {
        // 準備
        Intent intent = createUriClipDataIntent("ラベル", "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみIntent形式NULL() {
        // 準備
        Intent intent = createIntentClipDataIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみIntent形式ブランク() {
        // 準備
        Intent clipDataIntent = new Intent();
        Intent intent = createIntentClipDataIntent("", clipDataIntent);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ClipDataのみIntent形式値あり() {
        // 準備
        Intent clipDataIntent = new Intent(getContext(), RecvActivity.class);
        clipDataIntent.setAction(Intent.ACTION_SEND);
        clipDataIntent.addCategory(Intent.CATEGORY_TEST);
        clipDataIntent.putExtra(Intent.EXTRA_TEXT, "Intentテキスト");
        clipDataIntent.putExtra("dummyText", "Dummyテキスト");
        clipDataIntent.putExtra("dummyFlag", true);
        Intent intent = createIntentClipDataIntent("ラベル", clipDataIntent);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "Intentテキスト\nDummyテキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみHTML形式NULL() {
        // 準備
        Intent intent = createHtmlIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみHTML形式ブランク() {
        // 準備
        Intent intent = createHtmlIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみHTML形式値あり() {
        // 準備
        Intent intent = createHtmlIntent("ラベル", "テキスト");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "ラベル\nテキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみTEXT形式NULL() {
        // 準備
        Intent intent = createTextIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみTEXT形式ブランク() {
        // 準備
        Intent intent = createTextIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみTEXT形式値あり() {
        // 準備
        Intent intent = createTextIntent("ラベル", "テキスト");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "ラベル\nテキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみURI形式NULL() {
        // Uri.parseメソッドで、NullPointerExceptionが発生するため、テスト不可。
//        // 準備
//        Intent intent = createUriClipDataIntent(null, null);
//        sut.setIntent(intent);
//
//        // テスト実行
//        String actual = sut.getShortIntentStr();
//
//        // 検証
//        String expected = "テキストデータがありません。";
//        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみURI形式ブランク() {
        // 準備
        Intent intent = createUriIntent("", "");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみURI形式値あり() {
        // 準備
        Intent intent = createUriIntent("ラベル", "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper\nラベル";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみIntent形式NULL() {
        // 準備
        Intent intent = createIntentIntent(null, null);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみIntent形式ブランク() {
        // 準備
        Intent intent = createIntentIntent("", new Intent());
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_ExtraのみIntent形式値あり() {
        // 準備
        Intent extraIntent = new Intent(getContext(), RecvActivity.class);
        extraIntent.setAction(Intent.ACTION_SEND);
        extraIntent.addCategory(Intent.CATEGORY_TEST);
        extraIntent.putExtra(Intent.EXTRA_TEXT, "テキスト");
        extraIntent.putExtra("dummy", true);
        Intent intent = createIntentIntent("ラベル", extraIntent);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキスト\nラベル";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_Extra混在() {
        // 準備
        Intent extraIntent = new Intent(getContext(), RecvActivity.class);
        extraIntent.setAction(Intent.ACTION_SEND);
        extraIntent.addCategory(Intent.CATEGORY_TEST);
        extraIntent.putExtra(Intent.EXTRA_TEXT, "Intentテキスト");
        extraIntent.putExtra("dummy", true);
        Intent intent = new Intent();
        intent = setHtml(intent, "Extraラベル", "ExtraHtmlテキスト");
        intent = setText(intent, "Extraラベル", "ExtraTextテキスト");
        intent = setUri(intent, "Extraラベル", "ExtraUriテキスト");
        intent = setIntent(intent, "Extraラベル", extraIntent);
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "Intentテキスト\nExtraUriテキスト\nExtraラベル\nExtraTextテキスト";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_DataのみNULL() {
        // Uri.parseメソッドで、NullPointerExceptionが発生するため、テスト不可。
//        // 準備
//        Intent intent = createDataIntent(null);
//        sut.setIntent(intent);
//
//        // テスト実行
//        String actual = sut.getShortIntentStr();
//
//        // 検証
//        String expected = "テキストデータがありません。";
//        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_Dataのみブランク() {
        // 準備
        Intent intent = createDataIntent("");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "テキストデータがありません。";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_Dataのみ値あり() {
        // 準備
        Intent intent = createDataIntent("https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        String expected = "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper";
        assertEquals(expected, actual);
    }

    public void testGetShortIntentStr_各種混在() {
        // 準備
        Intent intent = new Intent();
        intent = setText(intent, "Extraラベル", "Extraテキスト");
        intent = setTextClipData(intent, "ClipDataラベル", "ClipDataテキスト");
        intent = setData(intent, "https://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper");
        sut.setIntent(intent);

        // テスト実行
        String actual = sut.getShortIntentStr();

        // 検証
        // クリップボード優先
        String expected = "ClipDataテキスト\nExtraラベル\nhttps://play.google.com/store/apps/details?id=com.kurukurupapa.appsharehelper";
        assertEquals(expected, actual);
    }

    private Intent createHtmlClipDataIntent(String label, String text) {
        Intent intent = new Intent();
        return setHtmlClipData(intent, label, text);
    }

    private Intent createTextClipDataIntent(String label, String text) {
        Intent intent = new Intent();
        return setTextClipData(intent, label, text);
    }

    private Intent createUriClipDataIntent(String label, String uri) {
        Intent intent = new Intent();
        return setUriClipData(intent, label, uri);
    }

    private Intent createIntentClipDataIntent(String label, Intent clipDataIntent) {
        Intent intent = new Intent();
        return setIntentClipData(intent, label, clipDataIntent);
    }

    private Intent createHtmlIntent(String label, String text) {
        return setHtml(new Intent(), label, text);
    }

    private Intent createTextIntent(String label, String text) {
        return setText(new Intent(), label, text);
    }

    private Intent createUriIntent(String label, String uri) {
        return setUri(new Intent(), label, uri);
    }

    private Intent createIntentIntent(String label, Intent extraIntent) {
        return setIntent(new Intent(), label, extraIntent);
    }

    private Intent createDataIntent(String uri) {
        return setData(new Intent(), uri);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Intent setHtmlClipData(Intent intent, String label, String text) {
        ClipData clipData = ClipData.newHtmlText(label, text, "<p>" + text + "</p>");
        intent.setClipData(clipData);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Intent setTextClipData(Intent intent, String label, String text) {
        ClipData clipData = ClipData.newPlainText(label, text);
        intent.setClipData(clipData);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Intent setUriClipData(Intent intent, String label, String uri) {
        ClipData clipData = ClipData.newRawUri(label, Uri.parse(uri));
        intent.setClipData(clipData);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Intent setIntentClipData(Intent intent, String label, Intent clipDataIntent) {
        ClipData clipData = ClipData.newIntent(label, clipDataIntent);
        intent.setClipData(clipData);
        return intent;
    }

    private Intent setHtml(Intent intent, String label, String text) {
        intent.putExtra(Intent.EXTRA_SUBJECT, label);
        intent.putExtra(Intent.EXTRA_HTML_TEXT, "<p>" + text + "</p>");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    private Intent setText(Intent intent, String label, String text) {
        intent.putExtra(Intent.EXTRA_SUBJECT, label);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    private Intent setUri(Intent intent, String label, String uri) {
        intent.putExtra(Intent.EXTRA_SUBJECT, label);
        intent.putExtra(Intent.EXTRA_ORIGINATING_URI, Uri.parse(uri));
        return intent;
    }

    private Intent setIntent(Intent intent, String label, Intent extraIntent) {
        intent.putExtra(Intent.EXTRA_SUBJECT, label);
        intent.putExtra(Intent.EXTRA_INTENT, extraIntent);
        return intent;
    }

    private Intent setData(Intent intent, String uri) {
        intent.setData(Uri.parse(uri));
        return intent;
    }

}
