package com.aptitekk.binghamapp.Fragments.News;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.Fragments.HelperFragments.WebViewFragment;
import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.News.NewsFeed;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.Views.SlidingTab.SlidingTabLayout;
import com.aptitekk.binghamapp.Views.SlidingTab.ViewPagerAdapter;

import java.util.LinkedHashMap;


public class SchoolNewsFragment extends Fragment implements SchoolNewsListFragment.ArticleClickedListener, MainActivity.BackButtonListener {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    SlidingTabLayout slidingTabLayout;

    public SchoolNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).addBackButtonListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinkedHashMap<CharSequence, Fragment> fragmentTitleMap = new LinkedHashMap<>();

        for (NewsFeed feed : ((MainActivity) getActivity()).getNewsFeedManager().getNewsFeeds()) {
            SchoolNewsListFragment fragment = new SchoolNewsListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("NewsFeedType", feed.getFeedType().ordinal());
            fragment.setArguments(bundle);
            fragment.setArticleClickedListener(this);

            fragmentTitleMap.put(feed.getFeedName(), fragment);
        }

        this.viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), fragmentTitleMap);

        this.viewPager = (ViewPager) view.findViewById(R.id.newsViewPager);
        viewPager.setAdapter(this.viewPagerAdapter);

        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.newsSlidingTabs);
        slidingTabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getActivity(), Color.WHITE);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    public void onArticleClicked(String URL) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", URL);
        webViewFragment.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentSpaceReplaceable, webViewFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("newsArticle")
                .commit();

        setTabsVisible(false);
    }

    @Override
    public boolean onBackPressed() {
        if (!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        setTabsVisible(true);
        return false;
    }

    private void setTabsVisible(boolean visible)
    {
        slidingTabLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
