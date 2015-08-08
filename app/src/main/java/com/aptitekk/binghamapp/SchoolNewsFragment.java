package com.aptitekk.binghamapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.RSSNewsFeed.RSSNewsFeed;

import java.util.concurrent.Callable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolNewsFragment extends Fragment {

    public static RSSNewsFeed feed;

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

        //Show Loading Fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        LoadingFragment loadingFragment = new LoadingFragment();
        fragmentTransaction.replace(R.id.fragmentSpace, loadingFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

        if (isNetworkConnected()) {

            populateNewsFeed();

        } else {
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download news!");
            messageCardFragment.setArguments(args);

            //Show No Internet Fragment
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentSpace, messageCardFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack("NoInternet");
            fragmentTransaction.commit();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void populateNewsFeed() {
        final Callable<Void> refresh = new Callable<Void>() {
            public Void call() {

                //Show News List Fragment
                NewsListFragment newsListFragment = new NewsListFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentSpace, newsListFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack("NewsList");
                fragmentTransaction.commit();
                return null;
            }
        };

        feed = new RSSNewsFeed(refresh);
    }

}
