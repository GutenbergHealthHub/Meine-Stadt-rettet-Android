package com.aurel.ecorescue.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

public class SubContract {

    private String objectId;
    private Date validFrom;
    private Date validUntil;
    private String url;
    private String controlCenter;
    private String title;
    private String subtitle;
    private String version;
    private int state;


    public SubContract(){}

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getControlCenter() {
        return controlCenter;
    }

    public void setControlCenter(String controlCenter) {
        this.controlCenter = controlCenter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @NotNull
    @Override
    public String toString() {
        return "SubContract{" +
                "objectId='" + objectId + '\'' +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", url='" + url + '\'' +
                ", controlCenter='" + controlCenter + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", version='" + version + '\'' +
                ", state=" + state +
                '}';
    }
}
