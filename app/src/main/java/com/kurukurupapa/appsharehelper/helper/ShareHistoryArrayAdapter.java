package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.ShareHistory;

import java.util.List;

/**
 * 共有履歴アダプタークラス
 *
 * 共有履歴データをリストビュー表示するためのアダプターです。
 */
public class ShareHistoryArrayAdapter extends ArrayAdapter<ShareHistory> {
    private final LayoutInflater mLayoutInflater;
    private int mPosition;
    private boolean mDevFlag;

    public ShareHistoryArrayAdapter(Context context, List<ShareHistory> objects) {
        super(context, 0, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDevFlag(boolean devFlag) {
        mDevFlag = devFlag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Viewが未作成の場合、新規に作成します。
        // 作成済みの場合は、そのまま再利用します。
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.card_share_history, null);
        }

        // 当該行のデータを取得します。
        ShareHistory shareHistory = getItem(position);

        // Viewオブジェクトを設定します。
        TextView timestampTextView = (TextView) convertView.findViewById(R.id.timestamp_text_view);
        ImageView srcImageView = (ImageView) convertView.findViewById(R.id.src_image_view);
        TextView srcTextView = (TextView) convertView.findViewById(R.id.src_text_view);
        ImageView destImageView = (ImageView) convertView.findViewById(R.id.dest_image_view);
        TextView destTextView = (TextView) convertView.findViewById(R.id.dest_text_view);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.content_text_view);

        ShareActivity shareActivity = shareHistory.getShareActivity();
        ShareActivityHelper helper = new ShareActivityHelper(shareActivity, getContext());
        timestampTextView.setText(shareHistory.getTimestampStr(getContext()));
        srcImageView.setImageDrawable(helper.loadSrcPackageIcon());
        srcTextView.setText(helper.loadSrcPackageLabel());
        destImageView.setImageDrawable(helper.loadDestActivityIcon());
        destTextView.setText(helper.loadDestActivityLabel());

        String content;
        if (mDevFlag) {
            content = shareActivity.getAction() + "\n" + helper.getTypeForDisplay() + "\n" + shareHistory.getContent();
        } else {
            content = shareHistory.getContent();
        }
        contentTextView.setText(content);

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
