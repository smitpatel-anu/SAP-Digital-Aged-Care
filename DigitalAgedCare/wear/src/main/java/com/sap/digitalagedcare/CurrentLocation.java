package com.sap.digitalagedcare;

public class CurrentLocation {
    public double currentLatitude;
    public double currentLongitude;
    public CurrentLocation (double Lat, double Lon){
        currentLatitude= Lat;
        currentLongitude= Lon;
    }
    /**
     * @return Location latitude
     */
    public double getCurrentLatitude(){
        return currentLatitude;
    }

    /**
     * @return Location longitude
     */
    public double getCurrentLongitude(){
        return currentLongitude;
    }

}
