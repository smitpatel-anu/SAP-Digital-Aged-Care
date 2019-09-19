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

    private static final int SAMPLING_PERIOD_MILLISECONDS = 60000;
    private static final int SENSOR_DELAY_MICROSECONDS = 4000;
    private static final int MOVING_AVERAGE_FILTER_NUM_POINTS = 10;

    private static final int TREMOR_THRESHOLD_FREQUENCY_HERTZ = 3; // > 3 Hz is categorized as a tremor

    private static final int SAMPLING_RATE_HERTZ = MICROSECONDS_PER_SECOND / (SENSOR_DELAY_MICROSECONDS * MOVING_AVERAGE_FILTER_NUM_POINTS); // samples per second (Hz)

    private static final int NUM_DATA_POINTS_PER_SAMPLE = (SAMPLING_PERIOD_MILLISECONDS / MILLISECONDS_PER_SECOND) * SAMPLING_RATE_HERTZ;
    // find nearest power of 2 greater than number of data points per sample
    private static final int FFT_NUM_DATA_POINTS = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(NUM_DATA_POINTS_PER_SAMPLE - 1));

    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGyroscope;
    private final TremorDatabase tremorDatabase;

    private long sampleStartTimestamp;
    private long sampleEndTimestamp;

    private final Timer detectionSampleTimer;

    private final ArrayList<Double> xAcceleration = new ArrayList<>();
    private final ArrayList<Double> yAcceleration = new ArrayList<>();
    private final ArrayList<Double> zAcceleration = new ArrayList<>();

    private final ArrayList<Double> xRotation = new ArrayList<>();
    private final ArrayList<Double> yRotation = new ArrayList<>();
    private final ArrayList<Double> zRotation = new ArrayList<>();

    private final ArrayList<Double> acceleration = new ArrayList<>();
    private final ArrayList<Double> rotation = new ArrayList<>();

    private static TremorMonitor instance = null;

    private TremorMonitor(Context appContext) throws NullContextException, NullSensorManagerException {
        if (appContext == null) {
            throw new NullContextException();
        }

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

                sampleEndTimestamp = System.currentTimeMillis();

                processAccelerometerData(new ArrayList<>(xAcceleration),
                        new ArrayList<>(yAcceleration),
                        new ArrayList<>(zAcceleration),
                        new ArrayList<>(acceleration),
                        sampleStartTimestamp,
                        sampleEndTimestamp);

                processGyroscopeData(new ArrayList<>(xRotation),
                        new ArrayList<>(yRotation),
                        new ArrayList<>(zRotation),
                        new ArrayList<>(rotation),
                        sampleStartTimestamp,
                        sampleEndTimestamp);

                sampleStartTimestamp = sampleEndTimestamp;

                xAcceleration.clear();
                yAcceleration.clear();
                zAcceleration.clear();
                acceleration.clear();

                xRotation.clear();
                yRotation.clear();
                zRotation.clear();
                rotation.clear();

            }
        }, SAMPLING_PERIOD_MILLISECONDS, SAMPLING_PERIOD_MILLISECONDS);
        sampleStartTimestamp = System.currentTimeMillis();
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

    private void printInfo() {
        Log.d(LOG_TAG, "SAMPLING_PERIOD_MILLISECONDS = " + SAMPLING_PERIOD_MILLISECONDS);
        Log.d(LOG_TAG, "SENSOR_DELAY_MICROSECONDS = " + SENSOR_DELAY_MICROSECONDS);
        Log.d(LOG_TAG, "MOVING_AVERAGE_FILTER_NUM_POINTS = " + MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "TREMOR_THRESHOLD_FREQUENCY_HERTZ = " + TREMOR_THRESHOLD_FREQUENCY_HERTZ);
        Log.d(LOG_TAG, "SAMPLING_RATE_HERTZ = " + SAMPLING_RATE_HERTZ);
        Log.d(LOG_TAG, "NUM_DATA_POINTS_PER_SAMPLE = " + NUM_DATA_POINTS_PER_SAMPLE);
        Log.d(LOG_TAG, "FFT_NUM_DATA_POINTS = " + FFT_NUM_DATA_POINTS);
    }

    public void start() {
        sensorManager.registerListener(this,
                sensorAccelerometer,
                SENSOR_DELAY_MICROSECONDS,
                SENSOR_DELAY_MICROSECONDS);
        sensorManager.registerListener(this,
                sensorGyroscope,
                SENSOR_DELAY_MICROSECONDS,
                SENSOR_DELAY_MICROSECONDS);
        printInfo();
    }

    private void processGyroscopeData(ArrayList<Double> xRotation,
                                          ArrayList<Double> yRotation,
                                          ArrayList<Double> zRotation,
                                          ArrayList<Double> rotation,
                                          long startTimestamp,
                                          long endTimestamp) {
        Log.d(LOG_TAG, "X gyroscope (" + xRotation.size() + "): " + xRotation.toString());
        Log.d(LOG_TAG, "Y gyroscope (" + yRotation.size() + "): " + yRotation.toString());
        Log.d(LOG_TAG, "Z gyroscope (" + zRotation.size() + "): " + zRotation.toString());
        Log.d(LOG_TAG, "Total rotation (" + rotation.size() + "): " + rotation.toString());

        ArrayList<Double> xRotFiltered = movingAverageFilter(xRotation, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "X rotation filtered (" + xRotFiltered.size() + "): " + xRotFiltered.toString());

        ArrayList<Double> yRotFiltered = movingAverageFilter(yRotation, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Y rotation filtered (" + yRotFiltered.size() + "): " + yRotFiltered.toString());

        ArrayList<Double> zRotFiltered = movingAverageFilter(zRotation, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Z rotation filtered (" + zRotFiltered.size() + "): " + zRotFiltered.toString());

        ArrayList<Double> rotFiltered = movingAverageFilter(rotation, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Gyroscope filtered (" + rotFiltered.size() + "): " + rotFiltered.toString());

        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

        Double[] rotFilteredArray = rotFiltered.toArray(new Double[0]);
        Complex[] rotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(rotFilteredArray), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double dominantFrequency = getDominantFrequency(rotFFT);
        Log.d(LOG_TAG, "gyro dominant frequency: " + dominantFrequency);

        Double[] xRot = xRotFiltered.toArray(new Double[0]);
        Complex[] xRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xRot), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xRotFFT);
        Log.d(LOG_TAG, "X gyro dominant frequency: " + xDominantFrequency);

        Double[] yRot = xRotFiltered.toArray(new Double[0]);
        Complex[] yRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yRot), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yRotFFT);
        Log.d(LOG_TAG, "Y gyro dominant frequency: " + yDominantFrequency);

        Double[] zRot = xRotFiltered.toArray(new Double[0]);
        Complex[] zRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zRot), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zRotFFT);
        Log.d(LOG_TAG, "Z gyro dominant frequency: " + zDominantFrequency);

        if (dominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || xDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || yDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || zDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ) {
            Log.d(LOG_TAG, "Tremor detected");
            tremorDatabase.tremorRecordDao().insert(new TremorRecord(startTimestamp, endTimestamp, TremorSeverity.TREMOR_SEVERITY_5));
        }
    }

    private void processAccelerometerData(ArrayList<Double> xAcceleration,
                                          ArrayList<Double> yAcceleration,
                                          ArrayList<Double> zAcceleration,
                                          ArrayList<Double> acceleration,
                                          long startTimestamp,
                                          long endTimestamp) {
        Log.d(LOG_TAG, "X accelerometer (" + xAcceleration.size() + "): " + xAcceleration.toString());
        Log.d(LOG_TAG, "Y accelerometer (" + yAcceleration.size() + "): " + yAcceleration.toString());
        Log.d(LOG_TAG, "Z accelerometer (" + zAcceleration.size() + "): " + zAcceleration.toString());
        Log.d(LOG_TAG, "Total acceleration (" + acceleration.size() + "): " + acceleration.toString());

        ArrayList<Double> xAccFiltered = movingAverageFilter(xAcceleration, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "X accelerometer filtered (" + xAccFiltered.size() + "): " + xAccFiltered.toString());

        ArrayList<Double> yAccFiltered = movingAverageFilter(yAcceleration, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Y accelerometer filtered (" + yAccFiltered.size() + "): " + yAccFiltered.toString());

        ArrayList<Double> zAccFiltered = movingAverageFilter(zAcceleration, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Z accelerometer filtered (" + zAccFiltered.size() + "): " + zAccFiltered.toString());

        ArrayList<Double> accFiltered = movingAverageFilter(acceleration, MOVING_AVERAGE_FILTER_NUM_POINTS);
        Log.d(LOG_TAG, "Accelerometer filtered (" + accFiltered.size() + "): " + accFiltered.toString());

        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

        Double[] accFilteredArray = accFiltered.toArray(new Double[0]);
        Complex[] accFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(accFilteredArray), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double dominantFrequency = getDominantFrequency(accFFT);
        Log.d(LOG_TAG, "acc dominant frequency: " + dominantFrequency);

        Double[] xAcc = xAccFiltered.toArray(new Double[0]);
        Complex[] xAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xAcc), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xAccFFT);
        Log.d(LOG_TAG, "X acc dominant frequency: " + xDominantFrequency);

        Double[] yAcc = yAccFiltered.toArray(new Double[0]);
        Complex[] yAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yAcc), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yAccFFT);
        Log.d(LOG_TAG, "Y acc dominant frequency: " + yDominantFrequency);

        Double[] zAcc = zAccFiltered.toArray(new Double[0]);
        Complex[] zAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zAcc), FFT_NUM_DATA_POINTS), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zAccFFT);
        Log.d(LOG_TAG, "Z acc dominant frequency: " + zDominantFrequency);

        if (dominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || xDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || yDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ
                || zDominantFrequency >= TREMOR_THRESHOLD_FREQUENCY_HERTZ) {
            Log.d(LOG_TAG, "Tremor detected");
            tremorDatabase.tremorRecordDao().insert(new TremorRecord(startTimestamp, endTimestamp, TremorSeverity.TREMOR_SEVERITY_5));
        }
    }

    private ArrayList<Double> movingAverageFilter(ArrayList<Double> inputData, int numPoints) {
        if (inputData == null) {
            return null;
        }

        ArrayList<Double> resultData = new ArrayList<Double>();

        for (int resultDataIndex = 0; resultDataIndex < (inputData.size() / numPoints); resultDataIndex++) {
            double sum = 0;
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                sum += inputData.get(resultDataIndex + pointIndex);
            }
            resultData.add(sum / numPoints);
        }

        return resultData;
    }

    private double getDominantFrequency(Complex[] fftResult) {
        if (fftResult == null) {
            return 0;
        }

        double[] amplitudes = getAmplitudesFromFFT(fftResult);
        if (amplitudes != null) {
            amplitudes[0] = 0; // fftResult[0] is the offset, zero it before getting index of max
        }

        int indexOfMaximum = getIndexOfMaximum(amplitudes);
        if (indexOfMaximum < 0) {
            return 0;
        }

        return ((double) indexOfMaximum * SAMPLING_RATE_HERTZ) / (double) (fftResult.length);
    }

    private int getIndexOfMaximum(double[] arr) {
        if (arr == null || arr.length == 0) {
            return -1;
        }

        int maxIndex = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] >= arr[maxIndex]) {
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    private double getVectorMagnitude(double x, double y, double z) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    private double[] getAmplitudesFromFFT(Complex[] fftResult) {
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
        sensorManager.unregisterListener(this, sensorGyroscope);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            return;
        }

        if (event.sensor.getType() == TYPE_LINEAR_ACCELERATION) {
            xAcceleration.add((double) event.values[0]);
            yAcceleration.add((double) event.values[1]);
            zAcceleration.add((double) event.values[2]);
            acceleration.add(getVectorMagnitude(event.values[0], event.values[1], event.values[2]));
        } else if (event.sensor.getType() == TYPE_GYROSCOPE) {
            xRotation.add((double) event.values[0]);
            yRotation.add((double) event.values[1]);
            zRotation.add((double) event.values[2]);
            rotation.add(getVectorMagnitude(event.values[0], event.values[1], event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: handle sensor accuracy changes
    }
}
