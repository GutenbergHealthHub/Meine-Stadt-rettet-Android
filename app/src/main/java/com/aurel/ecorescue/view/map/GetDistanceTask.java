package com.aurel.ecorescue.view.map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnDistanceLoaded;
import com.parse.ParseGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by daniel on 7/18/17.
 */

public class GetDistanceTask extends AsyncTask<String, Void, String> {

    OnDistanceLoaded listener;
    ParseGeoPoint geoPoint;

    public GetDistanceTask(OnDistanceLoaded listener, ParseGeoPoint geoPoint) {
        this.listener = listener;
        this.geoPoint = geoPoint;
    }

    String url;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        url = params[0];
        DownloadUrl jParser = new DownloadUrl();
        String json = null;
        try {
            json = jParser.readUrl(url);
        } catch (IOException e) {
            Log.d("EcoRescue", "Error downloading directions. " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return json;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            try {
                final JSONObject json = new JSONObject(result);
                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject routes = routeArray.getJSONObject(0);
                JSONArray legs = routes.getJSONArray("legs");
                JSONObject leg = legs.getJSONObject(0);
                JSONObject duration = leg.getJSONObject("duration");
                JSONObject distance = leg.getJSONObject("distance");
                int valueDuration = duration.getInt("value") / 60;
                float valueDistance = ((float) distance.getInt("value")) / 1000;
                listener.setDistanceAndDuration(valueDistance, valueDuration);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static String GetURLForDirections(Context context, double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=false");
        urlString.append("&key=" + context.getString(R.string.google_maps_key_web));
        String url = urlString.toString();
        Log.d("EcoRescue", "GetURLForDirections: " + url);
        return url;
    }
}
