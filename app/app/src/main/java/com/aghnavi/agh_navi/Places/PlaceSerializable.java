package com.aghnavi.agh_navi.Places;

//region Usings
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

//endregion

public class PlaceSerializable implements Serializable {

    //region Fields

    private String id;
    private String name;
    private String address;
    private double longitude;
    private double latitude;
    private List<Integer> placeTypes;
    private float rating;
    private String uri;

    //endregion

    //region Constructor

    public PlaceSerializable(String id, String name, String address,LatLng latLng, List<Integer> placeTypes, float rating, String uri) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.longitude = latLng.longitude;
        this.latitude = latLng.latitude;
        this.placeTypes = placeTypes;
        this.rating = rating;
        this.uri = uri;
    }

    //endregion

    //region Getters and Setters

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<Integer> getPlaceTypes() {
        return placeTypes;
    }

    public void setPlaceTypes(List<Integer> placeTypes) {
        this.placeTypes = placeTypes;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    //endregion
}
