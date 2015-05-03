package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 設定データにアクセスするヘルパークラスです。
 */
public class PreferenceHelper {
    private Context mContext;

    /**
     * 通知フラグを取得します。
     *
     * @param context コンテキスト
     * @return フラグ
     */
    public static boolean getNotificationFlag(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("notification_flag", true);
    }

    /**
     * クリップボード共有通知フラグを取得します。
     *
     * @param context コンテキスト
     * @return フラグ
     */
    public static boolean getClipboardFlag(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("clipboard_flag", true);
    }

    /**
     * 開発者向け表示フラグを取得します。
     *
     * @param context コンテキスト
     * @return フラグ
     */
    public static boolean getDeveloperFlag(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("developer_flag", false);
    }

    public PreferenceHelper(Context context) {
        mContext = context;
    }

    public boolean getNotificationFlag() {
        return getNotificationFlag(mContext);
    }

    public boolean getDeveloperFlag() {
        return getDeveloperFlag(mContext);
    }
}
