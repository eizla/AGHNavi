package com.aghnavi.agh_navi;


import java.io.File;

public class AnyplaceAPI {

    public final static String FLURRY_APIKEY = "insert_flurry_key";
    public final static Boolean FLURRY_ENABLE = false;
    public final static Boolean FLOOR_SELECTOR = true;

    // Lock Location to GPS
    public final static Boolean LOCK_TO_GPS = false;
    // Show Debug Messages
    public final static Boolean DEBUG_MESSAGES = false;
    // Wifi and GPS Data
    public final static Boolean DEBUG_WIFI = false;
    // API URLS
    public final static Boolean DEBUG_URL = false;

    // Load All Building's Floors and Radiomaps
    public final static Boolean PLAY_STORE = true;

    private static String getServerIPAddress(){
        if (!DEBUG_URL) {
            return "https://campusnavigation.preview.cloudart.pl";
        } else {
            return "https://campusnavigation.preview.cloudart.pl";
        }
    }

    private final static String PREDICT_FLOOR_ALGO1 = "/anyplace/position/predictFloorAlgo1";
    private final static String PREDICT_FLOOR_ALGO2 = "/anyplace/position/predictFloorAlgo2";

    private final static String RADIO_DOWNLOAD_XY = "/anyplace/position/radio_download_floor";
    private final static String RADIO_DOWNLOAD_BUID = "/anyplace/position/radio_by_building_floor";
    private final static String RADIO_UPLOAD_URL_API = "/anyplace/position/radio_upload";

    private final static String NAV_ROUTE_URL_API = "/anyplace/navigation/route";
    private final static String NAV_ROUTE_XY_URL_API = "/anyplace/navigation/route_xy";

    private final static String FLOOR_PLAN_DOWNLOAD = "/anyplace/floorplans";
    private final static String FLOOR_TILES_ZIP_DOWNLOAD = "/anyplace/floortiles/zip";

    public static String predictFloorAlgo1() {
        return getServerIPAddress() + PREDICT_FLOOR_ALGO1;
    }

    public static String predictFloorAlgo2() {
        return getServerIPAddress() + PREDICT_FLOOR_ALGO2;
    }

    public static String getRadioDownloadBuid() {
        return getServerIPAddress() + RADIO_DOWNLOAD_BUID;
    }

    public static String getRadioDownloadXY() {
        return getServerIPAddress() + RADIO_DOWNLOAD_XY;
    }

    public static String getRadioUploadUrl() {
        return getServerIPAddress() + RADIO_UPLOAD_URL_API;
    }

    public static String getNavRouteUrl() {
        return getServerIPAddress() + NAV_ROUTE_URL_API;
    }

    public static String getNavRouteXYUrl() {
        return getServerIPAddress() + NAV_ROUTE_XY_URL_API;
    }

    // --------------Select Building Activity--------------------------

    public static String getFetchBuildingsUrl() {
        return getServerIPAddress() + "/anyplace/mapping/building/all";
    }

    public static String getFetchBuildingsByBuidUrl() {
        return getServerIPAddress() + "/anyplace/navigation/building/id";
    }

    public static String getFetchFloorsByBuidUrl() {
        return getServerIPAddress() + "/anyplace/mapping/floor/all";
    }

    public static String getServeFloorTilesZipUrl(String buid, String floor_number) {
        return getServerIPAddress() + FLOOR_TILES_ZIP_DOWNLOAD + File.separatorChar + buid + File.separatorChar + floor_number;
    }

    // -------------- Near coordinates ----------------------------------

    private static String getFetchBuildingsCoordinatesUrl() {
        return getServerIPAddress() + "/anyplace/mapping/building/coordinates";
    }

    private static String getServeFloorPlanUrl(String buid, String floor_number) {
        return getServerIPAddress() + FLOOR_PLAN_DOWNLOAD + File.separatorChar + buid + File.separatorChar + floor_number;
    }

    // ----------------------------------------------------------------

    // --------------POIS Api--------------------------

    public static String getFetchPoisByBuidUrl() {
        return getServerIPAddress() + "/anyplace/mapping/pois/all_building";
    }

    public static String getFetchPoisByBuidFloorUrl() {
        return getServerIPAddress() + "/anyplace/mapping/pois/all_floor";
    }

    public static String getFetchPoisByPuidUrl() {
        return getServerIPAddress() + "/anyplace/navigation/pois/id";
    }

    // ------------------------------------------------

}

