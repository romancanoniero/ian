package com.iyr.ian.services.falling_detection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.iyr.ian.R
import com.iyr.ian.services.falling_detection.Constants.BROADCAST_FALLING_EVENT
import com.iyr.ian.utils.playSound

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class FallDetectionServiceMethod : Service(), SensorEventListener {



    var handler = Handler(Looper.getMainLooper())
    private val mPeriodicEventHandler = Handler()
    private val fuseTimer = Timer()
    private var sendCount = 0
    private var sentRecently = 'N'

    // Indexes for x, y, and z values
    private val x = 0
    private val y = 1
    private val z = 2

    //Acceleration sensor
    var acceleration_g = 0.0

    // magnetic field vector
    private val magnet = FloatArray(3)

    var startTime: Long = 0 //Keeps the time the free fall started. e.g. 18:36:41


    //SOS button
    var layingOnGroundFlag = false

    // accelerometer vector
    private val accel = FloatArray(3)

    // orientation angles from accel and magnet
    private val accMagOrientation: FloatArray = FloatArray(3)


    // accelerometer and magnetometer based rotation matrix
    private val rotationMatrix = FloatArray(9)

    //Sensor Variables:
    private var senSensorManager: SensorManager? = null
    private var senAccelerometer: Sensor? = null
    var lastSecond //Keeps the last second, so that the countdown timer is updated once per second.
            = 0

    //GPS
    var latitude = 0.0
    var longitude = 0.0
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    @Volatile
    var running = true
    var freeFallFlag = false //Checks if a free fall has happened.

    var landingFlag = false //Checks if a landing has happened within a second after the free fall.


    private val doPeriodicTask = Runnable {
        Log.d("Delay", "Delay Ended**********")
        Log.d("Updating flag", "run: ")
        sentRecently = 'N'
        //            mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d("Initialing Service", "OnCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPeriodicEventHandler.removeCallbacks(doPeriodicTask)
        Log.d("Stopping Service", "OnDestroy")
        Log.d("SERVICE", "FALLING DETECTION STOPED")

        senSensorManager!!.unregisterListener(this)
        sendCount = 0
        Toast.makeText(this, "Stopped Tracking", Toast.LENGTH_SHORT).show()
        /*
         if (ActivityCompat.checkSelfPermission(
                 this,
                 Manifest.permission.ACCESS_COARSE_LOCATION
             ) !== PackageManager.PERMISSION_GRANTED
         ) {
         } else {
             */
        locationManager!!.removeUpdates(locationListener!!)
        fuseTimer.cancel()
        //}
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flag: Int, startId: Int): Int {
        Log.d("Starting work", "OnStart")
        /*
         val dpHelper = DBHelper(this)
         sql = dpHelper.getReadableDatabase()
 */
        Log.d("SERVICE", "FALLING DETECTION STARTED")
        // Acquire a reference to the system Location Manager
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        // Define a listener that responds to location updates
        locationListener = LocationListener {

            fun onLocationChanged(location: Location) {
                latitude = location.latitude
                longitude = location.longitude
                Log.d("latitude changed", "" + latitude)
                Log.d("longitude changed", "" + longitude)
            }

            fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            fun onProviderEnabled(provider: String) {}
            fun onProviderDisabled(provider: String) {
            }
        }

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            handler.post {
                Toast.makeText(
                    this@FallDetectionServiceMethod.applicationContext,
                    "No GPS Permission!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            5000,
            0f,
            locationListener!!
        )
        val locationProvider = LocationManager.NETWORK_PROVIDER
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            handler.post {
                Toast.makeText(
                    this@FallDetectionServiceMethod.applicationContext,
                    "No GPS Permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (locationManager!!.getLastKnownLocation(locationProvider) != null) {
            latitude = locationManager!!.getLastKnownLocation(locationProvider)!!.latitude
            longitude = locationManager!!.getLastKnownLocation(locationProvider)!!.longitude
        }
        Log.d("latitude", "" + latitude)
        Log.d("longitude", "" + longitude)

        onTaskRemoved(intent)
        senSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        senAccelerometer = senSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        initListeners()
        /*
        fuseTimer.scheduleAtFixedRate(
            calculateFusedOrientationTask(),
            1000, TIME_CONSTANT.toLong()
        )
        */
        return START_STICKY
    }

    private fun initListeners() {
        senSensorManager!!.registerListener(
            this,
            senSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        senSensorManager!!.registerListener(
            this,
            senSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        senSensorManager!!.registerListener(
            this,
            senSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        event.sensor
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                detectFall(
                    event.values[x].toDouble(), event.values[y].toDouble(),
                    event.values[z].toDouble()
                )
            }
        }
    }

    private fun detectFall(x: Double, y: Double, z: Double) {

        //Calculate the root-sum of squares of the signals of acceleration.
        val acceleration = sqrt(x.pow(2.0) + y.pow(2.0) + z.pow(2.0))

        //Convert it to g measurement.
        acceleration_g = acceleration / 9.80665
        //sqrtTextView.setText(Double.toString(acceleration_g));

        //PHASE ONE
        //In a free fall the x,y,z values of the accelerometer are near zero.
        if (acceleration_g < 0.3) //Free fall
        {
            freeFallFlag = true
            if (this.running === false) {
                running = true
            }
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
        }
    }


    //Runs without a timer by reposting this handler at the end of the runnable.
    var timerHandler = Handler()
    private var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!running) return
            val millis = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            seconds %= 60

            //PHASE TWO
            //A fall generally occurs in a short period of 0.4 - 0.8s.
            //A landing must happen within a second after a free fall has been detected.
            //If the vector sum raises to a value over 30m/s, the phone/person has landed.
            if (seconds <= 1 && acceleration_g > 2) //CONVERT TO 1
            {
                landingFlag = true
                //        AppClass.instance.getCurrentActivity()?.playSound(R.raw.policesiren)
                //        broadcastFallingStatus(LatLng(latitude, longitude))
            }
            //PHASE THREE
            //If the phone is not moving for two seconds after the landing, the user/phone is laying on the ground.
            if (seconds <= 3 && landingFlag && acceleration_g >= 0.9 && acceleration_g <= 1.1) {
                applicationContext.playSound(R.raw.policesiren)
                broadcastFallingStatus(LatLng(latitude, longitude))

                layingOnGroundFlag = true //Flag to start the reverse counting.

            }
            if (layingOnGroundFlag === true) {
                resetVariables()
                //   sosButton.setVisibility(View.INVISIBLE)
                if (seconds != lastSecond) {
                    //     showReverseTimer(seconds) //Start countdown.
                    lastSecond = seconds
                }


            } else if (seconds > 3 && !landingFlag) {
                //reset variables??
                running = false
                //      timerTextView.setText(String.format("-----"))

                freeFallFlag = false
            }
            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        //    Log.d("FALLING_SERVICE", sensor.toString() + "-" + i);
    }

    fun calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation)
        }
    }


    fun showReverseTimer(layingOnGroundSeconds: Int) {
        //    timerTextView.setVisibility(View.VISIBLE)
        //    abortButton.setVisibility(View.VISIBLE)
        //    sqrtTextView.setVisibility(View.VISIBLE)
        val remainingTime = 30 - layingOnGroundSeconds
        if (remainingTime == 0) {
            //      timerTextView.setVisibility(View.INVISIBLE)
            //Toast.makeText(this, "SOS sent!!", Toast.LENGTH_SHORT).show();
            running = false //stop the timer.

            //Write to firebase.
//            val currentTimestamp = Timestamp(System.currentTimeMillis())
            /*
             addWarning(username, "fall", latitudeTemp, longitudeTemp, currentTimestamp.toString())
             val sosToastMessage1: String =
                 this.getResources().getString(R.string.sosToastMessage1)
             val sosToastMessage2: String =
                 this.getResources().getString(R.string.sosToastMessage2)
             val sosFinalMessage =
                 sosToastMessage1 + latitudeTemp.toString() + ", " + longitudeTemp.toString() + sosToastMessage2

             //String sosToastMessage = MainActivity.this.getResources().getString(R.string.sosToastMessage);
             Toast.makeText(this@MainActivity, sosFinalMessage, Toast.LENGTH_SHORT).show()


             /*Read Database records and send SMS to all. */
             val buffer = StringBuffer()
             val cursor2: Cursor = db.rawQuery("SELECT * FROM contacts", null)
             if (cursor2.getCount() !== 0) {
                 while (cursor2.moveToNext()) {

                     //Send SMS to all numbers in the contact list.
                     sendSMS(cursor2.getString(1), sosFinalMessage)
                 }
             } else {
                 Toast.makeText(this@MainActivity, "No records found !", Toast.LENGTH_LONG).show()
             }
             */
            resetVariables()
        } else {
            /*
                  timerTextView.setText(String.format("%02d", remainingTime))
                  tts.speak(
                      String.format(Integer.toString(remainingTime)),
                      TextToSpeech.QUEUE_FLUSH,
                      null
                  )

             */
        }
    }


    fun resetVariables() {
        /*
          abortButton.setVisibility(View.INVISIBLE)
          timerTextView.setVisibility(View.INVISIBLE)
          sqrtTextView.setVisibility(View.INVISIBLE)
          sosButton.setVisibility(View.VISIBLE)
         */
        freeFallFlag = false

        landingFlag = false
        running = false
        layingOnGroundFlag = false
        //    tts.stop()
    }


    companion object;


    private fun broadcastFallingStatus(latLng: LatLng): Boolean {
        //    Log.d(TAG, "Emito el mensaje de cambio de ubicacion")
        val intent = Intent(applicationContext, FallDetectionServiceMethod::class.java)
        intent.action = BROADCAST_FALLING_EVENT
        val data = HashMap<String, Any>()
        data["location"] = latLng
        val dataJson = (Gson()).toJson(data)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            intent.putExtra(
                "data",
                dataJson
            )

        )
        return true
    }


    open class FallingReceiver : BroadcastReceiver {

        constructor()


        override fun peekService(myContext: Context?, service: Intent?): IBinder {
            return super.peekService(myContext, service)
        }

        override fun onReceive(context: Context, intent: Intent) {

         //   Gson().fromJson(intent.getStringExtra("data"), LocationUpdate::class.java)

        }


    }
}