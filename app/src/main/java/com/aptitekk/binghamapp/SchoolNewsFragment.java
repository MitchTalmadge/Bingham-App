package com.aptitekk.binghamapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.rssnewsfeed.NewsArticle;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolNewsFragment extends Fragment {

    public RSSNewsFeed feed;

    public SchoolNewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_school_news, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment loadingFragment = new LoadingFragment();
        fragmentTransaction.replace(R.id.fragmentSpace, loadingFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();

        final RecyclerView rv = (RecyclerView) getView().findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        if (isNetworkConnected()) {
            Fragment newsListFragment = new NewsListFragment();
            fragmentTransaction.replace(R.id.fragmentSpace, newsListFragment);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();

            final Callable<Void> refresh = new Callable<Void>() {
                public Void call() {
                    RVAdapter adapter = new RVAdapter(feed.getRssManager().getNewsArticles());
                    rv.setAdapter(adapter);
                    View fragContainView = getView().findViewById(R.id.fragment_container);
                    ViewGroup parentView = (ViewGroup) fragContainView.getParent();
                    parentView.removeView(fragContainView);
                    return null;
                }
            };

            feed = new RSSNewsFeed(refresh);

            /*frag = new SingleNotifCard();
            args = new Bundle();
            args.putString("title", "Bingham High School");
            args.putString("description", "News and Announcments");
            frag.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container, frag);*/

        } else {
            SingleNotifCard failed = new SingleNotifCard();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download news!");
            failed.setArguments(args);
            fragmentTransaction.replace(R.id.fragmentSpace, failed);
        }
        fragmentTransaction.commit();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void onArticleClick(String URL) {
        //FIXME
        /*Intent i = new Intent(this, WebViewFragment.class);
        i.putExtra("URL", URL);
        startActivity(i);*/
    }

    public static class SingleNotifCard extends Fragment {

        View view_a;

        public SingleNotifCard() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            view_a = inflater.inflate(R.layout.news_article, container, false);
            try {
                TextView titleView = (TextView) view_a.findViewById(R.id.title);
                titleView.setText(getArguments().getString("title"));
                TextView descriptionView = (TextView) view_a.findViewById(R.id.description);
                descriptionView.setText(getArguments().getString("description"));
            } catch (NullPointerException e) {
                TextView titleView = (TextView) view_a.findViewById(R.id.title);
                titleView.setText("Please wait...");
                TextView descriptionView = (TextView) view_a.findViewById(R.id.description);
                descriptionView.setText("Downloading news...");
            }

            //view_a = ll;

            return view_a;
        }
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
                ;
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
            NewsArticleViewHolder navh = new NewsArticleViewHolder(v);
            return navh;
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
