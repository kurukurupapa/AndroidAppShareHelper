package com.kurukurupapa.appsharehelper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.kurukurupapa.appsharehelper.helper.ClipboardIntentHelper;
import com.kurukurupapa.appsharehelper.helper.DbHelper;
import com.kurukurupapa.appsharehelper.helper.NotificationHelper;
import com.kurukurupapa.appsharehelper.helper.PreferenceHelper;
import com.kurukurupapa.appsharehelper.helper.RecvActivitySrcApp;
import com.kurukurupapa.appsharehelper.helper.ShareActivityAdapter;
import com.kurukurupapa.appsharehelper.model.ShareHistory;
import com.kurukurupapa.appsharehelper.service.IntentService;
import com.kurukurupapa.appsharehelper.service.ShareActivityService;
import com.kurukurupapa.appsharehelper.service.ShareActivityTableService;
import com.kurukurupapa.appsharehelper.service.ShareHistoryService;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * 受信アクティビティ
 *
 * インテント呼び出しを受信するアクティビティです。
 */
public class RecvActivity extends Activity {
    private static final String TAG = RecvActivity.class.getSimpleName();
    private static final String KEY_SRC_PACKAGE_NAME = "src_package_name";

    /** 当アプリから当アクティビティ起動時の動作モード */
    private static final String EXTRA_MODE_KEY = "com.kurukurupapa.appsharehelper.extra.MODE";
    /** クリップボード共有の動作モード */
    private static final String EXTRA_MODE_VALUE_CLIPBOARD = "CLIPBOARD";

    private DbHelper mDbHelper;
    private IntentService mIntentService;
    private ShareActivityTableService mShareActivityTableService;
    private ShareActivityService mShareActivityService;
    private ShareHistoryService mShareHistoryService;

    private RelativeLayout mRootLayout;
    private Switch mDevSwitch;
    private TextView mIntentValueTextView;
    private LinearLayout mDestLinearLayout;
    private Switch mStandardSwitch;
    private Animation mActivityShowAnimation;
    private Animation mViewChangeAnimation;

    private RecvActivitySrcApp mRecvActivitySrcApp;

    /**
     * 当アクティビティを起動するインテントを作成します。
     * @return インテント
     */
    public static Intent createIntentForClipboard(Context context) {
        Intent intent = new Intent(context, RecvActivity.class);
        intent.putExtra(EXTRA_MODE_KEY, EXTRA_MODE_VALUE_CLIPBOARD);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Called. savedInstanceState=" + savedInstanceState);
        setContentView(getLayoutResId());

        // オブジェクト生成
        mDbHelper = new DbHelper(this);
        mIntentService = new IntentService(this);
        mShareActivityTableService = new ShareActivityTableService(mDbHelper);
        mShareActivityService = new ShareActivityService(this, mShareActivityTableService);
        mShareHistoryService = new ShareHistoryService(mDbHelper);

        // UIオブジェクト取得
        mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        mDevSwitch = (Switch) findViewById(R.id.dev_switch);
        mIntentValueTextView = (TextView) findViewById(R.id.intent_value_text_view);
        mDestLinearLayout = (LinearLayout) findViewById(R.id.dest_linear_layout);
        mStandardSwitch = (Switch) findViewById(R.id.standard_switch);
        mActivityShowAnimation = AnimationUtils.loadAnimation(this, R.anim.activity_show);
        mViewChangeAnimation = AnimationUtils.loadAnimation(this, R.anim.view_change);

        if (IntentService.isValidSrcAppFunction()) {
            mRecvActivitySrcApp = new RecvActivitySrcApp(this);
        }

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
        if (!setupIntentService(getIntent(), savedInstanceState)) {
            finish();
        }
    }

    private int getLayoutResId() {
        int resId;
        if (IntentService.isValidSrcAppFunction()) {
            resId = R.layout.activity_recv;
        } else {
            resId = R.layout.activity_recv2;
        }
        return resId;
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
        if (!setupIntentService(intent, null)) {
            finish();
        }
    }

