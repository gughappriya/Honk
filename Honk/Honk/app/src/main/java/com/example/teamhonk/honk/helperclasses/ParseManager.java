package com.example.teamhonk.honk.helperclasses;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamhonk.honk.R;
import com.example.teamhonk.honk.contracts.IDatabaseManager;
import com.example.teamhonk.honk.controllers.EtaActivity;
import com.example.teamhonk.honk.controllers.MyTripsActivity;
import com.example.teamhonk.honk.controllers.ViewTripActivity;
import com.example.teamhonk.honk.models.Trip;
import com.example.teamhonk.honk.models.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Gughappriya Gnanasekar on 10/28/2015.
 *
 */

public class ParseManager implements IDatabaseManager {

    private ParseObject parseObj = null;
    private static final String TAG ="ParseManager";
    private static final String CLIENT_KEY = "pZ2EuSMERDplVwlxJsYsfsmZJnwPS6imZHhuxCe1";
    private static final String APP_ID = "wwilda8N71g5zZg00zDoXvSAwXs3giCHE3Rtgv2O";
    public ParseManager(){
        parseObj=null;
    }


    public void updateUserLocation(Location loc){
       ParseUser user =  ParseUser.getCurrentUser();
        if(user!= null) {
            user.put("position", new ParseGeoPoint(loc.getLatitude(), loc.getLongitude()));
            user.saveInBackground();
        }
    }

    @Override
    public void saveTrip(Trip trip){
        final ParseObject newTrip = new ParseObject("Trip");
        //newTrip.put("Id", trip.getId());
        newTrip.put("Name", trip.getName());
        newTrip.put("Location", trip.getLocation());
        newTrip.put("LocationName",trip.getLocationName());
        newTrip.put("PlannedDate", trip.getPlannedDate());
        newTrip.put("Creator", ParseUser.getCurrentUser());
        final Trip tripToSend = trip;
        // This will save trip
        newTrip.saveInBackground(new SaveCallback() {
            //on call back of a save trip the user trip records are created
            public void done(ParseException e) {
                if (e == null) {
                    // Trip saved successfully.
                    Log.d(TAG, "Trip saved!");
                    saveAllUserTrips(newTrip,tripToSend);
                } else {
                    //Trip save failed
                    Log.d(TAG, e.getMessage());
                }
            }
        });

    }

