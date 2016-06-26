package com.example.teamhonk.honk.models;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by Gughappriya Gnanasekar on 10/29/2015.
 * Person class holds the information about person who uses this app
 *
 */
//TODO: Remove this cl
@ParseClassName("_User")
public class User extends ParseUser {

    public User(String name){
        Name=name;
    }

    //Name of the person
    private String Name;
    public String getName(){
        return Name;
    }
    public void setName(String val){
        Name = val;
    }

    //Position of his position
    private ParseGeoPoint position;

    public ParseGeoPoint getPosition() {
        return position;
    }

    public void setPosition(ParseGeoPoint position) {
        this.position = position;
    }
}