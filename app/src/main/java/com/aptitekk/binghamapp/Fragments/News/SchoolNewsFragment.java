package com.aptitekk.binghamapp.Fragments.News;


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

import java.util.TreeMap;


public class SchoolNewsFragment extends Fragment implements SchoolNewsListFragment.ArticleClickedListener, MainActivity.BackButtonListener {

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;

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

        TreeMap<CharSequence, Fragment> fragmentTitleMap = new TreeMap<>();

        for (NewsFeed feed : ((MainActivity) getActivity()).getNewsFeedManager().getNewsFeeds()) {
            SchoolNewsListFragment fragment = new SchoolNewsListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("NewsFeedType", feed.getFeedType().ordinal());
            fragment.setArguments(bundle);
            fragment.setArticleClickedListener(this);

            fragmentTitleMap.put(feed.getFeedName(), fragment);
        }

        adapter = new ViewPagerAdapter(getFragmentManager(), fragmentTitleMap);

        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getActivity(), R.color.primary_light);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
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
    }

    @Override
    public boolean onBackPressed() {
        if (!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        return false;
    }
}
