package edu.isu.reu.intent_collection_service;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

/**
 * TODO list
 *
 * This Activity provides:
 *      -- preferences for a service collecting system intents
 *      -- when this activity is created, it starts the service if not already running
 *      -- buttons to call other activities which analyze these system intents
 *          ++ these other activities will also display graphically, the intent graph
 */

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up action bar
        setupActionBar();

        // create preference elements
        setup_preferences();

        // start the intent collection service
        start_collection_service();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * set up preferences options
     *
     * preferences are:
     *      - stop collection
     *      - start collection
     *
     * the goal of these presences is to modify the functionality of the
     * intent collection service
     */
    private void setup_preferences(){
        //TODO
    }

    /**
     * start the collection service
     */
    private void start_collection_service(){
        // ask the service to do some work
        Intent say_hello_intent = new Intent(this, IntentCollectionService.class);
        say_hello_intent.setData(IntentCollectionService.Command.START.get_uri());

        // start the service
        this.startService(say_hello_intent);
    }

    /**
     * stop the collection service
     */
    private void stop_collection_service(){
        Intent stop_intent = new Intent(this, IntentCollectionService.class);
        stop_intent.setData(IntentCollectionService.Command.STOP.get_uri());

        // stop the service
        stopService(stop_intent);
    }
}
