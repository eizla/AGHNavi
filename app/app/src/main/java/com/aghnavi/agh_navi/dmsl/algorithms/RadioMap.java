package com.aghnavi.agh_navi.dmsl.algorithms;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class RadioMap {

    private String NAN = "-110";
    private File radiomapMeanFile = null;
    private ArrayList<String> macAdressList = null;
    private HashMap<String, ArrayList<String>> locationRSSHashMap = null;
    private ArrayList<String> orderList = null;

    public RadioMap(File inFile) throws Exception {
        macAdressList = new ArrayList<>();
        locationRSSHashMap = new HashMap<>();
        orderList = new ArrayList<>();

        if (!ConstructRadioMap(inFile)) {
            throw new Exception("Inavlid Radiomap File");
        }
    }

    /**
     * Getter of MAC Address list in file order
     *
     * @return the list of MAC Addresses
     * */
    public ArrayList<String> getMacAdressList() {
        return macAdressList;
    }

    /**
     * Getter of HashMap Location-RSS Values list in no particular order
     *
     * @return the HashMap Location-RSS Values
     * */
    public HashMap<String, ArrayList<String>> getLocationRSS_HashMap() {
        return locationRSSHashMap;
    }

    /**
     * Getter of Location list in file order
     *
     * @return the Location list
     * */
    public ArrayList<String> getOrderList() {
        return orderList;
    }

    /**
     * Getter of radio map mean filename
     *
     * @return the filename of radiomap mean used
     * */
    public File getRadiomapMean_File() {
        return this.radiomapMeanFile;
    }

    public String getNaN() {
        return NAN;
    }

    /**
     * Construct a radio map
     *
     * @param inFile
     *            the radio map file to read
     *
     * @return true if radio map constructed successfully, otherwise false
     * */
    private boolean ConstructRadioMap(File inFile) {

        if (!inFile.exists() || !inFile.canRead()) {
            return false;
        }

        this.radiomapMeanFile = inFile;
        this.orderList.clear();
        this.macAdressList.clear();
        this.locationRSSHashMap.clear();

        ArrayList<String> RSS_Values;
        BufferedReader reader = null;
        String line;
        String[] temp;
        String key;

        try {
            reader = new BufferedReader(new FileReader(inFile));

            // Read the first line # NAN -110
            line = reader.readLine();
            temp = line.split(" ");
            if (!temp[1].equals("NaN"))
                return false;
            NAN = temp[2];
            line = reader.readLine();

            // Must exists
            if (line == null)
                return false;

            line = line.replace(", ", " ");
            temp = line.split(" ");

            final int startOfRSS = 4;

            // Must have more than 4 fields
            if (temp.length < startOfRSS)
                return false;

            // Store all Mac Addresses Heading Added
            this.macAdressList.addAll(Arrays.asList(temp).subList(startOfRSS, temp.length));

            while ((line = reader.readLine()) != null) {

                if (line.trim().equals(""))
                    continue;

                line = line.replace(", ", " ");
                temp = line.split(" ");

                if (temp.length < startOfRSS)
                    return false;

                key = temp[0] + " " + temp[1];

                RSS_Values = new ArrayList<>();

                RSS_Values.addAll(Arrays.asList(temp).subList(startOfRSS - 1, temp.length));

                // Equal number of MAC address and RSS Values
                if (this.macAdressList.size() != RSS_Values.size())
                    return false;

                this.locationRSSHashMap.put(key, RSS_Values);

                this.orderList.add(key);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    public static Collection<WeightedLatLng> readRadioMapLocations(File inFile) {
        class Weight {
            private String lat;
            private String lot;
            private int intensity;
        }

        //sixth decimal place is worth up to 0.11 m
        final int decimal_place = 6;
        HashMap<String, Weight> locations = new HashMap<>();
        BufferedReader reader = null;
        String line;
        String[] temp;
        String key;
        try {

            reader = new BufferedReader(new FileReader(inFile));

            // Read the first line # NAN -110
            line = reader.readLine();
            temp = line.split(" ");
            if (!temp[1].equals("NAN"))
                return null;

            line = reader.readLine();

            // Must exists
            if (line == null)
                return null;

            line = line.replace(", ", " ");
            temp = line.split(" ");

            final int startOfRSS = 4;

            // Must have more than 4 fields
            if (temp.length < startOfRSS)
                return null;

            while ((line = reader.readLine()) != null) {

                if (line.trim().equals(""))
                    continue;

                line = line.replace(", ", " ");
                temp = line.split(" ");

                if (temp.length < startOfRSS)
                    return null;

                String lat;
                String lot;
                try {
                    lat = temp[0].substring(0, temp[0].indexOf(".") + decimal_place);
                } catch (IndexOutOfBoundsException e) {
                    lat = temp[0];
                }

                try {
                    lot = temp[1].substring(0, temp[1].indexOf(".") + decimal_place);
                } catch (IndexOutOfBoundsException e) {
                    lot = temp[1];
                }

                key = lat + " " + lot;
                Weight weight = locations.get(key);
                if (weight == null) {
                    weight = new Weight();
                    weight.lat = temp[0];
                    weight.lot = temp[1];
                    locations.put(key, weight);
                }

                weight.intensity++;
            }

            Collection<WeightedLatLng> collection = new ArrayList<>();
            for (Weight w : locations.values()) {
                collection.add(new WeightedLatLng(new LatLng(Double.parseDouble(w.lat), Double.parseDouble(w.lot)), w.intensity));
            }

            return collection;
        } catch (Exception ex) {
            return null;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
        }
    }

    public String toString() {
        String str = "MAC Adresses: ";
        ArrayList<String> temp;
        for (int i = 0; i < macAdressList.size(); ++i)
            str += macAdressList.get(i) + " ";

        str += "\nLocations\n";
        for (String location : locationRSSHashMap.keySet()) {
            str += location + " ";
            temp = locationRSSHashMap.get(location);
            for (int i = 0; i < temp.size(); ++i)
                str += temp.get(i) + " ";
            str += "\n";
        }

        return str;
    }
}

