package com.aptitekk.binghamapp.googleCalendarAPI;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by kevint on 8/8/2015.
 */
@Deprecated
public class CalendarDog {

    List<Event> events;

    final boolean verbose = true;

    com.google.api.services.calendar.Calendar service;

    Context context;

    public CalendarDog(Context cxt) {
        context = cxt;
        Log.i(MainActivity.LOG_NAME, "Populating Calendar...\n");
        try {
            FetchEventsTask task = new FetchEventsTask();
            task.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<Event> getEvents() {
        Log.i(MainActivity.LOG_NAME, "\nThere are " + this.events.size() + " events in feed");
        return this.events;
    }

    private void logInfo(String msg) {
        if(verbose) {
            Log.i(MainActivity.LOG_NAME, msg);
        }
    }

    private class FetchEventsTask extends AsyncTask {


        @Override
        protected Object doInBackground(Object... params) {
            try {
                service = CalendarService.getCalendarService(context);
                // List the next 10 events from the primary calendar.
                DateTime now = new DateTime(System.currentTimeMillis());
                Events eventsList = service.events().list("primary")
                        .setMaxResults(10)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                events = eventsList.getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
