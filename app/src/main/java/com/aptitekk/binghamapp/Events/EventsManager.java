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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;

public class EventsManager {

    public final static String FILE_NAME = "events.cache";

    private MainActivity mainActivity;

    private ArrayList<EventsUpdateListener> eventsUpdateListeners = new ArrayList<>();

    private JSONObject jsonObject;

    public JSONObject getJSONObject() {
        return jsonObject;
    }

    List<Event> events;

    public static String BINGHAM_GOOGLE_CALENDAR = generateJSONURL();

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

            if (this.events != null)
                listener.onEventsUpdated(this); //If the events list is already populated, notify the listener immediately.
        }
    }

    /**
     * Notifies all listeners of an update to the events.
     */
    protected void notifyListenersOfUpdate() {
        for (EventsUpdateListener listener : eventsUpdateListeners) {
            if (listener != null && ((Fragment) listener).isAdded()) {
                listener.onEventsUpdated(this);
            }
        }
    }

    public List<Event> getEvents() {
        return this.events;
    }

    private static String generateJSONURL() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String timeZone = TimeZone.getDefault().getID();
        return "https://www.googleapis.com/calendar/v3/calendars/jordandistrict.org_o4d9atn49tbcvmc29451bailf0@group.calendar.google.com/events?maxResults=2500&" +
                "timeMin=" + format.format(c.getTime()).replace(" ", "T") + "Z" + "&timeZone=" + timeZone + "&singleEvents=true&key=AIzaSyBYdbs9jPSdqJRASyjEC7E6JjRTp20UxQk";
    }

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
            File eventsFeedFile = new File(mainActivity.getFilesDir(), FILE_NAME);

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

    private void downloadEventsFromWeb() {
        MainActivity.logVerbose("Downloading Events from Web...");
        try {
            WebFileDownloader.downloadFromURLAsJSONObject(new URL(BINGHAM_GOOGLE_CALENDAR), new WebFileDownloaderAdapter() {
                @Override
                public void fileDownloadedAsJSONObject(URL url, JSONObject jsonObject) {
                    MainActivity.logVerbose("Events have been Downloaded.");
                    // Save the events to file...
                    try {
                        MainActivity.logVerbose("Saving Events to file...");
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(mainActivity.getFilesDir(), FILE_NAME));

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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void updateEvents(JSONObject jsonObject) {
        buildFromJSONObject(jsonObject);
        notifyListenersOfUpdate();
    }

    public static int findPositionFromDate(List<Event> events, Date date) {
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

    public static Event getNextEvent(List<Event> events, Date currentDate, boolean excludeABDayLabel) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Event minDate = null;
        for (Event date : events) {
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

    public static int findNextAorBDay(List<Event> events) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().contains("A Day") || events.get(i).getTitle().contains("B Day"))
                return i;
        }
        return -1;
    }

    public static char isItAorBDay(List<Event> sortedEvents, Calendar currentDateTime) {
        for (Event event : sortedEvents) {
            if (Event.eventMatchesDay(event, currentDateTime)) {
                if (event.getTitle().contains("A Day"))
                    return BellSchedule.A_DAY;
                else if (event.getTitle().contains("B Day"))
                    return BellSchedule.B_DAY;
            }
        }
        return BellSchedule.NONE_DAY;
    }

    public static int findNextTargetByIndex(List<Event> events, CountdownCard.CountdownTarget target) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getTitle().toLowerCase().contains(target.getValue()))
                return i;
        }
        return -1;
    }

    public static BellSchedule determineSchedule(Fragment fragment, List<Event> events, Calendar dateTime) {

        ArrayList<Event> eventsOfDay = getEventsForDay(events, dateTime);

        if (eventsOfDay.isEmpty())
            return null;

        Collections.rotate(eventsOfDay, -1); // move top A/B Day labels to bottom so potential assembly events may appear first.

        for (Event e : eventsOfDay) {
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
                        MainActivity.logVerbose("Reverting to regular schedule");
                    }

                }
                return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[0], fragment.getResources().getStringArray(R.array.regularBellSchedule0));
            }

        }
        MainActivity.logVerbose("No schedule was determined, loading regular weekday schedule.");
        return new BellSchedule(fragment.getResources().getStringArray(R.array.regularBellSchedules)[0], fragment.getResources().getStringArray(R.array.regularBellSchedule0));
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

    public static ArrayList<Event> getEventsForDay(List<Event> events, Calendar dayToMatch) {
        ArrayList<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate().get(Calendar.YEAR) == dayToMatch.get(Calendar.YEAR) &&
                    e.getDate().get(Calendar.MONTH) == dayToMatch.get(Calendar.MONTH) &&
                    e.getDate().get(Calendar.DAY_OF_MONTH) == dayToMatch.get(Calendar.DAY_OF_MONTH)) {
                result.add(e);
            }
        }
        return result;
    }

    public static ArrayList<Event> getEventsForDay(List<Event> events, Calendar dayToMatch, boolean excludeABDayLabel) {
        ArrayList<Event> result = new ArrayList<>();
        for (Event e : events) {
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
        return MaterialLargeImageCard.with(fragment.getActivity())
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
    }

    private static String formatDateForCard(Event event) {
        String result;
        SimpleDateFormat headerFormat = new SimpleDateFormat("EEE hh:mmaa", Locale.US);
        SimpleDateFormat footerFormat = new SimpleDateFormat("hh:mmaa zzz", Locale.US);
        result = (headerFormat.format(event.getDate().getTime()) + " - " + footerFormat.format(event.getEndTime().getTime())).replace("PM", "pm").replace("AM", "am");
        String[] resultSplit = result.split(" ");
        if (result.split(" ")[1].equalsIgnoreCase(result.split(" ")[3]))
            result = resultSplit[0] + " " + resultSplit[1] + " " + resultSplit[4];
        return result;
    }

    public static boolean isSameDay(Event e1, Event e2) {
        return e1.getDate().get(Calendar.YEAR) == e2.getDate().get(Calendar.YEAR) &&
                e1.getDate().get(Calendar.MONTH) == e2.getDate().get(Calendar.MONTH) &&
                e1.getDate().get(Calendar.DAY_OF_MONTH) == e2.getDate().get(Calendar.DAY_OF_MONTH);
    }

    public static EventsManager determineRetrieval(SharedPreferences sharedPreferences,
                                                   int lastEventsFeedUpdateDay,
                                                   int lastEventsFeedUpdateMonth, Callable<Void> eventsFeedCallable,
                                                   File directory) {


        return null;
    }

    private void buildFromJSONObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        events = new ArrayList<>();

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
                try {
                    date.setTime(format.parse(rawStartTime.replace("T", " ")));
                    endTime.setTime(endTimeFormat.parse(rawEndTime.replace("T", " ")));
                    events.add(new Event(summary,
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

        events = Event.sort(events);
    }

}
