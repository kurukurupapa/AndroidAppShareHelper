package com.kurukurupapa.appsharehelper.helper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kurukurupapa.appsharehelper.R;

/**
 * 標準アプリ一覧アダプター用共有元アプリ表示クラス
 *
 * 標準アプリ一覧アダプタークラスで、共有元アプリを表示します。
 */
public class StandardAppArrayAdapterSrcApp {
    public View setupView(View convertView, ShareActivityAdapter shareActivity) {
        ImageView srcImageView = (ImageView) convertView.findViewById(R.id.src_image_view);
        TextView srcTextView = (TextView) convertView.findViewById(R.id.src_text_view);
        srcImageView.setImageDrawable(shareActivity.loadSrcPackageIcon());
        srcTextView.setText(shareActivity.loadSrcPackageLabel());
        return convertView;
    }
}
