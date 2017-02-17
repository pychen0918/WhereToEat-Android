package com.pychen0918.wheretoeat.googleplace;

import android.os.AsyncTask;

import com.pychen0918.wheretoeat.common.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pychen0918 on 2016/11/15.
 */

public class GooglePlaceDetailQueryTask extends AsyncTask<String, Void, GooglePlaceDetailQueryTask.ParamWrapper> {
    private DetailQueryTaskResponse taskResponse = null;
    private int index = -1;

    class ParamWrapper{
        boolean isSuccess;
        String result;

        ParamWrapper(boolean isSuccess, String result) {
            this.isSuccess = isSuccess;
            this.result = result;
        }
    }

    @Override
    protected ParamWrapper doInBackground(String... input) {
        this.index = Integer.parseInt(input[0]);
        return getDetail(input[1]);
    }

    @Override
    protected void onPostExecute(ParamWrapper param) {
        taskResponse.finishDetailQueryTask(param.isSuccess, this.index, param.result);
    }

    public DetailQueryTaskResponse getTaskResponse() {
        return taskResponse;
    }

    public void setTaskResponse(DetailQueryTaskResponse taskResponse) {
        this.taskResponse = taskResponse;
    }

    private ParamWrapper getDetail(String myUrl) {
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
                return new ParamWrapper(true, readIt(input));
            } else {
                return new ParamWrapper(false, "Error response code:"+responseCode);
            }
        }
        catch (IOException e) {
            return new ParamWrapper(false, "Unable to retrieve web page. URL may be invalid.");
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private String readIt(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }
}
