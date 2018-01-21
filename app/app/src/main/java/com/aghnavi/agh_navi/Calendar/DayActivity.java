package com.aghnavi.agh_navi.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aghnavi.agh_navi.Database.DatabaseConnector;
import com.aghnavi.agh_navi.DateUtils.DateUtils;
import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;
import com.google.api.client.util.DateTime;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DayActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener, FabricInitializer {

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private List<EventSerializable> mEventSerializableList;
    private List<EventSerializable> recuringEvents;
    private DayActivityAdapter mAdapter;
    public final static int REQ_CODE_CHILD = 1101;
    private Date mPassedDate;
    private DatabaseConnector database;
    private Date startDate;
    private Date endDate;
    private EventSerializable curEditedEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.day_events_list);
        mPassedDate = (Date) getIntent().getSerializableExtra("Date");
        Log.d("DayActivity","I received this date: "+mPassedDate.toString());

        curEditedEvent = null;


        String passedDateString = DateUtils.dateToString(mPassedDate);

        mRecyclerView = (RecyclerView) findViewById(R.id.events);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Room init
        database = new DatabaseConnector(getApplicationContext());


        // getting events by date

        //mEventSerializableList = storage.getEventsByDate(mPassedDate);
        String day = DateUtils.dateToString(mPassedDate);
        String startTime = "00:00";
        String endTime = "23:59";
        startDate = null;
        endDate = null;
        try {
            startDate = DateUtils.stringToDate(day,startTime);
            endDate = DateUtils.stringToDate(day,endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mEventSerializableList = new ArrayList<>();
        mAdapter = new DayActivityAdapter(mRecyclerView, mEventSerializableList, getApplicationContext(), DayActivity.this);
        mRecyclerView.setAdapter(mAdapter);


        database.getAllEventsBetweenDates(startDate.getTime(),endDate.getTime()).observe(this,event ->{
            if(event != null) {
                mEventSerializableList = event;
                //mAdapter = new DayActivityAdapter(mRecyclerView, mEventSerializableList, getApplicationContext(), DayActivity.this);
                //mRecyclerView.setAdapter(mAdapter);
                mAdapter.updateData(mEventSerializableList);
            }
        });

        recuringEvents = new ArrayList<>();
        database.getAllRecurringEvents().observe(this,event->{
            if(event != null){
                recuringEvents = event;
                int startSize = mEventSerializableList.size();
                for(EventSerializable e : recuringEvents) {
                    if (e.getRecurrence() == null) {
                        Log.e("DayActivity", "Database returned event with null recursion");
                    } else {
                        Log.d("DayActivity", "My event is: " + e.toString());
                        if (DateUtils.isLater(startDate, DateUtils.dateTimeToDate(e.getDateTimeStart()))) {
                            Log.d("DayActivity", "Event: " + DateUtils.dateToString(startDate) + " is later than " + e.toString());
                            switch (e.getRecurrence()) {
                                case "CODZIENNIE":
                                    mEventSerializableList.add(e);
                                    Log.d("DayActivity", "Adding recurring event: " + e.toString());
                                    break;
                                case "TYGODNIOWO":
                                    if (DateUtils.getDayOfTheWeekFromDateTime(e.getDateTimeStart()) == DateUtils.getDayOfTheWeekFromDate(startDate)) {
                                        Log.d("DayActivity", "Adding recurring event: " + e.toString());
                                        mEventSerializableList.add(e);
                                    }
                                    break;
                                case "MIESIĘCZNIE":
                                    if (DateUtils.getDayFromDateTime(e.getDateTimeStart()) == DateUtils.getDayFromDate(startDate)) {
                                        Log.d("DayActivity", "Adding recurring event: " + e.toString());
                                        mEventSerializableList.add(e);
                                    }
                                    break;
                                case "ROCZNIE":
                                    if (DateUtils.getMonthFromDateTime(e.getDateTimeStart()) == DateUtils.getMonthFromDate(startDate)) {
                                        if (DateUtils.getDayFromDateTime(e.getDateTimeStart()) == DateUtils.getDayFromDate(startDate)) {
                                            Log.d("DayActivity", "Adding recurring event: " + e.toString());
                                            mEventSerializableList.add(e);
                                        }
                                    }
                                    break;
                                default:
                                    Log.e("DayActivity", "Recurrence is not null, but it doesn't match any case, it is: " + e.getRecurrence());
                            }
                        }
                    }
                }
                if (startSize != mEventSerializableList.size()) {
                    mAdapter.updateData(mEventSerializableList);
                }
            }
        });

        //mAdapter = new DayActivityAdapter(mRecyclerView, mEventSerializableList, getApplicationContext(), DayActivity.this);
        //mRecyclerView.setAdapter(mAdapter);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        mToolbar.setTitle(passedDateString);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.day_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
                Intent i = new Intent(DayActivity.this, AddEventActivityNew.class);
                EventSerializable es = new EventSerializable();
                es.setDateTimeStart(new DateTime(mPassedDate.getTime(),60));
                es.setDateTimeEnd(new DateTime(mPassedDate.getTime()+3600000,60));
                i.putExtra("event", es);

                //todo: handle result and back button pressed &JAN
                startActivityForResult(i, REQ_CODE_CHILD);
                break;

            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                search(searchView);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_CHILD:
                //this is new added event
                if(resultCode == 1){
                    Log.d("ReturnedResult", "The result has returned");
                    EventSerializable resultEventSerializable = (EventSerializable) data.getSerializableExtra("ResultEvent");
                    if (resultEventSerializable.isEmpty() || resultEventSerializable == null) {
                        Toast.makeText(getApplicationContext(), "Wydarzenie nie zostało zapisane!", Toast.LENGTH_SHORT).show();
                    } else {

                        //normal way
                        database.addEventAsync(resultEventSerializable);
                        mEventSerializableList.add(resultEventSerializable);
                        mAdapter.updateData(resultEventSerializable);
                    }
                }
                //returning edited event
                else if(resultCode == 2){
                    EventSerializable resultEventSerializable = (EventSerializable) data.getSerializableExtra("editedEvent");
                    if (resultEventSerializable.isEmpty() || resultEventSerializable == null) {
                        Toast.makeText(getApplicationContext(), "Wydarzenie nie zostało zapisane!", Toast.LENGTH_SHORT).show();
                    } else {
                        database.editEventAsync(curEditedEvent,resultEventSerializable);
                        mEventSerializableList.remove(getEventIndex(curEditedEvent,mEventSerializableList));
                        mEventSerializableList.add(resultEventSerializable);
                        mAdapter.updateData(mEventSerializableList);
                    }
                }
                break;
        }
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

    //szymon
    //TODO: CALL FROM EVENTS LIST ON CLICK LISTENER
    private void processFinish(Serializable poi_id_etc) {
        Intent returnIntent = new Intent();
        if (poi_id_etc != null) { // we got what we wanted
            returnIntent.putExtra("calendar_event", poi_id_etc);
            //others if needed
            setResult(RESULT_OK, returnIntent);
            finish();
        }
        else // we didn't
            setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void editEvent(EventSerializable eventSerializable){
        curEditedEvent = eventSerializable;
        Intent intent = new Intent(DayActivity.this, EditEventActivity.class);
        intent.putExtra("event", eventSerializable);
        startActivityForResult(intent, REQ_CODE_CHILD);
    }

    public void deleteEvent(final EventSerializable eventSerializable){

        AlertDialog.Builder builder = new AlertDialog.Builder(DayActivity.this);
        builder.setTitle("Czy na pewno chcesz usunąć wydarzenie?");
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(eventSerializable != null){
                    database.removeEventAsync(eventSerializable);
                    int i=0;
                    for(EventSerializable e: mEventSerializableList){
                        if(e.getId().equals(eventSerializable.getId())){
                            mEventSerializableList.remove(i);
                            break;
                        }
                        i++;
                    }
                    mAdapter.updateData(mEventSerializableList);

                }else{
                    Toast.makeText(getApplicationContext(),"Nie ma takiego wydarzenia",Toast.LENGTH_SHORT).show();
                }


            }
        });
        builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        database.getAllEventsBetweenDates(startDate.getTime(),endDate.getTime()).observe(this,event ->{
            mEventSerializableList = event;
            mAdapter.updateData(mEventSerializableList);
        });
        android.app.AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    private boolean isEventAlreadyInList(EventSerializable e, List<EventSerializable> list){
        for(EventSerializable listEvent : list){
            if(listEvent.getId().equals(e.getId())){
                return true;
            }
        }
        return false;
    }

    private int getEventIndex(EventSerializable e, List<EventSerializable> list){
        int i=0;
        for(EventSerializable listEvent : list){
            if(listEvent.getId().equals(e.getId())){
                return i;
            }
            i++;
        }
        return -1;
    }
}
