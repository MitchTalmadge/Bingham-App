package com.aptitekk.binghamapp.News;

import android.content.Context;
import android.content.SharedPreferences;

import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.Utilities.WebFileDownloader.WebFileDownloader;
import com.aptitekk.binghamapp.Utilities.WebFileDownloader.WebFileDownloaderAdapter;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class NewsFeed {

    private NewsFeedManager manager;
    private final NewsFeedType feedType;
    private String feedName;
    private URL feedUrl;

    private ArrayList<NewsArticle> articlesList;

    private int fileSizeOnWeb = 0;

    public NewsFeed(NewsFeedManager manager, NewsFeedType feedType) {
        this.manager = manager;
        this.feedType = feedType;
        this.feedName = feedType.getFeedName();
        this.feedUrl = feedType.getFeedUrl();
    }

    public NewsFeedType getFeedType() {
        return feedType;
    }

    public String getFeedName() {
        return feedName;
    }

    public URL getFeedUrl() {
        return feedUrl;
    }

    public String getFileName() {
        return feedName.replace(" ", "").toLowerCase() + ".feed";
    }

    public String getPreferencesTag() {
        return feedName.replace(" ", "").toLowerCase() + "_lastSize";
    }

    public ArrayList<NewsArticle> getArticlesList() {
        return articlesList;
    }

    /**
     * Checks for updates to the NewsFeed and will download if:<br>
     * 1. The file size is inconsistent, or<br>
     * 2. The file size returns null (size <= 0)<br>
     * If the file size is not null and is consistent, the feed will be restored from a cached copy.
     */
    public void checkForUpdates() {
        MainActivity.logVerbose("Checking for Updates for " + feedName + " News Feed...");
        WebFileDownloader.getFileSizeFromURL(feedUrl, new WebFileDownloaderAdapter() {
            @Override
            public void fileSizeDetermined(URL url, int fileSizeInBytes) {
                fileSizeOnWeb = fileSizeInBytes;

                final SharedPreferences sharedPreferences = manager.getMainActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                int lastNewsFeedUpdateSize = sharedPreferences.getInt(getPreferencesTag(), 0);

                MainActivity.logVerbose("File size determined for " + feedName + " News Feed:");
                MainActivity.logVerbose("News Feed Size in Storage: " + lastNewsFeedUpdateSize);
                MainActivity.logVerbose("News Feed Size on Web: " + fileSizeInBytes);

                // If we have never downloaded the feed before or the feed on the website is a different size...
                if (lastNewsFeedUpdateSize <= 0 ||
                        fileSizeInBytes != lastNewsFeedUpdateSize ||
                        !new File(manager.getMainActivity().getFilesDir(), getFileName()).exists()) {
                    MainActivity.logVerbose("News feed is out of date. Downloading Feed...");
                    downloadNewsFeedFromWeb();
                }
            }
        });
    }

    /**
     * Downloads the NewsFeed from the web and saves it to a file.
     */
    public void downloadNewsFeedFromWeb() {
        MainActivity.logVerbose("Downloading " + feedName + " News Feed from Web...");
        WebFileDownloader.downloadFromURLAsDocument(feedUrl, new WebFileDownloaderAdapter() {
            @Override
            public void fileDownloadedAsDocument(URL url, Document document) {
                MainActivity.logVerbose(feedName + " News Feed has been Downloaded.");
                // Save the feed to file...
                try {
                    MainActivity.logVerbose("Saving " + feedName + " News Feed to file...");

                    manager.getMainActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE).edit().putInt(getPreferencesTag(), fileSizeOnWeb).apply();

                    FileOutputStream fileOutputStream = new FileOutputStream(new File(manager.getMainActivity().getFilesDir(), getFileName()));

                    //Converts the Document into a file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult(fileOutputStream);
                    transformer.transform(source, result);

                    fileOutputStream.close();
                } catch (IOException | TransformerException e) {
                    e.printStackTrace();
                }

                updateFeed(document);
            }
        });
    }

    /**
     * Updates the feed with a new list of articles from the given document,
     * and notifies all listeners of an update to the feed.
     * This method is used when downloading the feed from the web or restoring it from file.
     *
     * @param document The new document to build the list of articles from.
     */
    public void updateFeed(Document document) {
        this.articlesList = manager.buildFeedFromDocument(document);

        manager.notifyListenersOfUpdate(this);
    }
}
