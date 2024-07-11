package com.iyr.ian.ui.settings.push_button

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.*
import com.clj.fastble.scan.BleScanRuleConfig.*
import com.iyr.ian.BuildConfig
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.itag.ITagsService.Companion.intentBind
import com.iyr.ian.databinding.FragmentBluetoothConfigurationBinding
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITag.ble
import com.iyr.ian.itag.ITagInterface
import com.iyr.ian.itag.TagColor
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.push_button.fragments.DisabledBLEFragment
import com.iyr.ian.ui.settings.push_button.fragments.ITagsFragment
import com.iyr.ian.ui.settings.push_button.fragments.NoBLEFragment
import com.iyr.ian.ui.settings.push_button.fragments.ScanFragment
import com.iyr.ian.utils.bluetooth.BLUETOOTH_CONNECT_REQUEST_CODE
import com.iyr.ian.utils.bluetooth.adapters.BLEDeviceAdapter
import com.iyr.ian.utils.bluetooth.ble.AlertVolume
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.BLEState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.hasRequiredPermissionsBLE
import com.iyr.ian.utils.bluetooth.models.BLEScanResult
import com.iyr.ian.utils.bluetooth.requirePermissionsBLE
import com.iyr.ian.utils.bluetooth.views.RssiView
import com.iyr.ian.utils.showErrorDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.math.pow


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private enum class FragmentType_old {
    OTHER, ITAGS, SCANNER
}

@SuppressLint("MissingPermission")
class PushButtonSetupFragment_old(private val _interface: ISettingsFragment) : Fragment() {
    private var mEnableAttempts = 0
    private val disposableBag = DisposableBag()
    private var scanning = false
    private var mSelectedFragment: FragmentType_old? = null

    private val handler = Handler(Looper.getMainLooper())

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private val mITagAnimation: Animation? = null
    private val tagViews: Map<String, ViewGroup> = HashMap()

    val REQUEST_ENABLE_BT = 1
    val REQUEST_ENABLE_LOCATION = 2
    var iTagsService: ITagsService? = null
    var sIsShown = false
    private val LT = MainActivity::class.java.name


    //    val iTagServiceUuid = ParcelUuid.fromString("0000ffee-0000-1000-8000-00805f9b34fb")
    val iTagServiceUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    // 00002902-0000-1000-8000-00805f9b34fb

    //0000ffe0-0000-1000-8000-00805f9b34fb
//    private var currentPressMode: ITagService.ButtonMode = ITagService.ButtonMode.OFF

