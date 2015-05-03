package com.kurukurupapa.appsharehelper.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kurukurupapa.appsharehelper.helper.NotificationHelper;

/**
 * 端末起動レシーバクラス。
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called. action=" + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            new NotificationHelper(context).notifyClipboardIfNeed();
        }
    }
}
