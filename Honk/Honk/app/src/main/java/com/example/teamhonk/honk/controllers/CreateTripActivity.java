package com.example.teamhonk.honk.controllers;

//import statements from Android library
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

//Import statements from honk
import com.example.teamhonk.honk.contracts.IAsyncResponse;
import com.example.teamhonk.honk.contracts.IDatabaseManager;
import com.example.teamhonk.honk.helperclasses.ConversionHelper;
import com.example.teamhonk.honk.helperclasses.ConvertAddressToCoordinates;
import com.example.teamhonk.honk.helperclasses.ParseManager;
import com.example.teamhonk.honk.R;
import com.example.teamhonk.honk.models.Trip;
import com.example.teamhonk.honk.models.User;
import com.google.android.gms.maps.model.LatLng;


//General imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
* This activity is responsisble for create trip .
*
 */
public class CreateTripActivity extends Activity implements IAsyncResponse {
    private Button btnSave;
    private EditText textName;
    private EditText textLocation;
    private EditText textFriend;
    private Button btnCalendar, btnTimePicker, btnSearchLoc, btnFriendPicker;
    private EditText textDate;
    private EditText textTime;


    private int mYear, mMonth, mDay, mHour, mMinute;
    IDatabaseManager dbManager ;

    private static final String DATE_TIME_FORMAT="yyyy-MM-dd hh:mm:ss";
    private static final String CREATE_SUCCESS="Trip created successfully";
    private static final String REQUIRED_VALIDATION_TRIPNAME ="Trip name is required";
    private static final String REQUIRED_VALIDATION_TRIPLOC = "Trip location is required";
    private static final String REQUIRED_VALIDATION_FRIEND = "Select atleast one friend!!";
    private static final String REQUIRED_VALIDATION_DATE = "Trip date is required";
    private static final String REQUIRED_VALIDATION_TIME = "Trip time is required";
    private static final String TAG= "ConvertAddressToCoordinates";

    final int PICK_CONTACT = 1;
    ArrayList<Double> tripLocationValues;
    HashMap<String,String> friendList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        Intent receivedIntent = getIntent();

        //get the action
        String receivedAction = receivedIntent.getAction();

        //make sure it's an action and typdbe we can handle
        if(receivedAction.equals(Intent.ACTION_SEND)){
            //content is being shared
            String loc = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            textLocation = (EditText) findViewById(R.id.editTripLocaton);
            textLocation.setText(loc.split("\n")[0]);
            Log.i("IntentExtra: ", loc);
            ConvertAddressToCoordinates convertAddressHelper = new ConvertAddressToCoordinates();
            convertAddressHelper.delegate = this;
            convertAddressHelper.execute(loc);

        }



