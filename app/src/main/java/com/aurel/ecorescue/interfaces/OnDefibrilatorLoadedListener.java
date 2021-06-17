package com.aurel.ecorescue.interfaces;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by daniel on 7/6/17.
 */

public interface OnDefibrilatorLoadedListener {
    void defibrilatorLoaded(List<ParseObject> list);
}
