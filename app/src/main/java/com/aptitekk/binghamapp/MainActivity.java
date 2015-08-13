package com.aptitekk.binghamapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_NAME = "BinghamApp";
    public static final String PREF_NAME = "com.AptiTekk.BinghamApp";

    public static RSSNewsFeed newsFeed;
    public static CalendarDog eventsFeed;

    private RSSNewsFeed downloadingNewsFeed;
    private CalendarDog downloadingEventsFeed;

    private ArrayList<FeedListener> feedListeners = new ArrayList<>();

    private Toolbar toolbar;
    private BackButtonListener backButtonListener;
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

        // Load the MainFragment
        MainFragment mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentSpace, mainFragment).commit();

        // Download News & Events
        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
        int lastFeedUpdateDay = sharedPreferences.getInt("lastFeedUpdateDay", 0);
        int lastFeedUpdateMonth = sharedPreferences.getInt("lastFeedUpdateMonth", 0);

        Log.v(LOG_NAME, "lastUpdateFeedDay: "+lastFeedUpdateDay);
        Log.v(LOG_NAME, "lastUpdateFeedMonth: "+lastFeedUpdateMonth);

        if (lastFeedUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                || lastFeedUpdateMonth != Calendar.getInstance().get(Calendar.MONTH)) { // If the last time we updated was not today...
            Log.v(LOG_NAME, "Feeds are out of date. Downloading Feeds...");

            final Callable<Void> newsFeedCallable = new Callable<Void>() {
                public Void call() {
                    newsFeed = downloadingNewsFeed;
                    for (FeedListener listener : feedListeners) {
                        if (listener != null && (listener instanceof Fragment && !((Fragment) listener).isDetached())) {
                            listener.onNewsFeedDownloaded(newsFeed);
                        }
                    }
                    return null;
                }
            };

            final Callable<Void> eventsFeedCallable = new Callable<Void>() {
                public Void call() {
                    sharedPreferences.edit().putInt("lastFeedUpdateDay", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).putInt("lastFeedUpdateMonth", Calendar.getInstance().get(Calendar.MONTH)).apply();
                    eventsFeed = downloadingEventsFeed;
                    for (FeedListener listener : feedListeners) {
                        if (listener != null && (listener instanceof Fragment && !((Fragment) listener).isDetached())) {
                            listener.onEventFeedDownloaded(eventsFeed);
                        }
                    }
                    return null;
                }
            };

            downloadingNewsFeed = new RSSNewsFeed(newsFeedCallable);
            downloadingEventsFeed = new CalendarDog(CalendarDog.BINGHAM_GOOGLE_CALENDAR,
                    eventsFeedCallable,
                    CalendarDog.FetchType.ICAL);
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer(this.drawer)) {
            if (this.backButtonListener != null && this.backButtonListener instanceof Fragment && !((Fragment) this.backButtonListener).isDetached()) {
                if (this.backButtonListener.onBackPressed()) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
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
    }

    public void setBackButtonListener(BackButtonListener listener) {
        this.backButtonListener = listener;
    }

    public interface BackButtonListener {
        /**
         * @return true if super.onBackPressed() should be called in MainActivity, false if not
         */
        boolean onBackPressed();
    }

    public void addFeedListener(FeedListener listener) {
        if (!this.feedListeners.contains(listener))
            this.feedListeners.add(listener);
    }

    public interface FeedListener {
        void onNewsFeedDownloaded(RSSNewsFeed newsFeed);

        void onEventFeedDownloaded(CalendarDog eventFeed);
    }
}
