package com.amagh.silverscreen.silverscreen;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class MovieListActivity extends AppCompatActivity {
    // Constants
    private final String TAG = MovieListActivity.class.getSimpleName();

    // Mem Vars
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.movie_list_rv);
    }


}
