package com.aghnavi.agh_navi.Database;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.aghnavi.agh_navi.Event.EventSerializable;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scarf_000 on 07.12.2017.
 */

public class DatabaseConnector {
    private static final String TAG = DatabaseConnector.class.getName();
    private static Context context;
    private static DoAsync task;

    public static DoAsync getTask() {
        return task;
    }

    public static Context getContext() {
        return context;
    }

    public DatabaseConnector(Context con){
        context = con;
        task = new DoAsync(context,"DO_NOTHING");
    }

    public static void addEventsAsync(List<EventSerializable> list, GoogleAccountCredential mCredential){
        DoAsync task = new DoAsync(context,"ADD_ALL");
        task.setT("ADD_ALL");
        Log.d("addEventAsync","Starting task, adding events: "+list.get(0).toString());
        task.setList(list);
        task.setCredential(mCredential);
        task.execute();
        Log.d("addEventAsync","MainThread after task, adding events: "+list.get(0).toString());
    }

    public static void addEventAsync(EventSerializable e){
        DoAsync task = new DoAsync(context,"ADD");
        task.setT("ADD");
        task.setE(e);
        Log.d("addEventAsync","Starting task, adding event: "+e.toString());
        task.execute();
        Log.d("addEventAsync","MainThread after task, adding event: "+e.toString());
    }

    public static void  editEventAsync(EventSerializable toBeReplaced, EventSerializable toReplace){
        DoAsync task = new DoAsync(context,"EDIT");
        task.setT("EDIT");
        task.setE(toBeReplaced);
        task.setE1(toReplace);
        Log.d("addEventAsync","Starting task, editing event: "+toBeReplaced.toString());
        task.execute();
        Log.d("addEventAsync","MainThread after task, event has changed to: "+toReplace.toString());
    }

    public static void getAllAsync(){
        DoAsync task = new DoAsync(context,"GET_ALL");
        task.setT("GET_ALL");
        task.execute();
    }

    public static void removeEventAsync(EventSerializable e){
        DoAsync task = new DoAsync(context,"REMOVE");
        task.setT("REMOVE");
        task.setE(e);
        Log.d("addEventAsync","Starting task, removing event: "+e.toString());
        task.execute();
        Log.d("addEventAsync","MainThread after task, event: "+e.toString()+" has been removed");
    }

    public static  LiveData<List<EventSerializable>> getAllOfflineEvents(){
        return EventsDatabase.getAppDatabase(context).eventDao().getAllOfflineEvents();
    }

    public static LiveData<List<EventSerializable>> getAllLiveData(){
        return  EventsDatabase.getAppDatabase(context).eventDao().getAllLiveData();
    }

    public static LiveData<List<EventSerializable>> getAllEventsBetweenDates(Long start, Long end){
        return  EventsDatabase.getAppDatabase(context).eventDao().getEventsBetweenDates(start,end);
    }

    public static LiveData<List<EventSerializable>> getAllRecurringEvents(){
        return  EventsDatabase.getAppDatabase(context).eventDao().getRecurringEvents();
    }

    private static void addEvent(EventsDatabase database, EventSerializable es){
        Log.d("asyncAddEvent","Inside async method, event: "+es.toString());
        database.eventDao().insert(es);
        Log.d("asyncAddEvent","Inside async method after addition, event: "+es.toString());
    }

