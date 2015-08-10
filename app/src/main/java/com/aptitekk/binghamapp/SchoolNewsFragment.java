package com.aptitekk.binghamapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.concurrent.Callable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchoolNewsFragment extends Fragment implements NewsListFragment.NewsListListener {

    public static RSSNewsFeed feed;

    public SchoolNewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_replaceable, container, false);

        //Show Loading Fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        LoadingFragment loadingFragment = new LoadingFragment();
        fragmentTransaction.add(R.id.fragmentSpace, loadingFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        if (isNetworkConnected()) {
            populateNewsFeed();
        } else {
            //Show No Internet Fragment
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download news!");
            messageCardFragment.setArguments(args);

            getFragmentManager().popBackStack(); //Remove Loading Fragment
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentSpace, messageCardFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        return view;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void populateNewsFeed() {
        final SchoolNewsFragment currentFragment = this;
        final Callable<Void> refresh = new Callable<Void>() {
            public Void call() {

                if (feed.getRssManager().getNewsArticles().isEmpty()) {
                    //Show Website Down Fragment
                    MessageCardFragment messageCardFragment = new MessageCardFragment();
                    Bundle args = new Bundle();
                    args.putString("title", "Unable to retrieve news!");
                    args.putString("description", "Could not download news! Is the website down?");
                    messageCardFragment.setArguments(args);

                    getFragmentManager().popBackStack(); //Remove Loading Fragment
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.fragmentSpace, messageCardFragment);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return null;
                } else {
                    //Show News List Fragment
                    NewsListFragment newsListFragment = new NewsListFragment();
                    newsListFragment.addNewsListListener(currentFragment);

                    getFragmentManager().popBackStack(); //Remove Loading Fragment
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.fragmentSpace, newsListFragment);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return null;
                }
            }
        };

        feed = new RSSNewsFeed(refresh);
    }

    @Override
    public void articleClicked(String URL) {
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
}
