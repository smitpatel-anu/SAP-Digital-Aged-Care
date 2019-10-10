package com.sap.digitalagedcare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;


public class MainActivity extends WearableActivity {

    public MyLocationService locationService;
    private static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPermission(this)) {
            Log.e(TAG, "Permission denied!");
        }

        Button tremorTestActivityButton = findViewById(R.id.tremorTestActivityButton);
        tremorTestActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, TremorTestActivity.class));
            }
        });

        Button fallDetectionActivityButton = findViewById(R.id.fallDetectionActivityButton);
        fallDetectionActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, FallDetectionActivity.class));
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    /**
     * Check the permission request result
     *
     * @param context
     * @return True if user allow the location service
     */
    boolean hasPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        }
        return true;
    }

}