    /**
     * インテント受信サービスを設定します。
     * @param intent アクティビティで取得するインテント
     * @return 処理成功の場合true
     */
    private boolean setupIntentService(Intent intent, Bundle savedInstanceState) {
        String mode = intent.getStringExtra(EXTRA_MODE_KEY);
        if (mode != null && mode.equals(EXTRA_MODE_VALUE_CLIPBOARD)) {
            // クリップボード共有の場合
            Intent newIntent = new ClipboardIntentHelper(this).createIntent();
            if (newIntent == null) {
                Toast.makeText(this, R.string.msg_no_clipdata, Toast.LENGTH_LONG).show();
                return false;
            }
            mIntentService.setIntent(newIntent);
            mIntentService.setSrcAppInfo(getPackageName());
        } else {
            if (savedInstanceState == null) {
                // 初期表示、または新インテントの場合
                // または、アプリ終了後に「Recent Apps」（最近使ったアプリ）から呼び出された場合
                // ※アプリ終了後に最近使ったアプリから呼び出された場合には、
                //   インテントの送信元アプリを特定することはできません。
                mIntentService.setIntent(intent);
                mIntentService.findSrcAppInfo();
            } else {
                // 再表示の場合（画面ローテーションした場合など）
                mIntentService.setIntent(intent);
                mIntentService.setSrcAppInfo(savedInstanceState.getString(KEY_SRC_PACKAGE_NAME));
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Called");

        // インテントが変わっていたら内容を画面表示
        if (mIntentService.isIntentChanged()) {
            if (mRecvActivitySrcApp != null) {
                mRecvActivitySrcApp.show(mIntentService);
            }
            showIntent();
            showDestActivity();
            mRootLayout.startAnimation(mActivityShowAnimation);
            mIntentService.setIntentNoChanged();
        } else {
            // クリップボード共有の場合、毎々内容を更新する。
            // TODO ロジック記述箇所は、あとで再考したい。
            String mode = getIntent().getStringExtra(EXTRA_MODE_KEY);
            if (mode != null && mode.equals(EXTRA_MODE_VALUE_CLIPBOARD)) {
                Intent newIntent = new ClipboardIntentHelper(this).createIntent();
                if (newIntent != null) {
                    mIntentService.setIntent(newIntent);
                    showIntent();
                }
            }
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
     * インテント受信可能アクティビティを表示
     */
    private void showDestActivity() {
        // ListViewを初期化します。
        mDestLinearLayout.removeAllViews();

        // アクティビティ一覧を取得します。
        List<ShareActivityAdapter> shareActivityList = mShareActivityService.find(
                mIntentService.getSrcPackageName(), mIntentService.getIntent(), mIntentService.isText());

        // ListViewに、アクティビティ表示/起動用のViewを追加します。
        PackageManager pm = this.getPackageManager();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (ShareActivityAdapter shareActivity : shareActivityList) {
            View view = layoutInflater.inflate(R.layout.item_activity, null);
            ImageView iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
            TextView labelTextView = (TextView) view.findViewById(R.id.label_text_view);
            TextView detailTextView = (TextView) view.findViewById(R.id.detail_text_view);

            // アイコン
            iconImageView.setImageDrawable(shareActivity.loadDestActivityIcon());

            // ラベルと詳細情報
            labelTextView.setText(shareActivity.loadDestActivityLabel());
            detailTextView.setText(shareActivity.getStartIntent().getAction() + "\n" + shareActivity.getTimestampStr());
            detailTextView.setVisibility(mDevSwitch.isChecked() ? View.VISIBLE : View.GONE);

            // タッチ時処理
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
                startStandardShareActivity(view);
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
        setDestActivityDetailTextView(isChecked);
    }

    private void setIntentValueTextView(boolean isChecked) {
        if (isChecked) {
            mIntentValueTextView.setText(mIntentService.getLongIntentStr());
        } else {
            mIntentValueTextView.setText(mIntentService.getShortIntentStr());
        }
    }

    private void setDestActivityDetailTextView(boolean isChecked) {
        for (int i = 0; i < mDestLinearLayout.getChildCount(); i++) {
            TextView detailTextView = (TextView) mDestLinearLayout.getChildAt(i).findViewById(R.id.detail_text_view);
            detailTextView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
        startShareActivity(v);
    }

    private void startStandardShareActivity(View v) {
        // アクティビティを起動します。
        startShareActivity(v);
    }

    private void startShareActivity(View v) {
        // アクティビティを起動します。
        ShareActivityAdapter shareActivity = (ShareActivityAdapter) v.getTag();
        startActivity(shareActivity.createIntent());

        // 当アクティビティを終了しておきます。
        // これにより、遷移先アクティビティでバックボタンクリック時に、当アクティビティを表示しなくなります。
        finish();

        // 共有アクティビティをDB登録します。
        if (!shareActivity.getStandardFlag()) {
            shareActivity.setStandardFlag(mStandardSwitch.isChecked());
        }
        mShareActivityService.updateOrInsert(shareActivity);

        // 共有履歴をDB登録します。
        ShareHistory shareHistory = new ShareHistory(shareActivity.getShareActivity(), mIntentService.getShortIntentStr());
        mShareHistoryService.insert(shareHistory);

        // 古い共有履歴を削除します。
        mShareHistoryService.deleteOld();

        // ステータスバーに通知（Notification）を登録します。
        new NotificationHelper(getBaseContext()).notifyIntentSendedIfNeed();
    }

}
