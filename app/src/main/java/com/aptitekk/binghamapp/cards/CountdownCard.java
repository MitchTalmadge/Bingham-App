package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.BellSchedulesFragmentClasses.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

import static com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog.isItAorBDay;
import static com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog.isSchoolInSession;

public class CountdownCard extends Card {

    public enum CountdownTarget {
        HOLIDAY("no school", ""),
        FOOTBALL("football", "http://img.deseretnews.com/images/article/midres/648384/648384.jpg"),
        TENNIS("tennis", "http://archive.southvalleyjournal.com/content/files/Article_Files/bhs-tennis-001-713.JPG"),
        SPORTS("vs.", ""),
        LASTDAY("last day of school", ""),
        DANCE("dance", "");

        String value;
        String image;

        CountdownTarget(String value, String image) {
            this.value = value;
            this.image = image;
        }

        public String getValue() {
            return value;
        }
        public String getImageUrl() { return image; }
    }

    TextView timeRemaining;
    TextView currentPeriod;
    TextView abDayLabel;

    ArcProgress progress;

    public CountdownCard(Context context) {
        this(context, R.layout.widget_countdown);
    }

    public CountdownCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    private void init() {
        this.setTitle("Time Remaining");
    }

    public void refresh(final CalendarDog eventsFeed, final Fragment context, final CardViewNative cardHolder) {
        //DETERMINE TIME
        final Date currentDateTime = new Date();
        Calendar targetDateTime = Calendar.getInstance(); // by default, todays datetime
        //DETERMINE A/B DAY
        char abday = isItAorBDay(eventsFeed.getEvents(), targetDateTime);
        //CREATE POINTER IF IT IS THE END OF PERIOD
        boolean isTimeEndTime = false;
        boolean tomorrowClosest = false;

        if (targetDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && targetDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            //PARSE ALL SCHEDULE TIMES
            BellSchedule schedule = CalendarDog.determineSchedule(context, eventsFeed.getEvents(), targetDateTime);
            // IS SCHOOL OUT THOUGH?
            try {
                if (!isSchoolInSession(schedule, targetDateTime)) {
                    Calendar tomorrow = targetDateTime;
                    tomorrow.add(Calendar.DATE, 1);
                    schedule = CalendarDog.determineSchedule(context, eventsFeed.getEvents(), tomorrow);
                    abday = isItAorBDay(eventsFeed.getEvents(), tomorrow);
                    targetDateTime = tomorrow;
                    tomorrowClosest = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.i(MainActivity.LOG_NAME, "Schedule determined for day: " + schedule.getScheduleName());
            if (schedule != null) {
                ArrayList<BellSchedule.Subject> timeTable = BellSchedule.parseScheduleTimes(schedule, abday, targetDateTime.getTime());
                BellSchedule.Subject closest = BellSchedule.getNextSubject(currentDateTime, timeTable);
                final Date closestTime = CalendarDog.getNearestDateBySubject(closest, currentDateTime, true, isTimeEndTime);
                new CountDownTimer(closestTime.getTime() - currentDateTime.getTime(), 1000) { // adjust the milli seconds here
                    public void onTick(long millisUntilFinished) {
                        progress.setProgress((int) (((closestTime.getTime() - currentDateTime.getTime() - millisUntilFinished)
                                / (closestTime.getTime() - currentDateTime.getTime())*100)));
                        timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                    }

                    public void onFinish() {
                        refresh(eventsFeed, context, cardHolder);
                    }
                }.start();
                currentPeriod.setText(formatCurrentPeriod(closest.getName(), closest.getABDay(), isTimeEndTime, tomorrowClosest));
                abDayLabel.setText(abday);
                cardHolder.setVisibility(View.VISIBLE);
                cardHolder.refreshCard(this);
                return;
            }
        }

        //IF ITS BEFORE/AFTER SCHOOL YEAR OR WEEKENDS
        try {
            CalendarEvent nextABDay = eventsFeed.getEvents().get(CalendarDog.findNextAorBDay(eventsFeed.getEvents()));

            Log.i(MainActivity.LOG_NAME, nextABDay.toString() + " of " + nextABDay.getDate().get(Calendar.YEAR));

            new CountDownTimer(nextABDay.getDate().getTimeInMillis() - targetDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    refresh(eventsFeed, context, cardHolder);
                }
            }.start();
            currentPeriod.setText("To Next " + nextABDay.getTitle());
            abDayLabel.setText(abday);
        } catch (ArrayIndexOutOfBoundsException e) {
            abDayLabel.setText(":(");
            currentPeriod.setText("Check if you can see the calendar events!");
            timeRemaining.setText("Whoops!");
        }
        cardHolder.setVisibility(View.VISIBLE);
        cardHolder.refreshCard(this);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        timeRemaining = ((TextView) parent.findViewById(R.id.timeRemaining));
        currentPeriod = ((TextView) parent.findViewById(R.id.currentPeriod));
        progress = ((ArcProgress) parent.findViewById(R.id.PROGRESS_BAR));
        abDayLabel = ((TextView) parent.findViewById(R.id.abdayLabel));
    }

    private String formatCurrentPeriod(String rawName, char abday, boolean isEndTime, boolean tomorrow) {
        Log.i(MainActivity.LOG_NAME, "rawName: " + rawName);
        if (rawName.equals("Announcements"))
            rawName = "1st/5th Period";
        if (rawName.contains("/")) {
            String[] words = rawName.split(" "); // 1st/5th --- Period
            String[] abdayLabels = words[0].split("/");
            switch (abday) {
                case BellSchedule.A_DAY:
                    return "To " + ((isEndTime) ? "end of " : "") + abdayLabels[0] + " " + words[1] + ((tomorrow) ? " tomorrow" : "");
                case BellSchedule.B_DAY:
                    return "To " + ((isEndTime) ? "end of " : "") + abdayLabels[1] + " " + words[1] + ((tomorrow) ? " tomorrow" : "");
                default:
                    return "To " + ((isEndTime) ? "end of " : "") + rawName + ((tomorrow) ? " tomorrow" : "");
            }
        }
        return "To " + ((isEndTime) ? "end of " : "") + rawName + ((tomorrow) ? " tomorrow" : "");
    }

    public static String formatLongToReadableTime(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long weeks = (millis / (1000*60*60*24*7));
        millis -= TimeUnit.DAYS.toMillis(weeks*7);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (weeks > 0) {
            sb.append(weeks);
            sb.append(" week" +((weeks == 1) ? "": "s")+" ");
        }
        if (days > 0 || (days == 0 && weeks > 0)) {
            sb.append(days);
            sb.append(" day" +((days == 1) ? "": "s")+" ");
        }
        if (hours > 0 || (hours == 0 && days > 0)) {
            sb.append(hours);
            sb.append(" hour" +((hours == 1) ? "": "s")+" ");
        }
        if (minutes > 0 || (minutes == 0 && hours >0)) {
            sb.append(minutes);
            sb.append(" minute" +((minutes == 1) ? "": "s")+" ");
        }
        sb.append(seconds);
        sb.append(" second" +((seconds == 1) ? "": "s"));

        return (sb.toString());
    }

}
