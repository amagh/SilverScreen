package com.amagh.silverscreen.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.amagh.silverscreen.data.MovieContract.*;

/**
 * Created by hnoct on 6/7/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    // **Constants** //
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    // SQL Statements
    private static final String CREATE_MOVIES_TABLE =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("                                      +

            MovieEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, "            +
            MovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, "                             +
            MovieEntry.COLUMN_TITLE         + " TEXT NOT NULL, "                                +
            MovieEntry.COLUMN_RELEASE_DATE  + " TEXT NOT NULL, "                                +
            MovieEntry.COLUMN_POSTER_PATH   + " TEXT NOT NULL, "                                +
            MovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, "                                +
            MovieEntry.COLUMN_VOTE_COUNT    + " INTEGER, "                                      +
            MovieEntry.COLUMN_VOTE_AVG      + " REAL NOT NULL, "                                +
            MovieEntry.COLUMN_SYNOPSIS      + " TEXT NOT NULL, "                                +
            MovieEntry.COLUMN_POPULARITY    + " REAL NOT NULL, "                                +
            MovieEntry.COLUMN_FAVORITE      + " INTEGER, "                                      +

            "UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

    private static final String CREATE_GENRES_TABLE =
            "CREATE TABLE " + GenreEntry.TABLE_NAME + " ("                                      +

            GenreEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, "            +
            GenreEntry.COLUMN_GENRE_ID      + " INTEGER NOT NULL, "                             +
            GenreEntry.COLUMN_GENRE         + " TEXT NOT NULL);";

    private static final String CREATE_TRAILERS_TABLE =
            "CREATE TABLE " + TrailerEntry.TABLE_NAME + " ("                                    +

            TrailerEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, "            +
            TrailerEntry.COLUMN_TRAILER_ID  + " INTEGER NOT NULL, "                             +
            TrailerEntry.COLUMN_KEY         + " TEXT NOT NULL, "                                +
            TrailerEntry.COLUMN_NAME        + " TEXT, "                                         +
            TrailerEntry.COLUMN_TYPE        + " TEXT, "                                         +
            MovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, "                             +

            "FOREIGN KEY (" + MovieEntry.COLUMN_MOVIE_ID + ") "                                 +
            "REFERENCES " + MovieEntry.TABLE_NAME + "(" + MovieEntry.COLUMN_MOVIE_ID + "));";


    private static final String CREATE_LINK_GENRES_MOVIES_TABLE =
            "CREATE TABLE " + LinkGenresMovies.TABLE_NAME + " ("                                +

            LinkGenresMovies._ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, "            +
            MovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, "                             +
            GenreEntry.COLUMN_GENRE_ID      + " INTEGER NOT NULL, "                             +

            "FOREIGN KEY (" + MovieEntry.COLUMN_MOVIE_ID + ") "                                 +
            "REFERENCES " + MovieEntry.TABLE_NAME + "(" +  MovieEntry.COLUMN_MOVIE_ID + "),"    +

            "FOREIGN KEY (" + GenreEntry.COLUMN_GENRE_ID +") "                                  +
            "REFERENCES " + GenreEntry.TABLE_NAME + "(" + GenreEntry.COLUMN_GENRE_ID + "));";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Open database to create tables
        sqLiteDatabase.beginTransaction();

        try {
            // Create tables in a single transaction
            sqLiteDatabase.execSQL(CREATE_MOVIES_TABLE);
            sqLiteDatabase.execSQL(CREATE_GENRES_TABLE);
            sqLiteDatabase.execSQL(CREATE_TRAILERS_TABLE);
            sqLiteDatabase.execSQL(CREATE_LINK_GENRES_MOVIES_TABLE);

            sqLiteDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // End transaction
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // To be implemented if DATABASE_VERSION is incremented
    }
}
