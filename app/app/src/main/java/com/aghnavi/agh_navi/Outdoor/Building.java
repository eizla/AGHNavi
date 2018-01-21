package com.aghnavi.agh_navi.Outdoor;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Building implements Serializable{

    @SerializedName("buid")
    private String id;
    @SerializedName("address")
    private String address;
    @SerializedName("is_published")
    private String isPublic;
    @SerializedName("coordinates_lat")
    private String latitude;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("bucode")
    private String code;
    @SerializedName("coordinates_lon")
    private String longitude;
    @SerializedName("url")
    private String url;

    public Building(String id, String address, String isPublic, String latitude, String name, String description, String code, String longitude, String url) {
        this.id = id;
        this.address = address;
        this.isPublic = isPublic;
        this.latitude = latitude;
        this.name = name;
        this.description = description;
        this.code = code;
        this.longitude = longitude;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

