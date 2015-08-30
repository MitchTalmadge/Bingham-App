package com.aptitekk.binghamapp.rssnewsfeed;

import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeedManager.buildFeedFromDocument;

/**
 * Created by kevint on 8/28/2015.
 */
public abstract class NewsFeed {

    private static RSSNewsFeedManager.NewsFeedSizeListener newsFeedSizeListener;

    ArrayList<NewsArticle> articles = new ArrayList<>();
    Document document;
    Callable<Void> refresh;
    boolean ready;

    /**
     * Constructor for downloading the news feed
     *
     * @param refresh
     */
    public NewsFeed(Callable<Void> refresh) {
        this.refresh = refresh;
    }

    /**
     * Constructor for loading news feed from file
     *
     */
    public NewsFeed() {}

    public void update(Document document, ArrayList<NewsArticle> articles) {
        this.document = document;
        this.articles = articles;
    }

    public abstract String getURL();
    public abstract String getName();
    public String getFileName() {
        return (getName().replace(" ", "").toLowerCase() + ".feed");
    }
    public String getPreferencesTag() {
        return ("last" + getName().replace(" ", "").toLowerCase() + "size");
    }

    public void fetch() throws NewsFeedFetchException {
        if(getRefresh() == null) {
            Log.e(MainActivity.LOG_NAME, "CANNOT FETCH NEWS");
            throw new NewsFeedFetchException("Refresh method is null");
        }
        Log.i(MainActivity.LOG_NAME, "Fetching news for " + getName());
        RSSNewsFeedManager.DownloadRSSFeedTask task = new RSSNewsFeedManager.DownloadRSSFeedTask(this, getRefresh());
        task.execute(getURL());
    }
    public void loadFeedFromFile(Document document) {
        setDocument(document);
        setArticles(buildFeedFromDocument(document));
    }

    public boolean isReady() {
        return ready;
    }

    protected void setReady(boolean ready) {
        this.ready = ready;
    }

    public ArrayList<NewsArticle> getArticles() {
        return articles;
    }
    protected void setArticles(ArrayList<NewsArticle> articles) {
        this.articles = articles;
    }
    public Document getDocument() {
        return document;
    }
    protected void setDocument(Document doc) {
        document = doc;
    }
    public Callable<Void> getRefresh() {
        return refresh;
    }
    protected void setRefresh(Callable<Void> voidCallable) { this.refresh = voidCallable; }


}
