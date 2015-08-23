package com.aptitekk.binghamapp.rssGoogleCalendar;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aptitekk.binghamapp.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.cards.CustomCountdownCardExpand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

public class CalendarDog {

    private static EventsFeedSizeListener eventsFeedSizeListener;
    private JSONObject jsonObject;

    public JSONObject getJSONObject() {
        return jsonObject;
    }

    public enum FetchType {
        JSON,
        XML,
        ICAL
    }

    ArrayList<CalendarEvent> events = new ArrayList<>();

    Callable<Void> refresh;

    final boolean verbose = true;

    public static String BINGHAM_GOOGLE_CALENDAR = "https://www.googleapis.com/calendar/v3/calendars/jordandistrict.org_o4d9atn49tbcvmc29451bailf0@group.calendar.google.com/events?maxResults=2500&timeMin=2015-08-01T00:00:00-07:00&singleEvents=true&key=AIzaSyBYdbs9jPSdqJRASyjEC7E6JjRTp20UxQk";
    public final static String BINGHAM_GOOGLE_CALENDAR_XML = "https://www.google.com/calendar/feeds/jordandistrict.org_o4d9atn49tbcvmc29451bailf0%40group.calendar.google.com/public/basic";
    public final static String BINGHAM_GOOGLE_CALENDAR_ICAL = "https://www.google.com/calendar/ical/jordandistrict.org_o4d9atn49tbcvmc29451bailf0%40group.calendar.google.com/public/basic.ics";

