package com.aptitekk.binghamapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setUp(DrawerLayout drawerLayout, Toolbar toolbar) {

        switchFragment(0);

        //Set up drawer list
        this.drawerList = (ListView) getActivity().findViewById(R.id.listView);
        this.drawerList.setDivider(null);
        this.drawerList.setDividerHeight(0);
        String[] drawerListStrings = getResources().getStringArray(R.array.drawer_list_strings);
        final int[] drawerListPositions = new int[drawerListStrings.length];

        for (int i = 0; i < drawerListStrings.length; i++) {
            String[] split = drawerListStrings[i].split("_");
            drawerListPositions[i] = Integer.parseInt(split[0]);
            drawerListStrings[i] = split[1];
        }

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<>(getActivity(),
                R.layout.navigation_drawer_list_item, drawerListStrings));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listItem = drawerList.getItemAtPosition(position);
                switchFragment(drawerListPositions[position]);
            }
        });


        this.drawerLayout = drawerLayout;
        this.drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        this.drawerLayout.setDrawerListener(this.drawerToggle);
        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
    }

    private void switchFragment(int fragmentID) {
        Fragment newFragment = null;
        switch (fragmentID) {
            case 0: //Main
                newFragment = new MainFragment();
                break;
            case 1: //School News
                newFragment = new SchoolNewsFragment();
                break;
            case 2: //Bell Schedules
                break;
            case 3: //Lunch Menus
                break;
            case 4: //School Map
                break;
            case 5: //Upcoming Events
                newFragment = new UpcomingEventsFragment();
                break;
            case 6: //Skyward Access
                newFragment = new WebViewFragment();
                break;
            default:
                break;
        }
        if (newFragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentSpace, newFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        if (this.drawerLayout != null && this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

}
