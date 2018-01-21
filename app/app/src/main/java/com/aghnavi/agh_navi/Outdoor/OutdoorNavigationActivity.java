package com.aghnavi.agh_navi.Outdoor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aghnavi.agh_navi.Places.PlaceSerializable;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.TestAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
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
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OutdoorNavigationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, View.OnClickListener, Toolbar.OnMenuItemClickListener, LocationListener {

    private static final String LOG_TAG = "OutdoorNavActivity";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final LatLng mDefaultLocation = new LatLng(50.0668858,19.9114252 );
    private static final int DEFAULT_ZOOM  = 16;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private PlaceDetectionClient mPlaceDetectionClient;

    private ArrayList<Marker> mPlacesMarkers = new ArrayList<>();
    private LatLng latLng1;
    private LatLng latLng2;
    private Marker markerDest, markerFrom;
    private String mPlacesFloatingButtonAction = "SHOW";
    private String mShowHideNavigation = "HIDE";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private ArrayList<PlaceSerializable> placesList = new ArrayList<>();
    private PlaceSerializable mPlaceSerializable;

    private Polyline newPolyline;
    private LatLngBounds latlngBounds;
    private int width, height;
    private  String activity;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.currentLocationFloatingButton)
    FloatingActionButton mCurrentLocationFloatingButton;

    @BindView(R.id.placesFloatingButton)
    FloatingActionButton mPlacesFloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outoor_navigation);
        Intent i = getIntent();
        handlePassedData(i);

        initUI();
        initApiClients();
        getSreenDimensions();

        if(latLng1 != null && latLng2 != null){
            findDirections(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, GMapV2Direction.MODE_WALKING);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.test_menu, menu);

        return true;
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){


            @Override
            public void onMapClick(LatLng point) {

//                if(markers.size() < 3) {
//                    markerNum++;
//                    MarkerOptions marker = new MarkerOptions().position(
//                            new LatLng(point.latitude, point.longitude)).title("marker " + markerNum)
//                            .draggable(true);
//
//                    markers.add(new LatLng(point.latitude, point.longitude));
//                    mMap.addMarker(marker);
//                }
//
//                if(markers.size() == 2){
//                    LatLng latLng1 = markers.get(0);
//                    LatLng latLng2 = markers.get(1);
//
//                    findDirections(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, GMapV2Direction.MODE_DRIVING );
//                }
            }
        });

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        updateLocationUI();
        getDeviceLocation();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
       switch (item.getItemId()){
           case R.id.action_hide_navigation:
               if(mShowHideNavigation.equals("HIDE")){
                   newPolyline.remove();
                   markerDest.setVisible(false);
                   markerFrom.setVisible(false);
                   item.setTitle("Pokaż trasę");
                   mShowHideNavigation = "SHOW";
               }
               else {
                   mShowHideNavigation = "HIDE";
                   item.setTitle("Ukryj trasę");
                   if(latLng1 != null && latLng2 != null){
                       findDirections(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, GMapV2Direction.MODE_WALKING);
                   }
               }

               break;
       }

       return true;
    }

    @OnClick(R.id.currentLocationFloatingButton)
    public void onDeviceLocationFloatinButtonClicked(){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        getDeviceLocation();
        LatLng current = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        builder.include(current).include(mDefaultLocation);
        CameraPosition newCamPos = new CameraPosition(current,
                15.5f,
                mMap.getCameraPosition().tilt, //use old tilt
                mMap.getCameraPosition().bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 4000, null);
    }

    @OnClick(R.id.placesFloatingButton)
    public void onPlacesFloatingButtonClicke(View view){

        if(placesList != null) {

            if (placesList.size() > 0) {
                if(activity.equals("PlacesInNeighbourhoodActivity") || activity.equals("MapsActivity")){
                    if (mPlacesFloatingButtonAction.equals("SHOW")) {

                        ArrayList<PlaceSerializable> temp = new ArrayList<>();
                        temp.add(mPlaceSerializable);
                        TestAdapter mAdapter = new TestAdapter(temp, this);
                        mRecyclerView.setAdapter(mAdapter);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mPlacesFloatingButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_room_white_24dp));
                        mPlacesFloatingButtonAction = "HIDE";
                        for (Marker marker : mPlacesMarkers) {
                            marker.setVisible(false);
                        }

                    } else {

                        TestAdapter mAdapter = new TestAdapter(placesList, this);
                        mRecyclerView.setAdapter(mAdapter);

                        mRecyclerView.setVisibility(View.VISIBLE);
                        mPlacesFloatingButtonAction = "SHOW";
                        mPlacesFloatingButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_24dp));

                        for (PlaceSerializable p : placesList) {
                            mPlacesMarkers.add(
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(p.getLatitude(), p.getLongitude()))
                                            .title(p.getName()))
                            );
                        }

                    }
                }else{
                    if (mPlacesFloatingButtonAction.equals("SHOW")) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        TestAdapter mAdapter = new TestAdapter(placesList, this);
                        mRecyclerView.setAdapter(mAdapter);

                        mRecyclerView.setVisibility(View.VISIBLE);
                        mPlacesFloatingButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
                        mPlacesFloatingButtonAction = "HIDE";

                        for (PlaceSerializable p : placesList) {
                            mPlacesMarkers.add(
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(p.getLatitude(), p.getLongitude()))
                                            .title(p.getName()))
                            );
                        }
                    }else{
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        mPlacesFloatingButtonAction = "SHOW";
                        mPlacesFloatingButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_room_white_24dp));

                        for (Marker marker : mPlacesMarkers) {
                            marker.setVisible(false);
                        }
                    }
                }

            }
        }
    }

    private void handlePassedData(Intent intent){

        activity = (String) intent.getExtras().get("activity");

        switch (activity){
            case "PlacesInNeighbourhoodActivity":
                latLng1 = (LatLng) intent.getExtras().get("start");
                latLng2 = (LatLng) intent.getExtras().get("end");
                mPlaceSerializable = (PlaceSerializable) intent.getSerializableExtra("place");
                placesList = (ArrayList<PlaceSerializable>) intent.getSerializableExtra("places");
                break;
            case "FindDirectionsActivity":
                latLng1 = (LatLng) intent.getExtras().get("start");
                latLng2 = (LatLng) intent.getExtras().get("end");
                placesList = (ArrayList<PlaceSerializable>) intent.getSerializableExtra("places");
                if (placesList == null) {
                    Log.d("place list", "empty");
                }

                break;
            case "MapsActivity":
                latLng1 = (LatLng) intent.getExtras().get("start");
                latLng2 = (LatLng) intent.getExtras().get("end");
                mPlaceSerializable = (PlaceSerializable) intent.getSerializableExtra("place");
                placesList = (ArrayList<PlaceSerializable>) intent.getSerializableExtra("places");
                break;
        }
    }

    private void initUI(){

        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        else{
            Log.d("error", "mapfragment null");
        }

        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if(mPlaceSerializable != null){

            ArrayList<PlaceSerializable> temp = new ArrayList<>();
            temp.add(mPlaceSerializable);
            TestAdapter mAdapter = new TestAdapter(temp, this);
            mRecyclerView.setAdapter(mAdapter);
        }
        else if(placesList != null){

            TestAdapter mAdapter = new TestAdapter(placesList, this);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVisibility(View.INVISIBLE);

        }else{

            mRecyclerView.setVisibility(View.INVISIBLE);
        }

    }

    private void initApiClients(){

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        mGoogleApiClient.connect();
    }

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
                            }
                            else {
                                Log.d(LOG_TAG, "NO LOCATION");
                            }
                        }
                    });
        }

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

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<>();
        map.put(GetDirectionAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionAsyncTask asyncTask = new GetDirectionAsyncTask(this);
        asyncTask.execute(map);
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {

        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }
        newPolyline = mMap.addPolyline(rectLine);

        latlngBounds = createLatLngBoundsObject(latLng1, latLng2);
        if(markerFrom == null)
            markerFrom = ( mMap.addMarker(new MarkerOptions().position(latLng1)));
        else
            markerFrom.setVisible(true);

        if(markerDest == null)
            markerDest = (  mMap.addMarker(new MarkerOptions().position(latLng2)));
        else
            markerDest.setVisible(true);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 170));

        //findDirections(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), p.getLatitude(), p.getLongitude(), GMapV2Direction.MODE_WALKING);
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastKnownLocation = location;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 21));
    }
}
