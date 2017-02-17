package com.pychen0918.wheretoeat.common;

/**
 * Created by pychen0918 on 2016/11/6.
 */

public final class Constants {
    public final static String googleApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    public final static String googlePhotoUrl = "https://maps.googleapis.com/maps/api/place/photo";
    public final static String googleDetailUrl = "https://maps.googleapis.com/maps/api/place/details/json";
    public final static String googlePlayUrl = "https://play.google.com/store/apps/details";
    public final static long queryInterval = 300000;  // 5 minutes in milliseconds
    public final static int httpURLConnectionReadTimeout = 10000;  // 10 seconds
    public final static int httpURLConnectionConnectionTimeout = 15000;  // 15 seconds
    public final static int photoMaxHeight = 400;
    public final static int photoMaxWidth = 400;
    public final static int rateAppIntervalDays = 7;
    public final static int rateAppIntervalLaunches = 10;
    public final static int maxKeywordHistory = 16;
}
