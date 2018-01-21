package com.aghnavi.agh_navi.Location;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Scarf_000 on 14.11.2017.
 */

public class LocationSerializable implements Serializable{
    private String id;
    private int floor;
    private String name;
    private String description;
    private String type;
    private double lat;
    private double lng;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public LocationSerializable(String name, String description, String type, double lat, double lng, int floor) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.floor = floor;
        this.id = createId();
    }

    private String createId(){
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId;
    }

    public String toString(){
        String string = new String();
        string = string.concat("Title: "+this.getName() + "\n");
        string = string.concat("Description: "+this.getDescription() + "\n");
        string = string.concat("Lat: "+this.getLat() + "\n");
        string = string.concat("Lng: "+this.getLng() + "\n");
        string = string.concat("Id: "+this.getId() + "\n");
        return string;
    }
}
