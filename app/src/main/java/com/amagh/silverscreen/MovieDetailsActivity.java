package com.amagh.silverscreen;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.amagh.silverscreen.databinding.ActivityMovieDetailsBinding;
import com.amagh.silverscreen.sync.MovieReviewsSyncTask;
import com.amagh.silverscreen.sync.MovieTrailersSyncTask;
import com.amagh.silverscreen.utilities.MovieUtils;
import com.amagh.silverscreen.utilities.ViewUtils;
import com.bumptech.glide.Glide;

import static com.amagh.silverscreen.data.MovieContract.*;

public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks {
    // **Constants** //
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // Loader IDs
    private static final int MOVIE_DETAILS_LOADER = 6984;
    private static final int TRAILERS_LOADER = 7531;
    private static final int TRAILERS_SYNC_LOADER = 2313;
    private static final int REVIEWS_LOADER = 6854;
    private static final int REVIEWS_SYNC_LOADER = 7887;

    // Dialog TAG
    private static final String REVIEW_DIALOG = "review_dialog";

    // Column Projection
    public static final String[] DETAILS_PROJECTION = new String[] {
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_BACKDROP_PATH,
            MovieEntry.COLUMN_VOTE_AVG,
            MovieEntry.COLUMN_SYNOPSIS,
            GenreEntry.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID,
            GenreEntry.COLUMN_GENRE
    };

    private static final int IDX_MOVIE_ID                   = 0;
    private static final int IDX_MOVITE_TITLE               = 1;
    private static final int IDX_MOVIE_RELEASE_DATE         = 2;
    private static final int IDX_MOVIE_POSTER_PATH          = 3;
    private static final int IDX_MOVIE_BACKDROP_PATH        = 4;
    private static final int IDX_MOVIE_VOTE_AVG             = 5;
    private static final int IDX_MOVIE_SYNPOSIS             = 6;
    private static final int IDX_MOVIE_GENRE_ID             = 7;
    private static final int IDX_MOVIE_GENRE                = 8;

    public static final String[] TRAILERS_PROJECTION = new String[] {
            MovieEntry.COLUMN_MOVIE_ID,
            TrailerEntry.COLUMN_NAME,
            TrailerEntry.COLUMN_TYPE,
            TrailerEntry.COLUMN_VIDEO_PATH,
            TrailerEntry.COLUMN_THUMBNAIL_PATH
    };

    public interface TRAILER_INDEX {
        int IDX_TRAILER_NAME                                = 1;
        int IDX_TRAILER_TYPE                                = 2;
        int IDX_TRAILER_VIDEO_PATH                          = 3;
        int IDX_TRAILER_THUMBNAIL_PATH                      = 4;
    }

    public static final String[] REVIEWS_PROJECTION = new String[] {
            MovieEntry.COLUMN_MOVIE_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT,
            ReviewEntry.COLUMN_REVIEW_ID
    };

    public interface REVIEWS_INDEX {
        int IDX_REVIEW_AUTHOR                               = 1;
        int IDX_REVIEW_CONTENT                              = 2;
        int IDX_REVIEW_ID                                   = 3;
    }

    // **Member Variables** //
    ActivityMovieDetailsBinding mBinding;

    private Uri mUri;
    private String[] genres;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private LinearLayoutManager mReviewLayoutManager;

    private int mReviewCount = -1;
    private int mReviewPosition = 0;

    private boolean trailersLoaded = false;
    private boolean reviewsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Retrieve the URI for the movie in the database passed through the Intent
        Intent intent = getIntent();
        if (intent.getData() != null) {
            mUri = intent.getData();
        }

        // Begin loading movie details from database
        getSupportLoaderManager().initLoader(MOVIE_DETAILS_LOADER, null, this);
        getSupportLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER, null, this);

        // Add a Listener to manage the animations of the favorite icon as the user scrolls
        mBinding.appBar.addOnOffsetChangedListener(new AppBarScrollListener(mBinding.movieDetailsPosterTopIv) {
            @Override
            public void onCollapsed() {
                // Hide the poster ImageView anchored to the CollapsingToolbar and show the poster
                // ImageView that in the content layout
                mBinding.movieDetailsPosterTopIv.setVisibility(View.INVISIBLE);
                mBinding.movieDetailsContent.movieDetailsPosterBottomIv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollUp() {
            }

            @Override
            public void onScrollDown() {
                // Hide the poster ImageView in the content layout and show the poster ImageView
                // anchored to the CollapsingToolbar
                mBinding.movieDetailsPosterTopIv.setVisibility(View.VISIBLE);
                mBinding.movieDetailsContent.movieDetailsPosterBottomIv.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPosterAtTop() {
                // Animate the favorite star to return to its original position
                ObjectAnimator animY = ObjectAnimator.ofFloat(
                        mBinding.movieDetailsContent.movieDetailsFavoriteLl,
                        "translationX",
                        0
                );
                animY.setDuration(100);
                animY.start();
            }

            @Override
            public void onPosterAtBottom() {
                // Animate the favorite star to move to the right half the width of the poster so
                // it is centered in the area between the poster and the right edge of the Activity
                float posterWidth = mBinding.movieDetailsPosterTopIv.getWidth();
                ObjectAnimator animY = ObjectAnimator.ofFloat(
                        mBinding.movieDetailsContent.movieDetailsFavoriteLl,
                        "translationX",
                        posterWidth / 2
                );
                animY.setDuration(100);
                animY.start();
            }
        });

        // Set up the RecyclerView for the trailers of the movie
        setupTrailers();

        // Set up the RecyclerView for the reviews of the movie
        setupReviews();

        // Set a Runnable to work after Views have been laid out so their heights can be calculated
        mBinding.appBar.post(applyPosterMargin);
    }

    /**
     * Initialize the mTrailerAdapter and the LayoutManager for the RecyclerView showing trailers,
     * then set the RecyclerView to use them.
     */
    private void setupTrailers() {
        // Init mTrailerAdapter
        mTrailerAdapter = new TrailerAdapter(trailerClickHandler);
        mBinding.movieDetailsContent.movieDetailsTrailersRv.setAdapter(mTrailerAdapter);

        // Init LayoutManager
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieDetailsContent.movieDetailsTrailersRv.setLayoutManager(llm);

        // Disable nested scrolling
        mBinding.movieDetailsContent.movieDetailsTrailersRv.setNestedScrollingEnabled(false);
    }

    /**
     * Initialize mReviewAdapter and the LayoutManager for the RecyclerView showing reviews. Then
     * the RecyclerView to use them.
     */
    private void setupReviews() {
        // Init mReviewAdapter
        mReviewAdapter = new ReviewAdapter(reviewClickHandler);
        mBinding.movieDetailsContent.movieDetailsReviewsRv.setAdapter(mReviewAdapter);

        // Init LayoutManager
        mReviewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieDetailsContent.movieDetailsReviewsRv.setLayoutManager(mReviewLayoutManager);

        // Init SnapHelper to ensure discrete scrolling
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.movieDetailsContent.movieDetailsReviewsRv);

        // Disable nested scrolling
        mBinding.movieDetailsContent.movieDetailsReviewsRv.setNestedScrollingEnabled(false);

        // Add OnScrollListener to show/hide the left/right chevrons
        mBinding.movieDetailsContent.movieDetailsReviewsRv.addOnScrollListener(
                new RecyclerViewOnScrollListener(mReviewLayoutManager) {
            @Override
            public void onScrolled(int position) {
                // Set member variable to new scroll position
                mReviewPosition = position;

                // Get total number of reviews
                if (mReviewCount == -1) {
                    mReviewCount = mReviewAdapter.getItemCount();
                }

                // Show/hide the visibility of the chevrons depending on the relative position of
                // visible review item
                if (mReviewCount == 1) {
                    mBinding.movieDetailsContent.movieDetailsReviewLeftChevron.setVisibility(View.INVISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewRightChevron.setVisibility(View.INVISIBLE);
                } else if (position > 0 && position < mReviewCount - 1){
                    mBinding.movieDetailsContent.movieDetailsReviewLeftChevron.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewRightChevron.setVisibility(View.VISIBLE);
                } else if (position == 0) {
                    mBinding.movieDetailsContent.movieDetailsReviewLeftChevron.setVisibility(View.INVISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewRightChevron.setVisibility(View.VISIBLE);
                } else if (position == mReviewCount - 1) {
                    mBinding.movieDetailsContent.movieDetailsReviewLeftChevron.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewRightChevron.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * Scrolls to the next review if there is one.
     *
     * @param view Clicked View
     */
    public void scrollRight(View view) {
        if (mReviewPosition < mReviewCount - 1) {
            mReviewLayoutManager.smoothScrollToPosition(
                    mBinding.movieDetailsContent.movieDetailsReviewsRv,
                    null,
                    mReviewPosition + 1
            );
        }
    }

    /**
     * Scrolls  to the previous review if there is one.
     * @param view Clicked View
     */
    public void scrollLeft(View view) {
        if (mReviewPosition > 0) {
            mReviewLayoutManager.smoothScrollToPosition(
                    mBinding.movieDetailsContent.movieDetailsReviewsRv,
                    null,
                    mReviewPosition - 1
            );
        }
    }

    /**
     * Use DataBinding feature to set the movie information to the Views
     *
     * @param cursor Cursor pointing to entry describing the selected movie
     */
    private void bindData(Cursor cursor) {
        // Retrieve the movie information
        String movieTitle = cursor.getString(IDX_MOVITE_TITLE);
        String releaseDate = MovieUtils.formatDate(cursor.getString(IDX_MOVIE_RELEASE_DATE));
        String voteAverage = getString(R.string.format_rating, cursor.getDouble(IDX_MOVIE_VOTE_AVG));
        String synopsis = cursor.getString(IDX_MOVIE_SYNPOSIS);

        // Retrieve paths for images associated with the movie
        String posterPath = cursor.getString(IDX_MOVIE_POSTER_PATH);
        String backdropPath = cursor.getString(IDX_MOVIE_BACKDROP_PATH);

        // Build the String describing the genres of the movie
        String genresString = TextUtils.join(", ", genres);

        // Populate the Views
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(movieTitle);

        mBinding.movieDetailsContent.movieDetailsReleaseDateTv.setText(releaseDate);
        mBinding.movieDetailsContent.movieDetailsRatingTv.setText(voteAverage);
        mBinding.movieDetailsContent.movieDetailsOverviewTv.setText(synopsis);
        mBinding.movieDetailsContent.movieDetailsGenreTv.setText(genresString);

        // Load the images
        Glide.with(this)
                .load(posterPath)
                .into(mBinding.movieDetailsPosterTopIv);

        Glide.with(this)
                .load(posterPath)
                .into(mBinding.movieDetailsContent.movieDetailsPosterBottomIv);

        Glide.with(this)
                .load(backdropPath)
                .into(mBinding.movieDetailsBackdropIv);



        mBinding.executePendingBindings();
    }

    Runnable applyPosterMargin = new Runnable() {
        @Override
        public void run() {
            // Retrieve the LayoutParams
            CoordinatorLayout.LayoutParams params =
                    (CoordinatorLayout.LayoutParams) mBinding.movieDetailsPosterTopIv.getLayoutParams();

            // Calculate the top margin (Should be equal to 'StatusBar Height' + 'Toolbar Height' +
            // 'Activity Margin'
            params.setMargins((int) ViewUtils.convertDpToPixels(16), (int) ViewUtils.calculatePosterTopMargin(getWindow(), mBinding.toolbar, 16), 0, 0);
            mBinding.movieDetailsPosterTopIv.setLayoutParams(params);
        }
    };

    ReviewAdapter.ReviewClickHandler reviewClickHandler = new ReviewAdapter.ReviewClickHandler() {
        @Override
        public void onClickReview(String reviewId) {
            // Build the URI for the movie review clicked
            Uri reviewUri = ReviewEntry.CONTENT_URI.buildUpon()
                    .appendPath(reviewId)
                    .build();

            // Show a Dialog with the full contents of the review
            showReviewDialog(reviewUri);
        }
    };

    TrailerAdapter.TrailerClickHandler trailerClickHandler = new TrailerAdapter.TrailerClickHandler() {
        @Override
        public void onTrailerClicked(String trailerPath) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerPath));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    };

    /**
     * Creates and shows a ReviewDialog with the full contents of the clicked review
     *
     * @param reviewUri URI pointing to the review in the database
     */
    private void showReviewDialog(Uri reviewUri) {
        // Init the ReviewDialog
        ReviewDialog dialog = new ReviewDialog();

        // Set the URI of the review
        dialog.setData(reviewUri);

        // Show
        dialog.show(getFragmentManager(), REVIEW_DIALOG);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // Variables for building the CursorLoader
        Uri uri;
        String[] projection;
        String sortOrder = null;

        switch (id) {
            case MOVIE_DETAILS_LOADER: {
                uri = mUri;
                projection = DETAILS_PROJECTION;

                // Sort by genre alphabetically
                sortOrder = GenreEntry.COLUMN_GENRE + " ASC";
                break;
            }

            case TRAILERS_LOADER: {
                // Retrieve the movieId and build the URI to load its trailers
                String movieIdString = mUri.getLastPathSegment();
                uri = TrailerEntry.CONTENT_URI.buildUpon()
                        .appendPath(PATH_MOVIES)
                        .appendPath(movieIdString)
                        .build();

                projection = TRAILERS_PROJECTION;
                break;
            }

            case REVIEWS_LOADER: {
                // Retrieve the movieId and build the URI to load its reviews
                String movieIdString = mUri.getLastPathSegment();
                uri = ReviewEntry.CONTENT_URI.buildUpon()
                        .appendPath(PATH_MOVIES)
                        .appendPath(movieIdString)
                        .build();

                projection = REVIEWS_PROJECTION;
                break;
            }

            case TRAILERS_SYNC_LOADER: {
                return new AsyncTaskLoader<Void>(this) {
                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();

                        forceLoad();
                    }

                    @Override
                    public Void loadInBackground() {
                        // Load the Trailers for the movie
                        MovieTrailersSyncTask.syncTrailers(
                                MovieDetailsActivity.this,
                                Integer.parseInt(mUri.getLastPathSegment())
                        );

                        // Set boolean to prevent syncing trailers more than once in the case that
                        // there are no trailers for the movie
                        trailersLoaded = true;
                        return null;
                    }
                };
            }

            case REVIEWS_SYNC_LOADER: {
                return new AsyncTaskLoader<Void>(this) {
                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();

                        forceLoad();
                    }

                    @Override
                    public Void loadInBackground() {
                        // Load the reviews for the movie
                        MovieReviewsSyncTask.syncTrailers(
                                MovieDetailsActivity.this,
                                Integer.parseInt(mUri.getLastPathSegment())
                        );

                        // Set boolean to prevent syncing reviews more than once in the case that
                        // there are no reviews for the movie
                        reviewsLoaded = true;
                        return null;
                    }


                };
            }

            default: throw new UnsupportedOperationException("Unknown LoaderID: " + id);
        }

        // Build and return the CursorLoader
        return new CursorLoader(
                this,
                uri,
                projection,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case MOVIE_DETAILS_LOADER: {
                // Cast the returned data Object to Cursor
                Cursor cursor = (Cursor) data;

                // Check to ensure it is a valid Cursor
                if (cursor != null && cursor.moveToFirst()) {
                    // Retrieve the genres of the movie
                    genres = new String[cursor.getCount()];

                    for (int i = 0; i < cursor.getCount(); i++) {
                        genres[i] = cursor.getString(IDX_MOVIE_GENRE);
                        cursor.moveToNext();
                    }

                    // Move Cursor to a valid row and bind the data
                    cursor.moveToFirst();
                    bindData(cursor);
                }

                break;
            }

            case TRAILERS_LOADER: {
                // Cast the data Object to a Cursor
                Cursor cursor = (Cursor) data;

                // Check that trailers exists in the table
                if ((cursor == null || !cursor.moveToFirst()) && !trailersLoaded) {
                    // Trailer data has not been loaded. Start the AsyncTaskLoader to load the data
                    getSupportLoaderManager().initLoader(TRAILERS_SYNC_LOADER, null, this);
                } else if (cursor != null && cursor.moveToFirst()) {
                    mTrailerAdapter.swapCursor(cursor);

                    // Show the trailer associated Views if there are trailers to display
                    mBinding.movieDetailsContent.movieDetailsTrailersRv.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsTrailersTitleTv.setVisibility(View.VISIBLE);
                }

                break;
            }

            case REVIEWS_LOADER: {
                // Cast the data Object to a Cursor
                Cursor cursor = (Cursor) data;

                // Check that reviews exist in the table
                if ((cursor == null || !cursor.moveToFirst()) && !reviewsLoaded) {
                    // Review data has not been loaded. Start the AsyncTaskLoader to load the data
                    getSupportLoaderManager().initLoader(REVIEWS_SYNC_LOADER, null, this);
                } else if (cursor != null && cursor.moveToFirst()) {
                    mReviewAdapter.swapCursor(cursor);

                    // Hide the reviews associated Views if there are no reviews to display
                    mBinding.movieDetailsContent.movieDetailsReviewsRv.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewsTitleTv.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewLeftChevron.setVisibility(View.VISIBLE);
                    mBinding.movieDetailsContent.movieDetailsReviewRightChevron.setVisibility(View.VISIBLE);
                }

                break;
            }

            case TRAILERS_SYNC_LOADER: {
                // Restart the CursorLoader for the trailers because of a difference in URIs
                getSupportLoaderManager().restartLoader(TRAILERS_LOADER, null, this);

                break;
            }

            case REVIEWS_SYNC_LOADER: {
                // Restart the CursorLoader for the reviews because of a difference in URIs
                getSupportLoaderManager().restartLoader(REVIEWS_LOADER, null, this);

                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // Stub. Nothing needs to be done on reset
    }
}
