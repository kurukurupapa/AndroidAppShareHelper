package com.kurukurupapa.appsharehelper.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kurukurupapa.appsharehelper.helper.DbHelper;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.ShareHistory;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 共有履歴サービスクラス
 *
 * 共有履歴データにアクセスするサービスクラスです。
 */
public class ShareHistoryService {
    private static final String TAG = ShareHistoryService.class.getSimpleName();
    private static final long ONE_DAY_AS_MILLS = 24 * 60 * 60 * 1000;
    private static final int REMAIN_DAYS = 10;
    private static final long REMAIN_MILLS = REMAIN_DAYS * ONE_DAY_AS_MILLS;

    private Context mContext;
    private DbHelper mDbHelper;
    private ShareActivityService mShareActivityService;

    public ShareHistoryService(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
        mShareActivityService = new ShareActivityService(context);
    }

    /**
     * 共有履歴データ一覧を取得します。
     * @return リスト
     */
    public List<ShareHistory> query() {
        List<ShareHistory> list = new ArrayList<ShareHistory>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            String selection = null;
            Log.d(TAG, "ShareHistoryテーブル検索 selection=[" + selection + "]");
            Cursor cursor = db.query(ShareHistory.TABLE_NAME, null, selection,
                    null, null, null, ShareHistory.COLUMN_TIMESTAMP + " desc, _id desc");
            Log.d(TAG, "ShareHistoryテーブル検索結果 件数=" + cursor.getCount());
            int idIndex = cursor.getColumnIndex(ShareHistory._ID);
            int timestampIndex = cursor.getColumnIndex(ShareHistory.COLUMN_TIMESTAMP);
            int shareActivityIdIndex = cursor.getColumnIndex(ShareHistory.COLUMN_SHARE_ACTIVITY_ID);
            int contentIndex = cursor.getColumnIndex(ShareHistory.COLUMN_CONTENT);
            boolean loop = cursor.moveToFirst();
            while (loop) {
                try {
                    ShareHistory shareHistory = new ShareHistory(
                            cursor.getLong(idIndex),
                            cursor.getLong(timestampIndex),
                            cursor.getLong(shareActivityIdIndex),
                            cursor.getString(contentIndex)
                    );

                    // 共有アクティビティデータの紐づけ
                    ShareActivity shareActivity = mShareActivityService.findById(shareHistory.getShareActivityId());
                    shareHistory.setShareActivity(shareActivity);

                    // リストへ追加
                    list.add(shareHistory);
                }
                catch (Exception e) {
                    Log.w(TAG, "ShareHistory,ShareActivityテーブルの検索に失敗しました。", e);
                }

                // 次のループへ
                loop = cursor.moveToNext();
            }
        }
        finally {
            db.close();
        }
        return list;
    }

    public void insert(ShareHistory shareHistory) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = shareHistory.createContentValues();
            Log.d(TAG, "ShareHistoryテーブル登録 values=[" + ToStringBuilder.reflectionToString(values, ToStringStyle.SHORT_PREFIX_STYLE) + "]");
            long id = db.insert(ShareHistory.TABLE_NAME, null, values);
            Log.d(TAG, "ShareHistoryテーブル登録結果 id=" + id);
            if (id < 0) {
                throw new RuntimeException("ShareHistoryレコード登録に失敗しました。");
            }
            shareHistory.setId(id);
        }
        finally {
            db.close();
        }
    }

    /**
     * 過去データを削除します。
     */
    public void deleteOld() {
        deleteOld(REMAIN_MILLS);
    }

    /**
     * 過去データを削除します。
     *
     * @param remainMills 残す時間（ミリ秒）
     */
    public void deleteOld(long remainMills) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            long remainTimestamp = System.currentTimeMillis() - remainMills;
            Log.d(TAG, "ShareHistoryテーブル削除 remainMills=[" + remainMills + "]");
            int count = db.delete(ShareHistory.TABLE_NAME, ShareHistory.COLUMN_TIMESTAMP + "<" + remainTimestamp, null);
            Log.d(TAG, "ShareHistoryテーブル削除結果 count=" + count);
        }
        finally {
            db.close();
        }
    }
}
