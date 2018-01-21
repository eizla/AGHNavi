package com.aghnavi.agh_navi.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.Event.EventSerializableDao;

/**
 * Created by Scarf_000 on 05.12.2017.
 */

@Database(entities = {EventSerializable.class}, version = 1, exportSchema = false)
@TypeConverters(TypeTransmogrifier.class)
public abstract class EventsDatabase extends RoomDatabase {

    private static EventsDatabase INSTANCE;

    public abstract EventSerializableDao eventDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };

    /*public static EventsDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), EventsDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }*/

    public static EventsDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    EventsDatabase.class, "events.db")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}