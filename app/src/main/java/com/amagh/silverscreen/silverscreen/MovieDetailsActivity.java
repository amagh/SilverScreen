package com.amagh.silverscreen.silverscreen;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.amagh.silverscreen.silverscreen.MovieDetailsActivity.EXTRAS.EXTRA_MOVIE;

public class MovieDetailsActivity extends AppCompatActivity {
    // Constants
    private final String TAG = MovieDetailsActivity.class.getSimpleName();
    private final boolean futureRelease = false;

    // Mem Vars
    Movie mMovie;
    ImageView mPosterImageView;
    ImageView mBackdropImageView;
    TextView mReleaseDateTextView;
    TextView mRatingTextView;
    TextView mOverviewTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Obtain references to Views
        mPosterImageView = (ImageView) findViewById(R.id.movie_details_poster_iv);
        mBackdropImageView = (ImageView) findViewById(R.id.movie_details_backdrop_iv);
        mReleaseDateTextView = (TextView) findViewById(R.id.movie_details_release_date_tv);
        mRatingTextView = (TextView) findViewById(R.id.movie_details_ratings_tv);
        mOverviewTextView = (TextView) findViewById(R.id.movie_details_overview_tv);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieve the Movie Object passed through the Intent
        Intent intent = getIntent();
        if (intent.getParcelableExtra(EXTRA_MOVIE) != null) {
            mMovie = intent.getParcelableExtra(EXTRA_MOVIE);
        } else {
            Log.d(TAG, "No Movie passed to Activity");
            finish();
        }

        // Set the Toolbar title to the Movie title
        setToolbarTitle();

        if (futureRelease) {
            loadBackdropImage();
        } else {
            // Load the Poster Image
            loadPosterImage();
        }

        // Set the text into the TextViews
        loadTextViews();
    }

    private void loadBackdropImage() {
        Glide.with(this)
                .load(mMovie.getBackdropPath())
                .into(mBackdropImageView);
    }

    /**
     * Utilizes Glide to load the image into mPosterImageView
     */
    private void loadPosterImage() {
        Glide.with(this)
                .load(mMovie.getPosterPath())
                .into(mPosterImageView);
    }

    /**
     * Retrieves the respective text for each TextView and sets the text
     */
    private void loadTextViews() {
        // Format and set the text for the release date
        mReleaseDateTextView.setText(
                getString(R.string.format_release_date, formatDate(mMovie.getReleaseDate()))
        );

        // Format and set the text for the movie rating
        mRatingTextView.setText(getString(R.string.format_rating, mMovie.getVoteAverage()));

        mOverviewTextView.setText(mMovie.getOverview());
    }

    /**
     * Converts the time utilized by TheMovieDB.org to an easier to read date format
     *
     * @param stringDate Date in String format (yyyy-MM-dd)
     * @return String of the date in MMM d, yyyy format
     */
    private String formatDate(String stringDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(stringDate);
            sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sets the Title of the Toolbar to that of the movie
     */
    private void setToolbarTitle() {
        // Retrieve the movie title and set it as the title
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(mMovie.getTitle());
    }

    // Standardized Strings for passing extra information in the Intent
    interface EXTRAS {
        String EXTRA_MOVIE = "movie";
    }


}
