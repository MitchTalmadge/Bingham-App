package com.aptitekk.binghamapp.BellSchedulesFragmentClasses;

import android.util.Log;

import com.aptitekk.binghamapp.MainActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BellSchedule {

    public static final BellSchedule NONE = null;

    String scheduleName;
    private String[] subjectNames;
    private String[] subjectStartTimes;
    private String[] subjectEndTimes;
    private int[] subjectLengths;

    public static ArrayList<Subject> parseScheduleTimes(final BellSchedule schedule) {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
        ArrayList<Subject> result = new ArrayList<>();
        for (int i = 0; i < schedule.getSubjectStartTimes().length; i++) {
            try {
                if(schedule.getSubjectNames()[i].toLowerCase().contains("warning")) // warning bell doesnt need to be in there
                    continue;
                if((schedule.getSubjectStartTimes()[i].contains("--")) || (schedule.getSubjectEndTimes()[i].contains("--")))
                    continue; // dont need non-existent times
                if(schedule.getSubjectNames()[i].toLowerCase().contains("conference")) // conference time removed for student's sake
                    continue;
                result.add(
                        new Subject(
                                schedule.getSubjectNames()[i],
                                df.parse(SimpleDateFormat.getDateInstance().format(today) + " " +schedule.getSubjectStartTimes()[i]),
                                df.parse(SimpleDateFormat.getDateInstance().format(today) + " " +schedule.getSubjectEndTimes()[i])));
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
        }
        return result;
    }

    public static Subject getNextSubject(Date currentTime, List<Subject> subjects) {
        long minDiff = -1;
        Subject minDate = null;
        for (Subject subject : subjects) {
            ArrayList<Date> dates = new ArrayList<>();
            dates.add(subject.getStartTime());
            dates.add(subject.getEndTime());
            Date tomorrowStart = subject.getStartTime(); tomorrowStart.setTime(subject.getStartTime().getTime() + 1 * 24 * 60 * 60 * 1000);
            Date tomorrowEnd = subject.getEndTime(); tomorrowStart.setTime(subject.getEndTime().getTime() + 1 * 24 * 60 * 60 * 1000);
            dates.add(tomorrowStart);
            dates.add(tomorrowEnd);
            for (Date date : dates) {
                if(currentTime.getTime() > date.getTime()) {
                    continue;
                }
                long diff = Math.abs(currentTime.getTime() - date.getTime());
                if ((minDiff == -1) || (diff < minDiff)) {
                    Log.i(MainActivity.LOG_NAME, "Next determined subject: " + subject.getName() + " at " + SimpleDateFormat.getDateTimeInstance().format(date));
                    minDiff = diff;
                    minDate = subject;
                }
            }
        }
        return minDate;
    }

    public static class Subject {
        String name;
        Date startTime;
        Date endTime;

        public Subject(String name, Date start, Date end) {
            this.name = name;
            startTime = start;
            endTime = end;
        }

        public String getName() {
            return name;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }
    }

    public BellSchedule(String scheduleName, String[] schedule) {

        this.scheduleName = scheduleName;

        if (schedule == null)
            return;

        subjectNames = new String[schedule.length];
        subjectStartTimes = new String[schedule.length];
        subjectEndTimes = new String[schedule.length];
        subjectLengths = new int[schedule.length];

        DateFormat df = new SimpleDateFormat("hh:mm a", Locale.US);

        for (int i = 0; i < schedule.length; i++) {
            String[] subjectSplit = schedule[i].split("_");
            subjectNames[i] = subjectSplit[0];
            subjectStartTimes[i] = subjectSplit[1];
            subjectEndTimes[i] = subjectSplit[2];
            if (!subjectSplit[1].equals("--") && !subjectSplit[2].equals("--")) {
                try {
                    Date date1 = df.parse(subjectSplit[1]);
                    Date date2 = df.parse(subjectSplit[2]);
                    int length = (int) ((date2.getTime() - date1.getTime()) / 1000) / 60;
                    subjectLengths[i] = length;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                subjectLengths[i] = 0;
            }
        }
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String[] getSubjectNames() {
        return subjectNames;
    }

    public String[] getSubjectStartTimes() {
        return subjectStartTimes;
    }

    public String[] getSubjectEndTimes() {
        return subjectEndTimes;
    }

    public int[] getSubjectLengths() {
        return subjectLengths;
    }
}
