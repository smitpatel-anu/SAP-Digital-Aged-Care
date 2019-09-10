package com.sap.digitalagedcare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;
    Gps gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(hasGps()) {
            gps= new Gps(this);
            if (!hasPermission(this)) {
                Log.e(TAG, "Permission denied!");
                return;
            }
            gps.getLastLocation();
            gps.createLocationRequest();
        }else {
            Log.e(TAG, "The device does not have gps");
        }
    }

    /**
     *Check if the device has gps feature
     * @return True if gps available
     */
    boolean hasGps(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    /**
     *Check the permission request result
     * @param context
     * @return True if user allow the location service
     */
    boolean hasPermission(Context context){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }return false;
        }
        return true;
    }

}
