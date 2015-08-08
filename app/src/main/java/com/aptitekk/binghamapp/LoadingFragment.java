package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoadingFragment extends Fragment {


    private boolean mAlreadyLoaded;

    public LoadingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;
            // Do this code only first time, not after rotation or reuse fragment from backstack
        }
        else if(mAlreadyLoaded)
        {
            view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            getFragmentManager().popBackStack();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(this);
            fragmentTransaction.commit();
        }
        // Inflate the layout for this fragment
        return view;
    }

}
