package com.sap.digitalagedcare;

/**
 * Fall detection feature - User interface of data demonstration and testing
 *
 * Authors:         Jinpei Chen
 *                  Yuzhao Li
 *
 * Created data:    02/09/2019
 * Last modified:   03/10/2019
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sap.shared.FallDetection_KNN;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FallDetectionActivity_ML extends AppCompatActivity implements SensorEventListener {

    private SensorManager mySensorManager;
    private Sensor myAccelerometer;
    private Sensor myGyroscope;

    private final static String TAG = FallDetectionActivity_ML.class.getSimpleName();
    private TextView xAcc, yAcc, zAcc, tAcc;
    private TextView xGyro, yGyro, zGyro, tGyro;

    private LineChart myChart;
    private Thread thread;
    private boolean plotData = true;

    private boolean bp = true;

    private int peak_count = 0;
    private boolean peak = false;

    private int MAX_ENTRY = 30;

    // lists for storing training and testing datasets label and features.
    private List<Float[]> trainfeatures = new ArrayList<>();
    private List<Float> trainlabel = new ArrayList<>();

    /* A LinkedList is used to store collected data, each piece of data is
     * an array of 2 elements: value and timestamp
     */
    private LinkedList<Float> data = new LinkedList<>();
    private LinkedList<Float> time = new LinkedList<>();
    private LinkedList<Float> dtime = new LinkedList<>();

    /* Call when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
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
        // Normal: 5Hz 200ms, UI: 16.7Hz 60ms, Game: 50Hz 20ms, Fastest: >100Hz, 0ms
        mySensorManager.registerListener(FallDetectionActivity_ML.this, myAccelerometer, mySensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, "onCreate: Accelerometer listener is registered.");
        mySensorManager.registerListener(FallDetectionActivity_ML.this, myGyroscope, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Gyroscope listener is registered.");



        // assign TextViews
        xAcc = (TextView) findViewById(R.id.textViewX);
        yAcc = (TextView) findViewById(R.id.textViewY);
        zAcc = (TextView) findViewById(R.id.textViewZ);
        tAcc = (TextView) findViewById(R.id.textViewAcc);
        xGyro = (TextView) findViewById(R.id.textViewGyroX);
        yGyro = (TextView) findViewById(R.id.textViewGyroY);
        zGyro = (TextView) findViewById(R.id.textViewGyroZ);
        tGyro = (TextView) findViewById(R.id.textViewGyro);

        // initialize LineChart
        myChart = (LineChart) findViewById(R.id.lineChart);
        myChart.getDescription().setEnabled(true);
        myChart.getDescription().setText("Real Time Accelerometer Data");
        myChart.getDescription().setTextSize(15f);

        myChart.setTouchEnabled(false);
        myChart.setDragEnabled(false);
        myChart.setScaleEnabled(false);
        myChart.setDrawGridBackground(false);
        myChart.setPinchZoom(false);
        myChart.setBackgroundColor(Color.LTGRAY);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);
        myChart.setData(lineData);

        // get the legend
        Legend l = myChart.getLegend();
        // modify the legend
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = myChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setTextSize(15f);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        YAxis yl = myChart.getAxisLeft();
        yl.setTextColor(Color.BLACK);
        yl.setTextSize(20f);
        yl.setDrawGridLines(false);
        yl.setAxisMaximum(20f);
        yl.setAxisMinimum(0f);
        yl.setDrawGridLines(true);

        YAxis yr = myChart.getAxisRight();
        yr.setEnabled(false);

        myChart.getAxisLeft().setDrawGridLines(false);
        myChart.getXAxis().setDrawGridLines(false);
        myChart.setDrawBorders(false);

        // Start to plot
        startPlot();
    }

    // loading training data and extracting features and label for training dataset
    private void loadtrainData(String filename) throws IOException {

//        File file = new File("./app/src/main/java/KNN/fall_data_101.csv");
        InputStream input = getAssets().open("fall_data_101.csv");
        InputStreamReader inputreader = new InputStreamReader(input);

        try {
            BufferedReader readFile = new BufferedReader(inputreader);
            String line;
            while ((line = readFile.readLine()) != null) {
                String[] split = line.split(",");
                Float[] values = new Float[split.length - 1];
                for (int i = 0; i < split.length-1; i++)
                    values[i] = Float.parseFloat(split[i+1]);
                trainfeatures.add(values);
                trainlabel.add(Float.parseFloat(split[0]));
            }
            readFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void startPlot(){
        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
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

            xAcc.setText("X: " + xA);
            yAcc.setText("Y: " + yA);
            zAcc.setText("Z: " + zA);
            tAcc.setText("Acc: " + acc);

            // record data
            float t = System.nanoTime()/1000000;
            float d = acc;
            if (time.size()>0)  {
                dtime.add(t - time.peekLast());
            }else{
                dtime.add(t);
            }
            data.add(d);
            time.add(t);
            if (data.size()>100){
                data.pop();
                time.pop();
                dtime.pop();
            }

//            Log.d(TAG, "onSensorChanged: value: "+ data.toString() + "time: " + time.toString());
            System.out.println("onSensorChanged: value: "+ data.toString() + "time: " + time.toString() + " dtime: "+dtime.toString());

            // free fall event
//            if (acc<1f && bp) {
//                bp = false;
//                beep();
//                Toast.makeText(FallDetectionActivity2.this, R.string.freefall,Toast.LENGTH_SHORT).show();
//                openDialog();
//            }

            if (acc>3f && !bp) {
                bp = true;
            }

            if (acc>15f && !peak) {
                peak = true;
            }
            if (peak) {
                peak_count++;
                if (peak_count>=51){
                    peak = false;
                    peak_count = 0;
                    FallDetection_KNN k = new FallDetection_KNN(data, trainfeatures, trainlabel);
                    int fall = k.wrappingUp();
                    if (fall==1){
                        openDialog();
                    }
                }
            }

            if (plotData){
                addEntry(acc);
                plotData = false;
            }
        }

        // collect Gyroscope data
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            Log.d(TAG, "onSensorChanged: ||| X:" + event.values[0] + ", Y:" + event.values[1] + ", Z:" + event.values[2]);
            DecimalFormat df = new DecimalFormat("#.#");
            float xG = Float.valueOf(df.format(event.values[0]));
            float yG = Float.valueOf(df.format(event.values[1]));
            float zG = Float.valueOf(df.format(event.values[2]));
            gyro = Float.valueOf(df.format((float) Math.sqrt(xG*xG + yG*yG + zG*zG)));

            xGyro.setText("X: " + xG);
            yGyro.setText("Y: " + yG);
            zGyro.setText("Z: " + zG);
            tGyro.setText("Gyro: " + gyro);
        }


    }

    public void openDialog() {
//        FallDialog fd = new FallDialog();
//        fd.show(getSupportFragmentManager(), "alert dialog");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Fall detected, send message to emergency contact?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Add positive button action code here
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            private static final int AUTO_DISMISS_MILLIS = 6000;
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                final CharSequence negativeButtonText = defaultButton.getText();
                new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        defaultButton.setText(String.format(
                                Locale.getDefault(), "%s (%d)",
                                negativeButtonText,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                        ));
                    }
                    @Override
                    public void onFinish() {
                        if (((AlertDialog) dialog).isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }.start();
            }
        });
        dialog.show();
    }

    private void addEntry(float f){
        LineData lineData = myChart.getData();

        if (lineData != null){
            LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);

            if (set == null){
                set = createSet();
                lineData.addDataSet(set);
            }

            // add entries of xValues to the data
            lineData.addEntry(new Entry(set.getEntryCount(), f), 0);
            if (set.getEntryCount() == MAX_ENTRY){
                set.removeFirst();
                for (Entry entry : set.getValues() )
                    entry.setX(entry.getX() - 1);
            }

            // enable the chart know when its data has changed
            lineData.notifyDataChanged();
            myChart.notifyDataSetChanged();
            myChart.setMaxVisibleValueCount(1500);
            myChart.moveViewToX(lineData.getEntryCount());
        }
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "dynamic data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(4f);
        set.setColor(Color.DKGRAY);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircleHole(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    protected void onDestroy() {
        mySensorManager.unregisterListener(FallDetectionActivity_ML.this);
        thread.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mySensorManager.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null){
            thread.interrupt();
        }
        mySensorManager.unregisterListener(this);
    }

    protected void beep(){
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }
}
