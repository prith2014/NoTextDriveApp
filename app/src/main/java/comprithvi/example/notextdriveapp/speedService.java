package comprithvi.example.notextdriveapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class speedService extends Service {
    private static final String TAG = "myApp";
    private final int REQUEST_LOCATION = 200;
    String ANDROID_CHANNEL_ID = "default_channel_id_2";

    // Speed variables
    long prevTime, currentTime, prevTime2;
    Location currentLocation, prevLocation, prevLocation2;
    LocationRequest mLocationRequest;
    double speed;
    LocationCallback mLocationCallBack;
    int timeInterval = 5000;

    // Variables for notification blocking
    private NotificationManager notificationManager;
    Boolean counter = false;
    int timer = 60000;
    Runnable r;
    Handler h = new Handler();


    public void onCreate() {
        Log.v(TAG, "Speed Service in onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Speed Service in onStartCommand");
        //super.onStartCommand(intent, START_STICKY, startId);

        Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Speed Notification")
                .setAutoCancel(true);

        Notification notification = builder.build();
        startForeground(1, notification);


        startLocationUpdates();
        return START_STICKY;
    }


    //-------------------------------Location Functions-----------------------------------------

    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        //mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(timeInterval);
        mLocationRequest.setFastestInterval(timeInterval);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }

       mLocationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                currentLocation = locationResult.getLastLocation();
                onLocationChanged(locationResult.getLastLocation());

                checkSpeedAndBlock();
            }
        };

        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    prevLocation = currentLocation;
                    prevTime = prevLocation.getTime();
                }
                if (prevLocation != null) {
                    prevLocation2 = prevLocation;
                    prevTime2 = prevLocation2.getTime();
                }
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 50);


        currentTime = currentLocation.getTime();

        if (prevLocation2 != null) {
            float distance = prevLocation.distanceTo(currentLocation);
            float distance2 = prevLocation2.distanceTo(prevLocation);
            float avgDistance = (distance + distance2)/2;
            //long timeDifference = currentTime - prevTime;
            //speed = ((avgDistance/1000) / ((timeInterval/1000)/3600));
            speed = ((avgDistance/1000) / 0.00277777778);
        }
        // You can now create a LatLng Object for use with maps

        //stopSelf();
        //launchSpeedService();
    }


    //------------------------Notification Functions-------------------------

    // Check do not disturb permissions, and activate
    void startNotifBlock(){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(!notificationManager.isNotificationPolicyAccessGranted()){
            Toast.makeText(this, "Please activate Do Not Disturb Access and press Back to return to the application!", Toast.LENGTH_LONG).show();
            //startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
        }
        else{
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
        }
    }
    // Turn off do not disturb
    void stopNotifBlock(){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    public void checkSpeedAndBlock() {
        // Default speed that works is 2
        //Handler h = new Handler();

        CountDownTimer counterTimer = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String seconds = String.valueOf(millisUntilFinished / 1000);
                Log.v(TAG,"Timer: " + seconds);
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "Notifications are NOT being blocked");
                stopNotifBlock();
            }
        };

        if (speed > 5) {
            Log.v(TAG, "Notifications are being blocked");
            //h.removeCallbacks(r);
            h.removeCallbacksAndMessages(r);
            counter = false;
            //counterTimer.cancel();
            startNotifBlock();
        } else {
            //Log.v(TAG, "Notifications are NOT being blocked");
            //stopNotifBlock();

            if (!counter) {
                Log.v(TAG, "Timer started");
                counter = true;

                r = new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "Notifications are NOT being blocked");
                        stopNotifBlock();
                    }
                };

                h.postDelayed(r, 60000);
                //counterTimer.start();
            }
        }
    }

    public void launchSpeedService() {
        Intent intent = new Intent(this, speedService.class);
        startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Speed Service is destroyed");
        getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallBack);
        stopNotifBlock();
        super.onDestroy();
    }
}
