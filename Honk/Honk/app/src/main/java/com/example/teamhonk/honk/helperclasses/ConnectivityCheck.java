package com.example.teamhonk.honk.helperclasses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Aravind on 11/18/2015.
 */
public class ConnectivityCheck {

    private static final String TAG="ConnectivityCheck";

    //To check connectivity- USE SOMEWHERE!
    private boolean isConnected(Context context){
        ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=null;
        if (mConnectivityManager!=null) {
            networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!networkInfo.isAvailable()){
                networkInfo= mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
        }
        if (networkInfo!=null){
            return true;
        }else{
            Log.d(TAG, networkInfo.getReason());
            return false;
        }
    }
}
