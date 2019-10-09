package com.sap.digitalagedcare;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

public class FallDetectionIntentService extends IntentService implements SensorEventListener {

    private static final String TAG = "FallDetectionIntentService";

    private SensorManager mySensorManager;
    private Sensor myAccelerometer;
    private Sensor myGyroscope;

    public FallDetectionIntentService() {
        super("FallDetectionIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Notification notification = new Notification.Builder(this, "1")
                    .setContentTitle("Fall Detection Service")
                    .setContentText("running...")
                    .setSmallIcon(R.drawable.ic_add_white_24dp)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: ");

        // get an instance of the SensorManager
        Log.d(TAG, "onCreate: Initializing sensor service.");
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // get an instance of the Accelerometer and Gyroscope
        Log.d(TAG, "onCreate: Initializing accelerometer.");
        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG, "onCreate: Initializing gyroscope.");
        myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // register the sensor listener
        mySensorManager.registerListener(FallDetectionIntentService.this, myAccelerometer, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Accelerometer listener is registered.");
        mySensorManager.registerListener(FallDetectionIntentService.this, myGyroscope, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Gyroscope listener is registered.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
