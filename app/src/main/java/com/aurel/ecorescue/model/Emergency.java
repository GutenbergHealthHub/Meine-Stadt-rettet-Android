package com.aurel.ecorescue.model;

import android.util.Log;

import com.aurel.ecorescue.utils.ParseUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by daniel on 7/11/17.
 */

public class Emergency {

    public String objectId;
    public String patientName;
    public String information;
    public String address;
    public String indicator;
    public String keyword;
    public String emergencyNumberDC;
    public String controlCenterNumber;
    public String objectName;
    public ParseGeoPoint geoPoint;
    public Date createdAt;
    public String controlCenterObjectId;
    public boolean reportRequired;

    public ParseObject parseEmergency;

    public void fromParseObject(ParseObject parseEmergency) {
        this.parseEmergency = parseEmergency;

        information = parseEmergency.getString("informationString");
        patientName = parseEmergency.getString("patientName");
        geoPoint = parseEmergency.getParseGeoPoint("locationPoint");
        objectName = parseEmergency.getString("objectName");
        createdAt = parseEmergency.getCreatedAt();
        //null check for address
        String streetName, streetNumber, zip, city;
        streetName = parseEmergency.getString("streetName");
        streetNumber = parseEmergency.getString("streetNumber");
        zip = parseEmergency.getString("zip");
        city = parseEmergency.getString("city");
        if (streetName == null)
            streetName = "";
        if (streetNumber == null)
            streetNumber = "";
        if (zip == null)
            zip = "";
        if (city == null)
            city = "";
        address = streetName + " " + streetNumber +
                "\n" + zip + " " + city;
//        if (parseEmergency.getString("objectName") != null)
//            address = address + "\n" + parseEmergency.getString("objectName");
        objectId = parseEmergency.getObjectId();
        emergencyNumberDC = ParseUtils.getString("emergencyNumberDC", parseEmergency);
        if (emergencyNumberDC.isEmpty()) emergencyNumberDC = ParseUtils.getString("emergencyNumber", parseEmergency);
//        emergencyNumberDC = parseEmergency.getString("emergencyNumberDC");
        try {
            // TODO: UI Blocking
            ParseObject controlCenter = parseEmergency.getParseObject("controlCenterRelation").fetch();
            controlCenterObjectId = controlCenter.getObjectId();
            controlCenterNumber = controlCenter.getString("phoneNumber");
            reportRequired = controlCenter.getBoolean("reportRequired");
        } catch (ParseException e) {
            Log.d("EcoRescue", "error getting phoneNumber from controlCenter");
            e.printStackTrace();
        }

        indicator = parseEmergency.getString("indicatorName");
        keyword = parseEmergency.getString("keyword");
        if (indicator == null)
            indicator = "";
        if (keyword == null)
            keyword = "";

    }


}
