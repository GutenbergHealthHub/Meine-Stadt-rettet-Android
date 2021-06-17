package com.aurel.ecorescue.adapter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aurel on 04-Sep-16.
 */
public class Notification implements Parcelable {
    private static SimpleDateFormat readDate = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    private String title;


    private String emergencyObjectId;


    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private String city;
    private String streetNumber;

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }


    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }


    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }


    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }


    public void setControlCenterId(String controlCenterId) {
        this.controlCenterId = controlCenterId;
    }

    public void setEmergencyNumber(int emergencyNumber) {
        this.emergencyNumber = emergencyNumber;
    }

    private String zip;
    private String keyword;
    private String objectName;
    private String streetName;
    private String patientName;
    private String country;
    private String indicatorName;
    private String controlCenterId;
    private long endTime;
    private int emergencyNumber;

    public long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    private long leftTime;
    private long duration;
    private String objectId;
    private int state;
    private boolean protocol;
    private long startTime;
    private String context;
    private Date endDate, startDate;
    private double latitude, longitude;

    public boolean isProtocol() {
        return protocol;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Notification() {}

    public Notification(String context) {
        this.context = context;
    }

    public Notification(String title, Long time, String context) {
        this.title = title;
        this.endTime = time;
        this.context = context;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTime() {
        return endTime;
    }

    public void setTime(Long time) {
        this.endTime = time;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeLong(endTime);
        parcel.writeString(endDate == null ? null : endDate.toString());
        parcel.writeString(startDate == null ? null : startDate.toString());
        parcel.writeLong(startTime);
        parcel.writeLong(endTime - startTime);
        parcel.writeString(context);
        parcel.writeString(objectId);
        parcel.writeInt(state);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeByte((byte) (protocol ? 1 : 0));
        parcel.writeLong(leftTime);
        parcel.writeString(emergencyObjectId);
        parcel.writeString(controlCenterId);
        parcel.writeInt(emergencyNumber);
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            Notification notification = new Notification();
            notification.title = in.readString();
            notification.endTime = in.readLong();
            try {
                String eDate = in.readString();
                if (eDate != null) {
                    notification.endDate = readDate.parse(eDate);
                }
            } catch (ParseException e) {
                Log.d("ERROR", e.getMessage());
            }
            try {
                String sDate = in.readString();
                if (sDate != null) {
                    notification.startDate = readDate.parse(sDate);
                }
            } catch (ParseException e) {
                Log.d("ERROR", e.getMessage());
            }
            notification.startTime = in.readLong();
            notification.duration = in.readLong();
            notification.context = in.readString();
            notification.objectId = in.readString();
            notification.state = in.readInt();
            notification.latitude = in.readDouble();
            notification.longitude = in.readDouble();
            notification.protocol = in.readByte() != 0;
            notification.leftTime = in.readLong();
            notification.emergencyObjectId = in.readString();
            notification.controlCenterId = in.readString();
            notification.emergencyNumber = in.readInt();
            return notification;
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}
