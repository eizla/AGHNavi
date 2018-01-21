package com.aghnavi.agh_navi.Database;

import android.arch.persistence.room.TypeConverter;

import com.google.api.client.util.DateTime;

import java.util.Date;

/**
 * Created by Scarf_000 on 05.12.2017.
 */

public class TypeTransmogrifier {

    @TypeConverter
    public static Long fromDate(Date date) {
        if (date==null) {
            return(null);
        }

        return(date.getTime());
    }

    @TypeConverter
    public static Date toDate(Long millisSinceEpoch) {
        if (millisSinceEpoch==null) {
            return(null);
        }

        return(new Date(millisSinceEpoch));
    }

    @TypeConverter
    public static Long fromDateTime(DateTime date) {
        if (date==null) {
            return(null);
        }

        return(date.getValue());
    }

    @TypeConverter
    public static DateTime toDateTime(Long millisSinceEpoch) {
        if (millisSinceEpoch==null) {
            return(null);
        }
        DateTime dateTime = new DateTime(millisSinceEpoch, 60);

        return(dateTime);
    }
}
