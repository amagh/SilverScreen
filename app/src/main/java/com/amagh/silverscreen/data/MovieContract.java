package com.amagh.silverscreen.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieContract {
    // **Constants** //
    public static final String AUTHORITY = "com.amagh.silverscreen";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_GENRES = "genres";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    public static final String PATH_LINK_GENRES_MOVIES = "link_genres_movies";
    public static final String PATH_LINK_TRAILERS_MOVIES = "link_trailers_movies";

    public static class MovieEntry implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Table information
        public static final String TABLE_NAME = "movies";

        // Columns
        public static final String COLUMN_MOVIE_ID          = "movie_id";
        public static final String COLUMN_TITLE             = "title";
        public static final String COLUMN_RELEASE_DATE      = "release_date";
        public static final String COLUMN_POSTER_PATH       = "poster_path";
        public static final String COLUMN_BACKDROP_PATH     = "backdrop_path";
        public static final String COLUMN_VOTE_COUNT        = "vote_count";
        public static final String COLUMN_VOTE_AVG          = "vote_average";
        public static final String COLUMN_SYNOPSIS          = "synposis";
        public static final String COLUMN_POPULARITY        = "popularity";
        public static final String COLUMN_FAVORITE          = "favorite";
    }

    public static class GenreEntry implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GENRES).build();

        // Table information
        public static final String TABLE_NAME = "genres";

        // Columns
        public static final String COLUMN_GENRE_ID          = "genre_id";
        public static final String COLUMN_GENRE             = "genre";
    }

    public static class TrailerEntry implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        // Table information
        public static final String TABLE_NAME = "trailers";

        // Columns
        public static final String COLUMN_TRAILER_ID        = "trailer_id";
        public static final String COLUMN_VIDEO_PATH        = "video_path";
        public static final String COLUMN_NAME              = "name";
        public static final String COLUMN_TYPE              = "type";
        public static final String COLUMN_THUMBNAIL_PATH    = "thumbnail_path";
    }

    public static class ReviewEntry implements BaseColumns {
        // ContentURI for accessing table information
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        // Table information
        public static final String TABLE_NAME = "reviews";

        // Columns
        public static final String COLUMN_REVIEW_ID         = "review_id";
        public static final String COLUMN_AUTHOR            = "author";
        public static final String COLUMN_CONTENT           = "content";
    }

    public static class LinkGenresMovies implements BaseColumns {
        // Content URI for accessing table information
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINK_GENRES_MOVIES).build();

        // Table information
        public static final String TABLE_NAME = "link_genres_movies";

        // All columns in this table will be foreign keys
    }
}
