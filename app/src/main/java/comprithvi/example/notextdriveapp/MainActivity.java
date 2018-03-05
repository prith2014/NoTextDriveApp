package comprithvi.example.notextdriveapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prithvi branch

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
}
