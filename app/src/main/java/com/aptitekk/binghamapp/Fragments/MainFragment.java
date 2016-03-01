package com.aptitekk.binghamapp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.Events.Event;
import com.aptitekk.binghamapp.Events.EventsManager;
import com.aptitekk.binghamapp.Events.EventsUpdateListener;
import com.aptitekk.binghamapp.Fragments.HelperFragments.WebViewFragment;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.News.NewsFeed;
import com.aptitekk.binghamapp.News.NewsFeedType;
import com.aptitekk.binghamapp.News.NewsFeedUpdateListener;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.cards.CountdownCard;

import java.util.Calendar;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardViewNative;


public class MainFragment extends Fragment implements MainActivity.BackButtonListener, NewsFeedUpdateListener, EventsUpdateListener {

    CountdownCard baseCountDownCard;
    CardViewNative countDownCardView;

    View latestNewsView;
    TextView latestNewsViewTitle;
    TextView latestNewsViewDescription;
    TextView latestNewsViewPublishDate;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        countDownCardView = (CardViewNative) view.findViewById(R.id.countdowns);
        latestNewsView = view.findViewById(R.id.latestnews);
        nextEventCardView = (CardViewNative) view.findViewById(R.id.nextevent);

        ((MainActivity) getActivity()).getNewsFeedManager().addNewsFeedUpdateListener(this);
        ((MainActivity) getActivity()).getEventsManager().addEventsUpdateListener(this);
    }

    @Override
    public boolean onBackPressed() {
        if (!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onNewsFeedUpdated(final NewsFeed feed) {
        latestNewsViewTitle = (TextView) latestNewsView.findViewById(R.id.title);
        latestNewsViewPublishDate = (TextView) latestNewsView.findViewById(R.id.publishDate);
        latestNewsViewDescription = (TextView) latestNewsView.findViewById(R.id.description);

        if (feed.getFeedType() == NewsFeedType.ANNOUNCEMENTS) {

            latestNewsViewTitle.setText(feed.getArticlesList().get(0).getArticleTitle());
            latestNewsViewPublishDate.setText(feed.getArticlesList().get(0).getArticlePublishDate());
            latestNewsViewDescription.setText(feed.getArticlesList().get(0).getArticleDescription());
            latestNewsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WebViewFragment webViewFragment = new WebViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("URL", feed.getArticlesList().get(0).getArticleLink());
                    webViewFragment.setArguments(bundle);

                    getChildFragmentManager().beginTransaction()
                            .add(R.id.calendar_web_view_container, webViewFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack("main")
                            .commit();
                }
            });
        }
    }

    @Override
    public void onEventsUpdated(EventsManager eventsManager) {
        /////////////////// COUNTDOWN ////////////////////////

        //Create a Card
        baseCountDownCard = new CountdownCard(getActivity());
        //Set the card inner text
        CardHeader header = new CardHeader(getActivity());
        header.setTitle("Countdown");
        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);
        //Add Header to card
        baseCountDownCard.addCardHeader(header);

        if (getView() != null)
            getView().findViewById(R.id.countdowns_progress_wheel).setVisibility(View.GONE);

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

        baseCountDownCard.refresh(eventsManager, countDownCardView);

        baseCountDownCard.notifyDataSetChanged();

        //////////////// NEXT EVENT //////////////////////////

        getView().findViewById(R.id.nextevent_progress_wheel).setVisibility(View.GONE);

        //find if event if has already happened today
        Event trueNextEvent = eventsManager.getEventInfoHelper().getNextEvent(Calendar.getInstance(), true);

        nextEventCard = EventsManager.makeCalendarCard(this, trueNextEvent);
        CardHeader nextEventHeader = new CardHeader(getActivity());
        nextEventHeader.setTitle("Next Event");
        nextEventCard.addCardHeader(nextEventHeader);
        nextEventCardView.setCard(nextEventCard);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(baseCountDownCard != null)
            baseCountDownCard.cancel();
    }
}
