package comprithvi.example.notextdriveapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService  extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        for (DetectedActivity activity : detectedActivities) {
            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            Toast.makeText(this, "Detected activity: " + activity.getType() + ", " + activity.getConfidence(), Toast.LENGTH_SHORT).show();
            if(activity.getType()==DetectedActivity.IN_VEHICLE && activity.getConfidence()>60){
                // Person is in vehicle with decent confidence
                SharedPreferences prefs = this.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editPrefs = prefs.edit();

                if (!(prefs.getBoolean("userdetails.isSpeedServiceOn",false))) {
                    Intent intent1 = new Intent(this, speedService.class);
                    startService(intent1);
                }
            }
            else if (activity.getType()==DetectedActivity.IN_VEHICLE && activity.getConfidence()<60){
                Intent intent2 = new Intent(this, speedService.class);
                stopService(intent2);
            }
            //-----------HERE THE SERVICE FIGURES OUT WHICH ACTIVITY IS OCCURING-----------//
            //broadcastActivity(activity);
        }
    }

    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}