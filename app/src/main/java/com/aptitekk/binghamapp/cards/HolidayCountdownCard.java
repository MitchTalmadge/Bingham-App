package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * Created by kevint on 8/16/2015.
 */
public class HolidayCountdownCard extends Card {

    TextView timeRemaining;
    TextView currentPeriod;

    public HolidayCountdownCard(Context context) {
        this(context, R.layout.card_countdown);
    }

    public HolidayCountdownCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    private void init(){
        this.setTitle("Time Remaining");
    }

    public void refresh(final CalendarDog eventsFeed, final Fragment context, final CardViewNative cardHolder) {
        final String FORMAT = "%02d hours %02d minutes %02d seconds";

        //DETERMINE TIME
        Calendar currentDateTime = Calendar.getInstance();

        //IF ITS BEFORE/AFTER SCHOOL
        try {
            CalendarEvent nextABDay = eventsFeed.getEvents().get(CalendarDog.findNextHolidayByIndex(eventsFeed.getEvents()));

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
            currentPeriod.setText("To " + nextABDay.getTitle());
        } catch (ArrayIndexOutOfBoundsException e) {
            currentPeriod.setText("Check if you can see the calendar events!");
            timeRemaining.setText("Whoops! :(");
        }
        cardHolder.setVisibility(View.VISIBLE);
        cardHolder.refreshCard(this);
        return;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        timeRemaining = ((TextView) parent.findViewById(R.id.timeRemaining));
        currentPeriod = ((TextView) parent.findViewById(R.id.currentPeriod));
    }
}