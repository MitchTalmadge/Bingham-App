package com.aptitekk.binghamapp.Fragments.LunchMenu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aptitekk.binghamapp.Fragments.HelperFragments.WebViewFragment;
import com.aptitekk.binghamapp.R;

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

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentSpaceReplaceable, webViewFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        return view;
    }
}
