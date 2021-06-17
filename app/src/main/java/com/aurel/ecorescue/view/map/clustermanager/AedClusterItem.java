package com.aurel.ecorescue.view.map.clustermanager;

import com.aurel.ecorescue.view.map.MarkerTag;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseGeoPoint;

public class AedClusterItem implements ClusterItem {

    private final LatLng position;
    public final MarkerTag markerTag;

    public AedClusterItem(ParseGeoPoint parseGeoPoint, MarkerTag markerTag){
        this.position = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
        this.markerTag = markerTag;
    }

    public String getId(){
        return markerTag.placeId;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getSnippet() {
        return "";
    }
}
