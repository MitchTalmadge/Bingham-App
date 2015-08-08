package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LunchMenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_replaceable, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //TODO: Notify if lunch menu can't be loaded
        //TODO: Automatically accept terms and go to bingham menus (somehow)
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", "http://jordandistrict.nutrislice.com/mobile/");
        bundle.putBoolean("useJavaScript", true);
        webViewFragment.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.fragmentSpace, webViewFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }
}
