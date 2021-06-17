package com.aurel.ecorescue.view.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class LocalPlacesStore {

    private static final int MAX_DISTANCE = 5000; // in meters
    private static final String SEPARATOR = "//-//";
    private SharedPreferences sharedPreferences;

    public LocalPlacesStore(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public String getPlaces(LatLng currentLocation, Date now, String typeString) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, -7);
        Date nowBefore7Days = calendar.getTime();

        List<GooglePlaces> placesList = getAllStoredDataOfType(typeString);
        for (GooglePlaces places : placesList) {
            // only use entries that are not older than 7 days
            if (places.storedTime.before(nowBefore7Days)) {
                deleteFromStorage(places);
                continue;
            }
            // use local data if loadingLocation - currentLocation < 1km
            if (distance(places.loadingLocation, currentLocation) < MAX_DISTANCE) {
                return places.placesString;
            }
        }
        return null;
    }

    private int distance(LatLng loadingLocation, LatLng currentLocation) {
        Location tempLoading = getLocation(loadingLocation);
        Location tempCurrent = getLocation(currentLocation);
        float distance = tempLoading.distanceTo(tempCurrent);
        return Math.round(distance);
    }

    private Location getLocation(LatLng latLng) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    private void deleteFromStorage(GooglePlaces places) {
        String storedPlaces = sharedPreferences.getString(places.type, "");
        String toDelete = places.toString();
        String otherPlaces = storedPlaces.replace(SEPARATOR + toDelete, "");
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(places.type, otherPlaces);
        edit.apply();
    }

    private List<GooglePlaces> getAllStoredDataOfType(String typeString) {
        String allPlaces = sharedPreferences.getString(typeString, "");
        String[] allPlacesSeparated = allPlaces.split(SEPARATOR);
        List<GooglePlaces> googlePlacesList = new ArrayList<>();
        for (String place : allPlacesSeparated) {
            if(!place.equals(""))
                googlePlacesList.add(new GooglePlaces(place));
        }
        return googlePlacesList;
    }

    public void storePlaces(LatLng loadingLocation, Date loadingDate, String googlePlacesData, String typeString) {
        String storedPlaces = sharedPreferences.getString(typeString, "");
        GooglePlaces newPlaces = new GooglePlaces(typeString, loadingLocation, loadingDate, googlePlacesData);
        String allPlaces = storedPlaces + SEPARATOR + newPlaces.toString();
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(typeString, allPlaces);
        edit.apply();
    }
}
