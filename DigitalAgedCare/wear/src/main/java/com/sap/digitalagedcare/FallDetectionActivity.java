package com.sap.digitalagedcare;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FallDetectionActivity extends WearableActivity implements SensorEventListener {

    private SensorManager mySensorManager;
    private Sensor myAccelerometer;
    private Sensor myGyroscope;
    private Thread thread;
    private boolean bp = true;
    private boolean ff = false;
    public MyLocationService locationService;
    private static CurrentLocation currentLocation;


    private final static String TAG = FallDetectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection);
        currentLocation= new CurrentLocation();

        final Intent intent = new Intent(this.getApplication(), MyLocationService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(FallDetectionActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));

        // get an instance of the SensorManager
        Log.d(TAG, "onCreate: Initializing sensor service.");
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // get an instance of the Accelerometer and Gyroscope
        Log.d(TAG, "onCreate: Initializing accelerometer.");
        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG, "onCreate: Initializing gyroscope.");
        myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // register the sensor listener
        mySensorManager.registerListener(FallDetectionActivity.this, myAccelerometer, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Accelerometer listener is registered.");
        mySensorManager.registerListener(FallDetectionActivity.this, myGyroscope, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Gyroscope listener is registered.");


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float acc = 0f;
        float gyro = 0f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            Log.d(TAG, "onSensorChanged: ||| X:" + event.values[0] + ", Y:" + event.values[1] + ", Z:" + event.values[2]);
            DecimalFormat df = new DecimalFormat("#.#");
            float xA = Float.valueOf(df.format(event.values[0]));
            float yA = Float.valueOf(df.format(event.values[1]));
            float zA = Float.valueOf(df.format(event.values[2]));
            acc = Float.valueOf(df.format((float) Math.sqrt(xA*xA + yA*yA + zA*zA)));

            // record data

            // free fall event
            if (acc<1f && bp) {
                ff = true;
                bp = false;
                Toast.makeText(FallDetectionActivity.this, R.string.freefall,Toast.LENGTH_SHORT).show();
            }

            if (acc>20f && ff){
                ff = false;
                AlertDialog dialog=new AlertDialog.Builder(FallDetectionActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Fall Detected!")
                        .setMessage("The Location is:" + currentLocation.getLatitude() + " " + currentLocation.getLongitude())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            }
            if (acc>3f && !bp) {
                bp = true;
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * monitoring the state of MyLocationService.
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
     * receive an intent message from another class
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle b = intent.getBundleExtra("Location");
            Location location = (Location) b.getParcelable("Location");
            if (location != null) {
                currentLocation.SetLocation(location);
                Log.i(TAG, "The Location is*:" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
            }
        }
    };
}
