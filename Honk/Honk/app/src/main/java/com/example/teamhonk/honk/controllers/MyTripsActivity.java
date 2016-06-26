package com.example.teamhonk.honk.controllers;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.example.teamhonk.honk.helperclasses.ParseManager;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

public class MyTripsActivity extends ListActivity {
    private static final String TAG="MyTripsActivity";
    TextView myTripHeadingView;
    TextView trip_id;
    ListView lv;
    IDatabaseManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"Inside onCreate()" );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mytrips);

        //myTripHeadingView=(TextView)findViewById(R.id.myTripsHeadingView);

        Log.d(TAG,"Got past the list declaration" );

        //Catching the intent from LoggedInActivity
        Intent catchIntent = getIntent();

        //Gets all the trips in the database for THE SPECIFIC user
        db = new ParseManager();
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        ArrayList<HashMap<String, String>> myTripList =new ArrayList<HashMap<String, String>>();


        db.getMyTripDetails(currentUserId,getListView(),MyTripsActivity.this);


    }
}
