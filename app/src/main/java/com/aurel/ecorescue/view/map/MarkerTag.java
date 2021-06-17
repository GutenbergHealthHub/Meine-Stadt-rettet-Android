package com.aurel.ecorescue.view.map;

import com.aurel.ecorescue.enums.MapMarkerType;
import android.location.Location;

import java.io.Serializable;

/**
 * Created by daniel on 7/6/17.
 */

public class MarkerTag implements Serializable {
    public String title, address, info;
    public MapMarkerType type;
    public Double lat,lng;
    public String placeId;
}
