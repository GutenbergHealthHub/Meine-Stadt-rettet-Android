package com.aurel.ecorescue.model;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.Date;

public class OwnDefibrillator {
    public String id;
    public String object;
    public ParseGeoPoint geoPoint;
    public ParseObject parseObject;
    public String information;
    public String Address;
    public String street;
    public String street_number;
    public String zipcode;
    public String city;
    public int producer;
    public String model;
    public String type;
    public int state;
    public Date createdAt;
    public Date updatedAt;
    public JSONArray files;
    public double latitude, longitude;

}