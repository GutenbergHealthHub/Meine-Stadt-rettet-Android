package com.aurel.ecorescue.adapter;

/**
 * Created by aurel on 20-Nov-16.
 */

public class HeaderItem extends ElementType {
    public String getHeader() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
