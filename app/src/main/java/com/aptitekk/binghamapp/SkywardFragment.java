package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SkywardFragment extends Fragment {

    public SkywardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_skyward, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        /*//TODO: Notify if skyward can't be loaded
        //TODO: Automatically log user in and skip all the bull poop
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", "https://skystu.jordan.k12.ut.us/scripts/wsisa.dll/WService=wsEAplus/mobilelogin.w");
        //bundle.putString("POSTData", "login="+username+"&password="+password+"&autologin=true&hideui=true&mobiledevice=android&version=1.20");
        bundle.putBoolean("useJavaScript", true);
        webViewFragment.setArguments(bundle);

        getFragmentManager().beginTransaction().add(R.id.fragmentSpace, webViewFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();*/
    }


}
