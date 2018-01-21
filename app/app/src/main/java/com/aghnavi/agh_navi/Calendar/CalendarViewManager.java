package com.aghnavi.agh_navi.Calendar;

import android.support.annotation.NonNull;
import android.widget.CalendarView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Scarf_000 on 21.09.2017.
 */

public class CalendarViewManager {
    private CompactCalendarView calendarView;
    private ArrayList<String> eventArray;

    public CalendarViewManager(CompactCalendarView cv){
        this.calendarView = cv;
    }

    private void initCalendar(final List<Event> events){
        /*calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                for(Event event : events){
                   // if(event.getStart().getDateTime())
                }
            }
        });*/
    }

    protected void markInCalendar(List<Event> events){
        List<String> eventStrings = new ArrayList<String>();
        for (Event event : events) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDateTime();
            }
            //SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy hh:mm aaa");

            eventStrings.add(
                    String.format("%s - %s",  start, event.getSummary()));
        }
        initCalendar(events);
    }
}
