package com.aptitekk.binghamapp.rssGoogleCalendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aptitekk.binghamapp.BellSchedulesFragmentClasses.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.WebViewFragment;
import com.aptitekk.binghamapp.cards.CountdownCard;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;

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

    List<CalendarEvent> events = new ArrayList<>();

    Callable<Void> refresh;

    final boolean verbose = true;

    public static String BINGHAM_GOOGLE_CALENDAR = "https://www.googleapis.com/calendar/v3/calendars/jordandistrict.org_o4d9atn49tbcvmc29451bailf0@group.calendar.google.com/events?maxResults=2500&timeMin=2015-08-01T00:00:00-07:00&singleEvents=true&key=AIzaSyBYdbs9jPSdqJRASyjEC7E6JjRTp20UxQk";
    public final static String BINGHAM_GOOGLE_CALENDAR_XML = "https://www.google.com/calendar/feeds/jordandistrict.org_o4d9atn49tbcvmc29451bailf0%40group.calendar.google.com/public/basic";
    public final static String BINGHAM_GOOGLE_CALENDAR_ICAL = "https://www.google.com/calendar/ical/jordandistrict.org_o4d9atn49tbcvmc29451bailf0%40group.calendar.google.com/public/basic.ics";

    boolean ready = false;

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

    public List<CalendarEvent> getEvents() {
        return this.events;
    }

    public boolean isReady() { return this.ready; }

    private void logDebug(String msg) {
        if (verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
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

    public static int findPositionFromDate(List<CalendarEvent> events, Date date) {
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

    public static Date getNearestDate(List<Date> dates, Date currentDate, boolean skipPastEvents) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {
            if ((currentTime > date.getTime()) && skipPastEvents) { // Skip any Dates that have already past
                continue;
            }
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }

    public static Date getNearestDateBySubject(BellSchedule.Subject subject, Date currentDate, boolean skipPastEvents) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        Date[] times = new Date[]{subject.getStartTime(), subject.getEndTime()};
        for (Date date : times) {
            if ((currentTime > date.getTime()) && skipPastEvents) { // Skip any Dates that have already past
                continue;
            }
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }

    public static Date getNearestDateBySubject(BellSchedule.Subject subject, Date currentDate, boolean skipPastEvents, boolean endTimePointer) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        Date[] times = new Date[]{subject.getStartTime(), subject.getEndTime()};
        for (Date date : times) {
            if ((currentTime > date.getTime()) && skipPastEvents) { // Skip any Dates that have already past
                continue;
            }
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;

                if(date.equals(times[1]))
                    endTimePointer = true;
                else
                    endTimePointer = false;
            }
        }
        return minDate;
    }

    public static CalendarEvent getNextEvent(List<CalendarEvent> events, Date currentDate, boolean excludeABDayLabel) {
        long minDiff = -1, currentTime = currentDate.getTime();
        CalendarEvent minDate = null;
        for (CalendarEvent date : events) {
            if (((date.getTitle().contains("A Day")) || (date.getTitle().contains("B Day"))) && excludeABDayLabel) {
                continue;
            }
            if (currentTime > date.getDate().getTimeInMillis()) { // Skip any Dates that have already past
                continue;
            }
            long diff = Math.abs(currentTime - date.getDate().getTimeInMillis());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }

    public static int findNextAorBDay(List<CalendarEvent> events) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().contains("A Day") || events.get(i).getTitle().contains("B Day"))
                return i;
        }
        return -1;
    }

    public static char isItAorBDay(List<CalendarEvent> sortedEvents, Calendar currentDateTime) {
        for (CalendarEvent event : sortedEvents) {
            if (CalendarEvent.eventMatchesDay(event, currentDateTime)) {
                if (event.getTitle().contains("A Day"))
                    return BellSchedule.A_DAY;
                else if (event.getTitle().contains("B Day"))
                    return BellSchedule.B_DAY;
            }
        }
        return BellSchedule.NONE_DAY;
    }

    public static int findNextTargetByIndex(List<CalendarEvent> events, CountdownCard.CountdownTarget target) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().toLowerCase().contains(target.getValue()))
                return i;
        }
        return -1;
    }

    public static BellSchedule determineSchedule(Fragment fragment, List<CalendarEvent> events, Calendar dateTime) {

        ArrayList<CalendarEvent> eventsOfDay = getEventsForDay(events, dateTime);

        if (eventsOfDay.isEmpty())
            return null;

        Collections.rotate(eventsOfDay, -1); // move top A/B Day labels to bottom so potential assembly events may appear first.

        for (CalendarEvent e : eventsOfDay) {
            if (e.getTitle().toLowerCase().contains("schedule") && e.getTitle().toLowerCase().contains("assembly")) { // Assembly Event Found!
                String schedule = e.getTitle().toLowerCase().split("-")[1].split("assembly")[0].replaceFirst("\\s+$", "");

                //DETERMINE MORNING/AFTERNOON
                int timeOfDay = e.getDate().get(Calendar.HOUR_OF_DAY);
                if ((timeOfDay >= 0 && timeOfDay < 12) && schedule.contains("A/B")) { // IF A/B is detected, theres only one type of "A/B" assembly in the mornings
                    return new BellSchedule(fragment.getResources().getStringArray(R.array.assemblyBellSchedules)[4], fragment.getResources().getStringArray(R.array.assemblyBellSchedule4));
                } else if ((timeOfDay >= 0 && timeOfDay < 12) || schedule.contains("AM")) { // MORNING
                    schedule = "Morning (" + schedule.replace("AM", "") + ")";
                } else if ((timeOfDay >= 12 && timeOfDay < 16) || schedule.contains("PM")) { // AFTERNOON
                    schedule = "Afternoon (" + schedule.replace("PM", "") + ")";
                } else {
                    return null;
                }
                for (int i = 0; i < fragment.getResources().getStringArray(R.array.assemblyBellSchedules).length; i++) {
                    if (fragment.getResources().getStringArray(R.array.assemblyBellSchedules)[i].split("_")[1].equalsIgnoreCase(schedule)) {// if string matches name
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
            } else if (e.getTitle().contains("A Day") || e.getTitle().contains("B Day")) { // Just a regular day
                if (dateTime.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) { // Friday schedule
                    return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[1], fragment.getResources().getStringArray(R.array.regularBellSchedule1));
                } else if (dateTime.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) { //If after school on thursday
                    try {
                        if (!hasSchoolEndedForDay(new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[0],
                                        fragment.getResources().getStringArray(R.array.regularBellSchedule0)),
                                dateTime)) {
                            return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[1], fragment.getResources().getStringArray(R.array.regularBellSchedule1));
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                        Log.e(MainActivity.LOG_NAME, "Reverting to regular schedule");
                    }

                }
                return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[0], fragment.getResources().getStringArray(R.array.regularBellSchedule0));
            }

        }
        return null;
    }

    public static boolean hasSchoolEndedForDay(BellSchedule regularSchedule, Calendar dateTime) throws ParseException {
        String rawEndDay = regularSchedule.getSubjectEndTimes()[regularSchedule.getSubjectEndTimes().length - 1]; //GRAB END TIME
        Date endTime = new SimpleDateFormat("MMM dd, yyyy hh:mm aa z", Locale.US).parse(SimpleDateFormat.getDateInstance().format(dateTime.getTime()) + " " + rawEndDay + " MDT");
        if (dateTime.getTime().after(endTime)) {
            return false;
        }
        return true;
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

    public static ArrayList<CalendarEvent> getEventsForDay(List<CalendarEvent> events, Calendar dayToMatch, boolean excludeABDayLabel) {
        ArrayList<CalendarEvent> result = new ArrayList<>();
        for (CalendarEvent e : events) {
            if (e.getDate().get(Calendar.YEAR) == dayToMatch.get(Calendar.YEAR) &&
                    e.getDate().get(Calendar.MONTH) == dayToMatch.get(Calendar.MONTH) &&
                    e.getDate().get(Calendar.DAY_OF_MONTH) == dayToMatch.get(Calendar.DAY_OF_MONTH)) {
                if (excludeABDayLabel) {
                    if (!(e.getTitle().equalsIgnoreCase("A Day") || e.getTitle().equalsIgnoreCase("B Day"))) {
                        //Log.i(MainActivity.LOG_NAME, "\"" + e.getTitle() + "\" != A Day == " + (!e.getTitle().equalsIgnoreCase("A Day")));
                        //Log.i(MainActivity.LOG_NAME, "\"" + e.getTitle() + "\" != B Day == " + (!e.getTitle().equalsIgnoreCase("B Day")));

                        result.add(e);
                    }
                } else {
                    result.add(e);
                }
            }
        }
        return result;
    }

    public static MaterialLargeImageCard makeCalendarCard(final Fragment fragment, final CalendarEvent event) {
        ArrayList<BaseSupplementalAction> actions = new ArrayList<BaseSupplementalAction>();
        IconSupplementalAction t1 = new IconSupplementalAction(fragment.getActivity(), R.id.icon_calendar); // calendar
        t1.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent calIntent = new Intent(Intent.ACTION_EDIT);
                calIntent.setType("vnd.android.cursor.item/event");
                calIntent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        event.getDate().getTimeInMillis());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                        event.getEndTime().getTimeInMillis());
                fragment.getActivity().startActivity(calIntent);
            }
        });
        actions.add(t1);

        IconSupplementalAction t2 = new IconSupplementalAction(fragment.getActivity(), R.id.icon_share); // share
        t2.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                textShareIntent.setType("text/plain");
                textShareIntent.putExtra(Intent.EXTRA_TEXT, event.toString());
                fragment.getActivity().startActivity(Intent.createChooser(textShareIntent, "Share event with..."));
            }
        });
        actions.add(t2);
        IconSupplementalAction t3 = new IconSupplementalAction(fragment.getActivity(), R.id.icon_details); // open web view
        t3.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                WebViewFragment webViewFragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("URL", event.getLink());
                webViewFragment.setArguments(bundle);

                fragment.getChildFragmentManager().beginTransaction()
                        .add(R.id.calendar_web_view_container, webViewFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("upcomingEvents")
                        .commit();
            }
        });
        actions.add(t3);

        String imageUrl = "garbageurl.blah";

        for (CountdownCard.CountdownTarget target : CountdownCard.CountdownTarget.values()) {
            if (target.getImageUrl().equals("")) continue;
            if (event.getTitle().toLowerCase().contains(target.getValue())) {
                imageUrl = target.getImageUrl();
                break;
            }
        }

        final String finalImageUrl = imageUrl;
        MaterialLargeImageCard card =
                MaterialLargeImageCard.with(fragment.getActivity())
                        .setTextOverImage(event.getTitle())
                        .setTitle(formatDateForCard(event))
                        .setSubTitle(event.getLocation())
                        .useDrawableExternal(new MaterialLargeImageCard.DrawableExternal() {
                            @Override
                            public void setupInnerViewElements(ViewGroup parent, View viewImage) {

                                //Picasso.with(fragment.getActivity()).setIndicatorsEnabled(true);  //only for debug tests
                                Picasso.with(fragment.getActivity())
                                        .load(finalImageUrl)
                                        .error(R.color.primary_light)
                                        .into((ImageView) viewImage);
                                //((ImageView) viewImage).setImageResource(R.color.primary);
                            }
                        })
                        .setupSupplementalActions(R.layout.supplemental_actions_calendar_event, actions)
                        .build();
        return card;
    }

    private static String formatDateForCard(CalendarEvent event) {
        String result = "";
        SimpleDateFormat headerFormat = new SimpleDateFormat("EEE hh:mmaa");
        SimpleDateFormat footerFormat = new SimpleDateFormat("hh:mmaa zzz");
        result = (headerFormat.format(event.getDate().getTime()) + " - " + footerFormat.format(event.getEndTime().getTime())).replace("PM", "pm").replace("AM", "am");
        String[] resultSplit = result.split(" ");
        if (result.split(" ")[1].equalsIgnoreCase(result.split(" ")[3]))
            result = resultSplit[0] + " " + resultSplit[1] + " " + resultSplit[4];
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

    public static CalendarDog determineRetrieval(SharedPreferences sharedPreferences,
                                                   int lastEventsFeedUpdateDay,
                                                   int lastEventsFeedUpdateMonth, Callable<Void> eventsFeedCallable,
                                                   File directory) {


        Log.v(MainActivity.LOG_NAME, "lastEventsFeedUpdateDay: " + lastEventsFeedUpdateDay);
        Log.v(MainActivity.LOG_NAME, "lastEventsFeedUpdateMonth: " + lastEventsFeedUpdateMonth);


        if (lastEventsFeedUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                || lastEventsFeedUpdateMonth != Calendar.getInstance().get(Calendar.MONTH)) { // If the last time we updated was not today...
            Log.v(MainActivity.LOG_NAME, "Events feed is out of date. Downloading events...");
            sharedPreferences.edit().putInt("lastEventsFeedUpdateDay", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).putInt("lastEventsFeedUpdateMonth", Calendar.getInstance().get(Calendar.MONTH)).apply();

            return new CalendarDog(eventsFeedCallable,
                    CalendarDog.FetchType.JSON);
        } else { // We have already downloaded the events today.. Lets retrieve the file and create a feed from it.
            File eventsFeedFile = new File(directory, "events.feed");

            if (eventsFeedFile.exists()) {
                Log.v(MainActivity.LOG_NAME, "Restoring events feed from file...");
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(eventsFeedFile));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    String ls = System.getProperty("line.separator");

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append(ls);
                    }

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    return new CalendarDog(jsonObject);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(MainActivity.LOG_NAME, "Could not restore events feed from file.");
                return new CalendarDog(eventsFeedCallable,
                        CalendarDog.FetchType.JSON);
            }
        }
        return null;
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

        events = CalendarEvent.sort(events);
        ready = true;
    }

    @Deprecated
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
            events = CalendarEvent.sort(events);
            try {
                refresh.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Deprecated
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
            events = CalendarEvent.sort(events);
            try {
                refresh.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
