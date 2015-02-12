package com.kurukurupapa.appsharehelper.service;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.activity.RecvActivity;
import com.kurukurupapa.appsharehelper.helper.IntentToStringStyle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * インテント受信に関するサービスクラスです。
 */
public class IntentService {
    private static final String TAG = IntentService.class.getSimpleName();

    private Context mContext;
    private Intent mIntent;
    private boolean intentChangedFlag;
    private PackageManager mPackageManager;
    private ApplicationInfo mSrcAppInfo;

    public IntentService(Context context) {
        mContext = context;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void setIntent(Intent intent) {
        // インテントを保持
        // Componentには自アクティビティが設定されているのでクリアします。
        mIntent = intent;
        mIntent.setComponent(null);
        intentChangedFlag = true;
        Log.d(TAG, "インテント内容=" + ToStringBuilder.reflectionToString(mIntent));
    }

    public boolean isIntentChanged() {
        return intentChangedFlag;
    }

    public void setIntentNoChanged() {
        intentChangedFlag = false;
    }

    public String getShortIntentStr() {
        ArrayList<String> lines = new ArrayList<String>();
        ClipData clipData = getClipData(mIntent);
        if (clipData != null) {
            lines.add(getClipDataStr(clipData));
        }
        if (mIntent.getData() != null) {
            lines.add(mIntent.getDataString());
        }
        if (lines.size() == 0) {
            if (mIntent.getExtras() != null) {
                lines.add(getExtrasStr(mIntent));
            }
        }
        return StringUtils.join(lines, "\n");
    }

    public String getLongIntentStr() {
        return ""
                + ToStringBuilder.reflectionToString(mIntent, new IntentToStringStyle()) + "\n"
                ;
    }

    private String getClipDataStr(ClipData clipData) {
//        String clipDataStr = ToStringBuilder.reflectionToString(clipData, ToStringStyle.SIMPLE_STYLE);
//        Log.d(TAG, "ClipData=" + clipDataStr);
//        return clipDataStr;

        ArrayList<CharSequence> lines = new ArrayList<CharSequence>();
        if (clipData != null) {
//            Log.d(TAG, "ClipData Description=" + ToStringBuilder.reflectionToString(clipData.getDescription()));
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
//                Log.d(TAG, "ClipData Item " + i + "=" + ToStringBuilder.reflectionToString(item));
//                Log.d(TAG, "ClipData Item String " + i + "=" + item.getText());
                lines.add(item.getText());
            }
        }
        return StringUtils.join(lines, "\n");
    }

    /**
     * IntentからClipDataを取得します。
     *
     * Intent#getCliplData()で取得できますが、APIレベル16(Android 4.1)以上のAPIです。
     * APIレベル14(Android 4.0)でも動作させるため、リフレクションで実装します。
     *
     * @param intent
     * @return
     */
    private static ClipData getClipData(Intent intent) {
        // APIレベル16以上
        //ClipData clipData = intent.getClipData();

        ClipData clipData = null;
        try {
            Method method = Intent.class.getMethod("getClipData");
            if (method != null) {
                Object result = method.invoke(intent);
                if (result instanceof ClipData) {
                    clipData = (ClipData) result;
                }
            }
        } catch (NoSuchMethodException e) {
            // APIレベル15以下では当例外が発生
            clipData = null;
        } catch (IllegalAccessException e) {
            Log.w(TAG, "Intent#getClipDataメソッド実行に失敗しました。" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.w(TAG, "Intent#getClipDataメソッド実行に失敗しました。" + e.getMessage());
        }
        return clipData;
    }

    private String getExtrasStr(Intent intent) {
        Bundle extras = intent.getExtras();
        ArrayList<CharSequence> keys = new ArrayList<CharSequence>();
        if (extras != null) {
            Set<String> keySet = extras.keySet();
            for (String key : keySet) {
                keys.add(key);
            }
        }
        return mContext.getString(R.string.label_extra_key) + " " + StringUtils.join(keys, ", ");
    }

    public void findSrcAppInfo() {
        // インテント呼び出し元アプリを取得
        // 「android.permission.GET_TASKS」が必要
        // Androidの「Recent Apps」（最近使ったアプリ）から呼び出された場合は、呼び出し元アプリとしてAndroidシステムを取得します。
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> recentTaskInfoList = activityManager.getRecentTasks(2, ActivityManager.RECENT_WITH_EXCLUDED);

//        // 動作確認用ログ出力
//        Log.d(TAG, "recentTaskInfoList.size()=" + recentTaskInfoList.size());
//        for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfoList) {
//            // 1件目：自アクティビティ
//            // 2件目：呼び出し元アクティビティ
//            String className = recentTaskInfo.baseIntent.getComponent().getClassName().toString();
//            Log.d(TAG, "className=" + className);
//        }

        String packageName;
        if (recentTaskInfoList.size() >= 2) {
            // 自アクティビティにandroid:launchMode="singleInstance"設定されていた場合
            //ComponentName componentName = recentTaskInfoList.get(1).baseIntent.getComponent();
            // 自アクティビティのandroid:launchModeがデフォルト設定の場合
            ComponentName componentName = recentTaskInfoList.get(0).baseIntent.getComponent();
            packageName = componentName.getPackageName();
        } else {
            Log.w(TAG, "インテント呼び出し元のRecentTaskInfoの取得に失敗しました。recentTaskInfoList.size()=" + recentTaskInfoList.size());
            packageName = null;
        }
        setSrcAppInfo(packageName);
    }

    public void setSrcAppInfo(String srcPackageName) {
        // クリア
        mSrcAppInfo = null;

        // インテント呼び出し元アプリを取得
        mPackageManager = mContext.getPackageManager();
        try {
            mSrcAppInfo = mPackageManager.getApplicationInfo(srcPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "インテント呼び出し元のApplicationInfoの取得に失敗しました。srcPackageName=" + srcPackageName, e);
            mSrcAppInfo = null;
        }
        Log.d(TAG, "インテント呼び出し元=" + getSrcPackageName());
    }

    public boolean isValidSrcAppInfo() {
        return mSrcAppInfo != null;
    }

    public String getSrcPackageName() {
        return mSrcAppInfo == null ? null: mSrcAppInfo.packageName;
    }

    public Drawable getSrcAppIcon() {
        return mSrcAppInfo.loadIcon(mPackageManager);
    }

    public CharSequence getSrcAppLabel() {
        return mSrcAppInfo.loadLabel(mPackageManager);
    }

    public Intent createIntent() {
        Intent intent = new Intent(mIntent);
        intent.setComponent(null);
        return intent;
    }

    public PendingIntent createPendingIntent() {
        Intent intent = createIntent();
        intent.setClass(mContext, RecvActivity.class);
        return PendingIntent.getActivity(mContext, 0, intent, 0);
    }

}
