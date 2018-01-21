package com.aghnavi.agh_navi.dmsl.tracker;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.aghnavi.agh_navi.GlobalContextSingleton;
import com.aghnavi.agh_navi.dmsl.algorithms.LogRecord;
import com.aghnavi.agh_navi.dmsl.algorithms.RadioMap;
import com.aghnavi.agh_navi.dmsl.sensors.SensorsMain;
import com.aghnavi.agh_navi.dmsl.wifi.SimpleWifiManager;
import com.aghnavi.agh_navi.dmsl.wifi.WifiReceiver;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The main tracker component of Anyplace Navigator. Detects changes in WiFi
 * Scan results and Heading using the Positioning object
 *
 * Additionally it is the object that runs the algorithm that calculates the
 * position according to the RadioMap file provided. It notifies you for each
 * position calculated or if there is an error while calculating it.
 *
 */
public class AnyplaceTracker {

    // Wifi
    public interface WifiResultsAnyplaceTrackerListener {
        void onNewWifiResults(int aps);
    }

    private List<WifiResultsAnyplaceTrackerListener> wrlisteners = new ArrayList<WifiResultsAnyplaceTrackerListener>(1);

    public void addListener(WifiResultsAnyplaceTrackerListener list) {
        wrlisteners.add(list);
    }

    public void removeListener(WifiResultsAnyplaceTrackerListener list) {
        wrlisteners.remove(list);
    }

    private void triggerWiFiResultsListeners(int aps) {
        for (WifiResultsAnyplaceTrackerListener l : wrlisteners) {
            l.onNewWifiResults(aps);
        }
    }

    // Location
    public interface TrackedLocAnyplaceTrackerListener {
        void onNewLocation(LatLng pos);
    }

    private List<TrackedLocAnyplaceTrackerListener> tllisteners = new ArrayList<TrackedLocAnyplaceTrackerListener>(1);

    public void addListener(TrackedLocAnyplaceTrackerListener list) {
        tllisteners.add(list);
    }

    public void removeListener(TrackedLocAnyplaceTrackerListener list) {
        tllisteners.remove(list);
    }

    private void triggerTrackedLocListeners(LatLng pos) {
        for (TrackedLocAnyplaceTrackerListener l : tllisteners) {
            l.onNewLocation(pos);
        }
    }

    // Error
    public interface ErrorAnyplaceTrackerListener {
        void onTrackerError(String msg);
    }

    private List<ErrorAnyplaceTrackerListener> errorlisteners = new ArrayList<ErrorAnyplaceTrackerListener>(1);

    public void addListener(ErrorAnyplaceTrackerListener list) {
        errorlisteners.add(list);
    }

    public void removeListener(ErrorAnyplaceTrackerListener list) {
        errorlisteners.remove(list);
    }

    private void triggerErrorListeners(String msg) {
        for (ErrorAnyplaceTrackerListener l : errorlisteners) {
            l.onTrackerError(msg);
        }
    }

    // Flag to show if there is an ongoing progress
    private Boolean inProgress = false;
    private ExecutorService executorService;
    private Future future;
    private boolean trackMe;
    private boolean trackResume;
    // The latest scan list of APs and heading
    private ArrayList<LogRecord> latestScanList = new ArrayList<LogRecord>();
    private float RAWheading;

    // WiFi manager
    private SimpleWifiManager wifi;
    // WiFi Receiver
    private WifiReceiver receiverWifi;
    private SensorsMain positioning;

    // Algorithm
    private String radiomap_file;
    private byte algoChoice;
    private RadioMap rm;
    // private com.cy.wifi.algorithms.Algorithms algo; //RBF

    // flags
    private boolean isWifiOn = false;

    public AnyplaceTracker(SensorsMain positioning, Context context) {
        this.positioning = positioning;
        // WiFi manager to manage scans
        wifi = GlobalContextSingleton.getInstance(context).getSimpleWifiManager();
        // Create new receiver to get broadcasts
        receiverWifi = new SimpleWifiReceiver();
        trackMe = false;
        trackResume = false;

        executorService = Executors.newSingleThreadExecutor();
    }

    synchronized private boolean setProgress() {
        if (inProgress) {
            return false;
        }
        inProgress = true;
        return true;
    }

    synchronized private boolean unsetProgress() {
        inProgress = false;
        return true;
    }

