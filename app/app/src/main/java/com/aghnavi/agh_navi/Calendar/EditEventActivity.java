package com.aghnavi.agh_navi.Calendar;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aghnavi.agh_navi.DateUtils.DateUtils;
import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.Tools.DateDialog;
import com.aghnavi.agh_navi.Tools.RecurrenceDialog;
import com.aghnavi.agh_navi.Tools.TimeDialog;
import com.aghnavi.agh_navi.Tools.TypeDialog;
import com.aghnavi.agh_navi.fabric.FabricInitializer;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditEventActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, FabricInitializer {

    private Toolbar mToolbar;
    private EditText mStartDateEditText, mStartDateTimeEditText,
            mEndDateEditText, mEndDateTimeEditText,
            mEventNameEditText, mEventTypeEditText, mEventDescriptionEditText,mEventRecurrenceEditText;

    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;

    private Button mSaveButton;

    private String startDate, endDate, startTime, endTime, eventName, eventType, eventDescription, eventRecurrence, eventId, eventGoogleId;
    private Place placeSelected;
    private EventSerializable mPassedEventSerializable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.activity_edit_event);

        mPassedEventSerializable = (EventSerializable) getIntent().getSerializableExtra("event");
        Log.d("EditEvent","Date passed: "+ mPassedEventSerializable.getTitle().toString());

        eventId = mPassedEventSerializable.getId();
        eventGoogleId = mPassedEventSerializable.getGoogleEventId();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        mToolbar.setTitle("Edytuj wydarzenie");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

        mStartDateEditText = (EditText) findViewById(R.id.startDateEditText);
        mStartDateTimeEditText = (EditText) findViewById(R.id.startDateTimeEditText);
        mEndDateEditText = (EditText) findViewById(R.id.endDateEditText);
        mEndDateTimeEditText = (EditText) findViewById(R.id.endDateTimeEditText);
        mEventNameEditText = (EditText) findViewById(R.id.eventNameEditText);
        mEventTypeEditText = (EditText) findViewById(R.id.eventTypeEditText);
        mEventRecurrenceEditText = (EditText) findViewById(R.id.eventRecurrenceEditText);
        //   mEventPlaceEditText = (EditText) findViewById(R.id.eventPlaceEditText);
        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mEventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);
        mSaveButton = (Button) findViewById(R.id.saveChangesButton);

        if(mPassedEventSerializable.getDateTimeStart() != null) {
            mStartDateEditText.setText(DateUtils.dateTimeToString(mPassedEventSerializable.getDateTimeStart()));
            mStartDateTimeEditText.setText(DateUtils.dateTimeToHours(mPassedEventSerializable.getDateTimeStart()));
        }else{
            mStartDateEditText.setText("01-01-2017");
            mStartDateTimeEditText.setText("12:00");
        }

        if(mPassedEventSerializable.getDateTimeEnd() != null) {
            mEndDateEditText.setText(DateUtils.dateTimeToString(mPassedEventSerializable.getDateTimeEnd()));
            mEndDateTimeEditText.setText(DateUtils.dateTimeToHours(mPassedEventSerializable.getDateTimeEnd()));
        }else{
            mEndDateEditText.setText("01-01-2017");
            mEndDateTimeEditText.setText("13:00");
        }

        if(mPassedEventSerializable.getTitle() != null) {
            mEventNameEditText.setText(mPassedEventSerializable.getTitle());
        }else{
            mEventNameEditText.setText("Nazwa wydarzenia");
        }

        if(mPassedEventSerializable.getType() != null) {
            mEventTypeEditText.setText(mPassedEventSerializable.getType());
        }else{
            mEventTypeEditText.setText("INNE");
        }

        if(mPassedEventSerializable.getRecurrence() != null) {
            mEventRecurrenceEditText.setText(mPassedEventSerializable.getRecurrence());
        }else{
            mEventRecurrenceEditText.setText("NIE POWTARZAJ");
        }

        if(mPassedEventSerializable.getLocation() != null) {
            mPlaceAutocompleteFragment.setText(mPassedEventSerializable.getLocation());
        }else{
            mPlaceAutocompleteFragment.setText("");
        }

        if(mPassedEventSerializable.getDescription() != null) {
            mEventDescriptionEditText.setText(mPassedEventSerializable.getDescription());
        }else{
            mEventDescriptionEditText.setText("");
        }
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                placeSelected = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("b", "An error occurred: " + status);
            }
        });



        //Setting empty result to prevent crash after clicking back button

        Intent resultIntent = new Intent();
        resultIntent.putExtra("ResultEvent", mPassedEventSerializable);
        setResult(RESULT_CANCELED, resultIntent);

        //mEventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);

        mStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateDialog dateDialog = DateDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("name", "mStartDateEditText");
                args.putString("activity", "EditEventActivity");
                dateDialog.setArguments(args);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                dateDialog.show(ft, "DatePicker");
                mStartDateEditText.setTextColor(Color.BLACK);
            }
        });

        mStartDateTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeDialog mTimePickerDialog = TimeDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("name", "mStartDateTimeEditText");
                args.putString("activity", "EditEventActivity");
                mTimePickerDialog.setArguments(args);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                mTimePickerDialog.show(ft, "TimePicker");
                mStartDateTimeEditText.setTextColor(Color.BLACK);

            }
        });

        mEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateDialog dateDialog = DateDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("name", "mEndDateEditText");
                args.putString("activity", "EditEventActivity");
                dateDialog.setArguments(args);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                dateDialog.show(ft, "DatePicker");
                mStartDateEditText.setTextColor(Color.BLACK);
            }
        });

        mEndDateTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeDialog mTimePickerDialog = TimeDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("name", "mEndDateTimeEditText");
                args.putString("activity", "EditEventActivity");
                mTimePickerDialog.setArguments(args);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                mTimePickerDialog.show(ft, "TimePicker");
                mEndDateTimeEditText.setTextColor(Color.BLACK);

            }
        });

        mEventTypeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = TypeDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("activity", "EditEventActivity");
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "dialog");

            }
        });

        mEventRecurrenceEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = RecurrenceDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("activity", "EditEventActivityNew");
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "dialog");

            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedEvent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_event_menu, menu);

        return true;
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_ready:
                saveEditedEvent();
                break;
        }
        return true;
    }


    private Boolean checkValues(){

        startDate = mStartDateEditText.getText().toString();
        endDate = mEndDateEditText.getText().toString();
        startTime = mStartDateTimeEditText.getText().toString();
        endTime = mEndDateTimeEditText.getText().toString();
        eventName = mEventNameEditText.getText().toString();
        eventType = mEventTypeEditText.getText().toString();
        if(mEventRecurrenceEditText == null || mEventRecurrenceEditText.getText().toString().equals("NIE POWTARZAJ")){
            eventRecurrence = null;
        }else{
            eventRecurrence = mEventRecurrenceEditText.getText().toString();
        }

        if( ! checkIfAllValuesSet()){
            Toast.makeText(getApplicationContext(), "Uzupełnij czerwone pola", Toast.LENGTH_SHORT).show();
            return false;
        }

        if( !compareDates(startDate, endDate)) {
            Toast.makeText(getApplicationContext(), "Nieprawidłowe daty", Toast.LENGTH_SHORT).show();
            mStartDateEditText.setTextColor(Color.RED);
            mEndDateEditText.setTextColor(Color.RED);
            return false;
        }
        else{
            if( startDate.equals(endDate) && !compareTime(startTime, endTime)){
                Toast.makeText(getApplicationContext(), "Nieprawidłowy zakres czasowy", Toast.LENGTH_SHORT).show();
                mStartDateTimeEditText.setTextColor(Color.RED);
                mEndDateTimeEditText.setTextColor(Color.RED);
                return false;
            }
        }
        return true;

    }

    private boolean checkIfAllValuesSet(){

        boolean allValuesSet = true;

        if(startDate == null || startDate.isEmpty()){

            allValuesSet = false;
            mStartDateEditText.setHintTextColor(Color.RED);
        }
        if(endDate == null || endDate.isEmpty()){
            allValuesSet = false;
            mEndDateEditText.setHintTextColor(Color.RED);
        }
        if(startTime == null || startTime.isEmpty()){
            allValuesSet = false;
            mStartDateTimeEditText.setHintTextColor(Color.RED);
        }
        if(endTime == null || endTime.isEmpty()){
            allValuesSet = false;
            mEndDateTimeEditText.setHintTextColor(Color.RED);
        }
        if(eventName == null || eventName.isEmpty()){
            allValuesSet = false;
            mEventNameEditText.setHintTextColor(Color.RED);
        }
        if(eventType == null || eventType.isEmpty()){
            allValuesSet = false;
            mEventTypeEditText.setHintTextColor(Color.RED);
        }
        /*if(placeSelected == null ){
            allValuesSet = false;
            //mEventPlaceEditText.setHintTextColor(Color.RED);
        }*/
        if(eventDescription == null || eventDescription.isEmpty()){
            eventDescription = " ";
        }
        return allValuesSet;

    }

    private boolean compareDates(String startDate, String endDate){

        boolean isStartBeforeEnd = false;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date mStartDate, mEndDate;
        try{
            mStartDate = dateFormat.parse(startDate);
            mEndDate = dateFormat.parse(endDate);

            if(mStartDate.before(mEndDate) || mStartDate.compareTo(mEndDate) == 0){
                isStartBeforeEnd = true;
            }

        }catch(ParseException p){
            Log.d("Error", p.getMessage());
        }

        return isStartBeforeEnd;
    }

    private boolean compareTime(String startTime, String endTime){

        boolean isStartBeforeEnd = false;

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
        Date mStartTime, mEndTime;
        try{
            mStartTime = timeFormat.parse(startTime);
            mEndTime = timeFormat.parse(endTime);

            if(mStartTime.before(mEndTime)){
                isStartBeforeEnd = true;
            }


        }catch (ParseException p){
            Log.d("Error", p.getMessage());
        }

        return  isStartBeforeEnd;
    }

    private EventSerializable returnEvent(){

        DateTime startDate_correctFormat = DateUtils.stringToDateTime(startDate,startTime);
        DateTime endDate_correctFormat = DateUtils.stringToDateTime(endDate,endTime);
        Log.d("returnEvent","startDate: "+startDate_correctFormat.toString());
        Log.d("returnEvent","endDate: "+endDate_correctFormat.toString());

        EventSerializable event = new EventSerializable(eventName,
                eventDescription,
                null,
                startDate_correctFormat,
                null,
                endDate_correctFormat,
                null,//placeSelected.getName().toString(), //todo: zmienic na PlaceSerializable
                eventType);
        event.setRecurrence(eventRecurrence);
        event.setUpdated(DateUtils.createCurrentDateTime());
        if(placeSelected != null){
            event.setLocation(placeSelected.getName().toString());
        }
        event.setId(eventId);
        event.setGoogleEventId(eventGoogleId);

        return event;
    }


    public void doPositiveClick(String eventType) {
        mEventTypeEditText.setText(eventType);
    }

    public void doPositiveClickOnRecurrence(String eventRecurrence) {
        mEventRecurrenceEditText.setText(eventRecurrence);
    }

    private void saveEditedEvent(){

        EventSerializable event = null;
        if(checkValues()){
            Log.d("addEvent","CORRECT VALUES");
            event = returnEvent();
            Log.d("addEvent","After event creation");
        }

        if(event != null){
            int resultCode = 2;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("editedEvent", event);
            setResult(resultCode, resultIntent);
            Log.d("editEvent","Before finish");
            finish();
        }
    }

    public void onTimeSet(List<String> args){

        switch(args.get(0)){
            case "mStartDateTimeEditText":
                mStartDateTimeEditText.setText(args.get(1));
                break;
            case "mEndDateTimeEditText":
                mEndDateTimeEditText.setText(args.get(1));
                break;
        }
    }

    public void onDateSet(List<String> args){

        switch(args.get(0)){
            case "mStartDateEditText":
                mStartDateEditText.setText(args.get(1));
                break;
            case "mEndDateEditText":
                mEndDateEditText.setText(args.get(1));
                break;
        }
    }
}
