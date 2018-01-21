package com.aghnavi.agh_navi.Settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import com.aghnavi.agh_navi.BaseNavigatorActivity;
import com.aghnavi.agh_navi.Calendar.CalendarActivity;
import com.aghnavi.agh_navi.MapNavigationActivity;
import com.aghnavi.agh_navi.MapsActivity;
import com.aghnavi.agh_navi.NavigatorFlowCodes;
import com.aghnavi.agh_navi.R;

import java.util.concurrent.Executors;

public class SettingsActivity extends BaseNavigatorActivity implements NavigatorFlowCodes{

    private Toolbar mToolbar;
    private PreferenceFragment mPreferenceFragment;
    private Preference mInformationPreference;
    private Preference mPolicyPrivacyPreference;
    private Preference mRelatedAccountsPreference;

    //FOR DATE EVENT PICK (Szymon)
    private final static int CALENDAR_EVENT_ACTIVITY_RESULT = 1116;
    private final static int NEARBY_PLACE_ACTIVITY_RESULT = 1118;

    private final static int NEARBY_FROM_SETTINGS = 2525;

    //private static final int EVENT_RESULT_OK = 4444; //result code for navigation


    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_settings);
        getLayoutInflater().inflate(R.layout.activity_settings, contentFrameLayout);
        sourceActivity = getIntent().getStringExtra("sourceActivity");


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Ustawienia");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        //FOR NAVIGATION DRAWER
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, navigationLayout,mToolbar,R.string.app_name, R.string.app_name);
        mToolbar.setContentInsetStartWithNavigation(0);
        navigationLayout.addDrawerListener(actionBarDrawerToggle);

        mPreferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
        mInformationPreference = mPreferenceFragment.findPreference(getString(R.string.informationKey));
        mPolicyPrivacyPreference = mPreferenceFragment.findPreference(getString(R.string.policyPrivacyKey));
        mRelatedAccountsPreference = mPreferenceFragment.findPreference(getString(R.string.relatedAccountsKey));

        mInformationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, InformationActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mPolicyPrivacyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, PolicyPrivacyActivity.class);
                startActivity(intent);
                return false;
            }
        });

        mRelatedAccountsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, RelatedAccountsActivity.class);
                startActivity(intent);
                return false;
            }
        });
    }

    //DRAWER INTEGRATION//
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(MENU_OPTION_SETTINGS).setChecked(true);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CALENDAR_EVENT_ACTIVITY_RESULT:
                if(sourceActivity.equals("MapsActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_SETTINGS) {
                        setResult(REQUEST_PLACE_PICK_TO_PLACE, data);
                        finish();
                    //add for EVENT/SOON
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                } else if(sourceActivity.equals("MapNavigationActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_SETTINGS) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_EVENT_PICK_TO_SETTINGS) {
                        setResult(EVENT_RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_SOON_PICK_TO_SETTINGS) {
                        setResult(SOON_RESULT_OK, data);
                        finish();
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
                break;
            case NEARBY_PLACE_ACTIVITY_RESULT:
                if(sourceActivity.equals("CalendarActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_SETTINGS) {
                        setResult(REQUEST_PLACE_PICK_TO_CALENDAR, data);
                        finish();
                    //add for EVENT/SOON
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                } else if(sourceActivity.equals("MapNavigationActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_SETTINGS) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_EVENT_PICK_TO_SETTINGS) {
                        setResult(EVENT_RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_SOON_PICK_TO_SETTINGS) {
                        setResult(SOON_RESULT_OK, data);
                        finish();
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
                break;
        }
    }

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
                requestCode = 0;
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
                if(itemId == R.id.nav_calendar || itemId == R.id.nav_places) {
                    startActivityForResult(intent, requestCode);
                }
                else {
                    startActivity(intent);
                    finish();
                }
            }
        });
        item.setChecked(true);
        navigationLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
