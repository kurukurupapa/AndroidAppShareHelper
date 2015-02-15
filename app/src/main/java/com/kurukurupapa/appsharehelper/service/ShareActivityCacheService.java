package com.kurukurupapa.appsharehelper.service;

import com.kurukurupapa.appsharehelper.model.ShareActivity;

import java.util.TreeMap;

/**
 * 共有アクティビティデータキャッシュサービスクラス
 */
public class ShareActivityCacheService {
    private ShareActivityTableService mTableService;
    private TreeMap<Long, ShareActivity> mMap;

    public ShareActivityCacheService(ShareActivityTableService tableService) {
        mTableService = tableService;
        mMap = new TreeMap<Long, ShareActivity>();
    }

    public ShareActivity get(long id) {
        ShareActivity shareActivity = mMap.get(id);
        if (shareActivity == null) {
            shareActivity = mTableService.findByIdOrNull(id);
            mMap.put(id, shareActivity);
        }
        return shareActivity;
    }
}
