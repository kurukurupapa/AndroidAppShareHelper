package com.kurukurupapa.appsharehelper.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.activity.MainActivity;

/**
 * 通知（Notification）ヘルパー
 */
public class NotificationHelper {
    private final int ID_INTENT_SENDED = 1;

    private Context mContext;
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public void notifyIntentSended() {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        Notification notification = new Notification.Builder(mContext)
                // ステータスバーのアイコン
                .setSmallIcon(R.drawable.ic_status_bar)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.msg_intent_send))
                .setContentIntent(pendingIntent)
                // タップされたときに消去
                .setAutoCancel(true)
                .build();
        mNotificationManager.notify(ID_INTENT_SENDED, notification);
    }

    public void cancelIntentSended() {
        mNotificationManager.cancel(ID_INTENT_SENDED);
    }
}
