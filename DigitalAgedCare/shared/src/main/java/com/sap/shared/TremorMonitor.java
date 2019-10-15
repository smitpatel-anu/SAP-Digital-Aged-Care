package com.sap.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
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
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GRAVITY;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

@SuppressWarnings("all")
public class TremorMonitor implements SensorEventListener {
    private static final String LOG_TAG = "TremorMonitor";

    private static final int MICROSECONDS_PER_SECOND = 1000000;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    private static final int SAMPLING_PERIOD_MILLISECONDS = 2000;
    private static final int SENSOR_DELAY_MICROSECONDS = 5000;
    private static final int MOVING_AVERAGE_FILTER_NUM_POINTS = 1;
    private static final float LOW_PASS_FILTER_ALPHA = 0f;

    private static final int TREMOR_THRESHOLD_FREQUENCY_HERTZ = 3; // > 3 Hz is categorized as a tremor

    private static final int SAMPLING_RATE_HERTZ = MICROSECONDS_PER_SECOND / (SENSOR_DELAY_MICROSECONDS * MOVING_AVERAGE_FILTER_NUM_POINTS); // samples per second (Hz)

    private static final int NUM_DATA_POINTS_PER_SAMPLE = (SAMPLING_PERIOD_MILLISECONDS / MILLISECONDS_PER_SECOND) * SAMPLING_RATE_HERTZ;
    // find nearest power of 2 greater than number of data points per sample
    private static final int FFT_SIZE = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(NUM_DATA_POINTS_PER_SAMPLE - 1));

    private static final long TREMOR_DATA_REQUEST_REPEATING_ALARM_INTERVAL = 2 * AlarmManager.INTERVAL_HOUR;

    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;
    private final Sensor sensorGravity;
    private final Sensor sensorGyroscope;
    private final TremorDatabase tremorDatabase;

    private long sampleStartTimestampMillis;
    private long sampleEndTimestampMillis;

    private final Timer detectionSampleTimer;

    private final ArrayList<Double> xAcceleration = new ArrayList<>();
    private final ArrayList<Double> yAcceleration = new ArrayList<>();
    private final ArrayList<Double> zAcceleration = new ArrayList<>();

    private final ArrayList<Double> xRotation = new ArrayList<>();
    private final ArrayList<Double> yRotation = new ArrayList<>();
    private final ArrayList<Double> zRotation = new ArrayList<>();

    private final ArrayList<Double> acceleration = new ArrayList<>();
    private final ArrayList<Double> rotation = new ArrayList<>();

    private PendingIntent tremorDataRequestPendingIntent;
    private AlarmManager alarmManager;

    private WeakReference<Context> appContext;

    private static TremorMonitor instance = null;

    private boolean monitorIsRunning = false;

    private TremorMonitor(Context applicationContext) throws NullContextException, NullSensorManagerException {
        if (applicationContext == null) {
            throw new NullContextException();
        }

        this.appContext = new WeakReference<Context>(applicationContext);

        this.tremorDatabase = Room.databaseBuilder(appContext.get(),
                TremorDatabase.class,
                "tremor-database").build();

        sensorManager = (SensorManager) appContext.get().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            throw new NullSensorManagerException();
        }

        sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        sensorGravity = sensorManager.getDefaultSensor(TYPE_GRAVITY);
        sensorGyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

        detectionSampleTimer = new Timer();
    }

    protected void sendTremorData() {
        Log.d(LOG_TAG, "Sending tremor data...");
    }

    protected void startRepeatingTremorAlarm() {
        Intent tremorDataRequestAlarmIntent = new Intent(appContext.get(), TremorRequestAlarmReceiver.class);
        tremorDataRequestPendingIntent = PendingIntent.getBroadcast(appContext.get(), 0, tremorDataRequestAlarmIntent, 0);
        alarmManager = (AlarmManager) appContext.get().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 10000,
                10000, tremorDataRequestPendingIntent);
    }

    private TremorSeverity calculateTremorSeverity(double frequency) {
        if (frequency >= 7) {
            return TremorSeverity.TREMOR_SEVERITY_5;
        } else if (frequency >= 6) {
            return TremorSeverity.TREMOR_SEVERITY_4;
        } else if (frequency >= 5) {
            return TremorSeverity.TREMOR_SEVERITY_3;
        } else if (frequency >= 4) {
            return TremorSeverity.TREMOR_SEVERITY_2;
        } else if (frequency >= 3) {
            return TremorSeverity.TREMOR_SEVERITY_1;
        }

        return TremorSeverity.TREMOR_SEVERITY_0;
    }

    public TremorDatabase getTremorDatabase() {
        return tremorDatabase;
    }

    public TremorRecord getMostRecentTremorRecord() {
        return tremorDatabase.tremorRecordDao().getMostRecentRecord();
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
        Log.d(LOG_TAG, "FFT_SIZE = " + FFT_SIZE);
    }

    public void start() {
        if (monitorIsRunning) {
            return;
        }

        sendTremorData();
        startRepeatingTremorAlarm();

//        sensorManager.registerListener(this,
//                sensorAccelerometer,
//                SENSOR_DELAY_MICROSECONDS);
//        sensorManager.registerListener(this,
//                sensorGyroscope,
//                SENSOR_DELAY_MICROSECONDS);
        sensorManager.registerListener(this,
                sensorAccelerometer,
                0);
        sensorManager.registerListener(this,
                sensorGravity,
                0);
//        sensorManager.registerListener(this,
//                sensorGyroscope,
//                0);

        sampleStartTimestampMillis = System.currentTimeMillis();
        detectionSampleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sampleEndTimestampMillis = System.currentTimeMillis();

                Log.d(LOG_TAG, "Average Accelerometer Sensor Delay: " + averageAccSensorDelayMicros / 1000);
                Log.d(LOG_TAG, "Average Gyroscope Sensor Delay: " + averageGyroSensorDelayMicros / 1000);

                if ((xAcceleration.size() < 0.25 * NUM_DATA_POINTS_PER_SAMPLE)
                        || (yAcceleration.size() < 0.25 * NUM_DATA_POINTS_PER_SAMPLE)
                        || (zAcceleration.size() < 0.25 * NUM_DATA_POINTS_PER_SAMPLE)) {
                    tremorDatabase.tremorRecordDao().insert(new TremorRecord(sampleStartTimestampMillis,
                            sampleEndTimestampMillis,
                            TremorSeverity.TREMOR_SEVERITY_0));
                    sampleStartTimestampMillis = sampleEndTimestampMillis;
                    return;
                }

                if (xAcceleration.isEmpty() && yAcceleration.isEmpty() && zAcceleration.isEmpty() && acceleration.isEmpty()) {
                    tremorDatabase.tremorRecordDao().insert(new TremorRecord(sampleStartTimestampMillis,
                            sampleEndTimestampMillis,
                            TremorSeverity.TREMOR_SEVERITY_0));
                    sampleStartTimestampMillis = sampleEndTimestampMillis;
                    return;
                }
//                if(xRotation.isEmpty() && yRotation.isEmpty() && zRotation.isEmpty() && rotation.isEmpty()) {
//                    tremorDatabase.tremorRecordDao().insert(new TremorRecord(sampleStartTimestampMillis,
//                            sampleEndTimestampMillis,
//                            TremorSeverity.TREMOR_SEVERITY_0));
//                    sampleStartTimestampMillis = sampleEndTimestampMillis;
//                    return;
//                }

                double accelerometerDominantFrequency = getAccelerometerDominantFrequency(new ArrayList<>(xAcceleration),
                        new ArrayList<>(yAcceleration),
                        new ArrayList<>(zAcceleration),
                        new ArrayList<>(acceleration),
                        sampleStartTimestampMillis,
                        sampleEndTimestampMillis);

//                double gyroscopeDominantFrequency = getGyroscopeDominantFrequency(new ArrayList<>(xRotation),
//                        new ArrayList<>(yRotation),
//                        new ArrayList<>(zRotation),
//                        new ArrayList<>(rotation),
//                        sampleStartTimestampMillis,
//                        sampleEndTimestampMillis);


//                double maxDominantFrequency = Math.max(accelerometerDominantFrequency, gyroscopeDominantFrequency);
                double maxDominantFrequency = Math.max(accelerometerDominantFrequency, 0);
                TremorSeverity tremorSeverity = calculateTremorSeverity(maxDominantFrequency);
                tremorDatabase.tremorRecordDao().insert(new TremorRecord(sampleStartTimestampMillis,
                        sampleEndTimestampMillis,
                        tremorSeverity));

                sampleStartTimestampMillis = sampleEndTimestampMillis;

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
        printInfo();
    }

    private double getGyroscopeDominantFrequency(ArrayList<Double> xRotation,
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
        Complex[] rotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(rotFilteredArray), FFT_SIZE), TransformType.FORWARD);
        double dominantFrequency = getDominantFrequency(rotFFT);
        Log.d(LOG_TAG, "gyroscope dominant frequency: " + dominantFrequency);

        Double[] xRot = xRotFiltered.toArray(new Double[0]);
        Complex[] xRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xRot), FFT_SIZE), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xRotFFT);
        Log.d(LOG_TAG, "X gyroscope dominant frequency: " + xDominantFrequency);

        Double[] yRot = xRotFiltered.toArray(new Double[0]);
        Complex[] yRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yRot), FFT_SIZE), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yRotFFT);
        Log.d(LOG_TAG, "Y gyroscope dominant frequency: " + yDominantFrequency);

        Double[] zRot = xRotFiltered.toArray(new Double[0]);
        Complex[] zRotFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zRot), FFT_SIZE), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zRotFFT);
        Log.d(LOG_TAG, "Z gyroscope dominant frequency: " + zDominantFrequency);

        return Collections.max(Arrays.asList(dominantFrequency, xDominantFrequency, yDominantFrequency, zDominantFrequency));
    }

    private double getAccelerometerDominantFrequency(ArrayList<Double> xAcceleration,
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
        Complex[] accFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(accFilteredArray), FFT_SIZE), TransformType.FORWARD);
        double dominantFrequency = getDominantFrequency(accFFT);
        Log.d(LOG_TAG, "acc dominant frequency: " + dominantFrequency);

        Double[] xAcc = xAccFiltered.toArray(new Double[0]);
        Complex[] xAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(xAcc), FFT_SIZE), TransformType.FORWARD);
        double xDominantFrequency = getDominantFrequency(xAccFFT);

        Double[] yAcc = yAccFiltered.toArray(new Double[0]);
        Complex[] yAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(yAcc), FFT_SIZE), TransformType.FORWARD);
        double yDominantFrequency = getDominantFrequency(yAccFFT);

        Double[] zAcc = zAccFiltered.toArray(new Double[0]);
        Complex[] zAccFFT = fastFourierTransformer.transform(Arrays.copyOf(ArrayUtils.toPrimitive(zAcc), FFT_SIZE), TransformType.FORWARD);
        double zDominantFrequency = getDominantFrequency(zAccFFT);

        Log.d(LOG_TAG, "X acc dominant frequency: " + xDominantFrequency);
        Log.d(LOG_TAG, "Y acc dominant frequency: " + yDominantFrequency);
        Log.d(LOG_TAG, "Z acc dominant frequency: " + zDominantFrequency);

