package com.amagh.silverscreen.adapters;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amagh.silverscreen.MovieListActivity;
import com.amagh.silverscreen.R;
import com.amagh.silverscreen.databinding.ListItemMovieBinding;
import com.bumptech.glide.Glide;

/**
 * Created by hnoct on 6/1/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    // **Mem Vars** //
    private Cursor mCursor;
    private final MovieClickHandler mClickHandler;
    private int posterHeight = 0;
    private boolean heightSet = false;

    public MovieAdapter(MovieClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public void swapCursor(Cursor newCursor) {
        // Set mem var to reference newCursor
        mCursor = newCursor;

        notifyDataSetChanged();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Init the LayoutInflater and inflate the list item layout
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemMovieBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_movie, parent, false);

        if (heightSet) {
            // Set the height of the ViewHolder's ImageView
            ViewGroup.LayoutParams params = binding.movieItemPosterIv.getLayoutParams();
            params.height = posterHeight;
            binding.movieItemPosterIv.setLayoutParams(params);

            heightSet = true;
//            mListener.onHeightLoaded();
        }
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

    public interface MovieClickHandler {
        void onMovieClick(int movieId);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Mem Vars
        private final ListItemMovieBinding mBinding;

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


            // Retrieve the poster location
            int position = getAdapterPosition();
            cursor.moveToPosition(position);

            String posterPath = cursor.getString(MovieListActivity.IDX_POSTER_PATH);

            // Bind the data to the View
            Glide.with(mBinding.getRoot().getContext())
                    .load(posterPath)
                    .into(mBinding.movieItemPosterIv);

            // Get the height that will be used to set the height of all ViewHolders' ImageViews
            if (!heightSet) {
                posterHeight = mBinding.movieItemPosterIv.getHeight();
            }
        }
    }
}
