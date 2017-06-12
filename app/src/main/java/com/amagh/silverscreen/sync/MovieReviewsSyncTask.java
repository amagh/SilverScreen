package com.amagh.silverscreen.sync;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.utilities.TheMovieDBUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by hnoct on 6/12/2017.
 */

public class MovieReviewsSyncTask {

    synchronized public static void syncTrailers(@NonNull Context context, int movieId) {
        try {
            // Build the URL to retrieve the trailer information from TheMovieDB.org
            URL reviewsUrl = TheMovieDBUtils.getReviewsURL(movieId);

            // Retrieve the HTTP response from connecting to the URL
            String reviewsJsonResponse = TheMovieDBUtils.getHttpResponse(reviewsUrl);

            // Build the ContentValues for the trailers using the response from TheMovieDB.org
            ContentValues[] reviewsValues =
                    TheMovieDBUtils.getReviewsContentValuesFromJson(reviewsJsonResponse);

            // Bulk-insert the values into the database
            context.getContentResolver().bulkInsert(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    reviewsValues
            );

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
