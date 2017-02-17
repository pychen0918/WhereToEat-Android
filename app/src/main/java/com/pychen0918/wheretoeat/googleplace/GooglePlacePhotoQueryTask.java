package com.pychen0918.wheretoeat.googleplace;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.pychen0918.wheretoeat.common.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pychen0918 on 2016/11/10.
 */

public class GooglePlacePhotoQueryTask extends AsyncTask<String, Void, GooglePlacePhotoQueryTask.ParamWrapper> {
    private PhotoQueryTaskResponse taskResponse = null;
    private int index = -1;

    class ParamWrapper{
        boolean isSuccess;
        Bitmap result;

        ParamWrapper(boolean isSuccess, Bitmap result) {
            this.isSuccess = isSuccess;
            this.result = result;
        }
    }

    @Override
    protected ParamWrapper doInBackground(String... input) {
        this.index = Integer.parseInt(input[0]);
        return getPhotoImage(input[1]);
    }

    @Override
    protected void onPostExecute(ParamWrapper param) {
        taskResponse.finishPhotoQueryTask(param.isSuccess, this.index, param.result);
    }

    public PhotoQueryTaskResponse getTaskResponse() {
        return taskResponse;
    }

    public void setTaskResponse(PhotoQueryTaskResponse taskResponse) {
        this.taskResponse = taskResponse;
    }

    private ParamWrapper getPhotoImage(String myUrl) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(myUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(Constants.httpURLConnectionReadTimeout);
            httpURLConnection.setConnectTimeout(Constants.httpURLConnectionConnectionTimeout);
            httpURLConnection.setDoInput(true);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream input = new BufferedInputStream(httpURLConnection.getInputStream());
                return new ParamWrapper(true, readImage(input));
            } else {
                return new ParamWrapper(false, null);
            }
        }
        catch (IOException e) {
            return new ParamWrapper(false, null);
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private Bitmap readImage(InputStream input) {
        return BitmapFactory.decodeStream(input);
    }
}
