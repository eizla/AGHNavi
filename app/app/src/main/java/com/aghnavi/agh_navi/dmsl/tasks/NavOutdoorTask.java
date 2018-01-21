package com.aghnavi.agh_navi.dmsl.tasks;


import android.os.AsyncTask;

import com.aghnavi.agh_navi.dmsl.google.api.GMapV2Direction;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class NavOutdoorTask extends AsyncTask<Void, Void, String> {

    public interface NavDirectionsListener {
        void onNavDirectionsAbort();
        void onNavDirectionsErrorOrCancel(String result);
        void onNavDirectionsSuccess(String result, List<LatLng> points);
    }

    private enum Status {
        SUCCESS, ERROR, ABORT
    }

    private NavDirectionsListener mListener;
    private GeoPoint fromPosition;
    private GeoPoint toPosition;
    private ArrayList<LatLng> directionPoints;
    private Status status = Status.ERROR;
    private boolean fromNPlaces; //neighbourhood places

    public NavOutdoorTask(NavDirectionsListener listener, GeoPoint fromPosition, GeoPoint toPosition, boolean fromNPlaces) {
        this.mListener = listener;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.fromNPlaces = fromNPlaces;
    }

    public void onPreExecute() {
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            if (fromPosition == null || toPosition == null) {
                status = Status.ABORT;
                return "Task Cancelled";
            }

            // Avoid Running if the user is in or near the building
            if(!fromNPlaces) {
                double distance = GeoPoint.getDistanceBetweenPoints(fromPosition.dlon, fromPosition.dlat, toPosition.dlon, toPosition.dlat, "");
                if (distance < 500) {
                    status = Status.ABORT;
                    return "Task Cancelled";
                }
            }

            GMapV2Direction md = new GMapV2Direction();
            Document doc = md.getDocument(fromPosition.dlat, fromPosition.dlon, toPosition, GMapV2Direction.MODE_DRIVING);
            directionPoints = md.getDirection(doc);
            status = Status.SUCCESS;
            return "Successfully plotted navigation route!";
        } catch (Exception e) {
            return "Error plotting navigation route. Exception[ " + e.getMessage() + " ]";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        switch (status) {
            case SUCCESS:
                // call the success listener
                mListener.onNavDirectionsSuccess(result, directionPoints);
                break;
            case ERROR:
                // call the error listener
                mListener.onNavDirectionsErrorOrCancel(result);
                break;
            case ABORT:
                mListener.onNavDirectionsAbort();
        }
    }
}
