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
 */

public class MyBTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")) {
            //Log.d(TAG, "Bluetooth connect");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth found")
            .show();
        }
    }
}
