package com.aptitekk.binghamapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BellSchedule {

    public static final BellSchedule NONE = null;

    String scheduleName;
    private String[] subjectNames;
    private String[] subjectStartTimes;
    private String[] subjectEndTimes;
    private int[] subjectLengths;

    public static ArrayList<Subject> parseScheduleTimes(final BellSchedule schedule) {
        DateFormat df = new SimpleDateFormat("hh:mm a", Locale.US);
        ArrayList<Subject> result = new ArrayList<>();
        for (int i = 0; i < schedule.getSubjectStartTimes().length; i++) {
            try {
                if(schedule.getScheduleName().toLowerCase().contains("warning")) // warning bell doesnt need to be in there
                    continue;
                result.add(
                        new Subject(
                                schedule.getSubjectNames()[i],
                                df.parse(schedule.getSubjectStartTimes()[i]),
                                df.parse(schedule.getSubjectEndTimes()[i])));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Subject getNextSubject(Date currentTime, ArrayList<Subject> subjects) {
        long minDiff = -1;
        Subject minDate = null;
        for (Subject subject : subjects) {
            ArrayList<Date> dates = new ArrayList<>();
            dates.add(subject.getStartTime());
            dates.add(subject.getEndTime());
            for (Date date : dates) {
                long diff = Math.abs(currentTime.getTime() - date.getTime());
                if ((minDiff == -1) || (diff < minDiff)) {
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
