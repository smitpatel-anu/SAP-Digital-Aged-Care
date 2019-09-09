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

public class TremorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "TremorMonitor";
    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGyroscope;

    private static TremorMonitor instance = null;

    private TremorMonitor(Context appContext) throws NullContextException, NullSensorManagerException {
        if (appContext == null) {
            throw new NullContextException();
        }

        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            throw new NullSensorManagerException();
        }

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);
    }

    // static method to create instance of Singleton class
    public static TremorMonitor getInstance(Context appContext) throws NullContextException, NullSensorManagerException {
        if (instance == null) {
            instance = new TremorMonitor(appContext);
        }

        return instance;
    }

    public void start() {
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorGyroscope);
    }

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(LOG_TAG, "Sensor accuracy changed");
        Log.d(LOG_TAG, "Sensor: " + sensor);
        Log.d(LOG_TAG, "Accuracy: " + accuracy);
    }
}
