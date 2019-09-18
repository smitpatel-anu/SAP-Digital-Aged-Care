package com.sap.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 *
 */
public class GPSLocationTracking {
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "GpsActivity";
    private LocationRequest locationRequest;
    private double lastLatitude;
    private double lastLongitude;

    /**
     * Initialization
     * @param context
     */
    public GPSLocationTracking(Context context){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }


    /**
     *Get last location using FusedLocationProviderClient and initialize lastLatitude and
     * lastLongitude if it is not null
     */
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

    /**
     * @return Location latitude
     */
    public double getLatitude(){
        return lastLatitude;
    }

    /**
     * @return Location longitude
     */
    public double getLongitude(){
        return lastLongitude;
    }

    /**
     * The location wil continue to be updating according to the location request time
     */
    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
    }

    /**
     * The location wil stop updating
     */
    public void stopLocationUpdate() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Assign the location request settings
     */
    public void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //The locationCallback idea from https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());
                //mLastLocation = location;
                lastLatitude=location.getLatitude();
                lastLongitude= location.getLongitude();
            }
        }
    };

}
