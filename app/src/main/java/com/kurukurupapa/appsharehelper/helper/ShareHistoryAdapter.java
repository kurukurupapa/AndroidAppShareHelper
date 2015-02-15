package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.ShareHistory;
import com.kurukurupapa.appsharehelper.service.ShareActivityCacheService;

import java.sql.Date;

/**
 * 共有履歴アダプタークラス
 */
public class ShareHistoryAdapter {
    private Context mContext;
    private ShareActivityCacheService mShareActivityCacheService;
    private ShareHistory mShareHistory;
    private ShareActivityAdapter mShareActivityAdapter;

    public ShareHistoryAdapter(ShareHistory shareHistory, ShareActivityCacheService shareActivityCacheService, Context context) {
        mShareHistory = shareHistory;
        mShareActivityCacheService = shareActivityCacheService;
        mContext = context;
    }

    private ShareActivityAdapter getShareActivity() {
        if (mShareActivityAdapter == null) {
            ShareActivity shareActivity = mShareActivityCacheService.get(mShareHistory.getShareActivityId());
            if (shareActivity != null) {
                mShareActivityAdapter = new ShareActivityAdapter(shareActivity, mContext);
            }
        }
        return mShareActivityAdapter;
    }

    public String getAction() {
        String action = null;
        if (getShareActivity() != null) {
            action = getShareActivity().getAction();
        }
        return action;
    }

    public String getTypeForDisplay() {
        String type = null;
        if (getShareActivity() != null) {
            type = getShareActivity().getTypeForDisplay();
        }
        return type;
    }

    public Drawable loadSrcPackageIcon() {
        Drawable icon;
        if (getShareActivity() != null) {
            icon = getShareActivity().loadSrcPackageIcon();
        } else {
            icon = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return icon;
    }

    public CharSequence loadSrcPackageLabel() {
        CharSequence label;
        if (getShareActivity() != null) {
            label = getShareActivity().loadSrcPackageLabel();
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    public Drawable loadDestActivityIcon() {
        Drawable icon;
        if (getShareActivity() != null) {
            icon = getShareActivity().loadDestActivityIcon();
        } else {
            icon = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return icon;
    }

    public CharSequence loadDestActivityLabel() {
        CharSequence label;
        if (getShareActivity() != null) {
            label = getShareActivity().loadDestActivityLabel();
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    public String getShortContent() {
        return mShareHistory.getContent();
    }

    public String getLongContent() {
        return getAction() + "\n" + getTypeForDisplay() + "\n" + mShareHistory.getContent();
    }

    public String getTimestampStr() {
        Date date = new Date(mShareHistory.getTimestamp());
        return DateFormat.getDateFormat(mContext).format(date) + " " + DateFormat.getTimeFormat(mContext).format(date);
    }

}
