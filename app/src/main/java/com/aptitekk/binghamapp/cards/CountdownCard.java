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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

import static com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog.isItAorBDay;

public class CountdownCard extends Card {

    TextView timeRemaining;
    TextView currentPeriod;

    public CountdownCard(Context context) {
        this(context, R.layout.card_countdown);
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
        Calendar currentDateTime = Calendar.getInstance();
        //DETERMINE A/B DAY
        char abday = isItAorBDay(eventsFeed.getEvents(), currentDateTime);

        if (currentDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && currentDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            //PARSE ALL SCHEDULE TIMES
            BellSchedule schedule = CalendarDog.determineSchedule(context, eventsFeed.getEvents(), currentDateTime);
            Log.i(MainActivity.LOG_NAME, "Schedule determined for day: " + schedule.getScheduleName());
            if (schedule != null) {
                ArrayList<BellSchedule.Subject> timeTable = BellSchedule.parseScheduleTimes(schedule, abday);

                BellSchedule.Subject closest = BellSchedule.getNextSubject(currentDateTime.getTime(), timeTable);
                ArrayList<Date> closestPotentialTimes = new ArrayList<>();
                closestPotentialTimes.add(closest.getStartTime());
                closestPotentialTimes.add(closest.getEndTime());
                Date closestTime = CalendarDog.getNearestDate(closestPotentialTimes, currentDateTime.getTime(), true);
                new CountDownTimer(closestTime.getTime() - currentDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here
                    public void onTick(long millisUntilFinished) {
                        timeRemaining.setText(CustomCountdownCardExpand.formatLongToReadableTime(millisUntilFinished));
                    }
                    public void onFinish() {
                        refresh(eventsFeed, context, cardHolder);
                    }
                }.start();
                currentPeriod.setText(formatCurrentPeriod(closest.getName(), closest.getABDay()));
                cardHolder.setVisibility(View.VISIBLE);
                cardHolder.refreshCard(this);
                return;
            }
        }

        //IF ITS BEFORE/AFTER SCHOOL YEAR OR WEEKENDS
        try {
            CalendarEvent nextABDay = eventsFeed.getEvents().get(CalendarDog.findNextAorBDay(eventsFeed.getEvents()));

            Log.i(MainActivity.LOG_NAME, nextABDay.toString() + " of " + nextABDay.getDate().get(Calendar.YEAR));

            new CountDownTimer(nextABDay.getDate().getTimeInMillis() - currentDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText(CustomCountdownCardExpand.formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    refresh(eventsFeed, context, cardHolder);
                }
            }.start();
            currentPeriod.setText("To Next " + nextABDay.getTitle());
        } catch (ArrayIndexOutOfBoundsException e) {
            currentPeriod.setText("Check if you can see the calendar events!");
            timeRemaining.setText("Whoops! :(");
        }
        cardHolder.setVisibility(View.VISIBLE);
        cardHolder.refreshCard(this);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        timeRemaining = ((TextView) parent.findViewById(R.id.timeRemaining));
        currentPeriod = ((TextView) parent.findViewById(R.id.currentPeriod));
    }

    private String formatCurrentPeriod(String rawName, char abday) {
        Log.i(MainActivity.LOG_NAME, "rawName: "+rawName);
        if (rawName.equals("Announcements"))
            rawName = "1st/5th Period";
        if (rawName.contains("/")) {
            String[] words = rawName.split(" "); // 1st/5th --- Period
            String[] abdayLabels = words[0].split("/");
            switch (abday) {
                case BellSchedule.A_DAY:
                    return "To " + abdayLabels[0] + " " + words[1];
                case BellSchedule.B_DAY:
                    return "To " + abdayLabels[1] + " " + words[1];
                default:
                    return "To " + rawName;
            }
        }
        return "To " + rawName;
    }


}
