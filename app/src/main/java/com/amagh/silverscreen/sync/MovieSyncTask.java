package com.amagh.silverscreen.sync;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.utilities.DatabaseUtils;
import com.amagh.silverscreen.utilities.TheMovieDBUtils;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieSyncTask {

    /**
     * Connects to TheMovieDB.org, downloads movie information, and then utilizes the
     * ContentProvider to insert the information into the database.
     *
     * @param context Interface to global Context
     */
    synchronized public static void syncMovies(@NonNull Context context) {
        try {
            // Connect to TheMovieDB.org and retrieve movie information based on how the user has
            // selected the sort method
            URL tmdbMoviesUrl = TheMovieDBUtils.getMoviesURL(context);
            String moviesJsonResponse = TheMovieDBUtils.getHttpResponse(tmdbMoviesUrl);

            ContentValues[] movieValues =
                    TheMovieDBUtils.getMoviesContentValuesFromJson(moviesJsonResponse);

            ContentValues[] linkGenreMovieValues =
                    TheMovieDBUtils.getGenreForMoviesContentValuesFromJson(context, moviesJsonResponse);

            // Check if genre table needs to be filled
            if (!DatabaseUtils.genreTableExists(context)) {
                // Genre table is empty, retrieve genre info from TheMovieDB.org
                URL tmdbGenreUrl = TheMovieDBUtils.getGenresURL();
                String genreJsonResponse = TheMovieDBUtils.getHttpResponse(tmdbGenreUrl);

                ContentValues[] genreValues =
                        TheMovieDBUtils.getGenresContentValuesFromJson(genreJsonResponse);

                // Bulk insert ContentValues into database
                context.getContentResolver().bulkInsert(MovieContract.GenreEntry.CONTENT_URI,
                        genreValues);
            }

            // Check if the movies table is empty
            boolean emptyTable = DatabaseUtils.getMoviesCount(context) == 0;

            if (emptyTable) {
                // Movies table is empty, values can be bulk inserted without checking
                context.getContentResolver().bulkInsert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        movieValues
                );
            } else {
                // Movies table is not empty. Existing movies will need to have their values updated
                // to reflect change in popularity and vote average
                DatabaseUtils.updateAndInsertMovieValues(context, movieValues);
            }

            // LinkGenreMovie values are checked to see if they exist prior to creating
            // ContentValues, so they can always be bulkInserted
            context.getContentResolver().bulkInsert(
                    MovieContract.LinkGenresMovies.CONTENT_URI,
                    linkGenreMovieValues
            );

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
