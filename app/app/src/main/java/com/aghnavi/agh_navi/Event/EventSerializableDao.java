package com.aghnavi.agh_navi.Event;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.google.api.client.util.DateTime;

import java.util.List;

/**
 * Created by Scarf_000 on 05.12.2017.
 */

@Dao
public interface EventSerializableDao {
    @Query("SELECT * FROM events")
    List<EventSerializable>  getAll();

    @Query("SELECT * FROM events")
    LiveData< List<EventSerializable> > getAllLiveData();

    @Query("SELECT * FROM events WHERE " +
            "(dateTime_start >= :start AND dateTime_start <= :end) " +
            "OR (dateTime_end >= :start AND dateTime_end <= :end) " +
            "OR (dateTime_start <= :start AND dateTime_end >= :end)" +
            "OR (date_start >= :start AND date_start <= :end) " +
            "OR (date_end >= :start AND date_end <= :end) " +
            "OR (date_start <= :start AND date_end >= :end)")
    LiveData<List<EventSerializable>> getEventsBetweenDates(Long start, Long end);

    @Query("SELECT * FROM events WHERE google_event_id IS NULL")
    LiveData<List<EventSerializable>> getAllOfflineEvents();

    @Query("SELECT * FROM events WHERE event_id==:id")
    LiveData<List<EventSerializable>> getEventById(String id);
    //@Query("SELECT * FROM events WHERE CONVERT(dateTime_start,getDate()) <= :start AND CONVERT(dateTime_end,getDate()) <= :end")
    //List<EventSerializable> getDatesFromPeriod(String start, String end);

    //@Query("SELECT * FROM events WHERE CONVERT(dateTime_start,getDate()) LIKE :dateStart")
    //EventSerializable findByDay(String dateStart);

    @Query("SELECT * FROM events WHERE google_event_id ==:google_id")
    List<EventSerializable> getEventsByGoogleId(String google_id);

    @Query("SELECT dateTime_start FROM events")
    DateTime getDateTimes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventSerializable event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventSerializable> events);

    @Delete
    void delete(EventSerializable event);

    @Query("SELECT * FROM events WHERE recurrence IS NOT NULL")
    LiveData<List<EventSerializable>> getRecurringEvents();
}