package com.aptitekk.binghamapp.Fragments.News;

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

import com.aptitekk.binghamapp.Fragments.HelperFragments.MessageCardFragment;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.News.NewsArticle;
import com.aptitekk.binghamapp.News.NewsFeed;
import com.aptitekk.binghamapp.News.NewsFeedType;
import com.aptitekk.binghamapp.News.NewsFeedUpdateListener;
import com.aptitekk.binghamapp.R;

import java.util.List;

public class SchoolNewsListFragment extends Fragment implements NewsFeedUpdateListener {

    private RecyclerView recyclerView;
    private ArticleClickedListener articleClickedListener;
    private NewsFeedType newsFeedType;

    public SchoolNewsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.newsFeedType = NewsFeedType.values()[getArguments().getInt("NewsFeedType")];
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

        ((MainActivity) getActivity()).getNewsFeedManager().addNewsFeedUpdateListener(this);
    }

    @Override
    public void onNewsFeedUpdated(NewsFeed feed) {
        if (feed.getFeedType() == newsFeedType)
            populateNewsFeed(feed);
    }

    public void populateNewsFeed(NewsFeed newsFeed) {
        //Hide progress wheel
        if (getView() != null)
            getView().findViewById(R.id.progress_wheel).setVisibility(View.GONE);

        if (newsFeed.getArticlesList().isEmpty()) {
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
            if (getView() != null)
                recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            RVAdapter adapter = new RVAdapter(newsFeed.getArticlesList());
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
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
        if (articleClickedListener != null)
            articleClickedListener.onArticleClicked(URL);
    }

    public void setArticleClickedListener(ArticleClickedListener articleClickedListener) {
        this.articleClickedListener = articleClickedListener;
    }

    protected interface ArticleClickedListener {

        void onArticleClicked(String URL);

    }
}
