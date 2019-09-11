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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;

public class TremorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "TremorMonitor";
    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGyroscope;
    private static final int SAMPLE_DURATION_MILLISECONDS = 10000;
    private static final int SENSOR_DELAY_MICROSECONDS = 100000;
    private static final int MICROSECONDS_PER_SECOND = 1000000;
    private static final int SAMPLING_RATE_HERTZ = MICROSECONDS_PER_SECOND / SENSOR_DELAY_MICROSECONDS; // samples per second (Hz)
    private static final int TREMOR_THRESHOLD_FREQUENCY_HERTZ = 3; // > 3 Hz is categorized as a tremor

    private Timer detectionSampleTimer;

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

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
//        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

        detectionSampleTimer = new Timer();
        detectionSampleTimer.scheduleAtFixedRate(new TimerTask() {
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

        Double[] xAcc = xAcceleration.toArray(new Double[0]);
        Complex[] xAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xAcc), 1024), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "X dominant frequency: " + xDominantFrequency);

        Double[] yAcc = yAcceleration.toArray(new Double[0]);
        Complex[] yAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yAcc), 1024), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "Y dominant frequency: " + yDominantFrequency);

        Double[] zAcc = zAcceleration.toArray(new Double[0]);
        Complex[] zAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zAcc), 1024), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "Z dominant frequency: " + zDominantFrequency);

        if (xDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || yDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || zDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ) {
            Log.d(LOG_TAG, "Tremor detected");
        }
    }

    private double getDominantFrequency(Complex[] fftResult, int samplingRateHertz) {
        if (fftResult == null) {
            return 0;
        }

        double[] magnitudes = getMagnitudesFromFFT(fftResult);
        int indexOfMaximum = getIndexOfMaximum(magnitudes);
        if (indexOfMaximum < 0) {
            return 0;
        }

        return (indexOfMaximum * (samplingRateHertz / 2)) / (fftResult.length / 2);
    }

    public int getIndexOfMaximum(double[] arr) {
        if (arr == null || arr.length == 0) {
            return -1;
        }

        int maxIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIndex]) {
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    private double[] getMagnitudesFromFFT(Complex[] fftResult) {
        if (fftResult == null) {
            return null;
        }

        double[] magnitudes = new double[fftResult.length / 2];

        for (int i = 0; i < fftResult.length / 2; i++) {
            magnitudes[i] = fftResult[i].abs();
        }

        return magnitudes;
    }

    public void stop() {
        sensorManager.unregisterListener(this, sensorAccelerometer);
//        sensorManager.unregisterListener(this, sensorGyroscope);
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
