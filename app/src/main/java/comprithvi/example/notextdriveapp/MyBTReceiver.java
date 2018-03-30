package comprithvi.example.notextdriveapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import static android.content.ContentValues.TAG;

/**
 * Created by Prithvi V on 2/20/2018.
 * In case we need to create a custom Broadcast Receiver
 */

public class MyBTReceiver extends BroadcastReceiver {
    BroadcastReceiver mReceiver;
    String blueToothAddress;
    /*
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")) {
            //Log.d(TAG, "Bluetooth connect");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth found")
            .show();
        }
    }*/
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Boardcast Receiver onReceive function was called");
        String action = intent.getAction();

        //if (BluetoothDevice.ACTION_FOUND.equals(action)) {
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
        //}
    }
}
