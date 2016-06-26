package com.example.teamhonk.honk.models;

/**
 * Created by Gughappriya Gnanasekar on 11/19/2015.
 */
public class UserTrip {
    private String UserId;
    private String TripId;

    public String getTripId() {
        return TripId;
    }

    public void setTripId(String tripId) {
        TripId = tripId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
