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

public class CalendarListFragment extends Fragment implements OnDateChangedListener {


    public CalendarListFragment() {
        // Required empty public constructor
    }

    RecyclerView rv;
    //RVAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_calendar, container, false);
        MaterialCalendarView calendarView = (MaterialCalendarView) mainView.findViewById(R.id.calendarView);

        calendarView.setOnDateChangedListener(this);
        calendarView.setShowOtherDates(true);

        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        calendarView.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
        calendarView.setMaximumDate(calendar.getTime());

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle args) {
        super.onActivityCreated(args);
        rv = (RecyclerView) getView().findViewById(R.id.calendarRecyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(UpcomingEventsFragment.feed.getEvents());
        rv.setAdapter(adapter);

        ((MaterialCalendarView) getView().findViewById(R.id.calendarView)).setSelectedDate(Calendar.getInstance());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
        RVAdapter adapter = new RVAdapter(CalendarEvent.sort(
                CalendarEvent.matchesDay(
                        UpcomingEventsFragment.feed.getEvents(),
                        calendarDay.getCalendar())));
        rv.swapAdapter(adapter, false);
        ((TextView) getView().findViewById(R.id.selectedDate)).setText(SimpleDateFormat.getDateInstance().format(calendarDay.getDate()));
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CalendarEventViewHolder> {

        SimpleDateFormat headerFormat = new SimpleDateFormat("EEE hh:mmaa");
        SimpleDateFormat footerFormat = new SimpleDateFormat("hh:mmaa zzz");

        public class CalendarEventViewHolder extends RecyclerView.ViewHolder {
            CardView card;
            TextView title;
            TextView duration;
            TextView location;
            String url = "";

            CalendarEventViewHolder(View itemView) {
                super(itemView);
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
            this.events = events;
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
            return (headerFormat.format(event.getDate().getTime()) + " - " + footerFormat.format(event.getEndTime().getTime())).replace("PM","pm").replace("AM", "am");
        }

        @Override
        public void onBindViewHolder(CalendarEventViewHolder calendareventViewHolder, int i) {
            calendareventViewHolder.title.setText(events.get(i).getTitle());
            calendareventViewHolder.duration.setText(formatDate(events.get(i)));
            calendareventViewHolder.location.setText(events.get(i).getLocation());
            calendareventViewHolder.url = events.get(i).getLink();
        }


    }
}
