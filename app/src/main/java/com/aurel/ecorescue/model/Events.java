package com.aurel.ecorescue.model;

import com.aurel.ecorescue.enums.InformationItemType;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;

/**
 * Created by daniel on 6/8/17.
 */

public class Events {
    public String objectId;
    public String title;
    public String description;

    public Date Timestamp;
    public Date CreatedAt;

    public String Text;
    public String Id;
    public File Image;
    public String Url;
    public InformationItemType Type;
    public String City;
    public String Zip;
    public String Street;
    public String Organizer;
    public Date From;
    public Date To;
    public String Email;
    public String Phone;

    public Events(ParseObject o){

        objectId = o.getObjectId();

        title = o.getString("title");
        Text = o.getString("information");
        Timestamp = o.getDate("date");
        description = o.getString("additionalInformation");
        Id = o.getObjectId();
        Url = o.getString("eventUrl");

        Type = InformationItemType.EVENT;
        CreatedAt = o.getCreatedAt();
        City = o.getString("city");
        Street = o.getString("street");
        Zip = o.getString("zip");
        Organizer = o.getString("organizer");
        To = o.getDate("to");
        From = o.getDate("from");
        Email = o.getString("email");
        Phone = o.getString("phone");

        ParseFile parseFile = o.getParseFile("imageObject");
        if (parseFile!=null) {
            try {
                Image = parseFile.getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "Events{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", Timestamp=" + Timestamp +
                ", CreatedAt=" + CreatedAt +
                ", Text='" + Text + '\'' +
                ", Id='" + Id + '\'' +
                //", Image=" + Image +
                ", Url='" + Url + '\'' +
                ", Type=" + Type +
                ", City='" + City + '\'' +
                ", Zip='" + Zip + '\'' +
                ", Street='" + Street + '\'' +
                ", Organizer='" + Organizer + '\'' +
                ", From=" + From +
                ", To=" + To +
                ", Email='" + Email + '\'' +
                ", Phone='" + Phone + '\'' +
                '}';
    }
}
