package com.aghnavi.agh_navi.dmsl.tasks;


import android.support.annotation.NonNull;

import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FetchNearBuildingsTask {

    public List<Double> distances;
    public List<BuildingModel> buildings;

    public FetchNearBuildingsTask() {

    }

    public void run(Iterator<BuildingModel> buildings, String lat, String lon, int max_distance) {
        double dlat = Double.parseDouble(lat);
        double dlon = Double.parseDouble(lon);
        run(buildings, dlat, dlon, max_distance);
    }

    public void run(Iterator<BuildingModel> loadBuildings, double lat, double lon, int max_distance) {
        LinkedList<BuildingModelDistance> sorted = new LinkedList<BuildingModelDistance>();

        while (loadBuildings.hasNext()) {
            BuildingModel b = loadBuildings.next();
            BuildingModelDistance bmd = new BuildingModelDistance(b, lat, lon);
            if (bmd.distance < max_distance) {
                sorted.add(bmd);
            }
        }

        Collections.sort(sorted);

        this.distances = new ArrayList<>(sorted.size());
        this.buildings = new ArrayList<>(sorted.size());

        for (BuildingModelDistance bmd : sorted) {
            buildings.add(bmd.bm);
            distances.add(bmd.distance);
        }
    }

    private static class BuildingModelDistance implements Comparable<BuildingModelDistance> {
        public BuildingModel bm;
        public Double distance;

        BuildingModelDistance(BuildingModel bm, double lat, double lon) {
            this.bm = bm;
            distance = GeoPoint.getDistanceBetweenPoints(bm.longitude, bm.latitude, lon, lat, "");
        }

        @Override
        public int compareTo(@NonNull BuildingModelDistance arg0) {
            return this.distance.compareTo(arg0.distance);
        }

    }
}

