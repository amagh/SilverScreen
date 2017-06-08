package com.amagh.silverscreen.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.amagh.silverscreen.BuildConfig;
import com.amagh.silverscreen.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static com.amagh.silverscreen.data.MovieContract.*;

/**
 * Created by hnoct on 6/7/2017.
 */

public class TheMovieDBUtils {
    // **Constants** //

    // For building URL
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_MOVIE_PATH = "movie";

    private static final String TMDB_GENRE_PATH = "genre";
    private static final String TMDB_LIST_PATH = "list";

    private static final String TMDB_API_QUERY = "api_key";
    // TODO: Replace API-Key Here
    private static final String TMDB_API_KEY = BuildConfig.API_KEY;

    // For parsing JSON response
    private static final  String TMDB_POSTER_BASE_PATH = "https://image.tmdb.org/t/p/w185";
    private static final  String TMDB_BACKDROP_BASE_PATH = "https://image.tmdb.org/t/p/w780";

    private static final String JSON_RESULTS_ARRAY = "results";
    private static final String JSON_POSTER = "poster_path";
    private static final String JSON_OVERVIEW = "overview";
    private static final String JSON_RELEASE_DATE = "release_date";
    private static final String JSON_TITLE = "title";
    private static final String JSON_BACKDROP = "backdrop_path";

    private static final String JSON_ID = "id";
    private static final String JSON_VOTE_COUNT = "vote_count";
    private static final String JSON_VOTE_AVERAGE = "vote_average";
    private static final String JSON_POPULARITY = "popularity";

    private static final String JSON_GENRES_ARRAY = "genres";
    private static final String JSON_NAME = "name";

    private static final String JSON_GENRE_ID_ARRAY = "genre_ids";

