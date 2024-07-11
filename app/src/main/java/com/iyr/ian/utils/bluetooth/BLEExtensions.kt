package com.iyr.ian.utils.bluetooth

/*
 * Copyright 2019 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


// BluetoothAdapter
val BluetoothAdapter.isDisabled: Boolean
    get() = !isEnabled

var handler: Handler = Handler(Looper.getMainLooper())


/** UUID of the Client Characteristic Configuration Descriptor (0x2902). */
const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
const val BLUETOOTH_CONNECT_REQUEST_CODE = 4

// BluetoothGatt




fun Context.startBLEService(bleServiceConn: ServiceConnection) {
    CoroutineScope(Dispatchers.IO).launch {
 /*
        var intentService = Intent(this@startBLEService, ITagService::class.java)
        startService(intentService)
        bindService(intentService, bleServiceConn, Context.BIND_AUTO_CREATE)
   */
    }
}

fun Context.stopBLEService(bleServiceConn: ServiceConnection) {
    /*
    var intentService = Intent(this, ITagService::class.java)
    unbindService(bleServiceConn)
    stopService(Intent(this, ITagService::class.java))

     */
}

@SuppressLint("MissingPermission")
fun BluetoothGatt.readBatteryLevel() {
    val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    val batteryLevelChar =
        getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)!!

   // ConnectionManager.readCharacteristic(device, batteryLevelChar)
}

fun BluetoothGatt.printGattTable() {
    if (services.isEmpty()) {
        Log.i("BLE", "No service and characteristic available, call discoverServices() first?")
        return
    }
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { char ->
            var description = "${char.uuid}: ${char.printProperties()}"
            if (char.descriptors.isNotEmpty()) {
                description += "\n" + char.descriptors.joinToString(
                    separator = "\n|------",
                    prefix = "|------"
                ) { descriptor ->
                    "${descriptor.uuid}: ${descriptor.printProperties()}"
                }
            }
            description
        }
        Log.i("BLE", "Service ${service.uuid}\nCharacteristics:\n$characteristicsTable")
    }
}

fun BluetoothGatt.findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
    services?.forEach { service ->
        service.characteristics?.firstOrNull { characteristic ->
            characteristic.uuid == uuid
        }?.let { matchingCharacteristic ->
            return matchingCharacteristic
        }
    }
    return null
}

fun BluetoothGatt.findDescriptor(uuid: UUID): BluetoothGattDescriptor? {
    services?.forEach { service ->
        service.characteristics.forEach { characteristic ->
            characteristic.descriptors?.firstOrNull { descriptor ->
                descriptor.uuid == uuid
            }?.let { matchingDescriptor ->
                return matchingDescriptor
            }
        }
    }
    return null
}


@SuppressLint("MissingPermission")
fun BluetoothGatt.setCharacteristicNotificationCust(
    characteristic: BluetoothGattCharacteristic,
    enable: Boolean
): Boolean {
    Log.d("BLE", "setCharacteristicNotification")
    this.setCharacteristicNotification(characteristic, enable)

    val descriptor =
        characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
    descriptor.value =
        if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else byteArrayOf(0x00, 0x00)
    return this.writeDescriptor(descriptor) //descriptor write operation successfully started?
}


@SuppressLint("MissingPermission")
fun BluetoothGatt.sendImmediateAlarm(alarmValue: Int): Boolean {
    val alarmServiceUuid = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
    val alarmCharUuid = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
    var characteristic = this.getService(alarmServiceUuid)?.getCharacteristic(alarmCharUuid)

    val value = ByteArray(1)
    value[0] = (alarmValue and 0xFF).toByte()
    characteristic?.value = value
    val status: Boolean = writeCharacteristic(characteristic)
    return status
}


// BluetoothGattCharacteristic

fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isIndicatable()) add("INDICATABLE")
    if (isNotifiable()) add("NOTIFIABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

fun getCharacteristic(
    bluetoothgatt: BluetoothGatt?,
    serviceUuid: UUID,
    characteristicUuid: UUID
): BluetoothGattCharacteristic? {
    if (bluetoothgatt != null) {
        val service: BluetoothGattService? = bluetoothgatt.getService(serviceUuid)
        if (service != null) {
            return service.getCharacteristic(characteristicUuid)
        }
    }
    return null
}


// BluetoothGattDescriptor

fun BluetoothGattDescriptor.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0

/**
 * Convenience extension function that returns true if this [BluetoothGattDescriptor]
 * is a Client Characteristic Configuration Descriptor.
 */
fun BluetoothGattDescriptor.isCccd() =
    uuid.toString().uppercase(Locale.US) == CCC_DESCRIPTOR_UUID.uppercase(Locale.US)

// ByteArray

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


fun Context.broadcastMessage(data: Any?, action: String) {
    val intent = Intent(action)
    if (data is Bundle) {
        intent.putExtras(data)
    } else {
        data?.let {
            var dataJson = Gson().toJson(it)
            intent.putExtra("data", dataJson)
        }
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(
        intent
    )

}

fun Context.hasRequiredPermissionsBLE(): Boolean {
    if (Build.VERSION.SDK_INT >= 31) {
        // Android 12 (S)
        return hasPermissions(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        return hasPermissions(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    return false
}

fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.hasPermissions(vararg permissions: String): Boolean {
    var permissionGranted = true
    permissions.forEach { permission ->
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = false
            return@forEach
        }
    }
    return permissionGranted
}

fun AppCompatActivity.requirePermissionsBLE(requestCode: Int) {

    if (Build.VERSION.SDK_INT >= 31) {
        // Android 12 (S)

        requestPermissions(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            requestCode
        )
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                requestCode
            )
        }
    }

    //------------- OLD
    /*
      var requiredPermissions = mutableListOf<String>()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                  requestPermissions(
                      arrayOf(
                          Manifest.permission.BLUETOOTH_SCAN,
                          Manifest.permission.BLUETOOTH_CONNECT
                      ),
                      FULL_PERMISSIONS_REQUEST_CODE
                  )


              } else {
                  requestPermissions(
                      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                      FULL_PERMISSIONS_REQUEST_CODE
                  )

              }
          } else {
              requestPermissions(
                  arrayOf(
                      Manifest.permission.ACCESS_COARSE_LOCATION
                  ), FULL_PERMISSIONS_REQUEST_CODE
              )
          }
      }
      */


}


fun calculateDistanceX(txPower: Double, rssi: Double): Double {
    val ratio = rssi / txPower
    if (rssi == 0.0) { // Cannot determine accuracy, return -1.
        return -1.0
    } else if (ratio < 1.0) { //default ratio
        return Math.pow(ratio, 10.0)
    }//rssi is greater than transmission strength
    return (0.89976) * Math.pow(ratio, 7.7095) + 0.111
}

fun calculateDistance(txPower: Double, rssi: Double): Double {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */

    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */
    return Math.pow(10.0, (txPower - rssi) / (10 * 2))
}