    public CalendarDog(Callable<Void> refresh, FetchType type) {
        BINGHAM_GOOGLE_CALENDAR = generateJSONURL();
        Log.i(MainActivity.LOG_NAME, "Populating Calendar...\n");
        this.refresh = refresh;
        try {
            switch (type) {
                case JSON:
                    FetchJSONTask jsonTask = new FetchJSONTask();
                    jsonTask.execute(BINGHAM_GOOGLE_CALENDAR);
                    break;
                case XML:
                    FetchXMLTask XMLtask = new FetchXMLTask();
                    XMLtask.execute(BINGHAM_GOOGLE_CALENDAR_XML);
                    break;
                case ICAL:
                    FetchICalTask ICALtask = new FetchICalTask();
                    ICALtask.execute(BINGHAM_GOOGLE_CALENDAR_ICAL);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateJSONURL() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String timeZone = TimeZone.getDefault().getID();
        String url = "https://www.googleapis.com/calendar/v3/calendars/jordandistrict.org_o4d9atn49tbcvmc29451bailf0@group.calendar.google.com/events?maxResults=2500&" +
                "timeMin=" + format.format(c.getTime()).replace(" ", "T") + "Z" + "&timeZone=" + timeZone + "&singleEvents=true&key=AIzaSyBYdbs9jPSdqJRASyjEC7E6JjRTp20UxQk";
        return url;
    }

    public CalendarDog(JSONObject jsonObject) {
        buildFromJSONObject(jsonObject);
    }

    /**
     * Gets the size of the events feed on the web for specified fetch type
     *
     * @param fetchType type of feed to check
     */
    public static void getEventsFeedSize(FetchType fetchType, EventsFeedSizeListener listener) {

        eventsFeedSizeListener = listener;

        String url = null;
        switch (fetchType) {
            case JSON:
                url = generateJSONURL();
                break;
            case XML:
                url = BINGHAM_GOOGLE_CALENDAR_XML;
                break;
            case ICAL:
                url = BINGHAM_GOOGLE_CALENDAR_ICAL;
                break;
        }

        new getEventsFeedSizeTask().execute(url);
    }

    public interface EventsFeedSizeListener {

        void onGetEventsFeedSize(final int eventsFeedSize);

    }

    private static class getEventsFeedSizeTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... url) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url[0]).openConnection();
                conn.setRequestMethod("HEAD");
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
        protected void onPostExecute(Integer eventsFeedSize) {
            if (eventsFeedSizeListener != null) {
                eventsFeedSizeListener.onGetEventsFeedSize(eventsFeedSize);
            }
        }
    }

    public static int findPositionFromDate(ArrayList<CalendarEvent> events, Date date) {
        long minDiff = -1, currentTime = date.getTime();
        int minDate = 0;
        for (int i = 0; i < events.size(); i++) {
            long diff = Math.abs(currentTime - events.get(i).getDate().getTime().getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = i;
            }
        }
        return minDate;
    }

    public static Date getNearestDate(ArrayList<Date> dates, Date currentDate) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }

    public static int findNextAorBDay(ArrayList<CalendarEvent> events) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().contains("A Day") || events.get(i).getTitle().contains("B Day"))
                return i;
        }
        return -1;
    }

    public static int findNextTargetByIndex(ArrayList<CalendarEvent> events, CustomCountdownCardExpand.CountdownTarget target) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().toLowerCase().contains(target.getValue()))
                return i;
        }
        return -1;
    }

    public static BellSchedule determineSchedule(Fragment fragment, ArrayList<CalendarEvent> events, Calendar dateTime) {

        ArrayList<CalendarEvent> eventsOfDay = getEventsForDay(events, dateTime);

        if (eventsOfDay.isEmpty())
            return null;

        Collections.rotate(eventsOfDay, -1); // move top A/B Day labels to bottom so potential assembly events may appear first.

        for (CalendarEvent e : eventsOfDay) {
            if (e.getTitle().toLowerCase().contains("schedule") && e.getTitle().toLowerCase().contains("assembly")) { // Assembly Event Found!
                String schedule = e.getTitle().toLowerCase().split("-")[1].split("assembly")[0].replaceFirst("\\s+$", "");

                //DETERMINE MORNING/AFTERNOON
                int timeOfDay = e.getDate().get(Calendar.HOUR_OF_DAY);
                if ((timeOfDay >= 0 && timeOfDay < 12) || schedule.contains("AM")) { // MORNING
                    schedule = "Morning (" + schedule.replace("AM", "") + ")";
                } else if ((timeOfDay >= 12 && timeOfDay < 16) || schedule.contains("PM")) { // AFTERNOON
                    schedule = "Afternoon (" + schedule.replace("PM", "") + ")";
                } else {
                    return null;
                }
                for (int i = 0; i < fragment.getResources().getStringArray(R.array.assemblyBellSchedules).length; i++) {
                    if (fragment.getResources().getStringArray(R.array.assemblyBellSchedules)[i].split("_")[1].equalsIgnoreCase(schedule)) {
                        String[] scheduleTimeArray = null;
                        switch (i) {
                            case 0:
                                scheduleTimeArray = fragment.getResources().getStringArray(R.array.assemblyBellSchedule0);
                                break;
                            case 1:
                                scheduleTimeArray = fragment.getResources().getStringArray(R.array.assemblyBellSchedule1);
                                break;
                            case 2:
                                scheduleTimeArray = fragment.getResources().getStringArray(R.array.assemblyBellSchedule2);
                                break;
                            case 3:
                                scheduleTimeArray = fragment.getResources().getStringArray(R.array.assemblyBellSchedule3);
                                break;
                        }
                        return new BellSchedule(fragment.getResources().getStringArray(R.array.assemblyBellSchedules)[i], scheduleTimeArray);
                    }
                }
            } else if (e.getTitle().contains("A Day") || e.getTitle().contains("B Day")) {
                if (dateTime.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[1], fragment.getResources().getStringArray(R.array.regularBellSchedule1));
                }
                return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[0], fragment.getResources().getStringArray(R.array.regularBellSchedule0));
            }

        }
        return null;
    }

    public static ArrayList<CalendarEvent> getEventsForDay(List<CalendarEvent> events, Calendar dayToMatch) {
        ArrayList<CalendarEvent> result = new ArrayList<>();
        for (CalendarEvent e : events) {
            if (e.getDate().get(Calendar.YEAR) == dayToMatch.get(Calendar.YEAR) &&
                    e.getDate().get(Calendar.MONTH) == dayToMatch.get(Calendar.MONTH) &&
                    e.getDate().get(Calendar.DAY_OF_MONTH) == dayToMatch.get(Calendar.DAY_OF_MONTH)) {
                result.add(e);
            }
        }
        return result;
    }

    public static boolean isSameDay(CalendarEvent e1, CalendarEvent e2) {
        if (e1.getDate().get(Calendar.YEAR) == e2.getDate().get(Calendar.YEAR) &&
                e1.getDate().get(Calendar.MONTH) == e2.getDate().get(Calendar.MONTH) &&
                e1.getDate().get(Calendar.DAY_OF_MONTH) == e2.getDate().get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return false;
    }

    public ArrayList<CalendarEvent> getEvents() {
        return this.events;
    }

    private void logDebug(String msg) {
        if (verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    private class FetchJSONTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                Log.i(MainActivity.LOG_NAME, "JSON Parsing");
                URL url = new URL(urls[0]);
                logDebug(BINGHAM_GOOGLE_CALENDAR);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(1000);
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                return new JSONObject(responseStrBuilder.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            buildFromJSONObject(jsonObject);
            try {
                refresh.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void buildFromJSONObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        try {
            JSONArray arr = jsonObject.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                String link = arr.getJSONObject(i).getString("htmlLink");
                String summary = arr.getJSONObject(i).getString("summary");

                String location;
                String rawStartTime;
                String rawEndTime;
                DateFormat format;
                DateFormat endTimeFormat;
                Calendar date = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();

                try {
                    location = arr.getJSONObject(i).getString("location");
                } catch (JSONException e) { // A/B days dont have locations x)
                    location = "";
                }
                try {
                    rawStartTime = arr.getJSONObject(i).getJSONObject("start").getString("dateTime");
                    rawEndTime = arr.getJSONObject(i).getJSONObject("end").getString("dateTime");
                    format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZZZZZZ");
                    endTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZZZZZZ");
                } catch (JSONException e) { //Date not DateTime
                    rawStartTime = arr.getJSONObject(i).getJSONObject("start").getString("date");
                    rawEndTime = arr.getJSONObject(i).getJSONObject("end").getString("date");
                    format = new SimpleDateFormat("yyyy-MM-dd");
                    endTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
                }
                try {
                    date.setTime(format.parse(rawStartTime.replace("T", " ")));
                    endTime.setTime(endTimeFormat.parse(rawEndTime.replace("T", " ")));
                    events.add(new CalendarEvent(summary,
                            date,
                            endTime,
                            location,
                            link
                    ));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class FetchICalTask extends AsyncTask<String, Integer, ICalendar> {

        int percentProgress;

        @Override
        protected void onPreExecute() {
            percentProgress = 0;
        }

        @Override
        protected ICalendar doInBackground(String... given_url) {
            Log.i(MainActivity.LOG_NAME, "iCal Parsing");
            InputStream stream;
            try {
                URL url = new URL(given_url[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setDoInput(true);
                conn.connect();
                stream = conn.getInputStream();
                /*long totalLength = conn.getContentLength();
                while(stream.)
                percentProgress = (int) (totalBytesRead * 100 / totalLength);*/
                return Biweekly.parse(stream).first();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            //progressBar.setProgress(values[0]);
            //txt_percentage.setText("downloading " +values[0]+"%");

        }


        @Override
        protected void onPostExecute(ICalendar ical) {

            // convert ical to our CalendarEvents
            for (VEvent e : ical.getEvents()) {
                try {
                    Date dDate;
                    if (e.getDateEnd() == null)
                        dDate = e.getDateEnd().getValue(); //FIXME: Trying to access the getValue method of a null object..? -Mitch
                    else
                        dDate = e.getDateStart().getValue();
                    Date dEndTime;
                    if (e.getDateEnd() == null)
                        dEndTime = dDate;
                    else
                        dEndTime = e.getDateEnd().getValue();
                    Calendar date = Calendar.getInstance();
                    Calendar endTime = Calendar.getInstance();

                    if (dDate.before(date.getTime())) { //if this event we're parsing is before today, drop it
                        continue;
                    }

                    date.setTime(dDate);
                    endTime.setTime(dEndTime);
                    events.add(new CalendarEvent(e.getSummary().getValue(),
                            date,
                            endTime,
                            e.getLocation().getValue(),
                            null
                    ));
                } catch (NullPointerException ee) {
                    Log.e(MainActivity.LOG_NAME, "Failed on " + e.getSummary().getValue());
                    //ee.printStackTrace();
                }
            }
            try {
                refresh.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class FetchXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... url) {
            Log.i(MainActivity.LOG_NAME, "XML Parsing");
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
        protected void onPostExecute(Document docu) {
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            docu.getDocumentElement().normalize();

            //logDebug("Root element :" + doc.getDocumentElement().getNodeName());

            //logDebug("Title : " + doc.getElementsByTagName("title").item(0).getTextContent());

            NodeList nList = docu.getElementsByTagName("entry");

            //logDebug("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                logDebug("Current Element : " + nNode.getNodeName() + " --------------------");

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    logDebug("Title : " + eElement.getElementsByTagName("title").item(0).getTextContent());

                    String rawSummary = eElement.getElementsByTagName("summary").item(0).getTextContent();

                    if (!rawSummary.contains("Recurring Event")) {

                        rawSummary = rawSummary.replace("&nbsp;", " ");
                        //rawSummary.replace("&lt;br&gt;","");
                        String[] splitSummary = rawSummary.split("<br>");
                        String rawStartDate = splitSummary[0].replace("When: ", "").split("to")[0];

                        //logDebug("rawStartDate: " + rawStartDate);

                        String rawEndTimeAndTimeZone = splitSummary[0].replace("When: ", "").split("to")[1];

                        //logDebug("rawEndTimeAndTimeZone: " + rawEndTimeAndTimeZone.replace("\n",""));

                        String rawTimeZone = rawEndTimeAndTimeZone.split("\n")[1];
                        String rawEndTime = rawEndTimeAndTimeZone.split("\n")[0].replace(" ", "");

                        if (rawEndTime.length() < 7 && rawEndTime.contains(":")) {
                            rawEndTime = String.format("%7s", rawEndTime);
                            rawEndTime = rawEndTime.replace(" ", "0");
                        }

                        //logDebug("rawTimeZone: " + rawTimeZone);

                        String where = splitSummary[2].replace("Where: ", ""); // We dont need [1] cus the Who is always public....and we dont need that

                        if (where.contains("Who")) {
                            where = splitSummary[3].replace("Where: ", "");
                        }


                        //logDebug("where: " + where);

                        String link = ((Element) nNode.getChildNodes().item(8)).getAttribute("href");
                        DateFormat format = (rawStartDate.contains(":") ? new SimpleDateFormat("EEE MMM dd, yyyy hh:mmaa zzz") : new SimpleDateFormat("EEE MMM dd, yyyy hhaa zzz"));
                        DateFormat endTimeFormat = (rawEndTime.contains(":") ? new SimpleDateFormat("hh:mmaa") : new SimpleDateFormat("hhaa"));
                        Calendar date = Calendar.getInstance();
                        Calendar endTime = Calendar.getInstance();
                        try {
                            logDebug("Start time pre-parse: \"" + rawStartDate + rawTimeZone + "\"");
                            logDebug("End time pre-parse: \"" + rawEndTime + "\"");
                            date.setTime(format.parse(rawStartDate + rawTimeZone));
                            endTime.setTime(endTimeFormat.parse(rawEndTime));
                            endTime.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
                            logDebug("Start time post-parse: \"" + format.format(date.getTime()) + "\"");
                            logDebug("End time post-parse: \"" + format.format(endTime.getTime()) + "\'");
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
