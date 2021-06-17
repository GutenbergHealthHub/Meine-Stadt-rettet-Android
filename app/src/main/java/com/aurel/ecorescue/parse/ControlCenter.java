package com.aurel.ecorescue.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by aurel on 31-Aug-16.
 */
@ParseClassName("ControlCenter")
public class ControlCenter extends ParseObject {

    public String getZip() {
        return getString("zip");
    }

    public String getFax() {
        return getString("faxNumber");
    }

    public String getCity() {
        return getString("city");
    }

    public ParseFile getLogo() {
        return getParseFile("logo");
    }

    public String getName() {
        return getString("name");
    }

    public String getAddress() {
        return getString("address");
    }

    public String getPhoneNumber() {
        return getString("phoneNumber");
    }
}
