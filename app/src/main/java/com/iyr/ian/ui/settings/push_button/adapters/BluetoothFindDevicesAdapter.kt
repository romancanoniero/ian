package com.iyr.ian.ui.settings.push_button.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.BuildConfig
import com.iyr.ian.R
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagDefault
import com.iyr.ian.utils.bluetooth.models.BLEScanResult
import com.iyr.ian.utils.bluetooth.views.RssiView


class BluetoothFindDevicesAdapter() :
    RecyclerView.Adapter<BluetoothFindDevicesAdapter.ViewHolder>() {
    private var context: Context? = null

    private val scanResults: ArrayList<BLEScanResult> = ArrayList<BLEScanResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        context = parent.context

        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_le_scan_item, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(
                "ITag",
                "onBindViewHolder position=" + position + " thread=" + Thread.currentThread().name
            )
        }
        val scanResult: BLEScanResult = scanResults.get(position)
        val onClickListener = View.OnClickListener { sender: View? ->
            if (BuildConfig.DEBUG) {
                Log.d(
                    "ITag", "onRemember  thread=" + Thread.currentThread().name
                )
            }
            if (!ITag.store.remembered(scanResult.id)) {
                ITag.store.remember(ITagDefault(scanResult))
                ITag.ble.scanner().stop()
                //btScannerDialog.dismiss()
            }
        }
        holder.textName.text = scanResult.name
        holder.textAddr.text = scanResult.id
        holder.btnRemember.tag = scanResult
        holder.btnRemember.setOnClickListener(onClickListener)
        holder.btnRemember2.setOnClickListener(onClickListener)
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(-0x1f1f20)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
        holder.rssiView.setRssi(scanResult.rssi!!)

        holder.textRSSI.text =
            String.format(context!!.getString(R.string.rssi), scanResult.rssi)

    }

    override fun getItemCount(): Int {
        return scanResults.size
    }


    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val textName: TextView
        val textAddr: TextView
        val textRSSI: TextView
        val rssiView: RssiView
        val btnRemember: ImageView
        val btnRemember2: View


        init {
            textName = rootView.findViewById(R.id.text_name)
            textAddr = rootView.findViewById(R.id.text_addr)
            textRSSI = rootView.findViewById(R.id.text_rssi)
            rssiView = rootView.findViewById(R.id.rssi)

            btnRemember = rootView.findViewById(R.id.btn_connect)
            btnRemember2 = rootView.findViewById(R.id.btn_connect_2)
        }
    }

}

