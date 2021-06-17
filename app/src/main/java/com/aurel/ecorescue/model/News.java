package com.aurel.ecorescue.model;

import com.aurel.ecorescue.enums.InformationItemType;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * Created by daniel on 6/8/17.
 */

public class News {
    public String objectId;
    public String title;
    public String subtitle;
    public String imageObject;

    public Date CreatedAt;

    public String Text;
    public String Id;

    public String Url;
    public InformationItemType Type;


    public News(ParseObject o){

        objectId = o.getObjectId();
        title = o.getString("title");
        Text = o.getString("abstract");
        subtitle = o.getString("subtitle");

        Id = o.getObjectId();
        Url = o.getString("newsUrl");
        Type = InformationItemType.NEWS;

        CreatedAt = o.getCreatedAt();

        ParseFile parseFile = o.getParseFile("imageObject");
        if (parseFile!=null) {
            imageObject = parseFile.getUrl();
//                try {
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
        }
//        try {
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    @NotNull
    @Override
    public String toString() {
        return "News{" +
                "objectId='" + objectId + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
//                ", imageObject=" + imageObject +
                ", CreatedAt=" + CreatedAt +
                ", Text='" + Text + '\'' +
                ", Id='" + Id + '\'' +
                ", Url='" + Url + '\'' +
                ", Type=" + Type +
                '}';
    }
}
