package com.pychen0918.wheretoeat.googleplace;

/**
 * Created by pychen0918 on 2016/11/15.
 */

public interface DetailQueryTaskResponse {
    void finishDetailQueryTask(boolean status, int index, String output);
}
