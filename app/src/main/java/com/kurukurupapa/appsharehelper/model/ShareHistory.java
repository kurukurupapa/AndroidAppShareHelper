package com.kurukurupapa.appsharehelper.model;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.text.format.DateFormat;

import java.sql.Date;

/**
 * 共有履歴データ
 *
 * インテントを転送した履歴です。
 */
public class ShareHistory implements BaseColumns {
    public static final String TABLE_NAME = "share_history";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SHARE_ACTIVITY_ID = "share_activity_id";
    public static final String COLUMN_CONTENT = "content";
    public static final int NO_ID = -1;

    /** ID */
    private long mId;
    /** 更新日時 */
    private long mTimestamp;
    /** ShearActivityレコードID */
    private long mShareActivityId;
    /** インテント内容 */
    private String mContent;

    public ShareHistory(ShareActivity shareActivity, String content) {
        this(NO_ID, shareActivity.getTimestamp1(), shareActivity.getId(), content);
    }

    public ShareHistory(long id, long timestamp, long shareActivityId, String content) {
        mId = id;
        mTimestamp = timestamp;
        mShareActivityId = shareActivityId;
        mContent = content;
    }

    public long getId() {
        return mId;
    }
    public void setId(long id) {
        mId = id;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public long getShareActivityId() {
        return mShareActivityId;
    }

    public String getContent() {
        return mContent;
    }
    public void setContent(String content) {
        mContent = content;
    }

    public ContentValues createContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, mTimestamp);
        values.put(COLUMN_SHARE_ACTIVITY_ID, mShareActivityId);
        values.put(COLUMN_CONTENT, mContent);
        return values;
    }
}
