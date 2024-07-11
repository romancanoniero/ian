package com.iyr.ian.utils.bluetooth.ble

import android.os.Handler
import android.os.Looper
import com.iyr.ian.utils.bluetooth.ble.BLEScannerInterface
import com.iyr.ian.utils.bluetooth.models.BLEScanResult
import com.iyr.ian.utils.bluetooth.ble.rasat.Channel
import com.iyr.ian.utils.bluetooth.ble.rasat.ChannelDistinct
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable

internal class BLEScannerDefault(private val manager: BLECentralManagerInterface) :
    BLEScannerInterface {
    private val channelTimer = Channel(0)
    private val channelActive = ChannelDistinct(false)
    private val channelScan = Channel<BLEScanResult>()

    // private final List<BLEDiscoveryResult> resultList = new ArrayList<>();
    private val handlerTimer = Handler(Looper.getMainLooper())
    private val runnableTimer: Runnable = object : Runnable {
        override fun run() {
            val timeout = channelTimer.observable.value() - 1
            channelTimer.broadcast(timeout)
            if (timeout < 0) {
                stop()
            } else {
                handlerTimer.postDelayed(this, 1000)
            }
        }
    }
    private val isScanning = arrayOf(false)
    override fun isScanning(): Boolean {
        synchronized(isScanning) { return isScanning[0] }
    }

    private fun setScanning(scanning: Boolean) {
        synchronized(isScanning) { isScanning[0] = scanning }
    }

    override fun observableTimer(): Observable<Int> {
        return channelTimer.observable
    }

    override fun observableScan(): Observable<BLEScanResult> {
        return channelScan.observable
    }

    override fun observableActive(): Observable<Boolean> {
        return channelActive.observable
    }

    private val disposableBag = DisposableBag()
    override fun start(timeout: Int, forceCancelIds: Array<String>) {
        stop()
        if (!manager.canScan()) return
        //                        resultList.add(result);
        disposableBag.add(
            manager.observables().observablePeripheralDiscovered()
                .subscribe { event: BLEDiscoveryResult ->
                    channelScan.broadcast(
                        BLEScanResult(
                            event.peripheral.address(),
                            event.peripheral.name()?:"",
                            event.rssi
                        )
                    )
                })
        setScanning(true)
        manager.startScan()
        channelTimer.broadcast(timeout)
        channelActive.broadcast(true)
        handlerTimer.postDelayed(runnableTimer, 1000)
    }

    override fun stop() {
        handlerTimer.removeCallbacks(runnableTimer)
        manager.stopScan()
        //   resultList.clear();
        disposableBag.dispose()
        setScanning(false)
        channelActive.broadcast(false)
    }
}