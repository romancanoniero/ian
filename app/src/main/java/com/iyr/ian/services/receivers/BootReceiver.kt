package com.iyr.ian.services.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITag
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.utils.GPSNotificationUtils
import com.iyr.ian.utils.isGPSEnabled
import com.iyr.ian.utils.startLocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "El telefono se acaba de iniciar y IAN se enteró.")

        Log.d("BOOTEO", "El dispositivo se acaba de iniciar y IAN se entero.")


        if (Intent.ACTION_BOOT_COMPLETED == intent.action || "android.intent.action.QUICKBOOT_POWERON" == intent.action) {
            // Crear un ServiceConnection para conectarse al servicio TTS
            /*
                        val serviceConnection = object : ServiceConnection {
                            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                                // Obtener el IBinder del servicio TTS
                                val binder = service as LocalBinder
                                val theService = binder.getService() as TextToSpeechServiceImpl
                                context.applicationContext.speak("Sorete")
                                /*
                                                    Log.d("TTS 4","Voy a decir hola mundo")
                                                    // Llamar al método de reproducción de audio del servicio TTS
                                                    theService.speak("Hola, mundo!", TextToSpeech.QUEUE_FLUSH, null)
                                                    Log.d("TTS 4","Voy a decir sorete")
                                                 */
                            }

                            override fun onServiceDisconnected(name: ComponentName?) {
                                // El servicio TTS se ha desconectado
                            }
                        }

                        Log.d(TAG, "PASO 1")
                        // Conectarse al servicio TTS
                        val intent = Intent(context, TextToSpeechServiceImpl::class.java)
                        Log.d(TAG, "PASO 2")

                        context.applicationContext.bindService(
                            intent,
                            serviceConnection,
                            Context.BIND_AUTO_CREATE
                        )
                        Log.d(TAG, "PASO 3")
            */

            //     context.speak("BootReceiver activado")
            //    MediaPlayerUtils.getInstance(context).startFindPhone()


            //      if (SessionApp.getInstance(context.applicationContext).isTrackingEnabled) {
            val appContext = context.applicationContext
            /*
                          if (!appContext.isServiceRunning(LocationJobService::class.java)) {
                              appContext.startService(Intent(context, LocationJobService::class.java))
                          }

             */
//            appContext.startService(Intent(context, LocationJobService::class.java))

            //      context.applicationContext.scheduleLocationUpdateJob()
            Log.d(TAG, "PASO 6")
            //scheduleLocationJob(appContext)


            //  }
            Log.d(TAG, "EMG_ Inicio ")
            if (FirebaseAuth.getInstance().currentUser != null) {

                Log.d("BOOTEO", "El usuario de IAN estaba logueado.")

                //----------------------------------------------------------------
                if (context.isGPSEnabled()) {
//                    context.startLocationService()


                    val serviceConnection = object : ServiceConnection {

                        override fun onServiceConnected(className: ComponentName, service: IBinder) {
                            // Este método se llama cuando la conexión con el servicio se ha establecido.
                            // 'service' es la interfaz de comunicación que podemos usar para interactuar con el servicio.

                            Log.d("BOOTEO", "Se vinculo al servicio.")


                            // Por ejemplo, si tu servicio tiene un método personalizado para devolver una instancia del servicio:
                            val binder = service as ServiceLocation.ServiceLocationBinder
                            val service = binder.getService()


                            // Ahora puedes llamar a los métodos públicos de 'myService'.

                            val receiver = AppStatusReceiver(service)
                            val filter = IntentFilter()
                            filter.addAction(AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND)
                            filter.addAction(AppConstants.BROADCAST_ACTION_ENTER_FOREGROUND)
                            LocalBroadcastManager.getInstance(context).registerReceiver(
                                receiver, filter
                            )
                            Log.d("BOOTEO", "Ordeno iniciar el servicio en segundo plano.")

                            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(
                                AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND))

                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            // Este método se llama cuando la conexión con el servicio se ha interrumpido inesperadamente,
                            // por ejemplo, cuando el servicio se ha caído o se ha detenido.
                        }
                    }

                    val intent = Intent(context, ServiceLocation::class.java)
                    intent.putExtra("autonomous_start", true)

                    Log.d("BOOTEO", "Creo el servicio de ubicacion.")



                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }

                    //context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                    //Log.d("BOOTEO", "Ordeno vincular el servicio.")







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

                // --------- ITAG SERVICE
                if (SessionApp.getInstance(context.applicationContext).isBTPanicButtonEnabled) {
                    Log.d(TAG, "EMG_ Inicio servicio de ITag")
                    //    ITagsService.start(context.applicationContext) // expected to create application and thus start waytooday
                    ITag.initITag(context.applicationContext)
                    AppClass.instance.initializeITags()
                }


            } else {
                Log.d(TAG, "LOC_ NO inicio el servicio de ubicación por no estar logueado")
            }
        }
    }


    /*
        private fun scheduleLocationUpdateJob(appContext: Context) {
            val jobScheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName = ComponentName(appContext, LocationJobService::class.java)

            val jobInfo = JobInfo.Builder(1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setPersisted(true)
              //  .setExtras(extras)
                .build()

            val resultCode = jobScheduler.schedule(jobInfo)
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                // El trabajo se programó con éxito
                Log.d("LOCATION_JOB_SERVICE", "Voy a iniciar el trackeo")
                appContext.speak("Iniciando servicio de Tracking")

            } else {
                // Error al programar el trabajo
                Log.d("LOCATION_JOB_SERVICE", "No pude iniciar el trackeo")

                appContext.speak("Error al iniciar servicio de Tracking")
            }
        }
    */


    /*
        private fun scheduleLocationJob(context: Context) {

            Log.d(TAG, "PASO 7")
            val componentName = ComponentName(context, NewLocationJobService::class.java)
            val builder = JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.MINUTES.toMillis(1)) // Intervalo de actualización (por ejemplo, cada 15 minutos)
                .setPersisted(true) // Para que la tarea sobreviva al reinicio del dispositivo
            try {


                Log.d(TAG, "PASO 7 - 1")
                val jobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                Log.d(TAG, "PASO 7 - 2")
                jobScheduler.schedule(builder.build())
                Log.d(TAG, "PASO 7 - 3")
            }
            catch (ex : Exception)
            {
                Log.d(TAG, "PASO 7 - 4 - Error: "+ex.message)
            }
        }
    */
    companion object {
        private const val JOB_ID = 123 // Identificador único para la tarea en segundo plano
        private const val TAG =
            "BOOT_RECEIVER" // Identificador único para la tarea en segundo plano

    }

}
