package com.aurel.ecorescue.view.map;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

class GooglePlaces {

    private static String SEPARATOR = "-----";
    final String type;
    final LatLng loadingLocation;
    final Date storedTime;
    final String placesString;

    public GooglePlaces(String fromString){
        String[] parts = fromString.split(SEPARATOR);
        type = parts[0];
        loadingLocation = new LatLng(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
        storedTime = new Date(Long.parseLong(parts[3]));
        placesString = parts[4];
    }

    public GooglePlaces(String type, LatLng loadingLocation, Date storedTime, String placesString) {
        this.type = type;
        this.loadingLocation = loadingLocation;
        this.storedTime = storedTime;
        this.placesString = placesString;
    }

    @Override
    public String toString(){
        return type + SEPARATOR +
                loadingLocation.latitude + SEPARATOR +
                loadingLocation.longitude + SEPARATOR +
                storedTime.getTime() + SEPARATOR +
                placesString;
    }

}
