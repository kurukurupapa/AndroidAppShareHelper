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

    public StandardAppArrayAdapter(Context context, List<ShareActivity> objects,
                                   Switch.OnCheckedChangeListener onStandardCheckedChangeListener) {
        super(context, 0, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mOnStandardCheckedChangeListener = onStandardCheckedChangeListener;
    }

    public void setDevFlag(boolean devFlag) {
        mDevFlag = devFlag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Viewが未作成の場合、新規に作成します。
        // 作成済みの場合は、そのまま再利用します。
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.card_standard_app, null);
        }

        // 当該行のデータを取得します。
        ShareActivity shareActivity = getItem(position);

        // Viewオブジェクトを設定します。
        ImageView srcImageView = (ImageView) convertView.findViewById(R.id.src_image_view);
        TextView srcTextView = (TextView) convertView.findViewById(R.id.src_text_view);
        ImageView destImageView = (ImageView) convertView.findViewById(R.id.dest_image_view);
        TextView destTextView = (TextView) convertView.findViewById(R.id.dest_text_view);
        TextView actionTypeTextView = (TextView) convertView.findViewById(R.id.action_type_text_view);
        Switch standardSwitch = (Switch) convertView.findViewById(R.id.standard_switch);

        ShareActivityHelper helper = new ShareActivityHelper(shareActivity, getContext());
        srcImageView.setImageDrawable(helper.loadSrcPackageIcon());
        srcTextView.setText(helper.loadSrcPackageLabel());
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
