package com.aptitekk.binghamapp.cards;

/**
 * Created by kevint on 8/18/2015.
 */
public interface CalendarEventView {

    void setTitle(String title);

    void setDuration(String formattedDuration);

    void setLocation(String location);

    String getTitle();

    String getDuration();

    String getLocation();

}
