package com.example.android.lifecycleweather;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.AsyncTask;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.lifecycleweather.data.WeatherPreferences;
import com.example.android.lifecycleweather.utils.NetworkUtils;
import com.example.android.lifecycleweather.utils.OpenWeatherMapUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.OnForecastItemClickListener, LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FORECAST_ITEM_LIST_KEY = "forecastItem";
    private static final String FORECAST_URL_KEY = "forecastURL";

    private static final int FORECAST_LOADER_ID = 0;

    private TextView mForecastLocationTV;
    private RecyclerView mForecastItemsRV;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private ForecastAdapter mForecastAdapter;
    private ArrayList<OpenWeatherMapUtils.ForecastItem> mForecastItemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mForecastLocationTV = findViewById(R.id.tv_forecast_location);
        mForecastLocationTV.setText(WeatherPreferences.getDefaultForecastLocation());

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = findViewById(R.id.tv_loading_error_message);
        mForecastItemsRV = findViewById(R.id.rv_forecast_items);

        mForecastAdapter = new ForecastAdapter(this);
        mForecastItemsRV.setAdapter(mForecastAdapter);
        mForecastItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mForecastItemsRV.setHasFixedSize(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(FORECAST_ITEM_LIST_KEY)) {
            mForecastItemList = (ArrayList<OpenWeatherMapUtils.ForecastItem>) savedInstanceState.getSerializable(FORECAST_ITEM_LIST_KEY);
            mForecastAdapter.updateForecastItems(mForecastItemList);
        }

        String openWeatherMapForecastURL = OpenWeatherMapUtils.buildForecastURL(WeatherPreferences.getDefaultForecastLocation(), WeatherPreferences.getDefaultTemperatureUnits());
        Log.d(TAG, "got forecast url: " + openWeatherMapForecastURL);
        Bundle args = new Bundle();
        args.putString(FORECAST_URL_KEY, openWeatherMapForecastURL);
        getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, args, this);
        //reloadForecast();
    }

    @Override
    public void onForecastItemClick(OpenWeatherMapUtils.ForecastItem forecastItem) {
        Intent intent = new Intent(this, ForecastItemDetailActivity.class);
        intent.putExtra(OpenWeatherMapUtils.EXTRA_FORECAST_ITEM, forecastItem);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mForecastItemList != null) {
            outState.putSerializable(FORECAST_ITEM_LIST_KEY, mForecastItemList);
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        String url = null;
        if (bundle != null) {
            url = bundle.getString(FORECAST_URL_KEY);
        }
        return new ForecastLoader(this, url);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String forecastJSON) {
        Log.d(TAG, "Got results from the loader");
        if (forecastJSON != null) {
            mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
            mForecastItemsRV.setVisibility(View.VISIBLE);
            mForecastItemList = OpenWeatherMapUtils.parseForecastJSON(forecastJSON);
            mForecastAdapter.updateForecastItems(mForecastItemList);
        } else {
            mForecastItemsRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
        }
        mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Nothing to do here...
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                showForecastLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void reloadForecast() {
        String openWeatherMapForecastURL = OpenWeatherMapUtils.buildForecastURL(
                WeatherPreferences.getDefaultForecastLocation(),
                WeatherPreferences.getDefaultTemperatureUnits()
        );
        Log.d(TAG, "got forecast url: " + openWeatherMapForecastURL);
        Bundle args = new Bundle();
        args.putString(FORECAST_URL_KEY, openWeatherMapForecastURL);
        mLoadingIndicatorPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, args, this);
    }

    public void showForecastLocation() {
        Uri geoUri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", WeatherPreferences.getDefaultForecastLocation())
                .build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}