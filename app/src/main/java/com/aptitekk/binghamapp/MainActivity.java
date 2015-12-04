package com.aptitekk.binghamapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarDog;
import com.aptitekk.binghamapp.rssnewsfeed.NewsFeed;
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeedManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity implements RSSNewsFeedManager.NewsFeedSizeListener {

    public static final String LOG_NAME = "BinghamAppVerbose";
    public static final String PREF_NAME = "com.AptiTekk.BinghamApp";

    public static RSSNewsFeedManager newsFeeds;
    public static CalendarDog eventsFeed;

    private CalendarDog downloadingEventsFeed;

    private ArrayList<FeedListener> feedListeners = new ArrayList<>();

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

        // Set up news feed manager
        newsFeeds = new RSSNewsFeedManager();

        // Download News & Events
        checkForNewsUpdates();
        checkForEventsUpdates();
    }

    public void checkForNewsUpdates() {
        for (NewsFeed newsFeed : newsFeeds.getNewsFeeds())
            RSSNewsFeedManager.getNewsFeedSize(this, newsFeed);
    }

    public void checkForEventsUpdates() {
        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int lastEventsFeedUpdateDay = sharedPreferences.getInt("lastEventsFeedUpdateDay", 0);
        int lastEventsFeedUpdateMonth = sharedPreferences.getInt("lastEventsFeedUpdateMonth", 0);

        final Callable<Void> eventsFeedCallable = new Callable<Void>() {
            public Void call() {
                eventsFeed = downloadingEventsFeed;
                for (FeedListener listener : feedListeners) {
                    if (listener != null && (listener instanceof Fragment && !((Fragment) listener).isDetached())) {
                        listener.onEventsFeedDownloaded(eventsFeed);
                    }
                }
                // Save the feed to file...
                try {
                    Log.v(LOG_NAME, "Saving events feed to file...");
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "events.feed"));

                    if (eventsFeed.getJSONObject() != null) {
                        String jsonString = eventsFeed.getJSONObject().toString();

                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        outputStreamWriter.write(jsonString);

                        outputStreamWriter.close();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        eventsFeed = CalendarDog.determineRetrieval(sharedPreferences, lastEventsFeedUpdateDay, lastEventsFeedUpdateMonth,
                eventsFeedCallable, getFilesDir());
    }


    public class NewsFeedRefreshCallable implements Callable<Void> {

        NewsFeed newsFeed;
        SharedPreferences sharedPreferences;
        ArrayList<FeedListener> feedListeners;
        int newsFeedSize;

        public NewsFeedRefreshCallable(NewsFeed newsFeed,
                                       SharedPreferences sharedPreferences,
                                       ArrayList<FeedListener> feedListeners,
                                       int newsFeedSize) {
            this.newsFeed = newsFeed;
            this.sharedPreferences = sharedPreferences;
            this.feedListeners = feedListeners;
            this.newsFeedSize = newsFeedSize;
        }

        @Override
        public Void call() throws Exception {
            for (FeedListener listener : feedListeners) {
                if (listener != null && (listener instanceof Fragment && ((Fragment) listener).isAdded())) {
                    listener.onNewsFeedDownloaded(newsFeed);
                }
            }
            // Save the feed to file...
            try {
                Log.v(LOG_NAME, "Saving news feed to file...");

                sharedPreferences.edit().putInt(newsFeed.getPreferencesTag(), newsFeedSize).apply();

                FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), newsFeed.getFileName()));
                Document document = newsFeed.getDocument();

                //Converts the Document into a file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(fileOutputStream);
                transformer.transform(source, result);

                fileOutputStream.close();
            } catch (IOException | TransformerException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onGetNewsFeedSize(final int newsFeedSize, NewsFeed newsFeed) {
        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int lastNewsFeedUpdateSize = sharedPreferences.getInt(newsFeed.getPreferencesTag(), 0);

        Log.v(LOG_NAME, "lastNewsFeedUpdateSize: " + lastNewsFeedUpdateSize);
        Log.v(LOG_NAME, "News Feed Size on Web: " + newsFeedSize);


        final Callable<Void> newsFeedCallable = new NewsFeedRefreshCallable(newsFeed,
                sharedPreferences, feedListeners, newsFeedSize);

        newsFeeds.determineRetrieval(newsFeed, lastNewsFeedUpdateSize,
                newsFeedSize, newsFeedCallable, getFilesDir());
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

    public void addFeedListener(FeedListener listener) {
        if (!this.feedListeners.contains(listener))
            this.feedListeners.add(listener);

        // If the listener was late to the party, send them what we already got
        for(NewsFeed newsFeed : newsFeeds.getNewsFeeds()) {
            if(newsFeed.isReady())
                listener.onNewsFeedDownloaded(newsFeed);
        }

        if (eventsFeed.isReady()) {
            listener.onEventsFeedDownloaded(eventsFeed);
        }
    }

    public interface FeedListener {
        void onNewsFeedDownloaded(NewsFeed newsFeed);

        void onEventsFeedDownloaded(CalendarDog eventsFeed);
    }

    public static int pixelsToDP(int pixels, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (pixels / (metrics.densityDpi / 160f));
    }

    public static void listFragments(FragmentManager manager) {
        if (manager != null) {
            int count = manager.getBackStackEntryCount();
            Log.i(LOG_NAME, "*----------------");
            Log.i(LOG_NAME, "*Backstack Count: " + count);
            for (int i = 0; i < count; i++) {
                Log.i(LOG_NAME, "*" + i + ": " + manager.getBackStackEntryAt(i));
            }
        }
    }
}
