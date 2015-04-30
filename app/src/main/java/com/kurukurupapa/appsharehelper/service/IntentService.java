package com.kurukurupapa.appsharehelper.service;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.activity.RecvActivity;
import com.kurukurupapa.appsharehelper.helper.IntentToStringStyle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * インテント受信に関するサービスクラスです。
 */
public class IntentService {
    /** 当アプリから当アプリ内アクティビティを呼び出すときに設定するインテントのExtraキー */
    public static final String EXTRA_MY_APP = "com.kurukurupapa.appsharehelper.intent.extra.MY_APP";

    private static final String TAG = IntentService.class.getSimpleName();
    private static final String ANDROID_SYSTEM_PACKAGE_NAME = "android";

    /** ClipDataに対応するIntentのExtraキーリスト */
    private static final String[] IGNORE_EXTRA_KEYS = new String[]{
            Intent.EXTRA_HTML_TEXT,
            Intent.EXTRA_TEXT,
            Intent.EXTRA_ORIGINATING_URI,
            Intent.EXTRA_INTENT,
    };

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

    private void initShortIntentStr() {
        mShortIntentStr = null;
        mTextFlag = false;

        try {
            mShortIntentStr = getShortIntentStr(mIntent);
            if (mShortIntentStr == null) {
                // テキストが見つからなかった場合のメッセージ
                mShortIntentStr = mContext.getString(R.string.msg_no_intent_text);
            } else {
                mTextFlag = true;
            }
        } catch (Exception e) {
            Log.w(TAG, "共有内容の解析に失敗しました。", e);
            mShortIntentStr = mContext.getString(R.string.msg_err_intent_str);
        }
    }

    private static String getShortIntentStr(Intent intent) {
        String str = null;
        boolean clipDataFlag = false;
        ArrayList<String> lines = new ArrayList<String>();
        for (String item : getClipDataShortStrList(intent)) {
            if (!lines.contains(item)) {
                lines.add(item);
                clipDataFlag = true;
            }
        }
        for (String item : getExtrasStrList(intent, clipDataFlag)) {
            if (!lines.contains(item)) {
                lines.add(item);
            }
        }
        if (intent.getData() != null) {
            String tmp = intent.getData().toString();
            if (StringUtils.isNotEmpty(tmp)) {
                lines.add(tmp);
            }
        }
        if (lines.size() > 0) {
            str = StringUtils.join(lines, "\n");
        }
        return str;
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

    private static ArrayList<String> getClipDataShortStrList(Intent intent) {
        return getClipDataShortStrList(getClipData(intent));
    }

    private static ArrayList<String> getClipDataShortStrList(ClipData clipData) {
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

                // 優先順位を考慮して、文字列化します。
                // HTMLが設定されている場合、必ずTEXTが設定されているため、HTMLは見ていません。
                String value = null;
                if (item.getText() != null) {
                    value = item.getText().toString();
                } else if (item.getUri() != null) {
                    value = item.getUri().toString();
                } else if (item.getIntent() != null) {
                    value = getShortIntentStr(item.getIntent());
                }
                if (!StringUtils.isEmpty(value)) {
                    lines.add(value);
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

    private static ArrayList<String> getExtrasStrList(Intent intent, boolean clipDataFlag) {
        ArrayList<String> items = new ArrayList<String>();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : new TreeSet<String>(extras.keySet())) {

                // 既にClipDataから文字列化している項目はスキップします。
                if (clipDataFlag) {
                    if (ArrayUtils.contains(IGNORE_EXTRA_KEYS, key)) {
                        continue;
                    }
                }

                // Intent.EXTRA_HTML_TEXTが存在する場合、Intent.EXTRA_TEXTも存在し、
                // Intent.EXTRA_TEXTを優先する。
                if (key.equals(Intent.EXTRA_HTML_TEXT)) {
                    continue;
                }

                Object value = extras.get(key);
                if (value instanceof String && StringUtils.isNotEmpty(value.toString())) {
                    items.add(value.toString());
                } else if (value instanceof Uri) {
                    String str = value.toString();
                    if (StringUtils.isNotEmpty(str)) {
                        items.add(str);
                    }
                } else if (value instanceof Intent) {
                    String str = getShortIntentStr((Intent)value);
                    if (StringUtils.isNotEmpty(str)) {
                        items.add(str);
                    }
                }
            }
        }
        return items;
    }

    /**
     * 共有元アプリの取得が可能であるか判定します。
     * @return 可能の場合true
     */
    public static boolean isValidSrcAppFunction() {
        // Android 5 では、セキュリティが強化されたため、Recent Apps（最近使ったアプリ）で取得できるのは、
        // 自アプリと、ホームの情報のみとなりました。
        // そのため、共有元アプリは取得できません。
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public void findSrcAppInfo() {
        if (isValidSrcAppFunction()) {
            if (mIntent.getBooleanExtra(EXTRA_MY_APP, false)) {
                // 当アプリ内からの呼び出しであると判定
                setSrcAppInfo(mContext.getPackageName());
            } else {
                findSrcAppInfo_Android4();
            }
        } else {
            findSrcAppInfo_Android5();
        }
    }

    private void findSrcAppInfo_Android5() {
        // Android 5 では、共有元アプリの取得できません。
        setSrcAppInfo(null);
    }

    private void findSrcAppInfo_Android4() {
        String packageName = null;
        try {
            // インテント呼び出し元アプリを取得
            // 「android.permission.GET_TASKS」が必要
            // Androidの「Recent Apps」（最近使ったアプリ）から呼び出された場合は、呼び出し元アプリとしてAndroidシステムを取得します。
            // ※Android 5 では、セキュリティが強化されたため、Recent Apps（最近使ったアプリ）で取得できるのは、自アプリと、ホームの情報のみとなりました。
            // 　そのため、共有元アプリは取得できません。
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> recentTaskInfoList = activityManager.getRecentTasks(3, ActivityManager.RECENT_WITH_EXCLUDED);

//            // 動作確認用ログ出力
//            Log.d(TAG, "recentTaskInfoList.size()=" + recentTaskInfoList.size());
//            for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfoList) {
//                // Androidシステムのアプリ選択ダイアログを挟む場合
//                // 1件目：Androidシステム
//                // 2件目：自アクティビティ
//                // 3件目：呼び出し元アクティビティ
//                // Androidシステムのアプリ選択ダイアログを挟まない場合
//                // 1件目：自アクティビティ
//                // 2件目：呼び出し元アクティビティ
//                String tmpPackageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
//                String tmpClassName = recentTaskInfo.baseIntent.getComponent().getClassName();
//                Log.d(TAG, "packageName=" + tmpPackageName + ", className=" + tmpClassName);
//            }

            for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTaskInfoList) {
                String tmpPackageName = null;
                if (recentTaskInfo.baseIntent.getComponent() != null) {
                    tmpPackageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
                }
                if (tmpPackageName == null) {
                    // パッケージ名が取得できない場合は、共有元アプリ不明とします。
                    break;
                }
                // 自アプリとAndroidシステムは対象外とします。
                if (!tmpPackageName.equals(mContext.getPackageName())
                        && !tmpPackageName.equals(ANDROID_SYSTEM_PACKAGE_NAME)) {
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
        if (srcPackageName != null) {
            mPackageManager = mContext.getPackageManager();
            try {
                mSrcAppInfo = mPackageManager.getApplicationInfo(srcPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "インテント呼び出し元のApplicationInfoの取得に失敗しました。srcPackageName=" + srcPackageName, e);
                mSrcAppInfo = null;
            }
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
