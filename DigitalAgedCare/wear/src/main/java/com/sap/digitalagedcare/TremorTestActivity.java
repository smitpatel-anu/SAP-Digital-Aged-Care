package com.sap.digitalagedcare;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import com.sap.shared.NullContextException;
import com.sap.shared.TremorDatabase;
import com.sap.shared.TremorMonitor;
import com.sap.shared.TremorRecord;

import java.util.List;

public class TremorTestActivity extends WearableActivity {

    private static final String LOG_TAG = "TremorTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tremor_test);
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//        try {
//            TremorMonitor.getInstance(this.getApplicationContext()).stop();
//        } catch (NullContextException e) {
//            Log.e(LOG_TAG, "Failed to stop tremor monitoring, " + e.getMessage());
//        }
//    }


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
