package com.aghnavi.agh_navi.Calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aghnavi.agh_navi.BaseNavigatorActivity;
import com.aghnavi.agh_navi.Database.DatabaseConnector;
import com.aghnavi.agh_navi.Database.EventsDatabase;
import com.aghnavi.agh_navi.DateUtils.DateUtils;
import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.MapNavigationActivity;
import com.aghnavi.agh_navi.MapsActivity;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.Settings.InformationActivity;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

//endregion

public class CalendarActivity extends BaseNavigatorActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener, Toolbar.OnMenuItemClickListener {

    //private static final int EVENT_RESULT_OK = 4444; //result code for navigation
    private static final int SETTINGS_ACTIVITY_RESULT = 10010;
    private static final int NEARBY_PLACE_ACTIVITY_RESULT = 10011;

    //region Fields
    private GoogleAccountCredential mCredential;
    private Toolbar mToolbar;
    private Button mLeftButton;
    private TextView mDateTextView;
    private Button mRightButton;
    private CompactCalendarView mCompactCalendarView;
    private Button mAddEventButton;
    private ProgressDialog mProgress;

    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat(" MMMM - yyyy", Locale.getDefault());
    private CalendarViewManager calendarViewManager;
    private DatabaseConnector database;
    private Date selectedDate;
    private List<Event> synchronizedEvents;
    private List<EventSerializable> offlineEvents;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    //FOR DATE EVENT PICK (Szymon)
    static final int REQUEST_EVENT_DAY_PICK_MAP_PLACE = 1004;
    static final int REQUEST_EVENT_DAY_PICK_MAP_NAVIGATION = 1005;
    static final int REQUEST_EVENT_DAY_PICK_SETTINGS = 1006;

    private static final int FINAL_REQUEST_TO_PLACES = 9987;
    private static final int FINAL_REQUEST_TO_SETTINGS = 9986;
    private static final int FINAL_REQUEST_TO_NAVIGATION = 9985;

