package com.aptitekk.binghamapp.Events;

import com.aptitekk.binghamapp.Fragments.BellSchedules.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class provides convenience methods for events
 */
public class EventInfoHelper {

    private EventsManager eventsManager;
    private MainActivity mainActivity;

    public EventInfoHelper(EventsManager eventsManager, MainActivity mainActivity) {

        this.eventsManager = eventsManager;
        this.mainActivity = mainActivity;
    }

    public DayType getDayType(Calendar date) {
        for (Event event : eventsManager.getEventsList()) {
            if (event.isOnDate(date)) {
                return event.getDayType();
            }
        }
        return DayType.OTHER;
    }

    public Event getNextABDayEvent(Calendar date) {
        for (Event event : eventsManager.getEventsList()) {
            if (event.isOnOrAfterDate(date))
                if (event.getDayType() == DayType.A_DAY || event.getDayType() == DayType.B_DAY)
                    return event;
        }
        return null;
    }

    public Event getNextEvent(Calendar time, boolean excludeABDayEvents) {
        for (Event event : eventsManager.getEventsList()) {
            if (event.isOnOrAfterTime(time))
                if (excludeABDayEvents && (event.getDayType() == DayType.A_DAY || event.getDayType() == DayType.B_DAY))
                    continue;
            return event;
        }
        return null;
    }

    public List<Event> getEventsForDay(Calendar date, boolean excludeABDayEvents) {
        List<Event> results = new ArrayList<>();
        for (Event event : eventsManager.getEventsList()) {
            if (event.isOnDate(date)) {
                if (excludeABDayEvents && (event.getDayType() == DayType.A_DAY || event.getDayType() == DayType.B_DAY))
                    continue;
                results.add(event);
            }
        }
        return results;
    }

    public boolean hasSchoolStartedForDay(BellSchedule schedule, Calendar dateTime) throws ParseException {
        String rawStartDay = schedule.getSubjectStartTimes()[0]; //GET START TIME
        Date startTime = new SimpleDateFormat("dd MMM yyyy hh:mm aa z", Locale.US).parse(SimpleDateFormat.getDateInstance().format(dateTime.getTime()) + " " + rawStartDay + " MDT");
        return !dateTime.getTime().before(startTime);
    }

    public boolean hasSchoolEndedForDay(BellSchedule schedule, Calendar dateTime) throws ParseException {
        String rawEndDay = schedule.getSubjectEndTimes()[schedule.getSubjectEndTimes().length - 1]; //GET END TIME
        Date endTime = new SimpleDateFormat("dd MMM yyyy hh:mm aa z", Locale.US).parse(SimpleDateFormat.getDateInstance().format(dateTime.getTime()) + " " + rawEndDay + " MDT");
        return !dateTime.getTime().after(endTime);
    }

    public BellSchedule getBellScheduleForDay(Calendar date) {
        //TODO: redo this
        List<Event> eventsOfDay = getEventsForDay(date, false);

        if (eventsOfDay.isEmpty())
            return null;

        Collections.rotate(eventsOfDay, -1); // move top A/B Day labels to bottom so potential assembly events may appear first.

        for (Event e : eventsOfDay) {
            if (e.getTitle().toLowerCase().contains("schedule") && e.getTitle().toLowerCase().contains("assembly")) { // Assembly Event Found!
                String schedule = e.getTitle().toLowerCase().split("-")[1].split("assembly")[0].replaceFirst("\\s+$", "");

                //DETERMINE MORNING/AFTERNOON
                int timeOfDay = e.getStartTime().get(Calendar.HOUR_OF_DAY);
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
                if (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) { // Friday schedule
                    return new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[1], mainActivity.getResources().getStringArray(R.array.regularBellSchedule1));
                } else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) { //If after school on thursday
                    try {
                        if (!hasSchoolEndedForDay(new BellSchedule(mainActivity.getResources().getStringArray(R.array.regularBellSchedules)[0],
                                mainActivity.getResources().getStringArray(R.array.regularBellSchedule0)), date)) {
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
}