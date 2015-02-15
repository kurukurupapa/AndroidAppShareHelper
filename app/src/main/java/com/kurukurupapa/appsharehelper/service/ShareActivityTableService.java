package com.kurukurupapa.appsharehelper.service;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kurukurupapa.appsharehelper.helper.DbHelper;
import com.kurukurupapa.appsharehelper.model.ShareActivity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 共有アクティビティサービスクラス
 *
 * 共有アクティビティデータにアクセスするサービスです。
 */
public class ShareActivityTableService {
    private static final String TAG = ShareActivityTableService.class.getSimpleName();

    private DbHelper mDbHelper;

    public ShareActivityTableService(DbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    public ShareActivity findByIdOrNull(long shareActivityId) {
        List<ShareActivity> list = query(ShareActivity._ID + "=" + shareActivityId, ShareActivity._ID);
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

    public ShareActivity findByIdOrThrow(long shareActivityId) {
        List<ShareActivity> list = query(ShareActivity._ID + "=" + shareActivityId, ShareActivity._ID);
        if (list.size() != 1) {
            throw new RuntimeException("ShareActivityデータの検索に失敗しました。count=" + list.size());
        }
        return list.get(0);
    }

    public void updateOrInsert(ShareActivity shareActivity) {
        // 更新日時を更新します。
        shareActivity.modifyTimestamp();

        // DBに保存します。
        if (shareActivity.isNew()) {
            insert(shareActivity);
        } else {
            update(shareActivity);
        }
    }

    /**
     * 標準アプリ一覧を検索します。
     * @return 共有アクティビティリスト
     */
    public List<ShareActivity> findStandardApp() {
        return query(ShareActivity.COLUMN_STANDARD_FLAG + "='1'",
                ShareActivity.COLUMN_SRC_PACKAGE + "," +
                ShareActivity.COLUMN_DEST_PACKAGE +  "," + ShareActivity.COLUMN_DEST_ACTIVITY + "," +
                ShareActivity.COLUMN_ACTION + "," + ShareActivity.COLUMN_TYPE + "," +
                ShareActivity._ID);
    }

    /**
     * 共有内容に応じた共有アクティビティデータを検索します。
     * @param srcPackage 共有元アプリ
     * @param srcIntent インテント
     * @return 共有アクティビティリスト
     */
    public List<ShareActivity> query(String srcPackage, Intent srcIntent) {
        return query(
                ShareActivity.COLUMN_SRC_PACKAGE + "='" + srcPackage + "' and " +
                ShareActivity.COLUMN_ACTION + "='" + srcIntent.getAction() + "' and " +
                ShareActivity.COLUMN_TYPE + (srcIntent.getType() == null ? " is null" : "='" + srcIntent.getType() + "'"),
                ShareActivity.COLUMN_TIMESTAMP + " desc," + ShareActivity._ID);
    }

    private List<ShareActivity> query(String selection, String orderBy) {
        List<ShareActivity> list = new ArrayList<ShareActivity>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            Log.d(TAG, "ShareActivityテーブル検索 selection=[" + selection + "]");
            Cursor cursor = db.query(ShareActivity.TABLE_NAME, null, selection,
                    null, null, null, orderBy);
            Log.d(TAG, "ShareActivityテーブル検索結果 件数=" + cursor.getCount());
            boolean loop = cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(ShareActivity._ID);
            int srcPackageIndex = cursor.getColumnIndex(ShareActivity.COLUMN_SRC_PACKAGE);
            int actionIndex = cursor.getColumnIndex(ShareActivity.COLUMN_ACTION);
            int typeIndex = cursor.getColumnIndex(ShareActivity.COLUMN_TYPE);
            int destPackageIndex = cursor.getColumnIndex(ShareActivity.COLUMN_DEST_PACKAGE);
            int destActivityIndex = cursor.getColumnIndex(ShareActivity.COLUMN_DEST_ACTIVITY);
            int timestampIndex = cursor.getColumnIndex(ShareActivity.COLUMN_TIMESTAMP);
            int timestamp1Index = cursor.getColumnIndex(ShareActivity.COLUMN_TIMESTAMP1);
            int timestamp2Index = cursor.getColumnIndex(ShareActivity.COLUMN_TIMESTAMP2);
            int timestamp3Index = cursor.getColumnIndex(ShareActivity.COLUMN_TIMESTAMP3);
            int standardFlagIndex = cursor.getColumnIndex(ShareActivity.COLUMN_STANDARD_FLAG);
            while (loop) {
                ShareActivity shareActivity = new ShareActivity(
                        cursor.getLong(idIndex),
                        cursor.getString(srcPackageIndex),
                        cursor.getString(actionIndex),
                        cursor.getString(typeIndex),
                        cursor.getString(destPackageIndex),
                        cursor.getString(destActivityIndex),
                        cursor.getLong(timestampIndex),
                        cursor.getLong(timestamp1Index),
                        cursor.getLong(timestamp2Index),
                        cursor.getLong(timestamp3Index),
                        cursor.getInt(standardFlagIndex) == 1
                );
                list.add(shareActivity);
                loop = cursor.moveToNext();
            }
        }
        finally {
            db.close();
        }
        return list;
    }

    public void insert(ShareActivity shareActivity) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = shareActivity.createContentValues();
            Log.d(TAG, "ShareActivityテーブル登録 values=[" + ToStringBuilder.reflectionToString(values, ToStringStyle.SHORT_PREFIX_STYLE) + "]");
            long id = db.insert(ShareActivity.TABLE_NAME, null, values);
            Log.d(TAG, "ShareActivityテーブル登録結果 id=" + id);
            if (id < 0) {
                throw new RuntimeException("ShareActivityレコード登録に失敗しました。");
            }
            shareActivity.setId(id);
        }
        finally {
            db.close();
        }
    }

    public void update(ShareActivity shareActivity) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = shareActivity.createContentValues();
            Log.d(TAG, "ShareActivityテーブル更新 values=[" + ToStringBuilder.reflectionToString(values, ToStringStyle.SHORT_PREFIX_STYLE) + "]");
            int count = db.update(ShareActivity.TABLE_NAME, values, ShareActivity._ID + "=" + shareActivity.getId(), null);
            Log.d(TAG, "ShareActivityテーブル更新結果 count=" + count);
            if (count != 1) {
                throw new RuntimeException("ShareActivityレコード更新に失敗しました。count=" + count);
            }
        }
        finally {
            db.close();
        }
    }

    public void delete(ShareActivity shareActivity) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            Log.d(TAG, "ShareActivityテーブル削除 shareActivity=[" + ToStringBuilder.reflectionToString(shareActivity, ToStringStyle.SHORT_PREFIX_STYLE) + "]");
            int count = db.delete(ShareActivity.TABLE_NAME, ShareActivity._ID + "=" + shareActivity.getId(), null);
            Log.d(TAG, "ShareActivityテーブル削除結果 count=" + count);
            if (count != 1) {
                throw new RuntimeException("ShareActivityレコード削除に失敗しました。count=" + count);
            }
        }
        finally {
            db.close();
        }
    }

}
