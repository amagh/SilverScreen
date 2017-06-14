package com.amagh.silverscreen.adapters;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amagh.silverscreen.R;
import com.amagh.silverscreen.databinding.ListItemTrailerBinding;
import com.bumptech.glide.Glide;

import static com.amagh.silverscreen.MovieDetailsActivity.TRAILER_INDEX.IDX_TRAILER_NAME;
import static com.amagh.silverscreen.MovieDetailsActivity.TRAILER_INDEX.IDX_TRAILER_THUMBNAIL_PATH;
import static com.amagh.silverscreen.MovieDetailsActivity.TRAILER_INDEX.IDX_TRAILER_VIDEO_PATH;

/**
 * Created by hnoct on 6/9/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    // **Constants** //

    // **Member Variables **//
    private Cursor mCursor;
    private final TrailerClickHandler mTrailerClickHandler;

    public TrailerAdapter(TrailerClickHandler trailerClickHandler) {
        mTrailerClickHandler = trailerClickHandler;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Init LayoutInflater to pass to DataBindingUtils
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Init the DataBinding
        ListItemTrailerBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.list_item_trailer, parent, false);

        return new TrailerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
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

    public interface TrailerClickHandler {
        void onTrailerClicked(String trailerPath);
    }

    /**
     * ViewHolder pattern for trailers
     */
    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // **Member Variables** //
        final ListItemTrailerBinding mBinding;

        public TrailerViewHolder(ListItemTrailerBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            mBinding = binding;
        }

        private void bind(Cursor cursor) {
            // Retrieve trailer info from Cursor
            int position = getAdapterPosition();
            cursor.moveToPosition(position);

            String trailerThumbPath = cursor.getString(IDX_TRAILER_THUMBNAIL_PATH);
            String trailerName = cursor.getString(IDX_TRAILER_NAME);

            // Bind data to Views
            mBinding.listTrailerTitleTv.setText(trailerName);

            Glide.with(mBinding.getRoot().getContext())
                    .load(trailerThumbPath)
                    .into(mBinding.listTrailerThumbIv);

            // Force bindings to run immediately
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            // Retrieve the path for the trailer video
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);

            String videoPath = mCursor.getString(IDX_TRAILER_VIDEO_PATH);

            mTrailerClickHandler.onTrailerClicked(videoPath);
        }
    }
}
