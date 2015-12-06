package com.aptitekk.binghamapp.Utilities.WebFileDownloader;

import android.os.AsyncTask;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The purpose of this class is to provide a simple interface for downloading files from the web.
 */
public class WebFileDownloader {

    public static void downloadFromURLAsDocument(URL url, WebFileDownloaderListener listener) {
        MainActivity.logVerbose("Downloading Document from " + url.toString());
        new DownloadAsDocumentTask(url, listener).execute();
    }

    public static void downloadFromURLAsStream(URL url, WebFileDownloaderListener listener) {
        MainActivity.logVerbose("Downloading Stream from " + url.toString());
        new DownloadAsStreamTask(url, listener).execute();
    }

    public static void downloadFromURLAsJSONObject(URL url, WebFileDownloaderListener listener) {
        MainActivity.logVerbose("Downloading JSON Object from " + url.toString());
        new DownloadAsJSONObjectTask(url, listener).execute();
    }

    public static void getFileSizeFromURL(URL url, WebFileDownloaderListener listener) {
        MainActivity.logVerbose("Determining file size of " + url.toString());
        new getFileSizeTask(url, listener).execute();
    }

    private static class DownloadAsDocumentTask extends AsyncTask<Void, Void, Document> {

        private final URL url;
        private final WebFileDownloaderListener listener;

        public DownloadAsDocumentTask(URL url, WebFileDownloaderListener listener) {

            this.url = url;
            this.listener = listener;
        }

        @Override
        protected Document doInBackground(Void... voids) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(url.toString());
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Document document) {
            listener.fileDownloadedAsDocument(url, document);
        }
    }

    private static class DownloadAsStreamTask extends AsyncTask<Void, Void, BufferedReader> {

        private final URL url;
        private final WebFileDownloaderListener listener;

        public DownloadAsStreamTask(URL url, WebFileDownloaderListener listener) {

            this.url = url;
            this.listener = listener;
        }

        @Override
        protected BufferedReader doInBackground(Void... voids) {
            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(1000);
                return new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BufferedReader bufferedReader) {
            listener.fileDownloadedAsStream(url, bufferedReader);
        }
    }

    private static class DownloadAsJSONObjectTask extends AsyncTask<Void, Void, JSONObject> {

        private final URL url;
        private final WebFileDownloaderListener listener;

        public DownloadAsJSONObjectTask(URL url, WebFileDownloaderListener listener) {

            this.url = url;
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(1000);
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                return new JSONObject(responseStrBuilder.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            listener.fileDownloadedAsJSONObject(url, jsonObject);
        }
    }

    private static class getFileSizeTask extends AsyncTask<Void, Void, Integer> {

        private URL url;
        private final WebFileDownloaderListener listener;

        public getFileSizeTask(URL url, WebFileDownloaderListener listener) {

            try {
                this.url = new URL(url.toString() + "?unused=" + Calendar.getInstance().get(Calendar.MILLISECOND)); //Attempt to add an unused variable to the URL to avoid caching (which gives no file size)
            } catch (MalformedURLException e) {
                this.url = url;
            }
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                conn.getInputStream();
                return conn.getContentLength();
            } catch (IOException e) {
                return 0;
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Integer fileSizeInBytes) {
            listener.fileSizeDetermined(url, fileSizeInBytes);
        }
    }

}
