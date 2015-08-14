package com.aptitekk.binghamapp.rssnewsfeed;

import android.os.AsyncTask;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * RSS News Feed Downloader / Displayer
 */
public class RSSNewsFeed {

    ArrayList<NewsArticle> articles = new ArrayList<>();

    Document document;

    Callable<Void> refresh;

    final boolean verbose = false;

    public RSSNewsFeed(Callable<Void> refresh) {
        Log.i(MainActivity.LOG_NAME, "Populating Articles...\n");
        this.refresh = refresh;
        try {
            DownloadRSSFeedTask task = new DownloadRSSFeedTask();
            task.execute("http://www.binghamminers.org/apps/news/news_rss.jsp?id=0");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RSSNewsFeed(Document document) {
        buildFeedFromDocument(document);
    }

    public Document getDocument() {
        return this.document;
    }

    public ArrayList<NewsArticle> getNewsArticles() {
        Log.i(MainActivity.LOG_NAME, "\nThere are " + this.articles.size() + " articles in feed");
        return this.articles;
    }

    private void logInfo(String msg) {
        if (verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    private void buildFeedFromDocument(Document document) {
        this.document = document;

        if (this.document == null) {
            try {
                refresh.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        this.document.getDocumentElement().normalize();

        logInfo("Root element :" + this.document.getDocumentElement().getNodeName());

        logInfo("Title : " + this.document.getElementsByTagName("title").item(0).getTextContent());

        NodeList nList = this.document.getElementsByTagName("item");

        logInfo("----------------------------");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            logInfo("Current Element : " + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                logInfo("Title : " + eElement.getElementsByTagName("title").item(0).getTextContent());
                logInfo("Link : " + eElement.getElementsByTagName("link").item(0).getTextContent());
                logInfo("guid  : " + eElement.getElementsByTagName("guid").item(0).getTextContent());
                logInfo("Description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                logInfo("pubDate : " + eElement.getElementsByTagName("pubDate").item(0).getTextContent() + "\n");

                articles.add(new NewsArticle(eElement.getElementsByTagName("title").item(0).getTextContent(),
                        eElement.getElementsByTagName("link").item(0).getTextContent(),
                        eElement.getElementsByTagName("guid").item(0).getTextContent(),
                        eElement.getElementsByTagName("description").item(0).getTextContent(),
                        eElement.getElementsByTagName("pubDate").item(0).getTextContent()));

            }
        }
        try {
            refresh.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DownloadRSSFeedTask extends AsyncTask<String, Void, Document> {

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
            buildFeedFromDocument(document);
        }
    }

}
