package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BellSchedulesListFragment extends Fragment implements MainActivity.BackButtonListener {


    private ListView regularScheduleList;
    private ListView assemblyScheduleList;

    private ArrayAdapter<String> regularScheduleAdapter;
    private ArrayAdapter<String> assemblyScheduleAdapter;

    private int[] regularScheduleListPositions;
    private int[] assemblyScheduleListPositions;

    private ArrayList<BellSchedulesListListener> listeners = new ArrayList<>();

    public BellSchedulesListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bell_schedules, container, false);

        ((MainActivity) getActivity()).setBackButtonListener(this);

        this.regularScheduleList = (ListView) view.findViewById(R.id.regularSchedulesListView);
        this.assemblyScheduleList = (ListView) view.findViewById(R.id.assemblySchedulesListView);

        // Set up Regular Bell Schedule List
        final String[] regularBellScheduleStrings = getResources().getStringArray(R.array.regularBellSchedules);
        this.regularScheduleListPositions = new int[regularBellScheduleStrings.length];

        for (int i = 0; i < regularBellScheduleStrings.length; i++) {
            String[] split = regularBellScheduleStrings[i].split("_");
            regularScheduleListPositions[i] = Integer.parseInt(split[0]);
            regularBellScheduleStrings[i] = split[1];
        }

        this.regularScheduleAdapter = new ArrayAdapter<String>(getActivity(), R.layout.bell_schedules_list_item, regularBellScheduleStrings);
        regularScheduleList.setAdapter(regularScheduleAdapter);
        regularScheduleList.setDivider(null);
        regularScheduleList.setDividerHeight(0);

        // Set up Assembly Bell Schedule List
        final String[] assemblyBellScheduleStrings = getResources().getStringArray(R.array.assemblyBellSchedules);
        this.assemblyScheduleListPositions = new int[assemblyBellScheduleStrings.length];

        for (int i = 0; i < assemblyBellScheduleStrings.length; i++) {
            String[] split = assemblyBellScheduleStrings[i].split("_");
            assemblyScheduleListPositions[i] = Integer.parseInt(split[0]);
            assemblyBellScheduleStrings[i] = split[1];
        }

        this.assemblyScheduleAdapter = new ArrayAdapter<String>(getActivity(), R.layout.bell_schedules_list_item, assemblyBellScheduleStrings);
        assemblyScheduleList.setAdapter(assemblyScheduleAdapter);
        assemblyScheduleList.setDivider(null);
        assemblyScheduleList.setDividerHeight(0);

        // Add Listeners
        regularScheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] scheduleStrings = null;
                switch (regularScheduleListPositions[position]) {
                    case 0:
                        scheduleStrings = getResources().getStringArray(R.array.regularBellSchedule0);
                        break;
                    case 1:
                        scheduleStrings = getResources().getStringArray(R.array.regularBellSchedule1);
                        break;
                    default:
                        break;
                }

                BellSchedule schedule = new BellSchedule(regularBellScheduleStrings[position], scheduleStrings);

                for(BellSchedulesListListener listener : listeners)
                {
                    listener.openSchedule(BellScheduleFragment.newInstance(schedule));
                }
            }
        });

        assemblyScheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] scheduleStrings = null;
                switch(assemblyScheduleListPositions[position])
                {
                    case 0:
                        scheduleStrings = getResources().getStringArray(R.array.assemblyBellSchedule0);
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }

                BellSchedule schedule = new BellSchedule(assemblyBellScheduleStrings[position], scheduleStrings);

                for(BellSchedulesListListener listener : listeners)
                {
                    listener.openSchedule(BellScheduleFragment.newInstance(schedule));
                }
            }
        });

        return view;
    }

    public void addBellSchedulesListListener(BellSchedulesListListener listener)
    {
        this.listeners.add(listener);
    }

    @Override
    public boolean onBackPressed() {
        ((MainActivity) getActivity()).popToMainMenu();
        return false;
    }

    public interface BellSchedulesListListener
    {
        void openSchedule(Fragment scheduleToOpen);
    }

}
