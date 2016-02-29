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
import com.aptitekk.binghamapp.Events.LunchType;
import com.aptitekk.binghamapp.Fragments.BellSchedules.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class CountdownCard extends Card {

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

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        timeRemaining = ((TextView) parent.findViewById(R.id.timeRemaining));
        currentPeriod = ((TextView) parent.findViewById(R.id.currentPeriod));
        progress = ((ArcProgress) parent.findViewById(R.id.PROGRESS_BAR));
        abDayLabel = ((TextView) parent.findViewById(R.id.abdayLabel));
    }

    public void refresh(final EventsManager eventsManager, final Fragment context, final CardViewNative cardHolder) {
        final Calendar todayCalendar = Calendar.getInstance();
        final DayType todayDayType = eventsManager.getEventInfoHelper().getDayType(todayCalendar);
        final BellSchedule todayBellSchedule = eventsManager.getEventInfoHelper().getBellScheduleForDay(todayCalendar);
        final List<Event> todayEvents = eventsManager.getEventInfoHelper().getEventsForDay(todayCalendar, true);

        Calendar timeToCountdownTo = null;
        String descriptionText = "";

        if (todayBellSchedule != null) {
            List<BellSchedule.Subject> subjects = todayBellSchedule.getSubjects(todayDayType, LunchType.AA_AB); //TODO: Get from student

            BellSchedule.Subject currentSubject = null;
            boolean beforeSchool = true;
            boolean afterSchool = true;

            for (BellSchedule.Subject subject : subjects) {
                //Check if the current time is in between this subject's start and end times
                if (subject.getStartTime().compareTo(todayCalendar) <= 0) { //A subject has passed, so we're not before school.
                    beforeSchool = false;
                    if (subject.getEndTime().compareTo(todayCalendar) >= 0) {
                        currentSubject = subject;
                    }
                } else //A subject is coming up, so we're not after school.
                {
                    afterSchool = false;
                }
            }

            if (currentSubject == null) //We are either before or after school, or there are no subjects today.
            {
                if (beforeSchool) {
                    timeToCountdownTo = subjects.get(0).getStartTime();
                    descriptionText = "To the beginning of " + subjects.get(0).getName();
                } else if (afterSchool) {
                    for (Event event : todayEvents) { //Countdown to next event today (if exists)
                        if (event.getEventDate().compareTo(todayCalendar) >= 0) {
                            timeToCountdownTo = event.getEventDate();
                            descriptionText = "To the beginning of the event: " + event.getTitle();
                            break;
                        }
                    }
                    //TODO: Countdown to next event OR first subject of next school day
                } else { //Time between subjects (5 minute hallway time)
                    for (BellSchedule.Subject subject : subjects) { //Countdown to next subject.
                        if (subject.getStartTime().compareTo(todayCalendar) > 0) {
                            timeToCountdownTo = subject.getStartTime();
                            descriptionText = "To the beginning of " + subject.getName();
                            break;
                        }
                    }
                }
            } else {
                timeToCountdownTo = currentSubject.getEndTime(); //Countdown to end of current subject
                descriptionText = "To the end of " + currentSubject.getName();
            }
        } else {
            for (Event event : todayEvents) {
                if (event.getEventDate().compareTo(todayCalendar) >= 0) {
                    timeToCountdownTo = event.getEventDate();
                    descriptionText = "To the beginning of the event: " + event.getTitle();
                    break;
                }
            }
            //TODO: Countdown to next event OR first subject of next school day
        }

        if (timeToCountdownTo != null) {
            final Calendar finalTimeToCountdownTo = timeToCountdownTo;
            new CountDownTimer(finalTimeToCountdownTo.getTime().getTime() - todayCalendar.getTime().getTime(), 1000) { // adjust the milli seconds here
                public void onTick(long millisUntilFinished) {
                    long current = Math.abs(finalTimeToCountdownTo.getTime().getTime() - millisUntilFinished);
                    long end = Math.abs(finalTimeToCountdownTo.getTime().getTime());
                    int percent = (int) Math.round(((double) current / end) * 100);
                    MainActivity.logVerbose(current + "/" + end + " *100 = " + percent + "% (Millis until finished = " + millisUntilFinished + ")");
                    progress.setProgress(percent);
                    timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    refresh(eventsManager, context, cardHolder);
                }
            }.start();
            currentPeriod.setText(descriptionText);
            abDayLabel.setText(todayDayType.getFriendlyName());
            cardHolder.setVisibility(View.VISIBLE);
            cardHolder.refreshCard(this);
        }
        return;
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
