package com.iyr.ian.ui.settings.push_button

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.clj.fastble.BleManager
import com.iyr.ian.BuildConfig
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.databinding.FragmentBluetoothConfigurationBinding
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagModesEnum
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.itag.StoreOpType
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.SettingsFragmentViewModel
import com.iyr.ian.ui.settings.push_button.adapters.TagsAdapter
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.bluetooth.adapters.BLEDeviceAdapter
import com.iyr.ian.utils.bluetooth.adapters.IBLEDeviceAdapter
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.BLEState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.errors.ErrorsObservable
import com.iyr.ian.utils.bluetooth.models.BLEScanResult
import com.iyr.ian.utils.multimedia.MediaPlayerUtils
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID


private enum class FragmentType {
    OTHER, ITAGS, SCANNER
}

@SuppressLint("MissingPermission")
class PushButtonSetupFragment() : Fragment(), IBLEDeviceAdapter {
    private var mEnableAttempts = 0
    private val disposableBag = DisposableBag()
    private var scanningDisposableBag : DisposableBag? = null

    private var scanning = false
    private var mSelectedFragment: FragmentType? = null
    private var viewModel: PushButtonsSetupFragmentViewModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pressedTimes = 0

    private val mainActivityViewModel: MainActivityViewModel by lazy {
        MainActivityViewModel.getInstance(
            requireContext(),
            UserViewModel.getInstance().getUser()?.user_key.toString()
        )
    }
    private val settingsFragmentViewModel: SettingsFragmentViewModel by lazy { SettingsFragmentViewModel.getInstance() }




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

    private lateinit var binding: FragmentBluetoothConfigurationBinding
    private var adapter: BLEDeviceAdapter? = null

    private val searchAdapter: BLEDeviceAdapter by lazy { BLEDeviceAdapter(requireContext(), this) }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    val tagsAdapter: TagsAdapter by lazy { TagsAdapter(requireActivity(), ITag.store) }


    private val bluetoothManager by lazy { requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    //  private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }


    internal class ITagServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val itagService = service as ITagsService.ITagBinder
            itagService.removeFromForeground()


        }
        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val mServiceConnection = ITagServiceConnection()

    private val mErrorListener: ErrorsObservable.IErrorListener =
        ErrorsObservable.IErrorListener { errorNotification ->
            requireActivity().runOnUiThread(java.lang.Runnable {
                Toast.makeText(
                    requireContext(), errorNotification.message, Toast.LENGTH_LONG
                ).show()
            })
            Log.e(LT, errorNotification.message, errorNotification.th)
        }

    private var resumeCount = 0

