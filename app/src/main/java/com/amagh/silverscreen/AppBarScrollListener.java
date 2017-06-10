package com.amagh.silverscreen;

import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by hnoct on 6/9/2017.
 */

public abstract class AppBarScrollListener implements AppBarLayout.OnOffsetChangedListener {
    public int mScrollState = ScrollState.EXPANDED;
    public int mPosterState = PosterState.AT_TOP;
    private int mPreviousY;
    private ImageView mPosterView;

    public interface ScrollState {
        int EXPANDED = 0;
        int COLLAPSED = 1;
        int SCROLL_UP= 2;
        int SCROLL_DOWN = 3;
    }

    public interface PosterState {
        int AT_TOP = 4;
        int AT_BOTTOM = 5;
    }

    /**
     * Constructor taking in the ImageView anchored to the CollapsingToolbar
     *
     * @param posterImageView Poster ImageView anchored to the CollapsingToolbar
     */
    public AppBarScrollListener(ImageView posterImageView) {
        // Set member var
        mPosterView = posterImageView;

        // Init the Y-location of the ImageView
        int[] posterLocation = new int[2];
        posterImageView.getLocationOnScreen(posterLocation);

        mPreviousY = posterLocation[1];
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            // No offset - completely expanded
            mScrollState = ScrollState.EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            // Offset is equal or greater than totalScrollRange of the AppBar - completely collapsed
            if (mScrollState != ScrollState.COLLAPSED) {
                // Inform listener of the change in the ScrollState
                onCollapsed();
            }
            mScrollState = ScrollState.COLLAPSED;
        } else {
            // Offset is somewhere in between - user is scrolling. Inform the Observer of change in
            // ScrollState
            if (mScrollState == ScrollState.COLLAPSED || mScrollState == ScrollState.SCROLL_UP) {
                onScrollDown();
                mScrollState = ScrollState.SCROLL_DOWN;

            } else if (mScrollState == ScrollState.EXPANDED || mScrollState == ScrollState.SCROLL_DOWN) {
                onScrollUp();
                mScrollState = ScrollState.SCROLL_UP;
            }
        }

        // Calculate the Y-position of the poster ImageView to see if it has moved
        int[] posterLocation = new int[2];
        mPosterView.getLocationOnScreen(posterLocation);

        if (posterLocation[1] == mPreviousY && mPosterState != PosterState.AT_BOTTOM &&
                mScrollState != ScrollState.EXPANDED) {
            // Poster stopped moving - it is at the bottom
            mPosterState = PosterState.AT_BOTTOM;
            onPosterAtBottom();
        } else if (posterLocation[1] != mPreviousY && mPosterState != PosterState.AT_TOP) {
            // Poster has begun moving - it has reached the top
            mPosterState = PosterState.AT_TOP;
            onPosterAtTop();
        }

        mPreviousY = posterLocation[1];
    }

    public abstract void onCollapsed();

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public abstract void onPosterAtTop();

    public abstract void onPosterAtBottom();
}
