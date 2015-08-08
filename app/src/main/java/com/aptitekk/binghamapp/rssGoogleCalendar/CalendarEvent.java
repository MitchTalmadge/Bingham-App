package com.aptitekk.binghamapp.rssGoogleCalendar;

import java.util.Date;

/**
 * Created by kevint on 8/8/2015.
 */
public class CalendarEvent {

    String title;
    Date date;
    Date endTime;
    String location;
    String link;

    public CalendarEvent(String title, Date date, Date endTime, String location, String link) {
        this.title = title;
        this.date = date;
        this.endTime = endTime;
        this.location = location;
        this.link = link;
    }

    public String getTitle() {return title;}
    public Date getDate() {return date;}
    public Date getEndTime() {return endTime;}
    public String getLocation() {
        return location;
    }
    public String getLink() {
        return link;
    }

}
