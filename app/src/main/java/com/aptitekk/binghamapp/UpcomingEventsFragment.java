package com.aptitekk.binghamapp;


import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class UpcomingEventsFragment extends Fragment implements MainActivity.FeedListener {

    private RecyclerView recyclerView;

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
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        /*MaterialCalendarView calendarView = (MaterialCalendarView) mainView.findViewById(R.id.calendarView);

        calendarView.setOnDateChangedListener(this);
        calendarView.setShowOtherDates(true);

        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        calendarView.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
        calendarView.setMaximumDate(calendar.getTime());*/

        if (!isNetworkConnected()) {
            //TODO: Add cardview and just make it GONE, then change visibility here
            //Show No Internet Fragment
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download events!");
            messageCardFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentSpace, messageCardFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isNetworkConnected()) {
            if (MainActivity.eventsFeed == null)
                ((MainActivity) getActivity()).addFeedListener(this);
            else
                populateCalendar(MainActivity.eventsFeed);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add("calendar").setIcon(R.drawable.calendar_icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void populateCalendar(CalendarDog eventsFeed) {

        //Hide progress wheel
        getView().findViewById(R.id.progress_wheel).setVisibility(View.GONE);

        //Show Recycler View
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(eventsFeed.getEvents());
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNewsFeedDownloaded(RSSNewsFeed newsFeed) {
    }

    @Override
    public void onEventFeedDownloaded(CalendarDog eventFeed) {
        populateCalendar(eventFeed);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CalendarEventViewHolder> {

        SimpleDateFormat headerFormat = new SimpleDateFormat("EEE hh:mmaa");
        SimpleDateFormat footerFormat = new SimpleDateFormat("hh:mmaa zzz");

        public class CalendarEventViewHolder extends RecyclerView.ViewHolder {
            TextView eventDate;
            CardView card;
            TextView title;
            TextView duration;
            TextView location;
            String url = "";

            View itemView;

            CalendarEventViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                eventDate = (TextView) itemView.findViewById(R.id.eventDate);
                card = (CardView) itemView.findViewById(R.id.calendarCard);
                title = (TextView) itemView.findViewById(R.id.title);
                duration = (TextView) itemView.findViewById(R.id.duration);
                location = (TextView) itemView.findViewById(R.id.location);
                /*itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!url.equals(""))
                            onArticleClick(url);
                    }
                });
                ;*/
            }

        }

        List<CalendarEvent> events;

        RVAdapter(List<CalendarEvent> events) {

            this.events = CalendarEvent.sort(events);
            for (int i = 0; i < events.size(); i++) {
                try {
                    events.get(i - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                if (events.get(i - 1).getDate().get(Calendar.YEAR) == events.get(i).getDate().get(Calendar.YEAR) &&
                        events.get(i - 1).getDate().get(Calendar.MONTH) == events.get(i).getDate().get(Calendar.MONTH) &&
                        events.get(i - 1).getDate().get(Calendar.DAY_OF_MONTH) == events.get(i).getDate().get(Calendar.DAY_OF_MONTH)) {
                    events.get(i).setDateLabelVisible(false);
                }
            }
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        @Override
        public CalendarEventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_event, viewGroup, false);
            return new CalendarEventViewHolder(v);
        }

        private String formatDate(CalendarEvent event) {
            return (headerFormat.format(event.getDate().getTime()) + " - " + footerFormat.format(event.getEndTime().getTime())).replace("PM", "pm").replace("AM", "am");
        }

        @Override
        public void onBindViewHolder(CalendarEventViewHolder calendareventViewHolder, int i) {
            if (!events.get(i).isDateLabelVisible()) {
                calendareventViewHolder.itemView.findViewById(R.id.eventDate).setVisibility(View.GONE);
            } else {
                calendareventViewHolder.itemView.findViewById(R.id.eventDate).setVisibility(View.VISIBLE);
                try {
                    calendareventViewHolder.eventDate.setText(SimpleDateFormat.getDateInstance().format(events.get(i).getDate().getTime()));
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            calendareventViewHolder.title.setText(events.get(i).getTitle());
            if (events.get(i).getTitle().equals("A Day") || events.get(i).getTitle().equals("B Day")) {
                calendareventViewHolder.duration.setVisibility(View.GONE);
                calendareventViewHolder.location.setVisibility(View.GONE);
                calendareventViewHolder.url = "";
                return;
            }
            if (events.get(i).getTitle().contains("Game") || events.get(i).getTitle().contains("Football")) {
                calendareventViewHolder.title.setBackgroundColor(getResources().getColor(R.color.primary));
                calendareventViewHolder.title.setTextColor(Color.WHITE);
            } else if(events.get(i).getTitle().contains("Dance")) {
                calendareventViewHolder.title.setBackgroundColor(getResources().getColor(R.color.primary_text));
                calendareventViewHolder.title.setTextColor(Color.WHITE);
            } else {
                calendareventViewHolder.title.setBackgroundColor(Color.WHITE);
                calendareventViewHolder.title.setTextColor(Color.BLACK);
            }
            calendareventViewHolder.duration.setVisibility(View.VISIBLE);
            calendareventViewHolder.location.setVisibility(View.VISIBLE);
            calendareventViewHolder.duration.setText(formatDate(events.get(i)));
            calendareventViewHolder.location.setText(events.get(i).getLocation());
            calendareventViewHolder.url = events.get(i).getLink();
        }

    }
}
