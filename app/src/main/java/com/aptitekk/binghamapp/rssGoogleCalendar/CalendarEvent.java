package com.aptitekk.binghamapp.rssGoogleCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kevint on 8/8/2015.
 */
public class CalendarEvent {

    String title;
    Calendar date;
    Calendar endTime;
    String location;
    String link;

    public CalendarEvent(String title, Calendar date, Calendar endTime, String location, String link) {
        this.title = title;
        this.date = date;
        this.endTime = endTime;
        this.location = location;
        this.link = link;
    }

    public String getTitle() {return title;}
    public Calendar getDate() {return date;}
    public Calendar getEndTime() {return endTime;}
    public String getLocation() {
        return location;
    }
    public String getLink() {
        return link;
    }

    public static List<CalendarEvent> sort(List<CalendarEvent> e) {
        Collections.sort(e, new CalendarEventComparator());
        return e;
    }

    public static class CalendarEventComparator implements Comparator<CalendarEvent> {
        @Override
        public int compare(CalendarEvent o1, CalendarEvent o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    public static List<CalendarEvent> matchesDay(List<CalendarEvent> e, Calendar day) {
        List<CalendarEvent> result = new ArrayList<CalendarEvent>();
        for(CalendarEvent event : e) {
            if(event.getDate().get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) &&
                    event.getDate().get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                    event.getDate().get(Calendar.YEAR) == day.get(Calendar.YEAR)) {
                result.add(event);
            }
        }
        return result;
    }

}
