package com.iyr.ian.ui.settings.push_button.dialogs

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.AppConstants
import com.iyr.ian.BuildConfig
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagDefault
import com.iyr.ian.ui.base.OnConfirmationButtonsListener
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.models.BLEScanResult
import com.iyr.ian.utils.bluetooth.views.RssiView
import java.util.Locale


class BTScannerDialog(context: Context, activity: Activity) :

    AlertDialog(context) {
    private val mThisDialog: BTScannerDialog
    private val mContext: Context
    private var mActivity: Activity
    private var mCallback: OnConfirmationButtonsListener? = null
    private val mDialoglayout: View
    private var tv: TextView? = null
    private var progressBar: ProgressBar? = null
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private var mButton2Caption: String? = null

    private val disposableBag = DisposableBag()
    private var adapter: Adapter? = null

    companion object {
        private val scanResults: ArrayList<BLEScanResult> = ArrayList<BLEScanResult>()
    }

    private var lastUpdate: Long = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTitle != null) {
            val title = mDialoglayout.findViewById<TextView>(R.id.dialogTitle)
            title.text = mTitle
        }
        if (mLegend != null) {
            val legend = mDialoglayout.findViewById<TextView>(R.id.dialogMessage)
            legend.text = mLegend
        }
        if (mButton1Caption != null) {
            val acceptButton = mDialoglayout.findViewById<Button>(R.id.acceptButton)
            acceptButton.text = mButton1Caption
        }
        if (mButton2Caption != null) {
            val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancelButton)
            cancelButton.text = mButton2Caption
        }
        tv = mDialoglayout.findViewById<TextView>(R.id.text_scanning)
        progressBar = mDialoglayout.findViewById<ProgressBar>(R.id.progress)
        setupDisposableBag()
        setupProgressBar()
        onStartStopScan()
    }


    private fun setupProgressBar() {
        val pb: ProgressBar = progressBar!!
        if (ITag.ble.scanner().isScanning) {
            pb.visibility = View.VISIBLE
            pb.isIndeterminate = false
            pb.max = ITag.SCAN_TIMEOUT
            pb.progress = ITag.ble.scanner().observableTimer().value()

        } else {
            pb.visibility = View.GONE
        }
    }

    private fun setupDisposableBag() {
        disposableBag.add(ITag.ble.scanner().observableScan().subscribe { result: BLEScanResult ->
            if (ITag.store.remembered(result.id)) {
                return@subscribe
            }
            if (adapter == null) {
                return@subscribe
            }
            var found = false
            var modified = false
            for (scanResult in scanResults) {
                if (scanResult.id == result.id) {
                    if (scanResult.rssi !== result.rssi) {
                        modified = true
                        scanResult.rssi = result.rssi
                    }
                    found = true
                    break
                }
            }
            if (!found) {
                if (BuildConfig.DEBUG) {
                    Log.d("ITag", "found=$found")
                }
                Log.d("ITag", "found=$result")

                if (result.name?.lowercase(Locale.getDefault())!!.contains("itag")) {
                    scanResults.add(result)
                }
                adapter?.notifyDataSetChanged()
                lastUpdate = System.currentTimeMillis()
            }
            if (modified) {
                if (System.currentTimeMillis() - lastUpdate > 1000) {
                    if (BuildConfig.DEBUG) {
                        Log.d("ITag", "modified=$modified")
                    }
                    adapter?.notifyDataSetChanged()
                    lastUpdate = System.currentTimeMillis()
                }
            }
        })

        disposableBag.add(ITag.ble.scanner().observableTimer().subscribe { tick: Int? ->
            updateResultsList()
        })

        disposableBag.add(ITag.ble.scanner().observableActive().subscribe { active: Boolean? ->
            if (!active!!) {
                return@subscribe
            }
            if (adapter == null) {
                return@subscribe
            }
            scanResults.clear()
            adapter?.notifyDataSetChanged()
        })
    }


    private fun updateResultsList() {
        if (ITag.ble.scanner().observableTimer().value() > 0) {
            if (adapter?.itemCount ?: 0 > 0) {

                tv?.setText(R.string.scanning_more)
            } else if (ITag.store.count() > 0) {
                tv?.setText(R.string.scanning_new)
            } else {
                tv?.setText(R.string.scanning)
            }
        } else {
            tv?.setText(R.string.scanning_stopped)
        }
        setupProgressBar()
    }


    fun setCallback(callback: OnConfirmationButtonsListener) {
        mCallback = callback
    }


    init {
        mContext = context
        mActivity = activity
        mThisDialog = this
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_bluetooth_scanner_popup, null)
        this.setView(mDialoglayout)
        val devicesRecyclerView = mDialoglayout.findViewById<RecyclerView>(R.id.recycler_devices)

        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancelButton)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        adapter = Adapter(this)
        devicesRecyclerView.setHasFixedSize(true)
        devicesRecyclerView.layoutManager = LinearLayoutManager(getContext())
        devicesRecyclerView.adapter = adapter

        cancelButton.setOnClickListener {
            context.handleTouch()
            mThisDialog.dismiss()
            dismiss()
            if (mCallback != null) {
                mCallback!!.onCancel()
            }
        }
    }


    fun onStartStopScan() {
        if (BuildConfig.DEBUG) {
            Log.d(
                "ITag",
                "onStartStopScan isScanning=" + ITag.ble.scanner().isScanning + " thread=" + Thread.currentThread().name
            )
        }
        if (ITag.ble.scanner().isScanning) {
            ITag.ble.scanner().stop()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(
                            mActivity, Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        val builder = android.app.AlertDialog.Builder(context)
                        builder.setMessage(R.string.request_location_permission)
                            .setTitle(R.string.request_permission_title).setPositiveButton(
                                android.R.string.ok
                            ) { dialog: DialogInterface?, which: Int ->
                                requestPermissions(
                                    mActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    Constants.LOCATION_PERMISSION_REQUEST_CODE
                                )
                            }.setNegativeButton(
                                android.R.string.cancel
                            ) { dialog: DialogInterface, which: Int -> dialog.cancel() }.show()
                        return
                    } else {
                        // isScanRequestAbortedBecauseOfPermission=true;
                        requestPermissions(
                            mActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            Constants.LOCATION_PERMISSION_REQUEST_CODE
                        )
                        return
                    }
                }
            }
            ITag.ble.scanner().start(ITag.SCAN_TIMEOUT, arrayOf())
        }
        setupProgressBar()
    }


    private class Adapter(val btScannerDialog: BTScannerDialog) :
        RecyclerView.Adapter<ViewHolder>() {
        private var context: Context? = null

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
                    btScannerDialog.dismiss()
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


    }


    internal class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
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