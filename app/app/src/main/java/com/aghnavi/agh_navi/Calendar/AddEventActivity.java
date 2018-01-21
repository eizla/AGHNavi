package com.aghnavi.agh_navi.Calendar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.Excpetions.MemoryException;
import com.aghnavi.agh_navi.Excpetions.NoDateException;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;
import com.google.api.client.util.DateTime;

import java.util.Date;

/**
 * Created by Scarf_000 on 11.10.2017.
 */

public class AddEventActivity extends AppCompatActivity implements FabricInitializer{
    private Button confirmEventButton;
    private DateTime dateTimeFrom;
    private DateTime dateTimeTo;
    private Date simpleDate;
    private String title;
    private String location;
    private String description;
    private EventSerializable event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.addeventscreen);

        simpleDate = (Date) getIntent().getSerializableExtra("Date");

        EditText dateField = (EditText) findViewById(R.id.event_date);
        dateField.setText(simpleDate.toString());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                event = createEvent();
                int resultCode = 1;
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ResultEvent", event );
                setResult(resultCode, resultIntent);
            }
        } catch (NoDateException e) {
            e.printStackTrace();
        } catch (MemoryException e) {
            e.printStackTrace();
        }

        confirmEventButton = (Button) findViewById(R.id.save_event_button);

        confirmEventButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                try {
                    event = createEvent();
                    int resultCode = 1;
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ResultEvent", event);
                    setResult(resultCode, resultIntent);
                    finish();

                } catch (NoDateException e) {
                    e.printStackTrace();
                } catch (MemoryException e) {
                    e.printStackTrace();
                }

            }

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private EventSerializable createEvent() throws NoDateException,MemoryException{
        //Getting date
        /*EditText dateText = (EditText) findViewById(R.id.event_date);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // Make sure user insert date into edittext in this format.


        try{
            String dob_var=(dateText.getText().toString());

            simpleDate = formatter.parse(dob_var);
        }
        catch (java.text.ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("E11111111111", e.toString());
        }*/

        //Getting event Name

        EditText eventNameText = (EditText) findViewById(R.id.event_name);
        title = (String) eventNameText.getText().toString();

        //Getting startTime
        TimePicker timePickerFrom = (TimePicker) findViewById(R.id.time_from);
        TimePicker timePickerTo = (TimePicker) findViewById(R.id.time_to);

        int hourFrom,hourTo,minutesFrom,minutesTo;
        hourFrom = timePickerFrom.getHour();
        hourTo = timePickerTo.getHour();
        minutesFrom = timePickerFrom.getMinute();
        minutesTo = timePickerTo.getMinute();

        Date dateFrom = new Date(simpleDate.getYear(),simpleDate.getMonth(),simpleDate.getDay(),hourFrom,minutesFrom);
        Date dateTo = new Date(simpleDate.getYear(),simpleDate.getMonth(),simpleDate.getDay(),hourTo,minutesTo);

        dateTimeFrom = new DateTime(dateFrom);
        dateTimeTo = new DateTime(dateTo);

        EditText locationText = (EditText) findViewById(R.id.location);
        location = (String) locationText.getText().toString();

        //Getting description
        EditText descriptionText = (EditText) findViewById(R.id.event_description);
        description = (String) descriptionText.getText().toString();

        //Creating Google Api event


      //  EventSerializable event = new EventSerializable(description,dateTimeFrom,dateTimeTo,location);

        /*Event event = new Event()
                .setSummary("Google I/O 2015")
                .setLocation("AGH, D-17, room 4.29")
                .setDescription("Laby z WDI");

        DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Poland");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Poland");
        event.setEnd(end);

        event.getStart().setDate(dateTimeFrom);
        event.getEnd().setDate(dateTimeTo);
        event.setDescription(eventName + " "+ description);
        event.setLocation(location);*/

        return event;
    }

}
