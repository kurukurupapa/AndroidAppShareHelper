package com.kurukurupapa.appsharehelper.model;

import android.content.Context;

import com.kurukurupapa.appsharehelper.R;

/**
 * ナビゲーションドロワーに表示する項目名を保持するクラスです。
 */
public class NavigationDrawerItems {
    public static final int POSITION_HISTORY = 0;
    public static final int POSITION_STANDARD_APP = 1;
    public static final int POSITION_CLIPBOARD = 2;
    public static final int POSITION_SETTINGS = 3;

    private static final int[] TITLE_ARR = new int[]{
            R.string.title_nav_history,
            R.string.title_nav_standard_app,
            R.string.title_nav_clipboard,
            R.string.title_nav_settings,
    };

    private String[] mTitleArray;

    public NavigationDrawerItems(Context context) {
        mTitleArray = new String[TITLE_ARR.length];
        for (int i = 0; i < TITLE_ARR.length; i++) {
            mTitleArray[i] = context.getString(TITLE_ARR[i]);
        }
    }

    public String[] getTitleArray() {
        return mTitleArray;
    }
}
