package com.aptitekk.binghamapp;

import android.content.Context;
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
import com.aptitekk.binghamapp.rssnewsfeed.RSSNewsFeed;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_NAME = "BinghamAppVerbose";
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

        FragmentManager.enableDebugLogging(true);

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
        int lastFeedUpdateDay = sharedPreferences.getInt("lastFeedUpdateDay", 0);
        int lastFeedUpdateMonth = sharedPreferences.getInt("lastFeedUpdateMonth", 0);

        Log.v(LOG_NAME, "lastUpdateFeedDay: " + lastFeedUpdateDay);
        Log.v(LOG_NAME, "lastUpdateFeedMonth: " + lastFeedUpdateMonth);

        final Callable<Void> newsFeedCallable = new Callable<Void>() {
            public Void call() {
                newsFeed = downloadingNewsFeed;
                for (FeedListener listener : feedListeners) {
                    if (listener != null && (listener instanceof Fragment && ((Fragment) listener).isAdded())) {
                        listener.onNewsFeedDownloaded(newsFeed);
                    }
                }
                // Save the feed to file...
                try {
                    Log.v(LOG_NAME, "Saving news feed to file...");
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "news.feed"));

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
        };

        final Callable<Void> eventsFeedCallable = new Callable<Void>() {
            public Void call() {
                eventsFeed = downloadingEventsFeed;
                for (FeedListener listener : feedListeners) {
                    if (listener != null && (listener instanceof Fragment && !((Fragment) listener).isDetached())) {
                        listener.onEventFeedDownloaded(eventsFeed);
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

        if (lastFeedUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                || lastFeedUpdateMonth != Calendar.getInstance().get(Calendar.MONTH)) { // If the last time we updated was not today...
            Log.v(LOG_NAME, "Feeds are out of date. Downloading Feeds...");
            sharedPreferences.edit().putInt("lastFeedUpdateDay", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).putInt("lastFeedUpdateMonth", Calendar.getInstance().get(Calendar.MONTH)).apply();


            downloadingNewsFeed = new RSSNewsFeed(newsFeedCallable);
            downloadingEventsFeed = new CalendarDog(eventsFeedCallable,
                    CalendarDog.FetchType.JSON);
        } else { // We have already downloaded the news and events today.. Lets retrieve the files and create feeds from them.
            File newsFeedFile = new File(getFilesDir(), "news.feed");
            File eventsFeedFile = new File(getFilesDir(), "events.feed");

            if (newsFeedFile.exists()) {
                Log.v(LOG_NAME, "Restoring news feed from file...");
                try {
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(newsFeedFile);
                    newsFeed = new RSSNewsFeed(document);
                } catch (SAXException | IOException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(LOG_NAME, "Could not restore news feed from file.");
                downloadingNewsFeed = new RSSNewsFeed(newsFeedCallable);
            }

            if (eventsFeedFile.exists()) {
                Log.v(LOG_NAME, "Restoring events feed from file...");
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(eventsFeedFile));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    String ls = System.getProperty("line.separator");

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append(ls);
                    }

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    eventsFeed = new CalendarDog(jsonObject);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(LOG_NAME, "Could not restore events feed from file.");
                downloadingEventsFeed = new CalendarDog(eventsFeedCallable,
                        CalendarDog.FetchType.JSON);
            }
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
            if (this.backButtonListener != null && this.backButtonListener instanceof Fragment && ((Fragment) this.backButtonListener).isAdded()) {
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
