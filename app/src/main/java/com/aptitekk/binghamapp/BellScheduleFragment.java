package com.aptitekk.binghamapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BellScheduleFragment extends Fragment implements MainActivity.BackButtonListener {

    private ListView listView;
    private ClassPeriodAdapter listAdapter;

    public enum ScheduleType {
        REGULAR,
        ASSEMBLY;
    }

    public static BellScheduleFragment newInstance(BellSchedule schedule) {

        BellScheduleFragment fragment = new BellScheduleFragment();
        Bundle args = new Bundle();

        args.putString("title", schedule.getScheduleName());
        args.putStringArray("subjectNames", schedule.getSubjectNames());
        args.putStringArray("subjectStartTimes", schedule.getSubjectStartTimes());
        args.putStringArray("subjectEndTimes", schedule.getSubjectEndTimes());
        args.putIntArray("subjectLengths", schedule.getSubjectLengths());

        fragment.setArguments(args);
        return fragment;
    }

    public BellScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bell_schedule, container, false);

        Bundle args = getArguments();
        String title = args.getString("title");
        String[] subjectNames = args.getStringArray("subjectNames");
        String[] subjectStartTimes = args.getStringArray("subjectStartTimes");
        String[] subjectEndTimes = args.getStringArray("subjectEndTimes");
        int[] subjectLengths = args.getIntArray("subjectLengths");

        if (title != null) {
            ((TextView) view.findViewById(R.id.titleTextView)).setText(title);
        }

        if (subjectNames != null) {
            this.listView = (ListView) view.findViewById(R.id.listView);
            this.listAdapter = new ClassPeriodAdapter(getActivity(), subjectNames, subjectStartTimes, subjectEndTimes, subjectLengths);
            this.listView.setAdapter(listAdapter);
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).addBackButtonListener(this);
    }

    private class ClassPeriodAdapter extends ArrayAdapter {
        private final String[] subjectNames;
        private final String[] subjectStartTimes;
        private final String[] subjectEndTimes;
        private final int[] subjectLengths;
        private final Context context;

        public ClassPeriodAdapter(Context context, String[] subjectNames, String[] subjectStartTimes, String[] subjectEndTimes, int[] subjectLengths) {
            super(context, -1, subjectNames);

            this.context = context;
            this.subjectNames = subjectNames;
            this.subjectStartTimes = subjectStartTimes;
            this.subjectEndTimes = subjectEndTimes;
            this.subjectLengths = subjectLengths;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bell_schedule_list_item, parent, false);

            ((TextView) view.findViewById(R.id.subjectNameTextView)).setText(subjectNames[position]);
            ((TextView) view.findViewById(R.id.subjectStartTimeTextView)).setText("Start: " + subjectStartTimes[position]);
            ((TextView) view.findViewById(R.id.subjectEndTimeTextView)).setText("End: " + subjectEndTimes[position]);
            ((TextView) view.findViewById(R.id.subjectLengthTextView)).setText((subjectLengths[position] > 0) ? subjectLengths[position] + " mins" : "--");

            return view;
        }
    }

    @Override
    public boolean onBackPressed() {
        return !getParentFragment().getChildFragmentManager().popBackStackImmediate();
    }

}
