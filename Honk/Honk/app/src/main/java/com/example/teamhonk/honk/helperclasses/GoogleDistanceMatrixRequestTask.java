package com.example.teamhonk.honk.helperclasses;

/**
 * Created by Aravind on 11/15/2015.

 Accepts the origin and destination (CURRENTLY AS NAMES BUT LATER IN LATTITUDE AND LONGITUDE)
 and returns eta and distance from destination as a List of Strings

 requestParameters.get(0)= origin location (SHOULD CHANGE TO LATTITUDE LONGITUDE USING GEOCODE)
 requestParameters.get(1)= destination location (SHOULD CHANGE TO LATTITUDE LONGITUDE USING GEOCODE)
 requestParameters.get(2)= departure time

 resultList.get(0) = ETA value
 resultList.get(1) = Distance from destination


 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
//import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//import java.util.List;

/**
 * Created by Aravind on 11/15/2015.

 Accepts the origin and destination (CURRENTLY AS NAMES BUT LATER IN LATTITUDE AND LONGITUDE)
 and returns eta and distance from destination as a List of Strings

 requestParameters.get(0)= origin location (SHOULD CHANGE TO LATTITUDE LONGITUDE USING GEOCODE)
 requestParameters.get(1)= destination location (SHOULD CHANGE TO LATTITUDE LONGITUDE USING GEOCODE)
 requestParameters.get(2)= departure time

 resultList.get(0) = ETA value
 resultList.get(1) = Distance from destination


 */
public class GoogleDistanceMatrixRequestTask extends AsyncTask<ArrayList<String>,String,ArrayList<String>>{

    private static final String TAG="GoogleMatrixRequest";
    private static final String API_KEY="AIzaSyCf7pQGuTFPrB5P6u0RMhtx2-QJaB3gPSU";

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... params) {
        try{
            Log.d(TAG,"doInBackground");
            ArrayList<String>requestParameters=params[0];
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?"
                    +"origins="+requestParameters.get(0).trim()+"&"
                    +"destinations="+requestParameters.get(1).trim()+"&"
                    +"departure_time=now"+"&"
                    +"mode=driving"+"&"
                    +"traffic_model=best_guess"+"&"
                    +"language=en-EN"+"&"
                    +"key="+API_KEY;
            JSONObject resultJSON= downloadFromURL(url);

            final String resultEta= resultJSON.getJSONArray("rows").getJSONObject(0)
                    .getJSONArray("elements").getJSONObject(0)
                    .getJSONObject("duration")
                    .getString("text");
            final String resultDistance = resultJSON.getJSONArray("rows").getJSONObject(0)
                    .getJSONArray("elements").getJSONObject(0)
                    .getJSONObject("distance")
                    .getString("text");
            ArrayList<String> resultList = new ArrayList<String>() {{
                add(resultEta);
                add(resultDistance);
            }};
            Log.d(TAG,resultList.toString());
            return resultList;
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            return null;
        }
    }
    //Method to connect to the web service and extract the JSON result from it
    private  JSONObject downloadFromURL(String inURL) throws IOException{
        URL url=new URL(inURL);
        HttpURLConnection connection= (HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in= connection.getInputStream();

            if (connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                Log.d(TAG,"Connection failed inside downloadFromURL()");
                return null;
            }

            int bytesRead=0;
            byte[] buffer= new byte[1024];
            while((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return new JSONObject(new String(out.toByteArray()));
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            return null;
        }
    }
    // Just for debugging
    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        Log.d(TAG, "HTTP Call inside GoogleDistanceMatrixRequestTask completed");
    }
}