//        return dominantFrequency;
        return Collections.max(Arrays.asList(dominantFrequency, xDominantFrequency, yDominantFrequency, zDominantFrequency));
    }

    private ArrayList<Double> movingAverageFilter(ArrayList<Double> inputData, int numPoints) {
        if (inputData == null || numPoints == 1) {
            return inputData;
        }

        ArrayList<Double> resultData = new ArrayList<Double>();

        for (int resultDataIndex = 0; resultDataIndex < (inputData.size() / numPoints); resultDataIndex++) {
            double sum = 0;
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                sum += inputData.get(resultDataIndex + pointIndex);
            }
            resultData.add(sum / (double) numPoints);
        }

        return resultData;
    }

    private double getDominantFrequency(Complex[] fftResult) {
        if (fftResult == null) {
            return 0;
        }

        double[] magnitudes = getMagnitudesFromFFT(fftResult);
        if (magnitudes != null) {
            magnitudes[0] = 0; // fftResult[0] is the offset, zero it before getting index of max
        }

        int indexOfMaximum = getIndexOfMaximum(magnitudes);
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
        sensorManager.unregisterListener(this, sensorGyroscope);
        monitorIsRunning = false;
        instance = null;
    }

    private long prevAccEventTimestampNanos;
    private long averageAccSensorDelayMicros;
    private long numAccSamples;

    private long prevGyroEventTimestampNanos;
    private long averageGyroSensorDelayMicros;
    private long numGyroSamples;

    private float gravityX;
    private float gravityY;
    private float gravityZ;

    private float[] accFiltered;

    protected float[] lowPassFilter(float[] prev, float[] curr) {
        if (curr == null) return prev;

        for (int i = 0; i < prev.length; i++) {
            curr[i] = curr[i] + LOW_PASS_FILTER_ALPHA * (prev[i] - curr[i]);
        }
        return curr;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            return;
        }

        if (event.sensor.getType() == TYPE_ACCELEROMETER) {
            accFiltered = lowPassFilter(event.values.clone(), accFiltered);

            final float alpha = 0.9f;

            // Isolate the force of gravity with the low-pass filter.
            float gravX = alpha * gravityX + (1 - alpha) * accFiltered[0];
            float gravY = alpha * gravityY + (1 - alpha) * accFiltered[1];
            float gravZ = alpha * gravityZ + (1 - alpha) * accFiltered[2];

            // Remove the gravity contribution with the high-pass filter.
            float linearAccelerationX = accFiltered[0] - gravX;
            float linearAccelerationY = accFiltered[1] - gravY;
            float linearAccelerationZ = accFiltered[2] - gravZ;

//            xAcceleration.add((double) linearAccelerationX);
//            yAcceleration.add((double) linearAccelerationY);
//            zAcceleration.add((double) linearAccelerationZ);

            xAcceleration.add((double) event.values[0]);
            yAcceleration.add((double) event.values[1]);
            zAcceleration.add((double) event.values[2]);

//            xAcceleration.add((double) accFiltered[0]);
//            yAcceleration.add((double) accFiltered[1]);
//            zAcceleration.add((double) accFiltered[2]);

//            acceleration.add(getVectorMagnitude(linearAccelerationX, linearAccelerationY, linearAccelerationZ));
            acceleration.add(getVectorMagnitude(event.values[0], event.values[1], event.values[2]));
//            acceleration.add(getVectorMagnitude(accFiltered[0], accFiltered[1], accFiltered[2]));

            if (prevAccEventTimestampNanos == 0) {
                prevAccEventTimestampNanos = event.timestamp;
            } else {
                averageAccSensorDelayMicros = (numAccSamples * averageAccSensorDelayMicros + (event.timestamp - prevAccEventTimestampNanos)) / (numAccSamples + 1);
                prevAccEventTimestampNanos = event.timestamp;
            }
            numAccSamples++;
        } else if (event.sensor.getType() == TYPE_GRAVITY) {
            gravityX = event.values[0];
            gravityY = event.values[1];
            gravityZ = event.values[2];
        } else if (event.sensor.getType() == TYPE_GYROSCOPE) {
            xRotation.add((double) event.values[0]);
            yRotation.add((double) event.values[1]);
            zRotation.add((double) event.values[2]);
            rotation.add(getVectorMagnitude(event.values[0], event.values[1], event.values[2]));


            if (prevGyroEventTimestampNanos == 0) {
                prevGyroEventTimestampNanos = event.timestamp;
            } else {
                averageGyroSensorDelayMicros = (numGyroSamples * averageGyroSensorDelayMicros + (event.timestamp - prevGyroEventTimestampNanos)) / (numGyroSamples + 1);
                prevGyroEventTimestampNanos = event.timestamp;
            }
            numGyroSamples++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: handle sensor accuracy changes
    }
}
