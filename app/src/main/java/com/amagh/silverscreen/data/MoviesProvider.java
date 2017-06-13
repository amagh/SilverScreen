package com.amagh.silverscreen.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.opengl.GLException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Set;

import static com.amagh.silverscreen.data.MovieContract.*;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MoviesProvider extends ContentProvider {
    // **Constants** //
    private final static String TAG = MoviesProvider.class.getSimpleName();

    // Int Codes for UriMatcher
    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_WITH_ID = 101;

    public static final int CODE_GENRES = 200;
    public static final int CODE_GENRES_WITH_ID = 201;

    public static final int CODE_TRAILERS = 300;
    public static final int CODE_TRAILERS_WITH_MOVIE_ID = 301;

    public static final int CODE_REVIEWS = 400;
    public static final int CODE_REVIEWS_WITH_MOVIE_ID = 401;
    public static final int CODE_REVIEWS_WITH_ID = 402;

    public static final int CODE_LINK_GENRES_MOVIES = 500;
    public static final int CODE_LINK_GENRES_MOVIES_WITH_MOVIE_ID = 501;
    public static final int CODE_LINK_GENRES_MOVIES_WITH_GENRE_ID = 502;

    // For utilizing the relational table to access movie and its genres
    public static final SQLiteQueryBuilder sMoviesAndGenresQueryBuilder;
    static {
        sMoviesAndGenresQueryBuilder = new SQLiteQueryBuilder();
        sMoviesAndGenresQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " LEFT JOIN " +
                LinkGenresMovies.TABLE_NAME + " ON " +
                MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = " +
                LinkGenresMovies.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " LEFT JOIN " +
                GenreEntry.TABLE_NAME + " ON " +
                LinkGenresMovies.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID + " = " +
                GenreEntry.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID
        );
    }

    // **Member Variables** //
    private MovieDbHelper mHelper;
    static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        // Obtain reference to Content Authority
        String authority = AUTHORITY;

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Paths and codes for URIs to match
        matcher.addURI(authority, PATH_MOVIES, CODE_MOVIES);
        matcher.addURI(authority, PATH_MOVIES + "/#", CODE_MOVIES_WITH_ID);

        matcher.addURI(authority, PATH_GENRES, CODE_GENRES);
        matcher.addURI(authority, PATH_GENRES + "/#", CODE_GENRES_WITH_ID);

        matcher.addURI(authority, PATH_TRAILERS, CODE_TRAILERS);
        matcher.addURI(
                authority,
                PATH_TRAILERS + "/" + PATH_MOVIES + "/#",
                CODE_TRAILERS_WITH_MOVIE_ID
        );

        matcher.addURI(authority, PATH_REVIEWS, CODE_REVIEWS);
        matcher.addURI(authority,
                PATH_REVIEWS + "/" + PATH_MOVIES + "/#",
                CODE_REVIEWS_WITH_MOVIE_ID
        );
        matcher.addURI(authority,
                PATH_REVIEWS + "/*",
                CODE_REVIEWS_WITH_ID
        );


        matcher.addURI(authority, PATH_LINK_GENRES_MOVIES, CODE_LINK_GENRES_MOVIES);
        matcher.addURI(
                authority,
                PATH_LINK_GENRES_MOVIES + "/" + PATH_MOVIES + "/#",
                CODE_LINK_GENRES_MOVIES_WITH_MOVIE_ID
        );
        matcher.addURI(
                authority,
                PATH_LINK_GENRES_MOVIES + "/" + PATH_GENRES + "/#",
                CODE_LINK_GENRES_MOVIES_WITH_GENRE_ID
        );

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Get readable instance of the database
        SQLiteDatabase db = mHelper.getReadableDatabase();

        // Init parameters for building Cursor
        String tableName = null;

        // Boolean check for whether to use a regular db.query or a QueryBuilder
        boolean useQueryBuilder = false;

        // Assign parameters for Cursor depending on URI
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                tableName = MovieEntry.TABLE_NAME;
                break;
            }

            case CODE_MOVIES_WITH_ID: {
                tableName = MovieEntry.TABLE_NAME;
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_GENRES: {
                tableName = GenreEntry.TABLE_NAME;
                break;
            }

            case CODE_GENRES_WITH_ID: {
                tableName = GenreEntry.TABLE_NAME;
                selection = GenreEntry.COLUMN_GENRE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_TRAILERS: {
                tableName = TrailerEntry.TABLE_NAME;
                break;
            }

            case CODE_TRAILERS_WITH_MOVIE_ID: {
                tableName = TrailerEntry.TABLE_NAME;
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_REVIEWS: {
                tableName = ReviewEntry.TABLE_NAME;
                break;
            }

            case CODE_REVIEWS_WITH_ID: {
                tableName = ReviewEntry.TABLE_NAME;
                selection = ReviewEntry.COLUMN_REVIEW_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_REVIEWS_WITH_MOVIE_ID: {
                tableName = ReviewEntry.TABLE_NAME;
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_LINK_GENRES_MOVIES: {
                useQueryBuilder = true;
                break;
            }

            case CODE_LINK_GENRES_MOVIES_WITH_MOVIE_ID: {
                useQueryBuilder = true;
                selection = MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            case CODE_LINK_GENRES_MOVIES_WITH_GENRE_ID: {
                useQueryBuilder = true;
                selection = GenreEntry.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            }

            default: throw new UnsupportedOperationException("Unknown URI: "+ uri);
        }

        // Init Cursor reference to return
        Cursor cursor;

        if (!useQueryBuilder) {
            cursor = db.query(
                    tableName,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        } else {
            cursor = sMoviesAndGenresQueryBuilder.query(
                    db,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Not implemented because not required for the operation of this ContentProvider
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Get readable instance of the database
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Init Uri reference to return
        Uri returnUri = null;

        // Variable for inserting into database
        String tableName;

        // Variables for building returnUri
        Uri contentUri;
        int id;

        // Set the insertion parameters based on URI
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                tableName = MovieEntry.TABLE_NAME;
                contentUri = MovieEntry.CONTENT_URI;
                id = contentValues.getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
                break;

            }

            case CODE_GENRES: {
                tableName = GenreEntry.TABLE_NAME;
                contentUri = GenreEntry.CONTENT_URI;
                id = contentValues.getAsInteger(GenreEntry.COLUMN_GENRE_ID);
                break;
            }

            case CODE_TRAILERS: {
                tableName = TrailerEntry.TABLE_NAME;
                contentUri = TrailerEntry.CONTENT_URI.buildUpon()
                        .appendPath(MovieContract.PATH_MOVIES)
                        .build();
                id = contentValues.getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
                break;
            }

            case CODE_REVIEWS: {
                tableName = ReviewEntry.TABLE_NAME;
                contentUri = ReviewEntry.CONTENT_URI.buildUpon()
                        .appendPath(PATH_MOVIES)
                        .build();
                id = contentValues.getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
                break;
            }

            case CODE_LINK_GENRES_MOVIES: {
                tableName = LinkGenresMovies.TABLE_NAME;
                contentUri = LinkGenresMovies.CONTENT_URI.buildUpon()
                        .appendPath(MovieContract.PATH_MOVIES)
                        .build();
                id = contentValues.getAsInteger(MovieEntry.COLUMN_MOVIE_ID);
                break;
            }

            default: throw new UnsupportedOperationException("Unknown URI: "+ uri);
        }

        // Get the id of the row inserted
        long _id = db.insert(
                tableName,
                null,
                contentValues
        );

        // Check that it returned a valid id
        if (_id > 0) {
            // Build the returnUri
            returnUri = ContentUris.withAppendedId(contentUri, id);
        } else {
            Log.d(TAG, "Values attempted to be inserted: " );
            Set<String> keySet = contentValues.keySet();

            for (String key : keySet) {
                Log.d(TAG, key + " - " + contentValues.getAsString(key));
            }
            throw new SQLException("Error inserting into URI: " + uri);
        }

        // Notify registered listeners of change in database contents
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable instance of database
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Init variable for num rows deleted
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                rowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }

            case CODE_LINK_GENRES_MOVIES: {
                rowsDeleted = db.delete(
                        LinkGenresMovies.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }

            default: throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        // Notify registered listeners of change in database contents
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        // Not required for the scope of this project. 'movies' and 'link_genres_movies' tables will
        // be deleted each time user selects a different filter option. This is required to match
        // the list provided by TheMovieDB.org each time. If database is not deleted, it may contain
        // entries that are no longer on TheMovieDB's list.

        // Get writable instance of database
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Get reference to variable to return
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            // Database is already set to REPLACE ON CONFLICT, so this method is probably not
            // required
            case CODE_MOVIES: {
                rowsUpdated = db.update(
                        MovieEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }

            case CODE_MOVIES_WITH_ID: {
                rowsUpdated = db.update(
                        MovieEntry.TABLE_NAME,
                        contentValues,
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[] {uri.getLastPathSegment()}
                );
                break;
            }

            default: throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        // Notify registered listeners of change in database contents
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        // Obtain writable instance of database
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Variable for bulk-insertion transaction
        String tableName;

        // Set the table name based on URI
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES: {
                tableName = MovieEntry.TABLE_NAME;
                break;
            }

            case CODE_GENRES: {
                tableName = GenreEntry.TABLE_NAME;
                break;
            }

            case CODE_TRAILERS: {
                tableName = TrailerEntry.TABLE_NAME;
                break;
            }

            case CODE_REVIEWS: {
                tableName = ReviewEntry.TABLE_NAME;
                break;
            }

            case CODE_LINK_GENRES_MOVIES: {
                tableName = LinkGenresMovies.TABLE_NAME;
                break;
            }

            default: return super.bulkInsert(uri, values);
        }

        // Init rowsInserted to return
        int rowsInserted = 0;

        // Open database for bulk insertion
        db.beginTransaction();

        try {
            // Iterate through ContentValues to insert them into database
            for (ContentValues value : values) {
                long _id = db.insert(
                        tableName,
                        null,
                        value
                );

                if (_id > 0) {
                    // Increment rowsInserted if successful insertion
                    rowsInserted++;
                }
            }

            db.setTransactionSuccessful();
        } finally {
            // End database transaction
            db.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsInserted;
    }
}
