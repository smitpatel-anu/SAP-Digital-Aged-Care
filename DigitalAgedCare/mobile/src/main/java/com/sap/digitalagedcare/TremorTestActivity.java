package com.sap.digitalagedcare;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sap.shared.NullContextException;
import com.sap.shared.TremorDatabase;
import com.sap.shared.TremorMonitor;
import com.sap.shared.TremorRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TremorTestActivity extends AppCompatActivity {

    private static final String LOG_TAG = "TremorTestActivity";

    private TextView tremorActivityLogTextView;
    private TremorRecord currentTremorRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tremor_test);

        tremorActivityLogTextView = (TextView) findViewById(R.id.tremorActivityLogTextView);

        Button clearTremorActivityLogButton = (Button) findViewById(R.id.clearTremorActivityLogButton);
        clearTremorActivityLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tremorActivityLogTextView.setText("");
            }
        });
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


    @Override
    protected void onResume() {
        super.onResume();

        try {
            TremorMonitor.getInstance(this.getApplicationContext()).start();
        } catch (NullContextException e) {
            Log.e(LOG_TAG, "Failed to start tremor monitoring, " + e.getMessage());
        }

        tremorActivityLogTextView.setText("Starting Tremor Monitor...");
        TremorMonitor tremorMonitor = null;
        try {
            tremorMonitor = TremorMonitor.getInstance(getApplicationContext());
        } catch (NullContextException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        final TremorMonitor finalTremorMonitor = tremorMonitor;
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                final TremorRecord mostRecentTremorRecord = finalTremorMonitor.getMostRecentTremorRecord();
                if (mostRecentTremorRecord == null) {
                    return;
                }
                if (currentTremorRecord == null) {
                    currentTremorRecord = mostRecentTremorRecord;
                } else if (currentTremorRecord.equals(mostRecentTremorRecord)) {
                    return;
                }

                currentTremorRecord = mostRecentTremorRecord;

                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String dateString = dateFormatter.format(new Date(mostRecentTremorRecord.startTimestamp));

                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
                String startTimeString = timeFormatter.format(new Date(mostRecentTremorRecord.startTimestamp));
                String endTimeString = timeFormatter.format(new Date(mostRecentTremorRecord.endTimestamp));

                String tremorSeverityString = "Tremor Severity: " + mostRecentTremorRecord.getTremorSeverity().ordinal();

                final String tremorActivityString = startTimeString + "-" + endTimeString + " - " + tremorSeverityString + "\n";

                TremorTestActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((Editable) tremorActivityLogTextView.getText()).insert(0, tremorActivityString);
                    }
                });
            }
        }, 4, 1, TimeUnit.SECONDS);
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

    public void onClickPrintTremorDatabaseButton(View view) {
        final Context appContext = this.getApplicationContext();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TremorDatabase tremorDatabase = TremorMonitor.getInstance(appContext).getTremorDatabase();
                    List<TremorRecord> tremorRecords = tremorDatabase.tremorRecordDao().getAll();
                    Log.d(LOG_TAG, "Tremor Database:");
                    for (TremorRecord tremorRecord : tremorRecords) {
                        Log.d(LOG_TAG, tremorRecord.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickResetTremorDatabaseButton(View view) {
        final Context appContext = this.getApplicationContext();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TremorDatabase tremorDatabase = TremorMonitor.getInstance(appContext).getTremorDatabase();
                    tremorDatabase.tremorRecordDao().delete();
                    Log.d(LOG_TAG, "Deleted Tremor Database");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
