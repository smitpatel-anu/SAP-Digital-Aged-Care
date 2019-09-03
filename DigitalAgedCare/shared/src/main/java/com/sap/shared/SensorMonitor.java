/**
 *
 */
package com.sap.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

public class SensorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "SensorMonitor";
    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGyroscope;

    /**
     * SensorMonitor constructor
     *
     * Registers a listener for the following sensors:
     * 1. Accelerometer
     * 2. Gyroscope
     * @param appContext the app context
     * @throws NullContextException thrown when the app context is null
     */
    public SensorMonitor(Context appContext) throws NullContextException, NullSensorManagerException {
        if(appContext == null) {
            throw new NullContextException();
        }

        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager == null) {
            throw new NullSensorManagerException();
        }

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }


    /**
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event == null) {
            return;
        }

        Log.d(LOG_TAG, "New SensorEvent");
        Log.d(LOG_TAG, "timestamp: " + event.timestamp);
        Log.d(LOG_TAG, "accuracy: " + event.accuracy);
        Log.d(LOG_TAG, "sensor: " + event.sensor);
        Log.d(LOG_TAG, "values: " + Arrays.toString(event.values));
    }

    /**
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(LOG_TAG, "Sensor accuracy changed");
        Log.d(LOG_TAG, "Sensor: " + sensor);
        Log.d(LOG_TAG, "Accuracy: " + accuracy);
    }
}
