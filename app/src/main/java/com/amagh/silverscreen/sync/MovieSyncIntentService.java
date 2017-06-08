package com.amagh.silverscreen.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieSyncIntentService extends IntentService {

    public MovieSyncIntentService() {
        super("MovieSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MovieSyncTask.syncMovies(this);
    }
}
