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
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoadingFragment loadingFragment = new LoadingFragment();
        fragmentTransaction.replace(R.id.fragmentSpace, loadingFragment);
        fragmentTransaction.commit();


        if (isNetworkConnected()) {
            NewsListFragment newsListFragment = new NewsListFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentSpace, newsListFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();

        } else {
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download news!");
            messageCardFragment.setArguments(args);

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentSpace, messageCardFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

}
