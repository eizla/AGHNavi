package com.aghnavi.agh_navi.dmsl.nav;


import java.io.Serializable;

public class PoisModel implements Serializable, IPoisClass {

    public String puid;
    public String buid;
    public String name;
    public String description = "";
    public String lat = "0.0";
    public String lng = "0.0";
    public String floor_name;
    public String floor_number;
    public String pois_type;
    public boolean is_building_entrance;
    public boolean source;

    public String toString(){
        return name + "[" + buid + "]";
    }

    @Override
    public double lat() {
        return Double.parseDouble(lat);
    }

    @Override
    public double lng() {
        return Double.parseDouble(lng);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Type type() {
        return Type.AnyPlacePOI;
    }

    @Override
    public String id() {
        return puid;
    }

    @Override
    public boolean source() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PoisModel)) return false;

        PoisModel poisModel = (PoisModel) o;

        return puid.equals(poisModel.puid);

    }

    @Override
    public int hashCode() {
        return puid.hashCode();
    }
}