    override fun OnItemClicked() {
        ITag.store.remember(ITag.store.byPos(0))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState!!)
        if (tagsAdapter != null) {
            tagsAdapter.saveStates(outState)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (tagsAdapter != null) {
            tagsAdapter.restoreStates(savedInstanceState)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothConfigurationBinding.inflate(layoutInflater, container, false)
        binding.recyclerDevices.adapter = adapter
        setupUI()

        binding.switchEnabled.setOnCheckedChangeListener { compoundButton, isChecked ->
            requireActivity().handleTouch()
            if (isChecked) {
                binding.scanButton.visibility = View.VISIBLE
            } else {
                binding.scanButton.visibility = View.INVISIBLE
            }
            mainActivityViewModel.setBluetoothState(isChecked)
        }


        binding.scanButton.setOnClickListener {
            requireContext().handleTouch()
            if (binding.switchEnabled.isChecked) {
                onStartStopScan()
            }
        }

        binding.storedTags.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
        binding.storedTags.adapter = tagsAdapter


        binding.detectedTags.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.detectedTags.adapter = searchAdapter

        return binding.root
    }


    private fun isFirstLaunch(): Boolean {
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getBoolean("first", true)
    }

    private fun setNotFirstLaunch() {
        val sharedPref: SharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val ed = sharedPref.edit()
        ed.putBoolean("first", false)
        ed.apply()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PushButtonsSetupFragmentViewModel()
    }


    @SuppressLint("MissingPermission")
    private fun setupUI() {
        updateUI()
    }


    private fun updateUI() {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "No Adapter", Toast.LENGTH_SHORT).show()
            binding.scanButton.visibility = View.INVISIBLE
            binding.switchEnabled.isEnabled = false
            binding.switchEnabled.isChecked = false
            // Device does not support Bluetooth
        } else {
            binding.switchEnabled.isChecked =
                SessionApp.getInstance(requireContext()).isBTPanicButtonEnabled
            if (binding.switchEnabled.isChecked) {
                binding.scanButton.visibility = View.VISIBLE
            } else {
                binding.scanButton.visibility = View.INVISIBLE
            }

        }


    }


    override fun onResume() {
        startObservers()
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.push_button_config))
        }
        AppClass.instance.itagPressMode = ITagModesEnum.test

        //----------------------------------------------------------------
        ErrorsObservable.addErrorListener(mErrorListener)
        sIsShown = true


        /*
          setupContent()
          // TODO:  Waytoday.gpsLocationUpdater.addOnPermissionListener(gpsPermissionListener)
          disposableBag.add(ITag.ble.observableState().subscribe { event -> setupContent() })
      */
        /*
        disposableBag.add(ITag.ble.scanner().observableActive().subscribe { event ->
            if (BuildConfig.DEBUG) {
                Log.d(
                    LT,
                    "ble.scanner activeEvent=$event isScanning=" + ITag.ble.scanner()
                        .isScanning + " thread=" + Thread.currentThread()
                        .name
                )
            }
            setupContent()
            setupProgressBar()
        })
        *//*
        disposableBag.add(
            ITag.ble.scanner().observableTimer().subscribe { event -> setupProgressBar() })
       */


        requireContext().bindService(
            ITagsService.intentBind(requireContext()), mServiceConnection, 0
        )


        /*
                TODO: Resolverlo
                if (Waytoday.tracker.isOn(this) && PowerManagement.needRequestIgnoreOptimization(this)) {
                    if (resumeCount++ > 1) {
                        Handler(getMainLooper()).post { PowerManagement.requestIgnoreOptimization(this) }
                    }
                }

         */


/*
        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i)
            if (itag != null) {
                val connection = ITag.ble.connectionById(itag.id())

                disposableBag.add(
                    connection.observableState().subscribe { state: BLEConnectionState? ->

                        when (state)
                        {
                            BLEConnectionState.disconnected -> {

                            }
                            BLEConnectionState.connecting -> {

                            }
                            BLEConnectionState.connected -> { }
                            BLEConnectionState.disconnecting -> {}
                            BLEConnectionState.writting -> {}

                            BLEConnectionState.reading -> {}
                            null -> {}
                        }

                        requireActivity().runOnUiThread(Runnable {
                            if (BuildConfig.DEBUG) {
                                Log.d(
                                    "Tags",
                                    "connection " + itag.id() + " state changed " + connection.state()
                                        .toString()
                                )
                            }
                            //      updateState(itag.id(), state!!)


                            tagsAdapter.notifyDataSetChanged()
                        })
                    })

/*
                disposableBag.add(ITag.store.observable().subscribe { event ->
                    when (event.op) {
                        StoreOpType.change -> {
                            Toast.makeText(requireContext(), "sorete", Toast.LENGTH_LONG).show()
                        }//setupContent()
                        StoreOpType.forget -> {

                            binding.storedTags.adapter?.notifyDataSetChanged()


                            /*
                                                var tag: ITagDefault = event.tag as ITagDefault
                                                var index = -1

                                                adapter?.scanResults?.forEach { existingTag ->
                                                    index++
                                                    if (existingTag.id == tag.id()) {
                                                        binding.currentTags.adapter?.notifyItemRemoved(index)
                                                        return@subscribe
                                                    }
                                                }

                             */
                        }

                        StoreOpType.remember -> {/*
                    var tag: ITagDefault = event.tag as ITagDefault
                    var exists = false
                    adapter?.scanResults?.forEach { existingTag ->
                        if (existingTag.id == tag.id()) {
                            exists = true
                            return@subscribe
                        }
                    }
                    if (!exists) {
                     //   adapter?.scanResults?.add(BLEScanResult())
                        binding.currentTags.adapter?.notifyItemInserted(adapter?.scanResults?.count() ?: 0 - 1)
                    }

                     */
                            binding.storedTags.adapter?.notifyDataSetChanged()
                        }
                    }
                })
