package com.example.teamhonk.honk.models;



import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.vision.barcode.Barcode;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Gughappriya Gnanasekar on 10/29/2015.
 * Stores the information regarding the trip
 */
public class Trip implements Parcelable{

    /**
     * Create a Trip model object from arguments
     *
     * @param name  Add arbitrary number of arguments to
     * instantiate Trip class based on member variables.
     */
    public Trip(String name,String locationName ,Date date,ArrayList<Double> latLong) {

        // TODO - fill in here, please note you must have more arguments here
        this.Name = name;
        this.LocationName =locationName;
        if(latLong != null)
            this.Location= new ParseGeoPoint(latLong.get(0), latLong.get(1));
        else
        //TDO: Should handle null reference exception some other way
            this.Location= new ParseGeoPoint(40.734572,-74.063154);
        this.PlannedDate = date;
    }

    public Trip(){

    }
    public Trip(String id){
        this.Id=id;
    }
    private String Id;
    public String getId(){
        return Id;
    }
    public void setId(String val){
        Id =val;
    }

    private String Name;
    public String getName(){
        return Name;
    }
    public void setName(String val){
        Name = val;
    }


    private String LocationName;
    public String getLocationName(){
        return LocationName;
    }
    public void setLocationName(String val){
        LocationName = val;
    }

    private ParseGeoPoint Location;
    public ParseGeoPoint getLocation(){
        return Location;
    }
    public void setLocation(ParseGeoPoint val){
        Location = val;
    }
    private Date PlannedDate;
    public Date getPlannedDate(){
        return PlannedDate;
    }
    public void setPlannedDate(Date val){
        PlannedDate = val;
    }
    private String PlannedTime;
    public String getPlannedTime(){
        return PlannedTime;
    }
    public void setPlannedTime(String val){
        PlannedTime = val;
    }
    private ParseUser Creator;
    public ParseUser getCreator(){
        return Creator;
    }
    public void setCreator(ParseUser val){
        Creator = val;
    }
    private HashMap<String,String> Friends;
    public HashMap<String,String> getFriends(){
        return Friends;
    }
    public void setFriends(HashMap<String,String> val){
        Friends = val;
    }

    /**
     * Parcelable creator. Do not modify this function.
     */
    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        public Trip createFromParcel(Parcel p) {
            return new Trip(p);
        }

        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    /**
     * Create a Trip model object from a Parcel. This
     * function is called via the Parcelable creator.
     *
     * @param p The Parcel used to populate the
     * Model fields.
     */
    public Trip(Parcel p) {

        // TODO - fill in here
        this.Id=p.readString();
        this.Name = p.readString();
        this.LocationName =p.readString();
        //time =new Date( p.readLong());
//        this.Location =new ParseGeoPoint(p.readDouble(),p.readDouble());
//        DateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
//        try{
//            this.PlannedDate = formatter.parse(p.readString());}
//        catch(java.text.ParseException ex){
//            Log.e("PARSINEXCEPTION", ex.toString());
//        }
//        String[] names = p.readString().split(",");
//        if(names != null) {
//            Friends = new HashMap<String, String>();
//            for (String name : names) {
//                String[] name_email = name.split(":");
//                Friends.put(name_email[0], name_email[1]);
//            }
//        }


    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // TODO - fill in here
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(LocationName);
        if(Location!=null) {
            dest.writeDouble(Location.getLatitude());
            dest.writeDouble(Location.getLongitude());
        }
        if(PlannedDate != null)
            dest.writeString(PlannedDate.toString());

        if(getFriends()!= null) {
            StringBuilder sb = new StringBuilder();
            for (String key : getFriends().keySet()
                    ) {
                sb.append(key + ":" + Friends.get(key) + ",");
            }
            dest.writeString(sb.toString());
        }


    }

    /**
     * Feel free to add additional functions as necessary below.
     */

    /**
     * Do not implement
     */
    @Override
    public int describeContents() {
        // Do not implement!
        return 0;
    }
}
