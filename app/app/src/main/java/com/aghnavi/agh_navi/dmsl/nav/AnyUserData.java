package com.aghnavi.agh_navi.dmsl.nav;


import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;

import java.util.List;

public class AnyUserData implements Parcelable {
    // the selected Floor displayed on map & used when requesting the radio map
    // = floor_number in FloorModel
    private FloorModel selectedFloor = null;
    // the selected building whose floor is displayed on the map
    private BuildingModel selectedBuilding = null;
    // building used for last navigation
    private BuildingModel navBuilding = null;
    // holds all the POIs for the Navigation route
    private List<PoisNav> navPois = null;
    // the position based on ip address
    private GeoPoint positionIP = null;
    // last position estimated by Wifi Tracker
    private GeoPoint positionCoordsWifi = null;
    // last position requested from Google Location API
    private Location positionGPS = null;

    // <BUILDING /FLOOR>
    public String getSelectedFloorNumber() {
        return selectedFloor != null ? selectedFloor.floor_number : null;
    }

    public String getSelectedBuildingId() {
        return selectedBuilding != null ? selectedBuilding.buid : null;
    }

    public BuildingModel getSelectedBuilding() {
        return selectedBuilding;
    }

    public void setSelectedFloor(FloorModel f) {
        selectedFloor = f;
    }

    public void setSelectedBuilding(BuildingModel b) {
        resetPosition();
        selectedBuilding = b;
        selectedFloor = null;
    }

    public boolean isFloorSelected() {
        return selectedFloor != null && selectedFloor.floor_number != null && !selectedFloor.floor_number.equalsIgnoreCase("") && !selectedFloor.floor_number.equalsIgnoreCase("-");
    }

    // <NAVIGATION>
    public boolean isNavBuildingSelected() {
        if (navBuilding == null || selectedBuilding == null)
            return false;
        return navBuilding.buid != null && selectedBuilding.buid != null && navBuilding.buid.equalsIgnoreCase(selectedBuilding.buid);
    }

    public BuildingModel getNavBuilding() {
        return navBuilding;
    }

    public List<PoisNav> getNavPois() {
        return navPois;
    }

    public void setNavBuilding(BuildingModel b) {
        navBuilding = b;
    }

    public void setNavPois(List<PoisNav> p) {
        navPois = p;
    }

    public void clearNav() {
        if (navPois != null) {
            navPois.clear();
        }
        navBuilding = null;
        navPois = null;
    }

    // <POSITIONING>
    public void setPositionWifi(double lat, double lng) {
        positionCoordsWifi = new GeoPoint(lat, lng);
    }

    public void setLocationGPS(Location loc) {
        positionGPS = loc;
    }

    public void setLocationIP(GeoPoint loc) {
        positionIP = loc;
    }

    public GeoPoint getLatestUserPosition() {
        GeoPoint result;
        if (!AnyplaceAPI.LOCK_TO_GPS && positionCoordsWifi != null) {
            result = positionCoordsWifi;
        } else {
            result = getLocationGPSorIP();
        }
        return result;
    }

    public GeoPoint getPositionWifi() {
        return positionCoordsWifi;
    }

    public GeoPoint getLocationGPSorIP() {
        GeoPoint result;
        if (positionGPS != null) {
                result = new GeoPoint(positionGPS.getLatitude(), positionGPS.getLongitude());
        } else {
            result = positionIP;
        }
        return result;
    }

    private void resetPosition() {
        positionCoordsWifi = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.selectedFloor, flags);
        dest.writeParcelable(this.selectedBuilding, flags);
        dest.writeParcelable(this.navBuilding, flags);
        dest.writeTypedList(this.navPois);
        dest.writeParcelable(this.positionIP, flags);
        dest.writeParcelable(this.positionCoordsWifi, flags);
        dest.writeParcelable(this.positionGPS, flags);
    }

    public AnyUserData() {
    }

    protected AnyUserData(Parcel in) {
        this.selectedFloor = in.readParcelable(FloorModel.class.getClassLoader());
        this.selectedBuilding = in.readParcelable(BuildingModel.class.getClassLoader());
        this.navBuilding = in.readParcelable(BuildingModel.class.getClassLoader());
        this.navPois = in.createTypedArrayList(PoisNav.CREATOR);
        this.positionIP = in.readParcelable(GeoPoint.class.getClassLoader());
        this.positionCoordsWifi = in.readParcelable(GeoPoint.class.getClassLoader());
        this.positionGPS = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Parcelable.Creator<AnyUserData> CREATOR = new Parcelable.Creator<AnyUserData>() {
        @Override
        public AnyUserData createFromParcel(Parcel source) {
            return new AnyUserData(source);
        }

        @Override
        public AnyUserData[] newArray(int size) {
            return new AnyUserData[size];
        }
    };
}

