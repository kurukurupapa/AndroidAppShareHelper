package com.kurukurupapa.appsharehelper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.helper.NotificationHelper;
import com.kurukurupapa.appsharehelper.helper.PreferenceHelper;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.ShareHistory;
import com.kurukurupapa.appsharehelper.service.IntentService;
import com.kurukurupapa.appsharehelper.service.ShareActivityService;
import com.kurukurupapa.appsharehelper.service.ShareHistoryService;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * インテント呼び出しを受信するアクティビティです。
 */
public class RecvActivity extends Activity {
    private static final String TAG = RecvActivity.class.getSimpleName();
    private static final String KEY_SRC_PACKAGE_NAME = "src_package_name";

    private IntentService mIntentService;
    private ShareActivityService mShareActivityService;
    private ShareHistoryService mShareHistoryService;

    private RelativeLayout mRootLayout;
    private Switch mDevSwitch;
    private ImageView mSrcImageView;
    private TextView mSrcNameTextView;
    private TextView mIntentValueTextView;
    private LinearLayout mDestLinearLayout;
    private Switch mStandardSwitch;
    private Animation mActivityShowAnimation;
    private Animation mViewChangeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Called. savedInstanceState=" + savedInstanceState);
        setContentView(R.layout.activity_recv);

        // オブジェクト生成
        mIntentService = new IntentService(this);
        mShareActivityService = new ShareActivityService(this);
        mShareHistoryService = new ShareHistoryService(this);

        // UIオブジェクト取得
        mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        mDevSwitch = (Switch) findViewById(R.id.dev_switch);
        mSrcImageView = (ImageView) findViewById(R.id.src_image_view);
        mSrcNameTextView = (TextView) findViewById(R.id.src_name_text_view);
        mIntentValueTextView = (TextView) findViewById(R.id.intent_value_text_view);
        mDestLinearLayout = (LinearLayout) findViewById(R.id.dest_linear_layout);
        mStandardSwitch = (Switch) findViewById(R.id.standard_switch);
        mActivityShowAnimation = AnimationUtils.loadAnimation(this, R.anim.activity_show);
        mViewChangeAnimation = AnimationUtils.loadAnimation(this, R.anim.view_change);

