package com.example.teamhonk.honk.helperclasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.example.teamhonk.honk.contracts.IAsyncResponse;
import com.example.teamhonk.honk.contracts.IDatabaseManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Gughappriya Gnanasekar on 12/10/2015.
 */
public class LocUpdateReceiver extends BroadcastReceiver implements IAsyncResponse {

    public static final String TAG = "LocationReceiver";
    IDatabaseManager dbManager ;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
            final Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
            Log.d(TAG, "  location=" + location);
            if(location != null) {
                onLocationReceived(context, location); // do something with Location object
                return;
            }
        }

        // if here, something else happened
        if(intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
            boolean enabled = intent.getBooleanExtra(LocationManager.
                    KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled); // do something with the change
        }
    }

    private void onProviderEnabledChanged(boolean enabled) {

    }

    private void onLocationReceived(Context context, Location loc) {
        sendUpdateToAppEngine(loc);
        Toast.makeText(context, "LocationUpdate: Latitude=" + loc.getLatitude() + " Longitude=" + loc.getLongitude(), Toast.LENGTH_LONG).show();
    }
    private void sendUpdateToAppEngine(Location location) {
        try{
            dbManager = new ParseManager();
            dbManager.updateUserLocation(location);
            }
        catch(Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }

    @Override
    //this override the implemented method from asyncTask
    public void processFinish(ArrayList<Double> latLong){
        try{
            if(latLong!= null ){

                if(latLong.get(0)==0){
                    Log.d("LOCUPDATE: ", "SENT");

                }

            }}
        catch (Exception ex)
        {
            Log.e(TAG,ex.getMessage());
        }

    }



}

