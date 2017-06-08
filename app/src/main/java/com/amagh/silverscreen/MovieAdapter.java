package com.amagh.silverscreen;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amagh.silverscreen.data.MovieContract;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by hnoct on 6/1/2017.
 */

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    // **Constants** //
    private final String TAG = MovieAdapter.class.getSimpleName();

    // **Mem Vars** //
    private Cursor mCursor;
    private final MovieClickHandler mClickHandler;
    private int posterHeight = 0;

    MovieAdapter(MovieClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    void swapCursor(Cursor newCursor) {
        // Set mem var to reference newCursor
        mCursor = newCursor;

        if (newCursor != null && newCursor.getCount() > 0) {
            // If newCursor has count greater than zero, notify Adapter to change in data
            notifyDataSetChanged();
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Init the LayoutInflater and inflate the list item layout
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_movie, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        // Set the height of the ViewHolder's ImageView
        if (posterHeight != 0) {
            ViewGroup.LayoutParams params = holder.mPosterImageView.getLayoutParams();
            params.height = posterHeight;

            holder.mPosterImageView.setLayoutParams(params);
        }

        // Retrieve the movie information
        mCursor.moveToPosition(position);
        String posterPath = mCursor.getString(MovieListActivity.IDX_POSTER_PATH);

        // Load the poster image into the ViewHolder's ImageView
        Glide.with(holder.itemView.getContext())
                .load(posterPath)
                .into(holder.mPosterImageView);

        // Get the height that will be used to set the height of all ViewHolders' ImageViews
        if (posterHeight == 0) {
            posterHeight = holder.mPosterImageView.getHeight();
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }

        return 0;
    }

    interface MovieClickHandler {
        void onMovieClick(Uri movieUri);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Mem Vars
        private final ImageView mPosterImageView;
//        private final TextView mRatingTextView;
//        private final TextView mReviewsTextView;

        MovieViewHolder(View view) {
            super(view);

            // Obtain references to the respective Views
            mPosterImageView = (ImageView) view.findViewById(R.id.movie_item_poster_iv);
//            mRatingTextView = (TextView) view.findViewById(R.id.movie_item_rating_tv);
//            mReviewsTextView = (TextView) view.findViewById(R.id.movie_item_review_tv);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Retrieve the movieId for the clicked ViewHolder
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);

            // Create a URI utilizing the movieId
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(mCursor.getString(MovieListActivity.IDX_MOVIE_ID))
                    .build();

            // Pass the URI to the registered listener
            mClickHandler.onMovieClick(movieUri);
        }
    }
}
