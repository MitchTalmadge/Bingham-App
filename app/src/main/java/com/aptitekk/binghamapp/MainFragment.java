package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.cards.CountdownCard;
import com.aptitekk.binghamapp.cards.CustomCountdownCardExpand;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardViewNative;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainActivity.FeedListener, MainActivity.BackButtonListener {

    CalendarDog eventsFeed;

    CountdownCard baseCountDownCard;
    CardViewNative countDownCardView;
    CustomCountdownCardExpand holidayCountDownCard;


    View latestNewsView;
    TextView latestNews_title;
    TextView latestNews_description;
    TextView latestNews_pubDate;

    Card nextEventCard;
    CardViewNative nextEventCardView;

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

        countDownCardView = (CardViewNative) getActivity().findViewById(R.id.countdowns);
        latestNewsView = getActivity().findViewById(R.id.latestnews);
        nextEventCardView = (CardViewNative) getActivity().findViewById(R.id.nextevent);

        ((MainActivity) getActivity()).addFeedListener(this);
    }

    @Override
    public void onNewsFeedDownloaded(final RSSNewsFeed newsFeed) {
        latestNews_title = (TextView) latestNewsView.findViewById(R.id.title);
        latestNews_description = (TextView) latestNewsView.findViewById(R.id.description);
        latestNews_pubDate = (TextView) latestNewsView.findViewById(R.id.pubDate);

        latestNews_title.setText(newsFeed.getNewsArticles().get(0).getTitle());
        latestNews_description.setText(newsFeed.getNewsArticles().get(0).getDescription());
        latestNews_pubDate.setText(newsFeed.getNewsArticles().get(0).getPubDate());
        latestNewsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewFragment webViewFragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("URL", newsFeed.getNewsArticles().get(0).getLink());
                webViewFragment.setArguments(bundle);

                getChildFragmentManager().beginTransaction()
                        .add(R.id.calendar_web_view_container, webViewFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("main")
                        .commit();
            }
        });
    }

    @Override
    public void onEventsFeedDownloaded(CalendarDog eventsFeed) {
        this.eventsFeed = eventsFeed;

        /////////////////// COUNTDOWN /////////////////////////////////////////////////////////////

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

        getView().findViewById(R.id.countdowns_progress_wheel).setVisibility(View.GONE);

        baseCountDownCard.addCardExpand(holidayCountDownCard);

        baseCountDownCard.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                card.doToogleExpand();
            }
        });

        countDownCardView.setCard(baseCountDownCard);
        ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder()
                        .setupView(countDownCardView);
        baseCountDownCard.setViewToClickToExpand(viewToClickToExpand);

        baseCountDownCard.refresh(this.eventsFeed, this, countDownCardView);
        holidayCountDownCard.refresh(this.eventsFeed, this, countDownCardView);

        baseCountDownCard.notifyDataSetChanged();

        //////////////// NEXT EVENT ///////////////////////////////////////////////////////////////

        getView().findViewById(R.id.nextevent_progress_wheel).setVisibility(View.GONE);

        //find if event if has already happened today
        CalendarEvent trueNextEvent = CalendarDog.getNextEvent(eventsFeed.getEvents(), new Date(), true);

        nextEventCard = CalendarDog.makeCalendarCard(this, trueNextEvent);
        CardHeader nextEventHeader = new CardHeader(getActivity());
        nextEventHeader.setTitle("Next Event");
        nextEventCard.addCardHeader(nextEventHeader);
        nextEventCardView.setCard(nextEventCard);

    }

    @Override
    public boolean onBackPressed() {
        if (!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        return false;
    }
}
