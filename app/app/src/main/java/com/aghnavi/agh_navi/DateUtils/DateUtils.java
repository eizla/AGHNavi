package com.aghnavi.agh_navi.DateUtils;

import android.util.Log;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Scarf_000 on 09.11.2017.
 */

public class DateUtils {
    public static DateTime createCurrentDateTime(){
        TimeZone tz = TimeZone.getTimeZone("Europe/Warsaw");
        return new DateTime(new Date(System.currentTimeMillis()),tz);
    }
    public static DateTime stringToDateTime(String date, String time){
        DateTime dateTime;
        java.util.Date new_date = null;
        java.util.Date new_time = null;
        java.util.Date final_date = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Log.d("stringToDateTime","Date: "+date+"\nTime: "+time+"\n");
        try {
            //setting date
            new_date = dateFormat.parse(date+" "+time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        TimeZone tz = TimeZone.getTimeZone("Europe/Warsaw");
        dateTime = new DateTime(new_date,tz);

        return dateTime;
    }

    public static Date dateTimeToDate(DateTime dateTime){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.parse(dateTime.toString());

        }catch(ParseException e){
            //Log.e("DateUtils","ParseException");
            e.printStackTrace();
        }
        return date;
    }

    public static Boolean sameDay(DateTime d1, DateTime d2){
        if(DateUtils.dateTimeToString(d1).equals(DateUtils.dateTimeToString(d2))){
            return true;
        }
        /*try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            date1 = dateFormat.parse(d1.toString());
            date2 = dateFormat.parse(d1.toString());

            if(date1.getYear() == date2.getYear()
                    && date1.getMonth() == date2.getMonth()
                    && date1.getDate() == date2.getDate()){
                return true;
            }
        }catch(ParseException e){
            Log.e("DateUtils","ParseException");
            return false;
        }*/
        return false;
    }

    public static boolean sameDay(Date d1, Date d2){
        if(DateUtils.dateToString(d1).equals(DateUtils.dateToString(d2))){
            return true;
        }
        return false;
    }

    public static String dateTimeToString(DateTime dt){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.parse(dt.toString());

            return DateUtils.dateToString(date);
        }catch(ParseException e){
            e.printStackTrace();
        } catch(NullPointerException ne){
            Log.e("DateUtils","NullPointerException");
        }
        return "Error during dateTimeToString conversion";
    }

    public static String dateToString(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);


        String passedDateString = day + "-" + month + "-" + year;
        return passedDateString;
    }

    public static Date stringToDate(String date, String time) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date newDate = null;
        //setting date
        newDate = dateFormat.parse(date+" "+time);
        return newDate;
    }

    public static String hoursToString(Date date){
        Calendar calendar = Calendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int minute = calendar.get(Calendar.MINUTE);
        String curTime = String.format("%02d:%02d", hour, minute);
        return curTime;
    }

    public static String dateTimeToHours(DateTime dt){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            date = dateFormat.parse(dt.toString());

            return DateUtils.hoursToString(date);
        }catch(ParseException e){
            e.printStackTrace();
        } catch(NullPointerException ne){
            Log.e("DateUtils","NullPointerException");
        }
        return "";
    }

    public static int getDayFromDateTime(DateTime dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTimeToDate(dt));
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static int getMonthFromDateTime(DateTime dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTimeToDate(dt));
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    public static int getYearFromDateTime(DateTime dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTimeToDate(dt));
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    public static int getDayFromDate(Date d){
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public static int getMonthFromDate(Date d){
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    public static int getYearFromDate(Date d){
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    public static int getDayOfTheWeekFromDateTime(DateTime dt){
        Calendar c = Calendar.getInstance();
        c.setTime(dateTimeToDate(dt));
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    public static int getDayOfTheWeekFromDate(Date d){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    //is d1 later than d2?
    public static boolean isLater(Date d1, Date d2){
        if(getYearFromDate(d1) > getYearFromDate(d2)){
            return true;
        } else if(getYearFromDate(d1) == getYearFromDate(d2)){
            if(getMonthFromDate(d1) > getMonthFromDate(d2)){
                return  true;
            } else if(getMonthFromDate(d1) == getMonthFromDate(d2)){
                // or equal?
                if(getDayFromDate(d1) > getDayFromDate(d2)){
                    return true;
                }
            }
        }
        return false;

    }

    public static String rRuleToType(String rRule){
        if(rRule.startsWith("RRULE:FREQ=")) {
            if (rRule.startsWith("RRULE:FREQ=DAILY")) {
                return "CODZIENNIE";
            } else if (rRule.startsWith("RRULE:FREQ=WEEKLY")) {
                return "TYGODNIOWO";
            } else if (rRule.startsWith("RRULE:FREQ=MONTHLY")) {
                return "MIESIĘCZNIE";
            } else if (rRule.startsWith("RRULE:FREQ=YEARLY")) {
                return "ROCZNIE";
            }
        }
        return null;

    }

    public static String typeToRRule(String type){
        if (type.equals("CODZIENNIE")) {
            return "RRULE:FREQ=DAILY";
        } else if (type.equals("TYGODNIOWO")) {
            return "RRULE:FREQ=WEEKLY";
        } else if (type.equals("MIESIĘCZNIE")) {
            return "RRULE:FREQ=MONTHLY";
        } else if (type.equals("ROCZNIE")) {
            return "RRULE:FREQ=YEARLY";
        }
        return "";
    }
}
