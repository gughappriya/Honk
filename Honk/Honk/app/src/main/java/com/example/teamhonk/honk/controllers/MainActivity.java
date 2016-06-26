package com.example.teamhonk.honk.controllers;

//Android imports
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//Facebook imports
import com.example.teamhonk.honk.contracts.IDatabaseManager;
import com.example.teamhonk.honk.helperclasses.ParseManager;
import com.example.teamhonk.honk.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import com.facebook.login.widget.LoginButton;

//Parse imports
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

//General imports
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * This is the activity which will be called on the launch of
 * the application. It helps to show the home screen to the user if he is already logged in
 * otherwise it takes the user to login screen where he can login using facebook .
 */

public class MainActivity extends Activity {
    private CallbackManager callbackManager;
    //GUI widgets
    private LoginButton loginButton;
    private TextView loginStatus;
    //Constant strings
    private static final String FB_PROFILE_PERMISSION="public_profile";
    private static final String FB_EMAIL_PERMISSION="email";
    private static final String FB_USERFRIENDS_PERMISSION="user_friends";
    private static final String LOGIN_CANCEL ="Uh oh. The user cancelled the Facebook login.";
    private static final String SIGNUP_LOGIN_SUCCESS = "User signed up and logged in through Facebook!";
    private static final String LOGIN_SUCCESS = "User logged in through Facebook! Welcome to Honk!!!";
    private static final Integer CREATE_TRIP_CODE=1;
    private static final Integer MY_TRIPS_CODE=2;
    private static final String ACTION_MAIN = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        IDatabaseManager dbManager = new ParseManager();
        //Parse should be initialized before calling facebook utils of parse
        dbManager.initialize(this);
        //Parse facebook utils is initialized
        ParseFacebookUtils.initialize(getApplicationContext());
        //Set the view
        setContentView(R.layout.activity_main);

        //make the login functional
        Button loginButton = (Button) findViewById(R.id.login);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                parseRegisterLogin();
            }
        });

        //make the new user functional
        TextView newUserRegister = (TextView) findViewById(R.id.newUser);
        newUserRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                parseRegisterLogin();
            }
        });


//
//        callbackManager = CallbackManager.Factory.create();
//
//        loginStatus =(TextView) findViewById(R.id.loginStatus);
//
//        //Array of fb permissions
//        ArrayList<String> permissions = new ArrayList<String>();
//        permissions.add(0,FB_PROFILE_PERMISSION);
//        permissions.add(1,FB_EMAIL_PERMISSION);
//        permissions.add(2,FB_USERFRIENDS_PERMISSION);
//
//        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
//            @Override
//            public void done(final ParseUser user, ParseException err) {
//                //Login is cancelled by user
//                if (user == null) {
//                    loginStatus.setText(LOGIN_CANCEL);
//                    //User is new so he is automatically signedup for the app and logged in
//                } else if (user.isNew()) {
//                    loginStatus.setText(SIGNUP_LOGIN_SUCCESS);
//                    saveUserDetails(user);
//                    startLoggedInActivity();
//                    //User already exists so directly signin
//                } else {
//                    loginStatus.setText(LOGIN_SUCCESS);
//                    saveUserDetails(user);
//                    Toast.makeText(getApplicationContext(),"Logged into Honk",Toast.LENGTH_LONG).show();
//                    startLoggedInActivity();
//                }
//            }
//        });
//


    }

    private void saveUserDetails(final ParseUser user){
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            if(object!=null && object.getString("email")!=null && object.getString("email")!=null)
                                user.setEmail(object.getString("email"));
                            user.put("name",object.getString("name"));
                            user.saveInBackground();

                        } catch (JSONException e) {
                            Log.e("HONK", "unexpected JSON exception", e);
                            // Do something to recover ... or kill the app.
                        }

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * This method should start the
     * Activity that is responsible for creating
     * a Trip.
     */
    public void startLoggedInActivity() {

        // TODO - fill in here
        Intent loggedInActivityIntent = new Intent(getApplicationContext(),LoggedInActivity.class);
        loggedInActivityIntent.setAction(Intent.ACTION_MAIN);
        startActivityForResult(loggedInActivityIntent,CREATE_TRIP_CODE);
    }

    /*
    * This method is called when intent completes and returns the result.
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CREATE_TRIP_CODE && data != null){
            Bundle b=data.getExtras();
        }
        else{
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void parseRegisterLogin() {

        callbackManager = CallbackManager.Factory.create();

        loginStatus = (TextView) findViewById(R.id.loginStatus);

        //Array of fb permissions
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(0, FB_PROFILE_PERMISSION);
        permissions.add(1, FB_EMAIL_PERMISSION);
        permissions.add(2, FB_USERFRIENDS_PERMISSION);

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                //Login is cancelled by user
                if (user == null) {
                    loginStatus.setText(LOGIN_CANCEL);
                    //User is new so he is automatically signedup for the app and logged in
                } else if (user.isNew()) {
                    loginStatus.setText(SIGNUP_LOGIN_SUCCESS);
                    saveUserDetails(user);
                    startLoggedInActivity();
                    //User already exists so directly signin
                } else {
                    loginStatus.setText(LOGIN_SUCCESS);
                    Toast.makeText(getApplicationContext(), "Logged into Honk", Toast.LENGTH_LONG).show();
                    startLoggedInActivity();
                }
            }
        });
    }

}
