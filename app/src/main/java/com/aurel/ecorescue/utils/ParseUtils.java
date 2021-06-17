package com.aurel.ecorescue.utils;

import com.parse.ParseFile;
import com.parse.ParseObject;

public class ParseUtils {

    public static String getString(String key, ParseObject object) {
        String string = object.getString(key);
        if (string != null) {
            return string;
        } else {
            return "";
        }
    }

    public static String getString(Object object) {
        if (object == null) return "";
        return object.toString();
    }



    public static String getUrl(String key, ParseObject object) {
        ParseFile parseFile = object.getParseFile(key);
        return getUrl(parseFile);
    }


    public static String getUrl(ParseFile file) {
        if (file == null) return "not_found";
        String url = file.getUrl();
        if (url != null) {
            return url;
        } else {
            return "not_found";
        }
    }


}
