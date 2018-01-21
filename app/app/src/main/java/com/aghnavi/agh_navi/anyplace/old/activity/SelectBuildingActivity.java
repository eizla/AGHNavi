package com.aghnavi.agh_navi.anyplace.old.activity;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.dmsl.cache.AnyplaceCache;
import com.aghnavi.agh_navi.dmsl.floors.Algo1Server;
import com.aghnavi.agh_navi.dmsl.floors.FloorSelector;
import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.aghnavi.agh_navi.dmsl.nav.FloorModel;
import com.aghnavi.agh_navi.dmsl.tasks.FetchBuildingsByBuidTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchBuildingsTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchFloorPlanTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchFloorsByBuidTask;
import com.aghnavi.agh_navi.dmsl.tasks.FetchNearBuildingsTask;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class SelectBuildingActivity extends FragmentActivity implements FloorSelector.FloorAnyplaceFloorListener, FloorSelector.ErrorAnyplaceFloorListener, FabricInitializer {

    public enum Mode {
        NONE, NEAREST, // Automatically show nearby Building
        INVISIBLE // Automatically submit
    }

    private Spinner spinnerBuildings;
    private Spinner spinnerFloors;

    private Button btnRefreshWorldBuildings;
    private Button btnRefreshNearmeBuildings;
    private Button btnRefreshFloors;

    private String lat;
    private String lon;
    private Mode mode;

    private Integer selectedFloorIndex = 0;

    // <Floor Selector>
    private boolean isBuildingsLoadingFinished;
    private boolean isfloorSelectorJobFinished;
    private FloorSelector floorSelector;
    private String floorResult;
    private ProgressDialog floorSelectorDialog;
    // </Floor Selector>

    private boolean isBuildingsJobRunning = false;
    private boolean isFloorsJobRunning = false;

    private AnyplaceCache mAnyplaceCache = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        requestWindowFeature((int) Window.FEATURE_NO_TITLE);
        setContentView(R.layout.anyplace_activity_select_building);

        // get the AnyplaceCache instance
        mAnyplaceCache = AnyplaceCache.getInstance(this);

        btnRefreshWorldBuildings = (Button) findViewById(R.id.anyplace_btnWorldBuildingsRefresh);
        btnRefreshWorldBuildings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBuildingsJobRunning) {
                    Toast.makeText(getBaseContext(), "Inna akcja dotycząca budynku w trakcie...", Toast.LENGTH_SHORT).show();
                    return;
                }
                startBuildingsFetch(false, true);
            }
        });

        btnRefreshNearmeBuildings = (Button) findViewById(R.id.anyplace_btnNearmeBuildingsRefresh);
        btnRefreshNearmeBuildings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBuildingsJobRunning) {
                    Toast.makeText(getBaseContext(), "Inna akcja dotycząca budynku w trakcie...", Toast.LENGTH_SHORT).show();
                    return;
                }
                startBuildingsFetch(true, false);
            }
        });

        spinnerFloors = (Spinner) findViewById(R.id.floors);
        spinnerFloors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                selectedFloorIndex = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerBuildings = (Spinner) findViewById(R.id.buildings);
        spinnerBuildings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (isFloorsJobRunning) {
                    Toast.makeText(getBaseContext(), "Inna akcja w trakcie", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAnyplaceCache.setSelectedBuildingIndex(pos);

                BuildingModel build = mAnyplaceCache.getSelectedBuilding();

                if (build != null && build.isFloorsLoaded()) {
                    setFloorSpinner(build.getFloors());
                    try {
                        spinnerFloors.setSelection(build.getSelectedFloorIndex());
                    } catch (IndexOutOfBoundsException ex) {
                    }
                    onFloorsLoaded(build.getFloors());
                } else {
                    startFloorFetch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnRefreshFloors = (Button) findViewById(R.id.btnFloorsRefresh);
        btnRefreshFloors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFloorsJobRunning) {
                    Toast.makeText(getBaseContext(), "Inna akcja dotycząca piętra w trakcie...", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    startFloorFetch();
                } catch (IndexOutOfBoundsException ex) {
                    Toast.makeText(getBaseContext(), "Wybierz budynek jeszcze raz.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button done = (Button) findViewById(R.id.btnDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFloorPlanTask();
            }
        });

        Bundle b = this.getIntent().getExtras();
        if (b == null) {
            mode = Mode.NONE;
            lat = "0.0";
            lon = "0.0";
        } else {
            mode = (Mode) b.getSerializable("mode");
            if (mode == null)
                mode = Mode.NONE;

            lat = b.getString("coordinates_lat");
            if (lat == null) {
                lat = "0.0";
                mode = Mode.NONE;
                btnRefreshNearmeBuildings.setEnabled(false);
            }

            lon = b.getString("coordinates_lon");
            if (lon == null) {
                lon = "0.0";
                mode = Mode.NONE;
                btnRefreshNearmeBuildings.setEnabled(false);
            }

        }
        // start automatically the buildings task if invisible mode
        if (mode != Mode.NONE) {
            floorSelectorDialog = new ProgressDialog(SelectBuildingActivity.this);
            floorSelectorDialog.setIndeterminate(true);
            floorSelectorDialog.setTitle("Wykrywanie piętra");
            floorSelectorDialog.setMessage("Proszę poczekać...");
            floorSelectorDialog.setCancelable(true);
            floorSelectorDialog.setCanceledOnTouchOutside(false);
            floorSelector = new Algo1Server(getApplicationContext());
            floorSelector.addListener((FloorSelector.FloorAnyplaceFloorListener) this);
            floorSelector.addListener((FloorSelector.ErrorAnyplaceFloorListener) this);
            isBuildingsLoadingFinished = false;
            isfloorSelectorJobFinished = false;
            floorSelector.Start(lat, lon);
            startBuildingsFetch(true, false);
        } else {
            List<BuildingModel> buildings = mAnyplaceCache.getSpinnerBuildings();

            if (buildings.size() == 0) {
                startBuildingsFetch(false, false);
            } else {
                setBuildingSpinner(buildings);
                spinnerBuildings.setSelection(mAnyplaceCache.getSelectedBuildingIndex());
            }
        }

        if (!checkIfAlreadyhavePermission()) {
            System.out.println("NOT GRANTED in SelectBuildingActi");
            requestForSpecificPermission();
        } else {
            System.out.println("ALREADY GRANTED in SelectBuildingActi");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floorSelector != null) {
            floorSelector.removeListener((FloorSelector.FloorAnyplaceFloorListener) this);
            floorSelector.removeListener((FloorSelector.ErrorAnyplaceFloorListener) this);
            floorSelector.Destroy();
        }
        System.gc();
    }

    /**
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.anyplace_main_menu_select_building, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_add_building: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Dodaj budynek");
                alert.setMessage("ID prywatnego budynku:");

                // Set an EditText view to get user input
                final EditText editText = new EditText(this);
                alert.setView(editText);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            // http://anyplace.cs.ucy.ac.cy/viewer/?buid=building_2f25420e-3cb1-4bc1-9996-3939e5530d30_1414014035379
                            // OR
                            // building_2f25420e-3cb1-4bc1-9996-3939e5530d30_1414014035379
                            String input = editText.getText().toString().trim();
                            String buid = input;
                            if (input.startsWith("http")) {

                                Uri uri = Uri.parse(URLDecoder.decode(input, "UTF-8"));
                                buid = uri.getQueryParameter("buid");
                                if (buid == null) {
                                    throw new Exception("Missing buid parameter");
                                }

                            }
                            new FetchBuildingsByBuidTask(new FetchBuildingsByBuidTask.FetchBuildingsByBuidTaskListener() {

                                @Override
                                public void onSuccess(String result, BuildingModel b) {
                                    ArrayList<BuildingModel> list = new ArrayList<BuildingModel>(1);
                                    list.add(b);
                                    setBuildingSpinner(list);
                                }

                                @Override
                                public void onErrorOrCancel(String result) {
                                    Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                                }
                            }, SelectBuildingActivity.this, buid).execute();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
                return true;
            }

        }
        return false;
    }

    private void startBuildingsFetch(final Boolean nearestOnly, Boolean forceReload) {
        final boolean btnRefreshWorldBuildingsState = btnRefreshWorldBuildings.isEnabled();
        final boolean btnRefreshNearmeBuildingsState = btnRefreshNearmeBuildings.isEnabled();
        btnRefreshWorldBuildings.setEnabled(false);
        btnRefreshNearmeBuildings.setEnabled(false);
        isBuildingsJobRunning = true;

        mAnyplaceCache.loadWorldBuildings(new FetchBuildingsTask.FetchBuildingsTaskListener() {

            @Override
            public void onSuccess(String result, List<BuildingModel> buildings) {
                finishJob();

                if (nearestOnly) {

                    FetchNearBuildingsTask nearBuildings = new FetchNearBuildingsTask();
                    nearBuildings.run(buildings.iterator(), lat, lon, 10000);

                    // if the user should not interact with the gui and
                    // automatically load the building
                    if (mode == Mode.INVISIBLE && (nearBuildings.buildings.isEmpty() || nearBuildings.distances.get(0) > 200)) {
                        // exit the activity since no building exists in your area
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("message", "Brak budynków w pobliżu!");
                        setResult(RESULT_CANCELED, returnIntent);
                        finish();
                    } else if (nearBuildings.buildings.isEmpty()) {
                        Toast.makeText(getBaseContext(), "Brak budynków w pobliżu!", Toast.LENGTH_SHORT).show();
                    } else {
                        setBuildingSpinner(nearBuildings.buildings, nearBuildings.distances);
                    }

                } else {
                    setBuildingSpinner(buildings);
                }
            }

            @Override
            public void onErrorOrCancel(String result) {
                finishJob();
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }

            private void finishJob() {
                // Enable only if invisibleSelection==false
                btnRefreshWorldBuildings.setEnabled(btnRefreshWorldBuildingsState);
                btnRefreshNearmeBuildings.setEnabled(btnRefreshNearmeBuildingsState);
                isBuildingsJobRunning = false;
            }
        }, this, forceReload);
    }

    private void startFloorFetch() throws IndexOutOfBoundsException {
        BuildingModel building = mAnyplaceCache.getSelectedBuilding();

        if (building != null) {

            spinnerBuildings.setEnabled(false);

            building.loadFloors(new FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener() {

                @Override
                public void onSuccess(String result, List<FloorModel> floors) {
                    finishJob(floors);
                    onFloorsLoaded(floors);
                }

                @Override
                public void onErrorOrCancel(String result) {
                    finishJob(new ArrayList<FloorModel>());
                    Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                }

                private void finishJob(List<FloorModel> floors) {
                    // UPDATE SPINNER FOR FLOORS
                    setFloorSpinner(floors);
                    spinnerBuildings.setEnabled(true);
                    isFloorsJobRunning = false;
                }

            }, this, true, true);

        }
    }

    // Create a list for the name of each building
    private void setBuildingSpinner(List<BuildingModel> buildings) {
        setBuildingSpinner(buildings, null);
    }

    private void setBuildingSpinner(List<BuildingModel> buildings, List<Double> distance) {
        if (!buildings.isEmpty()) {
            mAnyplaceCache.setSpinnerBuildings(buildings);
            List<String> list = new ArrayList<String>();
            if (distance == null) {
                for (BuildingModel building : buildings) {
                    list.add(building.name);
                }
            } else {
                for (int i = 0; i < buildings.size(); i++) {
                    double value = distance.get(i);
                    if (value < 1000) {
                        list.add(String.format("[%.0f m] %s", value, buildings.get(i).name));
                    } else {
                        list.add(String.format("[%.1f km] %s", value / 1000, buildings.get(i).name));
                    }
                }
            }

            ArrayAdapter<String> spinnerBuildingsAdapter;
            spinnerBuildingsAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, list);
            spinnerBuildingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBuildings.setAdapter(spinnerBuildingsAdapter);

        }
    }

    // Create a list for the number of each floor
    private void setFloorSpinner(List<FloorModel> floors) {

        List<String> list = new ArrayList<String>();
        for (FloorModel floor : floors) {
            list.add(floor.toString());
        }

        ArrayAdapter<String> spinnerFloorsAdapter;
        spinnerFloorsAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, list);
        spinnerFloorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFloors.setAdapter(spinnerFloorsAdapter);
    }

    @Override
    public void onFloorError(Exception ex) {
        onNewFloor("0");
    }

    private void loadFloor(String floorNumber) {

        BuildingModel build = mAnyplaceCache.getSelectedBuilding();
        if (build != null) {

            selectedFloorIndex = build.checkFloorIndex(floorNumber);
            if (selectedFloorIndex == null) {
                selectedFloorIndex = 0;
            }

            try {
                spinnerFloors.setSelection(selectedFloorIndex);
                if (mode == Mode.INVISIBLE)
                    startFloorPlanTask();
            } catch (IndexOutOfBoundsException ex) {

            }

        }

    }

    @Override
    public void onNewFloor(String floor) {

        floorSelector.Stop();

        isfloorSelectorJobFinished = true;

        if (isBuildingsLoadingFinished && floorSelectorDialog.isShowing()) {
            floorSelectorDialog.dismiss();
            loadFloor(floor);
        } else {
            floorResult = floor;
        }

    }

    private void onFloorsLoaded(List<FloorModel> floors) {
        // if the user should not interact with the gui and
        // automatically
        // load the building
        if (mode != Mode.NONE) {
            // TODO - automatically choose the floor to be shown if it's only one
            if (floors.isEmpty()) {
                Toast.makeText(getBaseContext(), "Brak istniejących pięter dla wybranego budynku!", Toast.LENGTH_SHORT).show();
                SelectBuildingActivity.this.finish();
            } else if (floors.size() == 1) {
                if (mode == Mode.INVISIBLE)
                    startFloorPlanTask();
            } else {

                isBuildingsLoadingFinished = true;

                if (isfloorSelectorJobFinished) {
                    loadFloor(floorResult);
                } else {
                    if (!AnyplaceAPI.FLOOR_SELECTOR) {
                        loadFloor("0");
                    } else {
                        // Wait Floor Selector Answer
                        floorSelectorDialog.show();
                    }
                }

            }
        } else {
            if (floors.isEmpty()) {
                Toast.makeText(getBaseContext(), "Brak istniejących pięter dla wybranego budynku!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* * TASKS */
    private void startFloorPlanTask() {

        try {
            final BuildingModel b = mAnyplaceCache.getSelectedBuilding();

            if (b == null) {
                Toast.makeText(getBaseContext(), "Nie wybrano budynku oraz piętra!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            final FloorModel f = b.getFloors().get(selectedFloorIndex);

            final FetchFloorPlanTask fetchFloorPlanTask = new FetchFloorPlanTask(this, b.buid, f.floor_number);
            fetchFloorPlanTask.setCallbackInterface(new FetchFloorPlanTask.FetchFloorPlanTaskListener() {

                private ProgressDialog dialog;

                @Override
                public void onSuccess(String result, File floor_plan_file) {
                    if (dialog != null)
                        dialog.dismiss();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("bmodel", mAnyplaceCache.getSelectedBuildingIndex());
                    returnIntent.putExtra("fmodel", selectedFloorIndex);
                    returnIntent.putExtra("floor_plan_path", floor_plan_file.getAbsolutePath());
                    returnIntent.putExtra("message", result);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }

                @Override
                public void onErrorOrCancel(String result) {
                    if (dialog != null)
                        dialog.dismiss();
                    Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("message", result);
                    setResult(RESULT_CANCELED);
                    finish();

                }

                @Override
                public void onPrepareLongExecute() {
                    dialog = new ProgressDialog(SelectBuildingActivity.this);
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
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getBaseContext(), "Nie wybrano budynku oraz piętra!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

}

