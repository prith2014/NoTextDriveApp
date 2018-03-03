package comprithvi.example.notextdriveapp;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    TextView speedTV;
    TextView distanceTV;
    TextView longitude;
    TextView latitude;

    long prevTime;
    long currentTime;

    Location currentLocation, prevLocation;
    LocationRequest mLocationRequest;

    private final int REQUEST_LOCATION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prevLocation = null;
        currentLocation = null;
        prevTime = 0;
        currentTime = 0;

        speedTV = findViewById(R.id.speed);
        distanceTV = findViewById(R.id.distance);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);

    }


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }

        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        currentLocation = locationResult.getLastLocation();
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    prevLocation = currentLocation;
                    prevTime = prevLocation.getTime();
                }
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 50);


        currentTime = currentLocation.getTime();
        longitude.setText(Double.toString(currentLocation.getLongitude()));
        latitude.setText(Double.toString(currentLocation.getLatitude()));

        if (prevLocation != null) {
            float distance = prevLocation.distanceTo(currentLocation);
            distanceTV.setText(Float.toString(distance));
            long timeDifference = currentTime - prevTime;
            double speed = ((distance/1000) / 0.001388888889);
            speedTV.setText(Double.toString(speed));
        }
        // You can now create a LatLng Object for use with maps
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