    /*
        private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BROADCAST_MESSAGE_REFRESH_BLE_DEVICES_LIST -> {
                        scanResultAdapter?.notifyDataSetChanged()
                    }
                    BROADCAST_MESSAGE_PANIC_BUTTON_TEST -> {
                        Toast.makeText(context, "El Boton Funciona!", Toast.LENGTH_SHORT).show()
                    }

                    BROADCAST_MESSAGE_SCAN_RESULT_UPDATED -> {
                        val dataAsJson = intent.getStringExtra("data")
                        val listType = object : TypeToken<MutableList<ScanResult>>() {}.type
                        val scanResult = Gson().fromJson<MutableList<ScanResult>>(dataAsJson, listType)
                        updateScanResults(scanResult)

                    }
                    BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_LIST -> {
                        scanResultAdapter?.notifyDataSetChanged()
                    }
                    BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED -> {

                        bleService = AppClass.instance.getBLEService()

                        /*                 if (bleService?.isScanning == false) {
                                             startScanning()
                                         }
                          */
                        updateScanResults(bleService?.getDevicesList() as MutableList<ScanResult>)
                    }
                    BROADCAST_MESSAGE_BLE_SCAN_DISCOVERING -> {

                    }
                    BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED -> {
                        val dataAsJson = intent.getStringExtra("data")
                        val device =
                            Gson().fromJson<BluetoothDevice>(dataAsJson, BluetoothDevice::class.java)
                        SessionApp.getInstance(context).addBleDeviceToConnectList(device)
                        currentPressMode = ITagService.ButtonMode.ON
                        bleService?.pressMode = ITagService.ButtonMode.TEST
                        scanResultAdapter?.notifyDataSetChanged()
                    }
                }
            }

            private fun updateScanResults(results: MutableList<ScanResult>) {

                // borro los que ya no estan..
                // Recorro el anterior
                val devicesToDelete = mutableListOf<ScanResult>()
                val currentScanResults = bleService?.getDevicesList()
                currentScanResults?.forEach { existingDevice ->
                    var deviceIndex = -1
                    results.forEach { device ->
                        deviceIndex++
                        if (existingDevice.device.address == device.device.address) {
                            return@forEach
                        }
                    }

                    if (deviceIndex == -1) {
                        devicesToDelete.add(existingDevice)
                    }
                }
                // Borro los que ya no estan
                scanResults.removeAll(devicesToDelete)

                // Actualizo o Agrego
                results.forEach { device ->
                    var deviceIndex = -1
                    scanResultAdapter?.items?.forEach { oldRecord ->
                        deviceIndex++
                        if (oldRecord.device.address == device.device.address) {
                            return@forEach
                        }
                    }
                    try {
                        if (deviceIndex == -1) {
                            scanResultAdapter?.items?.add(device)
                            scanResultAdapter?.notifyItemInserted(scanResultAdapter?.items?.size!! - 1)
                        } else {
                            scanResultAdapter?.items?.set(deviceIndex, device)
                            scanResultAdapter?.notifyItemChanged(deviceIndex)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            }


        }
    */
    private lateinit var binding: FragmentBluetoothConfigurationBinding


    private var devicesList = ArrayList<ScanResult>()

//    private var bleService: ITagService? = null  // Servicio de bluetooth
/*

    private val bluetoothAdapter: android.bluetooth.BluetoothAdapter by lazy {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
*/

    //    private var adapter = BleDeviceAdapter()
//    private val scanResults: List<BLEScanResult> = ArrayList()
    private var adapter: BLEDeviceAdapter? = null
    private val bluetoothManager by lazy { requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private var lastUpdate: Long = 0

    /**
     * Conexion al Servicio de Bluetooth
     */
    /*
     var bleServiceConn: ServiceConnection = object : ServiceConnection {
         override fun onServiceConnected(p0: ComponentName?, service: IBinder) {
             Log.d("BLE", "onServiceConnected")
             val binder: ITagService.ServiceBinder = service as ITagService.ServiceBinder
             bleService = binder.service
             if (this@PushButtonSetupFragment is IBle) {
                 bleService?.setServiceCallback(this@PushButtonSetupFragment)
             }
             //   if (binding.switchEnabled.isChecked) {
             bleService?.scan()

             //      }
         }

         override fun onServiceDisconnected(p0: ComponentName?) {
             Log.d("BLE", "onServiceDisconnected")
         }
     }
    */
    private val scanResults =
        mutableListOf<BLEScanResult>() // Lista donde se almacenaran los resultados del scaneo


    //private var scanResultAdapter: ScanResultAdapter? = null

    private val leScanCallback: BluetoothAdapter.LeScanCallback =
        object : BluetoothAdapter.LeScanCallback {

            override fun onLeScan(device: BluetoothDevice?, rssi: Int, data: ByteArray?) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!device?.name.isNullOrEmpty() && device?.name?.contains("ITAG") != null) {
                        Log.d("ITAGBLE", "device_name : " + device.name)
                        if (device.name.toString().contains("ITAG")) {
                            //  adapter?.scanResults?.add(s)
                            adapter?.notifyDataSetChanged()
                        } else {


                        }
                        /*
                             val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                 BleDevice(
                                     result.device.name ?: "Unknown Device",
                                     getBatteryLevel(result.device)!! ,
                                     calculateDistance(result.rssi, result.txPower)
                                 )
                             } else {
                                 TODO("VERSION.SDK_INT < O")
                             }
                             (adapter as BleDeviceAdapter).addDevice(device)
         */
                    }

                }

            }
        }

    private val scanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            CoroutineScope(Dispatchers.IO).launch {
                if (!result.device.name.isNullOrEmpty() && result.device.name.contains("ITAG") != null) {
                    Log.d("ITAGBLE", "device_name : " + result.device.name)
                    if (result.device.name.toString().contains("ITAG")) {

                        val newDevice = BleDevice(
                            result.device,
                            result.rssi,
                            result.scanRecord?.bytes,
                            System.currentTimeMillis()
                        )
                        //  adapter?.scanResults?.add(result)
                        adapter?.notifyDataSetChanged()
                    } else {


                    }
                    /*
                         val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                             BleDevice(
                                 result.device.name ?: "Unknown Device",
                                 getBatteryLevel(result.device)!! ,
                                 calculateDistance(result.rssi, result.txPower)
                             )
                         } else {
                             TODO("VERSION.SDK_INT < O")
                         }
                         (adapter as BleDeviceAdapter).addDevice(device)
     */
                }

            }


        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothConfigurationBinding.inflate(layoutInflater, container, false)
        binding.recyclerDevices.adapter = adapter

        setupUI()
        /*
          setupUI()
          updateUI()

          bleService?.getDevicesList()
          if (binding.switchEnabled.isChecked) {


              scanResultAdapter?.items?.clear()
              AppClass.instance.getBLEService()?.getAvailableDevices()
                  ?.let {
                      scanResultAdapter?.items?.addAll(it)
                  }

              if (requireContext().hasRequiredPermissionsBLE()) {
                  //           startService()
                  if (requireContext().isServiceRunning(ITagService::class.java) == false) {
                      AppClass.instance.startBLEService()
                  }
                  if (bluetoothAdapter.enable()) {
                      /*
                          if (bleService == null)
                          else
                              (requireActivity() as AppCompatActivity).requirePermissionsBLE(
                                  REQUEST_CODE_FOR_SERVICE_CREATION
                              )
      */
                      AppClass.instance.getBLEService()?.scan()
                  }
              }
          }
          */
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startScanning()
    }



    internal class ITagServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val itagService = service as ITagsService.ITagBinder
            itagService.removeFromForeground()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val mServiceConnection = ITagServiceConnection()

    /*
        private val mErrorListener: ErrorsObservable.IErrorListener =
            ErrorsObservable.IErrorListener { errorNotification ->
                requireActivity().runOnUiThread(java.lang.Runnable {
                    Toast.makeText(
                        this.requireActivity(),
                        errorNotification.getMessage(),
                        Toast.LENGTH_LONG
                    ).show()
                })
                Log.e(LT, errorNotification.getMessage(), errorNotification.th)
            }
        private val gpsPermissionListener: IPermissionListener = IPermissionListener {
            PermissionHandling.requestPermissions(
                requireActivity()
            )
        }
        */
    private var resumeCount = 0

    private fun startScanning() {

        // val filters: MutableList<ScanFilter> = ArrayList()
        //Filter on just our requested namespaces
        //Filter on just our requested namespaces
        // Reemplaza "xxxx" con el UUID de servicio del iTag

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(iTagServiceUuid))
                .build()
        )

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        //bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
        //  bluetoothLeScanner?.startScan(null,scanSettings,scanCallback)

        bluetoothAdapter?.startLeScan(leScanCallback)
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(scanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(scanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    private suspend fun getBatteryLevel(device: BluetoothDevice): Int? = withTimeoutOrNull(10_000) {
        val batteryLevelChannel = Channel<Int?>()

        val gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val batteryService =
                        gatt.getService(UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"))
                    val batteryLevelCharacteristic =
                        batteryService?.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"))

                    if (batteryLevelCharacteristic != null) {
                        gatt.readCharacteristic(batteryLevelCharacteristic)
                    } else {
                        batteryLevelChannel.trySend(null).isSuccess
                        gatt.close()
                    }
                } else {
                    batteryLevelChannel.trySend(null).isSuccess
                    gatt.close()
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val batteryLevel =
                        characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    batteryLevelChannel.trySend(batteryLevel).isSuccess
                } else {
                    batteryLevelChannel.trySend(null).isSuccess
                }
                gatt.close()
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS || newState != BluetoothGatt.STATE_CONNECTED) {
                    batteryLevelChannel.trySend(null).isSuccess
                    gatt.close()
                } else {
                    gatt.discoverServices()
                }
            }
        })

        val batteryLevel = batteryLevelChannel.receive()
        batteryLevelChannel.close()
        batteryLevel
    }

    /**
     * Calcula la distancia entre el dispositivo y el iTag.
     *
     * @param rssi La intensidad de la señal en decibelios-miliwatt (dBm).
     * @param txPower La potencia de transmisión del iTag en decibelios-miliwatt (dBm).
     * @return La distancia estimada en metros.
     */
    fun calculateDistance(rssi: Int, txPower: Int): Double {
        if (rssi == 0) {
            return -1.0 // Si no hay señal, retorna -1
        }

        val ratio = rssi.toDouble() / txPower
        return if (ratio < 1.0) {
            ratio.pow(10.0)
        } else {
            (0.89976) * ratio.pow(7.7095) + 0.111
        }
    }
