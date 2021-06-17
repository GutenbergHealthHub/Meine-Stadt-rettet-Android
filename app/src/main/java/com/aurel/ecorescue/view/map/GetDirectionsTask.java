package com.aurel.ecorescue.view.map;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 7/7/17.
 */

public class GetDirectionsTask extends AsyncTask<String, Void, String> {

    String url;
    private GoogleMap map;
    private TextView labelDuration;
    private TextView labelDistance;

    private Polyline line;

    public GetDirectionsTask(GoogleMap map, TextView labelDuration, TextView labelDistance) {
        this.map = map;
        this.labelDuration = labelDuration;
        this.labelDistance = labelDistance;
    }

    public void clear() {
        if (line != null) {
            line.remove();
        }
    }

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
            drawPath(result);
        }
    }

    void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONObject duration = leg.getJSONObject("duration");
            int durationInS = duration.getInt("value");
            setDurationText(durationInS);

            if(labelDistance != null) {
                JSONObject distance = leg.getJSONObject("distance");
                this.labelDistance.setText(distance.getString("text"));
            }

            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            line = map.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
        } catch (JSONException e) {
            Log.d("EcoRescue", "JSONException while drawing path. " + e.getLocalizedMessage());
        }
    }

    private void setDurationText(int durationInS) {
        int hours = durationInS / 3600;
        int minutes = (durationInS % 3600) / 60;
        String timeString;
        if (hours > 0) {
            timeString = String.format("%d h %d min", hours, minutes);
        } else {
            timeString = String.format("%d min", minutes);
        }
        labelDuration.setText(timeString);
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
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
