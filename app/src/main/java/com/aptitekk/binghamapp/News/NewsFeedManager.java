package com.aptitekk.binghamapp.News;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * News Feed Manager
 */
public class NewsFeedManager {

    final static boolean verbose = true;

    private MainActivity mainActivity;

    ArrayList<NewsFeed> newsFeeds;
    ArrayList<NewsFeedUpdateListener> newsFeedUpdateListeners = new ArrayList<>();

    public NewsFeedManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        initializeFeeds();
    }

    /**
     * Creates a new NewsFeed list and populates it with the default NewsFeeds
     */
    private void initializeFeeds() {
        Log.i(MainActivity.LOG_NAME, "Initializing NewsFeeds...\n");
        newsFeeds = new ArrayList<>();

        for(NewsFeedType feedType : NewsFeedType.values()) //Add all feed types from NewsFeedType enum.
            newsFeeds.add(new NewsFeed(this, feedType));
    }

    /**
     * @return A list of initialized NewsFeeds.
     */
    public ArrayList<NewsFeed> getNewsFeeds() {
        return newsFeeds;
    }

    private static void logInfo(String msg) {
        if (verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    /**
     * Adds a NewsFeedUpdateListener if not already added (Must be instanceof Fragment)
     *
     * @param listener the listener to add.
     */
    public void addNewsFeedUpdateListener(NewsFeedUpdateListener listener) {
        if (!newsFeedUpdateListeners.contains(listener) && listener instanceof Fragment) {
            newsFeedUpdateListeners.add(listener);

            for (NewsFeed feed : newsFeeds)
                if (feed.getArticlesList() != null)
                    listener.onNewsFeedUpdated(feed); //If the feeds are already populated, send the listener the feed immediately.
        }
    }

    /**
     * Notifies all listeners of an update to a feed
     *
     * @param feed The NewsFeed that was updated.
     */
    protected void notifyListenersOfUpdate(NewsFeed feed) {
        for (NewsFeedUpdateListener listener : newsFeedUpdateListeners) {
            if (listener != null && ((Fragment) listener).isAdded()) {
                listener.onNewsFeedUpdated(feed);
            }
        }
    }

    public ArrayList<NewsArticle> buildFeedFromDocument(Document document) {
        if (document == null) {
            return null;
        }

        ArrayList<NewsArticle> result = new ArrayList<>();

        document.getDocumentElement().normalize();

        NodeList nList = document.getElementsByTagName("item");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            logInfo("Current Element : " + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                Node articleTitleNode = eElement.getElementsByTagName("title").item(0);
                Node articleLinkNode = eElement.getElementsByTagName("link").item(0);
                Node articleGuidNode = eElement.getElementsByTagName("guid").item(0);
                Node articleDescriptionNode = eElement.getElementsByTagName("description").item(0);
                Node articlePublishDateNode = eElement.getElementsByTagName("pubDate").item(0);

                result.add(new NewsArticle(articleTitleNode != null ? articleTitleNode.getTextContent() : null,
                        articleLinkNode != null ? articleLinkNode.getTextContent() : null,
                        articleGuidNode != null ? articleGuidNode.getTextContent() : null,
                        articleDescriptionNode != null ? articleDescriptionNode.getTextContent() : null,
                        articlePublishDateNode != null ? articlePublishDateNode.getTextContent() : null));

            }
        }
        return result;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
