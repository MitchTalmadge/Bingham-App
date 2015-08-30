package com.aptitekk.binghamapp.rssnewsfeed;

import android.os.AsyncTask;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.rssnewsfeed.newsFeeds.Announcements;
import com.aptitekk.binghamapp.rssnewsfeed.newsFeeds.Prospector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * RSS News Feed Downloader / Displayer
 */
public class RSSNewsFeedManager {

    private static NewsFeedSizeListener newsFeedSizeListener;

    final boolean verbose = false;

    public static final Class<? extends NewsFeed>[] NEWS_FEED_CLASSES = new Class[]{Announcements.class, Prospector.class};
    NewsFeed[] newsFeeds = new NewsFeed[NEWS_FEED_CLASSES.length];

    /**
     * Inits all feeds
     */
    public RSSNewsFeedManager() {
        Log.i(MainActivity.LOG_NAME, "Populating Articles...\n");
        try {
            for (int i = 0; i < NEWS_FEED_CLASSES.length; i++) {
                newsFeeds[i] = NEWS_FEED_CLASSES[i].newInstance();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void determineRetrieval(NewsFeed newsFeed,
                                          int lastNewsFeedUpdateSize,
                                          int newsFeedSize, Callable<Void> newsFeedCallable,
                                          File directory) {
        if (lastNewsFeedUpdateSize == 0 || newsFeedSize != lastNewsFeedUpdateSize) { // If we have never downloaded the feed before or the feed on the website is a different size...
            Log.v(MainActivity.LOG_NAME, "News feed is out of date. Downloading Feed...");

            newsFeed.setRefresh(newsFeedCallable);
            try {
                newsFeed.fetch();
            } catch (NewsFeedFetchException e) {
                e.printStackTrace();
            }
        } else { // We already have the latest news... Lets retrieve the file and create a feed from it.
            File newsFeedFile = new File(directory, "news.feed");

            if (newsFeedFile.exists()) {
                Log.v(MainActivity.LOG_NAME, "Restoring news feed from file...");
                try {
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(newsFeedFile);
                    newsFeed.loadFeedFromFile(document);
                } catch (SAXException | IOException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(MainActivity.LOG_NAME, "Could not restore news feed from file.");
                newsFeed.setRefresh(newsFeedCallable);
                try {
                    newsFeed.fetch();
                } catch (NewsFeedFetchException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public NewsFeed[] getNewsFeeds() {
        return newsFeeds;
    }
    public List<NewsArticle> getNewsArticles(NewsFeed feed) {
        return feed.getArticles();
    }
    public NewsFeed getNewsFeedByName(String name) {
        for(NewsFeed feed : newsFeeds) {
            if(feed.getName().contains(name)) {
                return feed;
            }
        }
        return null;
    }

    /**
     * Gets the size of the news feed on the web.
     */
    public static void getNewsFeedSize(NewsFeedSizeListener listener, NewsFeed newsFeed) {
        newsFeedSizeListener = listener;

        new getNewsFeedSizeTask(newsFeed).execute(newsFeed.getURL());
    }

    public interface NewsFeedSizeListener {

        void onGetNewsFeedSize(final int newsFeedSize, NewsFeed newsFeed);

    }

    private static class getNewsFeedSizeTask extends AsyncTask<String, Void, Integer> {

        NewsFeed newsFeed;

        public getNewsFeedSizeTask(NewsFeed newsFeed) {
            this.newsFeed = newsFeed;
        }

        @Override
        protected Integer doInBackground(String... url) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url[0]).openConnection();
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
        protected void onPostExecute(Integer newsFeedSize) {
            if (newsFeedSizeListener != null) {
                newsFeedSizeListener.onGetNewsFeedSize(newsFeedSize, newsFeed);
            }
        }
    }

    private void logInfo(String msg) {
        if (verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    public static ArrayList<NewsArticle> buildFeedFromDocument(Document document) {
        if (document == null) {
            return null;
        }

        ArrayList<NewsArticle> result = new ArrayList<>();

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        document.getDocumentElement().normalize();

        //logInfo("Root element :" + document.getDocumentElement().getNodeName());

        //logInfo("Title : " + document.getElementsByTagName("title").item(0).getTextContent());

        NodeList nList = document.getElementsByTagName("item");

        //logInfo("----------------------------");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            //logInfo("Current Element : " + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                //logInfo("Title : " + eElement.getElementsByTagName("title").item(0).getTextContent());
                //logInfo("Link : " + eElement.getElementsByTagName("link").item(0).getTextContent());
                //logInfo("guid  : " + eElement.getElementsByTagName("guid").item(0).getTextContent());
                //logInfo("Description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                //logInfo("pubDate : " + eElement.getElementsByTagName("pubDate").item(0).getTextContent() + "\n");

                result.add(new NewsArticle(eElement.getElementsByTagName("title").item(0).getTextContent(),
                        eElement.getElementsByTagName("link").item(0).getTextContent(),
                        eElement.getElementsByTagName("guid").item(0).getTextContent(),
                        eElement.getElementsByTagName("description").item(0).getTextContent(),
                        eElement.getElementsByTagName("pubDate").item(0).getTextContent()));

            }
        }
        return result;
    }

    public static class DownloadRSSFeedTask extends AsyncTask<String, Void, Document> {

        Callable<Void> refresh;
        Document document;
        ArrayList<NewsArticle> articles;
        NewsFeed parent;

        public DownloadRSSFeedTask(NewsFeed nF, Callable<Void> refresh) {
            this.refresh = refresh;
            this.parent = nF;
        }

        public Document getDocument() {
            return document;
        }

        public ArrayList<NewsArticle> getArticles() {
            return articles;
        }

        @Override
        protected Document doInBackground(String... url) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(url[0]);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Document document) {
            this.document = document;
            this.articles = buildFeedFromDocument(document);
            try {
                this.parent.update(document, articles);
                refresh.call();
                parent.setReady(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
