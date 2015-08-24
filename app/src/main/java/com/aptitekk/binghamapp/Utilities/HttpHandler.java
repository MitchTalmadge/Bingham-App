package com.aptitekk.binghamapp.Utilities;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class HttpHandler {

    private final HttpHandlerListener listener;
    private final HttpHandlerAction action;
    private final String url;
    private final HashMap<String, String> data;

    public enum HttpHandlerAction {
        POST,
        GET,
        HEAD
    }

    public HttpHandler(HttpHandlerListener listener, HttpHandlerAction action, String url, HashMap<String, String> data) {
        this.listener = listener;
        this.action = action;
        this.url = url;
        this.data = data;
    }

    public void execute() {
        new HttpAsyncTask().execute(this.url);
    }

    public interface HttpHandlerListener {

        void onHttpTransactionComplete(String response);

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String requestMethod = null;
                switch (action) {
                    case POST:
                        requestMethod = "POST";
                        break;
                    case GET:
                        requestMethod = "GET";
                        break;
                    case HEAD:
                        requestMethod = "HEAD";
                        break;
                    default:
                        break;
                }
                URL url = new URL(params[0] + ((requestMethod != null) ? mapToURLParams(data) : ""));
                BufferedReader streamReader;
                if (params[0].startsWith("https")) {
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    if (requestMethod != null)
                        conn.setRequestMethod(requestMethod);

                    streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (requestMethod != null)
                        conn.setRequestMethod(requestMethod);

                    streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                }

                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                return responseStrBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (listener != null)
                listener.onHttpTransactionComplete(response);
        }
    }

    private String mapToURLParams(HashMap<String, String> map) {
        String params = "?";
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            params += entry.getKey() + "=" + entry.getValue() + "&";
        }

        if (params.endsWith("&"))
            params = params.substring(0, params.length() - 1);

        return params;
    }

}
