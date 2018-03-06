package comprithvi.example.notextdriveapp;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Prithvi V on 2/19/2018.
 * I really have no idea if this is needed
 */

public class BroadcastReceiverService extends IntentService {

    public BroadcastReceiverService(String name) {
        super("Blue-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // What happens when service is triggered
        // Gets data from incoming Intent
        String dataString = workIntent.getDataString();

    }
}
