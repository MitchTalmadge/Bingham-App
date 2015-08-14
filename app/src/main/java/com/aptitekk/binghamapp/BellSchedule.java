package com.aptitekk.binghamapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BellSchedule {

    String scheduleName;
    private String[] subjectNames;
    private String[] subjectStartTimes;
    private String[] subjectEndTimes;
    private int[] subjectLengths;

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
