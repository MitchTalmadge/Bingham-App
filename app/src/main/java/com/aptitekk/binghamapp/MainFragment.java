package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.cards.CountdownCard;
import com.aptitekk.binghamapp.cards.HolidayCountdownCard;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainActivity.FeedListener {

    CalendarDog eventsFeed;

    CountdownCard countDownCard;
    CardViewNative countDownCardView;
    HolidayCountdownCard holidayCountDownCard;
    CardViewNative holidayCountDownCardView;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Create a Card
        countDownCard = new CountdownCard(getActivity());
        //Set the card inner text
        countDownCard.setTitle("Time remaining");
        //Set card in the cardView
        countDownCardView = (CardViewNative) getActivity().findViewById(R.id.immediateCountdown);
        countDownCardView.setCard(countDownCard);
        //Create a Card
        holidayCountDownCard = new HolidayCountdownCard(getActivity());
        //Set the card inner text
        holidayCountDownCard.setTitle("Time remaining");
        //Set card in the cardView
        holidayCountDownCardView = (CardViewNative) getActivity().findViewById(R.id.holidayCountdown);
        holidayCountDownCardView.setCard(holidayCountDownCard);

        if (MainActivity.eventsFeed == null) {
            ((MainActivity) getActivity()).addFeedListener(this);
            countDownCardView.setVisibility(View.GONE);
            holidayCountDownCardView.setVisibility(View.GONE);
        } else {
            eventsFeed = MainActivity.eventsFeed;
            countDownCard.refresh(eventsFeed, this, countDownCardView);
            holidayCountDownCard.refresh(eventsFeed, this, holidayCountDownCardView);
        }
    }

    @Override
    public void onNewsFeedDownloaded(RSSNewsFeed newsFeed) {

    }

    @Override
    public void onEventFeedDownloaded(CalendarDog eventFeed) {
        eventsFeed = eventFeed;
        countDownCard.refresh(eventsFeed, this, countDownCardView);
        holidayCountDownCard.refresh(eventsFeed, this, holidayCountDownCardView);
    }
}
