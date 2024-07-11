package com.iyr.ian.itag

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.iyr.ian.BuildConfig
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.Notifications.EXTRA_STOP_SOUND
import com.iyr.ian.services.location.isServiceRunningInForeground
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.errors.ErrorsObservable
import com.iyr.ian.utils.multimedia.MediaPlayerUtils

class ITagsService : Service() {


    inner class ITagBinder : Binder() {
        fun removeFromForeground() {
            this@ITagsService.removeFromForeground()
        }
    }

    private val connectedDevicesIDs: ArrayList<String> = ArrayList<String>()
    private val mBinder: IBinder = ITagBinder()
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onCreate")
        }
    }

    override fun onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onDestroy")
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private var inForeground = false
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d("ITags", "onStartCommand")

        if (intent != null) {
            if (ACTION_STOP == intent.action) {
                stopSelf()
                return START_NOT_STICKY
            } else if (ACTION_START == intent.action) {
                return if (intent.getBooleanExtra(
                        EXTRA_STOP_SOUND, false
                    )
                ) {
                    MediaPlayerUtils.getInstance(applicationContext).stopSound()
//                    MultimediaUtils.getInstance(applicationContext).stopSound(this)
                    START_STICKY
                } else {
                    if (!inForeground) {
                        putInForeground()
                    }
                    // return START_REDELIVER_INTENT;
                    START_STICKY
                }
            } else if (ACTION_BIND == intent.action) {
                if (inForeground) {
                    removeFromForeground()
                }
                return START_NOT_STICKY
            }
        } else {
            if (!inForeground) {
                putInForeground()
            }
            return START_STICKY
        }
        return START_NOT_STICKY
    }

    fun putInForeground() {

        val isRunning = isServiceRunningInForeground()
        if (inForeground) {
            return
        }
        inForeground = true
        startForeground(FOREGROUND_ID, createForegroundNotification(this.applicationContext))
        startTagListening()
    }

    fun removeFromForeground() {
        stopForeground(true)
        inForeground = false
    }

    companion object {
        const val ACTION_STOP = "s4y.itag.stop"
        const val ACTION_BIND = "s4y.itag.bind"
        const val ACTION_START = "s4y.itag.start"
        const val FOREGROUND_ID = 1
        const val FOREGROUND_CHANNEL_ID = "itag3"
        private val LT = ITagsService::class.java.name

        private fun intent(context: Context): Intent {
            return Intent(context, ITagsService::class.java)
        }


        fun intentStart(context: Context): Intent {
            val intent = intent(context)
            intent.action = ACTION_START
            return intent
        }

        fun intentBind(context: Context): Intent {
            val intent = intent(context)
            intent.action = ACTION_BIND
            return intent
        }

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("ITags", "Inicio Servicio ITagService en Foreground")

                context.startForegroundService(intentStart(context))
            } else {
                Log.d("ITags", "Inicio Servicio  ITagService  Normalmente")
                try {
                    context.startService(intentStart(context))
                } catch (ex: Exception) {
                    Log.d("ITags", "Error: " + ex.localizedMessage.toString())

                }
            }


        }

        fun stop(context: Context) {
            context.stopService(intentStop(context))
        }

        fun intentStop(context: Context): Intent {

            Log.d("ITags", "El ITagService se detuvo")


            val intent = intent(context)
            intent.action = ACTION_STOP
            return intent
        }

    }


    private var createdForegroundChannel = false
    private fun createForegroundNotificationChannel(context: Context) {
        if (!createdForegroundChannel && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel)
                createdForegroundChannel = true
            }
        }
    }

    fun createForegroundNotification(context: Context): Notification {
        createForegroundNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
        builder.setTicker(null).setSmallIcon(R.mipmap.ic_custom_launcher)
            .setContentTitle(context.getString(R.string.service_in_background))
            .setContentText(context.getString(R.string.service_description))
        val intent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(
            0, PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)
        return builder.build()
    }


    private val disposableBag: DisposableBag = DisposableBag()
    private val mErrorListener: ErrorsObservable.IErrorListener =
        ErrorsObservable.IErrorListener { errorNotification ->
/*
                runOnUiThread {
        Toast.makeText(
                this@MainActivity,
        errorNotification.getMessage(),
                Toast.LENGTH_LONG
                ).show()
    }
*/
            Log.e(LT, errorNotification.message, errorNotification.th)
        }

//***********************

    private fun startTagListening() {

        Log.d("ITags", "Comienzo a escuchar los Tags")
        Log.d("ITags", " Tags recordados ${ITag.store.count()}")
        for (i in 0 until ITag.store.count()) {
            val tagId: String = ITag.store.byPos(i).id().toString()
            val connection: BLEConnectionInterface = ITag.ble.connectionById(tagId)

            Log.d("ITags", "Empiezo a escuchar a $tagId")

//        connection.connect()

            disposableBag.add(connection.observableRSSI()
                .subscribe { rssi ->  //  updateRSSI(id, rssi))

                    Log.d("ITags", "Empiezo a escuchar a RSSI $rssi")

                    var pp = 2/*
                                        getCurrentActivity()!!.runOnUiThread {
                                            Toast.makeText(applicationContext, "updateRSSI", Toast.LENGTH_LONG).show()

                                        }
                                        */
                })
            disposableBag.add(connection.observableImmediateAlert().subscribe { state ->
                //Toast.makeText(this@MainActivity, "updateITagImageAnimation", Toast.LENGTH_LONG).show()
                var pp = 2/*
                    updateITagImageAnimation(
                        itag,
                        connection
                    )

         */
            })
            disposableBag.add(connection.observableState().subscribe { state ->
                var pp = 333

                Log.d(
                    "ITags",
                    "Empiezo a escuchar a STATE ${connection.id()} - Estado: $state"
                )


                when (state) {
                    BLEConnectionState.disconnected -> {
                        ITag.store.forget(connection.id())
                        if (connectedDevicesIDs.contains(connection.id())) {
                            connectedDevicesIDs.remove(connection.id())

                        }


                    }

                    else -> {}
                }/*
                                getCurrentActivity()?.runOnUiThread {
                                    if (BuildConfig.DEBUG) {

                                        Toast.makeText(
                                            applicationContext, "connection  state changed " + connection.state()
                                                .toString(), Toast.LENGTH_LONG
                                        ).show()

                                    }
                                }
                                */
            })
            disposableBag.add(connection.observableClick().subscribe { event ->
                Log.d("ITags", "El boton fue presionado")
                //  if (connection.isFindMe) {
        /*
                applicationContext.broadcastMessage(
                    null, BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED
                )
          */
                //    }


                Log.d("ITags", "startTagListening() - onEmergencyButtonPressed()")
                AppClass.instance.onEmergencyButtonPressed()

                Toast.makeText(applicationContext, "observableClick()", Toast.LENGTH_LONG).show()

            })
        }

    }

}



