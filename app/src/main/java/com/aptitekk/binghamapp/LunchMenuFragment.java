package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LunchMenuFragment extends Fragment {

    boolean previouslyLoaded = false;

    public LunchMenuFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replaceable, container, false);

        //TODO: Notify if lunch menu can't be loaded
        //TODO: Automatically accept terms and go to bingham menus (somehow)
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", "http://jordandistrict.nutrislice.com/mobile/");
        bundle.putBoolean("useJavaScript", true);
        webViewFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentSpaceReplaceable, webViewFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("lunchMenu")
                .commit();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (previouslyLoaded) {
            getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().remove(this).commit();
        } else
            previouslyLoaded = true;
    }
}
