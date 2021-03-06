package com.amagh.silverscreen.adapters;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amagh.silverscreen.R;
import com.amagh.silverscreen.databinding.ListItemReviewBinding;

import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_INDEX.IDX_REVIEW_AUTHOR;
import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_INDEX.IDX_REVIEW_CONTENT;
import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_INDEX.IDX_REVIEW_ID;

/**
 * Created by hnoct on 6/12/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    // **Constants** //

    // **Member Variables** //
    private Cursor mCursor;
    private final ReviewClickHandler mReviewClickHandler;

    public ReviewAdapter(ReviewClickHandler reviewClickHandler) {
        mReviewClickHandler = reviewClickHandler;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Init LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Init DataBinding
        ListItemReviewBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.list_item_review, parent, false);

        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        // Bind data to Views
        holder.bind(mCursor);
    }

    @Override
    public int getItemCount() {
        // If Cursor is not null, return the number of rows contained
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * Sets the Cursor to be used by the Adapter to bind data to the Views
     * @param newCursor Cursor with trailer information
     */
    public void swapCursor(Cursor newCursor) {
        // Set the new Cursor to the mem var
        mCursor = newCursor;

        if (newCursor != null && newCursor.getCount() > 0) {
            // If Cursor is not null and contains entries, then notify the Adapter of the change in
            // data
            notifyDataSetChanged();
        }
    }

    public interface ReviewClickHandler {
        void onClickReview(String reviewId);
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final ListItemReviewBinding mBinding;

        public ReviewViewHolder(ListItemReviewBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            mBinding = binding;
        }

        private void bind(Cursor cursor) {
            // Retrieve review information from database
            int position = getAdapterPosition();
            cursor.moveToPosition(position);

            String author = cursor.getString(IDX_REVIEW_AUTHOR);
            String content = cursor.getString(IDX_REVIEW_CONTENT);

            // Bind data to Views
            mBinding.listReviewAuthorTv.setText(author);
            mBinding.listReviewContentTv.setText(content);

            // Force data to bind immediately
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            // Get the Id of the review clicked
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);

            String reviewId= mCursor.getString(IDX_REVIEW_ID);

            mReviewClickHandler.onClickReview(reviewId);
        }
    }
}
