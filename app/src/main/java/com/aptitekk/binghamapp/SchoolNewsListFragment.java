package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssnewsfeed.NewsArticle;
import com.aptitekk.binghamapp.rssnewsfeed.NewsFeed;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeedManager;
import com.aptitekk.binghamapp.rssnewsfeed.newsFeeds.Announcements;

import java.util.List;

public class SchoolNewsListFragment extends Fragment implements MainActivity.FeedListener {

    private RecyclerView recyclerView;

    private ArticleListener articleListener;

    private Class<? extends NewsFeed> newsFeedClass = Announcements.class;

    public SchoolNewsListFragment() {
        // Required empty public constructor
    }

    public void setFeed(Class<? extends NewsFeed> newsFeedClass) {
        this.newsFeedClass = newsFeedClass;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).addFeedListener(this);
    }

    @Override
    public void onNewsFeedDownloaded(NewsFeed newsFeed) {
        populateNewsFeed(newsFeed);
    }

    @Override
    public void onEventsFeedDownloaded(CalendarDog eventFeed) {
    }

    public void populateNewsFeed(NewsFeed newsFeed) {

        if(newsFeedClass.isInstance(newsFeed)) {

            //Hide progress wheel
            getView().findViewById(R.id.progress_wheel).setVisibility(View.GONE);

            if (newsFeed.getArticles().isEmpty()) {
                //Show Website Down Fragment
                MessageCardFragment messageCardFragment = new MessageCardFragment();
                Bundle args = new Bundle();
                args.putString("title", "Unable to retrieve news!");
                args.putString("description", "Could not download news! Is the website down?");
                messageCardFragment.setArguments(args);

                getChildFragmentManager().beginTransaction()
                        .add(R.id.fragmentSpaceRecycler, messageCardFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("messageCard")
                        .commit();
            } else {
                //Show Recycler View
                recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(llm);

                RVAdapter adapter = new RVAdapter(newsFeed.getArticles());
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            }
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

    public void onArticleClick(String URL) {
        if (articleListener != null)
            articleListener.onArticleClicked(URL);
    }

    public void setArticleListener(ArticleListener articleListener) {
        this.articleListener = articleListener;
    }

    protected interface ArticleListener {

        void onArticleClicked(String URL);

    }
}
