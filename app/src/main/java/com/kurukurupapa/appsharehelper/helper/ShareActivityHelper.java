package com.kurukurupapa.appsharehelper.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.model.ShareActivity;

import org.apache.commons.lang3.StringUtils;

/**
 * 共有アクティビティヘルパークラス
 *
 * 共有アクティビティデータを元に、UI関連の操作を行います。
 */
public class ShareActivityHelper {
    private static final String TAG = ShareActivityHelper.class.getSimpleName();

    private ShareActivity mShareActivity;
    private Context mContext;
    private PackageManager mPackageManager;
    private ApplicationInfo mSrcAppInfo;
    private ActivityInfo mDestActivityInfo;

    public ShareActivityHelper(ShareActivity shareActivity, Context context) {
        mShareActivity = shareActivity;
        mContext = context;
        mPackageManager = context.getPackageManager();

        try {
            mSrcAppInfo = mPackageManager.getApplicationInfo(mShareActivity.getSrcPackage(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "インテント送信元アプリのApplicationInfo取得に失敗しました。" + e.getMessage());
        }

        try {
            mDestActivityInfo = mPackageManager.getActivityInfo(
                    new ComponentName(mShareActivity.getDestPackage(), mShareActivity.getDestActivity()), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "インテント送信先アプリのActivityInfo取得に失敗しました。" + e.getMessage());
        }
    }

    public Drawable loadSrcPackageIcon() {
        Drawable drawable;
        if (mSrcAppInfo != null) {
            drawable = mSrcAppInfo.loadIcon(mPackageManager);
        } else {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return drawable;
    }

    public CharSequence loadSrcPackageLabel() {
        CharSequence label;
        if (mSrcAppInfo != null) {
            label = mSrcAppInfo.loadLabel(mPackageManager);
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    public Drawable loadDestActivityIcon() {
        Drawable drawable;
        if (mDestActivityInfo != null) {
            drawable = mDestActivityInfo.loadIcon(mPackageManager);
        } else {
            drawable = mContext.getResources().getDrawable(R.drawable.ic_unknown);
        }
        return drawable;
    }

    public CharSequence loadDestActivityLabel() {
        CharSequence label;
        if (mDestActivityInfo != null) {
            label = mDestActivityInfo.loadLabel(mPackageManager);
        } else {
            label = mContext.getString(R.string.label_unknown);
        }
        return label;
    }

    public String getTypeForDisplay() {
        return StringUtils.defaultString(mShareActivity.getType(), mContext.getString(R.string.label_empty_intent_type));
    }
}
