package com.sap.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TremorRequestAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            TremorMonitor.getInstance(context.getApplicationContext()).sendTremorData();
        } catch (NullContextException e) {
            e.printStackTrace();
        }
    }
}
