package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * Created by kevint on 8/17/2015.
 */
public class CustomCountdownCardExpand extends CardExpand {

    public static enum CountdownTarget {
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

    CountdownTarget target;

    TextView timeRemaining;
    TextView currentPeriod;

    public CustomCountdownCardExpand(Context context) {
        this(context, R.layout.card_countdown);
    }

    public CustomCountdownCardExpand(Context context, int innerLayout) {
        this(context, innerLayout, CountdownTarget.HOLIDAY);
    }

    public CustomCountdownCardExpand(Context context, CountdownTarget target) {
        super(context, R.layout.card_countdown);
        this.target = target;
    }

    public CustomCountdownCardExpand(Context context, int innerLayout, CountdownTarget target) {
        super(context, innerLayout);
        this.target = target;
    }

    public void refresh(final CalendarDog eventsFeed, final Fragment context, final CardViewNative cardHolder) {
        //DETERMINE TIME
        Calendar currentDateTime = Calendar.getInstance();

        //IF ITS BEFORE/AFTER SCHOOL
        try {
            CalendarEvent targetEvent = eventsFeed.getEvents().get(CalendarDog.findNextTargetByIndex(eventsFeed.getEvents(), target));

            new CountDownTimer(targetEvent.getDate().getTimeInMillis() - currentDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    refresh(eventsFeed, context, cardHolder);
                }
            }.start();
            currentPeriod.setText("To " + targetEvent.getTitle());
        } catch (ArrayIndexOutOfBoundsException e) {
            currentPeriod.setText("Check if you can see the calendar events!");
            timeRemaining.setText("Whoops! :(");
            e.printStackTrace();
        }
        cardHolder.setVisibility(View.VISIBLE);
        cardHolder.refreshCard(this.getParentCard());
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        timeRemaining = ((TextView) parent.findViewById(R.id.timeRemaining));
        currentPeriod = ((TextView) parent.findViewById(R.id.currentPeriod));
        timeRemaining.setTextColor(parent.getResources().getColor(R.color.inverse_primary_text));
        currentPeriod.setTextColor(parent.getResources().getColor(R.color.inverse_primary_text));
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
