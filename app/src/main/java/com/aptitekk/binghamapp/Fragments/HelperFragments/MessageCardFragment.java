package com.aptitekk.binghamapp.Fragments.HelperFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aptitekk.binghamapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageCardFragment extends Fragment {


    public MessageCardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_card, container, false);

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(getArguments().getString("title"));

        TextView descriptionView = (TextView) view.findViewById(R.id.description);
        descriptionView.setText(getArguments().getString("description"));

        return view;
    }


}
