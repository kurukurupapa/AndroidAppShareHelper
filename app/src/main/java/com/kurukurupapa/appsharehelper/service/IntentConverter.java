package com.kurukurupapa.appsharehelper.service;

import android.content.Intent;
import android.net.Uri;

import java.util.*;
import java.util.regex.*;

/**
 * インテントコンバータークラス
 */
public class IntentConverter {
    private static final String MIME_TEXT_PLAIN = "text/plain";

    public Intent convert(Intent srcIntent) {
        Intent destIntent = null;
        if (srcIntent.getAction().equals(Intent.ACTION_SEND)) {
            destIntent = toViewFromSend(srcIntent);
        } else if (srcIntent.getAction().equals(Intent.ACTION_VIEW)) {
            destIntent = toSendFromView(srcIntent);
        }
        return destIntent;
    }

    private Intent toSendFromView(Intent srcIntent) {
        String text = srcIntent.getDataString();
        if (text == null) {
            return null;
        }

        return createSendIntent(srcIntent, text, null);
    }

    private Intent toViewFromSend(Intent srcIntent) {
        Pattern pattern = Pattern.compile("(http|https)://[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);

        if (!MIME_TEXT_PLAIN.equals(srcIntent.getType())) {
            return null;
        }

        String text = srcIntent.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String uriStr = matcher.group();
        Uri uri = Uri.parse(uriStr);

        return createViewIntent(srcIntent, uri);
    }

    private Intent toViewFromSendto(Intent srcIntent) {
        Uri uri = srcIntent.getData();
        if (uri == null) {
            return null;
        }

        return createViewIntent(srcIntent, uri);
    }

    private Intent createSendIntent(Intent srcIntent, String text, String subject) {
        // Typeを判断する材料がない。とりあえず"text/plain"を設定する。
        Intent destIntent = new Intent(srcIntent);
        destIntent.setAction(Intent.ACTION_SEND);
        destIntent.setType(MIME_TEXT_PLAIN);
        destIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (subject != null) {
            destIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        return destIntent;
    }

    private Intent createViewIntent(Intent srcIntent, Uri uri) {
        Intent destIntent = new Intent(srcIntent);
        destIntent.setAction(Intent.ACTION_VIEW);
        destIntent.setData(uri);
        return destIntent;
    }

}
