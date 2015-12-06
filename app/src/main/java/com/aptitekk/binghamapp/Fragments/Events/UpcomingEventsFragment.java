package com.aptitekk.binghamapp.Fragments.Events;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.Events.Event;
import com.aptitekk.binghamapp.Events.EventsManager;
import com.aptitekk.binghamapp.Events.EventsUpdateListener;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.prototypes.CardSection;
import it.gmariotti.cardslib.library.prototypes.SectionedCardAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class UpcomingEventsFragment extends Fragment implements MainActivity.BackButtonListener, EventsUpdateListener {

    private CardArrayAdapter cardArrayAdapter;
    private CardListView listView;

    private EventsManager eventsFeed;

    private boolean showABDays = true;

    public UpcomingEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cardlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getEventsManager().addEventsUpdateListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem calendarItem = menu.add("calendar");
        calendarItem.setIcon(R.drawable.ic_calendar_grey600_48dp);
        calendarItem.getIcon().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP); // Set color to white
        calendarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Dialog.Builder builder;
        if (item.getTitle().toString().equalsIgnoreCase("calendar")) {
            builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker) {
                @Override
                public void onPositiveActionClicked(DialogFragment fragment) {
                    DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                    Date date = dialog.getCalendar().getTime();
                    listView.smoothScrollToPosition(EventsManager.findPositionFromDate(eventsFeed.getEvents(), date));
                    MainActivity.logVerbose(eventsFeed.getEvents().get(EventsManager.findPositionFromDate(eventsFeed.getEvents(), date)).getTitle());
                    super.onPositiveActionClicked(fragment);
                }

                @Override
                public void onNegativeActionClicked(DialogFragment fragment) {
                    super.onNegativeActionClicked(fragment);
                }
            };
            builder.positiveAction("OK")
                    .negativeAction("CANCEL");
            DialogFragment fragment = DialogFragment.newInstance(builder);
            fragment.show(getChildFragmentManager(), null);
            return true;
        }
        return false;
    }

    public void populateCalendar(EventsManager eventsFeed) {
        this.eventsFeed = eventsFeed;

        //Hide progress wheel
        if(getView() != null)
            getView().findViewById(R.id.progress_wheel).setVisibility(View.GONE);

        //populate a list full of calendar card events
        ArrayList<Card> cards = new ArrayList<>();
        ArrayList<CardSection> sections = new ArrayList<>();
        LinkedHashMap<String, Integer> sectionQueue = new LinkedHashMap<>();

        int sectionOffsetIndex = 0;

        for (int i = 0; i < eventsFeed.getEvents().size(); i++) {

            if (this.eventsFeed.getEvents().get(i).getTitle().equals("A Day") || this.eventsFeed.getEvents().get(i).getTitle().equals("B Day")) {
                if (!(EventsManager.getEventsForDay(this.eventsFeed.getEvents(), this.eventsFeed.getEvents().get(i).getDate(), true).isEmpty())) {
                    sectionQueue.put(SimpleDateFormat.getDateInstance().format(this.eventsFeed.getEvents().get(i).getDate().getTime())
                            + " (" + this.eventsFeed.getEvents().get(i).getTitle() + ")", sectionOffsetIndex);
                }
            } else if (!(EventsManager.getEventsForDay(this.eventsFeed.getEvents(), this.eventsFeed.getEvents().get(i).getDate()).isEmpty())) {
                for (Event e : EventsManager.getEventsForDay(this.eventsFeed.getEvents(), this.eventsFeed.getEvents().get(i).getDate(), true)) {
                    MainActivity.logVerbose(e.getTitle());
                }
                try {
                    if (!EventsManager.isSameDay(this.eventsFeed.getEvents().get(i), this.eventsFeed.getEvents().get(i - 1))) {
                        sectionQueue.put(SimpleDateFormat.getDateInstance().format(this.eventsFeed.getEvents().get(i).getDate().getTime()), sectionOffsetIndex);
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                } // No events prior to

                // The following section generates a card for the current event that we are iterating over.
                cards.add(EventsManager.makeCalendarCard(this, this.eventsFeed.getEvents().get(i)));
                // END CARD GENERATION
                sectionOffsetIndex += 1;
            }
        }

        for (Map.Entry<String, Integer> entry : sectionQueue.entrySet())
            sections.add(new CardSection(entry.getValue(), entry.getKey()));

        this.cardArrayAdapter = new CardArrayAdapter(getActivity(), cards);

        CardSection[] pointer = new CardSection[sections.size()];
        SectionedCardAdapter mAdapter = new SectionedCardAdapter(getActivity(), cardArrayAdapter);
        mAdapter.setCardSections(sections.toArray(pointer));

        this.listView = (CardListView) getActivity().findViewById(R.id.cardListView);
        if (listView != null) {
            listView.setExternalAdapter(mAdapter, cardArrayAdapter);
        }


    }

    @Override
    public boolean onBackPressed() {
        if (!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onEventsUpdated(EventsManager eventsManager) {
        populateCalendar(eventsManager);
    }
}
