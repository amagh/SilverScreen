package com.amagh.silverscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.sync.MovieSyncIntentService;
import com.amagh.silverscreen.sync.MovieSyncUtils;

public class MovieListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // **Constants** //
    private final String TAG = MovieListActivity.class.getSimpleName();
    private final static int MOVIE_POSTER_LOADER_ID = 8323;
    private static final String SORT_DIALOG = "sort_dialog";

    // Column projection
    public static final String[] MOVIE_POSTER_PROJECT = new String[] {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVG
    };

    public static final int IDX_MOVIE_ID = 0;
    public static final int IDX_POSTER_PATH = 1;
    public static final int IDX_POPULARITY = 2;
    public static final int IDX_RATING = 3;

    // **Mem Vars** //
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
        MovieSyncUtils.initialize(this);

        // Initialize the CursorLoader
        getSupportLoaderManager().initLoader(MOVIE_POSTER_LOADER_ID, null, this);

        // Show mProgressBar
        showLoading();
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
//                AlertDialog dialog = buildSortDialog();
                SortDialog dialog = new SortDialog();

                // Set the PositiveClickListener
                dialog.setPositiveClickHandler(new SortDialog.PositiveClickHandler() {
                    @Override
                    public void onPositiveClick() {
                        // Restart the Loader with the new sort order
                        MovieListActivity.this.getSupportLoaderManager()
                                .restartLoader(MOVIE_POSTER_LOADER_ID, null, MovieListActivity.this);
                    }
                });

                // Show the Dialog when the menu item is selected
                dialog.show(getSupportFragmentManager(), SORT_DIALOG);

                return true;
            }

            case R.id.action_favorite: {
                // Check whether currently filtering for favorites only
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();

                boolean favoritesOnly = prefs.getBoolean(
                        getString(R.string.pref_favorite_key),
                        getResources().getBoolean(R.bool.pref_favorite_default)
                );

                // Toggle the filter
                editor.putBoolean(
                        getString(R.string.pref_favorite_key),
                        !favoritesOnly
                );

                editor.apply();

                // Restart the Loader with the new filter options
                getSupportLoaderManager().restartLoader(MOVIE_POSTER_LOADER_ID, null, this);
            }

            default: return super.onOptionsItemSelected(item);
        }

    }

    private final MovieAdapter.MovieClickHandler mMovieClickHandler = new MovieAdapter.MovieClickHandler() {
        @Override
        public void onMovieClick(int movieId) {
            // Build an explicit Intent to launch MovieDetailsActivity
            Intent intent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);

            // Create the URI for loading a linked movies and genres table
            Uri movieUri = MovieContract.LinkGenresMovies.CONTENT_URI.buildUpon()
                    .appendPath(MovieContract.PATH_MOVIES)
                    .appendPath(Integer.toString(movieId))
                    .build();

            // Add the URI for the movie to the Intent
            intent.setData(movieUri);

            // Start MovieDetailsActivity
            startActivity(intent);
        }
    };

    /**
     * Shows the mProgressBar
     */
    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hides mProgressBar
     */
    private void hideLoading() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve user's sort and filter preference from SharedPreferences
        String sortMethod = prefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity));

        boolean favoritesOnly = prefs.getBoolean(
                getString(R.string.pref_favorite_key),
                getResources().getBoolean(R.bool.pref_favorite_default)
        );

        // Create parameters for filtering
        String selection = null;
        String[] selectionArgs = null;

        if (favoritesOnly) {
            selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
            selectionArgs = new String[] {"1"};
        }

        // Create String to be used for sortOrder when querying the database
        String sortOrder;

        if (sortMethod.equals(getString(R.string.pref_sort_popularity))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else {
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVG + " DESC";
        }

        return new CursorLoader(
                this,
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_POSTER_PROJECT,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the loaded Cursor into mAdapter
        mAdapter.swapCursor(data);

        // Hide the loading icon if movies have been loaded
        if (data != null && data.moveToFirst()) hideLoading();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
