package com.example.teamhonk.honk.helperclasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gughappriya Gnanasekar on 11/19/2015.
 */
public class ConversionHelper {
   public  ConversionHelper(){

    }
    public Calendar ConvertStringToCalendar(String datetime,String dateTimeFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);

        try {
            Date d = dateFormat.parse(datetime);
            cal.setTime(d);

        } catch (ParseException e) {
            //TODO: Should implement exception handling
            e.printStackTrace();
        }
        return cal;
    }
}
