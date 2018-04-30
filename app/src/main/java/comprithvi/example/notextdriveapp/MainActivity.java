package comprithvi.example.notextdriveapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static comprithvi.example.notextdriveapp.SetupActivity.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "myApp";
    // Variables for bluetooth
    String selectedDeviceName;
    String selectedDeviceAddress;
    TextView bluetoothName;
    TextView bluetoothAddress;
    String softDisableOptions[] = {"30 Minutes","1 Hour", "2 Hours"};
    int softDisableTimer;    // defaults to 30
    Boolean isSoftDisableOn = false;
    String customReplyMessage;

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
        setContentView(R.layout.final_design);
        final SharedPreferences prefs = this.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putBoolean("userdetails.isSoftDisableOn", false).apply();

        // BUTTONS //
        Button bluetoothSetupButton = findViewById(R.id.button_bluetoothsetup);
        bluetoothSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                startActivityForResult(intent, Intent_Constants.INTENT_REQUEST_CODE);
            }
        });

        final Button customReplyButton = findViewById(R.id.button);
        customReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Auto-Reply");
                builder.setMessage("Set Custom Auto-Reply Message");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        customReplyMessage = input.getText().toString();
                        editPrefs.putString("userdetails.customReplyMessage",customReplyMessage).apply();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                new Handler().postDelayed(new Runnable(){

                    public void run() {
                        builder.show();
                    }

                }, 200L);
            }
        });

        final AlertDialog.Builder builderSpinner = new AlertDialog.Builder(this);
        builderSpinner.setTitle("Select Disable Time");
        builderSpinner.setItems(softDisableOptions, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                dialog.dismiss();
                switch (position) {
                    case 0:
                        //softDisableTimer = 60000*30;
                        softDisableTimer = 30000;
                        editPrefs.putInt("userdetails.softDisableTimer", softDisableTimer).apply();

                        if (prefs.getBoolean("userdetails.isSpeedServiceOn", false)) {
                            isSoftDisableOn = true;

                            editPrefs.putBoolean("userdetails.isSoftDisableOn", isSoftDisableOn).apply();
                            launchBRService();
                        }

                        break;
                    case 1:
                        softDisableTimer = 60000*60;
                        editPrefs.putInt("userdetails.softDisableTimer", softDisableTimer).apply();

                        if (prefs.getBoolean("userdetails.isSpeedServiceOn", false)) {
                            isSoftDisableOn = true;

                            editPrefs.putBoolean("userdetails.isSoftDisableOn", isSoftDisableOn).apply();
                            launchBRService();
                        }

                        break;
                    case 2:
                        softDisableTimer = 60000*120;
                        editPrefs.putInt("userdetails.softDisableTimer", softDisableTimer).apply();

                        if (prefs.getBoolean("userdetails.isSpeedServiceOn", false)) {
                            isSoftDisableOn = true;

                            editPrefs.putBoolean("userdetails.isSoftDisableOn", isSoftDisableOn).apply();
                            launchBRService();
                        }

                        break;
                    default:
                        editPrefs.putInt("userdetails.softDisableTimer", 60000*30).apply();

                        if (prefs.getBoolean("userdetails.isSpeedServiceOn", false)) {
                            isSoftDisableOn = true;

                            editPrefs.putBoolean("userdetails.isSoftDisableOn", isSoftDisableOn).apply();
                            launchBRService();
                        }

                        break;
                }
            }

        });

        Button softDisable = findViewById(R.id.button2);
        softDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.v(TAG, "" + prefs.getBoolean("userdetails.isSpeedServiceOn", false));
                Log.v(TAG, "Soft Disable button has been pressed by user");

                if (!(prefs.getBoolean("userdetails.isSoftDisableOn", false))) {
                    builderSpinner.show();
                } else {
                    editPrefs.putBoolean("userdetails.isSoftDisableOn", false).apply();
                    launchBRService();
                }


            }
        });

        final ToggleButton startService = findViewById(R.id.serviceEnabled);
        startService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (prefs.getBoolean("userdetails.userIsUsingBluetooth", false))
                        launchBRService();
                    else {
                        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
                        startService(intent);
                    }
                } else {
                    if (prefs.getBoolean("userdetails.userIsUsingBluetooth", false))
                        stopBRService();
                    else {
                        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
                        stopService(intent);
                    }
                }
            }
        });

        Switch bluetoothSwitch = findViewById(R.id.switch1);
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // stop detected API service first
                    Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
                    stopService(intent);
                    startService.setChecked(false);
                    editPrefs.putBoolean("userdetails.userIsUsingBluetooth", true).apply();
                } else {
                    // stop bluetooth service first
                    stopBRService();
                    startService.setChecked(false);
                    editPrefs.putBoolean("userdetails.userIsUsingBluetooth", false).apply();
                }
            }
        });

        Switch autoReplySwitch = findViewById(R.id.switch2);
        autoReplySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // turn on auto reply
                    editPrefs.putBoolean("userdetails.isSMSOn",true).apply();
                } else {
                    // turn off auto reply
                    editPrefs.putBoolean("userdetails.isSMSOn",false).apply();
                }
            }
        });

        // BLUETOOTH //

        bluetoothName = findViewById(R.id.bluetoothName);
        bluetoothAddress = findViewById(R.id.bluetoothAddress);


        // Display selected bluetooth device
        File btFile = new File(getApplicationContext().getFilesDir(),"bluetoothData.txt");
        if (btFile.exists())
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

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }

        // Request Do Not Disturb Access
        if(!notificationManager.isNotificationPolicyAccessGranted()){
            Toast.makeText(MainActivity.this, "Please activate Do Not Disturb Access and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
        }

        // Request Bluetooth Permission
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                // Ask user to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        String permission2 = Manifest.permission.SEND_SMS;
        int grant2 = ContextCompat.checkSelfPermission(this, permission2);
        if ( grant2 != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission2;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

    }

    //----------------------- BLUETOOTH FUNCTIONS ----------------------------------------//

    // Launch a service in the background that will look for the car's bluetooth signal
    public void launchBRService() {
        Intent intent = new Intent(this, BroadcastReceiverService.class);
        intent.putExtra("softdisable", softDisableTimer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else
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
        //bluetoothName.setText(selectedDeviceName);
        //bluetoothAddress.setText(selectedDeviceAddress);
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
}