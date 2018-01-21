package com.aghnavi.agh_navi.dmsl.nav;

import android.os.Parcel;
import android.os.Parcelable;

//TODO: make getters later
public class PoisNav implements Parcelable {

    public String lat;
    public String lon;
    public String puid;

    public String buid;
    public String floor_number;

    //never used
    public String toString() {
        return "puid";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lat);
        dest.writeString(this.lon);
        dest.writeString(this.puid);
        dest.writeString(this.buid);
        dest.writeString(this.floor_number);
    }

    public PoisNav() {
    }

    protected PoisNav(Parcel in) {
        this.lat = in.readString();
        this.lon = in.readString();
        this.puid = in.readString();
        this.buid = in.readString();
        this.floor_number = in.readString();
    }

    public static final Parcelable.Creator<PoisNav> CREATOR = new Parcelable.Creator<PoisNav>() {
        @Override
        public PoisNav createFromParcel(Parcel source) {
            return new PoisNav(source);
        }

        @Override
        public PoisNav[] newArray(int size) {
            return new PoisNav[size];
        }
    };
}