    private String sourceActivity;


    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR};
    public final static int REQ_CODE_CHILD = 1101;

    //endregion

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_calendar);
        getLayoutInflater().inflate(R.layout.activity_calendar, contentFrameLayout);
        sourceActivity = getIntent().getStringExtra("sourceActivity");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mLeftButton = (Button) findViewById(R.id.leftButton);
        mRightButton = (Button) findViewById(R.id.rightButton);
        mDateTextView = (TextView) findViewById(R.id.dateTextView);
        mCompactCalendarView = (CompactCalendarView) findViewById(R.id.compactCalendarView);
        mAddEventButton = (Button) findViewById(R.id.addEventButton);

        //mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbar.setTitle("Kalendarz");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

        //FOR NAVIGATION DRAWER
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, navigationLayout,mToolbar,R.string.app_name, R.string.app_name);
        mToolbar.setContentInsetStartWithNavigation(0);
        navigationLayout.addDrawerListener(actionBarDrawerToggle);

        mCompactCalendarView.setUseThreeLetterAbbreviation(true);

        calendarViewManager = new CalendarViewManager(mCompactCalendarView);


        //Room database init
        database = new DatabaseConnector(this);

        selectedDate =  Calendar.getInstance().getTime();
        Calendar dateToday = Calendar.getInstance();
        String month = dateToday.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        int year = dateToday.get(Calendar.YEAR);
        mDateTextView.setText(month + " " + year);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Log.d("onDayClick", dateClicked.toString());

                if(synchronizedEvents == null){
                    Log.d("SynchronizedEvents","is null");
                    //return;
                }

                Intent i = new Intent(CalendarActivity.this , DayActivity.class);
                Log.d("Sending this date", dateClicked.toString());
                i.putExtra("Date", dateClicked);
                sourceActivity = "CalendarActivity";

                if(sourceActivity.equals("SettingsActivity") ) {
                    startActivityForResult(i, FINAL_REQUEST_TO_SETTINGS);
                } else if(sourceActivity.equals("MapsActivity")) {
                    startActivityForResult(i, FINAL_REQUEST_TO_PLACES);
                } else if(sourceActivity.equals("MapNavigationActivity")) {
                    startActivityForResult(i, FINAL_REQUEST_TO_NAVIGATION);
                } else{
                    startActivity(i);
                }

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mDateTextView.setText(dateFormatMonth.format(firstDayOfNewMonth));
                selectedDate.setMonth(firstDayOfNewMonth.getMonth());
            }
        });

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mAddEventButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CalendarActivity.this , AddEventActivityNew.class);
                //passing new EventSerializable to next activity
                EventSerializable es = new EventSerializable();
                if(es == null){
                    Log.e("CalendarActivity","EVENTSERIALIZABLE IS NULL!!!");
                }
                DateTime startDateTime = new DateTime(selectedDate.getTime(),60);
                Log.d("CalendarActivity","startTime: "+ DateUtils.dateTimeToString(startDateTime));
                //end time is hour later
                DateTime endDateTime = new DateTime(selectedDate.getTime()+3600000,60);
                Log.d("CalendarActivity","endTime: "+ DateUtils.dateTimeToString(startDateTime));
                es.setDateTimeStart(startDateTime);
                es.setDateTimeEnd(endDateTime);
                DateTime now = DateUtils.createCurrentDateTime();
                es.setCreated(now);
                es.setUpdated(now);

                i.putExtra("EventSerializable", es);

                startActivityForResult(i, REQ_CODE_CHILD);
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mCompactCalendarView.showPreviousMonth();
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompactCalendarView.showNextMonth();
            }
        });
    }

    private List<String> getEventNames(List<Event> events){
        List<String> sEvents = new ArrayList<>();
        for(Event e : events){
            sEvents.add(e.getSummary());
        }

        return  sEvents;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
          //  mOutputText.setText("No network connection available.");
            Toast.makeText(getApplicationContext(), "Brak połączenia z internetem!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("getResultFromApi","Start waiting for response");

            try{
                AsyncResponse asyncResponse = new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {

                        Log.d("GetResultsFromApi","Process has finished");

                        synchronizedEvents = (List<Event>) output;
                        for(Event e: synchronizedEvents){
                            Log.d("getResultsFromApi","Title: "+e.getSummary());
                            Log.d("getResultsFromApi","Date: "+e.getStart().getDate());
                            Log.d("getResultsFromApi","Time: "+e.getStart().getDateTime());
                            Log.d("getResultsFromApi","Original start: "+e.getOriginalStartTime());
                        }

                        //Room saving synchronized events into DB

                        List<EventSerializable> eventsToInsert = new ArrayList<>();
                        for(Event event: synchronizedEvents){
                            eventsToInsert.add(new EventSerializable(event));
                        }
                        Log.d("InsertAllGoogleEvents","Inserting all events into room database");
                        database.addEventsAsync(eventsToInsert,mCredential);



                        if(synchronizedEvents.isEmpty()){
                            Log.e("synchronizedEvents", "SynchronizedEvents is empty!!!");
                        }
                    }

                };
                MakeRequestTask makeRequestTask = new MakeRequestTask(mCredential, asyncResponse);
                makeRequestTask.execute();

            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNewEvent(EventSerializable sEvent) throws IOException {
        Log.d("addNewEventGoogle","Begining");
        if (! isGooglePlayServicesAvailable()) {
            Log.d("addNewEventGoogle","acquireGooglePlayServices");
            acquireGooglePlayServices();
        }
        if (mCredential.getSelectedAccountName() == null) {
            Log.d("addNewEventGoogle","chooseAccount");
            chooseAccount();
        }
        if (! isDeviceOnline()) {
            Log.d("addNewEventGoogle","No internet connection");
            Toast.makeText(getApplicationContext(),"Brak połączenia z internetem",Toast.LENGTH_SHORT).show();
        } else {
            Log.d("addNewEventGoogle","MakeRequestAddEventTask");
            new MakeRequestAddEventTask(mCredential,sEvent).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQ_CODE_CHILD:
                Log.d("ReturnedResult","The result has returned");
                EventSerializable resultEventSerializable = (EventSerializable) data.getSerializableExtra("ResultEvent");
                if(resultEventSerializable == null || resultEventSerializable.isEmpty()){
                    Toast.makeText(getApplicationContext() ,"Wydarzenie nie zostało zapisane!", Toast.LENGTH_SHORT).show();
                }else{

                    //adding to Room
                    Log.d("onResultCalendarAct",resultEventSerializable.toString());
                    database.addEventAsync(resultEventSerializable);
                }
                break;
            case RESULT_CANCELED:
                Toast.makeText(getApplicationContext() ,"Nie zapisano wydarzenia", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    /*mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");*/
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            //szymon
            case NEARBY_PLACE_ACTIVITY_RESULT:
                if(sourceActivity.equals("Settings")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_CALENDAR) {
                        setResult(FINAL_REQUEST_TO_SETTINGS, data);
                        finish();
                    //add for EVENT/SOON
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                } else if(sourceActivity.equals("MapNavigationActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_CALENDAR) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_EVENT_PICK_TO_CALENDAR) {
                        setResult(EVENT_RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_SOON_PICK_TO_CALENDAR) {
                        setResult(SOON_RESULT_OK, data);
                        finish();
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
                break;
            case SETTINGS_ACTIVITY_RESULT:
                if(sourceActivity.equals("MapsActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_CALENDAR) {
                        setResult(FINAL_REQUEST_TO_PLACES, data);
                        finish();
                    //add for EVENT/SOON
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                } else if(sourceActivity.equals("MapNavigationActivity")) {
                    if (resultCode == REQUEST_PLACE_PICK_TO_CALENDAR) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_EVENT_PICK_TO_CALENDAR) {
                        setResult(EVENT_RESULT_OK, data);
                        finish();
                    }
                    else if (resultCode == REQUEST_SOON_PICK_TO_CALENDAR) {
                        setResult(SOON_RESULT_OK, data);
                        finish();
                    } else if (resultCode == RESULT_CANCELED) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
                break;
            case FINAL_REQUEST_TO_PLACES:
                if(resultCode == RESULT_OK) {
                    setResult(REQUEST_PLACE_PICK_TO_PLACE, data);
                    finish();
                }
                else if (resultCode == RESULT_CANCELED) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            case FINAL_REQUEST_TO_NAVIGATION:
                if(resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                else if (resultCode == RESULT_CANCELED) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            case FINAL_REQUEST_TO_SETTINGS:
                if(resultCode == RESULT_OK) {
                    setResult(REQUEST_PLACE_PICK_TO_SETTINGS, data);
                    finish();
                }
                else if (resultCode == RESULT_CANCELED) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;


            /*case REQUEST_EVENT_DAY_PICK_MAP_PLACE:
                if (resultCode == RESULT_OK) {
                    setResult(EVENT_RESULT_OK, data);
                    finish();
                }*/
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private List<String> eventsToStrings(List<Event> events){

        List<String> eventStrings = new ArrayList<>();
        for (Event event : events) {

            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s)", event.getSummary(), start));
        }
        return eventStrings;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.calendar_menu, menu);

        return true;
    }

    @Override
    public void onClick(View v) {

        //todo: handle navigation button pressed
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_synchronize:
                Log.d("onMenuItemClick","calling getResultFromApi");
                getResultsFromApi();
                offlineEvents = new ArrayList<>();
                database.getAllOfflineEvents().observe(this,event ->{
                    if(event != null) {
                        offlineEvents = event;
                        Log.d("GoogleInsert","Start inserting offline events");
                        for(EventSerializable e : offlineEvents){
                            Log.d("GoogleInsert",e.toString());
                            try {
                                //adding event to Google Calendar
                                Log.d("OffilneEvents","Inserting offline event: "+e.toString());
                                addNewEvent(e);
                            } catch (IOException exc) {
                                Log.e("ResultEvent", "IOException");
                                exc.printStackTrace();
                            }
                        }
                    }
                });


                break;
            case R.id.action_add:
                Intent i = new Intent(CalendarActivity.this, AddEventActivityNew.class);
                EventSerializable es = new EventSerializable();
                if(es == null){
                    Log.e("CalendarActivity","EVENTSERIALIZABLE IS NULL!!!");
                }
                DateTime startDateTime = new DateTime(selectedDate.getTime(),60);
                Log.d("CalendarActivity","startTime is: "+ DateUtils.dateTimeToString(startDateTime));
                //end time is hour later
                DateTime endDateTime = new DateTime(selectedDate.getTime()+3600000,60);
                Log.d("CalendarActivity","endTime is: "+ DateUtils.dateTimeToString(startDateTime));
                es.setDateTimeStart(startDateTime);
                es.setDateTimeEnd(endDateTime);
                Log.d("CalendarActivity","Event which is going to be passed: "+ es.toString());

                i.putExtra("event", es);

                startActivityForResult(i, REQ_CODE_CHILD);
                break;
        }

        return true;
    }

    public interface AsyncResponse {

        void processFinish(Object output);
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        public com.google.api.services.calendar.Calendar getmService() {
            return mService;
        }

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private List<Event> requestedEvents;
        List<Event> items = null;
        public AsyncResponse delegate = null;

        public List<Event> getRequestedEvents() {
            return requestedEvents;
        }

        public void setRequestedEvents(List<Event> requestedEvents) {
            this.requestedEvents = requestedEvents;
        }

        MakeRequestTask(GoogleAccountCredential credential, AsyncResponse asyncResponse) {
            delegate = asyncResponse;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("AGH Navi")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            Log.d("GetDataFromApi","Doing in background");
            try {
                List<Event> events = getDataFromApi();
                this.setRequestedEvents(events);
                return eventsToStrings(events);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<Event> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            Log.d("GetDataFromApi","Beginning");
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();
            return items;

        }




        @Override
        protected void onPreExecute() {
           // mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
               // mOutputText.setText("No results returned.");
                Toast.makeText(getApplicationContext() ,"No results returned.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext() ,"Successfully connected to google calendar API.", Toast.LENGTH_SHORT).show();
                delegate.processFinish(requestedEvents);
              //  output.add(0, "Data retrieved using the Google Calendar API:");
               // mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarActivity.REQUEST_AUTHORIZATION);
                } else {
                    /*mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());*/
                }
            } else {
               // mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class MakeRequestAddEventTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private EventSerializable sEvent;

        MakeRequestAddEventTask(GoogleAccountCredential credential, EventSerializable sEvent) {
            Log.d("MakeRequestAddEventTask","Trying to create event");
            this.sEvent = sEvent;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("AGH Navi")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            Log.d("doInBack","Adding event to Google Calendar");
            try {
                Event event = this.addEvent();
                EventsDatabase.getAppDatabase(getApplicationContext()).eventDao().delete(this.sEvent);
                sEvent.setGoogleEventId(event.getId());
                EventsDatabase.getAppDatabase(getApplicationContext()).eventDao().insert(this.sEvent);
                String stringEvent = event.toString();
                Log.d("doInBack","Added event: "+stringEvent);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private Event addEvent() throws IOException {
            Log.d("doInBack","Final event addition method");
            Event event = sEvent.toGoogleEvent();
            Log.d("doInBack","After transformation to google event");
            String calendarId = "primary";
            event = mService.events().insert(calendarId, event).execute();
            Log.d("Add Google Event","Event created: %s\n"+event.getHtmlLink());
            return event;
        }

        @Override
        protected void onPreExecute() {
            Log.d("GoogleEventAdd","PreExecute");
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();

            if (output == null || output.size() == 0) {
               // mOutputText.setText("No event added");
                Log.d("GoogleEventAdd","No event added to Google");
            } else {
                //output.add(0, "Added one new event: ");
                Log.d("GoogleEventAdd","Event added successfully");
               // mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarActivity.REQUEST_AUTHORIZATION);
                } else {
                   // mOutputText.setText("The following error occurred:\n"
                           // + mLastError.getMessage());
                    Log.d("Add new event",("The following error occurred:\n"
                            + mLastError.getMessage()));
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class MakeRequestUpdateEventTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private EventSerializable sEvent;

        MakeRequestUpdateEventTask(GoogleAccountCredential credential, EventSerializable sEvent) {
            Log.d("MakeRequestAddEventTask","Trying to create event");
            this.sEvent = sEvent;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("AGH Navi")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            Log.d("doInBack","Adding event to Google Calendar");
            try {
                Event event = this.updateEvent();
                EventsDatabase.getAppDatabase(getApplicationContext()).eventDao().delete(this.sEvent);
                sEvent.setGoogleEventId(event.getId());
                EventsDatabase.getAppDatabase(getApplicationContext()).eventDao().insert(this.sEvent);
                String stringEvent = event.toString();
                Log.d("doInBack","Updated event: "+stringEvent);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private Event updateEvent() throws IOException {
            Log.d("doInBack","Final event updating method");
            Event event = sEvent.toGoogleEvent();
            Log.d("doInBack","After transformation to google event");
            String calendarId = "primary";
            event = mService.events().update(calendarId, event.getId(), event).execute();
            Log.d("Add Google Event","Event created: %s\n"+event.getHtmlLink());
            return event;
        }

        @Override
        protected void onPreExecute() {
            Log.d("GoogleEventUpdate","PreExecute");
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();

            if (output == null || output.size() == 0) {
                // mOutputText.setText("No event added");
                Log.d("GoogleEventUpdate","No event updated");
            } else {
                //output.add(0, "Added one new event: ");
                Log.d("GoogleEventUpdate","Event updated successfully");
                // mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarActivity.REQUEST_AUTHORIZATION);
                } else {
                    // mOutputText.setText("The following error occurred:\n"
                    // + mLastError.getMessage());
                    Log.d("Update event",("The following error occurred:\n"
                            + mLastError.getMessage()));
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }


    //DRAWER INTEGRATION//
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(MENU_OPTION_CALENDAR).setChecked(true);
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
                activityClass = MapNavigationActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = 0;
                break;
            case R.id.nav_calendar:
                activityClass = CalendarActivity.class;
                intent = new Intent(getApplicationContext(), activityClass);
                requestCode = 0;
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
                if(itemId == R.id.nav_places || itemId == R.id.nav_settings) {
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