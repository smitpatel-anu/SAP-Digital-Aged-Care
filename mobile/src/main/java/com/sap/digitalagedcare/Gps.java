package com.sap.digitalagedcare;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class Gps {

    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "GpsActivity";
    private LocationRequest locationRequest;
    private double lastLatitude;
    private double lastLongitude;

    public Gps(Context context){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener( new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            lastLatitude=location.getLatitude();
                            lastLongitude= location.getLongitude();
                        }else {
                            Log.e(TAG, "no location detected");
                        }
                    }
                });
    }

    public double getLatitude(){
        return lastLatitude;
    }

    public double getLongitude(){
        return lastLongitude;
    }

}