        // タイトル設定
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.title_activity_recv);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

        // 開発者向けスイッチ
        if (PreferenceHelper.getDeveloperFlag(this)) {
            mDevSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onDevSwitchCheckedChanged(buttonView, isChecked);
                }
            });
        } else {
            mDevSwitch.setVisibility(View.GONE);
        }

        // インテント取得（初回分）
        if (savedInstanceState == null) {
            // 初回表示の場合
            // または、アプリ終了後に「Recent Apps」（最近使ったアプリ）から呼び出された場合
            // ※アプリ終了後に最近使ったアプリから呼び出された場合には、
            //   インテントの送信元アプリを特定することはできません。
            mIntentService.setIntent(getIntent());
            mIntentService.findSrcAppInfo();
        } else {
            // 再表示の場合（画面ローテーションした場合など）
            mIntentService.setIntent(getIntent());
            mIntentService.setSrcAppInfo(savedInstanceState.getString(KEY_SRC_PACKAGE_NAME));
        }
    }

    /**
     * 当アクティビティのオブジェクトが存在する状態で、新しいインテントを受け取った場合の処理です。
     * @param intent 新しいインテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent Called");

        // インテントを保持（2回目以降分）
        mIntentService.setIntent(intent);
        mIntentService.findSrcAppInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Called");

        // インテントが変わっていたら内容を画面表示
        if (mIntentService.isIntentChanged()) {
            showSrcApp();
            showIntent();
            showDestActivity();
            mRootLayout.startAnimation(mActivityShowAnimation);
            mIntentService.setIntentNoChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState Called");

        outState.putString(KEY_SRC_PACKAGE_NAME, mIntentService.getSrcPackageName());
    }

    /**
     * インテント呼び出しのデータを表示
     */
    private void showIntent() {
        setIntentValueTextView(false);
    }

    /**
     * インテント呼び出し元アプリを表示
     */
    private void showSrcApp() {
        if (mIntentService.isValidSrcAppInfo()) {
            PackageManager pm = getPackageManager();
            mSrcImageView.setImageDrawable(mIntentService.getSrcAppIcon());
            mSrcNameTextView.setText(mIntentService.getSrcAppLabel());
        } else {
            mSrcImageView.setImageResource(R.drawable.ic_unknown);
            mSrcNameTextView.setText(getString(R.string.label_unknown));
            Toast.makeText(this, getString(R.string.msg_err_src_app), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * インテント受信可能アクティビティを表示
     */
    private void showDestActivity() {
        // ListViewを初期化します。
        mDestLinearLayout.removeAllViews();

        // アクティビティ一覧を取得します。
        List<ShareActivity> shareActivityList = mShareActivityService.find(mIntentService.getSrcPackageName(), mIntentService.getIntent());

        // ListViewに、アクティビティ表示/起動用のViewを追加します。
        PackageManager pm = this.getPackageManager();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (ShareActivity shareActivity : shareActivityList) {
            View view = layoutInflater.inflate(R.layout.item_activity, null);
            ImageView iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
            TextView labelTextView = (TextView) view.findViewById(R.id.label_text_view);

            iconImageView.setImageDrawable(shareActivity.getResolveInfo().activityInfo.loadIcon(pm));
            labelTextView.setText(shareActivity.getResolveInfo().activityInfo.loadLabel(pm));

            view.setTag(shareActivity);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "View.onClick called");
                    onShareActivityClick(v);
                }
            });
            mDestLinearLayout.addView(view);

            // 標準アプリであれば起動します。
            if (shareActivity.getStandardFlag()) {
                Log.d(TAG, "標準アプリを起動します。shareActivity=[" + ToStringBuilder.reflectionToString(shareActivity) + "]");
                view.callOnClick();
            }
        }
    }

    /**
     * インテント表示スイッチクリック時の処理です。
     *
     * @param buttonView インテントスイッチ
     * @param isChecked チェック有無
     */
    private void onDevSwitchCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setIntentValueTextView(isChecked);
        mIntentValueTextView.startAnimation(mViewChangeAnimation);
    }

    private void setIntentValueTextView(boolean isChecked) {
        if (isChecked) {
            mIntentValueTextView.setText(mIntentService.getLongIntentStr());
        } else {
            mIntentValueTextView.setText(mIntentService.getShortIntentStr());
        }
    }

    /**
     * 共有アクティビティクリック時の処理です。
     * @param v
     */
    private void onShareActivityClick(View v) {
        // クリックに反応を示します。
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.view_click);
        v.startAnimation(animation);

        // アクティビティを起動します。
        ShareActivity shareActivity = (ShareActivity) v.getTag();
        ResolveInfo info = shareActivity.getResolveInfo();
        Intent intent = mIntentService.createIntent();
        intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        startActivity(intent);

        // 当アクティビティを終了しておきます。
        // これにより、遷移先アクティビティでバックボタンクリック時に、当アクティビティを表示しなくなります。
        finish();

        // 共有アクティビティをDB登録します。
        if (!shareActivity.getStandardFlag()) {
            shareActivity.setStandardFlag(mStandardSwitch.isChecked());
        }
        mShareActivityService.updateOrInsert(shareActivity);

        // 共有履歴をDB登録します。
        ShareHistory shareHistory = new ShareHistory(shareActivity, mIntentService.getShortIntentStr());
        mShareHistoryService.insert(shareHistory);

        // 古い共有履歴を削除します。
        mShareHistoryService.deleteOld();

        // ステータスバーに通知（Notification）を登録します。
        if (PreferenceHelper.getNotificationFlag(this)) {
            new NotificationHelper(getBaseContext()).notifyIntentSended();
        }
    }

}
