package com.aurel.ecorescue.model;

import com.aurel.ecorescue.enums.InformationItemType;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;

/**
 * Created by daniel on 6/8/17.
 */

public class InformationItem {
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

    @NotNull
    @Override
    public String toString() {
        return "InformationItem{" +
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
