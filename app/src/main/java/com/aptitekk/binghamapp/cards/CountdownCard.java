package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.Events.DayType;
import com.aptitekk.binghamapp.Events.Event;
import com.aptitekk.binghamapp.Events.EventInfoHelper;
import com.aptitekk.binghamapp.Events.EventsManager;
import com.aptitekk.binghamapp.Fragments.BellSchedules.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class CountdownCard extends Card {

    public enum CountdownTarget {
        FOOTBALL("football", R.drawable.event_football),
        TENNIS("tennis", R.drawable.event_tennis),
        DANCE("dance", R.drawable.event_dance),
        MINERETTES("minerette", R.drawable.event_dance),
        VOLLEYBALL("volleyball", R.drawable.event_volleyball),
        BASKETBALL("basketball", R.drawable.event_basketball),
        BASKETBALL2("bball", R.drawable.event_basketball),
        BASEBALL("baseball", R.drawable.event_baseball),
        SOCCER("soccer", R.drawable.event_soccer),
        WRESTLING("wrestling", R.drawable.event_wrestling),
        SWIMMING("swimming", R.drawable.event_swimming),
        TRACK("track", R.drawable.event_track),
        CHOIR("choir", R.drawable.event_choir),
        SYMPHONY("symphony", R.drawable.event_symphony),
        CONCERT("concerto", R.drawable.event_symphony),
        ORCHESTRA("orchestra", R.drawable.event_symphony),
        BAND("band", R.drawable.event_symphony),
        JAZZ("jazz", R.drawable.event_jazz),
        CANDLE("candle", R.drawable.event_candle),
        PLAY("play", R.drawable.event_play),
        COUNCIL("council", R.drawable.event_council),
        ZEROFATALITIES("fatalities", R.drawable.event_zerofatalities),
        PTC("parent teacher conference", R.drawable.event_grades),
        SPIRIT("spirit", R.drawable.event_spirit),
        TEST("test", R.drawable.event_test),
        FCCLA("fccla", R.drawable.event_fccla),
        ART("art show", R.drawable.event_art),
        ASSEMBLY("assembly", R.drawable.event_assembly),
        TALENT_SHOW("talent show", R.drawable.event_assembly),
        HOLIDAY("no school", R.drawable.event_noschool),
        HOLIDAY2("no students attend", R.drawable.event_noschool),
        LAST_DAY("last day of school", R.drawable.event_lastday);

        String value;
        int drawableId;

        CountdownTarget(String value, int drawableId) {
            this.value = value;
            this.drawableId = drawableId;
        }

        public String getValue() {
            return value;
        }

        public int getImageDrawableId() {
            return drawableId;
        }
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

    public void refresh(final EventsManager eventsManager, final Fragment context, final CardViewNative cardHolder) {
        //DETERMINE TIME
        final Date currentDateTime = new Date();
        Calendar targetDateTime = Calendar.getInstance(); // by default, todays datetime
        //DETERMINE A/B DAY
        DayType currentDayType = eventsManager.getEventInfoHelper().getDayType(targetDateTime);
        //CREATE POINTER IF IT IS THE END OF PERIOD
        boolean isTimeEndTime;
        boolean tomorrowClosest = false;
        boolean schoolStartedForDay = false;

        if (targetDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && targetDateTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            //PARSE ALL SCHEDULE TIMES
            BellSchedule schedule = eventsManager.getEventInfoHelper().getBellScheduleForDay(targetDateTime);
            BellSchedule todaySchedule = schedule;
            // IS SCHOOL OUT THOUGH?
            try {
                if (!eventsManager.getEventInfoHelper().hasSchoolEndedForDay(schedule, targetDateTime)) {
                    Calendar tomorrow = targetDateTime;
                    tomorrow.add(Calendar.DATE, 1);
                    schedule = eventsManager.getEventInfoHelper().getBellScheduleForDay(tomorrow);
                    currentDayType = eventsManager.getEventInfoHelper().getDayType(tomorrow);
                    targetDateTime = tomorrow;
                    tomorrowClosest = true;
                }
                if (eventsManager.getEventInfoHelper().hasSchoolStartedForDay(schedule, targetDateTime)) {
                    schoolStartedForDay = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (schedule != null) {
                MainActivity.logVerbose("Schedule determined for day: " + schedule.getScheduleName());
                ArrayList<BellSchedule.Subject> timeTable = BellSchedule.parseScheduleTimes(schedule, currentDayType, targetDateTime.getTime());
                BellSchedule.Subject closest = BellSchedule.getNextSubject(currentDateTime, timeTable, true);
                EventInfoHelper.MultipleReturn response = eventsManager.getEventInfoHelper().getNearestDateBySubjectIsEndTime(closest, currentDateTime, true);
                final Date closestTime = (Date) response.getFirst();
                isTimeEndTime = (boolean) response.getSecond();

                Date closestPastTime;
                if (tomorrowClosest) { // The previous end time may have been from today if school ended
                    MainActivity.logVerbose("Today is the closest previous schedule");
                    BellSchedule.Subject closestPast = BellSchedule.getPreviousSubject(currentDateTime, BellSchedule.parseScheduleTimes(todaySchedule,
                            currentDayType, Calendar.getInstance().getTime()));
                    closestPastTime = eventsManager.getEventInfoHelper().getNearestDateBySubject(closestPast, currentDateTime, false);
                } else if (schoolStartedForDay) { // if school has started
                    MainActivity.logVerbose("Earlier class periods of today is the closest previous schedule");
                    BellSchedule.Subject closestPast = BellSchedule.getPreviousSubject(currentDateTime, timeTable);
                    closestPastTime = eventsManager.getEventInfoHelper().getNearestDateBySubject(closestPast, currentDateTime, false);
                } else { // pull yesterdays schedule
                    MainActivity.logVerbose("Yesterday is the closest previous schedule");
                    targetDateTime.add(Calendar.DATE, -1); //Yesterday
                    MainActivity.logVerbose("Yesterday is " + targetDateTime.getTime().toString());
                    BellSchedule yesterdaySchedule = eventsManager.getEventInfoHelper().getBellScheduleForDay(targetDateTime);
                    BellSchedule.Subject closestPast = BellSchedule.getPreviousSubject(currentDateTime, BellSchedule.parseScheduleTimes(yesterdaySchedule,
                            currentDayType, targetDateTime.getTime()));
                    closestPastTime = eventsManager.getEventInfoHelper().getNearestDateBySubject(closestPast, currentDateTime, false); // TODO: Have getPreviousSubject return closest date to resolve redundancy
                }
                final Date finalClosestPastTime = closestPastTime;
                if (finalClosestPastTime.getTime() == 0) {
                    MainActivity.logVerbose("closestPastTime is 0!!!");
                }
                new CountDownTimer(closestTime.getTime() - currentDateTime.getTime(), 1000) { // adjust the milli seconds here
                    public void onTick(long millisUntilFinished) {
                        long top = Math.abs(closestTime.getTime() - finalClosestPastTime.getTime() - millisUntilFinished);
                        long bottom = Math.abs(closestTime.getTime() - finalClosestPastTime.getTime());
                        int percent = (int) Math.round(((double) top / bottom) * 100);
                        MainActivity.logVerbose(top + "/" + bottom + " *100 = " + percent + "%");
                        progress.setProgress(percent);
                        timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                    }

                    public void onFinish() {
                        refresh(eventsManager, context, cardHolder);
                    }
                }.start();
                currentPeriod.setText(formatCurrentPeriod(closest.getName(), closest.getDayType(), isTimeEndTime, tomorrowClosest));
                abDayLabel.setText(currentDayType.getFriendlyName());
                cardHolder.setVisibility(View.VISIBLE);
                cardHolder.refreshCard(this);
                return;
            }
        }

        //IF ITS BEFORE/AFTER SCHOOL YEAR OR WEEKENDS
        try {
            Event nextABDay = eventsManager.getEventInfoHelper().getNextABDayEvent(Calendar.getInstance());

            MainActivity.logVerbose(nextABDay.toString() + " of " + nextABDay.getEventDate().get(Calendar.YEAR));

            new CountDownTimer(nextABDay.getEventDate().getTimeInMillis() - targetDateTime.getTimeInMillis(), 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    refresh(eventsManager, context, cardHolder);
                }
            }.start();
            currentPeriod.setText("To Next " + nextABDay.getTitle());
            abDayLabel.setText(currentDayType.getFriendlyName());
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

    private String formatCurrentPeriod(String rawName, DayType dayType, boolean isEndTime, boolean tomorrow) {
        MainActivity.logVerbose("rawName: " + rawName);
        if (rawName.equals("Announcements"))
            rawName = "1st/5th Period";
        if (rawName.contains("/")) {
            String[] words = rawName.split(" "); // 1st/5th --- Period
            String[] abdayLabels = words[0].split("/");
            switch (dayType) {
                case A_DAY:
                    return "To " + ((isEndTime) ? "end of " : "") + abdayLabels[0] + " " + words[1] + ((tomorrow) ? " tomorrow" : "");
                case B_DAY:
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
        long weeks = (millis / (1000 * 60 * 60 * 24 * 7));
        millis -= TimeUnit.DAYS.toMillis(weeks * 7);
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
            sb.append(" week").append((weeks == 1) ? "" : "s").append(" ");
        }
        if (days > 0 || (days == 0 && weeks > 0)) {
            sb.append(days);
            sb.append(" day").append((days == 1) ? "" : "s").append(" ");
        }
        if (hours > 0 || (hours == 0 && days > 0)) {
            sb.append(hours);
            sb.append(" hour").append((hours == 1) ? "" : "s").append(" ");
        }
        if (minutes > 0 || (minutes == 0 && hours > 0)) {
            sb.append(minutes);
            sb.append(" minute").append((minutes == 1) ? "" : "s").append(" ");
        }
        sb.append(seconds);
        sb.append(" second").append((seconds == 1) ? "" : "s");

        return (sb.toString());
    }

}
