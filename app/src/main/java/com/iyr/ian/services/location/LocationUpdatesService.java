package com.iyr.ian.services.location;

import static com.iyr.ian.AppConstants.MESSENGER_INTENT_KEY;
import static com.iyr.ian.Constants.BROADCAST_LOCATION_UPDATED;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.iyr.ian.dao.models.LocationUpdate;


/**
 * location update service continues to running and getting location information
 */
public class LocationUpdatesService extends JobService implements LocationUpdatesComponent.ILocationProvider {

    public static final int LOCATION_MESSAGE = 9999;
    private static final String TAG = LocationUpdatesService.class.getSimpleName();
    private Messenger mActivityMessenger;
    private LocationUpdatesComponent locationUpdatesComponent;
    private JobParameters jobParameters;

    public LocationUpdatesService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob....");
        this.jobParameters = params;
        locationUpdatesComponent.onStart();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob....");
        locationUpdatesComponent.onStop();
        return false;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "created...............");
        locationUpdatesComponent = new LocationUpdatesComponent(this);
        locationUpdatesComponent.onCreate(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Service started");
        if (intent != null) {
            mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        }
        //hey request for location updates
        locationUpdatesComponent.onStart();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        Handler mServiceHandler = new Handler(handlerThread.getLooper());

        //This thread is need to continue the service running
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    //                Log.i(TAG, "thread... is running...");
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        return true; // Ensures onRebind() is called when a client re-binds.
    }


    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy....");
    }

    /**
     * send message by using messenger
     *
     * @param messageID
     */
    private void sendMessage(int messageID, Location location) {
        // If this service is launched by the JobScheduler, there's no callback Messenger. It
        // only exists when the MainActivity calls startService() with the callback in the Intent.
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.obj = location;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        broadcastLocationUpdate(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @NonNull
    private Boolean broadcastLocationUpdate(LatLng latLng) {
        Log.d(TAG, "Emito el mensaje de cambio de ubicacion " + latLng.toString());
        Intent intent = new Intent(BROADCAST_LOCATION_UPDATED);
        LocationUpdate data = new LocationUpdate();
        data.setLocation(latLng);
        String dataJson = new Gson().toJson(data);
        intent.putExtra("data", dataJson);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


        Log.d(TAG, "Creo de nuevo el Job ");
        //  JobServicesUtils.scheduleJob(getApplicationContext(), LocationUpdatesService.class);


        return true;
    }


}