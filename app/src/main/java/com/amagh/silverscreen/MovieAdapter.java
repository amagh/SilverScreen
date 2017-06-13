package com.amagh.silverscreen;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amagh.silverscreen.data.MovieContract;
import com.amagh.silverscreen.databinding.ListItemMovieBinding;
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
        ListItemMovieBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_movie, parent, false);

        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(mCursor);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }

        return 0;
    }

    interface MovieClickHandler {
        void onMovieClick(int movieId);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Mem Vars
        private ListItemMovieBinding mBinding;

        MovieViewHolder(ListItemMovieBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);

            mBinding = binding;
        }

        @Override
        public void onClick(View view) {
            // Retrieve the movieId for the clicked ViewHolder
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int movieId = mCursor.getInt(MovieListActivity.IDX_MOVIE_ID);

            // Pass the URI to the registered listener
            mClickHandler.onMovieClick(movieId);
        }

        /**
         * Bind data to Views
         */
        private void bind(Cursor cursor) {
            // Set the height of the ViewHolder's ImageView
            if (posterHeight != 0) {
                ViewGroup.LayoutParams params = mBinding.movieItemPosterIv.getLayoutParams();
                params.height = posterHeight;

                mBinding.movieItemPosterIv.setLayoutParams(params);
            }

            // Retrieve the poster location
            int position = getAdapterPosition();
            cursor.moveToPosition(position);

            String posterPath = cursor.getString(MovieListActivity.IDX_POSTER_PATH);

            // Bind the data to the View
            Glide.with(mBinding.getRoot().getContext())
                    .load(posterPath)
                    .into(mBinding.movieItemPosterIv);

            // Get the height that will be used to set the height of all ViewHolders' ImageViews
            if (posterHeight == 0) {
                posterHeight = mBinding.movieItemPosterIv.getHeight();
            }
        }
    }
}
