package comprithvi.example.notextdriveapp;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Prithvi V on 2/19/2018.
 * Service that looks for selected Bluetooth device in background.
 * Not working
 */

public class BroadcastReceiverService extends Service {
    private static final String TAG = "myApp";
    String blueToothAddress;
    IntentFilter filter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Broadcast Receiver onReceive function was called");
            String action = intent.getAction();
            //Log.v(TAG, action);
            //Log.v(TAG, BluetoothDevice.ACTION_FOUND);
            if(blueToothAddress != null) {
                Log.v(TAG, blueToothAddress);
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.v(TAG, "Bluetooth Connection has been found in service");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getAddress().equals(blueToothAddress)) {
                        Log.v(TAG, "YEAH CAR BLUETOOTH CONNECTED !!!!!!");
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.v(TAG, "YEAH BLUETOOTH DISCONNECTED !!!!!!");
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

        //Intent intent2 = new Intent();
        //blueToothAddress = intent2.getStringExtra("address");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service in onStartCommand");
        //blueToothAddress = intent.getStringExtra("address");
        readFromFile(this);

        return super.onStartCommand(intent, flags, startId);
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service is destroyed");
        super.onDestroy();
    }
}
