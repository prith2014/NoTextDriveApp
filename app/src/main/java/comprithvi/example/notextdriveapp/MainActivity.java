package comprithvi.example.notextdriveapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "myApp";
    // Variables for bluetooth
    String selectedDeviceName;
    String selectedDeviceAddress;
    TextView bluetoothName;
    TextView bluetoothAddress;

    // Variables for notification blocking
    private NotificationManager notificationManager;
    private android.app.Notification builder;
    String channelId = "default_channel_id";
    String channelDescription = "Default Channel";

    // Variables for speed calculation
    TextView speedTV;
    TextView distanceTV;
    TextView longitude;
    TextView latitude;
    long prevTime, currentTime, prevTime2;
    Location currentLocation, prevLocation, prevLocation2;
    LocationRequest mLocationRequest;

    private final int REQUEST_LOCATION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BLUETOOTH //
/*
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
*/
        bluetoothName = findViewById(R.id.bluetoothName);
        bluetoothAddress = findViewById(R.id.bluetoothAddress);

        Button startService = findViewById(R.id.startService);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBRService();
            }
        });

        Button stopService = findViewById(R.id.stopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBRService();
            }
        });

        // Display selected bluetooth device
        readFromFile(this);

        // SPEED CALCULATION //
        prevLocation = null;
        currentLocation = null;
        prevTime = 0;
        currentTime = 0;
        prevTime2 = 0;

        speedTV = findViewById(R.id.speed);
        distanceTV = findViewById(R.id.distance);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);

        // NOTIFICATION BLOCKING //

        // Get the notification Channel
        if(notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        create_notification_channel();

        // Send Notification
        Button send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNotification("Title", "text");
            }
        });

        // Start Notification Blocking
        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotifBlock();
            }
        });

        // Stop Notification Blocking
        Button stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNotifBlock();
            }
        });

    }

    //----------------------- BLUETOOTH FUNCTIONS ----------------------------------------//
    /*
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Broadcast Receiver onReceive function was called");
            String action = intent.getAction();

            //if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Log.v(TAG, "Bluetooth Connection has been found in service");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device.getAddress().equals(bluetoothAddress)) {

                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //Device is now connected
                    Log.v(TAG, "YEAH BLUETOOTH CONNECTED !!!!!!");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    //Device is about to disconnect
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    //Device has disconnected
                    Log.v(TAG, "YEAH BLUETOOTH DISCONNECTED !!!!!!");
                }
            }
            //}
        }

    };*/

    // Launch a service in the background that will look for the car's bluetooth signal
    public void launchBRService() {
        Intent intent = new Intent(this, BroadcastReceiverService.class);
        intent.putExtra(selectedDeviceAddress, "address");
        startService(intent);
    }

    // Stop background service
    public void stopBRService() {
        Intent intent = new Intent(this, BroadcastReceiverService.class);
        stopService(intent);
    }

    // Setup button --  Goes to activity to allow the user to pick the car's bluetooth signal
    public void setupButton(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivityForResult(intent, Intent_Constants.INTENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Intent_Constants.INTENT_RESULT_CODE && intent != null) {
            // Setup activity completed, get device name and address
            Bundle extras = intent.getExtras();
            assert extras != null;
            selectedDeviceName = extras.getString("EXTRA_DEVICENAME");
            selectedDeviceAddress = extras.getString("EXTRA_DEVICEADDRESS");
            readFromFile(this);

        }
    }

    // Read from Bluetooth text file
    public void readFromFile(Context context) {
        // String is "bluetooth Device name + '\n' + bluetooth Device Address"
        ArrayList<String> readStrings = new ArrayList<>();
        String receivedString = "";
        int x = 0;

        try {
            InputStream inputStream = context.openFileInput("bluetoothData.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                while ((receivedString = bufferedReader.readLine()) != null) {
                    readStrings.add(receivedString);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "File Not Found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        selectedDeviceName = readStrings.get(0);
        selectedDeviceAddress = readStrings.get(1);
        bluetoothName.setText(selectedDeviceName);
        bluetoothAddress.setText(selectedDeviceAddress);
    }

    //-------------------------------- NOTIFICATION FUNCTIONS --------------------------------//

    // Function to send a notification
    void postNotification(String title, String text){
        builder = new android.app.Notification.Builder(MainActivity.this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .build();
        notificationManager.notify(1, builder);
    }

    // Function to get the notification channel
    void create_notification_channel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if(notificationChannel == null){
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    // Check do not disturb permissions, and activate
    void startNotifBlock(){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(!notificationManager.isNotificationPolicyAccessGranted()){
            Toast.makeText(MainActivity.this, "Please activate Do Not Disturb Access and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
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


    //--------------------------- SPEED CALC FUNCTIONS ---------------------------------------//

    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }

        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        currentLocation = locationResult.getLastLocation();
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
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
        longitude.setText(Double.toString(currentLocation.getLongitude()));
        latitude.setText(Double.toString(currentLocation.getLatitude()));

        if (prevLocation2 != null) {
            float distance = prevLocation.distanceTo(currentLocation);
            float distance2 = prevLocation2.distanceTo(prevLocation);
            float avgDistance = (distance + distance2)/2;
            distanceTV.setText(Float.toString(distance));
            //long timeDifference = currentTime - prevTime;
            double speed = ((avgDistance/1000) / 0.00277777778);
            speedTV.setText(Double.toString(speed));
        }
        // You can now create a LatLng Object for use with maps
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}