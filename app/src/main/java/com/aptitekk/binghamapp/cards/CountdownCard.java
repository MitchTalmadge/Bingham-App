package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.Events.DayType;
import com.aptitekk.binghamapp.Events.Event;
import com.aptitekk.binghamapp.Events.EventsManager;
import com.aptitekk.binghamapp.Events.LunchType;
import com.aptitekk.binghamapp.Fragments.BellSchedules.BellSchedule;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class CountdownCard extends Card {

    private final EventsManager eventsManager;
    private final CardViewNative cardHolder;
    private TextView timeRemaining;
    private TextView currentPeriod;
    private TextView abDayLabel;

    private ArcProgress progress;
    private boolean disabled = false;

    private CountDownTimer timer;

    public CountdownCard(Context context, EventsManager eventsManager, CardViewNative cardHolder) {
        super(context, R.layout.widget_countdown);
        this.eventsManager = eventsManager;
        this.cardHolder = cardHolder;
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

    public void cancel() {
        MainActivity.logVerbose("Cancelling Countdown Card");
        if (this.timer != null)
            timer.cancel();

        timer = null;
    }

    public void setDisabled(boolean disabled) {
        MainActivity.logVerbose((disabled ? "Disabling" : "Enabling") + " Countdown Card");
        this.disabled = disabled;
        refresh();
    }

    public void refresh() {
        if (disabled) {
            progress.setProgress(0);
            timeRemaining.setText("");
            currentPeriod.setText(R.string.countdown_no_events);
            abDayLabel.setText("?");
            return;
        }

        final Calendar todayCalendar = Calendar.getInstance();
        final DayType todayDayType = eventsManager.getEventInfoHelper().getDayType(todayCalendar);
        final BellSchedule todayBellSchedule = eventsManager.getEventInfoHelper().getBellScheduleForDay(todayCalendar);
        final List<Event> todayEvents = eventsManager.getEventInfoHelper().getEventsForDay(todayCalendar, true);

        final Calendar twelveAMCalendar = Calendar.getInstance();
        twelveAMCalendar.set(Calendar.HOUR_OF_DAY, 0);
        twelveAMCalendar.set(Calendar.MINUTE, 0);
        twelveAMCalendar.set(Calendar.SECOND, 0);

        Calendar timeToCountdownFrom = null;
        Calendar timeToCountdownTo = null;
        String descriptionText = "";

        if (todayBellSchedule != null) {
            List<BellSchedule.Subject> subjects = todayBellSchedule.getSubjects(todayDayType, LunchType.AA_AB, todayCalendar); //TODO: Get LunchType from student

            BellSchedule.Subject currentSubject = null;
            boolean beforeSchool = true;
            boolean afterSchool = true;

            for (BellSchedule.Subject subject : subjects) {
                //Check if the current time is in between this subject's start and end times
                if (subject.getStartTime().compareTo(todayCalendar) <= 0) { //A subject has started, so we're not before school.
                    beforeSchool = false;
                    if (subject.getEndTime().compareTo(todayCalendar) >= 0) {
                        currentSubject = subject;
                    }
                } else { //A subject is coming up, so we're not after school.
                    afterSchool = false;
                }
            }

            MainActivity.logVerbose("Before School? " + beforeSchool);
            MainActivity.logVerbose("After School? " + afterSchool);
            MainActivity.logVerbose("Current Subject: " + (currentSubject != null ? currentSubject.getName() : "None."));

            if (currentSubject == null) //We are not currently in a subject, or there are none today.
            {
                if (beforeSchool) {
                    MainActivity.logVerbose("(Before School) Counting down to " + subjects.get(0).getName());

                    timeToCountdownFrom = twelveAMCalendar; //Countdown from 12 AM of same day.

                    timeToCountdownTo = subjects.get(0).getStartTime();
                    descriptionText = "To the beginning of " + subjects.get(0).getName();
                } else if (afterSchool) {
                    for (Event event : todayEvents) { //Countdown to next event today (if exists)
                        if (event.getStartTime().compareTo(todayCalendar) >= 0) {
                            MainActivity.logVerbose("(After School) Counting down to " + event.getTitle());
                            timeToCountdownFrom = subjects.get(subjects.size() - 1).getEndTime(); //Countdown from end of school
                            timeToCountdownTo = event.getStartTime(); //Countdown to beginning of event
                            descriptionText = "To the beginning of the event: " + event.getTitle();
                            break;
                        }
                    }
                    //TODO: Countdown to next event OR first subject of next school day
                    if (timeToCountdownTo == null)
                        setDisabled(true); //Last resort, disable countdown.
                } else { //Time between subjects (5 minute hallway time)
                    for (int i = 0; i < subjects.size(); i++) { //Countdown to next subject.
                        BellSchedule.Subject subject = subjects.get(i);
                        if (subject.getStartTime().compareTo(todayCalendar) > 0) {
                            MainActivity.logVerbose("(Between Subjects) Counting down to " + subject.getName());
                            timeToCountdownFrom = subjects.get(i - 1).getEndTime(); //Countdown from end of last subject
                            timeToCountdownTo = subject.getStartTime(); //Countdown to beginning of next subject
                            descriptionText = "To the beginning of " + subject.getName();
                            break;
                        }
                    }
                }
            } else {
                MainActivity.logVerbose("(During Subject) Counting down to end of " + currentSubject.getName());
                timeToCountdownFrom = currentSubject.getStartTime(); //Countdown from beginning of current subject
                timeToCountdownTo = currentSubject.getEndTime(); //Countdown to end of current subject
                descriptionText = "To the end of " + currentSubject.getName();
            }
        } else {
            Event previousEvent = null;

            for (Event event : todayEvents) {
                if (event.getStartTime().compareTo(todayCalendar) >= 0) {
                    MainActivity.logVerbose("(No School) Counting down to beginning of " + event.getTitle());
                    if (previousEvent != null)
                        timeToCountdownFrom = previousEvent.getEndTime();
                    else
                        timeToCountdownFrom = twelveAMCalendar;
                    timeToCountdownTo = event.getStartTime();
                    descriptionText = "To the beginning of the event: " + event.getTitle();
                    break;
                }
                previousEvent = event;
            }

            if (timeToCountdownTo == null) {
                for (Event event : todayEvents) {
                    if (event.getStartTime().compareTo(todayCalendar) <= 0 && event.getEndTime().compareTo(todayCalendar) > 0) {
                        MainActivity.logVerbose("(No School) Counting down to end of " + event.getTitle());
                        timeToCountdownFrom = event.getStartTime();
                        timeToCountdownTo = event.getEndTime();
                        descriptionText = "To the end of the event: " + event.getTitle();
                        break;
                    }
                }
            }
            //TODO: Countdown to next event OR first subject of next school day

            if (timeToCountdownTo == null)
                setDisabled(true); //Last resort, disable countdown.
        }

        if (timeToCountdownTo != null) {
            final Calendar finalTimeToCountdownFrom = timeToCountdownFrom;
            final Calendar finalTimeToCountdownTo = timeToCountdownTo;
            MainActivity.logVerbose("Counting Down From: " + finalTimeToCountdownFrom.getTime().getTime() + " - To: " + finalTimeToCountdownTo.getTime().getTime() + " - Current Time: "+todayCalendar.getTime().getTime());
            if (this.timer != null)
                timer.cancel();
            this.timer = new CountDownTimer(finalTimeToCountdownTo.getTime().getTime() - todayCalendar.getTime().getTime(), 1000) {
                public void onTick(long millisUntilFinished) {
                    long current = Math.abs(finalTimeToCountdownTo.getTime().getTime() - millisUntilFinished - finalTimeToCountdownFrom.getTime().getTime());
                    long end = Math.abs(finalTimeToCountdownTo.getTime().getTime() - finalTimeToCountdownFrom.getTime().getTime());
                    int percent = (int) Math.round(((double) current / end) * 100);
                    progress.setProgress(percent);
                    timeRemaining.setText(formatLongToReadableTime(millisUntilFinished));
                }

                public void onFinish() {
                    MainActivity.logVerbose("Countdown Finished.");
                    refresh();
                }
            }.start();
            currentPeriod.setText(descriptionText);
            abDayLabel.setText(todayDayType.getFriendlyName());
            cardHolder.setVisibility(View.VISIBLE);
            cardHolder.refreshCard(this);
            MainActivity.logVerbose("Countdown Started.");
        }
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
