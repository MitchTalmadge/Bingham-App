package com.aptitekk.binghamapp.rssnewsfeed.newsFeeds;

import com.aptitekk.binghamapp.rssnewsfeed.NewsFeed;

import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * Created by kevint on 8/28/2015.
 */
public class Announcements extends NewsFeed{

    final String name = "Announcements";
    final String url = "http://www.binghamminers.org/apps/news/news_rss.jsp?unused=" + Calendar.getInstance().get(Calendar.MILLISECOND);

    public Announcements(Callable<Void> refresh) {
        super(refresh);
    }

    public Announcements() {super();}

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getName() {
        return name;
    }

}
