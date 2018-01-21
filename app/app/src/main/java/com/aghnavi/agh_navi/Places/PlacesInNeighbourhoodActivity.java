package com.aghnavi.agh_navi.Places;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aghnavi.agh_navi.Outdoor.OutdoorNavigationActivity;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlacesInNeighbourhoodActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, FabricInitializer {

    @BindView(R.id.placesInNeighbourhood)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Set<PlaceSerializable> mPlaceSerializableSet;
    private PlacesInNeighbourhoodAdapter mAdapter;
    private LatLng mCurrentDeviceLatLang;

    private String[] mPlaceTypesFromResource;
    boolean[] mCheckedItems;
    ArrayList<Integer> mUserItems = new ArrayList<>();

    private static final int PLACES_IN_NEIGHBOURHOOD_REQUEST_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.activity_places_in_neighbourhood);

        Intent intent = getIntent();
        mCurrentDeviceLatLang =  intent.getExtras().getParcelable("latlang");
        ArrayList<PlaceSerializable> list = (ArrayList<PlaceSerializable>) intent.getSerializableExtra("placeSerializableArrayList");
        mPlaceSerializableSet = new HashSet<>(list);

        initUI();

    }

    private void initUI(){

        ButterKnife.bind(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        mToolbar.setTitle("Miejsca w pobliżu");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

        mAdapter = new PlacesInNeighbourhoodAdapter(mRecyclerView, mPlaceSerializableSet, mCurrentDeviceLatLang, this);
        mRecyclerView.setAdapter(mAdapter);

        mPlaceTypesFromResource = getResources().getStringArray(R.array.place_types);
        mCheckedItems = new boolean[mPlaceTypesFromResource.length];
        for(int b = 0; b <  mCheckedItems.length; b++){
            mCheckedItems[b] = true;
            mUserItems.add(b);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.places_in_neighbourhood_toolbar_menu, menu);

        return true;
    }

    @Override
    public void onClick(View v) {

        this.finish();
    }



    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_types:

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlacesInNeighbourhoodActivity.this);
                alertDialogBuilder.setTitle("Typy miejsc");
                alertDialogBuilder.setMultiChoiceItems(mPlaceTypesFromResource, mCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {

                        if(mCheckedItems[position]){
                            mCheckedItems[position] = true ;
                            Log.d(mPlaceTypesFromResource[position], "" + mCheckedItems[position]);
                        }
                        else {
                            mCheckedItems[position] = false;
                            Log.d(mPlaceTypesFromResource[position], "" + mCheckedItems[position]);
                        }


                    }
                });

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        List<String> types = new ArrayList<>();
                        for (int i = 0; i < mCheckedItems.length; i++) {
                            if(mCheckedItems[i] == true){
                                types.add(mPlaceTypesFromResource[i]);
                                Log.d(mPlaceTypesFromResource[i], "" + mCheckedItems[i]);
                            }

                        }

                        mAdapter.filterByType(types);
                    }

                });

                alertDialogBuilder.setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog mDialog = alertDialogBuilder.create();
                mDialog.show();

                break;
            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                search(searchView);
                break;
        }
        return true;
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    public void showDirectionsToPlace(PlaceSerializable placeSerializable){

        Intent intent = new Intent(PlacesInNeighbourhoodActivity.this, OutdoorNavigationActivity.class);
        if(placeSerializable != null){
            intent.putExtra("activity", "PlacesInNeighbourhoodActivity");
            intent.putExtra("start",  mCurrentDeviceLatLang);
            intent.putExtra("end", new LatLng(placeSerializable.getLatitude(), placeSerializable.getLongitude()));
            intent.putExtra("place", placeSerializable);
            intent.putExtra("places", new ArrayList<>(mPlaceSerializableSet));
            startActivity(intent);

        }
    }
}
