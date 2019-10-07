package com.sap.digitalagedcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sap.shared.NullContextException;
import com.sap.shared.TremorMonitor;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button tremorTestActivityButton = (Button)findViewById(R.id.tremorTestActivityButton);

        tremorTestActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TremorTestActivity.class));
            }
        });

        button = findViewById(R.id.b);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openActivity();
            }
        });
    }

    public void openActivity(){
        Intent intent = new Intent(this, FallDetectionActivity.class);
        startActivity(intent);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        try {
//            TremorMonitor.getInstance(this.getApplicationContext()).start();
//        } catch (NullContextException e) {
//            Log.e(LOG_TAG, "Failed to start tremor monitoring, " + e.getMessage());
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        try {
//            TremorMonitor.getInstance(this.getApplicationContext()).stop();
//        } catch (NullContextException e) {
//            Log.e(LOG_TAG, "Failed to stop tremor monitoring, " + e.getMessage());
//        }
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        try {
//            TremorMonitor.getInstance(this.getApplicationContext()).start();
//        } catch (NullContextException e) {
//            Log.e(LOG_TAG, "Failed to start tremor monitoring, " + e.getMessage());
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        try {
//            TremorMonitor.getInstance(this.getApplicationContext()).stop();
//        } catch (NullContextException e) {
//            Log.e(LOG_TAG, "Failed to stop tremor monitoring, " + e.getMessage());
//        }
//    }
}
