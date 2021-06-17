package com.aurel.ecorescue.model;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Course implements Serializable {
    public final String objectId;
    public final String streetNr;
    public final String zip;
    public final String street;
    public final Date from;
    public final Date to;
    public final String city;
    public final String url;
    public final String name;
    public final String phone;
    public final String organizer;
    public final boolean activated;
    public final String country;
    public final String email;
    public final String information;
    public File image;
    
    public Course(ParseObject parseObject){

        objectId = parseObject.getObjectId();
        activated = parseObject.getBoolean("activated");

        name = parseObject.getString("name");
        organizer = parseObject.getString("organizer");
        phone = parseObject.getString("phone");
        from = parseObject.getDate("from");
        to = parseObject.getDate("to");
        email = parseObject.getString("email");
        information = parseObject.getString("information");
        url = parseObject.getString("url");

        streetNr = parseObject.getString("street_nr");
        zip = parseObject.getString("zip");
        street = parseObject.getString("street");
        city = parseObject.getString("city");
        country = parseObject.getString("country");

        ParseFile parseFile = parseObject.getParseFile("image");
        if (parseFile!=null) {
            try {
                image = parseFile.getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }


    @NotNull
    @Override
    public String toString() {
        return "Course{" +
                "objectId='" + objectId + '\'' +
                ", streetNr='" + streetNr + '\'' +
                ", zip='" + zip + '\'' +
                ", street='" + street + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", city='" + city + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", organizer='" + organizer + '\'' +
                ", activated=" + activated +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", information='" + information + '\'' +
//                ", image=" + image +
                '}';
    }
}
