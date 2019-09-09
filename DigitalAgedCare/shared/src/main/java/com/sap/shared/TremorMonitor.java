package com.sap.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

public class TremorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "TremorMonitor";
    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGyroscope;
    private static final int SAMPLE_DURATION_MILLISECONDS = 60000;
    private static final int SENSOR_DELAY_MICROSECONDS = 10000; // 10000 microsecond delay between SensorEvents


    private Timer sampleTimer;

    private ArrayList<Double> xAcceleration = new ArrayList<Double>();
    private ArrayList<Double> yAcceleration = new ArrayList<Double>();
    private ArrayList<Double> zAcceleration = new ArrayList<Double>();


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

        sampleTimer = new Timer();
        sampleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processAccelerometerData(new ArrayList<Double>(xAcceleration), new ArrayList<Double>(yAcceleration), new ArrayList<Double>(zAcceleration));
                xAcceleration.clear();
                yAcceleration.clear();
                zAcceleration.clear();
            }
        }, SAMPLE_DURATION_MILLISECONDS, SAMPLE_DURATION_MILLISECONDS);
    }

    public static TremorMonitor getInstance(Context appContext) throws NullContextException, NullSensorManagerException {
        if (instance == null) {
            instance = new TremorMonitor(appContext);
        }

        return instance;
    }

    public void start() {
//        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);

//        sensorManager.registerListener(this, sensorAccelerometer, SENSOR_DELAY_MICROSECONDS);
//        sensorManager.registerListener(this, sensorGyroscope, SENSOR_DELAY_MICROSECONDS);

        sensorManager.registerListener(this, sensorAccelerometer, SENSOR_DELAY_MICROSECONDS, SENSOR_DELAY_MICROSECONDS);
//        sensorManager.registerListener(this, sensorGyroscope, SENSOR_DELAY_MICROSECONDS, SENSOR_DELAY_MICROSECONDS);
    }

    private void processAccelerometerData(ArrayList<Double> xAcceleration, ArrayList<Double> yAcceleration, ArrayList<Double> zAcceleration) {
        Log.d(LOG_TAG, "X accelerometer (" + xAcceleration.size() + "): " + xAcceleration.toString());
        Log.d(LOG_TAG, "Y accelerometer (" + yAcceleration.size() + "): " + yAcceleration.toString());
        Log.d(LOG_TAG, "Z accelerometer (" + zAcceleration.size() + "): " + zAcceleration.toString());

        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Double[] a = xAcceleration.toArray(new Double[xAcceleration.size()]);
        Complex[] xAccFFT = fastFourierTransformer.transform(ArrayUtils.toPrimitive(a), TransformType.FORWARD);
    }

    public void stop() {
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorGyroscope);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            return;
        }

        xAcceleration.add((double) event.values[0]);
        yAcceleration.add((double) event.values[1]);
        zAcceleration.add((double) event.values[2]);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: handle sensor accuracy changes
    }
}
