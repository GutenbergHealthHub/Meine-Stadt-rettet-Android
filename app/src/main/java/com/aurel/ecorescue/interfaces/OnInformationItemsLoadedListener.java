package com.aurel.ecorescue.interfaces;

import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.model.InformationItem;

import java.util.ArrayList;

/**
 * Created by daniel on 6/8/17.
 */

public interface OnInformationItemsLoadedListener  {
    void informationItemsLoaded(InformationItemType type, ArrayList<InformationItem> informationItemList);
    void itemLoadedFromCache(InformationItemType type, InformationItem item);
}
