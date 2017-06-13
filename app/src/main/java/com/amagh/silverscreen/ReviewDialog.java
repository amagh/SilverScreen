package com.amagh.silverscreen;

import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;

import com.amagh.silverscreen.databinding.DialogReviewBinding;

import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_INDEX.IDX_REVIEW_AUTHOR;
import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_INDEX.IDX_REVIEW_CONTENT;
import static com.amagh.silverscreen.MovieDetailsActivity.REVIEWS_PROJECTION;

/**
 * Created by hnoct on 6/12/2017.
 */

public class ReviewDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // **Constants** //
    private static final int REVIEW_LOADER = 5653;

    // **Member Variables** //
    private Uri mUri;
    private Cursor mCursor;
    private DialogReviewBinding mBinding;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Setup DataBinding
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_review, null, false);

        // Setup the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mBinding.getRoot());

        // Load the data for the review
        ((AppCompatActivity) getActivity()).getSupportLoaderManager().restartLoader(REVIEW_LOADER, null, this);

        return builder.create();
    }

    /**
     * Binds data to the Views
     */
    void bind() {
        // Retrieve review information
        String content = mCursor.getString(IDX_REVIEW_CONTENT);
        String author = mCursor.getString(IDX_REVIEW_AUTHOR);

        // Bind data to Views
        mBinding.dialogReviewContentTv.setText(content);
        mBinding.dialogReviewAuthorTv.setText(author);
    }

    public void setData(Uri reviewUri) {
        mUri = reviewUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mUri,
                REVIEWS_PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Check to ensure Cursor is valid
        if (data != null && data.moveToFirst()) {
            mCursor = data;

            // Bind data to Views
            bind();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Stub. Nothing needs to be done.
    }
}
