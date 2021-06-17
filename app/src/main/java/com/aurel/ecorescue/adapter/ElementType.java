package com.aurel.ecorescue.adapter;

/**
 * Created by aurel on 20-Nov-16.
 */

public abstract class ElementType {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_EMERGENCY = 1;

    abstract public int getType();
}