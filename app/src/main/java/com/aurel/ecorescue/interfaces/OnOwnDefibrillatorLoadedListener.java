package com.aurel.ecorescue.interfaces;

import com.aurel.ecorescue.model.OwnDefibrillator;

import java.util.List;

public interface OnOwnDefibrillatorLoadedListener {
    void ownDefibrillatorLoaded(List<OwnDefibrillator> list);
    void ownDefibrillatorLoaded(OwnDefibrillator defrib);

}
