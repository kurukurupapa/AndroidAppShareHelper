package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.service.IntentService;

import java.util.List;

/**
 * 標準アプリ一覧アダプタークラス
 *
 * 標準アプリ設定された共有アクティビティデータをリストビュー表示するためのアダプターです。
 */
public class StandardAppArrayAdapter extends ArrayAdapter<ShareActivity> {
    private final LayoutInflater mLayoutInflater;
    private int mPosition;
    private boolean mDevFlag;
    private ToggleButton.OnCheckedChangeListener mOnStandardCheckedChangeListener;
    private int mLayoutId;
    private StandardAppArrayAdapterSrcApp mStandardAppArrayAdapterSrcApp;

    /**
     * インスタンスを生成します。
     * @param context
     * @param objects
     * @param onStandardCheckedChangeListener
     * @return
     */
    public static StandardAppArrayAdapter create(Context context, List<ShareActivity> objects, Switch.OnCheckedChangeListener onStandardCheckedChangeListener) {
        StandardAppArrayAdapter instance;
        if (IntentService.isValidSrcAppFunction()) {
            instance = new StandardAppArrayAdapter(context, objects, onStandardCheckedChangeListener, R.layout.card_standard_app, new StandardAppArrayAdapterSrcApp());
        } else {
            instance = new StandardAppArrayAdapter(context, objects, onStandardCheckedChangeListener, R.layout.card_standard_app2, null);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param context
     * @param objects
     * @param onStandardCheckedChangeListener
     * @param layoutId
     * @param standardAppArrayAdapterSrcApp
     */
    private StandardAppArrayAdapter(
            Context context, List<ShareActivity> objects,
            Switch.OnCheckedChangeListener onStandardCheckedChangeListener,
            int layoutId, StandardAppArrayAdapterSrcApp standardAppArrayAdapterSrcApp) {
        super(context, 0, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mOnStandardCheckedChangeListener = onStandardCheckedChangeListener;
        mLayoutId = layoutId;
        mStandardAppArrayAdapterSrcApp = standardAppArrayAdapterSrcApp;
    }

    public void setDevFlag(boolean devFlag) {
        mDevFlag = devFlag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Viewが未作成の場合、新規に作成します。
        // 作成済みの場合は、そのまま再利用します。
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutId, null);
        }

        // 当該行のデータを取得します。
        ShareActivity shareActivity = getItem(position);

        // Viewオブジェクトを設定します。
        ImageView destImageView = (ImageView) convertView.findViewById(R.id.dest_image_view);
        TextView destTextView = (TextView) convertView.findViewById(R.id.dest_text_view);
        TextView actionTypeTextView = (TextView) convertView.findViewById(R.id.action_type_text_view);
        Switch standardSwitch = (Switch) convertView.findViewById(R.id.standard_switch);

        ShareActivityAdapter helper = new ShareActivityAdapter(shareActivity, getContext(), getContext().getPackageManager(), null, null, null);
        destImageView.setImageDrawable(helper.loadDestActivityIcon());
        destTextView.setText(helper.loadDestActivityLabel());
        if (mDevFlag) {
            actionTypeTextView.setText(shareActivity.getAction() + "\n" + helper.getTypeForDisplay());
            actionTypeTextView.setVisibility(View.VISIBLE);
        } else {
            actionTypeTextView.setVisibility(View.GONE);
        }

        standardSwitch.setTag(shareActivity);
        standardSwitch.setOnCheckedChangeListener(null);
        standardSwitch.setChecked(shareActivity.getStandardFlag());
        standardSwitch.setOnCheckedChangeListener(mOnStandardCheckedChangeListener);

        // 共有元アプリ表示
        if (mStandardAppArrayAdapterSrcApp != null) {
            mStandardAppArrayAdapterSrcApp.setupView(convertView, helper);
        }

        // アニメーション開始
        // ※下にスクロールする場合だけアニメーションを表示します。
        if (mPosition < position) {
            convertView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.card_show));
            mPosition = position;
        } else {
            mPosition = position;
        }

        return convertView;
    }

}
