package comprithvi.example.notextdriveapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SetupActivity extends AppCompatActivity {
    public static final int REQUEST_ENABLE_BT = 2;
    ListView pairedDeviceListView;
    ArrayAdapter pairedDeviceAdapter;
    ArrayList<HashMap<String,String>> pairedDevicesList;
    ArrayList<String> pairedDevicesListNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Create array list of bluetooth devices to display
        listPairedBlueToothDevices();

        // Show list of paired devices
        pairedDeviceListView = findViewById(R.id.pairedDevicesListView);
        pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDevicesListNames);
        pairedDeviceListView.setAdapter(pairedDeviceAdapter);
        pairedDeviceAdapter.notifyDataSetChanged();

        // Person clicks on a bluetooth device in list
        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedBTHashMap = pairedDevicesList.get(position);
                String selectedDeviceName = pairedDevicesListNames.get(position);
                String selectedDeviceAddress = selectedBTHashMap.get(selectedDeviceName);
                String bluetoothToTxt = (selectedDeviceName + '\n' + selectedDeviceAddress);

                // Write Device name and address to text file
                writeToFile(bluetoothToTxt, getApplicationContext());

                // Go back to main activity
                Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                Bundle extras = new Bundle();
                extras.putString("EXTRA_DEVICENAME", selectedDeviceName);
                extras.putString("EXTRA_DEVICEADDRESS", selectedDeviceAddress);
                intent.putExtras(extras);
                setResult(Intent_Constants.INTENT_RESULT_CODE, intent);
                finish();
            }
        });

    }

    // Function to write to txt file
    public void writeToFile(String data, Context context) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("bluetoothData.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast toast = Toast.makeText(this, "Saved to bluetoothData.txt", Toast.LENGTH_LONG);
        toast.show();
    }

    // Creates an array list of Bluetooth devices remembered by phone
    public void listPairedBlueToothDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Device does support Bluetooth

            if (!mBluetoothAdapter.isEnabled()) {
                // Ask user to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            pairedDevicesListNames = new ArrayList<>();
            pairedDevicesList = new ArrayList<>();

            if (pairedDevices.size() > 0) {
                // There are paired devices, get name and address of each paired device
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    pairedDevicesListNames.add(deviceName);
                    HashMap<String, String> BTpairedDevicesHashes = new HashMap<>();
                    BTpairedDevicesHashes.put(deviceName, deviceHardwareAddress);
                    pairedDevicesList.add(BTpairedDevicesHashes);
                }
            }
        }
    }

}
