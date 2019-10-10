package com.sap.digitalagedcare;

import android.location.Location;

public class CurrentLocation {
    private double latitude;
    private double longitude;

    public void SetLocation(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }

}