    private static void addEvents(EventsDatabase database, List<EventSerializable> list, GoogleAccountCredential mCredential){
        List<EventSerializable> futureGoogleCalendarEvents = new ArrayList<>();
        List<EventSerializable> eventsToInsert = new ArrayList<>();
        for(EventSerializable google_event : list){
            Log.d(TAG,"Checking if there are duplicates in db");
            List<EventSerializable> duplicates = database.eventDao().getEventsByGoogleId(google_event.getGoogleEventId());
            if(duplicates != null && !duplicates.isEmpty()){
                Log.d(TAG,"This event: "+google_event.toString()+"is already in database");
                Log.d(TAG,"Checking if event has changed");
                if(duplicates.size() > 1){
                    Log.e(TAG,"There is more than one duplicate in DB");
                    Log.e(TAG,"Logging duplicates events: ");
                    for(EventSerializable dup : duplicates){
                        Log.e(TAG,"\t"+dup.toString());
                    }
                } else{
                    try {
                        EventSerializable database_event = duplicates.get(0);
                        if(!google_event.hasSameFieldsValues(database_event)){
                            //events are different
                            if(database_event.getUpdated() == null || google_event.getUpdated() == null){
                                if(database_event.getCreated() == null || google_event.getCreated() ==null){
                                    Log.e(TAG,"Field updated was null and field created was also null!!");
                                    //replacing events in db
                                    database.eventDao().delete(database_event);
                                    database.eventDao().insert(google_event);
                                    Log.d(TAG,"Event was successfully replaced!");
                                }
                                Log.e(TAG,"Field updated was null");
                                //replacing events in db
                                database.eventDao().delete(database_event);
                                database.eventDao().insert(google_event);
                                Log.d(TAG,"Event was successfully replaced!");
                            } else{
                                if(database_event.getUpdated().getValue() > google_event.getUpdated().getValue()){
                                    //database event was modified later
                                    Log.d(TAG,"This event: "+database_event.toString()+"\n must replace google event: \n"+google_event.toString());
                                    futureGoogleCalendarEvents.add(database_event);
                                }else{
                                    //google event was modified later
                                    //replacing events in db
                                    database.eventDao().delete(database_event);
                                    database.eventDao().insert(google_event);
                                    Log.d(TAG,"Event was successfully replaced!");
                                }
                            }
                        }
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                Log.d(TAG, "Inserting this Event in Room DB: ");
                Log.d(TAG, google_event.toString());
                eventsToInsert.add(google_event);
            }
        }
        if(eventsToInsert != null && !eventsToInsert.isEmpty()) {
            database.eventDao().insertAll(eventsToInsert);
        }
        if(futureGoogleCalendarEvents != null && !futureGoogleCalendarEvents.isEmpty()){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            com.google.api.services.calendar.Calendar mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("AGH Navi")
                    .build();
            for(EventSerializable event : futureGoogleCalendarEvents){
                try {
                    Log.d(TAG,"Modifying event: "+event.toString()+" in Google Calendar");
                    Event google_event = event.toGoogleEvent();
                    mService.events().update("primary", google_event.getId(), google_event).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static List<EventSerializable> getAll(EventsDatabase database){
        List<EventSerializable> events =  database.eventDao().getAll();

        for(EventSerializable e : events){
            Log.d("getAllInDBConnector","This is event in room db: ");
            Log.d(TAG,e.toString());
        }
        return events;
    }

    private static void edit(EventsDatabase database,EventSerializable e1, EventSerializable e2){
        database.eventDao().delete(e1);
        database.eventDao().insert(e2);
    }

    private static void remove(EventsDatabase database,EventSerializable e){
        database.eventDao().delete(e);
    }


    private static class DoAsync extends AsyncTask<Void, Void, Void> {

        private final EventsDatabase mDb;
        private EventSerializable e;
        private EventSerializable e1;
        private List<EventSerializable> list;
        private String t;
        private GoogleAccountCredential credential;
        private Context ctx;

        public EventSerializable getE() { return e; }

        public void setE(EventSerializable e) { this.e = e; }

        public List<EventSerializable> getList() { return list; }

        public void setList(List<EventSerializable> list) { this.list = list; }

        public EventSerializable getE1() { return e1; }

        public void setE1(EventSerializable e1) { this.e1 = e1; }

        public String getT() {  return t; }

        public void setT(String t) { this.t = t; }

        public void setCredential(GoogleAccountCredential mCredential){ this.credential = mCredential; }

        DoAsync(Context context, String task) {
            mDb = EventsDatabase.getAppDatabase(context);
            t = task;
            e=null;
            e1=null;
            list=null;
            ctx = context;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            switch(t){
                case "ADD":
                    if(e != null) {
                        addEvent(mDb,e);
                    }
                    break;
                case "ADD_ALL":
                    if(list != null) {
                        addEvents(mDb, list,credential);
                    }
                    break;
                case "GET_ALL":
                    getAll(mDb);
                    break;
                case "EDIT":
                    if(e!=null && e1!=null) {
                        edit(mDb,e,e1);
                    }
                    break;
                case "REMOVE":
                    if(e!=null){
                        remove(mDb,e);
                    }
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            switch(t){
                case "ADD":
                        Toast.makeText(this.ctx, "Wydarzenie zostało dodane", Toast.LENGTH_SHORT).show();
                    break;
                case "ADD_ALL":
                        Toast.makeText(this.ctx, "Wydarzenia zostały dodane", Toast.LENGTH_SHORT).show();
                    break;
                case "GET_ALL":
                    //Toast.makeText(this.ctx, "Wydarzenie zostały pobra", Toast.LENGTH_SHORT).show();
                    break;
                case "EDIT":
                    Toast.makeText(this.ctx, "Wydarzenie zostało zmienione", Toast.LENGTH_SHORT).show();
                    break;
                case "REMOVE":
                    Toast.makeText(this.ctx, "Wydarzenie zostało usunięte", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

}