            textDate = (EditText)findViewById(R.id.editTripDate);
            textTime = (EditText)findViewById(R.id.editTripTime);
            btnCalendar = (Button)findViewById(R.id.btnCalendar);
            btnCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Process to get Current Date
                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    // Launch Date Picker Dialog
                    DatePickerDialog datePickerDlg = new DatePickerDialog(CreateTripActivity.this,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {
                                    // Display Selected date in textbox
                                    textDate.setText( year + "-"
                                            + (monthOfYear + 1) + "-" +dayOfMonth);

                                }
                            }, mYear, mMonth, mDay);
                    datePickerDlg.show();
                }
            });
            btnTimePicker = (Button)findViewById(R.id.btnTimePicker);
            btnTimePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Process to get Current Time
                    final Calendar c = Calendar.getInstance();
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog tpd = new TimePickerDialog(CreateTripActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {
                                    // Display Selected time in textbox
                                    textTime.setText(hourOfDay + ":" + minute +":00");
                                }
                            }, mHour, mMinute, false);
                    tpd.show();
                }
            });
            btnSave = (Button) findViewById(R.id.saveTrip);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Trip trip = createTrip();
                    if(saveTrip(trip)){
                        Context context = getApplicationContext();
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, CREATE_SUCCESS, duration);
                        toast.show();
                        Intent homeIntent = new Intent(getApplicationContext(),LoggedInActivity.class);
                        startActivity(homeIntent);
                    }
                }
            });
            btnSearchLoc = (Button) findViewById(R.id.btnSearchLocation);
            btnSearchLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showMap(Uri.parse("geo:0,0?q=restaurants+near+me"));
                }
            });
        btnFriendPicker = (Button) findViewById(R.id.btnFriendPicker);
        btnFriendPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * This method creates the intent to call the contact picker
                 */
               startViewContactActivity();
            }
        });




    }



    public void startViewContactActivity() {


        startActivityForResult(new Intent(CreateTripActivity.this,ContactActivity.class),PICK_CONTACT);
    }

    @Override
    //this override the implemented method from asyncTask
    public void processFinish(ArrayList<Double> output){
       tripLocationValues = output;
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (intent.resolveActivity(getPackageManager()) != null) {
          // startActivityForResult(intent,1);
            startActivityForResult(intent,100);
        }

    }

    /**
     *Trip details are saved in parse database
     *
     * @return whether the Trip was successfully
     * saved.
     */
    public boolean saveTrip(Trip trip) {
        if (trip != null) {

            dbManager = new ParseManager();
            dbManager.saveTrip(trip);
            return true;
        } else {
            return false;
        }
    }


    /**
     * This method is used to
     * instantiate a Trip model object.
     *
     * @return The Trip as represented
     * by the View.
     */
    public Trip createTrip() {

        // TODO - fill in here
        Trip trip = null;
        textName = (EditText) findViewById(R.id.editTripName);
        textLocation = (EditText) findViewById(R.id.editTripLocaton);
        textTime = (EditText) findViewById(R.id.editTripTime);
        textDate = (EditText) findViewById(R.id.editTripDate);
        textFriend = (EditText) findViewById(R.id.editTripFriend);
        boolean isDirty = false;
        if (textName.getText().length() == 0) {
            textName.setError(REQUIRED_VALIDATION_TRIPNAME);
            isDirty = true;
        }
        if (textLocation.getText().length() == 0) {
            textLocation.setError(REQUIRED_VALIDATION_TRIPLOC);
            isDirty = true;
        }
        if (textFriend.getText().length() == 0) {
            textFriend.setError(REQUIRED_VALIDATION_FRIEND);
            isDirty = true;
        }
        if (textDate.getText().length() == 0) {
            textDate.setError(REQUIRED_VALIDATION_DATE);
            isDirty = true;
        }
        if (textTime.getText().length() == 0) {
            textTime.setError(REQUIRED_VALIDATION_TIME);
            isDirty = true;
        }
        if (isDirty == false) {
            String[] friendNames = textFriend.getText().toString().split(",");
            ArrayList<User> persons = new ArrayList<User>();
            String datetime = textDate.getText().toString()+" "+textTime.getText().toString();
            ConversionHelper convHelper = new ConversionHelper();
            Calendar cal =convHelper.ConvertStringToCalendar(datetime, DATE_TIME_FORMAT);

            trip = new Trip(textName.getText().toString(),
                    textLocation.getText().toString(),
                    cal.getTime(),
                    tripLocationValues
                    );
            trip.setFriends(friendList);
        }

        return trip;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode==RESULT_OK && !data.getExtras().isEmpty() && data.getExtras().containsKey("selectedContacts"))
        {
            StringBuilder sb = new StringBuilder();
            friendList = new HashMap<String,String>();
            Object[] objArray = (Object[])data.getExtras().getSerializable("selectedContacts");
            String selectedContacts[][]=null;
            if(objArray!=null)
            {
                selectedContacts = new String[objArray.length][];
                for(int i=0;i<objArray.length;i++)
                {
                    selectedContacts[i] = (String[]) objArray[i];
                    sb.append(selectedContacts[i][1] + ",");
                    friendList.put(selectedContacts[i][4], selectedContacts[i][1]);

                    //Toast.makeText(getApplicationContext(),"SelectedContact: "+ selectedContacts[i][4],Toast.LENGTH_LONG).show();
                }
                textFriend = (EditText) findViewById(R.id.editTripFriend);
                textFriend.setText(sb.toString());

                //Now selectedContacts[] contains the selected contacts
            }
        }

    }

}
