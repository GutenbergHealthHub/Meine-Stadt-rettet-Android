package com.aurel.ecorescue.view.map;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.MapMarkerType;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 7/4/17.
 */

public class GetNearbyPlacesTask extends AsyncTask<Object, String, String> {

    private static final int radius = 10000;
    String googlePlacesData;
    GoogleMap mMap;
    String url;

    public MapMarkerType markerType;
    private Context ctx;
    private LocalPlacesStore localPlacesStore;

    public GetNearbyPlacesTask(MapMarkerType type, Context context) {
        markerType = type;
        this.ctx = context;
        localPlacesStore = new LocalPlacesStore(context);
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            double latitude = (double) params[2];
            double longitude = (double) params[3];

            String typeString = (String) params[4];
            LatLng currentLocation = new LatLng(latitude, longitude);
            Date now = new Date();


            googlePlacesData = localPlacesStore.getPlaces(currentLocation, now, typeString);
            if (googlePlacesData == null) {
                DownloadUrl downloadUrl = new DownloadUrl();
                googlePlacesData = downloadUrl.readUrl(url);
                if (!googlePlacesData.contains("error_message"))
                    Log.d("EcoRescue", "Google Map API executed");
                    localPlacesStore.storePlaces(currentLocation, now, googlePlacesData, typeString);
            }
        } catch (Exception e) {
            Log.d("EcoRescue", e.toString());
        }
        if (googlePlacesData != null)
            Log.d("GooglePlacesData", googlePlacesData);
        return googlePlacesData;

    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            List<HashMap<String, String>> nearbyPlacesList = null;
            PlacesDataParser dataParser = new PlacesDataParser();
            nearbyPlacesList = dataParser.parse(result);
            showNearbyPlaces(nearbyPlacesList);
        }
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String placeId = googlePlace.get("place_id");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
//            markerOptions.title(placeName);
            switch (markerType) {
                case dentist:
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.mapmarker_dentist_v2)));
                    break;
                case doctor:
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.mapmarker_doctor_v2)));
                    break;
                case hospital:
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.mapmarker_hospital_v2)));
                    break;
                case pharmacy:
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.mapmarker_pharmacy_v2)));
                    break;
                case firefighting:
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.mapmarker_fire_brigade_v2)));
                    break;
            }

            Marker m = mMap.addMarker(markerOptions);
            MarkerTag tag = new MarkerTag();
            tag.address = vicinity;
            tag.title = placeName;
            tag.type = markerType;
            tag.lat = lat;
            tag.lng = lng;
            tag.placeId = placeId;
            m.setTag(tag);

        }
    }

    public static String getUrlForPlaces(Context ctx, double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + radius);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + ctx.getString(R.string.google_maps_key_web));
        return (googlePlacesUrl.toString());
    }
}
