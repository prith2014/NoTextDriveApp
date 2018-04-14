package comprithvi.example.notextdriveapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class detectedActivity extends Service{
    private static final String TAG = "myApp";

    ActivityTransitionRequest buildTransitionRequest() {
        List transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        return new ActivityTransitionRequest(transitions);
    }

    void requestActivityTransitionUpdates(final Context context, PendingIntent pendingIntent) {
        ActivityTransitionRequest request = buildTransitionRequest();
        Task task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getApplicationContext(),"in onSuccessListener", Toast.LENGTH_SHORT).show();

                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure...
                    }
                });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "(Detected activity)Broadcast Receiver onReceive function was called");
            Toast.makeText(getApplicationContext(),"(Detected activity)Broadcast Receiver onReceive function was called", Toast.LENGTH_SHORT).show();


            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    // Do something useful here...
                    if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER && event.getActivityType() == DetectedActivity.IN_VEHICLE){
                        // Person has entered a car
                        Toast.makeText(getApplicationContext(),"Person has entered car", Toast.LENGTH_SHORT).show();
                        launchSpeedService();
                    }
                    if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT && event.getActivityType() == DetectedActivity.IN_VEHICLE){
                        // Person has exited a car
                        Toast.makeText(getApplicationContext(),"Person has EXITED CAR", Toast.LENGTH_SHORT).show();
                        stopSpeedService();
                    }
                }
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate(){
        Log.v(TAG, "detectedActivity in onCreate()");
        // -----------------NEED FILTERS FOR registerRecevier ---------------//
        IntentFilter intentFilter = new IntentFilter(TAG);
        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.v(TAG, "detectedActivty in onStartCommand");
        Toast.makeText(getApplicationContext(),"detectedActivity in onStartCommand", Toast.LENGTH_SHORT).show();

        Intent newIntent = new Intent(this, detectedActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, newIntent, 0);
        //PendingIntent pendingIntent = PendingIntent.getService(this,0,newIntent, 0);
        requestActivityTransitionUpdates(this, pendingIntent);
        return START_STICKY;
    }

    public void launchSpeedService() {
        Intent intent = new Intent(this, speedService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else
            startService(intent);
    }

    public void stopSpeedService() {
        Intent intent = new Intent(this, speedService.class);
        stopService(intent);
    }
    @Override
    public void onDestroy() {
        Log.v(TAG, "detectedActivity is destroyed");
        unregisterReceiver(mReceiver);
        stopSpeedService();

        super.onDestroy();
    }
}
