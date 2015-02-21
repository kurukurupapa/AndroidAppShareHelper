package com.kurukurupapa.appsharehelper.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.kurukurupapa.appsharehelper.activity.ClipActivity;
import com.kurukurupapa.appsharehelper.helper.ShareActivityAdapter;
import com.kurukurupapa.appsharehelper.model.ShareActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 共有アクティビティサービスクラス
 *
 * 共有アクティビティデータにアクセスするサービスです。
 */
public class ShareActivityService {
    private static final String TAG = ShareActivityService.class.getSimpleName();

    private Context mContext;
    private PackageManager mPackageManager;
    private ShareActivityTableService mShareActivityTableService;

    public ShareActivityService(Context context, ShareActivityTableService shareActivityTableService) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        mShareActivityTableService = shareActivityTableService;
    }

    /**
     * インテント受信可能なアクティビティ一覧を取得します。
     * @param srcPackage インテント呼び出し元アプリのパッケージ
     * @param srcIntent インテント
     * @return リスト
     */
    public List<ShareActivityAdapter> find(String srcPackage, Intent srcIntent, boolean isText) {
        List<ShareActivityAdapter> resultShareActivityList = new ArrayList<ShareActivityAdapter>();

        // 共有先アクティビティ候補一覧を取得します。
        TreeMap<ComponentName, ShareActivityAdapter> allShareActivities = getAllShareActivities(srcPackage, srcIntent, isText);

        // DBからアクティビティ使用履歴を取得します。
        List<ShareActivity> shareActivityList = mShareActivityTableService.query(srcPackage, srcIntent);

        // 優先順位を考慮しながら結果リストを作成します。
        // 優先１：DB登録されている（過去に使用された）アクティビティを結果リストに追加します。
        for (ShareActivity shareActivity : shareActivityList) {
            String packageName = shareActivity.getDestPackage();
            String activity = shareActivity.getDestActivity();
            ComponentName componentName = new ComponentName(packageName, activity);

            // DB登録されていたアクティビティが、候補一覧に存在することを確認します。
            ShareActivityAdapter targetShareActivity = allShareActivities.get(componentName);
            if (targetShareActivity == null) {
                // DBのアクティビティ一覧に存在し、候補一覧に存在しなかったアクティビティは、アンインストールされたと考えます。
                // DBの該当データは不要なので、削除します。
                mShareActivityTableService.delete(shareActivity);
            } else {
                // 結果リストへ登録します。
                targetShareActivity.setShareActivity(shareActivity);
                resultShareActivityList.add(targetShareActivity);
                allShareActivities.remove(componentName);
            }
        }
        // 優先２：使われたことのないアクティビティを結果リストに追加します。
        resultShareActivityList.addAll(allShareActivities.values());

        return resultShareActivityList;
    }

    private TreeMap<ComponentName, ShareActivityAdapter> getAllShareActivities(String srcPackage, Intent srcIntent, boolean isText) {
        TreeMap<ComponentName, ShareActivityAdapter> shareActivities = new TreeMap<ComponentName, ShareActivityAdapter>();
        shareActivities.putAll(getMyselfShareActivities(srcPackage, srcIntent, isText));
        shareActivities.putAll(getResolveShareActivities(srcPackage, srcIntent));
        return shareActivities;
    }

    private TreeMap<ComponentName, ShareActivityAdapter> getMyselfShareActivities(String srcPackage, Intent srcIntent, boolean isText) {
        TreeMap<ComponentName, ShareActivityAdapter> shareActivities = new TreeMap<ComponentName, ShareActivityAdapter>();

        // 当アプリ内の共有先アクティビティ
        if (isText) {
            ComponentName componentName = new ComponentName(mContext.getPackageName(), ClipActivity.class.getName());
            ShareActivity shareActivity = new ShareActivity(srcPackage, srcIntent, componentName);
            ShareActivityAdapter shareActivityAdapter = new ShareActivityAdapter(shareActivity, mContext, mPackageManager, null, null, srcIntent);
            shareActivities.put(componentName, shareActivityAdapter);
        }

        return shareActivities;
    }

    private TreeMap<ComponentName, ShareActivityAdapter> getResolveShareActivities(String srcPackage, Intent srcIntent) {
        TreeMap<ComponentName, ShareActivityAdapter> shareActivities = new TreeMap<ComponentName, ShareActivityAdapter>();

        ArrayList<Intent> intentList = new ArrayList<Intent>();
        intentList.add(srcIntent);
        IntentConverter intentConverter = new IntentConverter();
        Intent convertedIntent = intentConverter.convert(srcIntent);
        if (convertedIntent != null) {
            intentList.add(convertedIntent);
        }

        // PackageMangerからアクティビティ一覧を取得します。
        for (Intent intent : intentList) {
            List<ResolveInfo> tmpResolveInfoList = getResolveInfoList(intent);
            for (ResolveInfo resolveInfo : tmpResolveInfoList) {
                if (resolveInfo.activityInfo.packageName.equals(mContext.getPackageName())) {
                    // 当アプリのアクティビティは無視します。
                    continue;
                }
                ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (shareActivities.containsKey(componentName)) {
                    continue;
                }
                // DB登録するIntentは、標準アプリ設定を考慮して、共有元アプリが作成した元のインテントとします。
                // 起動用のIntentは、変換後のインテント。
                ShareActivity shareActivity = new ShareActivity(srcPackage, srcIntent, componentName);
                ShareActivityAdapter shareActivityAdapter = new ShareActivityAdapter(shareActivity, mContext, mPackageManager, null, resolveInfo.activityInfo, intent);
                shareActivities.put(componentName, shareActivityAdapter);
            }
        }

        return shareActivities;
    }

    /**
     * インテント受信可能なアクティビティ一覧を取得します。
     * @return ResolveInfoオブジェクトのリスト
     */
    private List<ResolveInfo> getResolveInfoList(Intent srcIntent) {
        ArrayList<String> actions = new ArrayList<String>();
        Intent intent = new Intent(srcIntent);
        intent.setComponent(null);
        List<ResolveInfo> list = mPackageManager.queryIntentActivities(intent, 0);
        Log.d(TAG, "queryIntentActivities結果 件数=" + list.size());
        return list;
    }

    public void updateOrInsert(ShareActivityAdapter shareActivityAdapter) {
        ShareActivity shareActivity = shareActivityAdapter.getShareActivity();

        // 更新日時を更新します。
        shareActivity.modifyTimestamp();

        // DBに保存します。
        if (shareActivity.isNew()) {
            mShareActivityTableService.insert(shareActivity);
        } else {
            mShareActivityTableService.update(shareActivity);
        }
    }

    /**
     * 標準アプリ一覧を検索します。
     * @return 共有アクティビティリスト
     */
    public List<ShareActivity> findStandardApp() {
        return mShareActivityTableService.findStandardApp();
    }

    public void update(ShareActivity shareActivity) {
        mShareActivityTableService.update(shareActivity);
    }

}
