package com.iyr.ian.utils;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.iyr.ian.R;

public class GPSNotificationUtils {
    private static final String CHANNEL_ID = "gps_notification_channel";
    private static final int NOTIFICATION_ID = 2;

    public static void showGPSActivationNotification(Context context) {
        // Verificar si se necesita crear un canal de notificación (solo necesario en Android 8.0 y versiones posteriores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }

        // Verificar si el GPS está desactivado
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            // Construir la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_gps_notification)
                    .setContentTitle("GPS Desactivado")
                    .setContentText("Se necesita activar el GPS para continuar.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)  // Auto-cancelar cuando se hace clic en la notificación
                    .setDefaults(Notification.DEFAULT_ALL);

            // Agregar un botón para abrir la configuración de activación del GPS
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            builder.setContentIntent(PendingIntent.getActivity(context, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));

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
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GPS Notification Channel";
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