    /**
     * Builds a URL directing to the TheMovieDB.org API for accessing popular or top-rated movie
     * information or images.
     *
     * @param context Interface to global Context
     * @return URL for accessing movie information
     * @throws MalformedURLException If URL is not a valid URL
     */
    public static URL getMoviesURL(Context context) throws MalformedURLException {
        // Retrieve sort method from SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortMethod = prefs.getString(
                context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_popularity)
        );

        // Build URL using user's preferred sort-method
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(TMDB_MOVIE_PATH)
                .appendPath(sortMethod)
                .appendQueryParameter(TMDB_API_QUERY, TMDB_API_KEY)
                .build();

        return new URL(builtUri.toString());
    }

    /**
     * Builds a URL directing to TheMovieDB.org's API for requesting genre information
     *
     * @return URL for accessing genre information
     * @throws MalformedURLException If URL is not a valid URL
     */
    public static URL getGenresURL() throws MalformedURLException {
        // Build URL for accessing TheMovieDB.org's genre information
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(TMDB_GENRE_PATH)
                .appendPath(TMDB_MOVIE_PATH)
                .appendPath(TMDB_LIST_PATH)
                .appendQueryParameter(TMDB_API_QUERY, TMDB_API_KEY)
                .build();

        return new URL(builtUri.toString());
    }

    /**
     * Opens a connection to the URL and parses the response.
     *
     * @param url URL Object to fetch the response
     * @return The Contents of the URL if successful, null if not successful
     * @throws IOException If error connecting or reading the stream
     */
    public static String getHttpResponse(URL url) throws IOException {
        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Read contents to a String Object
            Scanner scanner = new Scanner(connection.getInputStream());

            // Delimiter for start of response
            scanner.useDelimiter("\\A");

            if (!scanner.hasNext()) {
                return null;
            }

            String response = scanner.next();

            // Close InputStream and Scanner
            scanner.close();

            return response;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Converts the String to a JSONObject and parses the data to create an Array of ContentValues
     * containing movie information to be inserted into the database.
     *
     * @param jsonResponse String containing a JSONObject
     * @return An Array of ContentValues with movie information
     * @throws JSONException If there is an error parsing the String to a JSONObject
     */
    public static ContentValues[] getMoviesContentValuesFromJson(String jsonResponse)
            throws JSONException {

        // Convert String to JSONObject
        JSONObject movieJson = new JSONObject(jsonResponse);

        JSONArray resultsArray = movieJson.getJSONArray(JSON_RESULTS_ARRAY);

        // Init the Array of ContentValues to be returned
        ContentValues[] movieValues = new ContentValues[resultsArray.length()];

        // Iterate, create ContentValues and add them to the Array
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieObject = resultsArray.getJSONObject(i);

            ContentValues value = new ContentValues();
            value.put(MovieEntry.COLUMN_POSTER_PATH, TMDB_POSTER_BASE_PATH + movieObject.getString(JSON_POSTER));
            value.put(MovieEntry.COLUMN_SYNOPSIS,  movieObject.getString(JSON_OVERVIEW));
            value.put(MovieEntry.COLUMN_RELEASE_DATE,  movieObject.getString(JSON_RELEASE_DATE));
            value.put(MovieEntry.COLUMN_TITLE,  movieObject.getString(JSON_TITLE));
            value.put(MovieEntry.COLUMN_BACKDROP_PATH,  TMDB_BACKDROP_BASE_PATH + movieObject.getString(JSON_BACKDROP));;
            value.put(MovieEntry.COLUMN_MOVIE_ID,  movieObject.getInt(JSON_ID));
            value.put(MovieEntry.COLUMN_VOTE_AVG,  movieObject.getDouble(JSON_VOTE_AVERAGE));
            value.put(MovieEntry.COLUMN_VOTE_COUNT,  movieObject.getInt(JSON_VOTE_COUNT));
            value.put(MovieEntry.COLUMN_POPULARITY, movieObject.getDouble(JSON_POPULARITY));

            movieValues[i] = value;
        }

        return movieValues;
    }

    /**
     * Converts a String to a JSON Object and then retrieves genre information from TheMovieDB.org.
     *
     * @param jsonResponse String from TheMovieDB.org containing a JSONObject with genre information
     * @return An Array of ContentValues describing genre information as organized on TheMovieDB.org
     * @throws JSONException If there is an error parsing the String to  JSONObject
     */
    public static ContentValues[] getGenresContentValuesFromJson(String jsonResponse)
            throws JSONException{
        // Convert String Object to JSON Object
        JSONObject genreJson = new JSONObject(jsonResponse);

        JSONArray genresArray = genreJson.getJSONArray(JSON_GENRES_ARRAY);

        // Init the ContentValues Array to hold all the genres to be inserted into the database
        ContentValues[] genreValues = new ContentValues[genresArray.length()];

        // Iterate, create ContentValues, and add them to the Array
        for (int i = 0; i < genresArray.length(); i++) {
            JSONObject genreObject = genresArray.getJSONObject(i);

            ContentValues value = new ContentValues();

            value.put(GenreEntry.COLUMN_GENRE_ID, genreObject.getInt(JSON_ID));
            value.put(GenreEntry.COLUMN_GENRE, genreObject.getString(JSON_NAME));

            genreValues[i] = value;
        }

        return genreValues;
    }

    /**
     * Retrieves genre information for each movie in the JSON response and creates a movie-genre
     * ContentValues pair to be added to the database.
     *
     * @param jsonResponse String containing a JSONObject
     * @return Array of ContentValues describing movies and their genres
     * @throws JSONException If there is an error parsing the String to  JSONObject
     */
    public static ContentValues[] getGenreForMoviesContentValuesFromJson(Context context,
             String jsonResponse) throws JSONException {
        // Convert String to JSONObject
        JSONObject movieJson = new JSONObject(jsonResponse);

        JSONArray resultsArray = movieJson.getJSONArray(JSON_RESULTS_ARRAY);

        // Number of total movie-genre pairs is not readily apparent, so they will need to be added
        // to an ArrayList before being converted back to an Array
        ArrayList<ContentValues> movieGenreValuesList = new ArrayList<>();

        // Iterate, create ContentValues and add them to the Array
        for (int i = 0; i < resultsArray.length(); i++) {
            // Retrieve the movieId to be added to ContentValues
            JSONObject movieObject = resultsArray.getJSONObject(i);
            int movieId = movieObject.getInt(JSON_ID);

            if (DatabaseUtils.movieInLinkGenreMovieTable(context, movieId)) {
                // If the LinkGenreMovie table already contains entries for this movie, then it
                // can be skipped
                continue;
            }

            // Retrieve JSONArray containing genre information
            JSONArray genreArrayObject = movieObject.getJSONArray(JSON_GENRE_ID_ARRAY);

            for (int j = 0; j < genreArrayObject.length(); j++) {
                // Create ContentValues from the movieId and genreIds from the JSON
                ContentValues value = new ContentValues();
                value.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                value.put(GenreEntry.COLUMN_GENRE_ID, genreArrayObject.getInt(j));

                movieGenreValuesList.add(value);
            }

        }

        // Convert ArrayList of movie-genre pairs to an Array
        ContentValues[] movieGenreValues = new ContentValues[movieGenreValuesList.size()];
        movieGenreValuesList.toArray(movieGenreValues);

        return movieGenreValues;
    }


}
