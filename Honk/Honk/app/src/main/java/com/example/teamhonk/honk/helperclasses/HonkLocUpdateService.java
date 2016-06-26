package com.example.teamhonk.honk.helperclasses;

/**
 * Created by Gughappriya Gnanasekar on 12/10/2015.
 */

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Gughappriya Gnanasekar on 12/6/2015.
 */
public class HonkLocUpdateService {
    private static final String TAG = "HonkLocUpdateServicen";

    public static final String ACTION_LOCATION = "com.example.teamhonk.honk.helperclasses.ACTION_LOCATION";

    private static HonkLocUpdateService sTripManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    // private constructor forces users to use ETALocationManager.get(Context)
    private HonkLocUpdateService(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager) mAppContext.
                getSystemService(Context.LOCATION_SERVICE);
    }

    public static HonkLocUpdateService get(Context c) {
        if (sTripManager == null) {
            // Use the application context to avoid leaking activities
            sTripManager = new HonkLocUpdateService(c.getApplicationContext());
        }
        return sTripManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        Log.d(TAG,"Inside Start Location Updates");
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d(TAG,"Inside GPS ENabled");
            String provider = LocationManager.GPS_PROVIDER;

            // Start updates from LocationManager
            PendingIntent pi = getLocationPendingIntent(true);

            mLocationManager.requestLocationUpdates(provider, 1000 * 60, 0, pi);
            Log.d(TAG, "Location Update requested");
        }else{
            Log.d(TAG,"Inside GPS Disabled. Exiting without starting loc updates!!!!");
            showGPSDisabledAlertToUser();
        }

    }
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mAppContext);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mAppContext.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingTrip() {
        return getLocationPendingIntent(false) != null;
    }
}


