package com.amagh.silverscreen.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.amagh.silverscreen.utilities.DatabaseUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by hnoct on 6/8/2017.
 */

public class MovieSyncUtils {
    // **Constants ** //
    private static final int SIX_HOURS = (int) TimeUnit.HOURS.toSeconds(6);
    private static final int SYNC_FLEX_THREE_HOURS = (int) TimeUnit.HOURS.toSeconds(3);

    private static final String MOVIE_SYNC_TAG = "movie_sync_job";

    // **Member Variables** //
    private static boolean sInitialized;

    /**
     * Schedules a re-curring Job to be done at a periodic time to sync the movies in the background
     * even while the app is not running
     *
     * @param context Interface to global Context
     */
    synchronized public static void initialize(@NonNull final Context context) {
        // Check if the Job has has already been scheduled
        if (sInitialized) return;

        // Set to true so this process isn't run again
        sInitialized = true;

        // Schedule the Job to recur using FirebaseJobDispatcher
        scheduleFirebaseJobDispatcherSync(context);

        // Create a new background Thread to check whether the database is empty. If it is empty,
        // immediately sync the database
        Thread checkDatabaseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Check if the movies table is empty
                if (DatabaseUtils.getMoviesCount(context) == 0) {
                    // If empty, sync the database with TheMovieDB.org
                    syncImmediately(context);
                }
            }
        });

        // Start the thread
        checkDatabaseThread.start();
    }

    /**
     * Utilizes FirebaseJobDispatcher to schedule a FirebaseJob at a regular sync period
     *
     * @param context Interface to global Context
     */
    private static void scheduleFirebaseJobDispatcherSync(@NonNull Context context) {
        // Init FirebaseJobDispatcher
        GooglePlayDriver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Build the Job
        Job movieSyncJob = dispatcher.newJobBuilder()
                .setService(MovieSyncFirebaseJobService.class)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRecurring(true)
                .setReplaceCurrent(true)
                .setTrigger(Trigger.executionWindow(
                        SIX_HOURS,
                        SIX_HOURS + SYNC_FLEX_THREE_HOURS
                ))
                .setTag(MOVIE_SYNC_TAG)
                .build();

        // Schedule the Job
        dispatcher.schedule(movieSyncJob);
    }

    /**
     * Calls MovieSyncIntentService to being syncing the database to TheMovieDB.org
     *
     * @param context Interface to global Context
     */
    private static void syncImmediately(@NonNull Context context) {
        // Create and start the MovieSyncIntentService
        Intent startSyncIntent = new Intent(context, MovieSyncIntentService.class);
        context.startService(startSyncIntent);
    }
}
