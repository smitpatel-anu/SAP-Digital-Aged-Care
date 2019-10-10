package com.sap.digitalagedcare;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class GPSLocationTracking {

    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "GpsActivity";
    private LocationRequest locationRequest;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 600000;
    private static final int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private Context context;

    /**
     * Initialization
     * @param context
     */
    public GPSLocationTracking(Context context){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.context=context;
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
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
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
                Log.i(TAG, "The Location is: " + location.getLatitude() + " " + location.getLongitude());
//                sendMessageToActivity(location,context);
            }
        }
    };



    /**
     * Send the last location to another activity
     * @param location
     * @param context
     */
//    private  void sendMessageToActivity(Location location,Context context) {
//        Intent intent = new Intent("GPSLocationUpdates");
//        // Here you can also include some extra data.
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("Location", location);
//        intent.putExtra("Location", bundle);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    }
}
