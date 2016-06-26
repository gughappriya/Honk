package com.example.teamhonk.honk.helperclasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.teamhonk.honk.contracts.IDatabaseManager;
import com.example.teamhonk.honk.controllers.MainActivity;

/**
 * Created by Gughappriya Gnanasekar on 12/10/2015.
 */

public class LocationServiceStarter extends BroadcastReceiver {
    private final String TAG="HONK ServiceStarter";

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
        IDatabaseManager dbManager = new ParseManager();
        //Parse should be initialized before calling facebook utils of parse
        dbManager.initialize(context.getApplicationContext());
        //Parse facebook utils is initialized

        Intent i = new Intent(context.getApplicationContext(),HonkLocUpdateService.class);
        HonkLocUpdateService locUpdateService = HonkLocUpdateService.get(context);
        locUpdateService.startLocationUpdates();
        // i.setClass(context, HonkLocationUpdateService.class);
        //HonkLocationUpdateService.setServiceAlarm(context,false);
        Log.d(TAG, "ServiceStarted");}
        catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
}
