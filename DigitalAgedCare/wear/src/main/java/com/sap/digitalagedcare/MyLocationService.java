package com.sap.digitalagedcare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyLocationService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private static final String TAG = "MyLocationService";
    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "channel_01";
    private static final String CHANEL_NAME="My Channel";
    private GPSLocationTracking gpsLocationTracking;

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        startForeground(NOTIFICATION_ID, getNotification());
        gpsLocationTracking = new GPSLocationTracking(this);
        gpsLocationTracking.createLocationRequest();
        gpsLocationTracking.startLocationUpdates();

    }

    /**
     *Build a notification appears when the app in background to inform the user about the
     * foreground services.
     */
    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID);
        return builder.build();
    }
    public class LocationServiceBinder extends Binder {
        public MyLocationService getService() {
            return MyLocationService.this;
        }
    }
}
