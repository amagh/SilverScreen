package com.amagh.silverscreen;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by hnoct on 6/12/2017.
 */

public abstract class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
    // **Member Variables** //
    private LinearLayoutManager mLayoutManager;

    public RecyclerViewOnScrollListener(LinearLayoutManager linearLayoutManager) {
        super();
        mLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // Get the position of the visible review
        int position = mLayoutManager.findFirstCompletelyVisibleItemPosition();

        onScrolled(position);
    }

    public abstract void onScrolled(int position);
}
