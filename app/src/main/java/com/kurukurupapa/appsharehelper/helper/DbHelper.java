package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.ShareHistory;

/**
 * データベース操作のヘルパークラス
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_FILE_NAME = "db";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ShareActivity.TABLE_NAME + " (" +
            ShareActivity._ID + " integer primary key autoincrement, " +
            ShareActivity.COLUMN_SRC_PACKAGE + " text, " +
            ShareActivity.COLUMN_ACTION + " text, " +
            ShareActivity.COLUMN_TYPE + " text, " +
            ShareActivity.COLUMN_DEST_PACKAGE + " text, " +
            ShareActivity.COLUMN_DEST_ACTIVITY + " text, " +
            ShareActivity.COLUMN_TIMESTAMP + " integer not null, " +
            ShareActivity.COLUMN_TIMESTAMP1 + " integer, " +
            ShareActivity.COLUMN_TIMESTAMP2 + " integer, " +
            ShareActivity.COLUMN_TIMESTAMP3 + " integer, " +
            ShareActivity.COLUMN_STANDARD_FLAG + " integer not null" +
            ")"
        );
        db.execSQL("create table " + ShareHistory.TABLE_NAME + " (" +
            ShareHistory._ID + " integer primary key autoincrement, " +
            ShareHistory.COLUMN_TIMESTAMP + " integer not null, " +
            ShareHistory.COLUMN_SHARE_ACTIVITY_ID + " integer not null, " +
            ShareHistory.COLUMN_CONTENT + " text " +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
