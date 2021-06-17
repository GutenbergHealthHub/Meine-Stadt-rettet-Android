package com.aurel.ecorescue.enums;

/**
 * Created by daniel on 6/13/17.
 */

public enum InformationItemType {
    NEWS("News"),
    EVENT("Event"),
    COURSE("Course");

    private String value;

    public String getValue(){
        return value;
    }

    InformationItemType(String value) {
        this.value = value;
    }
}
