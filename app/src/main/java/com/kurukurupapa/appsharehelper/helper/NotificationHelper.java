package com.kurukurupapa.appsharehelper.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.activity.MainActivity;
import com.kurukurupapa.appsharehelper.activity.RecvActivity;

/**
 * 通知（Notification）ヘルパー
 */
public class NotificationHelper {
    private static final int ID_INTENT_SENDED = 1;
    private static final int ID_CLIPBOARD = 2;

    private Context mContext;

    public NotificationHelper(Context context) {
        mContext = context;
    }

    /**
     * データ共有したことを通知します。
     */
    public void notifyIntentSendedIfNeed() {
        if (PreferenceHelper.getNotificationFlag(mContext)) {
            notifyIntentSended();
        }
    }

    /**
     * データ共有したことを通知します。
     */
    public void notifyIntentSended() {
        // タップ時に起動するインテント
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.msg_notification_shared))
                .setSmallIcon(R.drawable.ic_status_bar)
                        // 通知直後のメッセージ
                .setTicker(mContext.getString(R.string.msg_notification_shared))
                        // タップ時に起動するインテント
                .setContentIntent(pendingIntent)
                        // タップされたときに消去
                .setAutoCancel(true);

        // 通知領域に登録
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            nm.notify(ID_INTENT_SENDED, builder.getNotification());
        } else {
            nm.notify(ID_INTENT_SENDED, builder.build());
        }
    }

    public void cancelIntentSended() {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ID_INTENT_SENDED);
    }

    /**
     * クリップボード共有通知を再表示します。
     */
    public void renotifyClipboardIfNeed() {
        cancelClipboard();
        notifyClipboardIfNeed();
    }

    /**
     * クリップボード共有通知を表示します。
     */
    public void notifyClipboardIfNeed() {
        if (PreferenceHelper.getClipboardFlag(mContext)) {
            notifyClipboard();
        }
    }

    /**
     * クリップボード共有通知を表示します。
     */
    public void notifyClipboard() {
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.msg_notification_clipboard))
                .setSmallIcon(R.drawable.ic_status_bar)
                        // 通知直後のメッセージ
                .setTicker(mContext.getString(R.string.msg_notification_clipboard))
                        // タップされても消去しない
                .setOngoing(true);

        // タップ時に起動するインテントを設定します。
        Intent intent = RecvActivity.createIntentForClipboard(mContext);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        // 通知領域に登録します。
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            nm.notify(ID_CLIPBOARD, builder.getNotification());
        } else {
            nm.notify(ID_CLIPBOARD, builder.build());
        }
    }

    /**
     * クリップボード通知を削除します。
     */
    public void cancelClipboard() {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ID_CLIPBOARD);
    }

}
