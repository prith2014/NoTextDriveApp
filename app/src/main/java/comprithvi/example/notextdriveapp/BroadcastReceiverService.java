package comprithvi.example.notextdriveapp;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by Prithvi V on 2/19/2018.
 * Service that looks for selected Bluetooth device in background.
 * Not working
 */

public class BroadcastReceiverService extends IntentService {
    private static final String TAG = "myApp";
    BroadcastReceiver mReceiver;
    IntentFilter filter;

    public BroadcastReceiverService() {
        super("Blue-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // What happens when service is triggered
        // Gets data from incoming Intent
        String dataString = workIntent.getDataString();
        final String address = workIntent.getStringExtra("address");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device.getAddress().equals(address)) {

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
        try {
            this.unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
