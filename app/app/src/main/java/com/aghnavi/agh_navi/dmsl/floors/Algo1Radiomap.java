package com.aghnavi.agh_navi.dmsl.floors;


import android.content.Context;
import android.util.Log;

import com.aghnavi.agh_navi.dmsl.algorithms.LogRecord;
import com.aghnavi.agh_navi.dmsl.utils.AnyplaceUtils;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class Algo1Radiomap extends FloorSelector {

    private String[] files;
    private String[] floorNumbers;

    public Algo1Radiomap(final Context myContext) {
        super(myContext);
    }

    public void updateFiles(final String buid) {

        try {
            File radiomaps = AnyplaceUtils.getRadioMapsRootFolder(context);
            String[] file_names = radiomaps.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(buid);
                }
            });

            files = new String[file_names.length];
            floorNumbers = new String[file_names.length];

            for (int i = 0; i < file_names.length; i++) {
                floorNumbers[i] = file_names[i].substring(file_names[i].indexOf("fl_") + 3);
                files[i] = radiomaps.getAbsolutePath() + File.separator + file_names[i] + File.separator + AnyplaceUtils.getRadioMapFileName(floorNumbers[i]);
            }

        } catch (Exception e) {
            Log.e("Algo1Radiomap", e.getMessage());
        }
    }

    protected String    calculateFloor(Args args) throws Exception {

        if (files==null || files.length == 0)
            return "";

        GroupWifiFromRadiomap algo1 = new Algo1Help(args);

        for (int i = 0; i < files.length; i++) {

            algo1.run(new FileInputStream(files[i]), floorNumbers[i]);
        }

        return algo1.getFloor();
    }

    private static class Algo1Help extends GroupWifiFromRadiomap {

        final double a = 10;
        final double b = 10;
        final int l1 = 10;

        HashMap<String, Wifi> input = new HashMap<>();
        ArrayList<Score> mostSimilar = new ArrayList<>(10);

        private GeoPoint bbox[] = null;
        private Args args;

        Algo1Help(Args args) {
            super();

            this.args = args;
            if (!(args.dlat == 0 || args.dlong == 0)) {
                bbox = GeoPoint.getGeoBoundingBox(args.dlat, args.dlong, 100);
            }

            for (LogRecord listenObject : args.latestScanList) {
                input.put(listenObject.getBssid(), new Wifi(listenObject.getBssid(), listenObject.getRss()));
            }
        }

        private double compare(String[] macs, String line) {

            // # Timestamp, X, Y, HEADING, MAC Address of AP, RSS, FLOOR

            String[] segs = line.split(", ");
            long score = 0;
            int nNCM = 0;
            int nCM = 0;

            for (int i = 3; i < segs.length; i++) {
                if (!segs[i].equals(NaN)) {
                    if (input.containsKey(macs[i])) {
                        Integer diff = (Integer.parseInt(segs[i].split("\\.")[0]) - input.get(macs[i]).rss);
                        score += diff * diff;

                        nCM++;
                    } else {
                        nNCM++;
                    }
                }
            }

            return Math.sqrt(score) - a * nCM + b * nNCM;
        }

        private void checkScore(double similarity, String floor) {

            if (mostSimilar.size() == 0) {
                mostSimilar.add(new Score(similarity, floor));
                return;
            }

            for (int i = 0; i < mostSimilar.size(); i++) {
                if (mostSimilar.get(i).similarity > similarity) {
                    mostSimilar.add(i, new Score(similarity, floor));
                    if (mostSimilar.size() > l1) {
                        mostSimilar.remove(mostSimilar.size() - 1);
                    }
                    return;
                }
            }

            if (mostSimilar.size() < l1) {
                mostSimilar.add(new Score(similarity, floor));
            }

        }

        public String getFloor() {
            // Floor -Score
            HashMap<String, Integer> sum_floor_score = new HashMap<>();

            for (Score s : mostSimilar) {
                Integer score = 1;
                if (sum_floor_score.containsKey(s.floor)) {
                    score = sum_floor_score.get(s.floor) + 1;
                }

                sum_floor_score.put(s.floor, score);
            }

            String max_floor = "";
            int max_score = 0;

            for (String floor : sum_floor_score.keySet()) {
                int score = sum_floor_score.get(floor);
                if (max_score < score) {
                    max_score = score;
                    max_floor = floor;
                }
            }

            return max_floor;

        }

        protected void process(String maxMac, String[] macs, String line) {

            // # X, Y, HEADING, 00:16:b6:ee:00:7f, d4:d7:48:d8:28:30
            if (maxMac.equals(args.firstMac.getBssid()) || (args.secondMac != null && maxMac.equals(args.secondMac.getBssid()))) {

                if (bbox == null) {

                    double similarity = compare(macs, line);
                    checkScore(similarity, floor);
                } else {
                    String[] segs = line.split(",");
                    double x = Double.parseDouble(segs[0]);
                    double y = Double.parseDouble(segs[1]);

                    if (x > bbox[0].dlat && x < bbox[1].dlat && y > bbox[0].dlon && y < bbox[1].dlon) {
                        double similarity = compare(macs, line);
                        checkScore(similarity, floor);
                    }
                }
            }

        }

        private class Score {
            double similarity;
            String floor;

            Score(double similarity, String floor) {
                this.similarity = similarity;
                this.floor = floor;
            }
        }

        private class Wifi {
            String mac;
            Integer rss;

            Wifi(String mac, Integer rss) {
                this.mac = mac;
                this.rss = rss;
            }
        }
    }
}

