package com.aptitekk.binghamapp;


import android.content.Context;
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
import com.aptitekk.binghamapp.rssnewsfeed.NewsArticle;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolNewsFragment extends Fragment implements MainActivity.FeedListener {

    private RecyclerView recyclerView;

    public SchoolNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        if (!isNetworkConnected()) {
            //TODO: Add cardview and just make it GONE, then change visibility here
            //Show No Internet Fragment
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download news!");
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
            if (MainActivity.newsFeed == null)
                ((MainActivity) getActivity()).addFeedListener(this);
            else
                populateNewsFeed(MainActivity.newsFeed);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void populateNewsFeed(RSSNewsFeed newsFeed) {

        //Hide progress wheel
        getView().findViewById(R.id.progress_wheel).setVisibility(View.GONE);

        if (newsFeed.getRssManager().getNewsArticles().isEmpty()) {
            //TODO: Add cardview and just make it GONE, then change visibility here
            //Show Website Down Fragment
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "Unable to retrieve news!");
            args.putString("description", "Could not download news! Is the website down?");
            messageCardFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentSpace, messageCardFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            //Show Recycler View
            recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(llm);

            RVAdapter adapter = new RVAdapter(newsFeed.getRssManager().getNewsArticles());
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        Log.i(MainActivity.LOG_NAME, "*Creating");
        menu.add("calendar").setIcon(R.drawable.calendar_icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public void onArticleClick(String URL) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", URL);
        webViewFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSpace, webViewFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onNewsFeedDownloaded(RSSNewsFeed newsFeed) {
        populateNewsFeed(newsFeed);
    }

    @Override
    public void onEventFeedDownloaded(CalendarDog eventFeed) {
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NewsArticleViewHolder> {

        public class NewsArticleViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView title;
            TextView description;
            TextView pubDate;
            String url = "";

            NewsArticleViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.cv);
                title = (TextView) itemView.findViewById(R.id.title);
                description = (TextView) itemView.findViewById(R.id.description);
                pubDate = (TextView) itemView.findViewById(R.id.pubDate);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!url.equals(""))
                            onArticleClick(url);
                    }
                });
            }
        }

        List<NewsArticle> articles;

        RVAdapter(List<NewsArticle> articles) {
            this.articles = articles;
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        @Override
        public NewsArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_article, viewGroup, false);
            return new NewsArticleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(NewsArticleViewHolder newsarticleViewHolder, int i) {
            newsarticleViewHolder.title.setText(articles.get(i).getTitle());
            newsarticleViewHolder.description.setText(articles.get(i).getDescription());
            newsarticleViewHolder.pubDate.setText(articles.get(i).getPubDate());
            newsarticleViewHolder.url = articles.get(i).getLink();
        }
    }
}
