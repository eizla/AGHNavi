package com.aghnavi.agh_navi.dmsl.tracker;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RunningMedian {

    private static final int LENGTH = 10;
    private List<Double> lats;
    private List<Double> lonts;
    private List<Double> lats_history;
    private List<Double> lonts_history;

    RunningMedian(double lat0, double lot0) {
        lats = new ArrayList<>(LENGTH / 2);
        lonts = new ArrayList<>(LENGTH / 2);
        lats_history = new ArrayList<>(LENGTH / 2);
        lonts_history = new ArrayList<>(LENGTH / 2);
        reset(lat0, lot0);
    }

    public void reset(double lat0, double lot0) {
        lats.clear();
        lonts.clear();
        lats_history.clear();
        lonts_history.clear();
        Double xd = lat0;
        Double yd = lot0;
        lats.add(xd);
        lonts.add(yd);
        lats_history.add(xd);
        lonts_history.add(yd);
    }

    private void insertLat(double lat, int i) {
        Double xd = lat;
        lats.add(i, xd);
        lats_history.add(0, xd);

        if (lats.size() > LENGTH) {
            lats.remove(lats_history.remove(lats_history.size() - 1));
        }
    }

    private void insertLont(double lot, int i) {
        Double yd = lot;
        lonts.add(i, yd);
        lonts_history.add(0, yd);

        if (lonts.size() > LENGTH) {
            lonts.remove(lonts_history.remove(lonts_history.size() - 1));
        }
    }

    public LatLng update(double lat, double lot) {

        boolean waitX = true;
        boolean waitY = true;
        int i;
        final int size = lats.size();

        for (i = 0; i < size && (waitX || waitY); i++) {
            if (waitX && lat < lats.get(i)) {
                insertLat(lat, i);
                waitX = false;
            }
            if (waitY && lot < lonts.get(i)) {
                insertLont(lot, i);
                waitY = false;
            }
        }

        if (waitX) {
            insertLat(lat, i);
        }
        if (waitY) {
            insertLont(lot, i);
        }

        return new LatLng(lats.get(lats.size() / 2), lonts.get(lonts.size() / 2));
    }
}