/*
    private fun setupScanAdapter(scanResults: MutableList<ScanResult>) {
        scanResultAdapter =
            ScanResultAdapter(requireContext(), this as IScanResultAdapter, this.scanResults)
            { device ->

                if (this@PushButtonSetupFragment.isConnected(device)) {
                    requireContext().broadcastMessage(
                        device,
                        BROADCAST_ACTION_BLE_DEVICE_REMOVE_FROM_AUTO_CONNECTION
                    )
                    bleService?.disconnect(device)


                } else {
                    bleService?.connect(device)
                    requireContext().broadcastMessage(
                        device,
                        BROADCAST_ACTION_BLE_DEVICE_ADD_TO_AUTO_CONNECTION
                    )
                }

            }


    }

    override fun isConnected(device: BluetoothDevice): Boolean {
        return bleService?.isDeviceConnected(device) ?: false
    }
*/
    /*
    { result ->

        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        Toast.makeText(requireContext(), "Putazo!!", Toast.LENGTH_SHORT).show()
        var isConnected = requireContext().getConnectedDevices().contains(result.address)

        //bluetoothManager.getConnectionState(result, BluetoothProfile.GATT)== BluetoothGatt.STATE_CONNECTED

        if (!isConnected) {
            with(result) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                Toast.makeText(requireContext(), "Connecting to $address", Toast.LENGTH_SHORT)
                    .show()

                //   bleService?.connect(result)


                SessionApp.getInstance(requireContext()).addDeviceToConnectedList(result)
                //    CoroutineScope(Dispatchers.Main).launch {

                AppClass.instance.connect(result)
                //  }
            }
        } else {
            with(result) {

                requireActivity().runOnUiThread {
                    Log.w("ScanResultAdapter", "Disconnecting from $address")
                    Toast.makeText(
                        requireContext(),
                        "Disconnecting from $address",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    AppClass.instance.getBLEService()?.disconnect(result)
                    SessionApp.getInstance(requireContext())
                        .removeDeviceFromConnectedList(result)
                    scanResultAdapter.notifyDataSetChanged()
                }


            }
        }
    }
*/

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        //      setupScanAdapter(scanResults)
        BleManager.getInstance().init(AppClass.instance)
        if (BleManager.getInstance().isSupportBle == false) {
            requireActivity().showErrorDialog("Tu dispositivo no permite conectarse a dispositivos Bluetooth LE")

        }

    }


    companion object {
        @JvmStatic
        fun newInstance(context: Context, _interface: ISettingsFragment) =
            PushButtonSetupFragment_old( _interface)
    }

    @SuppressLint("MissingPermission")
    private fun setupUI() {

//        val view: View = inflater.inflate(R.layout.fragment_le_scan, container, false)
        val recyclerView: RecyclerView = binding.recyclerDevices
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

//        adapter = BLEDeviceAdapter(context,this)
        recyclerView.adapter = adapter
/*
        scanResultAdapter =
            ScanResultAdapter(requireContext(), this as IScanResultAdapter, this.scanResults)
*/
        if (BleManager.getInstance().isSupportBle == false) {
            binding.switchEnabled.isChecked = false
        } else {
            binding.switchEnabled.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    if (requireContext().hasRequiredPermissionsBLE()) {
                        BleManager.getInstance().enableBluetooth()
                        ble.scanner().start(ITag.SCAN_TIMEOUT, arrayOf<String>())
                    } else {
                        (requireActivity() as AppCompatActivity).requirePermissionsBLE(
                            BLUETOOTH_CONNECT_REQUEST_CODE
                        )
                    }
/*
                    ,
                    UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"),
                    UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
                    0000ffe1-0000-1000-8000-00805f9b34fb
                    ,
                        UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
                    */
                    /*
                    val serviceUuids: Array<UUID> = arrayOf(
                        UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
                    )
                    val scanRuleConfig: BleScanRuleConfig = Builder()
                        .setServiceUuids(serviceUuids)
                        .setAutoConnect(true)
                        .setScanTimeOut(10000)
                        .build()
                    BleManager.getInstance().initScanRule(scanRuleConfig)
                    startScan()*/


                } else {
                    BleManager.getInstance().disableBluetooth()
                }

                SessionApp.getInstance(requireContext())
                    .isBTPanicButtonEnabled(/* enabled = */ isChecked)
/*
            if (isChecked) {
                if (requireContext().isServiceRunning(ITagService::class.java) == false) {
                    AppClass.instance.startBLEService()
                    Toast.makeText(requireContext(), "Service started", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Service already running", Toast.LENGTH_SHORT)
                        .show()
                    bleService = AppClass.instance.getBLEService()
                    bleService?.setServiceCallback(object : IBle {
                        override fun onScanResultUpdate(scanResult: ScanResult) {
                            var index = -1
                            var counter = 0

                            //            CoroutineScope(Dispatchers.Main).launch {

                            Log.d("BLE", scanResult.rssi.toString())
                            scanResultAdapter?.items?.forEach { device ->
                                if (device.device.address == scanResult.device.address) {
                                    index = counter
                                    return@forEach
                                }
                                counter++
                            }
                            if (index == -1) {
                                scanResultAdapter?.items?.add(scanResult)
                                scanResultAdapter?.notifyItemInserted(scanResultAdapter?.items?.size!! - 1)

                            } else {
                                scanResultAdapter?.items?.set(index, scanResult)
                                scanResultAdapter?.notifyDataSetChanged()
                                //     delay(500)
                            }
                        }

                        //     }

                        override fun onScanFailed(errorCode: Int) {
                            requireActivity().showErrorDialog("Error Scaneando = $errorCode")
                        }

                        override fun onDeviceConnected(deviceAddress: String) {
                            //     TODO("Not yet implemented")
                            requireActivity().runOnUiThread {

                                Toast.makeText(
                                    requireContext(),
                                    "Device Connected",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                scanResultAdapter?.notifyDataSetChanged()
                            }
                        }

                        override fun onDeviceDesconnected(deviceAddress: String) {


                            requireActivity().runOnUiThread {

                                Toast.makeText(
                                    requireContext(),
                                    "Device Disconnected",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                scanResultAdapter?.notifyDataSetChanged()
                            }
                            //   TODO("Not yet implemented")
                        }

                        override fun onBatteryLevelUpdate(address: String, value: Int) {
                            //  TODO("Not yet implemented")
                        }
                    })
                    startScanning()
                }


            } else {
                scanResultAdapter?.items?.clear()
                scanResultAdapter?.notifyDataSetChanged()
                AppClass.instance.stopBLEService()

            }
            requireContext().broadcastMessage(
                null,
                BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED
            )
            */
            }

        }
        /*
        binding.recyclerDevices.apply {
            var linearLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = linearLayoutManager
            adapter = scanResultAdapter
        }*/

    }

    private fun startScan() {
        /*
           BleManager.getInstance().scan(object : BleScanCallback() {
               override fun onScanStarted(success: Boolean) {
                   var pp = 22
               }

               override fun onScanning(bleDevice: BleDevice) {
                   var pp = 22
                   scanResultAdapter?
               }

               override fun onScanFinished(scanResultList: List<BleDevice>) {
                   var pp = 22
               }
           })
           */
    }

    /*
    private fun startScanning() {
        if (requireContext().hasRequiredPermissionsBLE()) {
            if (bluetoothAdapter.enable()) {
                bluetoothAdapter.isDiscovering
                scanResultAdapter?.items?.clear()
                scanResultAdapter?.items?.addAll(bleService?.getDevicesList()!!)
                bleService?.scan()
                if (bleService == null)
                //                    scanIsRequested = true
                else
                    (requireActivity() as AppCompatActivity).requirePermissionsBLE(
                        REQUEST_CODE_FOR_SERVICE_CREATION
                    )
            }


        }
    }
*/

    private fun updateUI() {
        binding.switchEnabled.isChecked =
            SessionApp.getInstance(requireContext()).isBTPanicButtonEnabled
    }


    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.push_button_config))
        }
        //  registerReceivers()
        //  ErrorsObservable.addErrorListener(mErrorListener)
        sIsShown = true
        /*
        setupContent()
        disposableBag.add(
            ITag.ble.scanner().observableScan().subscribe { result: BLEScanResult ->
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
                        Log.d(LT, "found=$found")
                    }
                    scanResults.add(result)

                    adapter?.scanResults?.add(result)
                    adapter!!.notifyDataSetChanged()
                    lastUpdate = System.currentTimeMillis()
                }
                if (modified) {
                    if (System.currentTimeMillis() - lastUpdate > 1000) {
                        if (BuildConfig.DEBUG) {
                            Log.d(LT, "modified=$modified")
                        }
                        adapter!!.notifyDataSetChanged()
                        lastUpdate = System.currentTimeMillis()
                    }
                }
            }

        )



        disposableBag.add(
            ITag.ble.observableState().subscribe { event: BLEState? -> setupContent() })
        disposableBag.add(ITag.ble.scanner().observableActive().subscribe { event: Boolean ->
            if (BuildConfig.DEBUG) {
                Log.d(
                    LT,
                    "ble.scanner activeEvent=$event isScanning=" + ITag.ble.scanner()
                        .isScanning + " thread=" + Thread.currentThread().name
                )
            }
            setupContent()

        })



        disposableBag.add(ITag.store.observable().subscribe { event: StoreOp ->
            when (event.op) {
                StoreOpType.change -> setupContent()
                StoreOpType.forget -> {}
                else -> {}
            }
        })
*/
        if (binding.switchEnabled.isChecked) {
            (requireActivity() as AppCompatActivity).requirePermissionsBLE(
                BLUETOOTH_CONNECT_REQUEST_CODE
            )
            ble.scanner().start(ITag.SCAN_TIMEOUT, arrayOf<String>())
        }

        requireContext().bindService(intentBind(this.requireActivity()), mServiceConnection, 0)

        //------------ Este Segumento escucha las TAGS
      //  setupTags(root)
       /*
        disposableBag.add(ITag.store.observable().subscribe { event: StoreOp? ->
            setupTags(
                root
            )
        })
       */
        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i) ?: continue
            val id = itag.id()
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onResume connectionById $id")
            }
            val connection = ble.connectionById(id)
            disposableBag.add(connection.observableRSSI().subscribe { rssi: Int? ->
                updateRSSI(
                    id,
                    rssi!!
                )
            })
            disposableBag.add(
                connection.observableImmediateAlert().subscribe { state: AlertVolume? ->
                    updateITagImageAnimation(
                        itag,
                        connection
                    )
                })
            disposableBag.add(connection.observableState().subscribe { state: BLEConnectionState? ->
                requireActivity().runOnUiThread {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            LT,
                            "connection " + id + " state changed " + connection.state().toString()
                        )
                    }
                    updateAlertButton(id)
                    updateState(id, state!!)
                    updateITagImageAnimation(itag, connection)
                    if (connection.state() == BLEConnectionState.connected) { //isConnected()) {
                        connection.enableRSSI()
                    } else {
                        connection.disableRSSI()
                        updateRSSI(id, -999)
                    }
                }
            })
            disposableBag.add(connection.observableClick().subscribe { event: Int? ->
                updateITagImageAnimation(
                    itag,
                    connection
                )
            })
        }
       // HistoryRecord.addListener(this)
