package com.iyr.ian.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.services.bootservice.BootService


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "El telefono se acaba de iniciar y IAN se enteró.")

        Log.d("BOOTEO", "El dispositivo se acaba de iniciar y IAN se entero.")


        if (Intent.ACTION_BOOT_COMPLETED == intent.action || "android.intent.action.QUICKBOOT_POWERON" == intent.action) {

            Log.d(TAG, "EMG_ Inicio ")
            if (FirebaseAuth.getInstance().currentUser != null) {

                Log.d("BOOTEO", "El usuario de IAN estaba logueado.")
                //----------------------------------------------------------------
                //            if (context.isGPSEnabled()) {
                if (Intent.ACTION_BOOT_COMPLETED == intent.action || "android.intent.action.QUICKBOOT_POWERON" == intent.action) {
                    Log.d("BOOTEO", "Mando a iniciar el servicio de booteo.")
                    val serviceIntent = Intent(context, BootService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
                /*
                              } else {
                                  GPSNotificationUtils.showGPSActivationNotification(context)
                                  GlobalScope.launch(Dispatchers.Default) {
                                      // Bucle hasta que se cumpla la condición
                                      while (!context.isGPSEnabled()) {
                                          // Simulamos alguna tarea
                                          delay(1000) // Espera 1 segundo
                                          // Comprobamos la condición (aquí puedes poner tu propia lógica)
                                         Log.d("BOOTMANAGER", "BOOT_ Sigo dando vueltas hasta que activen el GPS")
                                      }
                                      Log.d("BOOTMANAGER", "BOOT_ voy a activar el servicio de ubicacion")
                                      context.startLocationService()
                                      Log.d("BOOTMANAGER", "BOOT_ Activado el servicio de ubicacion")
                                  }

                              }
              */

            } else {
                Log.d(TAG, "LOC_ NO inicio el servicio de ubicación por no estar logueado")
            }
        }
    }

    companion object {
        private const val JOB_ID = 123 // Identificador único para la tarea en segundo plano
        private const val TAG =
            "BOOT_RECEIVER" // Identificador único para la tarea en segundo plano

    }

}
