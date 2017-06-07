package com.amagh.silverscreen.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieContract {
    // Constants
    public static final String AUTHORITY = "com.amagh.silverscreen";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_GENRES = "genres";
    public static final String PATH_LINK_GENRES_MOVIES = "link_genres_movies";

    public static class MovieEntry implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES).build();

        // Table information
        public static final String TABLE_NAME = "movies";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVG = "vote_average";
        public static final String COLUMN_SYNOPSIS = "synposis";
    }

    public static class GenreEntry implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_GENRES).build();

        // Table information
        public static final String TABLE_NAME = "genres";

        // Columns
        public static final String COLUMN_GENRE_ID = "genre_id";
        public static final String COLUMN_GENRE = "genre";
    }

    public static class LinkGenresMovies implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LINK_GENRES_MOVIES).build();

        // Table information
        public static final String TABLE_NAME = "link_genres_movies";

        // All columns in this table will be foreign keys
    }
}
