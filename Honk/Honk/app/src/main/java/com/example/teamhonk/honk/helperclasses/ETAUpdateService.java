package com.example.teamhonk.honk.helperclasses;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ListView;

import com.example.teamhonk.honk.R;
import com.example.teamhonk.honk.controllers.EtaActivity;
import com.example.teamhonk.honk.controllers.LoggedInActivity;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Aravind on 11/23/2015.
 */

/*
INPUT: ArrayList listOfUserIds, double[] destinationParameters
OUTPUT: ArrayList<HashMap<"userName","eta,dist">>
 */
public class ETAUpdateService extends Service {

    private static final String TAG= "ETAUpdateService";
    private static final String API_KEY="AIzaSyCf7pQGuTFPrB5P6u0RMhtx2-QJaB3gPSU";


    private NotificationManager notificationManager;
    //private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final ArrayList<HashMap<String,String>> totalUserEta=new ArrayList<>();
        final ArrayList<String> listOfUserIds = intent.getStringArrayListExtra("listOfUserIds");
        double[] destParam= intent.getDoubleArrayExtra("destParams");

        //This string has "Lattitude,Longitude" for the destination
        String destLocString= String.valueOf(destParam[0]).trim()
                +","+String.valueOf(destParam[1]).trim();
        Log.d(TAG, "desLocString:" + destLocString);

        /*

        new Thread(new Runnable() {
            @Override
            public void run() {


                Log.d(TAG,"new Thread()");
                 */
        for( String singleUserId:listOfUserIds ){
            Log.d(TAG,"FirstUser:"+singleUserId);
            //Dealing with each single person for the trip

            //First get his Username and "Lattitude,Longitude" position from the user table
            ArrayList<String> resultGetUsernamePosition= getUsernamePosition(singleUserId);

            String userName= resultGetUsernamePosition.get(0);
            String userLocString= resultGetUsernamePosition.get(1);
            Log.d(TAG,"userLocStr:"+userLocString);
            ArrayList<String> userEtaDistArrayList=new ArrayList<>();
            String userEtaDistString=null;
            ArrayList<String> distMatrixParameters= new ArrayList<String>();
            distMatrixParameters.add(userLocString);
            distMatrixParameters.add(destLocString);

            try {
                userEtaDistArrayList=new GoogleDistanceMatrixRequestTask().execute(distMatrixParameters).get();
                if(userEtaDistArrayList!=null) {
                    userEtaDistString = "ETA:" + userEtaDistArrayList.get(0).trim()
                            + "Distance" + userEtaDistArrayList.get(1).trim();
                }else{
                    userEtaDistString = "ETA: Couldn't be calculated . Please select only drivable locations.";
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //etaDistanceInfo(userLocString,destLocString);
            Log.d(TAG,"userEtaDistString:"+userEtaDistString);

            HashMap<String,String> userHash=new HashMap<String, String>();
            userHash.put(userName,userEtaDistString);
            totalUserEta.add(userHash);
        }
        /*
            }
        }).start();
        Log.d(TAG, "Just after the new thread..");
        */

        //Intents
        //CHANGE THIS!!
        Intent notificationIntent=new Intent(this,EtaActivity.class);
        notificationIntent.putExtra("etaValues",totalUserEta);
        PendingIntent pIntent= PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //Creating ArrayList of Strings to send as broadcast
        ArrayList<String> resultString=new ArrayList<>();
        for (HashMap<String,String> eachData:totalUserEta){
            Set<String> userNameSet= eachData.keySet();
            String singleUserName=null;
            String EtaDistValues=null;
            for (String i:userNameSet){
                singleUserName=i;
                EtaDistValues= eachData.get(singleUserName);
            }
            Log.d(TAG,"Username: "+singleUserName);
            Log.d(TAG,"ETADistValue: "+EtaDistValues);
            //String EtaDistValues= eachData.get(singleUserName);
            String singleStatus= singleUserName+": "+EtaDistValues;
            resultString.add(singleStatus);
        }


        //NOTIFICATION
        notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification= new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ETA Updates")
                .setContentText(resultString.toString())
                .setContentIntent(pIntent).build();

        notificationManager.notify(1, notification);
        //stopSelf();


        //Broadcast
        Intent broadcatIntent= new Intent("etaUpdates");
        broadcatIntent.putExtra("etaData", resultString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcatIntent);

        scheduleNextUpdate();

        return Service.START_STICKY;
    }

    public ArrayList<String> getUsernamePosition(String singleUserId){
        /*
        INPUT: userId
        OUTPUT: <"username, ""Lattitude,Longitude"> corresponding to the userId
         */
        Log.d(TAG,"getUsernamePosition()");
        List<ParseObject> parseResult=null;
        ArrayList<String> resultArray=new ArrayList<>();

        String resultPosition=null;

        ParseQuery<ParseObject> query= new ParseQuery<ParseObject>("_User");
        query.whereEqualTo("objectId", singleUserId);
        try{
            parseResult= query.find();
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
        Log.d(TAG,"parseResult Size:"+parseResult.size());

        if(parseResult.size()==0){
            //DO SOMETHING!!
        }
        for (ParseObject resultUser: parseResult){
            resultArray.add(resultUser.getString("name"));
            resultPosition=String.valueOf(resultUser.getParseGeoPoint("position").getLatitude()).trim()
                    +","+String.valueOf(resultUser.getParseGeoPoint("position").getLongitude()).trim();
            resultArray.add(resultPosition);
        }
        Log.d(TAG, "resultArray:" + resultArray);
        return resultArray;
    }

    void scheduleNextUpdate(){
        stopSelf();
       /*
       Log.d(TAG,"scheduleNextUpdate()");
        Intent timerIntent=new Intent(this,ETAUpdateService.class);
        PendingIntent timerPi=PendingIntent.getService(this,0,timerIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTimeMillis = System.currentTimeMillis();
        long nextUpdateTimeMillis = currentTimeMillis + 2 * DateUtils.MINUTE_IN_MILLIS;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, timerPi);
        */
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
