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

import com.aptitekk.binghamapp.rssnewsfeed.NewsArticle;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsListFragment extends Fragment {

    private RSSNewsFeed feed;

    public NewsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        final RecyclerView rv = (RecyclerView) getView().findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        final Callable<Void> refresh = new Callable<Void>() {
            public Void call() {
                RVAdapter adapter = new RVAdapter(feed.getRssManager().getNewsArticles());
                rv.setAdapter(adapter);
                return null;
            }
        };

        feed = new RSSNewsFeed(refresh);
    }

    public void onArticleClick(String URL) {

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
