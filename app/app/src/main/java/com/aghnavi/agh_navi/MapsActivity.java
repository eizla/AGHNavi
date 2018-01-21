package com.aghnavi.agh_navi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aghnavi.agh_navi.Calendar.CalendarActivity;
import com.aghnavi.agh_navi.Outdoor.BuildingTemp;
import com.aghnavi.agh_navi.Outdoor.FindDirectionsActivity;
import com.aghnavi.agh_navi.Outdoor.GMapV2Direction;
import com.aghnavi.agh_navi.Outdoor.GetDirectionAsyncTask;
import com.aghnavi.agh_navi.Outdoor.OutdoorNavigationActivity;
import com.aghnavi.agh_navi.Places.PlaceSerializable;
import com.aghnavi.agh_navi.Places.PlacesInNeighbourhoodActivity;
import com.aghnavi.agh_navi.Settings.InformationActivity;
import com.aghnavi.agh_navi.dmsl.utils.AndroidUtils;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends BaseNavigatorActivity implements OnMapReadyCallback, View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private static final String LOG_TAG = "MapsActivity";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int CALENDAR_EVENT_ACTIVITY_RESULT = 1116;
    private final static int SETTINGS_ACTIVITY_RESULT = 1120;
    private static final int FINAL_REQUEST_TO_CALENDAR = 9987;
    private static final int FINAL_REQUEST_TO_SETTINGS = 9986;
    private static final int FINAL_REQUEST_TO_NAVIGATION = 9985;
    private static final int DIRECTIONS_ACTIVITY_CODE = 1111;
    private static final int PLACES_ACTIVITY_CODE = 1112;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private String sourceActivity;

    //default location : AGH
    private final LatLng mDefaultLocation = new LatLng(50.0668858,19.9114252 );
    private static final int DEFAULT_ZOOM  = 16;

    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;

    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private PlaceDetectionClient mPlaceDetectionClient;

    private Set<Place> placesSet = new LinkedHashSet<>();
    private Set<PlaceSerializable> placeSerializableSet = new LinkedHashSet<>();

    private ArrayList<Marker> mPlacesMarkers = new ArrayList<>();
    private BuildingTemp start;
    private BuildingTemp end;
    private LatLng latLng1;
    private LatLng latLng2;
    private int width = 0;
    private int height = 0;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    @BindView(R.id.placesInNeighbourhoodPanel)
    RecyclerView mRecyclerView;
    @BindView(R.id.deviceLocationFloatingButton)
    FloatingActionButton mDeviceLocationFloatingButton;
    @BindView(R.id.naviFloatingButton)
    FloatingActionButton mNaviFloatingButton;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private PlacesPanelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        initApiClients();
        initUI();
        getSreenDimensions();
    }

    @Override
    public void onStart(){
        super.onStart();
        check();

    }

    private void check(){

        Runnable checkGPS = new Runnable() {
            @Override
            public void run() {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfGPS == false) {
                    AndroidUtils.showGPSSettings(MapsActivity.this);
                }
            }
        };

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled() || !NetworkUtils.isOnline(MapsActivity.this)) {
            //AndroidUtils.showWifiSettings(this, null, checkGPS);
            wifi.setWifiEnabled(true);
        } else {
            checkGPS.run();
        }

    }

    private void initUI(){

        getLayoutInflater().inflate(R.layout.activity_maps_2, contentFrameLayout);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        else{
            Log.d(LOG_TAG, "mapfragment null");
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mToolbar.setTitle("Czego szukasz?");
        mToolbar.setTitleTextColor(Color.GRAY);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSearchMenuItemCliecked();
            }
        });

        //FOR NAVIGATION DRAWER
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, navigationLayout,mToolbar,R.string.app_name, R.string.app_name);
        mToolbar.setContentInsetStartWithNavigation(0);
        navigationLayout.addDrawerListener(actionBarDrawerToggle);

        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(mAdapter != null){
                    if(mAdapter.getPlaceSerializableSet() == null || mAdapter.getPlaceSerializableSet().size() < 1){
                        findPlacesInNeighbourhood();
                    }
                }
            }
        });


    }

    @OnClick(R.id.deviceLocationFloatingButton)
    public void onDeviceLocationFloatinButtonClicked(){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        getDeviceLocation();
        LatLng current = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        builder.include(current).include(mDefaultLocation);
        CameraPosition newCamPos = new CameraPosition(current,
                17.0f,
                mMap.getCameraPosition().tilt, //use old tilt
                mMap.getCameraPosition().bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 4000, null);
    }

    @OnClick(R.id.naviFloatingButton)
    public void onNaviFloatingButtonClicked(){
        Intent i = new Intent(MapsActivity.this, FindDirectionsActivity.class);
        i.putExtra("placeSerializableArrayList", new ArrayList<>(placeSerializableSet));
        startActivity(i);
    }

    private void initApiClients(){

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(mGeoDataClient == null || mPlaceDetectionClient == null || mFusedLocationClient == null){
            Log.d(LOG_TAG, "api client initialization went wrong");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    Log.i("T", "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i("T", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
//            case DIRECTIONS_ACTIVITY_CODE:
//                if(resultCode ==  RESULT_OK){
//                    start = (BuildingTemp) data.getSerializableExtra("start");
//                    end = (BuildingTemp)data.getSerializableExtra("end");
//                    if(start != null && end != null){
//
//                        latLng1 = new LatLng(start.getLatitude(), start.getLongitude());
//                        latLng2 = new LatLng(end.getLatitude(), end.getLongitude());
//                        //findDirections(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLatitude(), GMapV2Direction.MODE_WALKING);
//                    }
//
//                }
//                break;
//            case PLACES_ACTIVITY_CODE:
//                if(resultCode == RESULT_OK){
//                    PlaceSerializable p = (PlaceSerializable) data.getSerializableExtra("place");
//                    latLng1 = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLatitude());
//                    latLng2 = new LatLng(p.getLatitude(), p.getLongitude());
//                   // findDirections(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), p.getLatitude(), p.getLongitude(),GMapV2Direction.MODE_WALKING);
//                }
        }
            //szymon
//            case CALENDAR_EVENT_ACTIVITY_RESULT:
//                if (sourceActivity.equals("Settings")) {
//                    if (resultCode == REQUEST_PLACE_PICK_TO_PLACE) {
//                        setResult(FINAL_REQUEST_TO_SETTINGS, data);
//                        finish();
//                        //add for EVENT/SOON
//                    } else if (resultCode == RESULT_CANCELED) {
//                        setResult(RESULT_CANCELED);
//                        finish();
//                    }
//                } else if (sourceActivity.equals("MapNavigationActivity")) {
//                    if (resultCode == REQUEST_PLACE_PICK_TO_PLACE) {
//                        setResult(RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == REQUEST_EVENT_PICK_TO_PLACE) {
//                        setResult(EVENT_RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == REQUEST_SOON_PICK_TO_PLACE) {
//                        setResult(SOON_RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == RESULT_CANCELED) {
//                        setResult(RESULT_CANCELED);
//                        finish();
//                    }
//                }
//                break;
//            case SETTINGS_ACTIVITY_RESULT:
//                if (sourceActivity.equals("CalendarActivity")) {
//                    if (resultCode == REQUEST_PLACE_PICK_TO_PLACE) {
//                        setResult(FINAL_REQUEST_TO_CALENDAR, data);
//                        finish();
//                        //add for EVENT/SOON
//                    } else if (resultCode == RESULT_CANCELED) {
//                        setResult(RESULT_CANCELED);
//                        finish();
//                    }
//                } else if (sourceActivity.equals("MapNavigationActivity")) {
//                    if (resultCode == REQUEST_PLACE_PICK_TO_PLACE) {
//                        setResult(RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == REQUEST_EVENT_PICK_TO_PLACE) {
//                        setResult(EVENT_RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == REQUEST_SOON_PICK_TO_PLACE) {
//                        setResult(SOON_RESULT_OK, data);
//                        finish();
//                    } else if (resultCode == RESULT_CANCELED) {
//                        setResult(RESULT_CANCELED);
//                        finish();
//                    }
//                }
//                break;
//            case FINAL_REQUEST_TO_CALENDAR:
//                if (resultCode == RESULT_OK) {
//                    setResult(REQUEST_PLACE_PICK_TO_CALENDAR, data);
//                    finish();
//                } else if (resultCode == RESULT_CANCELED) {
//                    setResult(RESULT_CANCELED);
//                    finish();
//                }
//                break;
//            case FINAL_REQUEST_TO_NAVIGATION:
//                if (resultCode == RESULT_OK) {
//                    setResult(RESULT_OK, data);
//                    finish();
//                } else if (resultCode == RESULT_CANCELED) {
//                    setResult(RESULT_CANCELED);
//                    finish();
//                }
//                break;
//            case FINAL_REQUEST_TO_SETTINGS:
//                if (resultCode == RESULT_OK) {
//                    setResult(REQUEST_PLACE_PICK_TO_SETTINGS, data);
//                    finish();
//                } else if (resultCode == RESULT_CANCELED) {
//                    setResult(RESULT_CANCELED);
//                    finish();
//                }
//                break;
//
//
//        }
        //szymon
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mMap != null){
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);

            super.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        updateLocationUI();
        getDeviceLocation();
        findPlacesInNeighbourhood();
    }

    private void findPlacesInNeighbourhood(){
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        mProgressBar.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    PERMISSION_REQUEST_CODE);
        }
        else{


            callPlaceDetectionApi();
        }
    }

    private void callPlaceDetectionApi() throws SecurityException {

        Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);

        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                try{
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                    if (likelyPlaces == null) {
                        Log.d("likelyPlaces", "null");
                        return;
                    }
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.d("Place: " + placeLikelihood.getPlace().getId(), ", Name: " + placeLikelihood.getPlace().getName());
                        List<Integer> types = placeLikelihood.getPlace().getPlaceTypes();
                        for (Integer type : types) {
                            Log.d("PLACE TYPE", type.toString());
                            if (
                                            type.equals(Place.TYPE_BAR) ||
                                            type.equals(Place.TYPE_CAFE) ||
                                            type.equals(Place.TYPE_STORE) ||
                                            type.equals(Place.TYPE_BUS_STATION) ||
                                            type.equals(Place.TYPE_RESTAURANT)
                                    ) {

                                placesSet.add(placeLikelihood.getPlace());
                            }
                        }
                    }
                    placesToSerializable();

                    ArrayList<PlaceSerializable> temp = new ArrayList<>();
                    temp.addAll(placeSerializableSet);
                    if(locationDetected == true){
                        mAdapter = new PlacesPanelAdapter(mRecyclerView, placeSerializableSet, new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), MapsActivity.this);

                    }else{
                        mAdapter = new PlacesPanelAdapter(mRecyclerView, placeSerializableSet, null, MapsActivity.this);
                    }

                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    likelyPlaces.release();
                    mProgressBar.setVisibility(View.GONE);
                    placeItem.setVisible(false);
                }
                catch(RuntimeException re){

                    Toast.makeText(getApplicationContext(), "Nie udało się pobrać listy miejsc w pobliżu", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);

                }
            }

        });

    }

    MenuItem placeItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.maps_menu_2, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        placeItem = menu.findItem(R.id.refresh_place);
        Drawable icon = searchItem.getIcon();
        icon.mutate().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);
        searchItem.setIcon(icon);
        icon = placeItem.getIcon();
        icon.mutate().setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_IN);
        placeItem.setIcon(icon);
        return true;
    }

    @Override
    public void onClick(View v) {
        //todo: handle navigation button
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_search:
                actionSearchMenuItemCliecked();
                break;
            case R.id.refresh_place:
                findPlacesInNeighbourhood();
        }
        return true;
    }

    private void actionSearchMenuItemCliecked(){

        Intent intent = new Intent(MapsActivity.this, FindDirectionsActivity.class);
        intent.putExtra("placeSerializableArrayList", new ArrayList<>(placeSerializableSet));
        startActivity(intent);
    }
    private void placesToSerializable(){

        for(Place p : placesSet){

            PlaceSerializable placeSerializable = new PlaceSerializable(
                    p.getId(),
                    p.getName().toString(),
                    p.getAddress().toString(),
                    p.getLatLng(),
                    p.getPlaceTypes(),
                    p.getRating(),
                   ""
            );

            placeSerializableSet.add(placeSerializable);

        }
    }


    //DRAWER INTEGRATION//
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(MENU_OPTION_MAP).setChecked(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

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
                activityClass = MapsActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = 0;
                break;
            case R.id.nav_calendar:
                activityClass = CalendarActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = CALENDAR_EVENT_ACTIVITY_RESULT;
                break;
            case R.id.nav_places:
                activityClass = PlacesInNeighbourhoodActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                intent.putExtra("latlang", new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                intent.putExtra("placeSerializableArrayList", new ArrayList<>(placeSerializableSet));
                requestCode = 0;
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
                if(itemId == R.id.nav_calendar || itemId == R.id.nav_settings) {
                    startActivityForResult(intent, requestCode);
                }
                else if(itemId == R.id.nav_places){
                    startActivityForResult(intent, PLACES_ACTIVITY_CODE);
                }
                else {
                    startActivity(intent);
                   // finish();
                }
            }
        });
        item.setChecked(true);
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.DirectionsApiKey))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private boolean locationDetected;

    private void getDeviceLocation() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mLastKnownLocation = location ;
                                locationDetected = true;

                            }else{
                                locationDetected = false;
                            }
                        }
                    });
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {

            Log.d(LOG_TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    private void getSreenDimensions()
    {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    public void showDirectionsToPlace(PlaceSerializable p){

        latLng1 = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        latLng2 = new LatLng(p.getLatitude(), p.getLongitude());
        Intent i = new Intent(MapsActivity.this, OutdoorNavigationActivity.class);
        i.putExtra("activity", "MapsActivity");
        i.putExtra("start", latLng1);
        i.putExtra("end", latLng2);
        i.putExtra("place", p);
        if(placeSerializableSet.size() > 0){
            i.putExtra("places", new ArrayList<>(placeSerializableSet));
        }
        startActivity(i);

    }
}