*/




                disposableBag.add(connection.observableClick().subscribe { event: Int? ->
                    pressedTimes++
                    var tagPosition = -1
                    val adapterData = (binding.storedTags.adapter as TagsAdapter).dataSet

                    for (k in 0 until adapterData.count()) {
                        tagPosition++
                        if (adapterData.byPos(tagPosition).id() == itag.id()) {
                            binding.storedTags.scrollToPosition(tagPosition)

                            val connection = ITag.ble.connectionById(itag.id())

                            if (!connection.isAlerting) {
                                MediaPlayerUtils.getInstance(requireContext()).startFindPhone()

                                connection.isFindingActive(true)
                                tagsAdapter.notifyDataSetChanged()
                            } else {
                                MediaPlayerUtils.getInstance(requireContext()).stopSound()
                                connection.isFindingActive(false)
                                // connection.writeImmediateAlert(AlertVolume.NO_ALERT)
                                //        tagsAdapter.notifyItemChanged(tagsAdapter.currentPosition)
                            }

                            break
                        }
                    }
                    Toast.makeText(
                        activity,
                        "El Boton de Pánico está funcional! ${pressedTimes}",
                        Toast.LENGTH_SHORT
                    ).show()


                    //      }
                })

                disposableBag.add(
                    connection.observableImmediateAlert().subscribe { handler ->
                        var p = handler
                    })

            }

        }

