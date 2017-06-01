package com.amagh.silverscreen.silverscreen;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by hnoct on 6/1/2017.
 */

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    // Constants
    private final String TAG = MovieAdapter.class.getSimpleName();

    // Mem Vars
    private List<Movie> mMoviesList;
    private final MovieClickHandler mClickHandler;
    private int posterHeight = 0;

    MovieAdapter(MovieClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    void setMoviesList(List<Movie> movies) {
        // Set the mem var
        mMoviesList = movies;

        notifyDataSetChanged();
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

        // Retrieve the Movie from the List
        Movie movie = mMoviesList.get(position);

        // Load the poster image into the ViewHolder's ImageView
        Glide.with(holder.itemView.getContext())
                .load(movie.getPosterPath())
                .into(holder.mPosterImageView);

        // Get the height that will be used to set the height of all ViewHolders' ImageViews
        if (posterHeight == 0) {
            posterHeight = holder.mPosterImageView.getHeight();
        }
    }

    @Override
    public int getItemCount() {
        if (mMoviesList != null) {
            return mMoviesList.size();
        }

        return 0;
    }

    interface MovieClickHandler {
        void onMovieClick(Movie movie);
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
            int position = getAdapterPosition();
            Movie movie = mMoviesList.get(position);

            mClickHandler.onMovieClick(movie);
        }
    }
}
