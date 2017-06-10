package com.amagh.silverscreen.sync;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.utilities.TheMovieDBUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hnoct on 6/8/2017.
 */

public class MovieTrailersSyncTask {

    synchronized public static void syncTrailers(@NonNull Context context, int movieId) {
        try {
            // Build the URL to retrieve the trailer information from TheMovieDB.org
            URL trailerURL = TheMovieDBUtils.getTrailersURL(movieId);

            // Retrieve the HTTP response from connecting to the URL
            String trailerJsonResponse = TheMovieDBUtils.getHttpResponse(trailerURL);

            // Build the ContentValues for the trailers using the response from TheMovieDB.org
            ContentValues[] trailerValues =
                    TheMovieDBUtils.getTrailerContentValuesFromJson(trailerJsonResponse);

            // Bulk-insert the values into the database
            context.getContentResolver().bulkInsert(
                    MovieContract.TrailerEntry.CONTENT_URI,
                    trailerValues
            );

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
