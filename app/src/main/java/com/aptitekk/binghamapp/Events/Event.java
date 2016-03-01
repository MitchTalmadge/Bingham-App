package com.aptitekk.binghamapp.Events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Event {

    String title;
    Calendar eventDate;
    Calendar endTime;
    String location;
    String link;

    public Event(String title, Calendar date, Calendar endTime, String location, String link) {
        this.title = title;
        this.eventDate = date;
        this.endTime = endTime;
        this.location = location;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public Calendar getStartTime() {
        return eventDate;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        String dateStamp;
        String location = "";
        String title = this.getTitle();

        //Format Date
        if (SimpleDateFormat.getDateTimeInstance().format(this.getStartTime().getTime()).equalsIgnoreCase(
                SimpleDateFormat.getDateTimeInstance().format(this.getEndTime().getTime()))) {
            dateStamp = (" on " + new SimpleDateFormat("MMM dd", Locale.US).format(this.getStartTime().getTime()) + " at " +
                    new SimpleDateFormat("hh:mmaa", Locale.US).format(this.getStartTime().getTime()).toLowerCase());

        } else {
            if (new SimpleDateFormat("MMM dd", Locale.US).format(this.getStartTime().getTime()).equalsIgnoreCase(
                    new SimpleDateFormat("MMM dd", Locale.US).format(this.getEndTime().getTime()))) {
                dateStamp = (" on " + new SimpleDateFormat("MMM dd", Locale.US).format(this.getStartTime().getTime()) + " from " +
                        new SimpleDateFormat("hh:mmaa", Locale.US).format(this.getStartTime().getTime()).toLowerCase() + " to " +
                        new SimpleDateFormat("hh:mmaa", Locale.US).format(this.getEndTime().getTime()).toLowerCase());
            } else {
                dateStamp = (" on " + new SimpleDateFormat("MMM dd", Locale.US).format(this.getStartTime().getTime()) + " from " +
                        new SimpleDateFormat("hh:mmaa", Locale.US).format(this.getStartTime().getTime()).toLowerCase() + " to " +
                        new SimpleDateFormat("MMM dd", Locale.US).format(this.getEndTime().getTime()) + " " +
                        new SimpleDateFormat("hh:mmaa", Locale.US).format(this.getEndTime().getTime()).toLowerCase());
            }
        }
        // Format location
        if (this.getLocation().equals("")) {
            if (this.getLocation().contains(",") && this.getLocation().contains("Bingham High"))
                location = "Bingham High";
            else
                location = this.getLocation();

            if (location.contains(", United States")) {
                location = location.replace(", United States", "");
            }

            if (!this.getLocation().contains(" ") && !this.getLocation().contains("Bingham High")) {
                location = "the " + location;
            }
            location = " at " + location;
        }
        // Format title
        if (this.getTitle().contains("@")) {
            title = title.split("@")[0].replaceFirst("\\s+$", ""); // cut off any trailing spaces after splitting by @
        }
        return title + location + dateStamp;
    }

    public DayType getDayType()
    {
        if(title.contains(DayType.A_DAY.getFriendlyName()))
            return DayType.A_DAY;
        else if(title.contains(DayType.B_DAY.getFriendlyName()))
            return DayType.B_DAY;
        return DayType.OTHER;
    }

    public boolean isOnDate(Calendar date)
    {
        return eventDate.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) &&
                eventDate.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                eventDate.get(Calendar.YEAR) == date.get(Calendar.YEAR);
    }

    public boolean isOnOrAfterDate(Calendar date)
    {
        if(!isOnDate(date)) //If it's not today
        {
            if(eventDate.get(Calendar.YEAR) < date.get(Calendar.YEAR) || //If previous year
                    eventDate.get(Calendar.MONTH) < date.get(Calendar.MONTH) || //Or previous month
                    eventDate.get(Calendar.MONTH) < date.get(Calendar.DAY_OF_MONTH)) //Or previous day
                return false;

            if(eventDate.get(Calendar.YEAR) > date.get(Calendar.YEAR) || //If next year
                    eventDate.get(Calendar.MONTH) > date.get(Calendar.MONTH) || //Or next month
                    eventDate.get(Calendar.MONTH) > date.get(Calendar.DAY_OF_MONTH)) //Or next day
                return true;
        }
        return true;
    }

    public boolean isOnOrAfterTime(Calendar time)
    {
        return eventDate.getTime().getTime() >= time.getTime().getTime();
    }

}
