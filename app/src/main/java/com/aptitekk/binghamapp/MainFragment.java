package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.cards.CountdownCard;
import com.aptitekk.binghamapp.cards.CustomCountdownCardExpand;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardViewNative;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainActivity.FeedListener {

    CalendarDog eventsFeed;

    CountdownCard baseCountDownCard;
    CardViewNative countDownCardView;
    CustomCountdownCardExpand holidayCountDownCard;
    //CardViewNative holidayCountDownCardView;

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
        baseCountDownCard = new CountdownCard(getActivity());
        //Set the card inner text
        CardHeader header = new CardHeader(getActivity());
        header.setTitle("Countdown");
        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);
        //Add Header to card
        baseCountDownCard.addCardHeader(header);

        holidayCountDownCard = new CustomCountdownCardExpand(getActivity(), CustomCountdownCardExpand.CountdownTarget.HOLIDAY);
        baseCountDownCard.addCardExpand(holidayCountDownCard);

        countDownCardView = (CardViewNative) getActivity().findViewById(R.id.countdowns);
        ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder()
                        .setupView(countDownCardView);
        baseCountDownCard.setViewToClickToExpand(viewToClickToExpand);

        countDownCardView.setCard(baseCountDownCard);

        /*//Create a Card
        holidayCountDownCard = new HolidayCountdownCard(getActivity());
        //Set the card inner text
        holidayCountDownCard.setTitle("Time remaining");
        //Set card in the cardView
        holidayCountDownCardView = (CardViewNative) getActivity().findViewById(R.id.holidayCountdown);
        holidayCountDownCardView.setCard(holidayCountDownCard);*/

        countDownCardView.setVisibility(View.GONE);
        //holidayCountDownCardView.setVisibility(View.GONE);

        ((MainActivity) getActivity()).addFeedListener(this);
    }

    @Override
    public void onNewsFeedDownloaded(RSSNewsFeed newsFeed) {

    }

    @Override
    public void onEventsFeedDownloaded(CalendarDog eventsFeed) {
        this.eventsFeed = eventsFeed;

        countDownCardView.setVisibility(View.VISIBLE);

        baseCountDownCard.refresh(this.eventsFeed, this, countDownCardView);
        holidayCountDownCard.refresh(this.eventsFeed, this, countDownCardView);
    }
}
