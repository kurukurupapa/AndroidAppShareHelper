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
import com.kurukurupapa.appsharehelper.service.IntentService;

import java.util.List;

/**
 * 共有履歴アダプタークラス
 *
 * 共有履歴データをリストビュー表示するためのアダプターです。
 */
public class ShareHistoryArrayAdapter extends ArrayAdapter<ShareHistoryAdapter> {
    private static final String TAG = ShareHistoryArrayAdapter.class.getSimpleName();

    private final LayoutInflater mLayoutInflater;
    private int mPosition;
    private boolean mDevFlag;
    private int mLayoutId;
    private ShareHistoryArrayAdapterSrcApp mShareHistoryArrayAdapterSrcApp;

    /**
     * インスタンスを生成します。
     * @param context
     * @param objects
     * @return
     */
    public static ShareHistoryArrayAdapter create(Context context, List<ShareHistoryAdapter> objects) {
        ShareHistoryArrayAdapter instance;
        if (IntentService.isValidSrcAppFunction()) {
            instance = new ShareHistoryArrayAdapter(context, objects, R.layout.card_share_history, new ShareHistoryArrayAdapterSrcApp());
        } else {
            instance = new ShareHistoryArrayAdapter(context, objects, R.layout.card_share_history2, null);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param context
     * @param objects
     * @param layoutId
     * @param shareHistoryArrayAdapterSrcApp
     */
    private ShareHistoryArrayAdapter(
            Context context, List<ShareHistoryAdapter> objects,
            int layoutId, ShareHistoryArrayAdapterSrcApp shareHistoryArrayAdapterSrcApp) {
        super(context, 0, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutId = layoutId;
        mShareHistoryArrayAdapterSrcApp = shareHistoryArrayAdapterSrcApp;
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
        ShareHistoryAdapter shareHistory = getItem(position);

        // Viewオブジェクトを設定します。
        TextView timestampTextView = (TextView) convertView.findViewById(R.id.timestamp_text_view);
        ImageView destImageView = (ImageView) convertView.findViewById(R.id.dest_image_view);
        TextView destTextView = (TextView) convertView.findViewById(R.id.dest_text_view);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.content_text_view);

        timestampTextView.setText(shareHistory.getTimestampStr());
        destImageView.setImageDrawable(shareHistory.loadDestActivityIcon());
        destTextView.setText(shareHistory.loadDestActivityLabel());

        String content = null;
        if (mDevFlag) {
            content = shareHistory.getLongContent();
        } else {
            content = shareHistory.getShortContent();
        }
        contentTextView.setText(content);

        // 共有元アプリ表示
        if (mShareHistoryArrayAdapterSrcApp != null) {
            mShareHistoryArrayAdapterSrcApp.setupView(convertView, shareHistory);
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
