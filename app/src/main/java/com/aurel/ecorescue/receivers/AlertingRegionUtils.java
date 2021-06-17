package com.aurel.ecorescue.receivers;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlertingRegionUtils {

    static int determineMaxDistance(JSONObject config, List<ParseObject> alertingRegions, ParseGeoPoint userGeoPoint) {
        try {
            int alertingRegionRadius = AlertingRegionUtils.findAlertingRegionRadius(userGeoPoint, alertingRegions);
            return alertingRegionRadius != 0 ? alertingRegionRadius : config.getInt("distance");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 2000;
    }

    static boolean isUserWithinMaxDistance(ParseGeoPoint userGeoPoint, ParseGeoPoint emergencyGeoPoint, int maxDistanceInMeters) {
        if (userGeoPoint == null || emergencyGeoPoint == null)
            return false;
        double distanceInKilometers = userGeoPoint.distanceInKilometersTo(emergencyGeoPoint);
        double maxDistanceInKilometers = (double) maxDistanceInMeters / 1000;
        return distanceInKilometers < maxDistanceInKilometers;
    }

    static int findAlertingRegionRadius(ParseGeoPoint userGeoPoint, List<ParseObject> alertingRegions) {
        Collections.sort(alertingRegions, (o1, o2) -> {
            int alertingRadius1 = o1.getInt("alertingRadius");
            int alertingRadius2 = o2.getInt("alertingRadius");
            return alertingRadius1 - alertingRadius2;
        });
        for (ParseObject alertingRegion : alertingRegions) {
            if (isInside(userGeoPoint, alertingRegion))
                return alertingRegion.getInt("alertingRadius");
        }
        return 0;
    }

    private static boolean isInside(ParseGeoPoint userGeoPoint, ParseObject alertingRegion) {
        ParseGeoPoint alertingRegionCenter = alertingRegion.getParseGeoPoint("location");
        int alertingRegionRadius = alertingRegion.getInt("alertingRadius");

        return (userGeoPoint.distanceInKilometersTo(alertingRegionCenter) * 1000 < alertingRegionRadius);
    }
}
