package com.sap.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.room.Room;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;

public class TremorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "TremorMonitor";

    private static final int MICROSECONDS_PER_SECOND = 1000000;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int SAMPLE_DURATION_MILLISECONDS = 60000;
    private static final int SENSOR_DELAY_MICROSECONDS = 10000;
    private static final int SAMPLING_RATE_HERTZ = MICROSECONDS_PER_SECOND / SENSOR_DELAY_MICROSECONDS; // samples per second (Hz)
    private static final int TREMOR_THRESHOLD_FREQUENCY_HERTZ = 3; // > 3 Hz is categorized as a tremor
    private static final int NUM_DATA_POINTS_PER_SAMPLE = (SAMPLE_DURATION_MILLISECONDS / MILLISECONDS_PER_SECOND) * SAMPLING_RATE_HERTZ;
    // find nearest power of 2 greater than number of data points per sample
    private static final int FFT_WINDOW_SIZE = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(NUM_DATA_POINTS_PER_SAMPLE - 1));

    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private WeakReference<Context> appContext;
    private final Sensor sensorGyroscope;
    private final TremorDatabase tremorDatabase;

    private long currentSampleStartTimestamp;
    private long currentSampleEndTimestamp;

    private final Timer detectionSampleTimer;

    private final ArrayList<Double> xAcceleration = new ArrayList<>();
    private final ArrayList<Double> yAcceleration = new ArrayList<>();
    private final ArrayList<Double> zAcceleration = new ArrayList<>();


    private static TremorMonitor instance = null;

    private TremorMonitor(Context appContext) throws NullContextException, NullSensorManagerException {
        if (appContext == null) {
            throw new NullContextException();
        }

        this.appContext = new WeakReference<>(appContext);
        this.tremorDatabase = Room.databaseBuilder(appContext,
                TremorDatabase.class,
                "tremor-database").build();

        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            throw new NullSensorManagerException();
        }

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
        sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

        detectionSampleTimer = new Timer();
        detectionSampleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentSampleEndTimestamp = System.currentTimeMillis();
                processAccelerometerData(new ArrayList<>(xAcceleration),
                        new ArrayList<>(yAcceleration),
                        new ArrayList<>(zAcceleration),
                        currentSampleStartTimestamp,
                        currentSampleEndTimestamp);
                currentSampleStartTimestamp = currentSampleEndTimestamp;
                xAcceleration.clear();
                yAcceleration.clear();
                zAcceleration.clear();
            }
        }, SAMPLE_DURATION_MILLISECONDS, SAMPLE_DURATION_MILLISECONDS);
        currentSampleStartTimestamp = System.currentTimeMillis();
    }

    public TremorDatabase getTremorDatabase() {
        return tremorDatabase;
    }

    public static TremorMonitor getInstance(Context appContext)
            throws NullContextException, NullSensorManagerException {
        if (instance == null) {
            instance = new TremorMonitor(appContext);
        }

        return instance;
    }

    public void start() {
        sensorManager.registerListener(this,
                sensorAccelerometer,
                SENSOR_DELAY_MICROSECONDS,
                SENSOR_DELAY_MICROSECONDS);
//        sensorManager.registerListener(this, sensorGyroscope, SENSOR_DELAY_MICROSECONDS, SENSOR_DELAY_MICROSECONDS);
    }

    private void processAccelerometerData(ArrayList<Double> xAcceleration,
                                          ArrayList<Double> yAcceleration,
                                          ArrayList<Double> zAcceleration,
                                          long startTimestamp,
                                          long endTimestamp) {
        Log.d(LOG_TAG, "X accelerometer (" + xAcceleration.size() + "): " + xAcceleration.toString());
        Log.d(LOG_TAG, "Y accelerometer (" + yAcceleration.size() + "): " + yAcceleration.toString());
        Log.d(LOG_TAG, "Z accelerometer (" + zAcceleration.size() + "): " + zAcceleration.toString());

        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

        Double[] xAcc = xAcceleration.toArray(new Double[0]);
        Complex[] xAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xAcc), FFT_WINDOW_SIZE), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "X dominant frequency: " + xDominantFrequency);

        Double[] yAcc = yAcceleration.toArray(new Double[0]);
        Complex[] yAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yAcc), FFT_WINDOW_SIZE), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "Y dominant frequency: " + yDominantFrequency);

        Double[] zAcc = zAcceleration.toArray(new Double[0]);
        Complex[] zAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zAcc), FFT_WINDOW_SIZE), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zAccFFT, SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "Z dominant frequency: " + zDominantFrequency);

        if (xDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || yDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || zDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ) {
            Log.d(LOG_TAG, "Tremor detected");
            tremorDatabase.tremorRecordDao().insert(new TremorRecord(startTimestamp, endTimestamp, TremorSeverity.TREMOR_SEVERITY_5));
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
        detectionSampleTimer.cancel();
        detectionSampleTimer.purge();
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
