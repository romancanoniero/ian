package com.iyr.ian.itag;


import static com.iyr.ian.itag.Notifications.cancelDisconnectNotification;
import static com.iyr.ian.itag.Notifications.sendDisconnectNotification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.iyr.ian.BuildConfig;
import com.iyr.ian.R;
import com.iyr.ian.app.AppClass;
import com.iyr.ian.itag.history.HistoryRecord;
import com.iyr.ian.utils.bluetooth.ble.AlertVolume;
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState;
import com.iyr.ian.utils.bluetooth.ble.BLEDefault;
import com.iyr.ian.utils.bluetooth.ble.BLEInterface;
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag;
import com.iyr.ian.utils.bluetooth.preference.VolumePreference;
import com.iyr.ian.utils.multimedia.MediaPlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ITag {

    private Context context;
    public static final String LT = ITag.class.getName();
    public static final int BLE_TIMEOUT = 30;
    public static final int SCAN_TIMEOUT = 60;
    public static BLEInterface ble;
    public static ITagsStoreInterface store;

    private static final Map<String, AutoCloseable> reconnectListeners = new HashMap<>();
    private static final DisposableBag disposables = new DisposableBag();
    private static final DisposableBag disposablesConnections = new DisposableBag();

    public static DisposableBag getDisposablesConnections() {
        return disposablesConnections;
    }

    private static final Map<String, Thread> asyncConnections = new HashMap<>();
    private static final Map<String, DisposableBag> connectionBags = new HashMap<>();

    public static void initITag(Context _context) {

        ble = BLEDefault.shared(_context);
        store = new ITagsStoreDefault(AppClass.getInstance().getApplicationContext());
        Log.d("ITags", "ITags memorizados : " + store.count());

        for (int i = 0; i < store.count(); i++) {
            ITagInterface itag = store.byPos(i);
            if (itag != null) {
                //    if (itag.isAlertDisconnected()) {
                Log.d("ITags", "Voy a conectarme con  : " + itag.id());
                BLEConnectionInterface connection = ITag.ble.connectionById(itag.id());
                connectAsync(connection);
                enableReconnect(itag.id());
                //  }
            }
        }
        subscribeDisconnections();
        disposables.add(store.observable().subscribe(event -> {
            ITagInterface itag = event.tag;
            boolean reconnect = store.remembered(itag.id()) && itag.isAlertDisconnected();
            BLEConnectionInterface connection = ble.connectionById(itag.id());
            if (reconnect) {
                enableReconnect(itag.id());
                if (!connection.isConnected()) {
                    connectAsync(connection);
                }
            } else {
                disableReconnect(itag.id());
                if (connection.isConnected()) {
                    new Thread(() -> connection.disconnect(BLE_TIMEOUT)).start();
                }
            }
            subscribeDisconnections();
        }));
        showNotification(_context);
    }

    public static void close() {
        List<String> ids;
        synchronized (reconnectListeners) {
            ids = new ArrayList<>(reconnectListeners.keySet());
        }
        for (String id : ids) {
            disableReconnect(id);
        }

        List<Thread> threads;
        synchronized (asyncConnections) {
            threads = new ArrayList<>(asyncConnections.values());
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        try {
            ble.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (connectionBags) {
            for (DisposableBag bag : connectionBags.values()) {
                bag.dispose();
            }
        }
        disposables.dispose();
        disposablesConnections.dispose();
    }

    private static void subscribeDisconnections() {
        disposablesConnections.dispose();
        for (int i = 0; i < store.count(); i++) {
            final ITagInterface itag = store.byPos(i);
            if (itag != null) {
                BLEConnectionInterface connection = ble.connectionById(itag.id());
                if (itag.isAlertDisconnected()) {
                    disposablesConnections.add(connection.observableState().subscribe(event -> {
                        if (BuildConfig.DEBUG)
                            Log.d(LT, "connection " + connection.id() + " state " + connection.state());
                        if (itag.isAlertDisconnected() && BLEConnectionState.disconnected.equals(connection.state())) {
                            if (itag.alertDelay() == 0) {
                                if (BuildConfig.DEBUG)
                                    Log.d(LT, "connection " + connection.id() + " lost");

                                MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).startSoundDisconnected();

//                                        new MediaPlayerUtils(AppClass.getInstance()).startSoundDisconnected(AppClass.getInstance());
                                sendDisconnectNotification(AppClass.getInstance(), itag.name());
                                HistoryRecord.add(AppClass.getInstance(), itag.id());
                            } else {
                                if (BuildConfig.DEBUG)
                                    Log.d(LT, "connection " + connection.id() + " lost will be delayed by " + (itag.alertDelay() * 1000) + "ms");
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    if (BuildConfig.DEBUG)
                                        Log.d(LT, "connection " + connection.id() + " lost posted, state=" + connection.state());
                                    if (itag.isAlertDisconnected() && !connection.isConnected()) {
                                        if (BuildConfig.DEBUG)
                                            Log.d(LT, "connection " + connection.id() + " lost");
                                        int volume = new VolumePreference(AppClass.getInstance()).get();

                                        if (volume == VolumePreference.LOUD) {

                                            MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).startSoundDisconnected();
                                        } else if (volume == VolumePreference.VIBRATION) {
                                            MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).startVibrate();
                                        }
                                        sendDisconnectNotification(AppClass.getInstance(), itag.name());
                                        HistoryRecord.add(AppClass.getInstance(), itag.id());
                                    }
                                }, itag.alertDelay() * 1000L);
                            }
                        } else if (BLEConnectionState.connected.equals(connection.state())) {
                            if (BuildConfig.DEBUG)
                                Log.d(LT, "connection " + connection.id() + " restored");


                            MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).stopSound();
                            cancelDisconnectNotification(AppClass.getInstance());
                            HistoryRecord.clear(AppClass.getInstance(), itag.id());
                        }
                    }));
                }
                disposablesConnections.add(connection.observableClick().subscribe(click -> {
                    if (click != 0 && connection.isAlerting()) {
                        new Thread(() -> connection.writeImmediateAlert(AlertVolume.NO_ALERT, ITag.BLE_TIMEOUT)).start();
                    } else {
                        if (connection.isFindMe() && !MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).isSound()) {

                            //    MediaPlayerUtils.Companion.getInstance().startFindPhone(AppClass.getInstance());
/*
                                broadcastMessage(
                                        null,
                                        BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED
                                );

 */

                            Log.d("ITags", "subscribeDisconnections() - onEmergencyButtonPressed()");
                            AppClass.getInstance().onEmergencyButtonPressed();

                        } else {
                            if (connection.isConnected()) {

                                MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).stopSound();
                            }
                        }
                    }
                }));
            }
        }
    }

    private static int connectThreadsCount = 0;

    public static void connectAsync(final BLEConnectionInterface connection) {
        connectAsync(connection, true);
    }

    @SuppressWarnings("SameParameterValue")
    private static void connectAsync(final BLEConnectionInterface connection, boolean infinity) {

        Log.d("ITags", "connectAsync " + connection.id());
        connectAsync(connection, infinity, null);
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    static void connectAsync(final BLEConnectionInterface connection, Runnable onComplete) {
        connectAsync(connection, true, onComplete);
    }

    @SuppressWarnings("SameParameterValue")
    public static void connectAsync(final BLEConnectionInterface connection, boolean infinity, Runnable onComplete) {
        synchronized (asyncConnections) {
            if (asyncConnections.containsKey(connection.id())) {
                return;
            }
        }
        DisposableBag disposableBagTmp;
        synchronized (connectionBags) {
            disposableBagTmp = connectionBags.get(connection.id());
            if (disposableBagTmp == null) {
                disposableBagTmp = new DisposableBag();
                connectionBags.put(connection.id(), disposableBagTmp);
            } else {
                disposableBagTmp.dispose();
            }
        }

        final ITagInterface itag = store.byId(connection.id());
        if (itag == null) {
            return;
        }
        connectThreadsCount++;
        if (BuildConfig.DEBUG) {
            Log.d(LT, "BLE Connect thread started, count = " + connectThreadsCount);
        }
        Thread thread = new Thread("BLE Connect " + connection.id() + " " + System.currentTimeMillis()) {
            @Override
            public void run() {
                do {
                    if (BuildConfig.DEBUG) {
                        Log.d(LT, "BLE Connect thread connect " + connection.id() + "/" + itag.name() + " " + Thread.currentThread().getName());
                    }
                    connection.connect(infinity);
                } while (!isInterrupted() && itag.isAlertDisconnected() && infinity && !connection.isConnected());
                // stop sound on connection in any case
                MediaPlayerUtils.Companion.getInstance(AppClass.getInstance()).stopSound();
                if (!isInterrupted()) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
                synchronized (asyncConnections) {
                    asyncConnections.remove(connection.id());
                }
                connectThreadsCount--;
                if (BuildConfig.DEBUG) {
                    Log.d(LT, "BLE Connect thread finished, count = " + connectThreadsCount);
                }
            }
        };
        synchronized (asyncConnections) {
            asyncConnections.put(connection.id(), thread);
        }
        thread.start();
    }

    private static void enableReconnect(String id) {
        disableReconnect(id);
        synchronized (reconnectListeners) {
            final BLEConnectionInterface connection = ITag.ble.connectionById(id);
            reconnectListeners.put(id, connection.observableState().subscribe(state -> {
                if (BLEConnectionState.disconnected.equals(state)) {
                    connectAsync(connection);
                }
            }));
        }
    }

    private static void disableReconnect(String id) {
        synchronized (reconnectListeners) {
            AutoCloseable existing = reconnectListeners.get(id);
            if (existing != null) {
                try {
                    existing.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                reconnectListeners.remove(id);
            }
        }
    }

    private static void broadcastMessage(@Nullable Object data, String action) {
        Intent intent = new Intent(action);
        if (data instanceof Bundle) {
            intent.putExtras((Bundle) data);
        } else {
            if (data != null) {
                var dataJson = new Gson().toJson(data);
                intent.putExtra("data", dataJson);
            }
        }
        LocalBroadcastManager.getInstance(AppClass.getInstance()).sendBroadcast(intent);
    }


    public static void showNotification(Context context) {
        // Verificar si se necesita crear un canal de notificación (solo necesario en Android 8.0 y versiones posteriores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_custom_launcher)
                .setContentTitle("Conectado")
                .setContentText("Tu boton de panico fisico esta conectado")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Obtener el NotificationManagerCompat
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Mostrar la notificación
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    private static final String CHANNEL_ID = "itag_notification_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ITag Notification Channel";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Registrar el canal en el sistema
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
