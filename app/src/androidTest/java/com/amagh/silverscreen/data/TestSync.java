package com.amagh.silverscreen.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.amagh.silverscreen.sync.MovieReviewsSyncTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hnoct on 6/12/2017.
 */

@RunWith(AndroidJUnit4.class)
public class TestSync {
    // **Member Variables** //
    private Context context = InstrumentationRegistry.getTargetContext();
    private MovieDbHelper mHelper;
    private SQLiteDatabase mDatabase;

    @Before
    public void before() {
        // Init mHelper
        mHelper = new MovieDbHelper(context);

        // Delete previously created database
        context.deleteDatabase(mHelper.getDatabaseName());

        // Re-create the database
        mDatabase = mHelper.getWritableDatabase();
    }

    @Test
    public void testReviewsSync() {
        MovieReviewsSyncTask.syncTrailers(context, 321612);

        Cursor cursor = context.getContentResolver().query(
                MovieContract.ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        String insertionError = "Error inserting values for movie reviews into database";
        assertNotNull(insertionError, cursor);

        assertTrue(insertionError, cursor.moveToFirst());
    }
}
