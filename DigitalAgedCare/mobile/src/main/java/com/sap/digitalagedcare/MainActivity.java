package com.sap.digitalagedcare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.sap.shared.NullContextException;
import com.sap.shared.TremorMonitor;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            TremorMonitor.getInstance(this.getApplicationContext()).start();
        } catch (NullContextException e) {
            Log.e(LOG_TAG, "Failed to start tremor monitoring, " + e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            TremorMonitor.getInstance(this.getApplicationContext()).stop();
        } catch (NullContextException e) {
            Log.e(LOG_TAG, "Failed to stop tremor monitoring, " + e.getMessage());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            TremorMonitor.getInstance(this.getApplicationContext()).start();
        } catch (NullContextException e) {
            Log.e(LOG_TAG, "Failed to start tremor monitoring, " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            TremorMonitor.getInstance(this.getApplicationContext()).stop();
        } catch (NullContextException e) {
            Log.e(LOG_TAG, "Failed to stop tremor monitoring, " + e.getMessage());
        }
    }
}
