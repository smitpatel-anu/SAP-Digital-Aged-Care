package com.sap.digitalagedcare;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class FallDetectionActivity extends WearableActivity implements SensorEventListener {

    private TextView mTextView;
    private SensorManager mySensorManager;
    private Sensor myAccelerometer;
    private Sensor myGyroscope;
    private Thread thread;
    private boolean bp = true;

    private final static String TAG = FallDetectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection);

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
                bp = false;
                Toast.makeText(FallDetectionActivity.this,"free fall",Toast.LENGTH_SHORT).show();
            }
            if (acc>3f && !bp) {
                bp = true;
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
