package com.aghnavi.agh_navi.dmsl.nav;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.aghnavi.agh_navi.dmsl.cache.AnyplaceCache;
import com.aghnavi.agh_navi.dmsl.tasks.FetchFloorsByBuidTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class BuildingModel implements Comparable<BuildingModel>, ClusterItem, Serializable, Parcelable {

    public interface FetchBuildingTaskListener {
        void onErrorOrCancel(String result);
        void onSuccess(String result, BuildingModel building);
    }

    public String buid = "";
    public String name;
    public String description;
    // public String address;
    // public String url;
    //TODO: getters
    public double latitude;
    public double longitude;

    // last fetched floors
    private List<FloorModel> mLoadedFloors = new ArrayList<>(0);
    // List index Used in SelectBuilding Activity
    private int selectedFloorIndex = 0;

    public List<FloorModel> getFloors() {
        return mLoadedFloors;
    }

    public void loadFloors(final FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener l, final Context ctx, boolean forceReload, boolean showDialog) {
        if (!forceReload && isFloorsLoaded()) {
            l.onSuccess("Successfully read from cache", mLoadedFloors);
        } else {
            new FetchFloorsByBuidTask(new FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener() {

                @Override
                public void onSuccess(String result, List<FloorModel> floors) {
                    mLoadedFloors = floors;
                    AnyplaceCache.saveInstance(ctx);
                    l.onSuccess(result, floors);
                }

                @Override
                public void onErrorOrCancel(String result) {
                    l.onErrorOrCancel(result);
                }

            }, ctx, buid, showDialog).execute();
        }
    }

    public boolean isFloorsLoaded() {
        return mLoadedFloors.size() != 0;
    }

    public FloorModel getSelectedFloor() {
        FloorModel f = null;
        try {
            f = mLoadedFloors.get(selectedFloorIndex);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return f;
    }

    public int getSelectedFloorIndex() {
        return selectedFloorIndex;
    }

    public FloorModel getFloorFromNumber(String floor_number) {
        Integer index = checkFloorIndex(floor_number);
        if (index == null) {
            return null;
        }
        return mLoadedFloors.get(index);
    }

    // Set Currently Selected floor number
    public boolean setSelectedFloor(String floor_number) {
        Integer floor_index = checkFloorIndex(floor_number);
        if (floor_index != null) {
            selectedFloorIndex = floor_index;
            return true;
        } else {
            return false;
        }
    }

    // Set Currently Selected floor number (array index)
    public boolean checkIndex(int floor_index) {
        return floor_index >= 0 && floor_index < mLoadedFloors.size();
    }

    public Integer checkFloorIndex(String floor_number) {
        Integer index = null;

        for (int i = 0; i < mLoadedFloors.size(); i++) {
            FloorModel floorModel = mLoadedFloors.get(i);
            if (floorModel.floor_number.equals(floor_number)) {
                index = i;
                break;
            }
        }
        return index;
    }

    //never used
    @Override
    public String toString() {
        // return name + " [" + description + "]";
        return name;
    }

    public boolean equals(Object object2) {
        return object2 instanceof BuildingModel && buid.equals(((BuildingModel) object2).buid);
    }

    public String getLatitudeString() {
        return Double.toString(latitude);
    }

    public String getLongitudeString() {
        return Double.toString(longitude);
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public void setPosition(String latitude, String longitude) {
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
    }

    @Override
    public int compareTo(@NonNull BuildingModel arg0) {
        // ascending order
        return name.compareTo(arg0.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.buid);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeTypedList(this.mLoadedFloors);
        dest.writeInt(this.selectedFloorIndex);
    }

    public BuildingModel(Context ctx) {
    }

    protected BuildingModel(Parcel in) {
        this.buid = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.mLoadedFloors = in.createTypedArrayList(FloorModel.CREATOR);
        this.selectedFloorIndex = in.readInt();
    }

    public static final Parcelable.Creator<BuildingModel> CREATOR = new Parcelable.Creator<BuildingModel>() {
        @Override
        public BuildingModel createFromParcel(Parcel source) {
            return new BuildingModel(source);
        }

        @Override
        public BuildingModel[] newArray(int size) {
            return new BuildingModel[size];
        }
    };
}
