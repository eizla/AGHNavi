package com.aghnavi.agh_navi.dmsl.nav;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;


//TODO: Add getters
@SuppressWarnings("serial")
public class FloorModel implements Comparable<FloorModel>, Serializable, Parcelable {

    public String buid;
    public String description;
    public String floor_name;
    public String floor_number;

    public String bottom_left_lat;
    public String bottom_left_lng;
    public String top_right_lat;
    public String top_right_lng;

    //used
    public String toString() {
        return floor_number + " - [" + floor_name + "]";
    }

    public boolean isFloorValid() {
        return !(floor_number == null || floor_number.equalsIgnoreCase("-") || floor_number.trim().isEmpty());
    }

    @Override
    public int compareTo(@NonNull FloorModel arg0) {
        return Integer.compare(Integer.parseInt(floor_number), Integer.parseInt(arg0.floor_number));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.buid);
        dest.writeString(this.description);
        dest.writeString(this.floor_name);
        dest.writeString(this.floor_number);
        dest.writeString(this.bottom_left_lat);
        dest.writeString(this.bottom_left_lng);
        dest.writeString(this.top_right_lat);
        dest.writeString(this.top_right_lng);
    }

    public FloorModel() {
    }

    protected FloorModel(Parcel in) {
        this.buid = in.readString();
        this.description = in.readString();
        this.floor_name = in.readString();
        this.floor_number = in.readString();
        this.bottom_left_lat = in.readString();
        this.bottom_left_lng = in.readString();
        this.top_right_lat = in.readString();
        this.top_right_lng = in.readString();
    }

    public static final Parcelable.Creator<FloorModel> CREATOR = new Parcelable.Creator<FloorModel>() {
        @Override
        public FloorModel createFromParcel(Parcel source) {
            return new FloorModel(source);
        }

        @Override
        public FloorModel[] newArray(int size) {
            return new FloorModel[size];
        }
    };
}
