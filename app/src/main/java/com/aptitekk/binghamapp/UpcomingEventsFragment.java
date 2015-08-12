package com.aptitekk.binghamapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;

import java.util.concurrent.Callable;


public class UpcomingEventsFragment extends Fragment {

    public static CalendarDog feed;

    public UpcomingEventsFragment() {
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
            populateCalendar();
        } else {
            //Show No Internet Fragment
            MessageCardFragment messageCardFragment = new MessageCardFragment();
            Bundle args = new Bundle();
            args.putString("title", "No Internet Connection!");
            args.putString("description", "Could not download events!");
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

    @Override
    public void onStart() {
        super.onStart();


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void populateCalendar() {
        final Callable<Void> refresh = new Callable<Void>() {
            public Void call() {
                //Show Calendar List Fragment
                CalendarListFragment newsListFragment = new CalendarListFragment();

                getFragmentManager().popBackStack(); //Remove Loading Fragment
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragmentSpace, newsListFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return null;
            }
        };
        feed = new CalendarDog(CalendarDog.BINGHAM_GOOGLE_CALENDAR,
                refresh,
                CalendarDog.FetchType.ICAL);
    }
}
