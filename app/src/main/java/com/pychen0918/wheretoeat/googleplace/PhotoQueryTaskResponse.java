package com.pychen0918.wheretoeat.googleplace;

import android.graphics.Bitmap;

/**
 * Created by pychen0918 on 2016/11/10.
 */

public interface PhotoQueryTaskResponse {
    void finishPhotoQueryTask(boolean status, int index, Bitmap output);
}
