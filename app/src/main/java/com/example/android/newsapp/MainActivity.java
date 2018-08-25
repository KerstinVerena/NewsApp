package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    //The url to get the data from
    private static final String USGS_REQUEST_URL =
            "https://content.guardianapis.com/search?q=health&show-fields=byline&order-by=newest&api-key=test";
    private static final int NEWS_LOADER_ID = 1;
    public ArrayList<News> newsList;
    //Displaying an ArrayList using an RecyclerView uses code from the following video tutorial:
    //https://www.youtube.com/watch?v=ovmWgYxOCug
    //Please watch the video for more information.
    private RecyclerViewEmptySupport recyclerView;
    private NewsAdapter newsAdapter;
    private TextView mEmptyTextView;
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define the recyclerView which shall later show the News
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Set an emptyView in case there is no data available.
        mEmptyTextView = findViewById(R.id.empty_view);
        recyclerView.setEmptyView(mEmptyTextView);

        //Display the loading spinner while the data is loaded.
        loadingIndicator = findViewById(R.id.loading_spinner);


        //initialize the currentNews Adapter
        newsList = new ArrayList();
        newsAdapter = new NewsAdapter(newsList, this);
        recyclerView.setAdapter(newsAdapter);
        newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                //get the current News Item
                News clickedNews = newsList.get(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(clickedNews.getUrl());

                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Start the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        //Check if the device is connected to the internet and only initate the loader when a
        //the device is connected.
        getActiveNetworkInfo();
        if (getActiveNetworkInfo() != null
                && getActiveNetworkInfo().isConnectedOrConnecting()) {
            // Start the loader to get the news data
            Log.i(LOG_TAG, "Loader Initiated");

            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
        } else {
            //Load the empty state with the no_connction string if the device is not connected to
            //the interet.
            loadingIndicator.setVisibility(View.GONE);
            mEmptyTextView.setText(R.string.no_connection);
        }

    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "Loader Finished");
        return new NewsLoader(this, USGS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsList) {
        Log.i(LOG_TAG, "Loader Finished");
        //Clear the adapter of old data
        newsAdapter.clear();
        loadingIndicator.setVisibility(View.GONE);
        mEmptyTextView.setText(R.string.no_data);

        if (newsList != null && !newsList.isEmpty()) {
            newsAdapter.addAll(newsList);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.i(LOG_TAG, "Loader Resetted");
        newsAdapter.clear();
    }

    //Check if the device is connected to the internet.
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

}
