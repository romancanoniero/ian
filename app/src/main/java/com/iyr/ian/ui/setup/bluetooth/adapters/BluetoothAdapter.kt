package com.iyr.ian.ui.setup.bluetooth.adapters

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


import android.annotation.SuppressLint

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R


class BluetoothAdapter(
    val context: Context,
    private val items: MutableList<ScanResult>,
    private val onClickListener: ((device: BluetoothDevice) -> Unit)
) : RecyclerView.Adapter<BluetoothAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.row_scan_result,
            parent,
            false
        )
        return ViewHolder(context, view, onClickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
        private val context: Context,
        private val view: View,
        private val onClickListener: (device: BluetoothDevice) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        @SuppressLint("MissingPermission")
        fun bind(scanResult:  ScanResult) {

          val deviceName : TextView = view.findViewById(R.id.device_name)
          val macAddress : TextView = view.findViewById(R.id.mac_address)
          val signalStrength : TextView = view.findViewById(R.id.signal_strength)
            var gatt = scanResult.device.connectGatt(
                context,
                false,
                object : BluetoothGattCallback() {
                    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                        super.onReadRemoteRssi(gatt, rssi, status)
                        signalStrength.text = "${rssi} dBm"
                    }
                })
            deviceName.text = scanResult.device.name ?: "Unnamed"
            macAddress.text = scanResult.device.address
            signalStrength.text = scanResult.rssi.toString()
            view.setOnClickListener {
                onClickListener.invoke(scanResult.device)
            }
        }
    }
}