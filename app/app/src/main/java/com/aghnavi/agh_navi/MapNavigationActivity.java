package com.aghnavi.agh_navi;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aghnavi.agh_navi.Calendar.CalendarActivity;
import com.aghnavi.agh_navi.Places.PlaceSerializable;
import com.aghnavi.agh_navi.Settings.InformationActivity;
import com.aghnavi.agh_navi.anyplace.old.activity.AnyplacePrefs;
import com.aghnavi.agh_navi.anyplace.old.activity.SearchPOIActivity;
import com.aghnavi.agh_navi.anyplace.old.activity.SelectBuildingActivity;
import com.aghnavi.agh_navi.dmsl.cache.AnyplaceCache;
import com.aghnavi.agh_navi.dmsl.cache.BackgroundFetchListener;
import com.aghnavi.agh_navi.dmsl.floors.Algo1Radiomap;
import com.aghnavi.agh_navi.dmsl.floors.Algo1Server;
import com.aghnavi.agh_navi.dmsl.floors.FloorSelector;
import com.aghnavi.agh_navi.dmsl.google.maps.AnyplaceMapTileProvider;
import com.aghnavi.agh_navi.dmsl.google.maps.MyBuildingsRenderer;
import com.aghnavi.agh_navi.dmsl.google.maps.VisiblePois;
import com.aghnavi.agh_navi.dmsl.nav.AnyPlaceSearchingHelper;
import com.aghnavi.agh_navi.dmsl.nav.AnyUserData;
import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.aghnavi.agh_navi.dmsl.nav.FloorModel;
import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.aghnavi.agh_navi.dmsl.nav.PoisNav;
import com.aghnavi.agh_navi.dmsl.sensors.MovementDetector;
import com.aghnavi.agh_navi.dmsl.sensors.SensorsMain;
import com.aghnavi.agh_navi.dmsl.sensors.SensorsStepCounter;
import com.aghnavi.agh_navi.dmsl.tasks.AnyplaceSuggestionsTask;
import com.aghnavi.agh_navi.dmsl.tasks.DeleteFolderBackgroundTask;
import com.aghnavi.agh_navi.dmsl.tasks.DownloadRadioMapTaskBuid;
import com.aghnavi.agh_navi.dmsl.tasks.FetchBuildingsTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchFloorPlanTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchFloorsByBuidTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchNearBuildingsTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchPoiByPuidTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchPoisByBuidTask;
import com.aghnavi.agh_navi.dmsl.tasks.NavIndoorPTPTask;
import com.aghnavi.agh_navi.dmsl.tasks.NavIndoorTask;
import com.aghnavi.agh_navi.dmsl.tasks.NavOutdoorTask;
import com.aghnavi.agh_navi.dmsl.tracker.AnyplaceTracker;
import com.aghnavi.agh_navi.dmsl.tracker.TrackerLogicPlusIMU;
import com.aghnavi.agh_navi.dmsl.utils.AndroidUtils;
import com.aghnavi.agh_navi.dmsl.utils.AnyplaceUtils;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;
import com.aghnavi.agh_navi.dmsl.utils.PopularRecentContainer;
import com.aghnavi.agh_navi.layout.map.MapWrapperLayout;
import com.aghnavi.agh_navi.layout.map.OnInfoWindowElemTouchListener;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapNavigationActivity extends BaseNavigatorActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        AnyplaceTracker.TrackedLocAnyplaceTrackerListener, AnyplaceTracker.ErrorAnyplaceTrackerListener,
        FloorSelector.FloorAnyplaceFloorListener, FloorSelector.ErrorAnyplaceFloorListener, SharedPreferences.OnSharedPreferenceChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final double csLat = 35.144569;
    private static final double csLon = 33.411107;
    private static final float mInitialZoomLevel = 19.0f;
    public static final String SHARED_PREFS_ANYPLACE = "AGH_NAVI";

    //PERMISSION CODES
    private static final int EXTERNAL_WRITE_STORAGE_PERMISSON = 23;
    private static final int EXTERNAL_READ_STORAGE_PERMISSION = 22;

    //REQUESTS CODES
    private final static int LOCATION_CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9001;
    private final static int SELECT_PLACE_ACTIVITY_RESULT = 1112;
    private final static int SEARCH_POI_ACTIVITY_RESULT = 1113;
    private final static int PREFERENCES_ACTIVITY_RESULT = 1114;
    private final static int POINT_TO_POINT_ACTIVITY_RESULT = 1115;
    private final static int CALENDAR_EVENT_ACTIVITY_RESULT = 1116;
    private final static int NEARBY_PLACE_ACTIVITY_RESULT = 1118;
    private final static int SETTINGS_ACTIVITY_RESULT = 1120;

    //RESPONSE CODES
    private final static int USED_RECENT_OR_POPULAR = 1116;
    //private final static int EVENT_RESULT_OK = 4444;
    //private final static int SOON_RESULT_OK = 5555;

    // Location API
    GoogleApiClient apiClient;
    //Location lastLocation;

    // Holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    // UI Elements
    private ProgressBar progressBar;
    private ImageButton btnNavigate;
    private ImageButton btnFloorUp;
    private ImageButton btnFloorDown;
    private TextView textFloor;
    private TextView detectedAPs;
    private ImageButton btnTrackme;
    private TextView textDebug;

    // Suggestions in search views
    private SearchView searchView;
    private AnyPlaceSearchingHelper.SearchTypes searchType = AnyPlaceSearchingHelper.SearchTypes.OUTDOOR_MODE;
    private AnyplaceSuggestionsTask mSuggestionsTask;

    // Tasks
    private DownloadRadioMapTaskBuid downloadRadioMapTaskBuid;
    private boolean floorChangeRequestDialog = false;
    private boolean mAutomaticGPSBuildingSelection;

    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    private boolean cameraUpdate = false;
    private float bearing;

    // Navigation
    private AnyUserData userData = null;
    // holds the lines for the navigation route on map
    private Polyline pathLineInside = null;
    private PolylineOptions pathLineOutdoorOptions = null;
    private Polyline pathLineOutdoor = null;

    private AnyplaceCache mAnyplaceCache = null;
    // holds the PoisModels and Markers on map
    private VisiblePois visiblePois = null;
    private ClusterManager<BuildingModel> mClusterManager;

    // AnyplaceTracker
    private SensorsMain sensorsMain; // acceleration and orientation
    private MovementDetector movementDetector; // walking vs standing
    private SensorsStepCounter sensorsStepCounter; // step counter
    private TrackerLogicPlusIMU lpTracker;
    private Algo1Radiomap floorSelector;
    private String lastFloor;

    private boolean isTrackingErrorBackground;
    private Marker userMarker = null;
    private Marker googlePlaceMarker = null;

    //Container for search actions
    private PopularRecentContainer searchContainer;
    //private FusedLocationProviderClient mFusedLocationClient;
    //private LocationCallback mLocationCallback;
    //private boolean mRequestingLocationUpdates = true;


    /**
     * MAIN CODE==>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.map_navigation_activity, contentFrameLayout);


        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        textFloor = (TextView) findViewById(R.id.floorNumber);
        userData = new AnyUserData();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, navigationLayout, R.string.app_name, R.string.app_name);
        toolbar.setContentInsetStartWithNavigation(0);
        navigationLayout.addDrawerListener(actionBarDrawerToggle);

        GlobalContextSingleton.getInstance(this).getSimpleWifiManager().startScan();

        sensorsMain = new SensorsMain(getApplicationContext());
        movementDetector = new MovementDetector();
        sensorsMain.addListener(movementDetector);
        sensorsStepCounter = new SensorsStepCounter(getApplicationContext(), sensorsMain);
        lpTracker = new TrackerLogicPlusIMU(movementDetector, sensorsMain, sensorsStepCounter, getApplicationContext());
        floorSelector = new Algo1Radiomap(getApplicationContext());

        mAnyplaceCache = AnyplaceCache.getInstance(this);
        visiblePois = new VisiblePois();

        searchContainer = new PopularRecentContainer();

        setUpMapIfNeeded();

        btnNavigate = (FloatingActionButton) findViewById(R.id.map_floating_navigation_button);
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pointToPointIntent = new Intent(MapNavigationActivity.this, PointToPointActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("userdata", userData);
                pointToPointIntent.putExtra("search_container", searchContainer);
                GlobalContextSingleton.getInstance(getApplicationContext()).setUserData(userData);
                startActivityForResult(pointToPointIntent, POINT_TO_POINT_ACTIVITY_RESULT);
            }
        });

        btnTrackme = (FloatingActionButton) findViewById(R.id.map_location_button);
        btnTrackme.setImageResource(R.drawable.anyplace_dark_device_access_location_off);
        isTrackingErrorBackground = true;
        btnTrackme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GeoPoint gpsLoc = userData.getLocationGPSorIP();
                if (gpsLoc != null) {
                    AnyplaceCache mAnyplaceCache = AnyplaceCache.getInstance(MapNavigationActivity.this);
                    mAnyplaceCache.loadWorldBuildings(new FetchBuildingsTask.FetchBuildingsTaskListener() {
                        @Override
                        public void onSuccess(String result, List<BuildingModel> buildings) {
                            final FetchNearBuildingsTask nearest = new FetchNearBuildingsTask();
                            nearest.run(buildings.iterator(), gpsLoc.lat, gpsLoc.lng, 200);
                            if (nearest.buildings.size() > 0 && (userData.getSelectedBuildingId() == null || !userData.getSelectedBuildingId().equals(nearest.buildings.get(0).buid))) {
                                floorSelector.Stop();
                                final FloorSelector floorSelectorAlgo1 = new Algo1Server(getApplicationContext());
                                final ProgressDialog floorSelectorDialog = new ProgressDialog(MapNavigationActivity.this);

                                floorSelectorDialog.setIndeterminate(true);
                                floorSelectorDialog.setTitle("Wykrywanie piętra");
                                floorSelectorDialog.setMessage("Proszę poczekać...");
                                floorSelectorDialog.setCancelable(true);
                                floorSelectorDialog.setCanceledOnTouchOutside(false);
                                floorSelectorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        floorSelectorAlgo1.Destroy();
                                        bypassSelectBuildingActivity(nearest.buildings.get(0), "0", false);
                                    }
                                });

                                class Callback implements FloorSelector.ErrorAnyplaceFloorListener, FloorSelector.FloorAnyplaceFloorListener {
                                    @Override
                                    public void onNewFloor(String floor) {
                                        floorSelectorAlgo1.Destroy();
                                        if (floorSelectorDialog.isShowing()) {
                                            floorSelectorDialog.dismiss();
                                            bypassSelectBuildingActivity(nearest.buildings.get(0), floor, false);
                                        }
                                    }

                                    @Override
                                    public void onFloorError(Exception ex) {
                                        floorSelectorAlgo1.Destroy();
                                        if (floorSelectorDialog.isShowing()) {
                                            floorSelectorDialog.dismiss();
                                            bypassSelectBuildingActivity(nearest.buildings.get(0), "0", false);
                                        }
                                    }
                                }
                                Callback callback = new Callback();
                                floorSelectorAlgo1.addListener((FloorSelector.FloorAnyplaceFloorListener) callback);
                                floorSelectorAlgo1.addListener((FloorSelector.ErrorAnyplaceFloorListener) callback);
                                // Show Dialog
                                floorSelectorDialog.show();
                                floorSelectorAlgo1.Start(gpsLoc.lat, gpsLoc.lng);
                            } else {
                                focusUserLocation();
                                // Clear cancel request
                                lastFloor = null;
                                floorSelector.RunNow();
                                lpTracker.reset();
                            }
                        }

                        @Override
                        public void onErrorOrCancel(String result) {
                        }
                    }, MapNavigationActivity.this, false);
                } else {
                    focusUserLocation();
                    // Clear cancel request
                    lastFloor = null;
                    floorSelector.RunNow();
                    lpTracker.reset();
                }

            }
        });

        btnFloorUp = (FloatingActionButton) findViewById(R.id.map_floor_up_button);
        btnFloorUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!userData.isFloorSelected()) {
                    Toast.makeText(getBaseContext(), "Najpiew wybierz budynek!", Toast.LENGTH_SHORT).show();
                    return;
                }

                BuildingModel b = userData.getSelectedBuilding();
                if (b == null) {
                    return;
                }

                if (userData.isNavBuildingSelected()) {
                    // Move to start/destination poi's floor
                    String floor_number;
                    List<PoisNav> puids = userData.getNavPois();
                    // Check start and destination floor number
                    if (!puids.get(puids.size() - 1).floor_number.equals(puids.get(0).floor_number)) {
                        if (userData.getSelectedFloorNumber().equals(puids.get(puids.size() - 1).floor_number)) {
                            floor_number = puids.get(0).floor_number;
                        } else {
                            floor_number = puids.get(puids.size() - 1).floor_number;
                        }

                        FloorModel floor = b.getFloorFromNumber(floor_number);
                        if (floor != null) {
                            bypassSelectBuildingActivity(b, floor);
                            return;
                        }
                    }
                }
                // Move one floor up
                int index = b.getSelectedFloorIndex();

                if (b.checkIndex(index + 1)) {
                    bypassSelectBuildingActivity(b, b.getFloors().get(index + 1));
                }
            }
        });

        btnFloorDown = (FloatingActionButton) findViewById(R.id.map_floor_down_button);
        btnFloorDown.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!userData.isFloorSelected()) {
                    Toast.makeText(getBaseContext(), "Najpiew wybierz budynek!", Toast.LENGTH_SHORT).show();
                    return;
                }
                BuildingModel b = userData.getSelectedBuilding();
                if (b == null) {
                    return;
                }

                if (userData.isNavBuildingSelected()) {
                    // Move to start/destination poi's floor
                    String floor_number;
                    List<PoisNav> puids = userData.getNavPois();
                    // Check start and destination floor number
                    if (!puids.get(puids.size() - 1).floor_number.equals(puids.get(0).floor_number)) {
                        if (userData.getSelectedFloorNumber().equals(puids.get(puids.size() - 1).floor_number)) {
                            floor_number = puids.get(0).floor_number;
                        } else {
                            floor_number = puids.get(puids.size() - 1).floor_number;
                        }

                        FloorModel floor = b.getFloorFromNumber(floor_number);
                        if (floor != null) {
                            bypassSelectBuildingActivity(b, floor);
                            return;
                        }
                    }
                }

                // Move one floor down
                int index = b.getSelectedFloorIndex();

                if (b.checkIndex(index - 1)) {
                    bypassSelectBuildingActivity(b, b.getFloors().get(index - 1));
                }
            }

        });

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasPermissions(this, permissions)) {
            System.out.println("NOT GRANTED");
            requestForSpecificPermission(permissions);
        } else {
            System.out.println("ALREADY GRANTED");
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);

        /*
        mFusedLocationClient = getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        startLocationUpdates();
        */

        mAutomaticGPSBuildingSelection = true;

        // get/set settings
        PreferenceManager.setDefaultValues(this, SHARED_PREFS_ANYPLACE, MODE_PRIVATE, R.xml.anyplace_preferences_anyplace, true);
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS_ANYPLACE, MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        lpTracker.setAlgorithm(preferences.getString("TrackingAlgorithm", "WKNN"));

        // handle the search intent
        initCamera();
        handleIntent(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.anyplace_unified_options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Szukaj na zewnątrz");
        searchView.setAddStatesFromChildren(true);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(final String newText) {
                // return false; // false since we do not handle this call

                if (newText == null || newText.trim().length() < 1) {
                    if (mSuggestionsTask != null && !mSuggestionsTask.isCancelled()) {
                        mSuggestionsTask.cancel(true);
                    }
                    searchView.setSuggestionsAdapter(null);
                    return true;
                }
                if (mSuggestionsTask != null) {
                    mSuggestionsTask.cancel(true);
                }
                if (searchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                    if (!userData.isFloorSelected()) {
                        List<IPoisClass> places = new ArrayList<IPoisClass>(1);
                        PoisModel pm = new PoisModel();
                        pm.name = "Najpierw wybierz budynek...";
                        places.add(pm);
                        Cursor cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places);
                        showSearchResult(cursor);
                        return true;
                    }
                }
                GeoPoint gp = userData.getLatestUserPosition();

                mSuggestionsTask = new AnyplaceSuggestionsTask(new AnyplaceSuggestionsTask.AnyplaceSuggestionsListener() {
                    @Override
                    public void onSuccess(String result, List<? extends IPoisClass> pois) {
                        showSearchResult(AnyPlaceSearchingHelper.prepareSearchViewCursor(pois, newText));
                    }

                    @Override
                    public void onErrorOrCancel(String result) {
                        Log.d("AnyplaceSuggestions", result);
                    }

                    @Override
                    public void onUpdateStatus(String string, Cursor cursor) {
                        showSearchResult(cursor);
                    }
                }, MapNavigationActivity.this, searchType, (gp == null) ? new GeoPoint(csLat, csLon) : gp, newText, true);
                mSuggestionsTask.execute(null, null);

                // we return true to avoid caling the provider set in the xml
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        //Select building and floor to start navigating and positioning
        final SubMenu subMenuPlace = menu.addSubMenu("Wybierz budynek");
        final MenuItem sPlace = subMenuPlace.getItem();
        sPlace.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        sPlace.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // start the activity where the user can select the FROM and TO
                // pois he wants to navigate
                GeoPoint gp = userData.getLatestUserPosition();
                loadSelectBuildingActivity(gp, false);
                return true;
            }
        });

        // CLEAR NAVIGATION
        final SubMenu subMenuResetNav = menu.addSubMenu("Wyczyść nawigcję");
        final MenuItem ResetNav = subMenuResetNav.getItem();
        ResetNav.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        ResetNav.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                clearNavigationData();
                return true;
            }
        });

        // NAVIGATION PREFERENCES
        final SubMenu subMenuPreferences = menu.addSubMenu("Preferencje");
        final MenuItem prefsMenu = subMenuPreferences.getItem();
        prefsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        prefsMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(MapNavigationActivity.this, AnyplacePrefs.class);
                startActivityForResult(i, PREFERENCES_ACTIVITY_RESULT);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private void focusUserLocation() {
        if (userMarker != null) {
            if (AnyPlaceSearchingHelper.getSearchType(mMap.getCameraPosition().zoom) == AnyPlaceSearchingHelper.SearchTypes.OUTDOOR_MODE) {
                cameraUpdate = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), mInitialZoomLevel), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        cameraUpdate = false;
                    }

                    @Override
                    public void onCancel() {
                        cameraUpdate = false;
                    }
                });
            } else {
                cameraUpdate = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), mMap.getCameraPosition().zoom), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        cameraUpdate = false;
                    }

                    @Override
                    public void onCancel() {
                        cameraUpdate = false;
                    }
                });
            }
        }
    }

    private void showSearchResult(Cursor cursor) {
        String[] from = {SearchManager.SUGGEST_COLUMN_TEXT_1
                // ,SearchManager.SUGGEST_COLUMN_TEXT_2
        };
        int[] to = {android.R.id.text1
                // ,android.R.id.text2
        };
        AnyPlaceSearchingHelper.HTMLCursorAdapter adapter = new AnyPlaceSearchingHelper.HTMLCursorAdapter(MapNavigationActivity.this, R.layout.anyplace_queried_pois_item_1_searchbox, cursor, from, to);
        searchView.setSuggestionsAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // check what type of search we need
            AnyPlaceSearchingHelper.SearchTypes searchType = AnyPlaceSearchingHelper.getSearchType(mMap.getCameraPosition().zoom);
            String query = intent.getStringExtra(SearchManager.QUERY);
            GeoPoint gp = userData.getLatestUserPosition();

            // manually launch the real search activity
            Intent searchIntent = new Intent(MapNavigationActivity.this, SearchPOIActivity.class);
            // add query to the Intent Extras
            searchIntent.setAction(action);
            searchIntent.putExtra("searchType", searchType);
            searchIntent.putExtra("query", query);
            searchIntent.putExtra("lat", (gp == null) ? csLat : gp.dlat);
            searchIntent.putExtra("lng", (gp == null) ? csLon : gp.dlon);
            startActivityForResult(searchIntent, SEARCH_POI_ACTIVITY_RESULT);

        } else if (Intent.ACTION_VIEW.equals(action)) {
            String data = intent.getDataString();

            if (data != null && data.startsWith("http")) {
                final Uri uri = intent.getData();
                if (uri != null) {
                    String path = uri.getPath();
                    if (path != null && path.equals("/getnavigation")) {
                        String poid = uri.getQueryParameter("poid");
                        if (poid == null || poid.equals("")) {
                            // Share building
                            // http://anyplace.rayzit.com/getnavigation?buid=username_1373876832005&floor=0
                            String buid = uri.getQueryParameter("buid");
                            if (buid == null || buid.equals("")) {
                                Toast.makeText(getBaseContext(), "Buid parameter expected", Toast.LENGTH_SHORT).show();
                            } else {
                                mAutomaticGPSBuildingSelection = false;
                                mAnyplaceCache.loadBuilding(buid, new BuildingModel.FetchBuildingTaskListener() {
                                    @Override
                                    public void onSuccess(String result, final BuildingModel b) {
                                        bypassSelectBuildingActivity(b, uri.getQueryParameter("floor"), true);
                                    }

                                    @Override
                                    public void onErrorOrCancel(String result) {
                                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                                    }
                                }, MapNavigationActivity.this);
                            }
                        } else {
                            // Share POI
                            // http://anyplace.rayzit.com/getnavigation?poid=username_username_1373876832005_0_35.14424091022549_33.41139659285545_1382635428093
                            mAutomaticGPSBuildingSelection = false;
                            new FetchPoiByPuidTask(new FetchPoiByPuidTask.FetchPoiListener() {
                                @Override
                                public void onSuccess(String result, final PoisModel poi) {
                                    if (userData.getSelectedBuildingId() != null && userData.getSelectedBuildingId().equals(poi.buid)) {
                                        // Building is Loaded
                                        searchContainer.add(poi);
                                        startNavigationTask(poi.puid);
                                    } else {
                                        // Load Building
                                        mAnyplaceCache.loadBuilding(poi.buid, new BuildingModel.FetchBuildingTaskListener() {
                                            @Override
                                            public void onSuccess(String result, final BuildingModel b) {

                                                bypassSelectBuildingActivity(b, poi.floor_number, true, poi);

                                            }

                                            @Override
                                            public void onErrorOrCancel(String result) {
                                                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

                                            }
                                        }, MapNavigationActivity.this);
                                    }
                                }

                                @Override
                                public void onErrorOrCancel(String result) {
                                    Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                                }
                            }, this, poid).execute();
                        }
                    }
                }
            } else {
                // Search TextBox results only
                // PoisModel or Place Class
                System.out.println("ACTION::: " + action);
                IPoisClass place_selected = AnyPlaceSearchingHelper.getClassfromJson(data);
                if (place_selected.id() != null) {
                    // hide the search view when a navigation route is drawn
                    //if (searchView != null) {
                    //    searchView.setIconified(true);
                    //    searchView.clearFocus();
                    //}
                    searchView.setQuery(place_selected.name(), false);
                    searchView.clearFocus();
                    searchContainer.add(place_selected);
                    handleSearchPlaceSelection(place_selected);
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case LOCATION_CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                }
                break;
            case SEARCH_POI_ACTIVITY_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    // search activity finished OK
                    if (data == null)
                        return;
                    IPoisClass place = (IPoisClass) data.getSerializableExtra("ianyplace");
                    searchContainer.add(place);
                    handleSearchPlaceSelection(place);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // CANCELLED
                    if (data == null)
                        return;
                    String msg = (String) data.getSerializableExtra("message");
                    if (msg != null)
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case POINT_TO_POINT_ACTIVITY_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    searchContainer = (PopularRecentContainer) data.getSerializableExtra("search_container");

                    IPoisClass source_point = (IPoisClass) data.getSerializableExtra("source_point");
                    IPoisClass destination_point = (IPoisClass) data.getSerializableExtra("destination_point");
                    startPointToPointNavigationTask(source_point.id(), destination_point.id());
                    return;
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    return;
                } else if (resultCode == USED_RECENT_OR_POPULAR) {
                    searchContainer = (PopularRecentContainer) data.getSerializableExtra("search_container");

                    IPoisClass recentOrPopular = (IPoisClass) data.getSerializableExtra("used_rec_pop");
                    startNavigationTask(recentOrPopular.id());
                    return;
                }
            case SELECT_PLACE_ACTIVITY_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null)
                        return;
                    String fpf = data.getStringExtra("floor_plan_path");
                    if (fpf == null) {
                        Toast.makeText(getBaseContext(), "Niewybrano budynku oraz piętra!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        BuildingModel b = mAnyplaceCache.getSpinnerBuildings().get(data.getIntExtra("bmodel", 0));
                        FloorModel f = b.getFloors().get(data.getIntExtra("fmodel", 0));
                        selectPlaceActivityResult(b, f);
                    } catch (Exception ex) {
                        Toast.makeText(getBaseContext(), "Niewybrano budynku oraz piętra!", Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // CANCELLED
                    if (data == null)
                        return;
                    String msg = (String) data.getSerializableExtra("message");
                    if (msg != null)
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                }
                break;
            case PREFERENCES_ACTIVITY_RESULT:
                if (resultCode == RESULT_OK) {
                    AnyplacePrefs.Action result = (AnyplacePrefs.Action) data.getSerializableExtra("action");
                    switch (result) {
                        case REFRESH_BUILDING:
                            if (!userData.isFloorSelected()) {
                                Toast.makeText(getBaseContext(), "Najpierw wybierz budynek!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            if (progressBar.getVisibility() == View.VISIBLE) {
                                Toast.makeText(getBaseContext(), "Ładowanie budynku w toku. Proszę poczekać!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            try {
                                final BuildingModel b = userData.getSelectedBuilding();
                                // clear_floorplans
                                File floorsRoot = new File(AnyplaceUtils.getFloorPlansRootFolder(this), b.buid);
                                // clear radiomaps
                                File radiomapsRoot = AnyplaceUtils.getRadioMapsRootFolder(this);
                                final String[] radiomaps = radiomapsRoot.list(new FilenameFilter() {

                                    @Override
                                    public boolean accept(File dir, String filename) {
                                        if (filename.startsWith(b.buid))
                                            return true;
                                        else
                                            return false;
                                    }
                                });
                                for (int i = 0; i < radiomaps.length; i++) {
                                    radiomaps[i] = radiomapsRoot.getAbsolutePath() + File.separator + radiomaps[i];
                                }
                                floorSelector.Stop();
                                disableAnyplaceTracker();
                                DeleteFolderBackgroundTask task = new DeleteFolderBackgroundTask(new DeleteFolderBackgroundTask.DeleteFolderBackgroundTaskListener() {

                                    @Override
                                    public void onSuccess() {
                                        // clear any markers that might have already
                                        // been added to the map
                                        visiblePois.clearAll();
                                        // clear and resets the cached POIS inside
                                        // AnyplaceCache
                                        mAnyplaceCache.setPois(new HashMap<String, PoisModel>(), "");
                                        mAnyplaceCache.fetchAllFloorsRadiomapReset();
                                        bypassSelectBuildingActivity(b, b.getSelectedFloor());
                                    }
                                }, MapNavigationActivity.this, true);
                                task.setFiles(floorsRoot);
                                task.setFiles(radiomaps);
                                task.execute();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case REFRESH_MAP:
                            handleBuildingsOnMap(true);
                            break;
                    }
                }
                break;
            case CALENDAR_EVENT_ACTIVITY_RESULT:
                if (resultCode == EVENT_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "EVENT_OK");
                    Serializable gotEventPoi = data.getSerializableExtra("calendar_event");
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == SOON_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "SOON_OK");
                    Serializable gotSoonPoi = data.getSerializableExtra("calendar_event");
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == RESULT_OK) {
                    Log.d("NAV_NEARBY_PLACE", "OK");
                    PlaceSerializable placeSerializable = (PlaceSerializable) data.getSerializableExtra("place");
                    System.out.println(placeSerializable);
                    showGooglePoi(new LatLng(placeSerializable.getLatitude(), placeSerializable.getLongitude()));
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("NAV_CALENDAR", "CANCELLED");
                    Log.e("NAVIGATION", "Nawigacja do miejsca z wydarzenia nie powiadła się!");
                }
                break;
            case NEARBY_PLACE_ACTIVITY_RESULT:
                if (resultCode == EVENT_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "EVENT_OK");
                    Serializable gotEventPoi = data.getSerializableExtra("calendar_event");
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == SOON_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "SOON_OK");
                    Serializable gotSoonPoi = data.getSerializableExtra("calendar_event");
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == RESULT_OK) {
                    Log.d("NAV_NEARBY_PLACE", "OK");
                    PlaceSerializable placeSerializable = (PlaceSerializable) data.getSerializableExtra("place");
                    System.out.println(placeSerializable);
                    showGooglePoi(new LatLng(placeSerializable.getLatitude(), placeSerializable.getLongitude()));
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("NAV_CALENDAR", "CANCELLED");
                    Log.e("NAVIGATION", "Nawigacja do miejsca z wydarzenia nie powiadła się!");
                }
                break;
            case SETTINGS_ACTIVITY_RESULT:
                if (resultCode == EVENT_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "EVENT_OK");
                    Serializable gotEventPoi = data.getSerializableExtra("calendar_event");
                    //FIRE NAVIGATION
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == SOON_RESULT_OK) {
                    Log.d("NAV_CALENDAR", "SOON_OK");
                    Serializable gotSoonPoi = data.getSerializableExtra("calendar_event");
                    //FIRE NAVIGATION
                    //startNavigationTask(gotEventPoi.id);
                } else if (resultCode == RESULT_OK) {
                    PlaceSerializable placeSerializable = (PlaceSerializable) data.getSerializableExtra("place");
                    showGooglePoi(new LatLng(placeSerializable.getLatitude(), placeSerializable.getLongitude()));
                } else if (resultCode == RESULT_CANCELED) {
                    Log.e("NAVIGATION", "Nawigacja do pobliskiego miejsca nie powiadła się!");

                }
        }
    }

    /**
     * <==MAIN CODE
     */


    /**
     * KEY FEATURES==>
     */

    // Select Building Activity based on gps location
    private void loadSelectBuildingActivity(GeoPoint loc, boolean invisibleSelection) {
        Intent placeIntent = new Intent(MapNavigationActivity.this, SelectBuildingActivity.class);
        Bundle b = new Bundle();
        if (loc != null) {
            b.putString("coordinates_lat", String.valueOf(loc.dlat));
            b.putString("coordinates_lon", String.valueOf(loc.dlon));
        }
        b.putSerializable("mode", invisibleSelection ? SelectBuildingActivity.Mode.INVISIBLE : SelectBuildingActivity.Mode.NONE);
        placeIntent.putExtras(b);

        // start the activity where the user can select the building he is in
        startActivityForResult(placeIntent, SELECT_PLACE_ACTIVITY_RESULT);
    }

    private void bypassSelectBuildingActivity(final BuildingModel b, final String floor_number, final Boolean force) {
        // Load Building
        b.loadFloors(new FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener() {
            @Override
            public void onSuccess(String result, List<FloorModel> floors) {

                // Force loading of floor_number
                FloorModel floor;
                if ((floor = b.getFloorFromNumber(floor_number)) != null || !force) {
                    if (floor == null) {
                        floor = b.getSelectedFloor();
                    }

                    ArrayList<BuildingModel> list = new ArrayList<BuildingModel>(1);
                    list.add(b);
                    // Set building for Select Dialog
                    mAnyplaceCache.setSelectedBuildingIndex(0);
                    mAnyplaceCache.setSpinnerBuildings(list);

                    bypassSelectBuildingActivity(b, floor);

                } else {
                    Toast.makeText(getBaseContext(), "Piętro budynku nieznalezione!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorOrCancel(String result) {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

            }
        }, MapNavigationActivity.this, false, true);
    }

    private void bypassSelectBuildingActivity(final BuildingModel b, final String floor_number, final Boolean force, final PoisModel poi) {
        // Load Building
        b.loadFloors(new FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener() {

            @Override
            public void onSuccess(String result, List<FloorModel> floors) {

                // Force loading of floor_number
                FloorModel floor;
                if ((floor = b.getFloorFromNumber(floor_number)) != null || !force) {
                    if (floor == null) {
                        floor = b.getSelectedFloor();
                    }

                    ArrayList<BuildingModel> list = new ArrayList<BuildingModel>(1);
                    list.add(b);
                    // Set building for Select Dialog
                    mAnyplaceCache.setSelectedBuildingIndex(0);
                    mAnyplaceCache.setSpinnerBuildings(list);

                    bypassSelectBuildingActivity(b, floor, poi);

                } else {
                    Toast.makeText(getBaseContext(), "Piętro budynku nieznalezione!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorOrCancel(String result) {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

            }
        }, MapNavigationActivity.this, false, true);
    }

    private void bypassSelectBuildingActivity(final BuildingModel b, final FloorModel f) {

        final FetchFloorPlanTask fetchFloorPlanTask = new FetchFloorPlanTask(MapNavigationActivity.this, b.buid, f.floor_number);
        fetchFloorPlanTask.setCallbackInterface(new FetchFloorPlanTask.FetchFloorPlanTaskListener() {

            private ProgressDialog dialog;

            @Override
            public void onSuccess(String result, File floor_plan_file) {
                if (dialog != null)
                    dialog.dismiss();
                selectPlaceActivityResult(b, f);
            }

            @Override
            public void onErrorOrCancel(String result) {
                if (dialog != null)
                    dialog.dismiss();
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLongExecute() {
                dialog = new ProgressDialog(MapNavigationActivity.this);
                dialog.setIndeterminate(true);
                dialog.setTitle("Pobieranie planu piętra");
                dialog.setMessage("Proszę poczekać...");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        fetchFloorPlanTask.cancel(true);
                    }
                });
                dialog.show();
            }
        });
        fetchFloorPlanTask.execute();
    }

    private void bypassSelectBuildingActivity(final BuildingModel b, final FloorModel f, final PoisModel pm) {

        final FetchFloorPlanTask fetchFloorPlanTask = new FetchFloorPlanTask(MapNavigationActivity.this, b.buid, f.floor_number);
        fetchFloorPlanTask.setCallbackInterface(new FetchFloorPlanTask.FetchFloorPlanTaskListener() {

            private ProgressDialog dialog;

            @Override
            public void onSuccess(String result, File floor_plan_file) {
                if (dialog != null)
                    dialog.dismiss();
                selectPlaceActivityResult(b, f, pm);
            }

            @Override
            public void onErrorOrCancel(String result) {
                if (dialog != null)
                    dialog.dismiss();
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLongExecute() {
                dialog = new ProgressDialog(MapNavigationActivity.this);
                dialog.setIndeterminate(true);
                dialog.setTitle("Pobieranie planu piętra");
                dialog.setMessage("Proszę poczekać...");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        fetchFloorPlanTask.cancel(true);
                    }
                });
                dialog.show();
            }
        });
        fetchFloorPlanTask.execute();
    }

    private void selectPlaceActivityResult(final BuildingModel b, final FloorModel f, final PoisModel pm) {
        selectPlaceActivityResult_HELP(b, f);
        fetchPoisByBuidToCache(b.buid, new FetchPoisByBuidTask.FetchPoisListener() {
            @Override
            public void onSuccess(String result, Map<String, PoisModel> poisMap) {
                // This should never return null
                if (poisMap.get(pm.puid) == null) {
                    poisMap.put(pm.puid, pm);
                }
                handlePoisOnMap(poisMap.values());
                searchContainer.add(pm);
                startNavigationTask(pm.puid);
                selectPlaceActivityResult_HELP2(b, f);
            }

            @Override
            public void onErrorOrCancel(String result) {
                Collection<PoisModel> l = mAnyplaceCache.getPois();
                l.add(pm);
                handlePoisOnMap(l);
                searchContainer.add(pm);
                startNavigationTask(pm.puid);
                selectPlaceActivityResult_HELP2(b, f);
            }
        });
    }

    private void selectPlaceActivityResult(final BuildingModel b, final FloorModel f) {
        selectPlaceActivityResult_HELP(b, f);
        fetchPoisByBuidToCache(b.buid, new FetchPoisByBuidTask.FetchPoisListener() {
            @Override
            public void onSuccess(String result, Map<String, PoisModel> poisMap) {
                handlePoisOnMap(poisMap.values());
                loadIndoorOutdoorPath();
                selectPlaceActivityResult_HELP2(b, f);
            }

            @Override
            public void onErrorOrCancel(String result) {
                loadIndoorOutdoorPath();
                selectPlaceActivityResult_HELP2(b, f);
            }
        });
    }

    // Help tasks
    private void selectPlaceActivityResult_HELP(final BuildingModel b, final FloorModel f) {
        mAutomaticGPSBuildingSelection = false;
        floorSelector.Stop();
        disableAnyplaceTracker();

        // set the newly selected floor
        b.setSelectedFloor(f.floor_number);
        userData.setSelectedBuilding(b);
        userData.setSelectedFloor(f);
        textFloor.setText("Piętro: " + f.floor_name);

        // clean the map in case there are overlays
        mMap.clear();

        // add the Tile Provider that uses our Building tiles over Google Maps
        TileOverlay mTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(new AnyplaceMapTileProvider(getBaseContext(), b.buid, f.floor_number)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(b.getPosition(), 19.0f), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                cameraUpdate = false;
                handleBuildingsOnMap(false);
                updateLocation();
            }

            @Override
            public void onCancel() {
                cameraUpdate = false;
            }
        });
        // we must now change the radio map file since we changed floor RADIO MAP initialization
        try {
            File root = AnyplaceUtils.getRadioMapFolder(this, b.buid, userData.getSelectedFloorNumber());
            System.out.println(root.getAbsolutePath());
            lpTracker.setRadiomapFile(new File(root, AnyplaceUtils.getRadioMapFileName(userData.getSelectedFloorNumber())).getAbsolutePath());
        } catch (Exception e) {
            // exception thrown by GetRootFolder when sdcard is not writable
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Download RADIOMAP
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void selectPlaceActivityResult_HELP2(final BuildingModel b, final FloorModel f) {

        String trackedPositionLat = userData.getSelectedBuilding().getLatitudeString();
        String trackedPositionLon = userData.getSelectedBuilding().getLongitudeString();

        // first we should disable the tracker if it's working
        disableAnyplaceTracker();

        class Callback implements DownloadRadioMapTaskBuid.DownloadRadioMapListener, MapNavigationActivity.PreviousRunningTask {

            boolean progressBarEnabled = false;
            boolean disableSuccess = false;

            @Override
            public void onSuccess(String result) {
                if (disableSuccess) {
                    onErrorOrCancel("");
                    return;
                }
                // start the tracker
                enableAnyplaceTracker();

                // Download All Building Floors and Radiomaps
                if (AnyplaceAPI.PLAY_STORE) {

                    mAnyplaceCache.fetchAllFloorsRadiomapsRun(new BackgroundFetchListener() {

                        @Override
                        public void onSuccess(String result) {
                            //hideProgressBar();
                            if (AnyplaceAPI.DEBUG_MESSAGES) {
                                btnTrackme.setBackgroundColor(Color.YELLOW);
                            }
                            floorSelector.updateFiles(b.buid);
                            floorSelector.Start(b.getLatitudeString(), b.getLongitudeString());
                        }

                        @Override
                        public void onProgressUpdate(int progress_current, int progress_total) {
                            //progressBar.setProgress((int) ((float) progress_current / progress_total * progressBar.getMax()));
                        }

                        @Override
                        public void onErrorOrCancel(String result, ErrorType error) {
                            // Do not hide progress bar if previous task is running
                            // ErrorType.SINGLE_INSTANCE
                            // Do not hide progress bar because a new task will be created
                            // ErrorType.CANCELLED
                            if (error == BackgroundFetchListener.ErrorType.EXCEPTION) {
                                //hideProgressBar();
                            }
                        }

                        @Override
                        public void onPrepareLongExecute() {
                            //showProgressBar();
                        }

                    }, b);
                }
            }

            @Override
            public void onErrorOrCancel(String result) {
                if (progressBarEnabled) {
                    //hideProgressBar();
                }
            }

            @Override
            public void onPrepareLongExecute() {
                progressBarEnabled = true;
                //showProgressBar();
                // Set a smaller percentage than fetchAllFloorsRadiomapsOfBUID
                //progressBar.setProgress((int) (1.0f / (userData.getSelectedBuilding().getFloors().size() * 2) * progressBar.getMax()));
            }

            @Override
            public void disableSuccess() {
                disableSuccess = true;
            }

        }

        if (downloadRadioMapTaskBuid != null) {
            ((MapNavigationActivity.PreviousRunningTask) downloadRadioMapTaskBuid.getCallbackInterface()).disableSuccess();
        }

        downloadRadioMapTaskBuid = new DownloadRadioMapTaskBuid(new Callback(), this, trackedPositionLat, trackedPositionLon, userData.getSelectedBuildingId(), userData.getSelectedFloorNumber(), false);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            // Execute task parallel with others and multiple instances of
            // itself
            downloadRadioMapTaskBuid.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            downloadRadioMapTaskBuid.execute();
        }
    }


    //>NAVIGATION FUNCTIONS
    private void startNavigationTask(String id) {

        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "Brak połączenia z internetem!", Toast.LENGTH_SHORT).show();
            return;
        }

        // show the info window for the destination marker
        Marker marker = visiblePois.getMarkerFromPoisModel(id);
        if (marker != null) {
            marker.showInfoWindow();
        }

        final BuildingModel b = userData.getSelectedBuilding();
        final String currentFloor = userData.getSelectedFloorNumber();

        class Status {
            Boolean task1 = false;
            Boolean task2 = false;
        }
        final Status status = new Status();

        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Wyznaczanie trasy");
        dialog.setMessage("Proszę poczekać...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        PoisModel _entrance = null;
        GeoPoint pos = userData.getPositionWifi();
        if (pos == null) {
            // Find The nearest building entrance from the destination poi
            PoisModel _entranceGlobal = null;
            PoisModel _entrance0 = null;
            PoisModel _entranceCurrentFloor = null;
            double min = Double.MAX_VALUE;

            PoisModel dest = mAnyplaceCache.getPoisMap().get(id);
            for (PoisModel pm : mAnyplaceCache.getPoisMap().values()) {
                if (pm.is_building_entrance) {

                    if (pm.floor_number.equalsIgnoreCase(currentFloor)) {
                        double distance = Math.abs(pm.lat() - dest.lat()) + Math.abs(pm.lng() - dest.lng());
                        if (min > distance) {
                            _entranceCurrentFloor = pm;
                            min = distance;
                        }
                    } else if (pm.floor_number.equalsIgnoreCase("0")) {
                        _entrance0 = pm;
                    } else {
                        _entranceGlobal = pm;
                    }
                }
            }

            if (_entranceCurrentFloor != null) {
                _entrance = _entranceCurrentFloor;
            } else if (_entrance0 != null) {
                _entrance = _entrance0;
            } else if (_entranceGlobal != null) {
                _entrance = _entranceGlobal;
            } else {
                Toast.makeText(this, "Wejście do budynku nie znalezione!", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        // Does not run if entrance==null or is near the building
        final AsyncTask<Void, Void, String> async1f = new NavOutdoorTask(new NavOutdoorTask.NavDirectionsListener() {

            @Override
            public void onNavDirectionsSuccess(String result, List<LatLng> points) {
                onNavDirectionsFinished();

                if (!points.isEmpty()) {
                    // points.add(new LatLng(entrancef.dlat, entrancef.dlon));
                    pathLineOutdoorOptions = new PolylineOptions().addAll(points).width(10).color(Color.RED).zIndex(100.0f);
                    pathLineOutdoor = mMap.addPolyline(pathLineOutdoorOptions);
                }
            }

            @Override
            public void onNavDirectionsErrorOrCancel(String result) {
                onNavDirectionsFinished();
                // display the error cause
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNavDirectionsAbort() {
                onNavDirectionsFinished();
            }

            public void onNavDirectionsFinished() {
                status.task1 = true;
                if (status.task1 && status.task2)
                    dialog.dismiss();
                else {
                    // First task executed calls this
                    clearNavigationData();
                }
            }
        }, userData.getLocationGPSorIP(), (_entrance != null) ? new GeoPoint(_entrance.lat(), _entrance.lng()) : null, false);

        // start the navigation task
        final AsyncTask<Void, Void, String> async2f = new NavIndoorTask(new NavIndoorTask.NavRouteXYListener() {
            @Override
            public void onNavRouteSuccess(String result, List<PoisNav> points) {
                onNavDirectiosFinished();

                // set the navigation building and new points
                userData.setNavBuilding(b);
                userData.setNavPois(points);

                // handle drawing of the points
                handleIndoorPath(points);
            }

            @Override
            public void onNavRouteErrorOrCancel(String result) {
                onNavDirectiosFinished();
                // display the error cause
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            public void onNavDirectiosFinished() {
                status.task2 = true;
                if (status.task1 && status.task2)
                    dialog.dismiss();
                else {
                    // First task executed calls this
                    clearNavigationData();
                }
            }

        }, this, id, (pos == null) ? new GeoPoint(_entrance.lat(), _entrance.lng()) : pos, (pos == null) ? _entrance.floor_number : currentFloor);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                async1f.cancel(true);
                async2f.cancel(true);
            }
        });
        dialog.show();
        async1f.execute();
        async2f.execute();
    }

    private void startPointToPointNavigationTask(String source, String destination) {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "Brak połączenia z internetem!", Toast.LENGTH_SHORT).show();
            return;
        }

        final BuildingModel b = userData.getSelectedBuilding();

        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Wyznaczanie trasy");
        dialog.setMessage("Proszę poczekać...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);


        // start the navigation task
        final AsyncTask<Void, Void, String> asyncPTPNavigation = new NavIndoorPTPTask(new NavIndoorPTPTask.NavRouteListener() {
            @Override
            public void onNavRouteSuccess(String result, List<PoisNav> points) {
                onNavDirectionsFinished();

                // set the navigation building and new points
                userData.setNavBuilding(b);
                userData.setNavPois(points);

                // handle drawing of the points
                handleIndoorPath(points);
            }

            @Override
            public void onNavRouteErrorOrCancel(String result) {
                onNavDirectionsFinished();
                // display the error cause
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            void onNavDirectionsFinished() {
                dialog.dismiss();
                clearNavigationData();
            }

        }, this, source, destination);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                asyncPTPNavigation.cancel(true);
            }
        });
        dialog.show();
        asyncPTPNavigation.execute();

    }

    private void removeNavOverlays() {
        if (pathLineInside != null) {
            pathLineInside.remove();
        }
        if (pathLineOutdoor != null) {
            pathLineOutdoor.remove();
        }
        visiblePois.clearFromMarker();
        visiblePois.clearToMarker();
    }

    private void clearNavigationData() {
        if (userData != null) {
            userData.clearNav();
        }
        removeNavOverlays();
        btnFloorUp.setVisibility(View.VISIBLE);
        btnFloorDown.setVisibility(View.VISIBLE);
        visiblePois.clearGooglePlaceMarker();
    }

    // Loads the navigation route if any exists for the current floor selected
    private void loadIndoorOutdoorPath() {
        if (userData.isNavBuildingSelected()) {
            removeNavOverlays();
            handleIndoorPath(userData.getNavPois());
            if (pathLineOutdoorOptions != null) {
                pathLineOutdoor = mMap.addPolyline(pathLineOutdoorOptions);
            }
        } else {
            btnFloorUp.setVisibility(View.VISIBLE);
            btnFloorDown.setVisibility(View.VISIBLE);
        }
    }

    // draws the navigation route for the loaded floor
    private void handleIndoorPath(List<PoisNav> puids) {
        List<LatLng> p = new ArrayList<LatLng>();
        String selectedFloor = userData.getSelectedFloorNumber();
        for (PoisNav pt : puids) {
            // draw only the route for this floor
            if (pt.floor_number.equalsIgnoreCase(selectedFloor)) {
                p.add(new LatLng(Double.parseDouble(pt.lat), Double.parseDouble(pt.lon)));
            }
        }
        pathLineInside = mMap.addPolyline(new PolylineOptions().addAll(p).width(10).color(Color.RED).zIndex(100.0f));

        if (!puids.isEmpty()) {
            // add markers for starting and ending position
            // starting point
            PoisNav nrpFrom = puids.get(0);
            if (nrpFrom.floor_number.equalsIgnoreCase(selectedFloor))
                visiblePois.setFromMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(nrpFrom.lat), Double.parseDouble(nrpFrom.lon))).title("Starting Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation_source_icon))));
            // destination point
            PoisNav nrpTo = puids.get(puids.size() - 1);
            if (nrpTo.floor_number.equalsIgnoreCase(selectedFloor))
                visiblePois.setToMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(nrpTo.lat), Double.parseDouble(nrpTo.lon))).title("Final Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation_destination_icon))));

            // adjust floor buttons
            if (nrpTo.floor_number.equals(nrpFrom.floor_number)) {
                btnFloorUp.setVisibility(View.VISIBLE);
                btnFloorDown.setVisibility(View.VISIBLE);
            } else {
                // Go to Navigation Destination
                if (Integer.parseInt(nrpTo.floor_number) > Integer.parseInt(selectedFloor)) {
                    btnFloorDown.setVisibility(View.INVISIBLE);
                    btnFloorUp.setVisibility(View.VISIBLE);
                } else if (Integer.parseInt(nrpTo.floor_number) < Integer.parseInt(selectedFloor)) {
                    btnFloorUp.setVisibility(View.INVISIBLE);
                    btnFloorDown.setVisibility(View.VISIBLE);
                } else { // if Navigation Destination Floor Go to Navigation
                    // Start
                    if (Integer.parseInt(nrpFrom.floor_number) > Integer.parseInt(selectedFloor)) {
                        btnFloorDown.setVisibility(View.INVISIBLE);
                        btnFloorUp.setVisibility(View.VISIBLE);
                    } else {
                        btnFloorUp.setVisibility(View.INVISIBLE);
                        btnFloorDown.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    private void handleBuildingsOnMap(boolean forceReload) {
        AnyplaceCache mAnyplaceCache = AnyplaceCache.getInstance(MapNavigationActivity.this);
        mAnyplaceCache.loadWorldBuildings(new FetchBuildingsTask.FetchBuildingsTaskListener() {

            @Override
            public void onSuccess(String result, List<BuildingModel> buildings) {
                List<BuildingModel> collection = new ArrayList<BuildingModel>(buildings);
                mClusterManager.clearItems();
                BuildingModel buid = userData.getSelectedBuilding();
                if (buid != null)
                    collection.remove(buid);
                mClusterManager.addItems(collection);
                mClusterManager.cluster();
                // HACK. This dumps all the cached icons & recreates everything.
                mClusterManager.setRenderer(new MyBuildingsRenderer(MapNavigationActivity.this, mMap, mClusterManager));
            }

            @Override
            public void onErrorOrCancel(String result) {

            }

        }, this, forceReload);
    }
    //<NAVIGATION FUNCTIONS


    //>POIS
    private void handlePoisOnMap(Collection<PoisModel> collection) {

        visiblePois.clearAll();
        String currentFloor = userData.getSelectedFloorNumber();

        // Display part of Description Text Only
        // Make an approximation of available space based on map size
        final int fragmentWidth = (int) (findViewById(R.id.wrapped_map).getWidth() * 2);
        ViewGroup infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.anyplace_info_window, null);
        TextView infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
        TextPaint paint = infoSnippet.getPaint();

        for (PoisModel pm : collection) {
            if (pm.floor_number.equalsIgnoreCase(currentFloor)) {
                String snippet = AndroidUtils.fillTextBox(paint, fragmentWidth, pm.description);
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(pm.lat), Double.parseDouble(pm.lng))).title(pm.name).snippet(snippet).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_pin)));
                visiblePois.addMarkerAndPoi(m, pm);
            }
        }
    }

    private void fetchPoisByBuidToCache(final String buid, final FetchPoisByBuidTask.FetchPoisListener l) {
        // Check for cahced pois
        if (mAnyplaceCache.checkPoisBUID(buid)) {
            l.onSuccess("Pois read from cache", mAnyplaceCache.getPoisMap());
        } else {
            FetchPoisByBuidTask fetchPoisByBuidFloorTask = new FetchPoisByBuidTask(new FetchPoisByBuidTask.FetchPoisListener() {
                @Override
                public void onSuccess(String result, Map<String, PoisModel> poisMap) {
                    mAnyplaceCache.setPois(poisMap, buid);
                    l.onSuccess(result, poisMap);
                }

                @Override
                public void onErrorOrCancel(String result) {
                    // clear any markers that might have already been added to
                    // the map
                    visiblePois.clearAll();
                    // clear and resets the cached POIS inside AnyplaceCache
                    mAnyplaceCache.setPois(new HashMap<String, PoisModel>(), "");
                    l.onErrorOrCancel(result);
                }
            }, this, buid);

            fetchPoisByBuidFloorTask.execute();
        }
    }
    //<POIS


    // handle the selected place from the TextBox or search activity
    // either Anyplace POI or a Google Place
    private void handleSearchPlaceSelection(final IPoisClass place) {
        if (place == null)
            return;
        switch (place.type()) {
            case AnyPlacePOI:
                searchContainer.add(place);
                startNavigationTask(place.id());
                break;
            case GooglePlace:

                mAnyplaceCache.loadWorldBuildings(new FetchBuildingsTask.FetchBuildingsTaskListener() {

                    @Override
                    public void onSuccess(String result, List<BuildingModel> allBuildings) {
                        FetchNearBuildingsTask nearBuildings = new FetchNearBuildingsTask();
                        nearBuildings.run(allBuildings.iterator(), place.lat(), place.lng(), 200);

                        if (nearBuildings.buildings.size() > 0) {
                            final BuildingModel b = nearBuildings.buildings.get(0);
                            bypassSelectBuildingActivity(b, "0", false);
                        } else {
                            showGooglePoi(new LatLng(place.lat(), place.lng()));
                        }
                    }

                    @Override
                    public void onErrorOrCancel(String result) {
                        showGooglePoi(new LatLng(place.lat(), place.lng()));
                    }
                }, MapNavigationActivity.this, false);
                break;
        }
    }

    private void showGooglePoi(final LatLng place) {
        cameraUpdate = true;
        // add the marker for this Google Place

        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Wyznaczanie trasy");
        dialog.setMessage("Proszę poczekać...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        final AsyncTask<Void, Void, String> asyncOnlyOutdoorTask = new NavOutdoorTask(new NavOutdoorTask.NavDirectionsListener() {
            @Override
            public void onNavDirectionsSuccess(String result, List<LatLng> points) {
                onNavDirectionsFinished();

                if (!points.isEmpty()) {
                    googlePlaceMarker = mMap.addMarker(new MarkerOptions().position(place).icon(BitmapDescriptorFactory.fromResource(R.drawable.place_pin)));
                    pathLineOutdoorOptions = new PolylineOptions().addAll(points).width(10).color(Color.RED).zIndex(100.0f);
                    pathLineOutdoor = mMap.addPolyline(pathLineOutdoorOptions);
                }
            }

            @Override
            public void onNavDirectionsErrorOrCancel(String result) {
                onNavDirectionsFinished();
                // display the error cause
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNavDirectionsAbort() {
                onNavDirectionsFinished();
            }

            void onNavDirectionsFinished() {
                dialog.dismiss();
                clearNavigationData();
            }
        }, userData.getLocationGPSorIP(), new GeoPoint(place.latitude, place.longitude), true);

        visiblePois.setGooglePlaceMarker(googlePlaceMarker);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                asyncOnlyOutdoorTask.cancel(true);
            }
        });
        dialog.show();
        asyncOnlyOutdoorTask.execute();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.latitude, place.longitude), mInitialZoomLevel - 2), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                cameraUpdate = false;
            }

            @Override
            public void onCancel() {
                cameraUpdate = false;
            }
        });
    }

    /**
     * <==KEY FEATURES
     */


    /**
     * GOOGLE MAPS THINGS==>
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            mMap.setPadding(0, 55, 0, 0);
            mClusterManager = new ClusterManager<>(this, mMap);

            final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
            mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

            final ViewGroup infoWindow;
            final TextView infoTitle;
            final TextView infoSnippet;
            final ImageButton infoButton1;
            final OnInfoWindowElemTouchListener infoButtonListener1;
            final ImageButton infoButton2;
            final OnInfoWindowElemTouchListener infoButtonListener2;

            // We want to reuse the info window for all the markers,
            // so let's create only one class member instance
            infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.anyplace_info_window, null);
            infoTitle = (TextView) infoWindow.findViewById(R.id.title);
            infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
            infoButton1 = (ImageButton) infoWindow.findViewById(R.id.button1);
            infoButton2 = (ImageButton) infoWindow.findViewById(R.id.button2);

            // Setting custom OnTouchListener which deals with the pressed
            // state
            // so it shows up
            infoButtonListener1 = new OnInfoWindowElemTouchListener(infoButton1, getResources().getDrawable(R.drawable.anyplace_button_unsel), getResources().getDrawable(R.drawable.anyplace_button_sel)) {
                @Override
                protected void onClickConfirmed(View v, Marker marker) {

                    PoisModel poi = visiblePois.getPoisModelFromMarker(marker);
                    if (poi != null) {
                        // start the navigation using the clicked marker as
                        // destination
                        searchContainer.add(poi);
                        startNavigationTask(poi.puid);
                    }
                }
            };
            infoButton1.setOnTouchListener(infoButtonListener1);

            // Setting custom OnTouchListener which deals with the pressed
            // state
            // so it shows up
            infoButtonListener2 = new OnInfoWindowElemTouchListener(infoButton2, getResources().getDrawable(R.drawable.anyplace_button_unsel), getResources().getDrawable(R.drawable.anyplace_button_sel)) {
                @Override
                protected void onClickConfirmed(View v, Marker marker) {

                    PoisModel poi = visiblePois.getPoisModelFromMarker(marker);
                    if (poi != null) {
                        if (poi.description.equals("") || poi.description.equals("-")) {
                            // start the navigation using the clicked marker
                            // as destination
                            popup_msg("Brak opisu.", poi.name);
                        } else {
                            popup_msg(poi.description, poi.name);
                        }
                    }
                }
            };
            infoButton2.setOnTouchListener(infoButtonListener2);

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Setting up the infoWindow with current's marker info
                    infoTitle.setText(marker.getTitle());
                    infoSnippet.setText(marker.getSnippet());
                    infoButtonListener1.setMarker(marker);
                    infoButtonListener2.setMarker(marker);

                    // We must call this to set the current marker and
                    // infoWindow references
                    // to the MapWrapperLayout
                    mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                    return infoWindow;
                }
            });
            setUpMap();
        }
    }

    private void setUpMap() {
        initMap();
        //initCamera();
        initListeners();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.wrapped_map)).getMapAsync(this);
        mClusterManager = new ClusterManager<>(this, mMap);

    }

    private void initMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(false);
    }

    private void initCamera() {
        // Only for the first time
        if (userMarker != null) {
            return;
        }
        Location gps = null;
        try {
            gps = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            //gps = getLastLocation();
        } catch (SecurityException e) {
            Log.e("PERMISSION", "No location permission!");
        }
        if (gps != null) {
            cameraUpdate = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), mInitialZoomLevel), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    cameraUpdate = false;
                    handleBuildingsOnMap(false);
                }

                @Override
                public void onCancel() {
                    cameraUpdate = false;
                    handleBuildingsOnMap(false);
                }
            });
        } else {
            AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {
                GeoPoint location;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        location = AndroidUtils.getIPLocation();
                    } catch (Exception ignored) {
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (location != null && userMarker == null) {
                        userData.setLocationIP(location);
                        updateLocation();
                        cameraUpdate = true;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.dlat, location.dlon), mInitialZoomLevel), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                cameraUpdate = false;
                                handleBuildingsOnMap(false);
                            }

                            @Override
                            public void onCancel() {
                                cameraUpdate = false;
                                handleBuildingsOnMap(false);
                            }
                        });
                    } else {
                        handleBuildingsOnMap(false);
                    }

                }

            };
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }

    private void initListeners() {
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                // change search box message and clear pois
                if (searchType != AnyPlaceSearchingHelper.getSearchType(position.zoom)) {
                    searchType = AnyPlaceSearchingHelper.getSearchType(position.zoom);
                    if (searchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                        searchView.setQueryHint("Szukaj wewnątrz");
                        visiblePois.showAll();
                        if (pathLineInside != null)
                            pathLineInside.setVisible(true);
                    } else if (searchType == AnyPlaceSearchingHelper.SearchTypes.OUTDOOR_MODE) {
                        searchView.setQueryHint("Szukaj na zewnątrz");
                        visiblePois.hideAll();
                        if (pathLineInside != null)
                            pathLineInside.setVisible(false);
                    }
                }
                bearing = position.bearing;
                mClusterManager.onCameraChange(position);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // mClusterManager returns true if is a cluster item
                if (!mClusterManager.onMarkerClick(marker)) {
                    PoisModel poi = visiblePois.getPoisModelFromMarker(marker);
                    return poi == null;
                } else {
                    // Prevent Popup dialog
                    return true;
                }
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<BuildingModel>() {
            @Override
            public boolean onClusterClick(Cluster<BuildingModel> cluster) {
                // Prevent Popup dialog
                return true;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<BuildingModel>() {
            @Override
            public boolean onClusterItemClick(final BuildingModel b) {
                if (b != null) {
                    bypassSelectBuildingActivity(b, "0", false);
                }
                // Prevent Popup dialog
                return true;
            }
        });

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("AnyplaceNavigator", "This device is not supported.");
                finish();
            }
            return false;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkPlayServices()) {
            initCamera();
            Location currentLocation = null;

            try {
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                //currentLocation = getLastLocation();
            } catch (SecurityException e) {
                Log.e("PERMISSION", "No location permission!");
            }
            try {
                System.out.println("CRASH HERE");
                //startLocationUpdates();
                System.out.println("AFTER CRASH");
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, mLocationRequest, this);
            } catch (SecurityException e) {
                Log.e("PERMISSION", "No location permission");
            }
            if (currentLocation != null) {
                onLocationChanged(currentLocation);
            } else {
                if (mAutomaticGPSBuildingSelection)
                   Toast.makeText(getBaseContext(), "Lokalizacja niemożliwa w tym momencie! Spróbuj ponownie.", Toast.LENGTH_LONG).show();
            }
        }
    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Google Play services", "Disconnected. Please re-connect!");
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO - CHECK HOW THIS WORKS
        Log.d("Google Play Services", "Connection failed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, LOCATION_CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }



    /**
     * <==GOOGLE MAPS THINGS
     */


    /**
     * LOCATION LISTENERS==>
     */

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            userData.setLocationGPS(location);
            updateLocation();
            if (mAutomaticGPSBuildingSelection) {
                mAutomaticGPSBuildingSelection = false;
                loadSelectBuildingActivity(userData.getLatestUserPosition(), true);
            }
        }
    }

    /*
    private Location getLastLocation() throws SecurityException {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        lastLocation = location;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("LOCATION", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
        return lastLocation;
    }
    */

    @Override
    public void onNewLocation(final LatLng pos) {
        Log.d("TRACKER", "Maps -> onNewLocation");
        userData.setPositionWifi(pos.latitude, pos.longitude);
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (isTrackingErrorBackground) {
                    isTrackingErrorBackground = false;
                    btnTrackme.setImageResource(R.drawable.anyplace_dark_device_access_location_searching);
                }
                updateLocation();
            }
        });

    }

    @Override
    public void onTrackerError(final String msg) {
        if (!isTrackingErrorBackground)
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (!isTrackingErrorBackground) {
                        btnTrackme.setImageResource(R.drawable.anyplace_dark_device_access_location_off);
                        isTrackingErrorBackground = true;
                    }
                }
            });
    }

    @Override
    public void onFloorError(final Exception ex) {
        if (ex instanceof FloorSelector.NonCriticalError)
            return;
        floorSelector.Stop();
        Log.e("Floor Selector", ex.toString());
        Toast.makeText(getBaseContext(), "Błąd wyboru piętra (FloorSelector)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewFloor(final String floorNumber) {

        if (floorChangeRequestDialog)
            return;
        final BuildingModel b = userData.getSelectedBuilding();
        if (b == null) {
            Log.e("ON NEW FLOOR", "Floor Number Not found");
            return;
        }
        // Check if the floor is the loaded floor
        if (b.getSelectedFloor().floor_number.equals(floorNumber)) {
            lastFloor = null;
            return;
        }
        // User clicked Cancel
        if (lastFloor != null && lastFloor.equals(floorNumber)) {
            return;
        }
        lastFloor = floorNumber;
        final FloorModel f = b.getFloorFromNumber(floorNumber);
        if (f != null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapNavigationActivity.this);
            alertDialog.setTitle("Wykryto zmianę piętra");
            alertDialog.setMessage("Piętro: " + floorNumber + ". Kontynuować?");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    floorChangeRequestDialog = false;
                    bypassSelectBuildingActivity(b, f);
                }
            });
            alertDialog.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    floorChangeRequestDialog = false;
                }
            });
            alertDialog.show();
            floorChangeRequestDialog = true;
        }
    }

    private void updateLocation() {
        GeoPoint location = userData.getLatestUserPosition();
        if (location != null) {
            if (userMarker != null) {
                userMarker.remove();
            }
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(location.dlat, location.dlon));
            marker.title("User").snippet("Estimated Position");
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.anyplace_icon21));
            marker.rotation(sensorsMain.getRAWHeading() - bearing);
            userMarker = this.mMap.addMarker(marker);
        }
    }

    /**
     *  <==LOCATION LISTENERS
     */


    /**
     * TRACKERS HELPER FUNCTIONS==>
     */

    private void enableAnyplaceTracker() {
        if (lpTracker.trackOn()) {
            btnTrackme.setImageResource(R.drawable.anyplace_dark_device_access_location_searching);
            isTrackingErrorBackground = false;
        }
    }

    private void disableAnyplaceTracker() {
        lpTracker.trackOff();
        btnTrackme.setImageResource(R.drawable.anyplace_dark_device_access_location_off);
        isTrackingErrorBackground = true;
    }

    private void addTrackerListeners() {
        //lpTracker.addListener((AnyplaceTracker.WifiResultsAnyplaceTrackerListener) this);
        lpTracker.addListener((AnyplaceTracker.TrackedLocAnyplaceTrackerListener) this);
        lpTracker.addListener((AnyplaceTracker.ErrorAnyplaceTrackerListener) this);
        floorSelector.addListener((FloorSelector.FloorAnyplaceFloorListener) this);
        floorSelector.addListener((FloorSelector.ErrorAnyplaceFloorListener) this);
    }

    private void removeTrackerListeners() {
        //lpTracker.removeListener((AnyplaceTracker.WifiResultsAnyplaceTrackerListener) this);
        lpTracker.removeListener((AnyplaceTracker.TrackedLocAnyplaceTrackerListener) this);
        lpTracker.removeListener((AnyplaceTracker.ErrorAnyplaceTrackerListener) this);
        floorSelector.removeListener((FloorSelector.FloorAnyplaceFloorListener) this);
        floorSelector.removeListener((FloorSelector.ErrorAnyplaceFloorListener) this);
    }

    /**
     * <==TRACKERS HELPER FUNCTIONS
     */


    /**
     * SOME STRANGE SHIT==>
     */

    private void popup_msg(String msg, String title) {
        AlertDialog.Builder alert_box = new AlertDialog.Builder(this);
        alert_box.setTitle(title);
        alert_box.setMessage(msg);

        alert_box.setNeutralButton("Ukryj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alert_box.create();
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_SEARCH:
                // Handle these events so they don't launch the Camera app
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    interface PreviousRunningTask {
        void disableSuccess();
    }

    /**
     * <==SOME STRANGE SHIT
     */


    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();

        // Flurry Analytics
        if (AnyplaceAPI.FLURRY_ENABLE) {
            FlurryAgent.onStartSession(this, AnyplaceAPI.FLURRY_APIKEY);
        }

        Runnable checkGPS = new Runnable() {
            @Override
            public void run() {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfGPS == false) {
                    AndroidUtils.showGPSSettings(MapNavigationActivity.this);
                }
            }
        };

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled() || !NetworkUtils.isOnline(MapNavigationActivity.this)) {
            AndroidUtils.showWifiSettings(this, null, checkGPS);
        } else {
            checkGPS.run();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(MENU_OPTION_MAP).setChecked(true);
        setUpMapIfNeeded();
        addTrackerListeners();
        checkPlayServices();
        //startLocationUpdates();
        sensorsMain.resume();
        sensorsStepCounter.resume();
        lpTracker.resumeTracking();
        floorSelector.resumeTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (mRequestingLocationUpdates) {
        //    stopLocationUpdates();
        //}
        lpTracker.pauseTracking();
        floorSelector.pauseTracking();
        sensorsMain.pause();
        sensorsStepCounter.pause();
        removeTrackerListeners();
    }

    /*
    private void startLocationUpdates() throws SecurityException {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        mRequestingLocationUpdates = true;
    }
    */

    /*
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mRequestingLocationUpdates = false;
    }
    */

    @Override
    protected void onStop() {
        super.onStop();
        apiClient.disconnect();
        // Flurry Analytics
        if (AnyplaceAPI.FLURRY_ENABLE) {
            FlurryAgent.onEndSession(this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * SHARED PREFERENCES==>
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("TrackingAlgorithm")) {
            lpTracker.setAlgorithm(sharedPreferences.getString("TrackingAlgorithm", "WKNN"));
        }
    }

    /**
     * <==SHARED PREFERENCES
     */



    /**
     * PERMISSIONS FUNCTIONS==>
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted;
        switch (requestCode) {
            case 101:
                granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (granted) {
                    System.out.println("CALLBACK - granted");
                } else {
                    System.out.println("CALLBACK - not granted");
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestForSpecificPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, 101);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <==PERMISSIONS FUNCTIONS
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.isChecked()) {
            navigationLayout.closeDrawer(Gravity.START);
            return false;
        }

        final Intent intent;
        Class<? extends Activity> activityClass = null;
        final int requestCode;
        final int itemId = item.getItemId();

        switch (itemId) {
            case R.id.nav_map:
                activityClass = MapNavigationActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = 0;
                break;
            case R.id.nav_calendar:
                activityClass = CalendarActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = CALENDAR_EVENT_ACTIVITY_RESULT;
                break;
            case R.id.nav_places:
                activityClass = MapsActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = NEARBY_PLACE_ACTIVITY_RESULT;
                break;
            case R.id.nav_settings:
                activityClass = com.aghnavi.agh_navi.Settings.SettingsActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = SETTINGS_ACTIVITY_RESULT;
                break;
            case R.id.nav_about:
                activityClass = InformationActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = 0;
                break;
            default:
                //never get here, only for inner class below
                intent = null;
                requestCode = 0;
        }

        intent.putExtra("sourceActivity", getClass().getSimpleName());
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if(itemId == R.id.nav_calendar || itemId == R.id.nav_places) startActivityForResult(intent, requestCode);
                else startActivity(intent);
            }
        });
        item.setChecked(true);
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}


