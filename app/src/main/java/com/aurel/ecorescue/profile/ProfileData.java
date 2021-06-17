package com.aurel.ecorescue.profile;

import android.util.Log;

import com.aurel.ecorescue.parse.ControlCenter;
import com.aurel.ecorescue.utils.ParseUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aurel on 26-Sep-16.
 */

public class ProfileData implements Serializable {

    private String email;
    private String profession;
    private boolean touchID;
    private String name;
    private String surname;
    private Date birthday;
    private String street;
    private String stNumber;
    private String city;
    private String zip;
    private String country;
    private boolean mobileAED;
    private String phone;
    private String centralName, centralAddress, centralCity, centralPhone, centralFax;
    private boolean activated;
    private String logo;
    private String qualification;
    private boolean receiveTestAlarm;

    // Profile Status
    private boolean contractSigned;

    public ProfileData createFromCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        setEmail(currentUser.getString("username"));
        setName(currentUser.getString("firstname"));
        setSurname(currentUser.getString("lastname"));
        setStreet(currentUser.getString("thoroughfare"));
        setStNumber(currentUser.getString("subThoroughfare"));
        setCity(currentUser.getString("city"));
        setZip(currentUser.getString("zip"));
        setCountry(currentUser.getString("country"));
        setTouchID(currentUser.getBoolean("touchID"));
        setMobileAED(currentUser.getBoolean("mobileAED"));
        setBirthday(currentUser.getDate("birthday"));
        setTestAlarm(currentUser.getBoolean("receivesPracticeAlarm"));
        setContractSigned(currentUser);
        String phoneCode = "";
        Number phoneCodeNumber = currentUser.getNumber("phoneCode");
        if (phoneCodeNumber == null) {
            phoneCode = "";
        } else {
            phoneCode = "+" + phoneCodeNumber.toString();
        }
        String phone = "";
        Number phoneNumber = currentUser.getNumber("phoneNumber");
        if (phoneNumber != null) {
            phone = phoneNumber.toString();
        }
        setPhone(phoneCode + " " + phone);
        setQualification(ParseUtils.getString(currentUser.getString("qualification")));
        setProfession(currentUser.getString("profession"));
        setActivated(true);

        setLogo(ParseUtils.getUrl("profilePicture", currentUser));

        if (currentUser.getParseObject("controlCenterRelation") != null) {
            ParseQuery<ControlCenter> query = ParseQuery.getQuery(ControlCenter.class);
            query.whereEqualTo("objectId", currentUser.getParseObject("controlCenterRelation").getObjectId());
            query.findInBackground((objects, e) -> {
                if (e == null) {
                    for (ControlCenter i : objects) {
                        setCentralName(i.getName());
                        setCentralAddress(i.getAddress());
                        setCentralCity(i.getZip() + " " + i.getCity());
                        setCentralPhone(i.getPhoneNumber());
                        setCentralFax(i.getFax());
                    }
                } else {
                    Log.d("ERROR ", e.getMessage());
                }
            });
        }
        return this;
    }

    private void setCountry(String country) {
        this.country = country;
    }

    private void setBirthday(Date birthday) {
        if(birthday != null)
            this.birthday = birthday;
        else
            this.birthday = new Date();
    }

    public Date getBirthday(){
        return birthday;
    }

    private void setContractSigned(ParseUser currentUser) {
        contractSigned = currentUser.getParseObject("userContractBasic") != null;
    }

    public boolean getContractSigned() {
        return contractSigned;
    }

    public void setTestAlarm(boolean receivesPracticeAlarm) {
        this.receiveTestAlarm = receivesPracticeAlarm;
    }

    public boolean isTouchID() {
        return touchID;
    }

    public void setTouchID(boolean touchID) {
        this.touchID = touchID;
    }

    public void setMobileAED(boolean mobileAED) {
        this.mobileAED = mobileAED;
    }

    public String getProfession() {
        return profession;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null) {
            phone = "";
        }
        this.phone = phone;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        if (zip == null) {
            zip = "";
        }
        this.zip = zip;
    }

    public String getName() {
        return name;
    }

    public boolean getMobileAed() {
        if (this.mobileAED)
            return true;
        else
            return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        if (street == null) {
            street = "";
        }
        this.street = street;
    }

    public String getStNumber() {
        return stNumber;
    }

    public void setStNumber(String stNumber) {
        if (stNumber == null) {
            stNumber = "";
        }
        this.stNumber = stNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (city == null) {
            city = "";
        }
        this.city = city;
    }

    public String getCentralName() {
        return centralName;
    }

    public void setCentralName(String centralName) {
        this.centralName = centralName;
    }

    public String getCentralAddress() {
        return centralAddress;
    }

    public void setCentralAddress(String centralAddress) {
        this.centralAddress = centralAddress;
    }

    public String getCentralPhone() {
        return centralPhone;
    }

    public void setCentralPhone(String centralPhone) {
        this.centralPhone = centralPhone;
    }

    public String getCentralCity() {
        return centralCity;
    }

    public void setCentralCity(String centralCity) {
        this.centralCity = centralCity;
    }

    public String getCentralFax() {
        return centralFax;
    }

    public void setCentralFax(String centralFax) {
        this.centralFax = centralFax;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isTestAlarmActivated() {
        return receiveTestAlarm;
    }

    public boolean getPersonalInformationFilled() {
        if (name == null || surname == null || qualification == null) return false;
        return (!email.isEmpty() &&
                !name.isEmpty() &&
                !surname.isEmpty() &&
                !qualification.isEmpty());
    }

    public String getCountry() {
        return country;
    }

}
