package com.aptitekk.binghamapp.rssnewsfeed.newsFeeds;

import com.aptitekk.binghamapp.rssnewsfeed.NewsFeed;

import java.util.concurrent.Callable;

/**
 * Created by kevint on 8/28/2015.
 */
public class Prospector extends NewsFeed {

    final String name = "Prospector";
    final String url = "http://binghamprospector.org/feed/";

    public Prospector(Callable<Void> refresh) {
        super(refresh);
    }

    public Prospector() {super();}

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getName() {
        return name;
    }


}
