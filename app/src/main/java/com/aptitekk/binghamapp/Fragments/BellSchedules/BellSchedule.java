package com.aptitekk.binghamapp.Fragments.BellSchedules;

import com.aptitekk.binghamapp.Events.DayType;
import com.aptitekk.binghamapp.Events.LunchType;
import com.aptitekk.binghamapp.MainActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BellSchedule {

    String scheduleName;
    private String[] subjectNames;
    private String[] subjectStartTimes;
    private String[] subjectEndTimes;
    private int[] subjectLengths;

    private final DateFormat scheduleDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    public BellSchedule(String scheduleName, String[] schedule) {

        this.scheduleName = scheduleName;

        if (schedule == null)
            return;

        subjectNames = new String[schedule.length];
        subjectStartTimes = new String[schedule.length];
        subjectEndTimes = new String[schedule.length];
        subjectLengths = new int[schedule.length];

        for (int i = 0; i < schedule.length; i++) {
            String[] subjectSplit = schedule[i].split("_");
            subjectNames[i] = subjectSplit[0];
            subjectStartTimes[i] = subjectSplit[1];
            subjectEndTimes[i] = subjectSplit[2];
            if (!subjectSplit[1].equals("--") && !subjectSplit[2].equals("--")) {
                try {
                    Date date1 = scheduleDateFormat.parse(subjectSplit[1]);
                    Date date2 = scheduleDateFormat.parse(subjectSplit[2]);
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

    public List<Subject> getSubjects(DayType dayType, LunchType lunchType) {
        List<Subject> subjects = new ArrayList<>();

        for (int i = 0; i < getSubjectNames().length; i++) {
            if (subjectNames[i].startsWith("*")) //Names with asterisks are meant to be ignored
                continue;
            if (subjectNames[i].contains("/")) { //Example: "1st/5th Period"
                try {

                    if (subjectNames[i].toLowerCase().contains("lunch")) {
                        switch (dayType) {
                            case A_DAY:
                                switch (lunchType) {
                                    case AA_AB: //A Lunch on A Days
                                    case AA_BB:
                                        if (subjectNames[i].toLowerCase().contains("b lunch"))
                                            continue; //They have A lunch, not B, so skip this subject.
                                        break;
                                    case BA_AB: //B Lunch on A Days
                                    case BA_BB:
                                        if (subjectNames[i].toLowerCase().contains("a lunch"))
                                            continue; //They have B Lunch, not A, so skip this subject.
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case B_DAY:
                                switch (lunchType) {
                                    case AA_AB: //A Lunch on B Days
                                    case BA_AB:
                                        if (subjectNames[i].toLowerCase().contains("b lunch"))
                                            continue; //They have A lunch, not B, so skip this subject.
                                        break;
                                    case AA_BB: //B Lunch on B Days
                                    case BA_BB:
                                        if (subjectNames[i].toLowerCase().contains("a lunch"))
                                            continue; //They have B Lunch, not A, so skip this subject.
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                    }

                    StringBuilder newName = new StringBuilder();
                    switch (dayType) {
                        case A_DAY:
                            newName.append(subjectNames[i].substring(0, 3)).append(subjectNames[i].substring(7));
                            break; //Example: 1st Period
                        case B_DAY:
                            newName.append(subjectNames[i].substring(4));
                            break; //Example: 5th Period
                        default:
                            newName.append(subjectNames[i]);
                            break; //Example: 1st/5th Period
                    }

                    //Remove e.x.: "(B Lunchers)" from name
                    String name = newName.toString();
                    Pattern pattern = Pattern.compile("(.*)\\s\\([ab] lunchers\\)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(name);
                    if(matcher.find())
                    {
                        name = matcher.group(1);
                    }

                    Calendar startTime = Calendar.getInstance();
                    startTime.setTime(scheduleDateFormat.parse(subjectStartTimes[i]));

                    Calendar endTime = Calendar.getInstance();
                    endTime.setTime(scheduleDateFormat.parse(subjectStartTimes[i]));

                    subjects.add(new Subject(name, dayType, startTime, endTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return subjects;
    }

    public static class Subject {
        String name;
        DayType dayType;
        Calendar startTime;
        Calendar endTime;

        public Subject(String name, DayType dayType, Calendar start, Calendar end) {
            this.name = name;
            this.dayType = dayType;
            startTime = start;
            endTime = end;
        }

        public String getName() {
            return name;
        }

        public DayType getDayType() {
            return dayType;
        }

        public Calendar getStartTime() {
            return startTime;
        }

        public Calendar getEndTime() {
            return endTime;
        }
    }
}
