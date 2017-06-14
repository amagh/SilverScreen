package com.amagh.silverscreen.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hnoct on 6/8/2017.
 */

public final class MovieUtils {
    /**
     * Suppressed Constructor
     */
    private MovieUtils() {}

    /**
     * Converts the time utilized by TheMovieDB.org to an easier to read date format
     *
     * @param stringDate Date in String format (yyyy-MM-dd)
     * @return String of the date in MMM d, yyyy format
     */
    public static String formatDate(String stringDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(stringDate);
            sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
