package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CalendarListFragment extends Fragment implements MainActivity.BackButtonListener {


    public CalendarListFragment() {
        // Required empty public constructor
    }

    RecyclerView rv;
    //RVAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_recycler, container, false);
        /*MaterialCalendarView calendarView = (MaterialCalendarView) mainView.findViewById(R.id.calendarView);

        calendarView.setOnDateChangedListener(this);
        calendarView.setShowOtherDates(true);

        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        calendarView.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
        calendarView.setMaximumDate(calendar.getTime());*/

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle args) {
        super.onActivityCreated(args);
        rv = (RecyclerView) getView().findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(UpcomingEventsFragment.feed.getEvents());
        rv.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MainActivity) getActivity()).setBackButtonListener(this);
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return true;
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
            for(int i=0; i<events.size(); i++) {
                try {
                    events.get(i-1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                if (    events.get(i - 1).getDate().get(Calendar.YEAR) == events.get(i).getDate().get(Calendar.YEAR) &&
                        events.get(i - 1).getDate().get(Calendar.MONTH)== events.get(i).getDate().get(Calendar.MONTH) &&
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
            if(!events.get(i).isDateLabelVisible()) {
                calendareventViewHolder.itemView.findViewById(R.id.eventDate).setVisibility(View.GONE);
            } else {
                calendareventViewHolder.itemView.findViewById(R.id.eventDate).setVisibility(View.VISIBLE);
                try {
                    calendareventViewHolder.eventDate.setText(SimpleDateFormat.getDateInstance().format(events.get(i).getDate().getTime()));
                } catch (NullPointerException e) {
                    ;//pass
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            calendareventViewHolder.title.setText(events.get(i).getTitle());
            if(events.get(i).getTitle().equals("A Day")|| events.get(i).getTitle().equals("B Day")) {
                calendareventViewHolder.duration.setVisibility(View.GONE);
                calendareventViewHolder.location.setVisibility(View.GONE);
                calendareventViewHolder.url = "";
                return;
            }
            calendareventViewHolder.duration.setVisibility(View.VISIBLE);
            calendareventViewHolder.location.setVisibility(View.VISIBLE);
            calendareventViewHolder.duration.setText(formatDate(events.get(i)));
            calendareventViewHolder.location.setText(events.get(i).getLocation());
            calendareventViewHolder.url = events.get(i).getLink();
        }

    }
}
