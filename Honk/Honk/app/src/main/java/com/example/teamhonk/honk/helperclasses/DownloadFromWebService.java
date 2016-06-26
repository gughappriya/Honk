package com.example.teamhonk.honk.helperclasses;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Aravind on 11/18/2015.
 */
public class DownloadFromWebService {

    private static final String TAG= "DownloadFromWebService";

    protected JSONObject downloadFromURL(String inURL) throws IOException {
        URL url=new URL(inURL);
        HttpURLConnection connection= (HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in= connection.getInputStream();

            if (connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                Log.d(TAG, "Connection failed inside downloadFromURL()");
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
