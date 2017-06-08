package com.amagh.silverscreen.sync;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieSyncFirebaseJobService extends JobService {
    // **Member Variables** //
    Thread syncThread;

    @Override
    public boolean onStartJob(final JobParameters job) {
        // Create a new Runnable to be run in a background Thread
        Runnable syncRunnable = new Runnable() {
            @Override
            public void run() {
                // Sync the movie database
                MovieSyncTask.syncMovies(getApplicationContext());
                jobFinished(job, false);
            }
        };

        // Init/start a new Thread for syncRunnable
        syncThread = new Thread(syncRunnable);
        syncThread.start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (syncThread != null && !syncThread.isInterrupted()) {
            // Attempt to stop the Thread
            syncThread.interrupt();
        }
        return false;
    }
}
