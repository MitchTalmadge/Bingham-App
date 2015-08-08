package com.aptitekk.binghamapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


public class WebViewFragment extends Fragment {

    private boolean mAlreadyLoaded;

    public WebViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;
            // Do this code only first time, not after rotation or reuse fragment from backstack
        }
        else if(mAlreadyLoaded)
        {
            view.findViewById(R.id.webView).setVisibility(View.INVISIBLE);
            getFragmentManager().popBackStack();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(this);
            fragmentTransaction.commit();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String URL = getArguments().getString("URL");
        boolean useJavaScript = getArguments().getBoolean("useJavaScript");

        WebView browser = (WebView) getView().findViewById(R.id.webView);
        if(useJavaScript)
            browser.getSettings().setJavaScriptEnabled(true);
        browser.loadUrl(URL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
