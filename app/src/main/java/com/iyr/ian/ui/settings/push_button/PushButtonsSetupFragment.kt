package com.iyr.ian.ui.settings.push_button

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.clj.fastble.BleManager
import com.clj.fastble.scan.*
import com.clj.fastble.scan.BleScanRuleConfig.*
import com.iyr.ian.BuildConfig
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.databinding.FragmentBluetoothConfigurationBinding
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagModesEnum
import com.iyr.ian.itag.StoreOpType
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.push_button.adapters.TagsAdapter
import com.iyr.ian.ui.settings.push_button.dialogs.BTScannerDialog
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.bluetooth.adapters.BLEDeviceAdapter
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.BLEState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.errors.ErrorsObservable
import com.iyr.ian.utils.multimedia.MediaPlayerUtils
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import kotlinx.coroutines.*
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private enum class FragmentType {
    OTHER, ITAGS, SCANNER
}

@SuppressLint("MissingPermission")
class PushButtonSetupFragment(
    val mainActivityViewModel: MainActivityViewModel, private val _interface: ISettingsFragment
) : Fragment() {
    private var mEnableAttempts = 0
    private val disposableBag = DisposableBag()
    private var scanning = false
    private var mSelectedFragment: FragmentType? = null
    private var viewModel: PushButtonsSetupFragmentViewModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pressedTimes = 0
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val tagsAdapter: TagsAdapter by lazy { TagsAdapter(requireActivity(), ITag.store) }


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
    private val bluetoothManager by lazy { requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    //  private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }


    companion object {
        @JvmStatic
        fun newInstance(
            context: Context,
            mainActivityViewModel: MainActivityViewModel,
            _interface: ISettingsFragment
        ) = PushButtonSetupFragment(mainActivityViewModel, _interface)
    }


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


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothConfigurationBinding.inflate(layoutInflater, container, false)
        binding.recyclerDevices.adapter = adapter
        setupUI()
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


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = PushButtonsSetupFragmentViewModel()
    }


    @SuppressLint("MissingPermission")
    private fun setupUI() {


        binding.switchEnabled.setOnCheckedChangeListener { compoundButton, isChecked ->
            requireActivity().handleTouch()
            /*
                        if (bluetoothAdapter == null) {
                            Toast.makeText(context, "No Adapter", Toast.LENGTH_SHORT).show()
                            binding.scanButton.visibility = View.INVISIBLE
                            // Device does not support Bluetooth
                            viewModel?.setBluetoothStatus(BLEState.NO_ADAPTER)
                        } else if (!bluetoothAdapter.isEnabled()) {
                            // Bluetooth is not enabled :)
                            viewModel?.setBluetoothStatus(BLEState.NOT_ENABLED)
                        } else {
                            // Bluetooth is enabled
                            viewModel?.setBluetoothStatus(BLEState.OK)
                        }
            */
            if (isChecked) {
                binding.scanButton.visibility = View.VISIBLE
                binding.scanButton.setOnClickListener {
                    val scannerDialog = BTScannerDialog(requireContext(), requireActivity())
                    scannerDialog.show()
                    //                    onStartStopScan(it)
                }
            } else {
                binding.scanButton.visibility = View.INVISIBLE
            }

            mainActivityViewModel.setBluetoothState(isChecked)


        }/*
                val recyclerView: RecyclerView = binding.recyclerDevices
                recyclerView.setHasFixedSize(true)
                val layoutManager = LinearLayoutManager(context)
                recyclerView.setLayoutManager(layoutManager)
                adapter = BLEDeviceAdapter()
                recyclerView.setAdapter(adapter)
        */


        binding.recyclerTags.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerTags.adapter = tagsAdapter

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
        setupObservers()
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.push_button_config))
        }
        AppClass.instance.itagPressMode = ITagModesEnum.test

        //----------------------------------------------------------------
        ErrorsObservable.addErrorListener(mErrorListener)
        sIsShown = true/*
          setupContent()
          // TODO:  Waytoday.gpsLocationUpdater.addOnPermissionListener(gpsPermissionListener)
          disposableBag.add(ITag.ble.observableState().subscribe { event -> setupContent() })
      *//*
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

        disposableBag.add(ITag.store.observable().subscribe { event ->
            when (event.op) {
                StoreOpType.change -> {
                    Toast.makeText(requireContext(), "sorete", Toast.LENGTH_LONG).show()
                }//setupContent()
                StoreOpType.forget -> {

                    binding.recyclerTags.adapter?.notifyDataSetChanged()/*
                                        var tag: ITagDefault = event.tag as ITagDefault
                                        var index = -1

                                        adapter?.scanResults?.forEach { existingTag ->
                                            index++
                                            if (existingTag.id == tag.id()) {
                                                binding.recyclerTags.adapter?.notifyItemRemoved(index)
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
                        binding.recyclerTags.adapter?.notifyItemInserted(adapter?.scanResults?.count() ?: 0 - 1)
                    }

                     */
                    binding.recyclerTags.adapter?.notifyDataSetChanged()
                }
            }
        })
        requireContext().bindService(
            ITagsService.intentBind(requireContext()), mServiceConnection, 0
        )/*
                TODO: Resolverlo
                if (Waytoday.tracker.isOn(this) && PowerManagement.needRequestIgnoreOptimization(this)) {
                    if (resumeCount++ > 1) {
                        Handler(getMainLooper()).post { PowerManagement.requestIgnoreOptimization(this) }
                    }
                }

         */

        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i)
            if (itag != null) {
                val connection = ITag.ble.connectionById(itag.id())

                disposableBag.add(
                    connection.observableState().subscribe { state: BLEConnectionState? ->
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


                disposableBag.add(connection.observableClick().subscribe { event: Int? ->
                    pressedTimes++
                    var tagPosition = -1
                    val adapterData = (binding.recyclerTags.adapter as TagsAdapter).dataSet

                    for (k in 0 until adapterData.count()) {
                        tagPosition++
                        if (adapterData.byPos(tagPosition).id() == itag.id()) {
                            binding.recyclerTags.scrollToPosition(tagPosition)

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


    }


    override fun onPause() {
        removeObservers()
        super.onPause()
        unregisterReceivers()
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


    fun setupObservers() {

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
                    binding.recyclerTags.visibility = View.VISIBLE
                    binding.btStatusLayout.visibility = View.GONE
                    binding.switchEnabled.isEnabled = true
                }

                BLEState.NO_ADAPTER -> {
                    mainActivityViewModel.showError("Tu dispositivo no permite conectarse a dispositivos Bluetooth LE")
                    binding.recyclerTags.visibility = View.GONE
                    binding.btStatusDescrip.text = "Bluetooth no está disponible"
                    binding.btStatusLayout.visibility = View.VISIBLE
                    tagsAdapter.onBluetoothOff()
                }

                BLEState.NOT_ENABLED -> {
                    binding.switchEnabled.isEnabled = false
                    binding.switchEnabled.isChecked = false
                    binding.recyclerTags.visibility = View.GONE
                    binding.btStatusDescrip.text = "Bluetooth está desactivado"
                    binding.btStatusLayout.visibility = View.VISIBLE
                }
            }

        }


    }


    fun removeObservers() {
        mainActivityViewModel.bluetoothStatus.removeObservers(this)
        viewModel?.bluetoothStatus?.removeObservers(this)
    }


    fun startService() {
        Toast.makeText(context, "Start Service", Toast.LENGTH_SHORT).show()/*
            var intentService = Intent(requireContext(), ITagService::class.java)
            requireContext().bindService(intentService, bleServiceConn, Context.BIND_AUTO_CREATE)
            requireContext().startService(intentService)

        */
    }


    private fun registerReceivers() {
        val intentFilter = IntentFilter()/*
                intentFilter.addAction(BROADCAST_MESSAGE_SCAN_RESULT_UPDATED)
                intentFilter.addAction(BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_LIST)
                intentFilter.addAction(BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED)
                intentFilter.addAction(BROADCAST_MESSAGE_BLE_SCAN_DISCOVERING)
                intentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED)
                intentFilter.addAction(BROADCAST_MESSAGE_PANIC_BUTTON_TEST)
          *//*
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver,
            intentFilter
        )*/
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