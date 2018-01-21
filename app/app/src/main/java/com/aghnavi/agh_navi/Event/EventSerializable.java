package com.aghnavi.agh_navi.Event;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aghnavi.agh_navi.DateUtils.DateUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Entity(tableName ="events")
public class EventSerializable implements Serializable {

    private static final String ALL_DAY_EVENT = "Wydarzenie całodniowe";
    //region Fields
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "event_id")
    private String id;
    @ColumnInfo(name = "google_event_id")
    private String googleEventId;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "date_start")
    private Date dateStart;
    @ColumnInfo(name = "dateTime_start")
    private DateTime dateTimeStart;
    @ColumnInfo(name = "date_end")
    private Date dateEnd;
    @ColumnInfo(name = "dateTime_end")
    private DateTime dateTimeEnd;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "recurrence")
    private String recurrence;
    @ColumnInfo(name = "created")
    private DateTime created;
    @ColumnInfo(name = "updated")
    private DateTime updated;
    //endregion

    //region Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
    }

    public DateTime getDateTimeStart() {
        return dateTimeStart;
    }

    public void setDateTimeStart(DateTime dateTimeStart) {
        this.dateTimeStart = dateTimeStart;
    }

    public DateTime getDateTimeEnd() {
        return dateTimeEnd;
    }

    public void setDateTimeEnd(DateTime dateTimeEnd) {
        this.dateTimeEnd = dateTimeEnd;
    }

    public String getRecurrence() { return recurrence; }

    public void setRecurrence(String recurrence) { this.recurrence = recurrence;  }

    public DateTime getCreated() { return created; }

    public void setCreated(DateTime created) { this.created = created; }

    public DateTime getUpdated() {return updated; }

    public void setUpdated(DateTime updated) { this.updated = updated; }

    //endregion


    public EventSerializable(){
        this.title = null;
        this.description = null;
        this.location = null;
        this.dateStart = null;
        this.dateTimeStart = null;
        this.dateTimeEnd = null;
        this.dateEnd = null;
        this.type = null;
        this.id = null;
        this.googleEventId = null;
        this.created = null;
        this.updated = null;
    }
    //region Constructor
    public EventSerializable(String title,String description, Date start, DateTime dateTimeStart, Date end, DateTime dateTimeEnd, String location, String type){
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateStart = start;
        this.dateTimeStart = dateTimeStart;
        this.dateTimeEnd = dateTimeEnd;
        this.dateEnd = end;
        this.type = type;
        this.id = createId();
        this.googleEventId = null;
        this.created = DateUtils.createCurrentDateTime();
        this.updated = DateUtils.createCurrentDateTime();
    }

    public EventSerializable(Event event){
        /*String type = null;
        if(event.getGadget() != null){
            type = event.getGadget().getType();
        }*/

        Date dateStart, dateEnd;
        dateStart = dateEnd = null;

        if(event.getStart().getDate() != null){
            dateStart = DateUtils.dateTimeToDate(event.getStart().getDate());
        }

        if(event.getEnd().getDate() != null){
            dateStart = DateUtils.dateTimeToDate(event.getEnd().getDate());
        }

        String recurrence = null;
        if(event.getRecurrence() != null){
            recurrence = DateUtils.rRuleToType(event.getRecurrence().toString());
        }
        DateTime created,updated;
        created = updated = DateUtils.createCurrentDateTime();
        if(event.getCreated() != null){
            created = event.getCreated();
        }
        if(event.getUpdated() != null){
            updated = event.getUpdated();
        }

        String description,type,id;
        description = type = id ="";
        if(event.getDescription()!=null) {
            String[] descriptionList = event.getDescription().split("/#/");
            if(descriptionList.length > 2) {
                if (descriptionList[0] != null) {
                    description = descriptionList[0];
                    if (descriptionList[1] != null) {
                        id = descriptionList[1];
                        if (descriptionList[2] != null) {
                            type = descriptionList[2];
                        }
                    }
                }
            }
        }

        this.id = createId();
        this.title = event.getSummary();
        this.description = description;
        this.dateStart = dateStart;
        this.dateTimeStart = event.getStart().getDateTime();
        this.dateEnd = dateEnd;
        this.dateTimeEnd = event.getEnd().getDateTime();
        this.location = event.getLocation();
        this.type = type;
        this.googleEventId = event.getId();
        this.recurrence = recurrence;
        this.created = created;
        this.updated = updated;
    }
    //endregion



    private String createId(){
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId;
    }

    public String toString(){
        String string = new String();
        string = string.concat("Title: "+this.getTitle() + "\n");
        string = string.concat("Description: "+this.getDescription() + "\n");
        string = string.concat("Location: "+this.getLocation() + "\n");
        if(this.getDateTimeStart() != null) {
            string = string.concat("StartDateTime: " + DateUtils.dateTimeToString(this.getDateTimeStart()) + "\n");
            string = string.concat("StartDateTime - hours: " + DateUtils.dateTimeToHours(this.getDateTimeStart()) + "\n");
        }
        if(this.getDateTimeEnd() != null) {
            string = string.concat("EndDateTime: " + DateUtils.dateTimeToString(this.getDateTimeEnd()) + "\n");
            string = string.concat("EndDateTime - hours: " + DateUtils.dateTimeToHours(this.getDateTimeEnd()) + "\n");
        }
        if(this.getDateStart() != null) {
            string = string.concat("StartDate: " + DateUtils.dateToString(this.getDateStart()) + "\n");
        }
        if(this.getDateEnd() != null) {
            string = string.concat("EndDate: " + DateUtils.dateToString(this.getDateEnd()) + "\n");
        }
        if(this.getRecurrence() != null){
            string = string.concat("Recurrence: "+this.getRecurrence()+"\n");
        }
        string = string.concat("Id: "+this.getId() + "\n");
        string = string.concat("Created: "+DateUtils.dateTimeToString(this.getCreated())+"\n");
        string = string.concat("Updated: "+DateUtils.dateTimeToString(this.getUpdated())+"\n");
        return string;
    }

    public String getStringStartDate(){
        if(this.dateStart != null){
            return DateUtils.dateToString(this.dateStart);
        }

        if(this.dateTimeStart != null){
            return DateUtils.dateTimeToString(this.dateTimeStart);
        }
        return "Error - both dates are null";
    }

    public String getStringEndDate(){
        if(this.dateEnd != null){
            return DateUtils.dateToString(this.dateEnd);
        }

        if(this.dateTimeEnd != null){
            return DateUtils.dateTimeToString(this.dateTimeEnd);
        }
        Log.i("EventSerializable","Event has no end");
        return "";
    }

    public String getStartHours(){
        if(this.dateStart != null){
            return ALL_DAY_EVENT;
        }

        return DateUtils.dateTimeToHours(this.dateTimeStart);
    }

    public String getEndHours(){
        if(this.dateStart != null){
            return ALL_DAY_EVENT;
        }

        return DateUtils.dateTimeToHours(this.dateTimeEnd);
    }

    public String getDurationString(){
        String start = this.getStartHours();
        if(start.equals(ALL_DAY_EVENT)) {
            return start;
        }
        //if event last longer than a day
        if(DateUtils.getDayFromDateTime(this.getDateTimeStart()) != DateUtils.getDayFromDateTime(this.getDateTimeEnd())){
            return ALL_DAY_EVENT +" (od "+DateUtils.dateTimeToString(this.getDateTimeStart())+" do "+DateUtils.dateTimeToString(this.getDateTimeEnd())+")";
        }
        String end = this.getEndHours();
        return start +" - "+end;
    }

    public boolean isEmpty(){
        if(this.getId() == null && this.getTitle() == null && this.getDescription() == null){
            return true;
        }
        return false;
    }

    //based on fields not id
    public boolean hasSameFieldsValues(Object e2) throws InvocationTargetException, IllegalAccessException {
        Object e1 = this;
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                    && method.getParameterTypes().length == 0
                    && method.getReturnType() != void.class
                    && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                    && !method.getName().equals("getId")
                    && !method.getName().equals("getCreated")
                    && !method.getName().equals("getUpdated")) {
                //Log.d("hasSameFieldsValues","Method name: "+method.getName().toString());
                Object value1 = method.invoke(e1);
                Object value2 = method.invoke(e2);
                if (value1 != null && value2 != null) {
                    //Log.d("hasSameFieldsValues","First: "+method.getName() + "=" + value1.toString());
                    //Log.d("hasSameFieldsValues","Second: "+method.getName() + "=" + value2.toString());
                    if(!value1.equals(value2)){
                        Log.d("eventsAreEqual","Events are not equal!");
                        return false;
                    }

                }

            }
        }
        Log.d("eventsAreEqual","Events are equal!");
        return true;
    }

    public Event toGoogleEvent(){
        String title,location,description;
        if(this.getTitle() != null){
            title = this.getTitle();
        }else{
            title = "Wydarzenie bez nazwy";
        }

        if(this.getLocation() != null){
            location = this.getLocation();
        }else{
            location = "";
        }

        if(this.getDescription() != null){
            description = this.getDescription();
        }else{
            description = "";
        }

        Event event = new Event()
                .setSummary(title)
                .setLocation(location)
                .setDescription(description);

        EventDateTime start = new EventDateTime()
                .setDateTime(this.getDateTimeStart());
                //.setTimeZone("Europe/Warsaw");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(this.getDateTimeEnd());
                //.setTimeZone("Europe/Warsaw");
        event.setEnd(end);

        description = description.concat("\n/#/"+this.getId()+"\n/#/"+this.getType());
        event.setDescription(description);
        /*if(event.getGadget() == null){
            event.setGadget(new Event.Gadget());
        }
        event.getGadget().setTitle(this.getId());
        event.getGadget().setType(this.getType());*/

        if(this.getRecurrence()!= null) {
            String[] recurrence = new String[]{DateUtils.typeToRRule(this.getRecurrence())};
            event.setRecurrence(Arrays.asList(recurrence));
        }
        if(this.getGoogleEventId() != null) {
            event.setId(this.getGoogleEventId());
        }
        return event;
    }
}
