package com.aghnavi.agh_navi.dmsl.tracker;


import com.aghnavi.agh_navi.dmsl.sensors.SensorsMain;
import com.aghnavi.agh_navi.dmsl.sensors.SensorsStepCounter;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

class IMU {

    // Location
    interface TrackedLocAnyplaceIMUListener {
        void onNewLocation(LatLng pos);
    }

    private List<TrackedLocAnyplaceIMUListener> tllisteners = new ArrayList<>(1);

    public void addListener(TrackedLocAnyplaceIMUListener list) {
        tllisteners.add(list);
    }

    public void removeListener(TrackedLocAnyplaceIMUListener list) {
        tllisteners.remove(list);
    }

    private void triggerTrackedLocListeners(LatLng pos) {
        for (TrackedLocAnyplaceIMUListener l : tllisteners) {
            l.onNewLocation(pos);
        }
    }

    private final float STEP_LENGTH_KM;
    private double currLat;
    private double currLong;
    private float prevSteps;

    private SensorsMain sensorsMain;

    public IMU(SensorsMain sensorsMain, SensorsStepCounter sensorsStep, double currLat, double currLong) {
        this.sensorsMain = sensorsMain;
        this.currLat = currLat;
        this.currLong = currLong;
        this.STEP_LENGTH_KM = sensorsStep.getStepLength();
        sensorsStep.addListener(new StepListenerInit(sensorsStep));
    }

    public void reset(double lat0, double lot0) {
        synchronized (this) {
            currLat = lat0;
            currLong = lot0;
        }
    }

    // Same as GeoPoint getNewPointFromDistanceBearing
    private static LatLng calculateNewPoint(double lat, double lot, double distanceKM, double angleDegrees) {

        final int R = 6371;
        double angleRad = Math.toRadians(angleDegrees);
        double lat1, lat2, lon1, lon2;

        lat1 = Math.toRadians(lat); // #Current lat point converted to radians
        lon1 = Math.toRadians(lot); // #Current long point converted to radians
        lat2 = Math.asin(Math.sin(lat1) * Math.cos(distanceKM / R) + Math.cos(lat1) * Math.sin(distanceKM / R) * Math.cos(angleRad));
        lon2 = lon1 + Math.atan2(Math.sin(angleRad) * Math.sin(distanceKM / R) * Math.cos(lat1), Math.cos(distanceKM / R) - Math.sin(lat1) * Math.sin(lat2));

        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));

    }

    private class StepListenerInit implements SensorsStepCounter.IStepListener {

        SensorsStepCounter sensorsStep;

        StepListenerInit(SensorsStepCounter sensorsStep) {
            this.sensorsStep = sensorsStep;
        }

        @Override
        public void onNewStep(float value) {
            prevSteps = value;
            sensorsStep.removeListener(this);
            sensorsStep.addListener(new StepListener());
        }
    }

    private class StepListener implements SensorsStepCounter.IStepListener {

        @Override
        public void onNewStep(float value) {
            synchronized (IMU.this) {
                LatLng loc = calculateNewPoint(currLat, currLong, STEP_LENGTH_KM * (value - prevSteps), sensorsMain.getRAWHeading());
                prevSteps = value;
                currLat = loc.latitude;
                currLong = loc.longitude;
                //Do not move trigger Listeners Up
                triggerTrackedLocListeners(loc);
            }
        }
    }
}
