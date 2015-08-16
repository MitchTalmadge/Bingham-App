package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainActivity.FeedListener {

    CalendarDog eventsFeed;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.eventsFeed == null) {
            ((MainActivity) getActivity()).addFeedListener(this);
            this.getView().findViewById(R.id.currentPeriod).setVisibility(View.GONE);
            this.getView().findViewById(R.id.timeRemaining).setVisibility(View.GONE);
        } else {
            eventsFeed = MainActivity.eventsFeed;
            refreshCountdownCard();
        }
    }

    public void refreshCountdownCard() {
        final TextView timeRemaining = ((TextView) this.getView().findViewById(R.id.timeRemaining));
        TextView currentPeriod = ((TextView) this.getView().findViewById(R.id.currentPeriod));
        final String FORMAT = "%02d:%02d:%02d";

        //DETERMINE TIME
        Calendar currentDateTime = Calendar.getInstance();

        if (currentDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || currentDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            //pass onto before/after school
        } else {
            //PARSE ALL SCHEDULE TIMES
            ArrayList<BellSchedule.Subject> timeTable = BellSchedule.parseScheduleTimes(CalendarDog.determineSchedule(this, eventsFeed.getEvents(), currentDateTime));

            BellSchedule.Subject closest = BellSchedule.getNextSubject(currentDateTime.getTime(), timeTable);
            ArrayList<Date> closestPotentialTimes = new ArrayList<>();
            closestPotentialTimes.add(closest.getStartTime());
            closestPotentialTimes.add(closest.getEndTime());
            Date closestTime = CalendarDog.getNearestDate(closestPotentialTimes, currentDateTime.getTime());

            new CountDownTimer(closestTime.getTime(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }

                public void onFinish() {
                    refreshCountdownCard();
                }
            }.start();
            currentPeriod.setText("To " + closest.getName());
            this.getView().findViewById(R.id.progress_wheel_Main).setVisibility(View.GONE);
            this.getView().findViewById(R.id.currentPeriod).setVisibility(View.VISIBLE);
            this.getView().findViewById(R.id.timeRemaining).setVisibility(View.VISIBLE);
            return;
        }

        //IF ITS BEFORE/AFTER SCHOOL
        try {
            CalendarEvent nextABDay = eventsFeed.getEvents().get(CalendarDog.findNextAorBDay(eventsFeed.getEvents()));

            Log.i(MainActivity.LOG_NAME, nextABDay.toString() + " of " + nextABDay.getDate().get(Calendar.YEAR));

            new CountDownTimer(nextABDay.getDate().getTimeInMillis() - currentDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }

                public void onFinish() {
                    timeRemaining.setText("done!");
                }
            }.start();
            currentPeriod.setText("To Next " + nextABDay.getTitle());
        } catch (ArrayIndexOutOfBoundsException e) {
            currentPeriod.setText("Check if you can see the calendar events!");
            timeRemaining.setText("Whoops! :(");
        }
        this.getView().findViewById(R.id.progress_wheel_Main).setVisibility(View.GONE);
        this.getView().findViewById(R.id.currentPeriod).setVisibility(View.VISIBLE);
        this.getView().findViewById(R.id.timeRemaining).setVisibility(View.VISIBLE);
    }

    @Override
    public void onNewsFeedDownloaded(RSSNewsFeed newsFeed) {

    }

    @Override
    public void onEventFeedDownloaded(CalendarDog eventFeed) {
        eventsFeed = eventFeed;
        refreshCountdownCard();
    }
}
