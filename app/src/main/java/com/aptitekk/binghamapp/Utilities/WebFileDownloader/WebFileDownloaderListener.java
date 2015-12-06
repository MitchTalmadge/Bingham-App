package com.aptitekk.binghamapp.Utilities.WebFileDownloader;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.net.URL;

interface WebFileDownloaderListener {

    void fileDownloadedAsDocument(URL url, Document document);

    void fileDownloadedAsStream(URL url, BufferedReader stream);

    void fileDownloadedAsJSONObject(URL url, JSONObject jsonObject);

    void fileSizeDetermined(URL url, int fileSizeInBytes);

}
