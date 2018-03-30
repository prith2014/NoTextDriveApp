package comprithvi.example.notextdriveapp;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Prithvi V on 2/19/2018.
 * Service that looks for selected Bluetooth device in background.
 * Not working
 */

public class BroadcastReceiverService extends Service {
    private static final String TAG = "myApp";
    BroadcastReceiver mReceiver;
    IntentFilter filter;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service in onStartCommand");
        final String blueToothAddress = intent.getStringExtra("address");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.v(TAG, "Bluetooth Connection has been found in service");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device.getAddress().equals(blueToothAddress)) {

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
                }
            }

        };


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
