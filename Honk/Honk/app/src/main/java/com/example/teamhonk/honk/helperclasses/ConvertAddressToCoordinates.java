package com.example.teamhonk.honk.helperclasses;

import android.os.AsyncTask;
import android.util.Log;

import com.example.teamhonk.honk.contracts.IAsyncResponse;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Asynctask to accept addresses and provide (lattitude, longitude)
 * Eg:-
 * Accepted address: 1600 Amphitheatre Parkway,Mountain View,CA
 * resultList: [37.4220352, -122.0841244]
 *
 */
public class ConvertAddressToCoordinates extends AsyncTask<String,String,ArrayList<Double>> {
    private static final String TAG= "ConvertAddressToCoordinates";
    private static final String API_KEY="AIzaSyDakCt2ww6_E3XJ-c1JeJwIUs3bvGWeCO4";
    private static final String API_KEY_GEOCODE="AIzaSyBSDwi7LuSzSRZFk6RS8ybgEooUH9yXbRM";
    DownloadFromWebService downloadFromWebService=new DownloadFromWebService();

    public IAsyncResponse delegate = null;



    protected void onPostExecute(ArrayList<Double> response) {
        delegate.processFinish(response);
    }



    @Override
    protected ArrayList<Double> doInBackground(String... params) {
        try{
            String requestParameters=params[0];
            String addressInUrlFormat= convertAddressToUrlFormat(requestParameters);
          // downloadFromURL("http://maps.google.com/?cid=1426703155474451232&hl=en&gl=us&shorturl=1");
// read the response
            if(requestParameters.contains("goo.gl")){
            return getCidCoordinates(requestParameters);}
            else {
                //URL to go to the Google web service and fetch the result
                String url = "https://maps.googleapis.com/maps/api/geocode/json?"
                        + "address=" + addressInUrlFormat.trim()
                        + "&key=" + API_KEY_GEOCODE;

                //Resulting JSON object
                JSONObject resultJSON = downloadFromWebService.downloadFromURL(url);
                Log.d(TAG, "ResultJSON" + resultJSON.toString());

                //Gets the Lattitude
                final Double resultLattitude = resultJSON
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                        .getDouble("lat");

                //Gets the Longitude
                final Double resultLongitude = resultJSON
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                        .getDouble("lng");

                //Return Arraylist of results
                ArrayList<Double> resultList = new ArrayList<Double>() {{
                    add(resultLattitude);
                    add(resultLongitude);
                }};
                Log.d(TAG, "ResultList" + resultList.toString());
                return resultList;
            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*Method to convert the address to URL form
    Eg: Convert : 1600 Amphitheatre Parkway,Mountain View,CA
        To:       1600+Amphitheatre+Parkway,+Mountain+View,+CA
     */
    private String convertAddressToUrlFormat(String address){

        try {
            StringBuilder urlFormAddress = new StringBuilder(address);
            for (int i=0;i< address.length();i++){
                if (address.charAt(i)==' '){
                    urlFormAddress.setCharAt(i, '+');
                }
            }

            return urlFormAddress.toString();
        }catch (Exception e){
            Log.e(TAG,"AddressInUrlFormat"+e.getMessage());
            return null;
        }
    }

    public  ArrayList<Double> getCidCoordinates(String shorturl)
    {


        final String URL_FORMAT = "http://maps.google.com/maps?cid=%s";
        final String LATLNG_BEFORE = "viewport:{center:{";
        final String LATLNG_AFTER = "}";
        final String LATLNG_SEPARATOR = ",";
        final String LAT_PREFIX = "lat:";
        final String LNG_PREFIX = "lng:";

        try {
            String getLongUrl = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=" + shorturl.split("\n\n")[1]
                    + "&key=" + API_KEY + "&output=json";
            HttpURLConnection nconnection = (HttpURLConnection) (new URL(getLongUrl)).openConnection();

            StringBuilder longUrlJSON = readContents(nconnection);
            JSONObject longUrlJsonObj = new JSONObject(longUrlJSON.toString());
            Log.d(TAG, "ResultJSON" + longUrlJsonObj.toString());
            String longUrl = longUrlJsonObj
                    .getString("longUrl");
            StringBuffer chaine = new StringBuffer("");

            try {
                URL url = new URL(longUrl);
                HttpURLConnection newCon = (HttpURLConnection) url.openConnection();
                newCon.setInstanceFollowRedirects(false);
                newCon.connect();
                URL secondURL = new URL(newCon.getHeaderField("Location"));
                String a = newCon.getHeaderField("Location");
                URLConnection secondCOn = secondURL.openConnection();
                secondCOn.connect();

                InputStream inputStream = secondCOn.getInputStream();
                String s = secondCOn.getURL().toString();
                URLConnection lastCon = secondCOn.getURL().openConnection();
                lastCon.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(lastCon.getInputStream()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    chaine.append(line);
                }



            } catch (Exception ex) {
                Log.e("ERROR", ex.getMessage());
            }
            final String lat,longi;
            if(shorturl.contains("&")) {
                String text = chaine.toString();
                String[] results = text.split("cacheResponse\\(\\[\\[\\[");
                String[] latlong = results[1].split(",");
                  lat = latlong[2].split("\\]")[0];
                  longi = latlong[1];
            }else{
                String text = chaine.toString();


                String[] results = text.split(shorturl.split("\n")[0]);
                String[] latlong = results[1].split("\\[")[1].split(",");
                  lat = latlong[0];
                  longi = latlong[1].split("\\]")[0];
            }


            ArrayList<Double> resultList = new ArrayList<Double>() {{
                add(Double.parseDouble(lat));
                add(Double.parseDouble(longi));
            }};
            return resultList;
        }
        catch (NumberFormatException e)
        {
            //e.printStackTrace();
            return null;
        } catch(Exception ex)
        {
            return null;
        }

    }

    private StringBuilder readContents(HttpURLConnection nconnection) throws IOException {
        StringBuilder longUrlJSON = new StringBuilder();
        InputStream inputStream = nconnection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        while ((line = rd.readLine()) != null) {
            longUrlJSON.append(line);
        }
        return longUrlJSON;
    }

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
}