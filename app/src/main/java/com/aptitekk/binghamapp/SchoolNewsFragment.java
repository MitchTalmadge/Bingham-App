package com.aptitekk.binghamapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.View.SlidingTabLayout;
import com.aptitekk.binghamapp.rssnewsfeed.NewsFeed;
import com.aptitekk.binghamapp.rssnewsfeed.newsFeeds.Announcements;
import com.aptitekk.binghamapp.rssnewsfeed.newsFeeds.Prospector;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolNewsFragment extends Fragment implements SchoolNewsListFragment.ArticleListener, MainActivity.BackButtonListener {

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

        CharSequence[] titles = new CharSequence[MainActivity.newsFeeds.getNewsFeeds().length];
        for(int i = 0; i<MainActivity.newsFeeds.getNewsFeeds().length; i++) {
            titles[i] = MainActivity.newsFeeds.getNewsFeeds()[i].getName();
        }

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getFragmentManager(), titles);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.primary_light);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
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
        if(!getChildFragmentManager().popBackStackImmediate())
            getFragmentManager().popBackStack();
        return false;
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
        int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


        // Build a Constructor and assign the passed Values to appropriate values in the class
        public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[]) {
            super(fm);

            this.Titles = mTitles;
            this.NumbOfTabs = mTitles.length;

        }

        //This method return the fragment for the every position in the View Pager
        @Override
        public Fragment getItem(int position) {

            if(position == 0) // if the position is 0 we are returning the First tab
            {
                SchoolNewsListFragment announcements = new SchoolNewsListFragment();
                announcements.setFeed(Announcements.class);
                return announcements;
            }
            else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
            {
                SchoolNewsListFragment prospector = new SchoolNewsListFragment();
                prospector.setFeed(Prospector.class);
                return prospector;
            }

        }

        // This method return the titles for the Tabs in the Tab Strip

        @Override
        public CharSequence getPageTitle(int position) {
            return Titles[position];
        }

        // This method return the Number of tabs for the tabs Strip

        @Override
        public int getCount() {
            return NumbOfTabs;
        }
    }
}
