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

import java.util.List;

/**
 * Created by kevint on 8/8/2015.
 */
public class CalendarListFragment extends Fragment {

    public CalendarListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upcoming_events, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        final RecyclerView rv = (RecyclerView) getView().findViewById(R.id.CalendarRV);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(UpcomingEventsFragment.feed.getEvents());
        rv.setAdapter(adapter);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CalendarEventViewHolder> {

        public class CalendarEventViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView title;
            TextView description;
            TextView pubDate;
            String url = "";

           CalendarEventViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.cv);
                title = (TextView) itemView.findViewById(R.id.title);
                description = (TextView) itemView.findViewById(R.id.description);
                pubDate = (TextView) itemView.findViewById(R.id.pubDate);
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
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_article, viewGroup, false);
            CalendarEventViewHolder navh = new CalendarEventViewHolder(v);
            return navh;
        }

        @Override
        public void onBindViewHolder(CalendarEventViewHolder calendareventViewHolder, int i) {
            calendareventViewHolder.title.setText(events.get(i).getTitle());
            calendareventViewHolder.description.setText(events.get(i).getDate().toString());
            calendareventViewHolder.pubDate.setText(events.get(i).getLocation());
            calendareventViewHolder.url = events.get(i).getLink();
        }


    }
}
