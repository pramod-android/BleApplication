package com.mywork.bleapplication;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class Constants {
    public static String MY_PREFS_NAME = "spiromedPref";
    public static String DATABASE_NAME = "spiromed_database";


    public static UUID SERVICE_UUID = UUID.fromString("0000bcc0-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_UUID = UUID.fromString("0000bcc1-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("0000bcc3-0000-1000-8000-00805f9b34fb");

    public static final int GPS_REQUEST = 1001;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_FINE_LOCATION = 2;
    public static final long SCAN_PERIOD = 10000;

//    public static final ParcelUuid Service_UUID = ParcelUuid
//            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");
//


    public static String UTCToDate(String dateString) {
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date d = sd.parse(dateString);
            sd = new SimpleDateFormat("dd/MM/yyyy");
            return sd.format(d);
        } catch (ParseException e) {
            Log.i("expUTCToDate", e.toString());
        }
        return "";
    }

    public static String DateToUTC(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date time = new Date();
        try {
            time = dateFormat.parse(date);
        } catch (Exception e) {
            Log.i("expDateToUTC", e.toString());
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFmt.format(time);
    }

    public static long GetDateDifferenceInYears(String strStartDate, String strEndDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = dateFormat.parse(strStartDate);
            endDate = dateFormat.parse(strEndDate);
        } catch (Exception e) {
            Log.i("expGetDateDiffInYears", e.toString());
        }

        long userAge = (endDate.getTime() - startDate.getTime()) / 86400000 / 365;

        return userAge;
    }


    public static long DateToMilliseconds(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();
        return millis;
    }

    public static long GetDateDifferenceInMiliseconds(String strStartDate, String strEndDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = dateFormat.parse(strStartDate);
            endDate = dateFormat.parse(strEndDate);
        } catch (Exception e) {
            Log.i("expGetDateDiffInYears", e.toString());
        }

        long userAge = (endDate.getTime() - startDate.getTime());

        return userAge;
    }
}
