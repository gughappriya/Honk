package com.example.teamhonk.honk.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamhonk.honk.R;
import com.example.teamhonk.honk.contracts.IDatabaseManager;
import com.example.teamhonk.honk.helperclasses.ETAUpdateService;
import com.example.teamhonk.honk.helperclasses.ParseManager;
import com.example.teamhonk.honk.models.Trip;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aravind on 11/19/2015.
 */


public class  EtaActivity extends Activity  {
    private static final String TAG = "EtaActivity";
    private static final Integer UPDATE_ETA_INTERVAL_CURRENTUSER = 1000 * 60;//60 SECONDS

    IDatabaseManager db = new ParseManager();

    //Defining UI components
    private TextView destinationView;
    private TextView timeView;
    private ListView etaList;

    //ListAdapter
    private ArrayAdapter<String> listAdapter;

    //Dateformatter
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");


    ArrayList<String> listOfUserIds=new ArrayList<String>();
    ParseGeoPoint destLocation;
    double[] destParams;

    //Progress Dialog
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activetripdetails);

        //Setting the UI resources
        destinationView = (TextView) findViewById(R.id.destinationView);
        timeView = (TextView) findViewById(R.id.timeView);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                etaReceiver, new IntentFilter("etaUpdates"));

        //Extract the trip id on assumption that EtaActivity is called on clicking
        // 'Check ETA' button in ViewTripActivity
        Intent tripIdIntent = getIntent();

        Trip tripObject=tripIdIntent.getParcelableExtra("tripId");
        String tripIdReceived = tripObject.getId();//TripId

        final String currentUserId = ParseUser.getCurrentUser().getObjectId();//My UserId
        Log.d(TAG,"CurrentUserId:"+currentUserId);

        //Returns ParseObject with Trip Details
        ParseObject currentTripDetails = db.getTripDetailsForETA(tripIdReceived);

        //Lets populate the destination,date and time of the trip
        destinationView.setText((String) currentTripDetails.get("LocationName"));
        timeView.setText(dateFormatter.format(currentTripDetails.get("PlannedDate")));

        //Function to return person ids corresponding to the particular received trip id
        //Would be an arraylist ["uid1","uid2",.......]
        listOfUserIds = db.getPersonIdsOfParticularTripForETA(currentTripDetails);
        Log.d(TAG, "listOfUserIds:"+listOfUserIds);

        destLocation = currentTripDetails.getParseGeoPoint("Location");
        destParams=new double[]{destLocation.getLatitude(),destLocation.getLongitude()};
        Log.d(TAG,"destParams:"+destParams.toString());
        //Log.d(TAG,"destLocation:"+destLocation);

        Log.d(TAG,"Ready to start Service");
        Intent serviceIntent=new Intent(getApplicationContext(), ETAUpdateService.class);
        serviceIntent.putExtra("listOfUserIds", listOfUserIds);
        serviceIntent.putExtra("destParams", destParams);
        startService(serviceIntent);

        /*
        //Define the location manager for the particular user------BEGINS!
        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationManager locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                UPDATE_ETA_INTERVAL_CURRENTUSER,
                0,
                new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double newlat= location.getLatitude();
                double newlon= location.getLongitude();
                ParseGeoPoint newLocation=new ParseGeoPoint(newlat,newlon);
                Log.d(TAG, "onLocationChanged " + newLocation);
                new UpdateLocationTask().execute(newLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Do Later!
            }

            @Override
            public void onProviderEnabled(String provider) {
                //Do Later!
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG,"GPS provider has been disabled");
                Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
                intent.putExtra("enabled", true);
                sendBroadcast(intent);
            }
        });
        */

    }


    private BroadcastReceiver etaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Inside Broadcastreceiver");
            ArrayList<String> totalLisOfPersonDetails=new ArrayList<String>();
            totalLisOfPersonDetails= (ArrayList<String>) intent.getSerializableExtra("etaData");
            Log.d(TAG,"broadRecData:"+totalLisOfPersonDetails);

            etaList=(ListView)findViewById(R.id.etaList);
            if(totalLisOfPersonDetails.size()!=0) {
                listAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.activity_displayuserinlist,
                        R.id.etaAndDistanceView,
                        totalLisOfPersonDetails);
                etaList.setAdapter(listAdapter);
            }
            else{
                Toast.makeText(getApplicationContext(),"There are no attendees for this trip",Toast.LENGTH_LONG).show();
            }
        }
    };


    class UpdateLocationTask extends AsyncTask<ParseGeoPoint, Void, Void> {

        @Override
        protected Void doInBackground(ParseGeoPoint... params) {
            final ParseGeoPoint updateLocation=params[0];
            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
            // Retrieve the object by id
            query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
                public void done(ParseObject result, ParseException e) {
                    if (e == null) {
                        result.put("position", updateLocation);
                        Log.d(TAG, "curUsrLoc " + updateLocation + " saved");
                        result.saveInBackground();
                    } else {
                        Log.d(TAG, "updCurUserError" + e.getMessage());
                    }
                }
            });
            return null;
        }
    }


    /*
    private class UpdateEtaList extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            progressDialog = new ProgressDialog(EtaActivity.this);
            // Set progressdialog title
            progressDialog.setTitle("Updating ETA values");
            // Set progressdialog message
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            // Show progressdialog
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            if (listOfUserIds.size()!=0) {
                for (int i = 0; i < listOfUserIds.size(); i++) {
                    Log.d(TAG,"FirstUserId:"+listOfUserIds.get(i));
                    HashMap<String, String> singlePersonDetails = db.pullUserDetailsForETA(listOfUserIds.get(i), destLocation);
                    //singlePersonDetails = db.pullUserDetails(listOfUserIds.get(i), location);
                    totalLisOfPersonDetails.add(singlePersonDetails);
                    Log.d(TAG,totalLisOfPersonDetails.toString());
                    //Hashmaps <"Attendeename","eta,distance from destination"> for each personid in the list
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            etaList=(ListView)findViewById(R.id.etaList);
            if(totalLisOfPersonDetails.size()!=0) {
                listAdapter = new ArrayAdapter<HashMap<String, String>>(getApplicationContext(),
                        R.layout.activity_displayuserinlist,
                        totalLisOfPersonDetails);
                etaList.setAdapter(listAdapter);
            }
            else{
                Toast.makeText(getApplicationContext(),"There are no attendees for this trip",Toast.LENGTH_LONG).show();
            }
            totalLisOfPersonDetails=null;
            progressDialog.dismiss();
        }
    }
    */
}
