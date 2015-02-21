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
import android.os.Build;
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
    private String mShortIntentStr;
    private boolean mTextFlag;

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
        initShortIntentStr();
        Log.d(TAG, "インテント内容=" + ToStringBuilder.reflectionToString(mIntent));
    }

    public boolean isIntentChanged() {
        return intentChangedFlag;
    }

    public void setIntentNoChanged() {
        intentChangedFlag = false;
    }

    public void initShortIntentStr() {
        mShortIntentStr = null;
        mTextFlag = false;

        try {
            ArrayList<String> lines = new ArrayList<String>();
            if (mIntent.getData() != null) {
                lines.add(mIntent.getDataString());
            }
            for (String item : getClipDataShortStrList(mIntent)) {
                if (!lines.contains(item)) {
                    lines.add(item);
                }
            }
            for (String item : getExtrasStrList(mIntent)) {
                if (!lines.contains(item)) {
                    lines.add(item);
                }
            }
            if (lines.size() == 0) {
                // テキストが見つからなかった場合のメッセージ
                mShortIntentStr = mContext.getString(R.string.msg_no_intent_text);
            } else {
                mShortIntentStr = StringUtils.join(lines, "\n");
                mTextFlag = true;
            }
        } catch (Exception e) {
            Log.w(TAG, "共有内容の解析に失敗しました。", e);
            mShortIntentStr = mContext.getString(R.string.msg_err_intent_str);
        }
    }

    public boolean isText() {
        return mTextFlag;
    }

    /**
     * インテントの概要を取得します。一般ユーザ向け。
     * @return 文字列
     */
    public String getShortIntentStr() {
        return mShortIntentStr;
    }

    /**
     * インテントの詳細情報を取得します。開発者向け。
     * @return 文字列
     */
    public String getLongIntentStr() {
        return ""
                + ToStringBuilder.reflectionToString(mIntent, new IntentToStringStyle()) + "\n"
                ;
    }

    private ArrayList<String> getClipDataShortStrList(Intent intent) {
        return getClipDataShortStrList(getClipData(intent));
    }

    private ArrayList<String> getClipDataShortStrList(ClipData clipData) {
//        String clipDataStr = ToStringBuilder.reflectionToString(clipData, ToStringStyle.SIMPLE_STYLE);
//        Log.d(TAG, "ClipData=" + clipDataStr);
//        return clipDataStr;

        ArrayList<String> lines = new ArrayList<String>();
        if (clipData != null) {
//            Log.d(TAG, "ClipData Description=" + ToStringBuilder.reflectionToString(clipData.getDescription()));
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
//                Log.d(TAG, "ClipData Item " + i + "=" + ToStringBuilder.reflectionToString(item));
//                Log.d(TAG, "ClipData Item String " + i + "=" + item.getText());

                // ひとまずtextのみを対象とします。
                // html, uri, intentは無視します。
                if (item.getText() != null) {
                    lines.add(item.getText().toString());
                }
            }
        }
        return lines;
    }

    /**
     * IntentからClipDataを取得します。
     *
     * Intent#getCliplData()で取得できますが、APIレベル16(Android 4.1)以上のAPIです。
     * APIレベル14(Android 4.0)でも動作させるため、リフレクションで実装します。
     *
     * @param intent Intentオブジェクト
     * @return ClipDataオブジェクト
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
            Log.w(TAG, "Intent#getClipDataメソッド実行に失敗しました。", e);
            clipData = null;
        } catch (InvocationTargetException e) {
            Log.w(TAG, "Intent#getClipDataメソッド実行に失敗しました。", e);
            clipData = null;
        }
        return clipData;
    }

    private ArrayList<String> getExtrasStrList(Intent intent) {
        ArrayList<String> items = new ArrayList<String>();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value instanceof String) {
                    items.add(value.toString());
                }
            }
        }
        return items;
    }

    /**
     * 共有元アプリの取得が可能であるか判定します。
     * @return 可能の場合true
     */
    public boolean isValidSrcAppFunction() {
        // Android 5 では、セキュリティが強化されたため、Recent Apps（最近使ったアプリ）で取得できるのは、
        // 自アプリと、ホームの情報のみとなりました。
        // そのため、共有元アプリは取得できません。
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public void findSrcAppInfo() {
        String packageName = null;
        try {
            // インテント呼び出し元アプリを取得
            // 「android.permission.GET_TASKS」が必要
            // Androidの「Recent Apps」（最近使ったアプリ）から呼び出された場合は、呼び出し元アプリとしてAndroidシステムを取得します。
            // ※Android 5 では、セキュリティが強化されたため、Recent Apps（最近使ったアプリ）で取得できるのは、自アプリと、ホームの情報のみとなりました。
            // 　そのため、共有元アプリは取得できません。
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> recentTaskInfoList = activityManager.getRecentTasks(5, ActivityManager.RECENT_WITH_EXCLUDED);

//            // 動作確認用ログ出力
//            Log.d(TAG, "recentTaskInfoList.size()=" + recentTaskInfoList.size());
//            for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfoList) {
//                // 1件目：自アクティビティ
//                // 2件目：呼び出し元アクティビティ
//                String tmpPackageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
//                String tmpClassName = recentTaskInfo.baseIntent.getComponent().getClassName();
//                Log.d(TAG, "packageName=" + tmpPackageName + ", className=" + tmpClassName);
//            }

            // Android 5.0.2 だと自アプリを取得していたので、当ロジック廃止。
//            String packageName;
//            if (recentTaskInfoList.size() >= 2) {
//                // 自アクティビティにandroid:launchMode="singleInstance"設定されていた場合
//                //ComponentName componentName = recentTaskInfoList.get(1).baseIntent.getComponent();
//                // 自アクティビティのandroid:launchModeがデフォルト設定の場合
//                ComponentName componentName = recentTaskInfoList.get(0).baseIntent.getComponent();
//                packageName = componentName.getPackageName();
//            } else {
//                Log.w(TAG, "インテント呼び出し元のRecentTaskInfoの取得に失敗しました。recentTaskInfoList.size()=" + recentTaskInfoList.size());
//                packageName = null;
//            }

            for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfoList) {
                String tmpPackageName = null;
                if (recentTaskInfo.baseIntent.getComponent() != null) {
                    tmpPackageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
                }
                if (tmpPackageName == null) {
                    tmpPackageName = recentTaskInfo.baseIntent.getPackage();
                }
                if (tmpPackageName == null) {
                    // パッケージ名が取得できない場合は、共有元アプリ不明とします。
                    break;
                }
                if (!tmpPackageName.equals(mContext.getPackageName())) {
                    packageName = tmpPackageName;
                    break;
                }
            }
            if (packageName == null) {
                Log.w(TAG, "インテント呼び出し元のPackageNameの取得に失敗しました。recentTaskInfoList.size()=" + recentTaskInfoList.size());
            }
        } catch (Exception e) {
            Log.w(TAG, "インテント呼び出し元のPackageNameの取得に失敗しました。", e);
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

    /**
     * 取得した共有元アプリが有効であるか判定します。
     * @return 有効な場合true
     */
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
