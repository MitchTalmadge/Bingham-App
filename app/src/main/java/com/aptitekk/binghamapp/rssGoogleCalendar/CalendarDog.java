package com.aptitekk.binghamapp.rssGoogleCalendar;

import android.os.AsyncTask;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by kevint on 8/8/2015.
 */
public class CalendarDog {

    ArrayList<CalendarEvent> events = new ArrayList<>();

    Document doc;

    Callable<Void> refresh;

    final boolean verbose = true;

    public CalendarDog(String rssURL, Callable<Void> refresh) {
        Log.i(MainActivity.LOG_NAME, "Populating Calendar...\n");
        this.refresh = refresh;
        try {
            FetchXMLTask task = new FetchXMLTask();
            task.execute(rssURL);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<CalendarEvent> getEvents() {
        Log.i(MainActivity.LOG_NAME, "\nThere are " + this.events.size() + " events in feed");
        return this.events;
    }

    private void logInfo(String msg) {
        if(verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    private class FetchXMLTask extends AsyncTask<String, Void, Document> {

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

            //logInfo("Root element :" + doc.getDocumentElement().getNodeName());

            //logInfo("Title : " + doc.getElementsByTagName("title").item(0).getTextContent());

            NodeList nList = doc.getElementsByTagName("entry");

            //logInfo("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                logInfo("Current Element : " + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    logInfo("Title : " + eElement.getElementsByTagName("title").item(0).getTextContent());

                    String rawSummary = eElement.getElementsByTagName("summary").item(0).getTextContent();

                    if(!rawSummary.contains("Recurring Event")) {

                        rawSummary = rawSummary.replace("&nbsp;", " ");
                        //rawSummary.replace("&lt;br&gt;","");
                        String[] splitSummary = rawSummary.split("<br>");
                        String rawStartDate = splitSummary[0].replace("When: ", "").split("to")[0];

                        logInfo("rawStartDate: " + rawStartDate);

                        String rawEndTimeAndTimeZone = splitSummary[0].replace("When: ", "").split("to")[1];

                        logInfo("rawEndTimeAndTimeZone: " + rawEndTimeAndTimeZone);

                        String rawTimeZone = rawEndTimeAndTimeZone.split("\n")[1];

                        logInfo("rawTimeZone: " + rawTimeZone);

                        String where = splitSummary[2].replace("Where: ", ""); // We dont need [1] cus the Who is always public....and we dont need that

                        logInfo("where: " + where);

                        String link = ((Element) nNode.getChildNodes().item(8)).getAttribute("href");

                        DateFormat format = new SimpleDateFormat("EEE MMM dd, yyyy hh:mmaa zzz");
                        DateFormat endTimeFormat = new SimpleDateFormat("hh:mmaa  zzz");
                        try {
                            Date date = format.parse(rawStartDate + rawTimeZone);
                            Date endTime = endTimeFormat.parse(rawEndTimeAndTimeZone.replace("\n", " ").replace("\"", ""));

                            events.add(new CalendarEvent(eElement.getElementsByTagName("title").item(0).getTextContent(),
                                    date,
                                    endTime,
                                    where,
                                    link
                            ));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

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
