package com.aptitekk.binghamapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aptitekk.binghamapp.Events.EventsManager;
import com.aptitekk.binghamapp.Fragments.MainFragment;
import com.aptitekk.binghamapp.Fragments.NavigationDrawerFragment;
import com.aptitekk.binghamapp.News.NewsFeed;
import com.aptitekk.binghamapp.News.NewsFeedManager;

import java.util.ArrayList;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_NAME_INFO = "BinghamAppInfo";
    public static final String LOG_NAME_VERBOSE = "BinghamAppVerbose";
    public static final String PREF_NAME = "com.AptiTekk.BinghamApp";

    private NewsFeedManager newsFeedManager;
    private EventsManager eventsManager;

    private Toolbar toolbar;
    private ArrayList<BackButtonListener> backButtonListeners = new ArrayList<>();
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the NavigationDrawer
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp((DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        this.drawer = drawerFragment.getDrawerLayout();

        if (savedInstanceState == null) { //First time loaded; not a killed process
            // Load the MainFragment
            MainFragment mainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentSpaceMain, mainFragment).commit();
        }

        logInfo("Setting Up Managers...");

        this.newsFeedManager = new NewsFeedManager(this);
        this.eventsManager = new EventsManager(this);

        logInfo("Checking for Updates...");

        // Download News & Events
        checkForNewsUpdates();
        checkForEventsUpdates();
    }

    public NewsFeedManager getNewsFeedManager() {
        return newsFeedManager;
    }

    public EventsManager getEventsManager() {
        return eventsManager;
    }

    public void checkForNewsUpdates() {
        logVerbose("Checking for News Updates...");
        for (NewsFeed newsFeed : newsFeedManager.getNewsFeeds())
            newsFeed.checkForUpdates();
    }

    public void checkForEventsUpdates() {
        logVerbose("Checking for Events Updates...");
        eventsManager.checkForUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer(this.drawer)) {
            ListIterator<BackButtonListener> listenerIterator = backButtonListeners.listIterator();
            while (listenerIterator.hasNext()) {
                BackButtonListener listener = listenerIterator.next();
                if (listener != null && listener instanceof Fragment && ((Fragment) listener).isAdded()) {
                    if (!listener.onBackPressed()) {
                        return;
                    }
                } else {
                    listenerIterator.remove();
                }
            }
        }
        super.onBackPressed();
    }

    public void addBackButtonListener(BackButtonListener listener) {
        this.backButtonListeners.add(0, listener); // Insert listener at beginning of list so its actions are called first.
    }

    public interface BackButtonListener {
        /**
         * @return true if super.onBackPressed() should be called in MainActivity, false if not
         */
        boolean onBackPressed();
    }

    /**
     * Closes the drawer if it is open
     *
     * @return true if the drawer was closed, false if it was not.
     */
    public static boolean closeDrawer(DrawerLayout drawer) {
        if (drawer != null) {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else
                return false;
        }
        return false;
    }

    public void popToMainMenu() {
        getSupportFragmentManager().popBackStack("navigation", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        /*for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++) {
            getFragmentManager().popBackStack();
        }*/
    }

    public static int pixelsToDP(int pixels, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (pixels / (metrics.densityDpi / 160f));
    }

    public static void listFragments(FragmentManager manager) {
        if (manager != null) {
            int count = manager.getBackStackEntryCount();
            logInfo("*----------------");
            logInfo("*Backstack Count: " + count);
            for (int i = 0; i < count; i++) {
                logInfo("*" + i + ": " + manager.getBackStackEntryAt(i));
            }
        }
    }

    public static void logInfo(String message)
    {
        Log.i(LOG_NAME_INFO, message);
    }

    public static void logVerbose(String message)
    {
        Log.i(LOG_NAME_VERBOSE, message);
    }
}
