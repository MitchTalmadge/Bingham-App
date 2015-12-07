package com.aptitekk.binghamapp.Events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aptitekk.binghamapp.Fragments.BellSchedules.BellSchedule;
import com.aptitekk.binghamapp.Fragments.HelperFragments.WebViewFragment;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.Utilities.WebFileDownloader.WebFileDownloader;
import com.aptitekk.binghamapp.Utilities.WebFileDownloader.WebFileDownloaderAdapter;
import com.aptitekk.binghamapp.cards.CountdownCard;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;

public class EventsManager {

    public static URL EVENTS_CALENDAR_URL;

    static {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String timeZone = TimeZone.getDefault().getID();
        try {
            EVENTS_CALENDAR_URL = new URL("https://www.googleapis.com/calendar/v3/calendars/jordandistrict.org_o4d9atn49tbcvmc29451bailf0@group.calendar.google.com/events?maxResults=2500&" +
                    "timeMin=" + format.format(c.getTime()).replace(" ", "T") + "Z" + "&timeZone=" + timeZone + "&singleEvents=true&key=AIzaSyBYdbs9jPSdqJRASyjEC7E6JjRTp20UxQk");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public final static String EVENTS_FILE_NAME = "events.cache";

    private MainActivity mainActivity;

    private ArrayList<EventsUpdateListener> eventsUpdateListeners = new ArrayList<>();

    private JSONObject jsonObject;

    List<Event> eventsList;

    public EventsManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Adds an EventsUpdateListener if not already added (Must be instanceof Fragment)
     *
     * @param listener the listener to add.
     */
    public void addEventsUpdateListener(EventsUpdateListener listener) {
        if (listener instanceof Fragment) {
            if (!eventsUpdateListeners.contains(listener))
                eventsUpdateListeners.add(listener);

            if (this.eventsList != null)
                listener.onEventsUpdated(this); //If the events list is already populated, notify the listener immediately.
        }
    }

    /**
     * Notifies all listeners of an update to the eventsList.
     */
    protected void notifyListenersOfUpdate() {
        for (EventsUpdateListener listener : eventsUpdateListeners) {
            if (listener != null && ((Fragment) listener).isAdded()) {
                listener.onEventsUpdated(this);
            }
        }
    }

    public List<Event> getEventsList() {
        return this.eventsList;
    }

    /**
     * Checks for updates to the Events by determining if they have already been downloaded today or not.
     */
    public void checkForUpdates() {
        final SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        int lastEventsFeedUpdateDay = sharedPreferences.getInt("lastEventsFeedUpdateDay", 0);
        int lastEventsFeedUpdateMonth = sharedPreferences.getInt("lastEventsFeedUpdateMonth", 0);

        MainActivity.logVerbose("Last Events Feed Update Day: " + lastEventsFeedUpdateDay);
        MainActivity.logVerbose("Last Events Feed Update Month: " + lastEventsFeedUpdateMonth);

        if (lastEventsFeedUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                || lastEventsFeedUpdateMonth != Calendar.getInstance().get(Calendar.MONTH)) { // If the last time we updated was not today...
            MainActivity.logVerbose("Events Feed is out of date. Downloading Events...");
            sharedPreferences.edit().putInt("lastEventsFeedUpdateDay", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).putInt("lastEventsFeedUpdateMonth", Calendar.getInstance().get(Calendar.MONTH)).apply();
            downloadEventsFromWeb();
        } else { // We have already downloaded the events today.. Lets retrieve the file and create a feed from it.
            File eventsFeedFile = new File(mainActivity.getFilesDir(), EVENTS_FILE_NAME);

            if (eventsFeedFile.exists()) {
                MainActivity.logVerbose("Restoring Events Feed from file...");
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
                    updateEvents(jsonObject);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                MainActivity.logVerbose("Could not restore Events Feed from file.");
                downloadEventsFromWeb();
            }
        }
    }

    /**
     * Downloads the Events from the Web and saves them to a file.
     */
    private void downloadEventsFromWeb() {
        MainActivity.logVerbose("Downloading Events from Web...");
        WebFileDownloader.downloadFromURLAsJSONObject(EVENTS_CALENDAR_URL, new WebFileDownloaderAdapter() {
            @Override
            public void fileDownloadedAsJSONObject(URL url, JSONObject jsonObject) {
                MainActivity.logVerbose("Events have been Downloaded.");
                // Save the events to file...
                try {
                    MainActivity.logVerbose("Saving Events to file...");
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(mainActivity.getFilesDir(), EVENTS_FILE_NAME));

                    if (jsonObject != null) {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        outputStreamWriter.write(jsonObject.toString());

                        outputStreamWriter.close();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                updateEvents(jsonObject);
            }
        });
    }

    /**
     * Updates the Events lists and notifies all listeners of an update.
     *
     * @param jsonObject The jsonObject to build the eventsList from.
     */
    private void updateEvents(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        buildEventsList();
        notifyListenersOfUpdate();
    }

    /**
     * Determines the scroll position of a given date.
     *
     * @param date The date we want to scroll to
     * @return The position of the event on the given date.
     */
    public int findPositionFromDate(Date date) {
        long minDiff = -1, currentTime = date.getTime();
        int minDate = 0;
        for (int i = 0; i < eventsList.size(); i++) {
            long diff = Math.abs(currentTime - eventsList.get(i).getEventDate().getTime().getTime());
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
                MainActivity.logVerbose("Skipped event " + subject.getName() + " at " + subject.getStartTime());
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

    public static MultipleReturn getNearestDateBySubjectIsEndTime(BellSchedule.Subject subject, Date currentDate, boolean skipPastEvents) {
        boolean endTimePointer = false;
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

                endTimePointer = date.equals(times[1]);
            }
        }
        return new MultipleReturn(minDate, endTimePointer);
    }

    public static class MultipleReturn {
        Object first;
        Object second;

        public MultipleReturn(Object first, Object second) {
            this.first = first;
            this.second = second;
        }

        public Object getFirst() {
            return first;
        }

        public Object getSecond() {
            return second;
        }
    }

    public Event getNextEvent(Date date, boolean excludeABDayLabel) {
        long minDiff = -1, currentTime = date.getTime();
        Event nextEvent = null;
        for (Event event : eventsList) {
            if (((event.getTitle().contains("A Day")) || (event.getTitle().contains("B Day"))) && excludeABDayLabel) {
                continue; //Skip A and B day Events
            }
            if (currentTime > event.getEventDate().getTimeInMillis()) {
                continue; // Skip any Events that have already passed
            }
            long diff = Math.abs(currentTime - event.getEventDate().getTimeInMillis());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                nextEvent = event;
            }
        }
        return nextEvent;
    }

    public int findNextAorBDay() {
        for (int i = 0; i < eventsList.size(); i++) {
            if (eventsList.get(i).getTitle().contains("A Day") || eventsList.get(i).getTitle().contains("B Day"))
                return i;
        }
        return -1;
    }

    public char isItAorBDay(Calendar date) {
        for (Event event : eventsList) {
            if (event.isOnDate(date)) {
                if (event.getTitle().contains("A Day"))
                    return BellSchedule.A_DAY;
                else if (event.getTitle().contains("B Day"))
                    return BellSchedule.B_DAY;
            }
        }
        return BellSchedule.NONE_DAY;
    }

    public int findNextTargetByIndex(CountdownCard.CountdownTarget target) {
        for (int i = 0; i < eventsList.size(); i++) {
            if (eventsList.get(i).getTitle().toLowerCase().contains(target.getValue()))
                return i;
        }
        return -1;
    }

    public BellSchedule determineSchedule(Calendar dateTime) {

        ArrayList<Event> eventsOfDay = getEventsForDay(dateTime);

        if (eventsOfDay.isEmpty())
            return null;

        Collections.rotate(eventsOfDay, -1); // move top A/B Day labels to bottom so potential assembly events may appear first.

        for (Event e : eventsOfDay) {
            if (e.getTitle().toLowerCase().contains("schedule") && e.getTitle().toLowerCase().contains("assembly")) { // Assembly Event Found!
                String schedule = e.getTitle().toLowerCase().split("-")[1].split("assembly")[0].replaceFirst("\\s+$", "");

                //DETERMINE MORNING/AFTERNOON
                int timeOfDay = e.getEventDate().get(Calendar.HOUR_OF_DAY);
                if ((timeOfDay >= 0 && timeOfDay < 12) && schedule.contains("A/B")) { // IF A/B is detected, theres only one type of "A/B" assembly in the mornings
                    return new BellSchedule(mainActivity.getResources().getStringArray(R.array.assemblyBellSchedules)[4], mainActivity.getResources().getStringArray(R.array.assemblyBellSchedule4));
                } else if ((timeOfDay >= 0 && timeOfDay < 12) || schedule.contains("AM")) { // MORNING
                    schedule = "Morning (" + schedule.replace("AM", "") + ")";
                } else if ((timeOfDay >= 12 && timeOfDay < 16) || schedule.contains("PM")) { // AFTERNOON
                    schedule = "Afternoon (" + schedule.replace("PM", "") + ")";
                } else {
                    return null;
                }
                for (int i = 0; i < mainActivity.getResources().getStringArray(R.array.assemblyBellSchedules).length; i++) {
                    if (mainActivity.getResources().getStringArray(R.array.assemblyBellSchedules)[i].split("_")[1].equalsIgnoreCase(schedule)) {// if string matches name
                        String[] scheduleTimeArray = null;
                        switch (i) {
                            case 0:
                                scheduleTimeArray = mainActivity.getResources().getStringArray(R.array.assemblyBellSchedule0);
                                break;
                            case 1:
                                scheduleTimeArray = mainActivity.getResources().getStringArray(R.array.assemblyBellSchedule1);
                                break;
                            case 2:
                                scheduleTimeArray = mainActivity.getResources().getStringArray(R.array.assemblyBellSchedule2);
                                break;
                            case 3:
                                scheduleTimeArray = mainActivity.getResources().getStringArray(R.array.assemblyBellSchedule3);
                                break;
                        }
                        return new BellSchedule(mainActivity.getResources().getStringArray(R.array.assemblyBellSchedules)[i], scheduleTimeArray);
                    }
                }
            } else if (e.getTitle().contains("A Day") || e.getTitle().contains("B Day")) { // Just a regular day
                if (dateTime.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) { // Friday schedule
                    return new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[1], mainActivity.getResources().getStringArray(R.array.regularBellSchedule1));
                } else if (dateTime.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) { //If after school on thursday
                    try {
                        if (!hasSchoolEndedForDay(new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[0],
                                        mainActivity.getResources().getStringArray(R.array.regularBellSchedule0)),
                                dateTime)) {
                            return new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[1], mainActivity.getResources().getStringArray(R.array.regularBellSchedule1));
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                        MainActivity.logVerbose("Reverting to regular schedule");
                    }

                }
                return new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[0], mainActivity.getResources().getStringArray(R.array.regularBellSchedule0));
            }

        }
        MainActivity.logVerbose("No schedule was determined, loading regular weekday schedule.");
        return new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[0], mainActivity.getResources().getStringArray(R.array.regularBellSchedule0));
    }

    public static boolean hasSchoolStartedForDay(BellSchedule regularSchedule, Calendar dateTime) throws ParseException {
        String rawStartDay = regularSchedule.getSubjectStartTimes()[0]; //GRAB Start TIME
        Date startTime = new SimpleDateFormat("dd MMM yyyy hh:mm aa z", Locale.US).parse(SimpleDateFormat.getDateInstance().format(dateTime.getTime()) + " " + rawStartDay + " MDT");
        return !dateTime.getTime().before(startTime);
    }

    public static boolean hasSchoolEndedForDay(BellSchedule regularSchedule, Calendar dateTime) throws ParseException {
        String rawEndDay = regularSchedule.getSubjectEndTimes()[regularSchedule.getSubjectEndTimes().length - 1]; //GRAB END TIME
        Date endTime = new SimpleDateFormat("dd MMM yyyy hh:mm aa z", Locale.US).parse(SimpleDateFormat.getDateInstance().format(dateTime.getTime()) + " " + rawEndDay + " MDT");
        return !dateTime.getTime().after(endTime);
    }

    public ArrayList<Event> getEventsForDay(Calendar dayToMatch) {
        ArrayList<Event> result = new ArrayList<>();
        for (Event e : eventsList) {
            if (e.getEventDate().get(Calendar.YEAR) == dayToMatch.get(Calendar.YEAR) &&
                    e.getEventDate().get(Calendar.MONTH) == dayToMatch.get(Calendar.MONTH) &&
                    e.getEventDate().get(Calendar.DAY_OF_MONTH) == dayToMatch.get(Calendar.DAY_OF_MONTH)) {
                result.add(e);
            }
        }
        return result;
    }

    public ArrayList<Event> getEventsForDay(Calendar dayToMatch, boolean excludeABDayLabel) {
        ArrayList<Event> result = new ArrayList<>();
        for (Event e : eventsList) {
            if (e.getEventDate().get(Calendar.YEAR) == dayToMatch.get(Calendar.YEAR) &&
                    e.getEventDate().get(Calendar.MONTH) == dayToMatch.get(Calendar.MONTH) &&
                    e.getEventDate().get(Calendar.DAY_OF_MONTH) == dayToMatch.get(Calendar.DAY_OF_MONTH)) {
                if (excludeABDayLabel) {
                    if (!(e.getTitle().equalsIgnoreCase("A Day") || e.getTitle().equalsIgnoreCase("B Day"))) {
                        result.add(e);
                    }
                } else {
                    result.add(e);
                }
            }
        }
        return result;
    }

    public static MaterialLargeImageCard makeCalendarCard(final Fragment fragment, final Event event) {
        ArrayList<BaseSupplementalAction> actions = new ArrayList<>();
        IconSupplementalAction t1 = new IconSupplementalAction(fragment.getActivity(), R.id.icon_calendar); // calendar
        t1.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent calIntent = new Intent(Intent.ACTION_EDIT);
                calIntent.setType("vnd.android.cursor.item/event");
                calIntent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        event.getEventDate().getTimeInMillis());
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

        int imageDrawableId = R.drawable.event_default;

        for (CountdownCard.CountdownTarget target : CountdownCard.CountdownTarget.values()) {
            if (event.getTitle().toLowerCase().contains(target.getValue())) {
                imageDrawableId = target.getImageDrawableId();
                break;
            }
        }

        return MaterialLargeImageCard.with(fragment.getActivity())
                .setTextOverImage(event.getTitle())
                .setTitle(formatDateForCard(event))
                .setSubTitle(event.getLocation())
                .useDrawableId(imageDrawableId)
                /*.useDrawableExternal(new MaterialLargeImageCard.DrawableExternal() {
                    @Override
                    public void setupInnerViewElements(ViewGroup parent, View viewImage) {

                        //Picasso.with(fragment.getActivity()).setIndicatorsEnabled(true);  //only for debug tests
                        Picasso.with(fragment.getActivity())
                                .load(finalImageDrawableId)
                                .error(R.color.primary_light)
                                .into((ImageView) viewImage);
                        //((ImageView) viewImage).setImageResource(R.color.primary);
                    }
                })*/
                .setupSupplementalActions(R.layout.supplemental_actions_calendar_event, actions)
                .build();
    }

    private static String formatDateForCard(Event event) {
        String result;
        SimpleDateFormat headerFormat = new SimpleDateFormat("EEE hh:mmaa", Locale.US);
        SimpleDateFormat footerFormat = new SimpleDateFormat("hh:mmaa zzz", Locale.US);
        result = (headerFormat.format(event.getEventDate().getTime()) + " - " + footerFormat.format(event.getEndTime().getTime())).replace("PM", "pm").replace("AM", "am");
        String[] resultSplit = result.split(" ");
        if (result.split(" ")[1].equalsIgnoreCase(result.split(" ")[3]))
            result = resultSplit[0] + " " + resultSplit[1] + " " + resultSplit[4];
        return result;
    }

    private void buildEventsList() {
        //Recreate eventsList variable
        this.eventsList = new ArrayList<>();

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
                    format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZZZZZZ", Locale.US);
                    endTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZZZZZZ", Locale.US);
                } catch (JSONException e) { //Date not DateTime
                    rawStartTime = arr.getJSONObject(i).getJSONObject("start").getString("date");
                    rawEndTime = arr.getJSONObject(i).getJSONObject("end").getString("date");
                    format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    endTimeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                }
                date.setTime(format.parse(rawStartTime.replace("T", " ")));
                endTime.setTime(endTimeFormat.parse(rawEndTime.replace("T", " ")));
                eventsList.add(new Event(summary,
                        date,
                        endTime,
                        location,
                        link
                ));
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        sortEvents();
    }

    private void sortEvents() {
        Collections.sort(eventsList, new EventComparator());
    }

    public static class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event event1, Event event2) {
            //Put A/B Day Labels at the top of each day.
            if (event1.getTitle().equalsIgnoreCase("A Day") || event1.getTitle().equalsIgnoreCase("B Day")) {
                if (event1.isOnDate(event2.getEventDate())) {
                    return -1;
                }
            }
            if (event2.getTitle().equalsIgnoreCase("A Day") || event2.getTitle().equalsIgnoreCase("B Day")) {
                if (event2.isOnDate(event1.getEventDate())) {
                    return 1;
                }
            }

            return event1.getEventDate().compareTo(event2.getEventDate());
        }
    }

}
