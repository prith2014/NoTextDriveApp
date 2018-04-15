package comprithvi.example.notextdriveapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by Prithvi V on 2/19/2018.
 * Service that looks for selected Bluetooth device in background.
 */

public class BroadcastReceiverService extends Service {
    private static final String TAG = "myApp";
    private final int REQUEST_LOCATION = 200;
    String blueToothAddress;
    IntentFilter filter;
    String ANDROID_CHANNEL_ID = "default_channel_id_2";

    Boolean isSoftDisableOn = false;
    Boolean isBluetoothConnected = false;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v(TAG, "Broadcast Receiver onReceive function was called");
            String action = intent.getAction();
            Log.v(TAG, action);
            Log.v(TAG, BluetoothDevice.ACTION_FOUND);
            if (blueToothAddress != null) {
                Log.v(TAG, blueToothAddress);
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.v(TAG, "Bluetooth Connection has been found in service");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getAddress().equals(blueToothAddress)) {
                    // Car bluetooth is connected, time to measure speed/use accelerometer
                    Log.v(TAG, "YEAH CAR BLUETOOTH CONNECTED !!!!!!");
                    isBluetoothConnected = true;
                    launchSpeedService();
                }
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.v(TAG, "YEAH BLUETOOTH DISCONNECTED !!!!!!");
                isBluetoothConnected = false;
                stopSpeedService();
            }

        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service in onStartCommand");
        //blueToothAddress = intent.getStringExtra("address");


        Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Bluetooth Notification")
                .setAutoCancel(true);

        Notification notification = builder.build();
        startForeground(1, notification);

        readFromFile(this);

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

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

        blueToothAddress = readStrings.get(1);
    }

    public void launchBRService() {
        Intent intent = new Intent(this, BroadcastReceiverService.class);
        startService(intent);
    }

    public void launchSpeedService() {
        Intent intent = new Intent(this, speedService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else
            startService(intent);
    }

    public void stopSpeedService() {
        Intent intent = new Intent(this, speedService.class);
        stopService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service is destroyed");
        unregisterReceiver(mReceiver);
        stopSpeedService();

        super.onDestroy();
    }
}
