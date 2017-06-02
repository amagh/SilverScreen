package com.amagh.silverscreen.silverscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.amagh.silverscreen.silverscreen.MovieDetailsActivity.EXTRAS.EXTRA_MOVIE;

public class MovieListActivity extends AppCompatActivity {
    // Constants
    private final String TAG = MovieListActivity.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef ({POPULAR, RATING})
    @interface SortMethod {}
    private static final int POPULAR = 0;
    private static final int RATING = 1;

    // Mem Vars
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        // Obtain references to Views
        mRecyclerView = (RecyclerView) findViewById(R.id.movie_list_rv);
        mProgressBar = (ProgressBar) findViewById(R.id.movie_list_pb);

        // Init/set LayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        // Init/set MovieAdapter
        mAdapter = new MovieAdapter(mMovieClickHandler);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Fetch movies
        FetchMoviesTask moviesTask = new FetchMoviesTask(POPULAR);
        moviesTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu to allow for sorting
        getMenuInflater().inflate(R.menu.menu_movie_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort: {

                // Show a Dialog to allow the user to select how to query TheMovieDB.org
                AlertDialog dialog = buildSortDialog();

                // Show the Dialog when the menu item is selected
                dialog.show();

                return true;
            }

            default: return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Builds an AlertDialog with option to select either sort by popular or top rated
     *
     * @return AlertDialog
     */
    private AlertDialog buildSortDialog() {
        // Build and return the AlertDialog
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_sort_title))
                .setView(R.layout.dialog_sort)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int i) {
                        // Obtain references to the RadioButton Views
                        RadioButton popularRadioButton =
                                (RadioButton) ((AlertDialog) dialog1).findViewById(R.id.dialog_popular_rb);

                        RadioButton topRatedRadioButton =
                                (RadioButton) ((AlertDialog) dialog1).findViewById(R.id.dialog_top_rated_rb);

                        assert popularRadioButton != null;
                        assert topRatedRadioButton != null;

                        // Check which RadioButton is checked and launch FetchMovieTask with
                        // the correct parameters
                        if (popularRadioButton.isChecked()) {
                            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(POPULAR);
                            fetchMoviesTask.execute();
                        } else if (topRatedRadioButton.isChecked()){
                            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(RATING);
                            fetchMoviesTask.execute();
                        }

                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing. Just dismiss the Dialog
                    }
                })
                .create();
    }


    private final MovieAdapter.MovieClickHandler mMovieClickHandler = new MovieAdapter.MovieClickHandler() {
        @Override
        public void onMovieClick(Movie movie) {
            // Build an explicit Intent to launch MovieDetailsActivity
            Intent intent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);

            // Put the Movie Object corresponding to the selected movie poster as an Extra to the
            // Intent
            intent.putExtra(EXTRA_MOVIE, movie);

            // Start MovieDetailsActivity
            startActivity(intent);
        }
    };

    private class FetchMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        // Constants
        private final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
        private final String TMBD_MOVIE_PATH = "movie";
        private final String TMDB_POPULAR_PATH = "popular";
        private final String TMDB_TOP_RATED_PATH = "top_rated";
        private final String TMDB_API_QUERY = "api_key";

        // Actually hidden in gradle.properties but not uploaded to GitHub because of the file is
        // listed in .gitignore. Usually accessed as BuildConfig.API_KEY;
        private final String TMDB_API_KEY = "17a5cf0827856fce7a4338aa74d86d11";

        // Mem Vars
        private URL mBuiltUrl;

        FetchMoviesTask(@SortMethod int sortMethod) {
            // Build the URL for accessing TMDB API depending on what sort method the user has
            // selected

            String sortPath;
            if (sortMethod == POPULAR) {
                sortPath = TMDB_POPULAR_PATH;
            } else {
                sortPath = TMDB_TOP_RATED_PATH;
            }

            Uri uri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(TMBD_MOVIE_PATH)
                    .appendPath(sortPath)
                    .appendQueryParameter(TMDB_API_QUERY, TMDB_API_KEY)
                    .build();

            try {
                mBuiltUrl = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            if (mBuiltUrl == null) {
                // If URL was not built, then nothing to do
                return null;
            }

            // Init outside of try-catch block to be closed in finally block
            HttpURLConnection urlConnection = null;
            Scanner scanner = null;

            try {
                // Open connection and retrieve the HTML document
                urlConnection = (HttpURLConnection) mBuiltUrl.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                // Parse the document for the JSONObject
                scanner = new Scanner(inputStream);
                scanner.useDelimiter("\\A");

                return getMoviesFromJson(scanner.next());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                // Close open connections
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (scanner != null) {
                    scanner.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (movies != null && movies.size() > 0) {
                // Add movies to Adapter
                mAdapter.setMoviesList(movies);
            }
        }

        List<Movie> getMoviesFromJson(String jsonString) throws JSONException {
            final String TMDB_POSTER_BASE_PATH = "https://image.tmdb.org/t/p/w185";
            final String TMDB_BACKDROP_BASE_PATH = "https://image.tmdb.org/t/p/w780";

            // JSON parsing Strings
            String jsonResultsArray = "results";
            String jsonPoster = "poster_path";
            String jsonOverview = "overview";
            String jsonReleaseDate = "release_date";
            String jsonTitle = "title";
            String jsonBackdrop = "backdrop_path";

            String jsonId = "id";
            String jsonVoteCount = "vote_count";
            String jsonVoteAverage = "vote_average";

            // Convert String to JSONObject
            JSONObject movieJson = new JSONObject(jsonString);

            JSONArray resultsArray = movieJson.getJSONArray(jsonResultsArray);

            // Init List to hold Movie Objects
            List<Movie> movieList = new ArrayList<>();

            // Iterate, create Movie Objects, and add them to the List
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject movieObject = resultsArray.getJSONObject(i);

                Movie movie = new Movie();
                movie.setPosterPath(TMDB_POSTER_BASE_PATH + movieObject.getString(jsonPoster));
                movie.setOverview(movieObject.getString(jsonOverview));
                movie.setReleaseDate(movieObject.getString(jsonReleaseDate));
                movie.setTitle(movieObject.getString(jsonTitle));
                movie.setBackdropPath(TMDB_BACKDROP_BASE_PATH + movieObject.getString(jsonBackdrop));
                movie.setId(movieObject.getInt(jsonId));
                movie.setVoteCount(movieObject.getInt(jsonVoteCount));
                movie.setVoteAverage(movieObject.getDouble(jsonVoteAverage));

                movieList.add(movie);
            }


            return movieList;
        }
    }
}
