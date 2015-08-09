package com.aptitekk.binghamapp.rssnewsfeed;

import java.util.concurrent.Callable;

/**
 * Display Handler for RSS Feed
 *
 * @author kevint <br>
 *         Created Aug 6, 2015 <br> <br>
 *         Copyright 2015
 */
public class RSSNewsFeed {

    private RSSManager rssManager;

    public RSSNewsFeed(Callable<Void> refresh) {
        rssManager = new RSSManager("http://www.binghamminers.org/apps/news/news_rss.jsp?id=0", refresh);
    }

    public RSSManager getRssManager() {
        return rssManager;
    }


}
