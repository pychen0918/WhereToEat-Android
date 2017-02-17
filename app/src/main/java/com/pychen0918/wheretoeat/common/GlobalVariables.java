package com.pychen0918.wheretoeat.common;

import android.app.Application;

/**
 * Created by pychen0918 on 2016/11/7.
 */

public class GlobalVariables extends Application {
    public QueryResults queryResults;

    @Override
    public void onCreate(){
        queryResults = new QueryResults();
    }
}
