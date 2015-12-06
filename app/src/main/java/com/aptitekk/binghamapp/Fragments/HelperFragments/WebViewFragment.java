package com.aptitekk.binghamapp.Fragments.HelperFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;


public class WebViewFragment extends Fragment implements MainActivity.BackButtonListener {

    private boolean mAlreadyLoaded;
    private WebView webView;

    public WebViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        ((MainActivity) getActivity()).addBackButtonListener(this);

        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;
            // Do this code only first time, not after rotation or reuse fragment from backstack
        } else if (mAlreadyLoaded) {
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
        String POSTData = getArguments().getString("POSTData");

        if (getView() != null) {
            this.webView = (WebView) getView().findViewById(R.id.webView);
            webView.setWebViewClient(new CustomWebViewClient());
            if (useJavaScript)
                webView.getSettings().setJavaScriptEnabled(true);
            if (POSTData != null)
                webView.postUrl(URL, POSTData.getBytes());
            else
                webView.loadUrl(URL);
        }
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

    @Override
    public boolean onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
            return false;
        }
        return true;
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //TODO: Show progress wheel while loading and dispose of it here
            super.onPageFinished(view, url);
        }
    }

}