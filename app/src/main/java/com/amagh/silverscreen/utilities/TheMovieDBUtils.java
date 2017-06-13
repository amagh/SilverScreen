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

    private static final String TMDB_TRAILER_PATH = "videos";

    private static final String TMDB_REVIEWS_PATH = "reviews";

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

    private static final String YT_VIDEOS_BASE_PATH = "https://www.youtube.com/watch?v=";
    private static final String YT_THUMBNAIL_BASE_PATH = "https://img.youtube.com/vi/%s/hqdefault.jpg";

    private static final String JSON_KEY = "key";
    private static final String JSON_TYPE = "type";

    private static final String JSON_AUTHOR = "author";
    private static final String JSON_CONTENT = "content";



    /**
     * Builds a URL directing to the TheMovieDB.org API for accessing popular or top-rated movie
     * information or images.
     *
     * @return URL for accessing movie information
     * @throws MalformedURLException If URL is not a valid URL
     */
    public static URL getMoviesURL(String sortMethod) throws MalformedURLException {
        // Build URL using user's preferred sort-method
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(TMDB_MOVIE_PATH)
                .appendPath(sortMethod)
                .appendQueryParameter(TMDB_API_QUERY, TMDB_API_KEY)
                .build();

        return new URL(builtUri.toString());
    }

    /**
     * Builds a URL directing to TheMovieDB.org's API for requesting genre information.
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
     * Builds a URL directing to TheMovieDB.org's API for requesting trailer information.
     *
     * @param movieId The ID TheMovieDB.org uses to reference movies
     * @return URL for accessing trailer information
     * @throws MalformedURLException If the URL is not a valid URL
     */
    public static URL getTrailersURL(int movieId) throws MalformedURLException {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(TMDB_MOVIE_PATH)
                .appendPath(Integer.toString(movieId))
                .appendPath(TMDB_TRAILER_PATH)
                .appendQueryParameter(TMDB_API_QUERY, TMDB_API_KEY)
                .build();

        return new URL(builtUri.toString());
    }

    /**
     * Builds a URL directing to TheMovieDB.org's API for requesting review information.
     *
     * @param movieId The ID used by TheMovieDB.org to reference a movie
     * @return URL for accessing review information
     * @throws MalformedURLException If the URL is not a valid URL
     */
    public static URL getReviewsURL(int movieId) throws MalformedURLException {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(TMDB_MOVIE_PATH)
                .appendPath(Integer.toString(movieId))
                .appendPath(TMDB_REVIEWS_PATH)
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

    /**
     * Retrieve the information for all the trailers associated with a movie from TheMovieDB.org
     * and build ContentValues to be inserted into the database.
     *
     * @param jsonResponse The String containg a JSONOBject describing the trailer information
     * @return An Array of ContentValues to with trailer information to be inserted into the
     * database
     * @throws JSONException If there is an error converting the String to a JSONObject
     */
    public static ContentValues[] getTrailerContentValuesFromJson(String jsonResponse)
            throws JSONException {
        // Convert to JSONObject
        JSONObject trailersJson = new JSONObject(jsonResponse);

        // Retrieve the id of the movie contained in the JSONObject
        int movieId = trailersJson.getInt(JSON_ID);

        // Retrieve the JSONArray with all the trailer information
        JSONArray trailersArray = trailersJson.getJSONArray(JSON_RESULTS_ARRAY);

        // Init the Array of ContentValues to be returned
        ContentValues[] trailersValues = new ContentValues[trailersArray.length()];

        // Iterate, build the ContentValues, and add them to the Array
        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject trailerObject = trailersArray.getJSONObject(i);

            String key = trailerObject.getString(JSON_KEY);

            ContentValues trailerValues = new ContentValues();
            trailerValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
            trailerValues.put(TrailerEntry.COLUMN_TRAILER_ID, trailerObject.getString(JSON_ID));
            trailerValues.put(TrailerEntry.COLUMN_VIDEO_PATH, YT_VIDEOS_BASE_PATH + key);
            trailerValues.put(TrailerEntry.COLUMN_NAME, trailerObject.getString(JSON_NAME));
            trailerValues.put(TrailerEntry.COLUMN_TYPE, trailerObject.getString(JSON_TYPE));
            trailerValues.put(TrailerEntry.COLUMN_THUMBNAIL_PATH, String.format(YT_THUMBNAIL_BASE_PATH, key));

            trailersValues[i] = trailerValues;
        }

        return trailersValues;
    }

    /**
     * Creates an Array of ContentValues describing the review information for a movie from
     * TheMovieDB.org.
     *
     * @param jsonResponse String containing a JSONObject describing review information
     * @return An Array of ContentValues describing review information
     * @throws JSONException If there is an error parsing the String to a JSONObject
     */
    public static ContentValues[] getReviewsContentValuesFromJson(String jsonResponse)
            throws JSONException{
        // Convert the String parameter to a JSONObject
        JSONObject reviewsJson = new JSONObject(jsonResponse);

        // Retrieve the movieId from the JSONObject
        int movieId = reviewsJson.getInt(JSON_ID);

        // Get the Array containing all the reviews
        JSONArray reviewsArray = reviewsJson.getJSONArray(JSON_RESULTS_ARRAY);

        // Init the Array that will contain all the reviews
        ContentValues[] reviewsValues = new ContentValues[reviewsArray.length()];

        // Iterate and create the ContentValues for each review in the JSONArray
        for (int i = 0; i < reviewsArray.length(); i++) {
            JSONObject reviewObject = reviewsArray.getJSONObject(i);

            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
            reviewValues.put(ReviewEntry.COLUMN_REVIEW_ID, reviewObject.getString(JSON_ID));
            reviewValues.put(ReviewEntry.COLUMN_AUTHOR, reviewObject.getString(JSON_AUTHOR));
            reviewValues.put(ReviewEntry.COLUMN_CONTENT, reviewObject.getString(JSON_CONTENT));

            reviewsValues[i] = reviewValues;
        }

        return reviewsValues;
    }
}
