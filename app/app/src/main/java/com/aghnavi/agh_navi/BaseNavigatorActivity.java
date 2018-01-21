package com.aghnavi.agh_navi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.aghnavi.agh_navi.Calendar.CalendarActivity;
import com.aghnavi.agh_navi.Settings.InformationActivity;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import java.util.concurrent.Executors;


public class BaseNavigatorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigatorFlowCodes, FabricInitializer {

    protected static final int MENU_OPTION_MAP = 0;
    protected static final int MENU_OPTION_CALENDAR = 1;
    protected static final int MENU_OPTION_PLACES = 2;
    protected static final int MENU_OPTION_SETTINGS = 3;
    protected static final int MENU_OPTION_ABOUT = 4;

    protected FrameLayout contentFrameLayout;
    protected DrawerLayout navigationLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    //Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.navigator_main_layout);


        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //actionBarDrawerToggle = new ActionBarDrawerToggle(this, navigationLayout, toolbar, R.string.drawer_open, R.string.drawer_closed);
        //navigationLayout.addDrawerListener(actionBarDrawerToggle);

        navigationLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame); //Remember this is the FrameLayout area within your main layout

        navigationView = (NavigationView) findViewById(R.id.navigator_view);
        navigationView.setNavigationItemSelectedListener(this);

        //getLayoutInflater().inflate(R.layout.map_navigation_activity, contentFrameLayout);
        //navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.isChecked()) {
            navigationLayout.closeDrawer(Gravity.START);
            return false;
        }

        Class<? extends Activity> activityClass = null;
        switch (item.getItemId()) {

            case R.id.nav_map:
                activityClass = MapNavigationActivity.class;
                break;
            case R.id.nav_calendar:
                activityClass = CalendarActivity.class;
                break;
            case R.id.nav_places:
                activityClass = MapsActivity.class;
                break;
            case R.id.nav_settings:
                activityClass = com.aghnavi.agh_navi.Settings.SettingsActivity.class;
                break;
            case R.id.nav_about:
                activityClass = InformationActivity.class;
                break;
        }

        final Class<?> finalActivityClass = activityClass;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), finalActivityClass);
                startActivity(intent);
            }
        });
        item.setChecked(true);
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
