package com.aptitekk.binghamapp.Utilities.WebFileDownloader;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.net.URL;

public abstract class WebFileDownloaderAdapter implements WebFileDownloaderListener {

    @Override
    public void fileDownloadedAsDocument(URL url, Document document) {
    }

    @Override
    public void fileDownloadedAsStream(URL url, BufferedReader stream) {
    }

    @Override
    public void fileDownloadedAsJSONObject(URL url, JSONObject jsonObject) {
    }

    @Override
    public void fileSizeDetermined(URL url, int fileSizeInBytes) {
    }
}
