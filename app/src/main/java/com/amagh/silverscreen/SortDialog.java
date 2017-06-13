package com.amagh.silverscreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.amagh.silverscreen.databinding.DialogSortBinding;

/**
 * Created by Nocturna on 6/12/2017.
 */

public class SortDialog extends DialogFragment {
    // **Member Variables **//
    private DialogSortBinding mBinding;
    private String sortMethod;
    private PositiveClickHandler mPositiveClickHandler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get current sort preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortMethod = prefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity)
        );

        // Set up DataBinding
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_sort, null, false);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(mBinding.getRoot())
                .setTitle(getString(R.string.dialog_sort_title))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int i) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = prefs.edit();

                        // Check which RadioButton is checked and set the preference accordingly
                        if (mBinding.dialogPopularRb.isChecked()) {
                            if (sortMethod.equals(getString(R.string.pref_sort_popularity))) {
                                // Same sort method. Do nothing
                                return;
                            }

                            editor.putString(
                                    getString(R.string.pref_sort_key),
                                    getString(R.string.pref_sort_popularity)
                            );
                        } else if (mBinding.dialogTopRatedRb.isChecked()){
                            if (sortMethod.equals(getString(R.string.pref_sort_rating))) {
                                // Same sort method. Do nothing
                                return;
                            }

                            editor.putString(
                                    getString(R.string.pref_sort_key),
                                    getString(R.string.pref_sort_rating)
                            );
                        }

                        // Apply changes
                        editor.apply();

                        mPositiveClickHandler.onPositiveClick();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing. Just dismiss the Dialog
                    }
                });

        // Bind the data to the Views
        bind();

        return builder.create();
    }

    /**
     * Setter for the PositiveClickHandler
     *
     * @param positiveClickHandler PositiveClickHandler to register as the Observer
     */
    public void setPositiveClickHandler(PositiveClickHandler positiveClickHandler) {
        mPositiveClickHandler = positiveClickHandler;
    }

    /**
     * Binds data to the Views
     */
    private void bind() {
        // Check the RadiuButton depending on the sort-preference
        if (sortMethod.equals(getString(R.string.pref_sort_popularity))) {
            mBinding.dialogPopularRb.setChecked(true);
        } else if (sortMethod.equals(getString(R.string.pref_sort_rating))) {
            mBinding.dialogTopRatedRb.setChecked(true);
        }
    }

    public interface PositiveClickHandler {
        void onPositiveClick();
    }
}
