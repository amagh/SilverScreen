package com.amagh.silverscreen.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Set;

import static com.amagh.silverscreen.data.MovieContract.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hnoct on 6/7/2017.
 */

public class TestUtilities {
    public static void validateCursorValues(Cursor cursor, ContentValues values) {
        String nullCursorError = "Cursor is null. Is ContentProvider registered in " +
                "AndroidManifest.xml?";
        assertNotNull(nullCursorError, cursor);

        Set<String> keySet = values.keySet();

        for (String key : keySet) {
            int columnIndex = cursor.getColumnIndex(key);

            String columnNotFoundError = key + " column not found";
            assertFalse(columnNotFoundError, columnIndex == -1);

            String expectedValue = values.getAsString(key);
            String cursorValue = cursor.getString(columnIndex);

            String matchError = "Expected value: " + expectedValue +
                    " does not match actual value: " + cursorValue;

            assertEquals(matchError, expectedValue, cursorValue);
        }
    }

    public static ContentValues createTestMovieContentValues() {
        ContentValues value = new ContentValues();
        value.put(MovieEntry.COLUMN_POSTER_PATH, "posterpath");
        value.put(MovieEntry.COLUMN_SYNOPSIS, "Test synposis");
        value.put(MovieEntry.COLUMN_RELEASE_DATE, "2017-06-07");
        value.put(MovieEntry.COLUMN_TITLE, "Movie Title");
        value.put(MovieEntry.COLUMN_BACKDROP_PATH, "backdroppath");
        value.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        value.put(MovieEntry.COLUMN_VOTE_AVG, 4.5);
        value.put(MovieEntry.COLUMN_VOTE_COUNT, 1);
        value.put(MovieEntry.COLUMN_POPULARITY, 123.12);

        return value;
    }

    public static ContentValues createTestGenreContentValues() {
        ContentValues value = new ContentValues();
        value.put(GenreEntry.COLUMN_GENRE_ID, 123);
        value.put(GenreEntry.COLUMN_GENRE, "test genre");

        return value;
    }

    public static ContentValues createTrailerContentValues() {
        ContentValues value = new ContentValues();
        value.put(TrailerEntry.COLUMN_TRAILER_ID, 456);
        value.put(TrailerEntry.COLUMN_VIDEO_PATH, "testKey");
        value.put(TrailerEntry.COLUMN_NAME, "Test Trailer");
        value.put(TrailerEntry.COLUMN_TYPE, "Trailer");
        value.put(MovieEntry.COLUMN_MOVIE_ID, 1);

        return value;
    }

    public static ContentValues createTestLinkContentValues() {
        ContentValues value = new ContentValues();
        value.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        value.put(GenreEntry.COLUMN_GENRE_ID, 123);

        return value;
    }

}
