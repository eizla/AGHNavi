package com.aghnavi.agh_navi.dmsl.google.maps;


import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

public class VisibleObject<T> {

    // association between markers on screen and their PoisModels with the POI
    // information
    HashMap<Marker, T> mMarkersToPoi = null;

    public VisibleObject() {
        mMarkersToPoi = new HashMap<Marker, T>();
    }

    public void hideAll() {
        for (Marker m : mMarkersToPoi.keySet()) {
            m.setVisible(false);
        }
    }

    public void showAll() {
        for (Marker m : mMarkersToPoi.keySet()) {
            m.setVisible(true);
        }
    }

    public void clearAll() {
        for (Marker m : mMarkersToPoi.keySet()) {
            m.remove();
        }
        mMarkersToPoi.clear();
    }

    public T getPoisModelFromMarker(Marker m) {
        return mMarkersToPoi.get(m);
    }

    public void addMarkerAndPoi(Marker m, T pm) {
        mMarkersToPoi.put(m, pm);
    }

}

