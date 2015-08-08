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
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * RSSManager populates a list of news articles from a given RSS Feed
 *
 * @author kevint <br>
 *         Created Aug 6, 2015 <br> <br>
 *         Copyright 2015
 */
public class RSSManager {

    ArrayList<NewsArticle> articles = new ArrayList<NewsArticle>();

    Document doc;

    Logger log = Logger.getLogger("RSSNewsFeed");

    Callable<Void> refresh;

    public RSSManager(String rssURL, Callable<Void> refresh) {
        Log.i(MainActivity.LOG_NAME, "Populating Articles...\n");
        this.refresh = refresh;
        try {
            DownloadRSSFeedTask task = new DownloadRSSFeedTask();
            task.execute(rssURL);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<NewsArticle> getNewsArticles() {
        Log.i(MainActivity.LOG_NAME, "\nThere are " + this.articles.size() + " articles in feed");
        return this.articles;
    }

    private class DownloadRSSFeedTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... url) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(url[0]);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Document docu) {
            doc = docu;
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            Log.i(MainActivity.LOG_NAME, "Root element :" + doc.getDocumentElement().getNodeName());

            Log.i(MainActivity.LOG_NAME, "Title : " + doc.getElementsByTagName("title").item(0).getTextContent());

            NodeList nList = doc.getElementsByTagName("item");

            Log.i(MainActivity.LOG_NAME, "----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                Log.i(MainActivity.LOG_NAME, "Current Element : " + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    Log.i(MainActivity.LOG_NAME, "Title : " + eElement.getElementsByTagName("title").item(0).getTextContent());
                    Log.i(MainActivity.LOG_NAME, "Link : " + eElement.getElementsByTagName("link").item(0).getTextContent());
                    Log.i(MainActivity.LOG_NAME, "guid  : " + eElement.getElementsByTagName("guid").item(0).getTextContent());
                    Log.i(MainActivity.LOG_NAME, "Description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                    Log.i(MainActivity.LOG_NAME, "pubDate : " + eElement.getElementsByTagName("pubDate").item(0).getTextContent() + "\n");

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
    }
}
