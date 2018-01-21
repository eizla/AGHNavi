package com.aghnavi.agh_navi.Outdoor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aghnavi.agh_navi.Api.ApiClient;
import com.aghnavi.agh_navi.Api.ApiInterface;
import com.aghnavi.agh_navi.MapsActivity;
import com.aghnavi.agh_navi.Places.PlaceSerializable;
import com.aghnavi.agh_navi.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FindDirectionsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "FindDirActivity";

    @BindView(R.id.back_button)
    Button mBackButton;
    @BindView(R.id.start_edit_text)
    EditText mStartEditText;
    @BindView(R.id.end_edit_text)
    EditText mEndEditText;
    @BindView(R.id.change_directions_button)
    Button mChangeDirections;
    @BindView(R.id.show_directions_button)
    Button mShowDirections;
    @BindView(R.id.buildings_recycler_view)
    RecyclerView mBuildingsRecyclerView;

    private ArrayList<BuildingTemp> buildingTempArrayList = new ArrayList<>();
    private BuildingsAdapter mAdapter;
    private BuildingTemp startBuilding;
    private BuildingTemp destinationBuilding;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mCurrentLocation;
    private ArrayList<PlaceSerializable> placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            startBuilding = (BuildingTemp) savedInstanceState.getSerializable("start");
            destinationBuilding = (BuildingTemp) savedInstanceState.getSerializable("end");

        }

        placesList = (ArrayList<PlaceSerializable>) getIntent().getSerializableExtra("placeSerializableArrayList");
        if (placesList == null) {
            Log.d("place list", "empty");
        }
        setContentView(R.layout.activity_find_directions);
        ButterKnife.bind(this);
        getCurrentLocation();
        veryUglyBuildingsInit();
        initUI();
       // callAPI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(startBuilding != null)
            outState.putSerializable("start", startBuilding);
        if(destinationBuilding != null)
            outState.putSerializable("end", destinationBuilding);

        super.onSaveInstanceState(outState);
    }

    private void getCurrentLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                           mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            BuildingTemp current = new BuildingTemp("obecna lokalizacja", mCurrentLocation.latitude, mCurrentLocation.longitude);
                            buildingTempArrayList.add(current);

                        }
                        else {
                            Log.d(LOG_TAG, "NO LOCATION");
                        }
                    }
                });
    }

    private void veryUglyBuildingsInit(){

        BuildingTemp building0 = new BuildingTemp("A-0", 50.0653659,19.9204874);
        BuildingTemp building1 = new BuildingTemp("A-1", 50.064586,19.9215523);
        BuildingTemp building2 = new BuildingTemp("A-2", 50.0648856,19.9213028);
        BuildingTemp building3 = new BuildingTemp("C-1", 50.0653659,19.9204874);
        BuildingTemp building4 = new BuildingTemp("C-2", 50.0653659,19.9204874);
        BuildingTemp building5 = new BuildingTemp("C-3", 50.0649368,19.9204938 );
        BuildingTemp building6 = new BuildingTemp("D-17", 50.0678898,19.9117326);
        BuildingTemp building7 = new BuildingTemp("U-2", 50.06483,19.9203838);
        BuildingTemp building8 = new BuildingTemp("D-1", 50.06483,19.9203838);
        BuildingTemp building9 = new BuildingTemp("A-3", 50.0650489,19.9201988);
        BuildingTemp building10 = new BuildingTemp("A-4", 50.0651829,19.9193188);
        BuildingTemp building11 = new BuildingTemp("C-4", 50.0659719,19.9197158);
        BuildingTemp building12 = new BuildingTemp("B-1", 50.0658549,19.9187848);
        BuildingTemp building13 = new BuildingTemp("B-2", 50.0658549,19.9187848);
        BuildingTemp building14 = new BuildingTemp("B-3", 50.0661409,19.9175808);
        BuildingTemp building15 = new BuildingTemp("B-4", 50.0662779,19.9170368);
        BuildingTemp building16 = new BuildingTemp("D-2", 50.0657719,19.9171168);
        BuildingTemp building17 = new BuildingTemp("B-6", 50.0662679,19.9158128);
        BuildingTemp building18 = new BuildingTemp("B-5", 50.0669944,19.9167436);
        BuildingTemp building19 = new BuildingTemp("B-7", 50.0670994,19.9165451);
        BuildingTemp building20 = new BuildingTemp("D-5", 50.0670809,19.9148368);
        BuildingTemp building21 = new BuildingTemp("D-6", 50.0670809,19.9148368);
        BuildingTemp building22 = new BuildingTemp("D-11", 50.0674321,19.9124255);
        BuildingTemp building23 = new BuildingTemp("D-7", 50.066861, 19.912361);
        BuildingTemp building24 = new BuildingTemp("D-8", 50.066577, 19.911041);
        BuildingTemp building25 = new BuildingTemp("D-16", 50.067693, 19.910990);
        BuildingTemp building26 = new BuildingTemp("D-9", 50.067739, 19.909681);
        BuildingTemp building27 = new BuildingTemp("D-12", 50.067925, 19.909209);
        BuildingTemp building28 = new BuildingTemp("Biblioteka główna", 50.0649368,19.9204938);

        buildingTempArrayList.add(building0);
        buildingTempArrayList.add(building1);
        buildingTempArrayList.add(building2);
        buildingTempArrayList.add(building3);
        buildingTempArrayList.add(building4);
        buildingTempArrayList.add(building5);
        buildingTempArrayList.add(building6);
        buildingTempArrayList.add(building7);
        buildingTempArrayList.add(building8);
        buildingTempArrayList.add(building9);
        buildingTempArrayList.add(building10);
        buildingTempArrayList.add(building11);
        buildingTempArrayList.add(building12);
        buildingTempArrayList.add(building13);
        buildingTempArrayList.add(building14);
        buildingTempArrayList.add(building15);
        buildingTempArrayList.add(building16);
        buildingTempArrayList.add(building17);
        buildingTempArrayList.add(building18);
        buildingTempArrayList.add(building19);
        buildingTempArrayList.add(building20);
        buildingTempArrayList.add(building21);
        buildingTempArrayList.add(building22);
        buildingTempArrayList.add(building23);
        buildingTempArrayList.add(building24);
        buildingTempArrayList.add(building25);
        buildingTempArrayList.add(building26);
        buildingTempArrayList.add(building27);
        buildingTempArrayList.add(building28);

    }

    private void initUI(){

        mBuildingsRecyclerView.setHasFixedSize(true);
        mBuildingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new BuildingsAdapter(buildingTempArrayList, mBuildingsRecyclerView, this);
        mBuildingsRecyclerView.setAdapter(mAdapter);

        if(startBuilding != null){
            mStartEditText.setText(startBuilding.getName());
        }

        if(destinationBuilding != null){
            mEndEditText.setText(destinationBuilding.getName());
        }

        mStartEditText.setInputType(InputType.TYPE_NULL);
        mEndEditText.setInputType(InputType.TYPE_NULL);

    }

    @OnClick(R.id.back_button)
    public void onBackSpaceButtonClicked(){
        this.finish();
    }

    @OnClick(R.id.start_edit_text)
    public void onStartEditTextClicked(){
        mStartEditText.setHint(" ");
        if(mStartEditText.getText() != null && mStartEditText.getText().length() > 0){
            mStartEditText.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        mStartEditText.setCursorVisible(true);
    }

    @OnClick(R.id.end_edit_text)
    public void onEndEditTextClicked(){
        mEndEditText.setHint(" ");
        if(mEndEditText.getText() != null && mEndEditText.getText().length() > 0){
            mEndEditText.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        mEndEditText.setCursorVisible(true);
    }

    @OnClick(R.id.change_directions_button)
    public void onChangeDirectionsButtonClicked(){
        if(!isEmpty(mStartEditText) && !isEmpty(mEndEditText)){
            String start = mStartEditText.getText().toString();
            mStartEditText.setText(mEndEditText.getText());
            mEndEditText.setText(start);
        }
    }

    @OnClick(R.id.show_directions_button)
    public void onShowDirectionsButtonClicked(){
        if(!isEmpty(mStartEditText) && !isEmpty(mEndEditText)){

            if(startBuilding != null && destinationBuilding != null){

                Intent intent = new Intent(FindDirectionsActivity.this, OutdoorNavigationActivity.class);
                intent.putExtra("activity", "FindDirectionsActivity");
                intent.putExtra("start", new LatLng(startBuilding.getLatitude(), startBuilding.getLongitude()));
                intent.putExtra("end", new LatLng(destinationBuilding.getLatitude(), destinationBuilding.getLongitude()));
                intent.putExtra("places", placesList);
                startActivity(intent);

            }else {
                Toast.makeText(getApplicationContext(), "Wybierz dwie lokalizacje", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "one of the buildings is null");
            }

        }
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private void callAPI(){

        String campusId = "4a85e577-ef71-4b66-96d1-666275470b59_1513013313547";
        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<List<Building>> call = apiInterface.getBuildings(campusId );

        call.enqueue(new Callback<List<Building>>() {
            @Override
            public void onResponse(Call<List<Building>> call, Response<List<Building>> response) {

                Toast.makeText(getApplicationContext(), "" + response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<List<Building>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();

            }});



    }

    public void pickPlace(BuildingTemp buildingTemp){

        if(mStartEditText.hasFocus()){

            mStartEditText.setText(buildingTemp.getName());
            mStartEditText.setTextColor(ContextCompat.getColor(this, R.color.white));
            startBuilding = buildingTemp;
        }
        else if(mEndEditText.hasFocus()){

            mEndEditText.setText(buildingTemp.getName());
            destinationBuilding = buildingTemp;
            mEndEditText.setTextColor(ContextCompat.getColor(this, R.color.orange));


        }
    }

}