    public void saveAllUserTrips(final ParseObject createdTrip,final Trip trip){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> users, ParseException e) {
                HashMap<String, String> userid_email = new HashMap<String, String>();
                if (e == null && users != null && trip.getFriends() != null) {
                    for (int i = 0; i < users.size(); i++) {
                        if (trip.getFriends().get(users.get(i).getString("email")) != null) {
                            userid_email.put(users.get(i).getObjectId(), trip.getFriends().get(users.get(i).getString("email")));
                        }

                    }
                    ArrayList<ParseObject> userTripsList = new ArrayList<ParseObject>();
                    ParseObject userTrip = new ParseObject("UserTrip");
                    //UserTrip table stores trip object in tripId column
                    userTrip.put("UserId", ParseUser.getCurrentUser().getObjectId());
                    userTrip.put("TripId", createdTrip);
                    userTrip.put("nameOfUser", ParseUser.getCurrentUser().getString("name"));
                    userTripsList.add(userTrip);
                    //These contacts selected by user are already registered with HONK so
                    // Save usertrip with the userid of the respective person
                    for (String key : userid_email.keySet()
                            ) {
                        userTrip = new ParseObject("UserTrip");
                        userTrip.put("UserId", key);
                        userTrip.put("TripId", createdTrip);
                        userTrip.put("nameOfUser", userid_email.get(key));
                        userTripsList.add(userTrip);
                    }
                    for (String key : trip.getFriends().keySet()
                            ) {
                        //It means the contacts the user selected are either not registered as users of honk
                        //Or the contact doesnt have email address attached to it .
                        if (!userid_email.containsValue(trip.getFriends().get(key))) {
                            userTrip = new ParseObject("UserTrip");
                            userTrip.put("TripId", createdTrip);
                            userTrip.put("nameOfUser", trip.getFriends().get(key));
                            userTripsList.add(userTrip);
                        }
                    }


                    //Saving user trip
                    userTrip.saveAllInBackground(userTripsList);
                } else {
                    //USerTrip save failed
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    /*
    GET MY TRIP DETAILS
     */

    public ArrayList<HashMap<String, String>> getMyTripDetails(String currentUserId,final ListView lv,final Context c){
        //This method returns Arraylist<HashMap<"tripid","tripname">> of all trips
        //corresponding to the specific user
        //INPUT: Userid
        //OUTPUT: Arraylist<HashMap<"tripid","tripname">> of all trips
        Log.d(TAG, "Inside getMyTripDetails " + currentUserId);
        final ArrayList<HashMap<String,String>> myTripList=new ArrayList<HashMap<String,String>>();

        //Parse Query
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserTrip");
        query.whereEqualTo("UserId", currentUserId.trim());
        query.include("TripId");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> tripIdList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "getMyTripDetails Retrieved" + tripIdList.size() + "trips");
                    for (int i = 0; i < tripIdList.size(); i++) {
                        ParseObject trip = (ParseObject)tripIdList.get(i).get("TripId");

                        HashMap<String, String> tempHash = new HashMap<String, String>();
                        tempHash.put("id", trip.getObjectId());
                        tempHash.put("name", trip.getString("Name"));
                        myTripList.add(tempHash);
                    }
                    if(myTripList!= null) {
                        ListAdapter adapter = new SimpleAdapter(c,
                                myTripList,
                                R.layout.activity_viewtripentry,
                                new String[]{"id", "name"},
                                new int[]{R.id.tripIdText, R.id.tripNameText});
                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                               TextView trip_id = (TextView) view.findViewById(R.id.tripIdText);
                                Trip trip =new Trip( );
                                trip.setId(trip_id.getText().toString());
                                Intent viewThisTripIntent = new Intent(c, EtaActivity.class);
                                viewThisTripIntent.putExtra("tripId",trip);
                                c.startActivity(viewThisTripIntent);
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "getResultTrips Error: " + e.getMessage());
                }
            }
        });
        return myTripList;
    }

    /*
    * This method is used to getTrip details with tripid.
    * WRITE THIS LATER.
    * This returns destination,date and time.
     */
    public Trip getTripDetails(String tripId){
        /*
        This method should return a Trip object by querying the Trip table
        and pulling the trip which matches the 'tripId'
         */

        Log.d(TAG,"getTripDetails");

        final Trip resultTrip=new Trip(null,null,null,null);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Trip");
        query.whereEqualTo("objectId", tripId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> tripList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "getTripDetails Retrieved" + tripList.size() + "trips");
                    if (tripList.size() != 0) {
                        resultTrip.setName(tripList.get(0).getString("Name"));
                        resultTrip.setLocation(tripList.get(0).getParseGeoPoint("Location"));
                        resultTrip.setPlannedDate(tripList.get(0).getDate("PlannedDate"));
                        resultTrip.setCreator((ParseUser) tripList.get(0).get("Creator"));
                    }
                } else {
                    Log.d(TAG, "getResultTrips Error: " + e.getMessage());
                }
            }
        });
        return resultTrip;
    }

    public String getCurrentUserId(){
        Log.d(TAG, "getCurrentUserId");
        return ParseUser.getCurrentUser().getObjectId();
    }

    public void updateCurrentUserLocation(final ParseGeoPoint newLocation, String currentUserId){
        Log.d(TAG,"updateCurrentUserLocation");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        // Retrieve the object by id
        query.getInBackground(currentUserId, new GetCallback<ParseObject>() {
            public void done(ParseObject userObject, ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case, only cheatMode and score
                    // will get sent to the Parse Cloud. playerName hasn't changed.
                    userObject.put("position", newLocation);
                    userObject.saveInBackground();
                }
            }
        });
    }


    public User getUserDetails(String userId){
        /*
        This method should return a User object by querying the User table
        and pulling the User which matches the 'userId'
         */
        Log.d(TAG, "getUserDetails");
        final User userDetails=new User(null);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> userList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "getUserDetails Retrieved" + userList.size() + "trips");
                    if (userList.size() != 0) {
                        userDetails.setName(userList.get(0).getString("username"));
                        userDetails.setPosition(userList.get(0).getParseGeoPoint("position"));
                    }
                } else {
                    Log.d(TAG, "getResultTrips Error: " + e.getMessage());
                }
            }
        });
        return userDetails;
    }


    public ArrayList<String> getPersonIdsOfParticularTrip(String tripId){
        /*
        This method should query the User Trip table and using cursor
        parse through all the returned results and make a string of
        personIds from the results
        like ["uid1","uid2",.....]
        */
        /*
        DOUBT:
        In the UserTrip table, is the 'UserId' field a User object or a String.
         */

        //To get the Trip object to query in getPersonIdsOfParticularTrip()

        //Trip queryTrip=getTripDetails(tripId);
        Log.d(TAG,"getPersonIdsOfParticularTrip");
        final ArrayList<String> personIdList=null;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserTrip");
        query.whereEqualTo("TripId", tripId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> userIdList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "getPersonIds Retrieved" + userIdList.size() + "trips");
                    if (userIdList.size() != 0) {
                        for (int i = 0; i < userIdList.size(); i++) {
                            personIdList.add(userIdList.get(i).getString("UserId"));
                        }
                    }
                } else {
                    Log.d(TAG, "getPersonIds Error: " + e.getMessage());
                }
            }
        });
        return personIdList;
    }

    public HashMap<String,String> pullUserDetails(String userId,ParseGeoPoint location){
        //Recieve a personId
        //Return an Arraylist of hashmaps <"User name","eta,distance">
        //Need to call GoogleDistanceMatrixRequest to get eta and distance and make them into a string

        //Now we have the user details

        Log.d(TAG,"pullUserDetails");
        User userDetails= getUserDetails(userId);


        String destinationParamter= String.valueOf(location.getLatitude()).trim()
                +","+String.valueOf(location.getLongitude()).trim();
        String originParameter= String.valueOf(userDetails.getPosition().getLatitude()).trim()
                +","+String.valueOf(userDetails.getPosition().getLongitude()).trim();

        //ArrayList to be passed as parameter to GoogleDistanceMatrixRequestTask
        ArrayList<String> parameterArray=new ArrayList<String>();
        parameterArray.add(originParameter);
        parameterArray.add(destinationParamter);
        parameterArray.add("now");

        //Call GoogleDistancleMatrixRequestTask to get eta and distance
        GoogleDistanceMatrixRequestTask etaTask=new GoogleDistanceMatrixRequestTask();
        ArrayList<String> etaParameters=null;
        try {
            etaParameters= etaTask.execute(parameterArray).get();
        } catch (InterruptedException e) {
            Log.d(TAG,e.getMessage());
        } catch (ExecutionException e) {
            Log.d(TAG, e.getMessage());
        }
        HashMap<String,String> resultHashMap=new HashMap<String, String>();
        resultHashMap.put(userDetails.getName(), etaParameters.get(0) + "," + etaParameters.get(1));
        return resultHashMap;
    }

    @Override
    public ArrayList<HashMap<String, String>> getMyTripDetailsForETA(String currentUserId, ListView lv, Context c) {
        return null;
    }

    public ArrayList<User> getUsers(){
       final  ArrayList<User> userList = new ArrayList<User>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> users, ParseException e) {
                if (e == null) {
                    if (users !=null ){
                        Log.d(TAG, "get users returned: " + users.size() + "users");

                        for (int i=0;i<users.size();i++){
                           userList.add(new User(users.get(i).getString("objectid")));
                        }
                    }
                } else {
                    Log.d(TAG, "getPersonIds Error: " + e.getMessage());
                }
            }
        });
        return userList;
    }

    //#####################################JUST FOR ETA- BY ARAVIND#####################################
    //##################################################################################################

    /*
    GET MY TRIP DETAILS
     */
    public ArrayList<HashMap<String, String>> getMyTripDetailsforETA(String currentUserId,final ListView lv,final Context c){
        //INPUT: Userid
        //OUTPUT: Arraylist<HashMap<"tripid","tripname">> of all trips

        Log.d(TAG, "Inside getMyTripDetails " + currentUserId);
        final ArrayList<HashMap<String,String>> myTripList=new ArrayList<HashMap<String,String>>();
        List<ParseObject> parseResult=null;

        //Parse Query
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserTrip");
        query.whereEqualTo("UserId", currentUserId.trim());

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> tripIdList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "getMyTripDetails Retrieved" + tripIdList.size() + "trips");
                    for (int i = 0; i < tripIdList.size(); i++) {
                        String tripId = tripIdList.get(i).getString("TripId");
                        //Trip trip=getTripDetails(tripId);
                        //String tripName = trip.getLocationName();
                        HashMap<String, String> tempHash = new HashMap<String, String>();
                        //tempHash.put(tripId, tripName);
                        myTripList.add(tempHash);
                    }
                    Log.d(TAG, "myTripList " + myTripList);
                    if (myTripList != null) {
                        ListAdapter adapter = new SimpleAdapter(c,
                                myTripList,
                                R.layout.activity_viewtripentry,
                                new String[]{"id", "destination"},
                                new int[]{R.id.tripIdText, R.id.tripNameText});
                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                TextView trip_id = (TextView) view.findViewById(R.id.tripIdText);
                                String tripid = trip_id.getText().toString().trim();
                                Intent viewThisTripIntent = new Intent(c, ViewTripActivity.class);
                                viewThisTripIntent.putExtra("tripid", Integer.parseInt(tripid));
                                c.startActivity(viewThisTripIntent);
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "getResultTrips Error: " + e.getMessage());
                }
            }
        });
        return myTripList;
    }



    //<<<<<<<<<<<<<<< UPDATE CURRENT USER LOCATION >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
    public void updateCurrentUserLocationForETA(final ParseGeoPoint newLocation, String currentUserId) {
        Log.d(TAG, "updateCurrentUserLocation");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        // Retrieve the object by id
        query.getInBackground(currentUserId, new GetCallback<ParseObject>() {
            public void done(ParseObject result, ParseException e) {
                if (e == null) {
                    result.put("position", newLocation);
                    Log.d(TAG, "curUsrLoc " +newLocation+" saved");
                    result.saveInBackground();
                }else{
                    Log.d(TAG, "updCurUserError" + e.getMessage());
                }
            }
        });
        return;
    }

    //<<<<<<<<<<<<<<< GET TRIP DETAILS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
    public ParseObject getTripDetailsForETA(String tripId){
        /*
        INPUT: tripId
        OUTPUT: ParseObject of the matching trip.
         */
        Log.d(TAG, "getTripDetails");
        ParseObject resultTrip=null;
        List<ParseObject> parseResult=null;


        ParseQuery<ParseObject> query= new ParseQuery<ParseObject>("Trip");
        query.whereEqualTo("objectId",tripId);
        try{
            parseResult= query.find();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        Log.d(TAG, "ParseResult" + parseResult.size());
        for (ParseObject trip: parseResult){

            resultTrip=trip;
        }
        return resultTrip;
    }

    //<<<<<<<<<<<<<<< GET USER DETAILS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
    public ParseObject getUserDetailsForETA(String userId){
       /*
        INPUT: userId
        OUTPUT: ParseObject containing user details
         */
        Log.d(TAG, "getUserDetails of "+userId);
        ParseObject resultUser=null;
        List<ParseObject> parseResult=null;

        ParseQuery<ParseObject> query= new ParseQuery<ParseObject>("_User");
        query.whereEqualTo("objectId",userId.trim());
        try{
            parseResult= query.find();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        Log.d(TAG, "getUserDetails size " + parseResult.size());
        for (ParseObject user: parseResult){
            resultUser=user;
        }
        return resultUser;
    }

    //<<<<<<<<<<<<<<<<<<<<<<<<<<< PULL USER DETAILS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public HashMap<String,String> pullUserDetailsForETA(String userId,ParseGeoPoint location){
        //Recieve a personId
        //Return an Arraylist of hashmaps <"User name","eta,distance">

        Log.d(TAG, "pullUserDetails " + userId);
        ParseObject userDetails= getUserDetailsForETA(userId);

        String destinationParamter= String.valueOf(location.getLatitude()).trim()
                +","+String.valueOf(location.getLongitude()).trim();
        String originParameter= String.valueOf(userDetails.getParseGeoPoint("position").getLatitude()).trim()
                +","+String.valueOf(userDetails.getParseGeoPoint("position").getLongitude()).trim();

        //ArrayList to be passed as parameter to GoogleDistanceMatrixRequestTask
        ArrayList<String> parameterArray=new ArrayList<String>();

        //Call GoogleDistancleMatrixRequestTask to get eta and distance
        ArrayList<String> etaParameters=null;
        try {
            parameterArray.add(originParameter.trim());
            parameterArray.add(destinationParamter.trim());
            parameterArray.add("now");
            Log.d(TAG,"parameterArray "+parameterArray);

            /*
            Till here, everything went as planned, but the GoogleDistanceMatrix is not
            returning the data on time. I feel there is conflict between different threads which I
            am finding it difficult to understand. Can you please check this out?

            Just send any intent to EtaAvctivity on button click with an extra value 'tripId' containing the
             tripId value to check it.
             */

            GoogleDistanceMatrixRequestTask etaTask=new GoogleDistanceMatrixRequestTask();

            etaParameters= etaTask.execute(parameterArray).get();
            Log.d(TAG,"etaparams "+etaParameters);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.d(TAG, e.getMessage());
        }

        HashMap<String,String> resultHashMap=new HashMap<String, String>();
        resultHashMap.put(userDetails.getString("username"), etaParameters.get(0) + "," + etaParameters.get(1));
        Log.d(TAG, "resultHash " + resultHashMap);
        return resultHashMap;
    }

    //<<<<<<<<<<<<<<< GET PERSON IDS OF PARTICULAR TRIP >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
    public ArrayList<String> getPersonIdsOfParticularTripForETA(ParseObject tripObject){
        /*
        INPUT: ParseObject tripObject
        OUTPUT: Arraylist ["uid1","uid2",.....] of matching trip.
        */

        Log.d(TAG, "getPersonIdsOfParticularTrip");
        ArrayList<String> personIdList=new ArrayList<String>();
        List<ParseObject> parseResult=null;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserTrip");
        query.whereEqualTo("TripId", tripObject);
        try{
            parseResult= query.find();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        Log.d(TAG, "getPersonIdsOfTrip size" + parseResult.size());
        for (ParseObject result: parseResult){
            if(result.getString("UserId")!= null) {
                Log.d(TAG, "resuly.getString()" + result.getString("UserId"));
                personIdList.add(result.getString("UserId"));
            }
        }
        Log.d(TAG, "PersonIdList " + personIdList.toString());
        return personIdList;
    }

    @Override
    public void initialize(Context context) {
        Log.d(TAG,"initialize");
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, APP_ID, CLIENT_KEY);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