    private boolean waitFindMe() {
        if (future != null && !future.isDone()) {
            try {
                future.get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }

        return true;
    }

    // Turn On Tracker
    public boolean trackOn() {

        if (!waitFindMe()) {
            triggerErrorListeners("Cannot start Tracker.");
            return false;
        }

        // Check that radiomap file is readable
        File file = new File(radiomap_file);

        if (!file.exists()) {
            triggerErrorListeners("Please download the required radiomap file.");
            return false;
        } else if (!file.canRead()) {
            triggerErrorListeners("Radiomap file is not readable.");
            return false;
        }

        resumeTracking();

        // RBF
		/*
		 * if (algoChoice == 0) { rm = null; algo = new
		 * com.cy.wifi.algorithms.Algorithms(this.radiomap_file); } else { algo
		 * = null;
		 */

        try {
            rm = new RadioMap(new File(radiomap_file));
        } catch (Exception e) {
            e.printStackTrace();
            triggerErrorListeners("Error while reading radio map.\nDownload new Radio Map and try again");
            return false;
        }

        // }

        trackMe = true;
        return true;
    }

    // Turn off Tracker
    public void trackOff() {

        waitFindMe();

        trackMe = false;

        pauseTracking();

        rm = null;
    }

    // used in Activity Pause
    public void pauseTracking() {
        if (isWifiOn) {
            isWifiOn = false;
            wifi.unregisterScan(receiverWifi);
        }

        trackResume = false;

    }

    // Used on Activity Resume
    public void resumeTracking() {
        if (!isWifiOn) {
            isWifiOn = true;
            wifi.registerScan(receiverWifi);
        }

        trackResume = true;
    }

    public boolean isTrackingOn() {
        // return trackMe.get();
        return trackMe;
    }

    // Called after select place
    public void setRadiomapFile(String radiomap_file) {
        this.radiomap_file = radiomap_file;
    }

    public void setAlgorithm(String name) {

        byte algoValue = 1;

        switch (name) {
            case "KNN":
                algoValue = 1;
                break;
            case "WKNN":
                algoValue = 2;
                break;
            case "MAP":
                algoValue = 3;
                break;
            case "MMSE":
                algoValue = 4;
                break;
        }

        // Close and Restart Tarcker
        if (trackMe && algoChoice == 0) {
            trackOff();
            algoChoice = algoValue;
            trackOn();
        } else {
            algoChoice = algoValue;
        }

    }

    /**
     * Starts the appropriate positioning algorithm
     * */
    private boolean findMe() {
        if (!setProgress()) {
            return false;
        }
        try {
            // long startTime = System.currentTimeMillis();
            if (latestScanList.isEmpty()) {
                triggerErrorListeners("No Access Point Received.\nWait for a scan first and try again.");
                return false;
            }

            String calculatedLocation = com.aghnavi.agh_navi.dmsl.algorithms.TrackAlgorithms.ProcessingAlgorithms(latestScanList, rm, algoChoice);

            if (calculatedLocation == null) {
                triggerErrorListeners("Can't find location. Check that radio map file refers to the same area.");
            } else {
                String[] temp = calculatedLocation.split(" ");
                LatLng trackedPosition = new LatLng(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
                triggerTrackedLocListeners(trackedPosition);
            }
            return true;

        } catch (Exception ex) {
            triggerErrorListeners("Tracker Exception" + ex.getMessage());
            return false;
        } finally {
            unsetProgress();
        }

    }

    /**
     * The WifiReceiver is responsible to Receive Access Points results
     * */
    private class SimpleWifiReceiver extends WifiReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {

            try {
                if (intent == null || c == null || intent.getAction() == null)
                    return;

                String action = intent.getAction();

                if (!action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                    return;

                // BroadcastReceiver.onReceive always run in the UI thread
                List<ScanResult> wifiList = wifi.getScanResults();
                //triggerWiFiResultsListeners(wifiList.size());

                if (trackMe && trackResume) {

                    if (future != null && !future.isDone()) {
                        return;
                    }

                    RAWheading = positioning.getRAWHeading();
                    latestScanList.clear();

                    LogRecord lr;
                    // If we receive results, add them to latest scan list
                    if (!wifiList.isEmpty()) {
                        for (int i = 0; i < wifiList.size(); i++) {
                            lr = new LogRecord(wifiList.get(i).BSSID, wifiList.get(i).level);
                            latestScanList.add(lr);
                        }
                    }
                    future = executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            findMe();
                        }
                    });
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    public void Destroy() {
        trackOff();
        executorService.shutdown();
    }
}