/*
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        val wt_disabled = sp.getBoolean("wt_disabled0", false)

        if (wt_disabled) {
            root.findViewById(R.id.btn_waytoday).setVisibility(View.GONE)
        } else {
            Waytoday.tracker.addOnTrackingStateListener(this)
            TrackIDJobService.addOnTrackIDChangeListener(this)
        }
        */
        startRssi()
        /*
            if (Waytoday.tracker.isOn(requireContext()) && PowerManagement.needRequestIgnoreOptimization(this.requireContext())) {
                if (resumeCount++ > 1) {
                    Handler(getMainLooper()).post { PowerManagement.requestIgnoreOptimization(this.requireContext()) }
                }
            }

         */
    }

    private fun startRssi() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "startRssi")
        }
        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i) ?: continue
            val connection = ble.connectionById(itag.id())
            if (connection.state() == BLEConnectionState.connected) {
                connection.enableRSSI()
            } else {
                updateRSSI(itag.id(), -999)
            }
        }
    }

    private fun stopRssi() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "stopRssi")
        }
        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i)
            if (itag != null) {
                val connection = ble.connectionById(itag.id())
                connection.disableRSSI()
            }
        }
    }
    private fun updateRSSI(rootView: ViewGroup, rssi: Int) {
        val activity = activity ?: return
        //
        val rssiView = rootView.findViewById<RssiView>(R.id.rssi) ?: return
        rssiView.setRssi(rssi)
    }

    private fun updateRSSI(id: String, rssi: Int) {
        val view = tagViews[id] ?: return
        updateRSSI(view, rssi)
    }

    private fun updateState(rootView: ViewGroup, id: String, state: BLEConnectionState) {
        val activity = activity ?: return
        //
        val statusDrawableId: Int
        val statusTextId: Int
        if (ble.state() == BLEState.OK) {
            when (state) {
                BLEConnectionState.connected -> {
                    statusDrawableId = R.drawable.bt
                    statusTextId = R.string.bt
                }
                BLEConnectionState.connecting, BLEConnectionState.disconnecting -> {
                    val itag = ITag.store.byId(id)
                    if (itag != null && itag.isAlertDisconnected) {
                        statusDrawableId = R.drawable.bt_connecting
                        statusTextId = R.string.bt_lost
                    } else {
                        statusDrawableId = R.drawable.bt_setup
                        if (state == BLEConnectionState.connecting) statusTextId =
                            R.string.bt_connecting else statusTextId = R.string.bt_disconnecting
                    }
                }
                BLEConnectionState.writting, BLEConnectionState.reading -> {
                    statusDrawableId = R.drawable.bt_call
                    statusTextId = R.string.bt_call
                }
                BLEConnectionState.disconnected -> {
                    statusDrawableId = R.drawable.bt_disabled
                    statusTextId = R.string.bt_disabled
                }
                else -> {
                    statusDrawableId = R.drawable.bt_disabled
                    statusTextId = R.string.bt_disabled
                }
            }
        } else {
            statusDrawableId = R.drawable.bt_disabled
            statusTextId = R.string.bt_disabled
        }
        val imgStatus: ImageView = rootView.findViewById(R.id.bt_status)
        imgStatus.setImageResource(statusDrawableId)
        val textStatus = rootView.findViewById<TextView>(R.id.text_status)
        textStatus.setText(statusTextId)
    }

    private fun updateState(id: String, state: BLEConnectionState) {
        val view = tagViews[id] ?: return
        updateState(view, id, state)
    }

    private fun updateName(rootView: ViewGroup, name: String) {
        val activity = activity ?: return
        //
        val textName = rootView.findViewById<TextView>(R.id.text_name)
        textName.text = name
    }

    private fun updateAlertButton(
        rootView: ViewGroup,
        isAlertDisconnected: Boolean,
        isConnected: Boolean
    ) {
        val activity = activity ?: return
        //
        val btnAlert: ImageView = rootView.findViewById(R.id.btn_alert)
        if (BuildConfig.DEBUG) {
            Log.d(
                LT,
                "updateAlertButton2 isAlertDisconnected=$isAlertDisconnected isConnected=$isConnected"
            )
        }
        btnAlert.setImageResource(if (isAlertDisconnected || isConnected) R.drawable.linked else R.drawable.keyfinder)
    }

    private fun updateAlertButton(id: String) {
        val activity = activity ?: return
        //
        val view = tagViews[id]
        if (view == null) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "updateAlertButton1 id=$id view null")
            }
            return
        }
        val itag = ITag.store.byId(id)
        if (itag == null) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "updateAlertButton1 id=$id itag null")
            }
            return
        }
        if (BuildConfig.DEBUG) {
            Log.d(LT, "updateAlertButton connectionById $id")
        }
        val connection = ble.connectionById(id)
        val isConnected = connection.isConnected
        val isAlertDisconnected = itag.isAlertDisconnected
        if (BuildConfig.DEBUG) {
            Log.d(
                LT,
                "id = $id updateAlertButton2 isAlertDisconnected=$isAlertDisconnected isConnected=$isConnected"
            )
        }
        updateAlertButton(view, isAlertDisconnected, isConnected)
    }

    private fun updateITagImageAnimation(
        rootView: ViewGroup,
        itag: ITagInterface,
        connection: BLEConnectionInterface
    ) {
        val activity = activity ?: return
        //
        if (mITagAnimation == null) {
            return
        }
        var animShake: Animation? = null
        if (BuildConfig.DEBUG) {
            Log.d(
                LT, "updateITagImageAnimation isFindMe:" + connection.isFindMe +
                        " isAlerting:" + connection.isAlerting +
                        " isAlertDisconnected:" + itag.isAlertDisconnected +
                        " not connected:" + !connection.isConnected
            )
        }
        if (connection.isAlerting ||
            connection.isFindMe || itag.isAlertDisconnected && !connection.isConnected
        ) {
            animShake =
                mITagAnimation //AnimationUtils.loadAnimation(getActivity(), R.anim.shake_itag);
        }
        val imageITag: ImageView = rootView.findViewById(R.id.image_itag)
        if (animShake == null) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "updateITagImageAnimation: No animations appointed")
            }
            animShake = imageITag.animation
            if (animShake != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(LT, "updateITagImageAnimation: Stop previous animation")
                }
                animShake.cancel()
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "updateITagImageAnimation: Start animation")
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                imageITag.startAnimation(animShake)
            } else {
                val anim: Animation = animShake
                requireActivity().runOnUiThread { imageITag.startAnimation(anim) }
            }
        }
    }

    private fun updateITagImageAnimation(itag: ITagInterface, connection: BLEConnectionInterface) {
        activity ?: return
        //
        val view = tagViews[itag.id()] ?: return
        updateITagImageAnimation(view, itag, connection)
    }

    private fun updateITagImage(rootView: ViewGroup, itag: ITagInterface) {
        val activity = activity ?: return
        //
        val imageId: Int
        imageId = when (itag.color()) {
            TagColor.black -> R.drawable.itag_black
            TagColor.red -> R.drawable.itag_red
            TagColor.green -> R.drawable.itag_green
            TagColor.gold -> R.drawable.itag_gold
            TagColor.blue -> R.drawable.itag_blue
            else -> R.drawable.itag_white
        }
        val imageITag: ImageView = rootView.findViewById(R.id.image_itag)
        imageITag.setImageResource(imageId)
        imageITag.tag = itag
    }

    private fun setupContent() {
        val fragmentManager: FragmentManager = this.childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        var fragment: Fragment? = null
        if (BuildConfig.DEBUG) {
            Log.d(
                LT,
                "setupContent isScanning=" + ble.scanner().isScanning + " thread=" + Thread.currentThread().name
            )
        }
        if (ble.scanner().isScanning) {

            mEnableAttempts = 0
            if (mSelectedFragment !== FragmentType_old.SCANNER) {
                fragment = ScanFragment()
                mSelectedFragment = FragmentType_old.SCANNER
            }
        } else {

            if (ble.state() == BLEState.NO_ADAPTER) {
                fragment = NoBLEFragment()
                mSelectedFragment = FragmentType_old.OTHER
            } else {
                if (ble.state() == BLEState.OK) {
                    setNotFirstLaunch()
                    mEnableAttempts = 0
                    if (mSelectedFragment !== FragmentType_old.ITAGS) {
                        fragment = ITagsFragment()
                        mSelectedFragment = FragmentType_old.ITAGS
                    }
                } else {
                    if (mEnableAttempts < 60 && isFirstLaunch()) {
                        mEnableAttempts++
                        if (BuildConfig.DEBUG) {
                            Log.d(
                                LT,
                                "setupContent BT disabled, enable attempt=$mEnableAttempts"
                            )
                        }
                        if (mEnableAttempts == 1) {
                            Toast.makeText(requireContext(), R.string.try_enable_bt, Toast.LENGTH_LONG).show()
                        }
                        ble.enable()
                        try {
                            // A bit against rules but ok in this situation
                            Thread.sleep(500)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        setupContent()
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(LT, "setupContent BT disabled, auto enable failed")
                        }
                        fragment = DisabledBLEFragment()
                        mSelectedFragment = FragmentType_old.OTHER
                    }
                }
            }
        }
        if (fragment != null) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentTransaction.replace(R.id.content, fragment)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

    private fun setNotFirstLaunch() {
        //TODO("Not yet implemented")


        var sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        var ed: SharedPreferences.Editor = sharedPref.edit()
        ed.putBoolean("first", false)
        ed.apply()
    }

    private fun isFirstLaunch(): Boolean {
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getBoolean("first", true)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceivers()
        //   bleService?.pressMode = currentPressMode
        try {
            requireActivity().unbindService(mServiceConnection)
        } catch (e: IllegalArgumentException) {
            // ignore
        }
        disposableBag.dispose()
        sIsShown = false
        if (ITag.store.isDisconnectAlert) {
            ITagsService.start(this.requireContext())
        } else {
            ITagsService.stop(this.requireContext())
        }
        // ErrorsObservable.removeErrorListener(mErrorListener)
        //  Waytoday.gpsLocationUpdater.removePermissionListener(gpsPermissionListener)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothLeScanner?.stopScan(scanCallback)
    }




    fun startService() {
        Toast.makeText(context, "Start Service", Toast.LENGTH_SHORT).show()
        /*
            var intentService = Intent(requireContext(), ITagService::class.java)
            requireContext().bindService(intentService, bleServiceConn, Context.BIND_AUTO_CREATE)
            requireContext().startService(intentService)

        */
    }


    private fun registerReceivers() {
        val intentFilter = IntentFilter()
/*
        intentFilter.addAction(BROADCAST_MESSAGE_SCAN_RESULT_UPDATED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_LIST)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_SCAN_DISCOVERING)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED)
        intentFilter.addAction(BROADCAST_MESSAGE_PANIC_BUTTON_TEST)
  */
        /*
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver,
            intentFilter
        )*/
    }

    private fun unregisterReceivers() {
        //       LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
/*
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_PERMISSIONS -> {
                startService()

            }
        }
*/
    }

/*
    @SuppressLint("MissingPermission")
    override fun onScanResultUpdate(scanResult: ScanResult) {


        if (requireContext().hasRequiredPermissionsBLE()) {
            val indexQuery =
                scanResults.indexOfFirst { it.device.address == scanResult.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = scanResult!!
                scanResultAdapter?.notifyItemChanged(indexQuery)
            } else {
                with(scanResult!!) {
                    if (requireContext().hasRequiredPermissionsBLE()) {
                        Log.i(
                            "ScanCallback",
                            "Found BLE device! Name: ${device.name ?: "Unnamed"}, address: $device.address"
                        )
                        // val txpw: Byte = scanRecord!!.get(29)
                    }
                }
                scanResults.add(scanResult)
                scanResultAdapter?.notifyItemInserted(scanResults.size - 1)
            }
        }
    }


    override fun onScanFailed(errorCode: Int) {
        binding?.swipeRefreshDevices?.isRefreshing = false
    }

    override fun onRSSIUpdate(deviceAddress: String, rssi: Int) {
        var resultIndex = -1
        scanResults.forEach { result ->
            resultIndex++
            if (result.device.address == deviceAddress) {
                return@forEach
            }

        }
        if (resultIndex != -1) {
            var scanResult = scanResults.get(resultIndex)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var distance = calculateDistance(scanResult.txPower.toDouble(), rssi.toDouble())
                Log.d("BLE", "Distance = ${distance}")
            }
        }
    }

    override fun onDeviceConnected(deviceAddress: String) {

        requireActivity().runOnUiThread {
            Toast.makeText(
                requireActivity(),
                "Device Connected ${deviceAddress}",
                Toast.LENGTH_SHORT
            )
                .show()

        }

    }

    override fun onDeviceDesconnected(deviceAddress: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireActivity(),
                "Device Disconnected ${deviceAddress}",
                Toast.LENGTH_SHORT
            )
                .show()

        }
    }

    override fun onBatteryLevelUpdate(address: String, value: Int) {

        requireActivity().runOnUiThread {
            Toast.makeText(
                requireActivity(),
                "Battery Level = ${value}",
                Toast.LENGTH_SHORT
            )
                .show()

        }
    }
*/

/*
    private fun setupTags(root: ViewGroup) {
        val activity = activity ?: return
        //
        var tagsLayout = root.findViewById<View>(R.id.tags)
        var index = -1
        if (tagsLayout != null) {
            root.removeView(tagsLayout)
            index = root.indexOfChild(tagsLayout)
        }
        val s = ITag.store.count()
        val rid =
            if (s == 0) R.layout.itag_0 else if (s == 1) R.layout.itag_1 else if (s == 2) R.layout.itag_2 else if (s == 3) R.layout.itag_3 else R.layout.itag_4
        tagsLayout = activity.layoutInflater.inflate(rid, root, false)
        root.addView(tagsLayout, index)
        tagViews.clear()
        if (s > 0) {
            val itag = ITag.store.byPos(0)
            if (itag != null) {
                tagViews.put(
                    itag.id(),
                    root.findViewById<View>(R.id.tag_1).findViewById(R.id.layout_itag)
                )
            }
        }
        if (s > 1) {
            val itag = ITag.store.byPos(1)
            if (itag != null) {
                tagViews.put(
                    itag.id(),
                    root.findViewById<View>(R.id.tag_2).findViewById(R.id.layout_itag)
                )
            }
        }
        if (s > 2) {
            val itag = ITag.store.byPos(2)
            if (itag != null) {
                tagViews.put(
                    itag.id(),
                    root.findViewById<View>(R.id.tag_3).findViewById(R.id.layout_itag)
                )
            }
        }
        if (s > 3) {
            val itag = ITag.store.byPos(3)
            if (itag != null) {
                tagViews.put(
                    itag.id(),
                    root.findViewById<View>(R.id.tag_4).findViewById(R.id.layout_itag)
                )
            }
        }
        for ((id, rootView): Map.Entry<String, ViewGroup> in tagViews.entrySet()) {
            val itag = ITag.store.byId(id)
            val connection = ble.connectionById(id)
            if (itag != null) {
                setupButtons(rootView, itag)
                updateITagImage(rootView, itag)
                updateITagImageAnimation(rootView, itag, connection)
                updateName(rootView, itag.name())
                updateAlertButton(rootView, itag.isAlertDisconnected, connection.isConnected)
            }
            updateRSSI(rootView, connection.rssi())
            updateState(rootView, id, connection.state())
            updateLocationImage(rootView, id)
        }
        updateWayToday()
    }
*/
}