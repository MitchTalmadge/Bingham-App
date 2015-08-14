package com.aptitekk.binghamapp;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private NavigationDrawerAdapter adapter;
    private int[] drawerListPositions;

    private boolean skipMainSelection = false;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setUp(final DrawerLayout drawerLayout, Toolbar toolbar) {

        getFragmentManager().addOnBackStackChangedListener(this);

        //Set up drawer list
        this.drawerList = (ListView) getActivity().findViewById(R.id.listView);
        String[] drawerListStrings = getResources().getStringArray(R.array.drawer_list_strings);
        this.drawerListPositions = new int[drawerListStrings.length];

        for (int i = 0; i < drawerListStrings.length; i++) {
            String[] split = drawerListStrings[i].split("_");
            drawerListPositions[i] = Integer.parseInt(split[0]);
            drawerListStrings[i] = split[1];
        }

        // Set the adapter for the list view
        this.adapter = new NavigationDrawerAdapter(getActivity(), R.layout.navigation_drawer_list_item, drawerListStrings);
        drawerList.setAdapter(adapter);

        // Set adapter selected item to 0 (Main)
        adapter.setSelectedItem(0);
        adapter.notifyDataSetChanged();

        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
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

    private void selectItem(int position) {
        Fragment newFragment = null;
        Bundle bundle;
        int fragmentID = drawerListPositions[position];

        switch (fragmentID) {
            case 0: //Main
                break;
            case 1: //School News
                newFragment = new SchoolNewsFragment();
                break;
            case 2: //Bell Schedules
                newFragment = new BellSchedulesFragment();
                break;
            case 3: //Lunch Menus
                newFragment = new WebViewFragment();
                bundle = new Bundle();
                bundle.putString("URL", "http://jordandistrict.nutrislice.com/mobile/");
                bundle.putBoolean("useJavaScript", true);
                newFragment.setArguments(bundle);
                break;
            case 4: //School Map
                break;
            case 5: //Upcoming Events
                newFragment = new UpcomingEventsFragment();
                break;
            case 6: //Skyward Access
                newFragment = new WebViewFragment();
                bundle = new Bundle();
                bundle.putString("URL", "https://skystu.jordan.k12.ut.us/scripts/wsisa.dll/WService=wsEAplus/mobilelogin.w");
                bundle.putBoolean("useJavaScript", true);
                newFragment.setArguments(bundle);
                break;
            default:
                break;
        }

        if(fragmentID != 0 && newFragment != null) // If we are loading any other fragment than main...
            skipMainSelection = true; // ...skip setting the Main list item to selected (in onBackStackChanged()).

        ((MainActivity) getActivity()).popToMainMenu(); // Virtually presses the back button until we are at the main menu.

        if (newFragment != null) {
            adapter.setSelectedItem(position);
            adapter.notifyDataSetChanged();

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentSpace, newFragment);
            fragmentTransaction.addToBackStack("navigation");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();
        }

        MainActivity.closeDrawer(this.drawerLayout);
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    public void onBackStackChanged() {
        if(!skipMainSelection && getFragmentManager().getBackStackEntryCount() == 0) {
            this.adapter.setSelectedItem(0);
            this.adapter.notifyDataSetChanged();
        }
        else if(skipMainSelection)
            skipMainSelection = false;
    }

    public class NavigationDrawerAdapter extends ArrayAdapter {

        private int selectedItem;

        public NavigationDrawerAdapter(Context context, int resource, Object[] objects) {
            super(context, resource, objects);
        }

        public int getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(int selectedItem) {
            this.selectedItem = selectedItem;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView view = (TextView) super.getView(position, convertView, parent);

            view.setText((String) getItem(position));

            if (position == selectedItem) {
                view.setTextColor(getContext().getResources().getColor(R.color.primary));
                view.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                view.setTextColor(getContext().getResources().getColor(R.color.primary_text));
                view.setTypeface(Typeface.DEFAULT);
            }

            return view;
        }
    }

}
