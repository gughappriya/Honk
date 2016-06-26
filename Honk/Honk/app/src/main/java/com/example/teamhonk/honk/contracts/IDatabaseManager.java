package com.example.teamhonk.honk.contracts;

import android.content.Context;
import android.location.Location;
import android.widget.ListView;

import com.example.teamhonk.honk.models.Trip;
import com.example.teamhonk.honk.models.User;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gughappriya Gnanasekar on 10/28/2015.
 */
public interface IDatabaseManager {
    void initialize(Context context);
    void saveTrip( Trip t);
    ArrayList<HashMap<String, String>> getMyTripDetails(String currentUserId,final ListView lv,final Context c );
    Trip getTripDetails(String tripId);
    String getCurrentUserId();
    User getUserDetails(String userId);
    ArrayList<String> getPersonIdsOfParticularTrip(String tripId);
    HashMap<String,String> pullUserDetails(String userId,ParseGeoPoint location);
    void updateUserLocation(Location loc);

    //##############################Just for ETA################################################
    ArrayList<HashMap<String, String>> getMyTripDetailsForETA(String currentUserId,final ListView lv,final Context c );
    ParseObject getTripDetailsForETA(String tripId);
    ParseObject getUserDetailsForETA(String userId);
    HashMap<String,String> pullUserDetailsForETA(String userId,ParseGeoPoint location);
    ArrayList<String> getPersonIdsOfParticularTripForETA(ParseObject tripObject);
}
