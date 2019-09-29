package com.sap.digitalagedcare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.sap.shared.GPSLocationTracking;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    public MyLocationService locationService;
    private static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        if (!hasPermission(this)) {
            Log.e(TAG, "Permission denied!");
            return;
        }


        // Enables Always-on
        setAmbientEnabled();
    }


    /**
     *Check the permission request result
     * @param context
     * @return True if user allow the location service
     */
    boolean hasPermission(Context context){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }return false;
        }
        return true;
    }

    /**
     *monitoring the state of MyLocationService.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("MyLocationService")) {
                locationService = ((MyLocationService.LocationServiceBinder) service).getService();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            locationService = null;
        }
    };

    /**
     *receive an intent message from another class
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle b = intent.getBundleExtra("Location");
            Location location = (Location) b.getParcelable("Location");
            if (location != null) {
                Log.i(TAG, "The Location is*:" +location.getLatitude() + " " + location.getLongitude());
            }
        }
    };
}
