package com.aurel.ecorescue.view.map;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.MapMarkerType;
import com.aurel.ecorescue.interfaces.OnDefibrilatorLoadedListener;
import com.aurel.ecorescue.service.AedParser;
import com.aurel.ecorescue.view.map.clustermanager.AedClusterItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 11/11/2017.
 */

public class MapUtils {

    Activity activity;
    GoogleMap map;
    public boolean FilterDefi = true, FilterHospital = true, FilterPharmacy = true, FilterFireDep = true, FilterDoctor = true, FilterDentist = true;
    public OnDefibrilatorLoadedListener onDefiLoadedListener;

    public MapUtils (Activity a, GoogleMap m, OnDefibrilatorLoadedListener listener){
        activity = a;
        map = m;
        onDefiLoadedListener = listener;
    }

    public void loadMapMarker(LatLng location) {
        map.clear();
        if (FilterDefi) {
            Location loc = new Location("");
            loc.setLatitude(location.latitude);
            loc.setLongitude(location.longitude);
            loadDefibrilators(loc, onDefiLoadedListener);
        }

        if (FilterHospital) {
            loadGoogleMapsEntry( MapMarkerType.hospital, location);
        }
        if (FilterPharmacy) {
            loadGoogleMapsEntry(MapMarkerType.pharmacy, location);
        }
        if (FilterFireDep) {
            loadGoogleMapsEntry( MapMarkerType.firefighting, location);
        }
        if(FilterDoctor){
            loadGoogleMapsEntry( MapMarkerType.doctor, location);
        }
        if(FilterDentist){
            loadGoogleMapsEntry(MapMarkerType.dentist, location);
        }
    }

    public void loadGoogleMapsEntry(MapMarkerType type, LatLng location) {
        String typeString = "";
        switch (type) {
            case hospital:
                typeString = "hospital";
                break;
            case pharmacy:
                typeString = "pharmacy";
                break;
            case firefighting:
                typeString = "fire_station";
                break;
            case doctor:
                typeString = "doctor";
                break;
            case dentist:
                typeString = "dentist";
                break;
        }

        String url = GetNearbyPlacesTask.getUrlForPlaces(activity, location.latitude, location.longitude, typeString);
        Object[] dataTransfer = new Object[5];
        dataTransfer[0] = map;
        dataTransfer[1] = url;
        dataTransfer[2] = location.latitude;
        dataTransfer[3] = location.longitude;
        dataTransfer[4] = typeString;

        GetNearbyPlacesTask getNearbyPlacesData = new GetNearbyPlacesTask(type, activity);
        getNearbyPlacesData.execute(dataTransfer);
    }

    public void loadDefibrilators(Location loc,OnDefibrilatorLoadedListener listener) {
        int radius = getKmRadiusToLoad(map);
        AedParser parser = new AedParser(listener);
        parser.loadAeds(loc, radius);
    }


    public void createDefibrillatorsFromList(List<ParseObject> list){
        for (ParseObject p : list) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng ll = new LatLng(p.getParseGeoPoint("location").getLatitude(), p.getParseGeoPoint("location").getLongitude());
            markerOptions.position(ll);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.mapmarker_defibrillator_unverified_v2)));
            Marker m = map.addMarker(markerOptions);
            MarkerTag tag = new MarkerTag();
            tag.address = p.getString("street") + ", " + p.getString("zip") + " " + p.getString("city");
            tag.title = p.getString("object");
            tag.type = MapMarkerType.defibrilator;
            tag.info = p.getString("information");
            tag.lat = p.getParseGeoPoint("location").getLatitude();
            tag.lng = p.getParseGeoPoint("location").getLongitude();
            tag.placeId = p.getObjectId();
            m.setTag(tag);
        }
    }

    public static List<AedClusterItem> toAedClusterItems(List<ParseObject> list) {
        List<AedClusterItem> aedClusterItems = new ArrayList<>();
        for(ParseObject parseAed : list){
            MarkerTag markerTag = new MarkerTag();
            markerTag.address = parseAed.getString("street") + ", " + parseAed.getString("zip") + " " + parseAed.getString("city");
            markerTag.title = parseAed.getString("object");
            markerTag.type = MapMarkerType.defibrilator;
            markerTag.info = parseAed.getString("information");
            markerTag.lat = parseAed.getParseGeoPoint("location").getLatitude();
            markerTag.lng = parseAed.getParseGeoPoint("location").getLongitude();
            markerTag.placeId = parseAed.getObjectId();
            aedClusterItems.add(new AedClusterItem(parseAed.getParseGeoPoint("location"), markerTag));
        }
        return aedClusterItems;
    }

    public static int getKmRadiusToLoad(GoogleMap map){
        VisibleRegion vr = map.getProjection().getVisibleRegion();

        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        Location topLeft = new Location("topLeft");
        topLeft.setLatitude(top);
        topLeft.setLongitude(left);

        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;
        Location bottomRight = new Location("bottomRight" );
        bottomRight.setLatitude(bottom);
        bottomRight.setLongitude(right);

        float distance =  topLeft.distanceTo(bottomRight);
        int radius = (int) distance / 1000;
        return radius;
    }
}
