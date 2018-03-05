package comprithvi.example.notextdriveapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SetupActivity extends AppCompatActivity {
    public static final int REQUEST_ENABLE_BT = 2;
    ListView pairedDeviceListView;
    ArrayAdapter pairedDeviceAdapter;
    ArrayList<HashMap<String,String>> pairedDevicesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Device does support Bluetooth

            if (!mBluetoothAdapter.isEnabled()) {
                // Ask user to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            ArrayList<String> pairedDevicesListNames = new ArrayList<String>();
            ArrayList<String> pairedDevicesListAddresses = new ArrayList<String>();
            // make a hash map
            HashMap<String, String> BTpairedDevices = new HashMap<>();

            if (pairedDevices.size() > 0) {
                // There are paired devices, get name and address of each paired device
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    pairedDevicesListNames.add(deviceName);
                    pairedDevicesListAddresses.add(deviceHardwareAddress);
                    BTpairedDevices.put(deviceName, deviceHardwareAddress);
                }
            }

            // Show list of paired devices
            pairedDeviceListView = findViewById(R.id.pairedDevicesListView);
            pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDevicesListNames);
            pairedDeviceListView.setAdapter(pairedDeviceAdapter);
            pairedDeviceAdapter.notifyDataSetChanged();


        }

    }

    //


}
