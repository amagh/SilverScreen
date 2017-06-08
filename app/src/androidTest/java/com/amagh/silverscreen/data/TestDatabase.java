package com.amagh.silverscreen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.data.MovieDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static com.amagh.silverscreen.data.MovieContract.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hnoct on 6/7/2017.
 */

@RunWith(AndroidJUnit4.class)
public class TestDatabase {
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
    public void testCreateDb() {
        final HashSet<String> tableNames = new HashSet<>();
        tableNames.add(MovieEntry.TABLE_NAME);
        tableNames.add(GenreEntry.TABLE_NAME);
        tableNames.add(TrailerEntry.TABLE_NAME);
        tableNames.add(LinkGenresMovies.TABLE_NAME);

        String databaseNotOpenError = "Database not open";
        assertTrue(databaseNotOpenError, mDatabase.isOpen());

        Cursor tableNameCursor = mDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null
        );

        String createDatabaseError = "Error creating database";
        assertTrue(createDatabaseError, tableNameCursor.moveToFirst());

        do {
            tableNames.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        String tableError = "Database does not contain the correct tables" + "\nRemaining tables: ";
        for (String table : tableNames) {
            tableError = tableError + table + ",";
        }
        assertTrue(tableError, tableNames.isEmpty());
    }

    @Test
    public void testInsertWithProvider() {
        ContentValues movieValues = TestUtilities.createTestMovieContentValues();
        insertContentValues(MovieEntry.CONTENT_URI, movieValues);

        ContentValues genreValues = TestUtilities.createTestGenreContentValues();
        insertContentValues(GenreEntry.CONTENT_URI, genreValues);

        ContentValues trailerValues = TestUtilities.createTrailerContentValues();
        insertContentValues(TrailerEntry.CONTENT_URI, trailerValues);

        ContentValues linkGenreMoviesValues = TestUtilities.createTestLinkContentValues();
        insertContentValues(LinkGenresMovies.CONTENT_URI, linkGenreMoviesValues);
    }

    private void insertContentValues(Uri insertUri, ContentValues contentValues) {
        Uri uri = context.getContentResolver().insert(
                insertUri,
                contentValues
        );

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );

        assertNotNull(cursor);

        String emptyCursorError = "Cursor for " + uri + " returned no entries";
        assertTrue(emptyCursorError, cursor.moveToFirst());

        TestUtilities.validateCursorValues(cursor, contentValues);

        String multipleEntryError = "Cursor returned more than one entry";
        assertFalse(multipleEntryError, cursor.moveToNext());

        cursor.close();
    }

    @After
    public void after() {
        context.deleteDatabase(mHelper.getDatabaseName());
    }

    @Test
    public void testQueryLinkedTables() {
//        testInsertWithProvider();

        Cursor cursor = context.getContentResolver().query(
                LinkGenresMovies.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertTrue(cursor != null);

        ContentValues linkGenreMovieValues = TestUtilities.createTestLinkContentValues();

        String emptyCursorError = "Cursor for " + LinkGenresMovies.CONTENT_URI +
                " returned no entries";
        assertTrue(emptyCursorError, cursor.moveToFirst());

        TestUtilities.validateCursorValues(cursor, linkGenreMovieValues);

        String multipleEntryError = "Cursor returned more than one entry";
        assertFalse(multipleEntryError, cursor.moveToNext());
    }
}