*/
    }


    override fun onPause() {
        removeObservers()
        super.onPause()

        try {
            requireContext().unbindService(mServiceConnection)
        } catch (e: IllegalArgumentException) {
            // ignore
        }

        if (SessionApp.getInstance(requireContext()).isBTPanicButtonEnabled) {
            AppClass.instance.itagPressMode = ITagModesEnum.active
        }

        disposableBag.dispose()
        sIsShown = false
// TODO: Resolverlo        if (ITag.store.isDisconnectAlert || Waytoday.tracker.isUpdating) {
        if (ITag.store.isDisconnectAlert) {
            ITagsService.start(requireContext())
        } else {
            ITagsService.stop(requireContext())
        }
        ErrorsObservable.removeErrorListener(mErrorListener)
        // TODO: Resolverlo     Waytoday.gpsLocationUpdater.removePermissionListener(gpsPermissionListener)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableBag.dispose()
    }


    private val scanResults: ArrayList<BLEScanResult> = ArrayList<BLEScanResult>()
    private var lastUpdate: Long = 0

    fun startObservers() {

        if (bluetoothAdapter == null) viewModel?.setBluetoothStatus(BLEState.NO_ADAPTER)
        else if (bluetoothAdapter.isEnabled) viewModel?.setBluetoothStatus(BLEState.OK)
        else viewModel?.setBluetoothStatus(BLEState.NOT_ENABLED)


        mainActivityViewModel.bluetoothStatus.observe(this) { isOn ->
            binding.switchEnabled.isChecked = isOn
            if (isOn) {
                if (BleManager.getInstance().isSupportBle == false) {
                    requireActivity().showErrorDialog("Tu dispositivo no permite conectarse a dispositivos Bluetooth LE")
                }
                tagsAdapter.whenBluetoothDevicesIsActive()

            } else {
                tagsAdapter.whenBluetoothDevicesDeactivated()
            }
        }


        viewModel?.bluetoothStatus?.observe(this) { status ->
            when (status) {
                BLEState.OK -> {
                    binding.storedTags.visibility = View.VISIBLE
                    binding.btStatusLayout.visibility = View.GONE
                    binding.switchEnabled.isEnabled = true
                }

                BLEState.NO_ADAPTER -> {
                    mainActivityViewModel.showError("Tu dispositivo no permite conectarse a dispositivos Bluetooth LE")
                    binding.storedTags.visibility = View.GONE
                    binding.btStatusDescrip.text = "Bluetooth no está disponible"
                    binding.btStatusLayout.visibility = View.VISIBLE
                    tagsAdapter.onBluetoothOff()
                }

                BLEState.NOT_ENABLED -> {
                    binding.switchEnabled.isEnabled = false
                    binding.switchEnabled.isChecked = false
                    binding.storedTags.visibility = View.GONE
                    binding.btStatusDescrip.text = "Bluetooth está desactivado"
                    binding.btStatusLayout.visibility = View.VISIBLE
                }
            }

        }



/*
        disposableBag.add(ITag.ble.scanner().observableScan().subscribe { result: BLEScanResult ->
            if (ITag.store.remembered(result.id)) {
                return@subscribe
            }
            if (searchAdapter == null) {
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
                   // busca en searchAdapter.scanResults por id = result.id y toma su indice
                    val index = searchAdapter.scanResults.indexOfFirst { it.id == result.id }
                    if (index != -1) {
                        searchAdapter.scanResults[index] = result
                        searchAdapter.notifyItemChanged(index)
                    } else {
                        searchAdapter.scanResults.add(result)
                        searchAdapter.notifyItemInserted(scanResults.size - 1)
                    }
                }
                //            searchAdapter?.notifyDataSetChanged()
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
            binding.progress.progress = tick!!
            if (tick==0) {
                ITag.ble.scanner().stop()
                updateProgressBar()
            }
        })

        disposableBag.add(ITag.ble.scanner().observableActive().subscribe { active: Boolean? ->
            if (!active!!) {
                return@subscribe
            }
            if (searchAdapter == null) {
                return@subscribe
            }
            scanResults.clear()
            adapter?.notifyDataSetChanged()
        })
*/

        // Controla la conexion y desconexion de los dispositivos
        disposableBag.add(ITag.store.observable().subscribe { event ->
            when (event.op) {
                StoreOpType.change -> {
                    //Toast.makeText(requireContext(), "sorete", Toast.LENGTH_LONG).show()
                }
                StoreOpType.forget -> {
                    binding.storedTags.adapter?.notifyDataSetChanged()
                }
                StoreOpType.remember -> {
                    connectToTag(event.tag.id())
                    binding.storedTags.adapter?.notifyDataSetChanged()
                    val index = searchAdapter.scanResults.indexOfFirst { it.id == event.tag.id() }
                    if (index != -1) {
                        searchAdapter.scanResults.removeAt(index)
                        searchAdapter.notifyItemRemoved(index)
                    }
                }
            }
        })

    }


    /**
     * Conecta el Itag y actualiza su estado.
     */
    private fun connectToTag(tagId: String) {
        val connection = ITag.ble.connectionById(tagId)

        disposableBag.add(
            connection.observableState().subscribe { state: BLEConnectionState? ->

                when (state)
                {
                    BLEConnectionState.disconnected -> {

                    }
                    BLEConnectionState.connecting -> {

                    }
                    BLEConnectionState.connected -> {
                        var pp = 3
                    }
                    BLEConnectionState.disconnecting -> {}
                    BLEConnectionState.writting -> {}

                    BLEConnectionState.reading -> {}
                    null -> {}
                }

                requireActivity().runOnUiThread(Runnable {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "Tags",
                            "connection " + tagId + " state changed " + connection.state()
                                .toString()
                        )
                    }
                    //      updateState(itag.id(), state!!)

tagsAdapter
                    tagsAdapter.notifyDataSetChanged()
                })
            })


        disposableBag.add(connection.observableClick().subscribe { event: Int? ->
            pressedTimes++
            var tagPosition = -1
            val adapterData = (binding.storedTags.adapter as TagsAdapter).dataSet

            for (k in 0 until adapterData.count()) {
                tagPosition++
                if (adapterData.byPos(tagPosition).id() == tagId) {
                    binding.storedTags.scrollToPosition(tagPosition)

                    val connection = ITag.ble.connectionById(tagId)

                    if (!connection.isAlerting) {
                        MediaPlayerUtils.getInstance(requireContext()).startFindPhone()

                        connection.isFindingActive(true)
                        tagsAdapter.notifyDataSetChanged()
                    } else {
                        MediaPlayerUtils.getInstance(requireContext()).stopSound()
                        connection.isFindingActive(false)
                        // connection.writeImmediateAlert(AlertVolume.NO_ALERT)
                        //        tagsAdapter.notifyItemChanged(tagsAdapter.currentPosition)
                    }

                    break
                }
            }
            Toast.makeText(
                activity,
                "El Boton de Pánico está funcional! ${pressedTimes}",
                Toast.LENGTH_SHORT
            ).show()


            //      }
        })
    }


    fun removeObservers() {
        mainActivityViewModel.bluetoothStatus.removeObservers(this)
        viewModel?.bluetoothStatus?.removeObservers(this)
    }


    private fun onStartStopScan() {
        requireContext().handleTouch()
        if (BuildConfig.DEBUG) {
            Log.d(
                "ITag",
                "onStartStopScan isScanning=" + ITag.ble.scanner().isScanning + " thread=" + Thread.currentThread().name
            )
        }
        if (ITag.ble.scanner().isScanning) {
            // Detengo los observadores
            unRegisterScanningObservers()
            binding.scanButton.text = getString(R.string.start_scan)
            lifecycleScope.launch(Dispatchers.IO) {
                ITag.ble.scanner().stop()
                updateProgressBar()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        val builder = android.app.AlertDialog.Builder(context)
                        builder.setMessage(R.string.request_location_permission)
                            .setTitle(R.string.request_permission_title).setPositiveButton(
                                android.R.string.ok
                            ) { dialog: DialogInterface?, which: Int ->
                                ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    Constants.LOCATION_PERMISSION_REQUEST_CODE
                                )
                            }.setNegativeButton(
                                android.R.string.cancel
                            ) { dialog: DialogInterface, which: Int -> dialog.cancel() }.show()
                        return
                    } else {
                        // isScanRequestAbortedBecauseOfPermission=true;
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            Constants.LOCATION_PERMISSION_REQUEST_CODE
                        )
                        return
                    }
                }
            }
            binding.scanButton.text = getString(R.string.stop_scan)
            searchAdapter.scanResults.clear()
            searchAdapter.notifyDataSetChanged()

            registerScanningObservers()

            lifecycleScope.launch(Dispatchers.IO) {
                ITag.ble.scanner().start(ITag.SCAN_TIMEOUT, arrayOf())
                updateProgressBar()
            }
        }

    }

    private fun updateProgressBar() {
        val pb: ProgressBar = binding.progress
        if (ITag.ble.scanner().isScanning) {
            lifecycleScope.launch(Dispatchers.Main) {
                pb.visibility = View.VISIBLE
                pb.isIndeterminate = false
                pb.max = ITag.SCAN_TIMEOUT
                pb.progress = ITag.ble.scanner().observableTimer().value()
            }
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                pb.visibility = View.GONE
            }
        }
    }

    fun startService() {
        Toast.makeText(context, "Start Service", Toast.LENGTH_SHORT).show()/*
            var intentService = Intent(requireContext(), ITagService::class.java)
            requireContext().bindService(intentService, bleServiceConn, Context.BIND_AUTO_CREATE)
            requireContext().startService(intentService)

        */
    }


    fun registerScanningObservers()
    {
        if (scanningDisposableBag != null) {
            return
        }
        scanningDisposableBag = DisposableBag()

        scanningDisposableBag?.add(ITag.ble.scanner().observableScan().subscribe { result: BLEScanResult ->
            if (ITag.store.remembered(result.id)) {
                return@subscribe
            }
            if (searchAdapter == null) {
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
                    // busca en searchAdapter.scanResults por id = result.id y toma su indice
                    val index = searchAdapter.scanResults.indexOfFirst { it.id == result.id }
                    if (index != -1) {
                        searchAdapter.scanResults[index] = result
                        searchAdapter.notifyItemChanged(index)
                    } else {
                        searchAdapter.scanResults.add(result)
                        searchAdapter.notifyItemInserted(scanResults.size - 1)
                    }
                }
                //            searchAdapter?.notifyDataSetChanged()
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

        scanningDisposableBag?.add(ITag.ble.scanner().observableTimer().subscribe { tick: Int? ->
            binding.progress.progress = tick!!
            if (tick==0) {
                ITag.ble.scanner().stop()
                updateProgressBar()
            }
        })

        scanningDisposableBag?.add(ITag.ble.scanner().observableActive().subscribe { active: Boolean? ->
            if (!active!!) {
                return@subscribe
            }
            if (searchAdapter == null) {
                return@subscribe
            }
            scanResults.clear()
            adapter?.notifyDataSetChanged()
        })
    }

    fun unRegisterScanningObservers()
    {
        scanningDisposableBag?.dispose()
    }

    private fun unregisterReceivers() {
        //       LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)/*
                when (requestCode) {
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_PERMISSIONS -> {
                        startService()

                    }
                }
        */
    }

    /*
        private fun setupContent() {

            val fragmentManager: FragmentManager = childFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            var fragment: Fragment? = null
            if (BuildConfig.DEBUG) {
                Log.d(
                    LT,
                    "setupContent isScanning=" + ITag.ble.scanner().isScanning + " thread=" + Thread.currentThread().name
                )
            }
            if (ITag.ble.scanner().isScanning) {
                setupProgressBar()
                mEnableAttempts = 0
                if (mSelectedFragment !== FragmentType.SCANNER) {
                    fragment = ScanFragment()
                    mSelectedFragment = FragmentType.SCANNER
                }
            } else {
                setupProgressBar()
                if (ITag.ble.state() == BLEState.NO_ADAPTER) {
                    fragment = NoBLEFragment()
                    mSelectedFragment = FragmentType.OTHER
                } else {
                    if (ITag.ble.state() == BLEState.OK) {
                        setNotFirstLaunch()
                        mEnableAttempts = 0
                        if (mSelectedFragment !== FragmentType.ITAGS) {
                            fragment = ITagsFragment()
                            mSelectedFragment = FragmentType.ITAGS
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
                                Toast.makeText(
                                    requireContext(),
                                    R.string.try_enable_bt,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            ITag.ble.enable()
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
                            mSelectedFragment = FragmentType.OTHER
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
    */
    fun onStartStopScan(ignored: View?) {
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
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage(R.string.request_location_permission)
                            .setTitle(R.string.request_permission_title).setPositiveButton(
                                android.R.string.ok
                            ) { dialog: DialogInterface?, which: Int ->
                                requestPermissions(
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
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            Constants.LOCATION_PERMISSION_REQUEST_CODE
                        )
                        return
                    }
                }
            }
            ITag.ble.scanner().start(ITag.SCAN_TIMEOUT, arrayOf())
        }
    }


}