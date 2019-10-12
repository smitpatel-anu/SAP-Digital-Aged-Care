package com.sap.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Start repeating alarm to periodically send tremor data
            try {
                TremorMonitor.getInstance(context.getApplicationContext()).startRepeatingTremorAlarm();
            } catch (NullContextException e) {
                e.printStackTrace();
            }
        }
    }
}
