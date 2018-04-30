package comprithvi.example.notextdriveapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

        listPairedBlueToothDevices();

        // CUSTOM ADAPTER
        pairedDeviceListView = findViewById(R.id.pairedDevicesListView);
        CustomAdapterBluetooth customAdapterBluetooth = new CustomAdapterBluetooth();
        pairedDeviceListView.setAdapter(customAdapterBluetooth);

        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedBTHashMap = pairedDevicesList.get(position);
                String selectedDeviceName = pairedDevicesListNames.get(position);
                String selectedDeviceAddress = selectedBTHashMap.get(selectedDeviceName);
                String bluetoothToTxt = (selectedDeviceName + '\n' + selectedDeviceAddress);

                // Write Device name and address to text file
                writeToFile(bluetoothToTxt, getApplicationContext());
                markBluetooth();

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

    // Function to write to file
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

    // Function to mark bluetooth setup has occured
    public void markBluetooth(){
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.BLT_marker), true);
        editor.commit();
    }

    // Listing Bluetooth devices
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
            //ArrayList<String> pairedDevicesListAddresses = new ArrayList<String>();
            // make a hash map
            pairedDevicesList = new ArrayList<>();

            if (pairedDevices.size() > 0) {
                // There are paired devices, get name and address of each paired device
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    pairedDevicesListNames.add(deviceName);
                    //pairedDevicesListAddresses.add(deviceHardwareAddress);
                    HashMap<String, String> BTpairedDevicesHashes = new HashMap<>();
                    BTpairedDevicesHashes.put(deviceName, deviceHardwareAddress);
                    pairedDevicesList.add(BTpairedDevicesHashes);
                }
            }
        }
    }

    class CustomAdapterBluetooth extends BaseAdapter {
        @Override
        public int getCount() {
            return pairedDevicesList.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.custom_listview_bluetooth,null);
            TextView textView_bluetooth = view.findViewById(R.id.textView_bluetooth);

            textView_bluetooth.setText(pairedDevicesListNames.get(i));
            return view;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }
    }
}

