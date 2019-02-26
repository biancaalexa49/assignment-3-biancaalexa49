package com.example.android.lifecycleweather;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.lifecycleweather.utils.NetworkUtils;

import java.io.IOException;

public class ForecastLoader extends AsyncTaskLoader<String> {
    private static final String TAG = ForecastLoader.class.getSimpleName();

    private String mForecastLoaderJSON;
    private String mURL;

    ForecastLoader(Context context, String url) {
        super(context);
        mURL = url;
    }

    @Override
    protected void onStartLoading() {
        if (mURL != null) {
            if (mForecastLoaderJSON != null) {
                Log.d(TAG, "Delivering cached results");
                deliverResult(mForecastLoaderJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        if (mURL != null) {
            String results = null;
            try {
                Log.d(TAG, "loading results from OpenWeather with URL: " + mURL);
                results = NetworkUtils.doHTTPGet(mURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        } else {
            return null;
        }
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mForecastLoaderJSON = data;
        super.deliverResult(data);
    }
}


