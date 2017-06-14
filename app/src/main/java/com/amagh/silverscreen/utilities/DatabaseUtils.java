package com.amagh.silverscreen.utilities;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.amagh.silverscreen.data.MovieContract.*;

/**
 * Created by hnoct on 6/7/2017.
 */

public final class DatabaseUtils {
    /**
     * Suppressed Constructor
     */
    private DatabaseUtils() {}

    /**
     * Queries the database to check that the genre table was created and that it contains rows
     *
     * @param context Interface to global Context
     * @return true if genre table contains rows, false otherwise
     */
    public static boolean genreTableExists(@NonNull Context context) {
        // Query database for genre table
        Cursor cursor = context.getContentResolver().query(
                GenreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Check to make sure Cursor is valid
        if (cursor != null) {
            try {
                // Check to make sure Cursor contains rows
                return cursor.getCount() > 0;
            } finally {
                // Close the Cursor
                cursor.close();
            }
        } else {
            return false;
        }
    }

    /**
     * Queries the database and returns the number of rows in the movies table
     *
     * @param context Interface to global Context
     * @return Number of rows in the movies table
     */
    public static int getMoviesCount(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            try {
                return cursor.getCount();
            } finally {
                cursor.close();
            }
        } else {
            return 0;
        }
    }

    /**
     * Checks the database to see whether a movie entry needs to be inserted or updated into the
     * database and then performs batch operations to minimize operation time
     *
     * @param context   Interface to global Context
     * @param values    Array of ContentValues describing movie information
     */
    public static void updateAndInsertMovieValues(@NonNull Context context, ContentValues[] values) {
        // Lists to hold ContentValues to insert and ContentProviderOperations for movies that are
        // already in table
        List<ContentValues> insertList = new ArrayList<>();
        ArrayList<ContentProviderOperation> updateList = new ArrayList<>();

        // Iterate through ContentValues and check whether the movie needs to be inserted or updated
        for (ContentValues value : values) {
            // Init selection/selectionArg with values from the ContentValues
            String selection = MovieEntry.COLUMN_MOVIE_ID + " = ?";
            String[] selectionArg = new String[] {value.getAsString(MovieEntry.COLUMN_MOVIE_ID)};

            // Query the database
            Cursor cursor = context.getContentResolver().query(
                    MovieEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArg,
                    null
            );

            // Check to see if Cursor is valid
            if (cursor != null) {
                if (cursor.getCount() == 1) {
                    // If table contains movieId, then create an update ContentProviderOperation
                    ContentProviderOperation operation = ContentProviderOperation.newUpdate(
                            MovieEntry.CONTENT_URI)
                            .withSelection(selection, selectionArg)
                            .withValue(MovieEntry.COLUMN_POPULARITY, value.getAsDouble(MovieEntry.COLUMN_POPULARITY))
                            .withValue(MovieEntry.COLUMN_VOTE_AVG, value.getAsDouble(MovieEntry.COLUMN_VOTE_AVG))
                            .withValue(MovieEntry.COLUMN_VOTE_COUNT, value.getAsInteger(MovieEntry.COLUMN_VOTE_COUNT))
                            .build();

                    // Add the operation to updateList
                    updateList.add(operation);

                } else if (cursor.getCount() == 0){
                    // If table does not contain movieId, then add to insertList
                    insertList.add(value);

                } else {
                    // If table returns more than one entry, then no movieId was supplied in the
                    // ContentValues
                    throw new IllegalArgumentException("ContentValues must contain value for movie_id");
                }

                // Close the Cursor
                cursor.close();
            } else {
                insertList.add(value);
            }
        }

        // Bulk insert all values in insertList
        if (insertList.size() > 0) {
            context.getContentResolver().bulkInsert(
                    MovieEntry.CONTENT_URI,
                    insertList.toArray(new ContentValues[insertList.size()])  // Convert List to Array
            );
        }

        // Batch update all values in updateList
        if (updateList.size() > 0) {
            try {
                context.getContentResolver().applyBatch(
                        AUTHORITY,
                        updateList
                );
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Checks to see if a certain movie is already in the database
     *
     * @param context   Interface to global Context
     * @param movieId   The id supplied utilized by TheMovieDB.org to identify movies
     * @return true if movieId exists in database, false if it does not
     */
    public static boolean movieInLinkGenreMovieTable(@NonNull Context context, int movieId) {
        // Build URI for querying database
        Uri linkUri = LinkGenresMovies.CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .appendPath(Integer.toString(movieId))
                .build();

        // Query database
        Cursor cursor = context.getContentResolver().query(
                linkUri,
                null,
                null,
                null,
                null
        );

        // Check to see if Cursor is valid
        if (cursor != null) {
            try {
                return cursor.getCount() > 0;
            } finally {
                // Close the Cursor
                cursor.close();
            }
        }

        // Return false if Cursor is not valid
        return false;
    }

    /**
     * Toggles the favorite status of a movie in the database.
     *
     * @param context   Interface to global Context
     * @param movieId   ID of the movie to be altered
     * @return The boolean value of the favorite status of the movie after it has been altered
     */
    public static boolean toggleFavoriteStatus (@NonNull Context context, int movieId) {
        // Build the URI for the movieId
        Uri movieUri = MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(movieId))
                .build();

        // Query the database to check the current favorite status of the movie
        Cursor cursor = context.getContentResolver().query(
                movieUri,
                new String[] {MovieEntry.COLUMN_FAVORITE},
                null,
                null,
                null
        );

        // Init the boolean
        boolean favorite = false;

        // Set favorite to the movie's favorite status
        if (cursor != null) {
            favorite = cursor.moveToFirst() && cursor.getInt(0) == 1;

            // Close the Cursor
            cursor.close();
        }

        // Create the ContentValues to update the movie values
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_FAVORITE, favorite ? 0 : 1);

        context.getContentResolver().update(
                movieUri,
                values,
                null,
                null
        );

        return !favorite;
    }
}
