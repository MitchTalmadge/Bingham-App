package com.aptitekk.binghamapp.BellSchedulesFragmentClasses;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.R;

public class BellSchedulesFragment extends Fragment implements BellSchedulesListFragment.BellSchedulesListListener {

    public BellSchedulesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add BellSchedulesListFragment to fragmentSpaceReplaceable
            BellSchedulesListFragment bellSchedulesListFragment = new BellSchedulesListFragment();
            bellSchedulesListFragment.addBellSchedulesListListener(this);

            getChildFragmentManager().beginTransaction()
                    .add(R.id.fragmentSpaceReplaceable, bellSchedulesListFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_replaceable, container, false);
    }

    @Override
    public void openSchedule(Fragment scheduleToOpen) {

        // Replace the lists with the bell schedule, and add to backstack so that the back button will remove it.
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentSpaceReplaceable, scheduleToOpen)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("openSchedule")
                .commit();
    }
}
