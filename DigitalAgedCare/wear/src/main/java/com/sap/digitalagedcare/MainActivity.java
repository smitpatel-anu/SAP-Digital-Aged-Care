//package com.sap.digitalagedcare;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Build;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.wearable.activity.WearableActivity;
//
//import android.util.Log;
//import android.view.View;
//
//import android.view.View;
//import android.widget.Button;
//
//import android.widget.TextView;
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//
//public class MainActivity extends WearableActivity {
//
//
//    private TextView locationTv;
//    public MyLocationService locationService;
//    private static final String TAG = "MainActivity";
//    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;
//    //public static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 102;
//
//    private Button button;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
////        Button tremorTestActivityButton = (Button) findViewById(R.id.tremorTestActivityButton);
////        tremorTestActivityButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                MainActivity.this.startActivity(new Intent(MainActivity.this, TremorTestActivity.class));
////            }
////        });
//
//        TextView mTextView = (TextView) findViewById(R.id.text);
//        if (hasGps()) {
//            gps = new GPSLocationTracking(this);
//            if (!hasPermission(this)) {
//                Log.e(TAG, "Permission denied!");
//            } else {
//                gps.getLastLocation();
//                gps.createLocationRequest();
//            }
//        } else {
//            Log.e(TAG, "The device does not have gps");
//        }
//
//        locationTv = findViewById(R.id.location);
//        hasPermission(this);
//        if (!hasPermission(this)) {
//            Log.e(TAG, "Permission denied!");
//            return;
//        }
//
//
//        final Intent intent = new Intent(this.getApplication(), MyLocationService.class);
//        this.getApplication().startService(intent);
//        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
//                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
//
//
//        // Enables Always-on
////        setAmbientEnabled();
//    }
//
//    public void onClickTremorTestActivityButton(View view) {
//        startActivity(new Intent(MainActivity.this, TremorTestActivity.class));
//    }
//
//
//    /**
//     * Check if the device has gps feature
//     *
//     * @return True if gps available
//     */
//    boolean hasGps() {
//        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
//    }
//
//
//    /**
//     * Check the permission request result
//     *
//     * @param context
//     * @return True if user allow the location service
//     */
//    boolean hasPermission(Context context) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
//            }
//            return false;
//
//    boolean hasPermission(Context context){
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
//
//            }//return false;
//
//        return true;
//                        }
//    }
//
//
//        button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                openActivity();
//            }
//        });
//
//    }
//
//    public void openActivity(){
//        Intent intent = new Intent(this, FallDetectionActivity.class);
//        startActivity(intent);
//    }
//
//
//
//    /**
//     *monitoring the state of MyLocationService.
//     */
//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            String name = className.getClassName();
//            if (name.endsWith("MyLocationService")) {
//                locationService = ((MyLocationService.LocationServiceBinder) service).getService();
//            }
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            locationService = null;
//        }
//    };
//
//    /**
//     *receive an intent message from another class
//     */
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            Bundle b = intent.getBundleExtra("Location");
//            Location location = (Location) b.getParcelable("Location");
//            if (location != null) {
//                locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
//                Log.i(TAG, "The Location is*:" +location.getLatitude() + " " + location.getLongitude());
//            }
//        }
//    };
//}
