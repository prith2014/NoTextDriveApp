package comprithvi.example.notextdriveapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String selectedDeviceName;
    String selectedDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    // Launch a service in the background that will look for the car's bluetooth signal
    public void launchBRService() {
        Intent intent = new Intent(this, BroadcastReceiverService.class);
        intent.putExtra("foo", "bar");
        startService(intent);
    }

    // Setup button --  Goes to activity to allow the user to pick the car's bluetooth signal
    public void setupButton(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivityForResult(intent, Intent_Constants.INTENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Intent_Constants.INTENT_RESULT_CODE) {

            // Setup activity completed, get device name and address
            Bundle extras = intent.getExtras();
            assert extras != null;
            selectedDeviceName = extras.getString("EXTRA_DEVICENAME");
            selectedDeviceAddress = extras.getString("EXTRA_DEVICEADDRESS");

        }
        Intent intent = new Intent(MainActivity.this, SpeedCalcActivity.class);
        startActivity(intent);
    }
}