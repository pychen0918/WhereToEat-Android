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
 * Created by pychen0918 on 2016/11/8.
 */

// This class is used to handle the list query
// That is, when user first use this app and query the information of 20 restaurants
// One must implement interface finishListQueryTask as callback function when query result returned

public class GooglePlaceListQueryTask extends AsyncTask<String, Void, GooglePlaceListQueryTask.ParamWrapper> {
    private QueryTaskResponse taskResponse = null;

    class ParamWrapper{
        boolean isSuccess;
        String result;

        ParamWrapper(boolean isSuccess, String result) {
            this.isSuccess = isSuccess;
            this.result = result;
        }
    }

    public QueryTaskResponse getTaskResponse() {
        return taskResponse;
    }

    public void setTaskResponse(QueryTaskResponse taskResponse) {
        this.taskResponse = taskResponse;
    }

    @Override
    protected ParamWrapper doInBackground(String... params) {
        // params[0] is the URL that we wants to access
        return queryUrl(params[0]);
    }

    @Override
    protected void onPostExecute(ParamWrapper param) {
        taskResponse.finishListQueryTask(param.isSuccess, param.result);
    }

    private ParamWrapper queryUrl(String myUrl) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(myUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
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
