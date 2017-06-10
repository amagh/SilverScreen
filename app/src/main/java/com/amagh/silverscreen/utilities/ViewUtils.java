package com.amagh.silverscreen.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

/**
 * Created by hnoct on 6/9/2017.
 */

public class ViewUtils {
    /**
     * Convert a number of pixels to its closest equivalent display independent pixels.
     *
     * @param pixels Number of pixels to convert to DIPs
     * @return Equivalent number of DIPs corresponding to the input pixels
     */
    public static float convertPixelsToDp(float pixels) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return pixels / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Convert a number of display independent pixels to pixels.
     *
     * @param dips Number of DIPs to convert to pixels
     * @return Equivalent number of pixels corresponding to the input DIPs
     */
    public static float convertDpToPixels(float dips) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dips * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Calculates the height of the status bar in pixels
     *
     * @param window The Window being displayed by an Activity
     * @return The height of the status bar in pixels
     */
    public static float getStatusBarHeight(Window window) {
        // Init a Rect to hold the Display Frame of the DecorView
        Rect rectangle = new Rect();

        // Retrieve the usable space
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        // The StatusBar height will be the top margin of the Rect that is unusable because it is
        // reserved for the StatusBar
        return rectangle.top;
    }

    public static float calculatePosterTopMargin(Window window, Toolbar toolbar, float margin) {
        float statusBarHeight = getStatusBarHeight(window);
        Log.d("TEST", "StatusBar Height: " + statusBarHeight);
        float toolbarHeight = toolbar.getHeight();
        Log.d("TEST", "Toolbar Height: " + toolbarHeight);
        float marginPx = convertDpToPixels(margin);
        Log.d("TEST", "Margin in px: " + marginPx);
        return statusBarHeight + toolbarHeight + marginPx;
    }
}
