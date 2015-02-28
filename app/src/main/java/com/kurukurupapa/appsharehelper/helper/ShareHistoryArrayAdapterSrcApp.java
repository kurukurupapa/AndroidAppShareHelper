package com.kurukurupapa.appsharehelper.helper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kurukurupapa.appsharehelper.R;

/**
 * 共有履歴アダプタークラス用共有元アプリ表示クラス
 *
 * 共有履歴アダプタークラスで、共有元アプリを表示します。
 */
public class ShareHistoryArrayAdapterSrcApp {
    public View setupView(View convertView, ShareHistoryAdapter shareHistory) {
        ImageView srcImageView = (ImageView) convertView.findViewById(R.id.src_image_view);
        TextView srcTextView = (TextView) convertView.findViewById(R.id.src_text_view);
        srcImageView.setImageDrawable(shareHistory.loadSrcPackageIcon());
        srcTextView.setText(shareHistory.loadSrcPackageLabel());
        return convertView;
    }
}
