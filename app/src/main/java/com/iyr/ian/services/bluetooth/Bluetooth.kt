package com.iyr.ian.services.bluetooth



import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.iyr.ian.AppConstants.Companion.CHANNEL_DEFAULT_ID
import com.iyr.ian.R
import com.iyr.ian.services.bluetooth.interfaces.Callback
import com.iyr.ian.services.bluetooth.interfaces.Processor


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class Bluetooth : Service() {
    private var context: Context? = null
    private var callback: Callback? = null
    private var processor: Processor? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mHandler: Handler? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null
    private val mBinder: IBinder = BluetoothBinder()

    // for service binding
    inner class BluetoothBinder : Binder() {
        val service: Bluetooth
            get() = this@Bluetooth
    }

    fun initialize(context: Context?, callback: Callback?, processor: Processor?) {
        this.context = context
        this.processor = processor
        val bluetoothManager =
            this.context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter!!.bluetoothLeScanner
        }
        mHandler = Handler(this.context!!.mainLooper)
        this.callback = callback
    }

    fun activate() {
        scanLeDevice(true)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun deactivate() {
        callback?.notifyState(BLUETOOTH_STATE_INACTIVE)
        disconnect()
        scanLeDevice(false)
        stopForeground(SERVICE_FOREGROUND_ID)
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        if (mGatt != null) {
            mGatt!!.close()
            mGatt = null
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            disconnect()
            callback?.notifyState(BLUETOOTH_STATE_SCANNING)
            startScan()
        } else {
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        mBluetoothAdapter!!.startDiscovery()
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            mLEScanner!!.startScan(mScanCallback)
        }

    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        } else {
            mLEScanner!!.stopScan(mScanCallback)
        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("callbackType", callbackType.toString())
            Log.i("result", result.toString())
            val btDevice = result.device
            if (btDevice != null) {
                btDevice.name?.let {
                    var oo = 33

                }

                if (btDevice.name != null && btDevice.name == BLUETOOTH_NAME) {
                    connectToDevice(btDevice)
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i("scanResult - results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("scan failed", "error Code: $errorCode")
        }
    }
    private val mLeScanCallback =
        LeScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
        //        Log.i("onLeScan", device.toString())
                connectToDevice(device)
            }
        }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            mGatt = device.connectGatt(context, false, gattCallback)
            scanLeDevice(false)
        }
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("onConnectionStateChange", "Status: $status")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("gattCallback", "STATE_CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e("gattCallback", "STATE_DISCONNECTED")
                    scanLeDevice(true)
                }
                else -> Log.e("gattCallback", "STATE_OTHER")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    val receiveCharacteristic =
                        gatt.services[BLUETOOTH_SERVICE].characteristics[BLUETOOTH_CHARACTERISTIC]
                    val receiveConfigDescriptor =
                        receiveCharacteristic.descriptors[BLUETOOTH_DESCRIPTOR]
                    if (receiveConfigDescriptor != null) {
                        Log.i(ContentValues.TAG, "successfully connected!")
                        gatt.setCharacteristicNotification(receiveCharacteristic, true)
                        receiveConfigDescriptor.value =
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(receiveConfigDescriptor)
                        callback?.notifyState(BLUETOOTH_STATE_ACTIVE)
                    } else {
                        Log.e(ContentValues.TAG, "receive config descriptor not found!")
                    }
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "receive characteristic not found!")
                }
            } else {
                Log.w(ContentValues.TAG, "onServicesDiscovered received: $status")
            }
        }

        // triggered if got some signal from the connected device
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            val signal = characteristic.value[0]
                .toInt().toString()
            Log.i("signal", signal)
            processor?.process(signal)
        }
    }

    private fun runOnUiThread(r: Runnable) {
        mHandler!!.post(r)
    }

    // show notification to user
    // this function is needed in order to push service in foreground
    val notification: Notification
        get() {
            val intent = Intent(this, Bluetooth::class.java)
            val pendingIntent = PendingIntent.getActivity(this, ACTIVITY_REQUEST_CODE, intent,
                PendingIntent.FLAG_IMMUTABLE)
            val foregroundNotification: NotificationCompat.Builder =
                NotificationCompat.Builder(this, CHANNEL_DEFAULT_ID)
            foregroundNotification.setOngoing(true)
            foregroundNotification.setContentTitle("Bluetooth service activated")
                .setContentText("Listening for signals..")
                .setSmallIcon(R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
            return foregroundNotification.build()
        }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    // triggered when starting service
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(SERVICE_FOREGROUND_ID, notification)
        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onDestroy() {
        super.onDestroy()
        deactivate()
    }

    companion object {
        private const val ACTIVITY_REQUEST_CODE = 0
        private const val SERVICE_FOREGROUND_ID = 1
        private const val BLUETOOTH_NAME = "cmpe272"
        const val BLUETOOTH_SERVICE = 3
        const val BLUETOOTH_CHARACTERISTIC = 0
        const val BLUETOOTH_DESCRIPTOR = 0
        const val BLUETOOTH_STATE_INACTIVE = 0
        const val BLUETOOTH_STATE_SCANNING = 1
        const val BLUETOOTH_STATE_ACTIVE = 2
    }
}