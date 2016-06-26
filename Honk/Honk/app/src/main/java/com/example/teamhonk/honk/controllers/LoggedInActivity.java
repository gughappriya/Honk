package com.example.teamhonk.honk.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.teamhonk.honk.R;
import com.example.teamhonk.honk.helperclasses.ParseManager;
import com.parse.ParseUser;

public class LoggedInActivity extends Activity {

    private TextView welcome;
    private Button createTripButton;
    private Button myTripsButton;

    ParseManager db= new ParseManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        welcome=(TextView)findViewById(R.id.welcomeView);
        createTripButton= (Button) findViewById(R.id.createTripBtn);
        myTripsButton=(Button) findViewById(R.id.myTripsBtn);

        Object nameObj = ParseUser.getCurrentUser().get("name");
        if(nameObj!=null){
            String currentUserName = nameObj.toString();
            welcome.setText("Welcome "+currentUserName+"!");
        }



        createTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createTripIntent = new Intent(getApplicationContext(), CreateTripActivity.class);
                createTripIntent.setAction(Intent.ACTION_MAIN);
                startActivity(createTripIntent);
            }
        });
        myTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myTripsIntent=new Intent(getApplicationContext(),MyTripsActivity.class);
                myTripsIntent.setAction(Intent.ACTION_MAIN);
                startActivity(myTripsIntent);
            }
        });

    }

}
