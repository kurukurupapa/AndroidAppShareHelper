package com.kurukurupapa.appsharehelper.helper;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.activity.RecvActivity;
import com.kurukurupapa.appsharehelper.service.IntentService;

/**
 * 受信アクティビティ用共有元アプリ表示クラス
 */
public class RecvActivitySrcApp {
    private Context mContext;
    private ImageView mSrcImageView;
    private TextView mSrcNameTextView;

    public RecvActivitySrcApp(RecvActivity activity) {
        mContext = activity;
        mSrcImageView = (ImageView) activity.findViewById(R.id.src_image_view);
        mSrcNameTextView = (TextView) activity.findViewById(R.id.src_name_text_view);
    }

    public void show(IntentService mIntentService) {
        if (mIntentService.isValidSrcAppInfo()) {
            mSrcImageView.setImageDrawable(mIntentService.getSrcAppIcon());
            mSrcNameTextView.setText(mIntentService.getSrcAppLabel());
        } else {
            mSrcImageView.setImageResource(R.drawable.ic_unknown);
            mSrcNameTextView.setText(mContext.getString(R.string.label_unknown));
        }
    }

}
