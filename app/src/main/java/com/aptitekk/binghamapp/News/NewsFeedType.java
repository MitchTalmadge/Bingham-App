package com.aptitekk.binghamapp.News;

import java.net.MalformedURLException;
import java.net.URL;

public enum NewsFeedType {

    ANNOUNCEMENTS("Announcements", "https://www.binghamminers.org/apps/news/news_rss.jsp"),
    PROSPECTOR("Prospector", "https://binghamprospector.org/feed");

    private String feedName;
    private URL feedUrl;

    NewsFeedType(String feedName, String feedUrl) {

        this.feedName = feedName;
        try {
            this.feedUrl = new URL(feedUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getFeedName() {
        return feedName;
    }

    public URL getFeedUrl() {
        return feedUrl;
    }
}
