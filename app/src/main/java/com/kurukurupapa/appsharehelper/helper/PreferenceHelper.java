package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 設定データにアクセスするヘルパークラスです。
 */
public class PreferenceHelper {
    private Context mContext;

    public static boolean getNotificationFlag(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("notification_flag", true);
    }

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
