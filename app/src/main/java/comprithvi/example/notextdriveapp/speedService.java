package comprithvi.example.notextdriveapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
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
    IntentFilter filter;

    // Speed variables
    long prevTime, currentTime, prevTime2;
    Location currentLocation, prevLocation, prevLocation2;
    LocationRequest mLocationRequest;
    double speed;
    LocationCallback mLocationCallBack;
    long timeInterval = 5000;
    double timeIntervalDouble = (double) timeInterval;

    // Variables for notification blocking
    private NotificationManager notificationManager;
    Boolean counter = false;
    int timer = 60000;
    Runnable r;
    Handler h = new Handler();
    double speedMin = 8.04;     // 5 MPH
    //double speedMin = 3.2198688;     // 2 MPH
    String customReplyMessage = "";
    Boolean isAutoReplyOn;

    private final BroadcastReceiver sms = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("onReceive", "SMS onReceive was called" );

            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                Log.v("SMS Received", "SMS was received" );
                String smsSender = "";
                String smsBody = "";
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getDisplayOriginatingAddress();
                    smsBody += smsMessage.getMessageBody();
                    Toast.makeText(context, smsSender, Toast.LENGTH_LONG).show();

                    // Sending text back
                    if (isAutoReplyOn)
                        sendSMS(smsSender, customReplyMessage);
                }
            }
        }
    };


    public void onCreate() {
        Log.v(TAG, "Speed Service in onCreate");
        super.onCreate();

        filter = new IntentFilter();
        filter.setPriority(999);
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(sms, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Speed Service in onStartCommand");
        SharedPreferences prefs = this.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editPrefs = prefs.edit();

        editPrefs.putBoolean("userdetails.isSpeedServiceOn", true).apply();
        customReplyMessage = prefs.getString("userdetails.customReplyMessage","Sorry, I'm currently driving");
        isAutoReplyOn = prefs.getBoolean("userdetails.isSMSOn",false);

        //Log.v(TAG, "" + prefs.getBoolean("userdetails.isSpeedServiceOn", false));
        Log.v(TAG, "Is SMS on" + isAutoReplyOn);
        Log.v(TAG, "Message: " + customReplyMessage);
        Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Speed Notification")
                .setAutoCancel(true);

        Notification notification = builder.build();
        startForeground(1, notification);


        startLocationUpdates();
        return START_STICKY;
    }


    //--------------------------------------------Location Functions-----------------------------------------

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
            //double testing = ((timeIntervalDouble/1000)/3600);
            speed = ((avgDistance/1000) / ((timeIntervalDouble/1000)/3600));
            //speed = ((avgDistance/1000) / 0.00277777778);
            //speed = ((avgDistance/1000) / 0.0013888888889);
            Log.v(TAG, "Speed: " + String.valueOf(speed));
            //Log.v(TAG, "Testing: " + String.valueOf(testing));
        }
        // You can now create a LatLng Object for use with maps

    }


    //----------------------------------------Notification Functions--------------------------------------

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

        if (speed > 0) {
            Log.v(TAG, "Notifications are being blocked");
            h.removeCallbacksAndMessages(null);     // Cancel timer
            counter = false;
            startNotifBlock();          // Turn on DO not Disturb
        } else {

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

                h.postDelayed(r, timer);    // Timer till Do not Disturb turns on
            }
        }
    }

    //---------------------------------------------SMS FUNCTIONS---------------------------------------

    public void sendSMS(String phoneNum, String msg) {
        try {
            Log.v("Auto Reply", "sendSMS function called" );
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, msg, null, null);
            Log.v(TAG, "SMS Message sent");
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
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
        unregisterReceiver(sms);

        SharedPreferences prefs = this.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editPrefs = prefs.edit();

        editPrefs.putBoolean("userdetails.isSpeedServiceOn", false).apply();

        super.onDestroy();
    }
}
