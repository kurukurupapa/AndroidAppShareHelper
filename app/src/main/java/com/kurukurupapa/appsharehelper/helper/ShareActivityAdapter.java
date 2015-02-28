package com.kurukurupapa.appsharehelper.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Log;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.model.ShareActivity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.sql.Date;

/**
 * 共有アクティビティアダプタークラス
 *
 * 共有アクティビティデータを元に、UI関連のデータと操作を追加しています。
 */
public class ShareActivityAdapter {
    private static final String TAG = ShareActivityAdapter.class.getSimpleName();

    private ShareActivity mShareActivity;
    private Context mContext;
    private PackageManager mPackageManager;
    private ApplicationInfo mSrcAppInfo;
    private ActivityInfo mDestActivityInfo;
    private Intent mStartIntent;

    public ShareActivityAdapter(
            ShareActivity shareActivity, Context context) {
        this(shareActivity, context, context.getPackageManager(), null, null, null);
    }

    public ShareActivityAdapter(
            ShareActivity shareActivity, Context context, PackageManager packageManager,
            ApplicationInfo srcAppInfo, ActivityInfo destActivityInfo, Intent startIntent) {
        mShareActivity = shareActivity;
        mContext = context;
        mPackageManager = packageManager;
        mSrcAppInfo = srcAppInfo;
        mDestActivityInfo = destActivityInfo;
        mStartIntent = startIntent;
    }

    public ShareActivity getShareActivity() {
        return mShareActivity;
    }
    public void setShareActivity(ShareActivity shareActivity) {
        if ((shareActivity.getSrcPackage() == null && mShareActivity.getSrcPackage() != null) ||
                (shareActivity.getSrcPackage() != null && !shareActivity.getSrcPackage().equals(mShareActivity.getSrcPackage())) ||
                !shareActivity.getDestPackage().equals(mShareActivity.getDestPackage()) ||
                !shareActivity.getDestActivity().equals(mShareActivity.getDestActivity())) {
            throw new RuntimeException("引数が不正です。shareActivity=" + ToStringBuilder.reflectionToString(shareActivity));
        }
        mShareActivity = shareActivity;
    }

    public boolean isNew() {
        return mShareActivity.isNew();
    }

    public String getAction() {
        return mShareActivity.getAction();
    }

    private ApplicationInfo getSrcAppInfo() {
        if (mSrcAppInfo == null) {
            try {
                mSrcAppInfo = mPackageManager.getApplicationInfo(mShareActivity.getSrcPackage(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "インテント送信元アプリのApplicationInfo取得に失敗しました。" + e.getMessage());
            }
        }
        return mSrcAppInfo;
    }

    public Drawable loadSrcPackageIcon() {
        Drawable drawable;
        if (getSrcAppInfo() != null) {
            drawable = getSrcAppInfo().loadIcon(mPackageManager);
        } else {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return drawable;
    }

    public CharSequence loadSrcPackageLabel() {
        CharSequence label;
        if (getSrcAppInfo() != null) {
            label = getSrcAppInfo().loadLabel(mPackageManager);
        } else if(!mShareActivity.isNullSrcPackage()) {
            label = mShareActivity.getSrcPackage();
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    private ActivityInfo getDestActivityInfo() {
        if (mDestActivityInfo == null) {
            try {
                mDestActivityInfo = mPackageManager.getActivityInfo(
                        new ComponentName(mShareActivity.getDestPackage(), mShareActivity.getDestActivity()), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "インテント送信先アプリのActivityInfo取得に失敗しました。" + e.getMessage());
            }
        }
        return mDestActivityInfo;
    }

    public Drawable loadDestActivityIcon() {
        Drawable drawable;
        if (getDestActivityInfo() != null) {
            drawable = getDestActivityInfo().loadIcon(mPackageManager);
        } else {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return drawable;
    }

    public CharSequence loadDestActivityLabel() {
        CharSequence label;
        if (getDestActivityInfo() != null) {
            label = getDestActivityInfo().loadLabel(mPackageManager);
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    public Intent getStartIntent() {
        return mStartIntent;
    }

    public String getTypeForDisplay() {
        return StringUtils.defaultString(mShareActivity.getType(), mContext.getString(R.string.label_empty_intent_type));
    }

    public boolean getStandardFlag() {
        return getShareActivity().getStandardFlag();
    }
    public void setStandardFlag(boolean standardFlag) {
        getShareActivity().setStandardFlag(standardFlag);
    }

    public String getTimestampStr() {
        String timestampStr;
        long timestamp = mShareActivity.getTimestamp1();
        if (timestamp > 0) {
            Date date = new Date(mShareActivity.getTimestamp1());
            timestampStr = DateFormat.getDateFormat(mContext).format(date) + " " + DateFormat.getTimeFormat(mContext).format(date);
        } else {
            timestampStr = mContext.getString(R.string.label_no_use);
        }
        return timestampStr;
    }

    public Intent createIntent() {
        Intent intent = new Intent(mStartIntent);
        intent.setComponent(new ComponentName(mShareActivity.getDestPackage(), mShareActivity.getDestActivity()));
        return intent;
    }
}
