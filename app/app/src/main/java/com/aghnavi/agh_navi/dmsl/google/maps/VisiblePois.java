package com.aghnavi.agh_navi.dmsl.google.maps;


import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.google.android.gms.maps.model.Marker;

public class VisiblePois extends VisibleObject<PoisModel> {

    private Marker mFromMarker = null;
    private Marker mToMarker = null;

    private Marker mGooglePlaceMarker = null;
    private IPoisClass mGooglePlace = null;

    public VisiblePois() {
    }

    @Override
    public void hideAll() {
        super.hideAll();
        if (mFromMarker != null)
            mFromMarker.setVisible(false);
        if (mToMarker != null)
            mToMarker.setVisible(false);
    }

    @Override
    public void showAll() {
        super.showAll();
        if (mFromMarker != null)
            mFromMarker.setVisible(true);
        if (mToMarker != null)
            mToMarker.setVisible(true);
    }

    @Override
    public void clearAll() {
        super.clearAll();
        clearFromMarker();
        clearToMarker();
    }

    public Marker getMarkerFromPoisModel(String id) {
        for (Marker m : mMarkersToPoi.keySet()) {
            if (mMarkersToPoi.get(m).puid.equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    public boolean isFromMarker(Marker other) {
        return mFromMarker != null && mFromMarker.equals(other);
    }

    public boolean isToMarker(Marker other) {
        return mToMarker != null && mToMarker.equals(other);
    }

    // <From/To Marker>
    public void setFromMarker(Marker m) {
        mFromMarker = m;
    }

    public Marker getFromMarker() {
        return mFromMarker;
    }

    public void setToMarker(Marker m) {
        mToMarker = m;
    }

    public Marker getToMarker() {
        return mToMarker;
    }

    public void clearFromMarker() {
        if (mFromMarker != null) {
            mFromMarker.remove();
            mFromMarker = null;
        }
    }

    public void clearToMarker() {
        if (mToMarker != null) {
            mToMarker.remove();
            mToMarker = null;
        }
    }

    // </From/To Marker>

    // <Google Poi>
    public void setGooglePlaceMarker(Marker m) {
        clearGooglePlaceMarker();
        mGooglePlaceMarker = m;
    }

    public Marker getGooglePlaceMarker() {
        return mGooglePlaceMarker;
    }

    public IPoisClass getGooglePlace() {
        return mGooglePlace;
    }

    public void clearGooglePlaceMarker() {
        if (mGooglePlaceMarker != null) {
            mGooglePlaceMarker.remove();
            mGooglePlaceMarker = null;
            mGooglePlace = null;
        }
    }

    public boolean isGooglePlaceMarker(Marker other) {
        return mGooglePlaceMarker != null && mGooglePlaceMarker.equals(other);
    }
    // </Google Poi>

}
