package com.iyr.ian.ui.main

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor.Solid
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_REFRESH_PANIC_BUTTON
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_SHOW_FOOTER_TOOLBAR
import com.iyr.ian.Constants
import com.iyr.ian.Constants.Companion.BROADCAST_LOCATION_SERVICE_AVALAIBILITY
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.GeoLocation
import com.iyr.ian.databinding.FragmentMainBinding
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.SOSActivationMethodsEnum
import com.iyr.ian.nonui.NonUI
import com.iyr.ian.services.eventservice.EventService
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.PulseValidationRequest
import com.iyr.ian.ui.interfaces.EventsPublishingCallback
import com.iyr.ian.ui.views.home.fragments.main.adapters.ISpeedDialAdapter
import com.iyr.ian.ui.views.home.fragments.main.adapters.SpeedDialAdapter
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.SMSUtils
import com.iyr.ian.utils.UIUtils
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.connectivity.NetworkStatus
import com.iyr.ian.utils.connectivity.NetworkStatusHelper
import com.iyr.ian.utils.connectivity.makeNetworkRequest
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.createDirectoryStructure
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.hasOpenedDialogs
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.makeAPhoneCall
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.multimedia.prepareMediaMessage
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.permissionsReadWrite
import com.iyr.ian.utils.permissionsVibration
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.requestPermissionsLocation
import com.iyr.ian.utils.saveImageToCache
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.utils.telephony.makePhoneCall
import com.iyr.ian.utils.toGrayscale
import com.iyr.ian.viewmodels.HomeFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import com.triggertrap.seekarc.SeekArc
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.UUID


interface HomeFragmentInteractionCallback {
    fun onError(exception: Exception)
    fun publishPanicEventDone(event: Event) {
    }/*
        fun onSpeedDialAdded(contact: Contact)
        fun onSpeedDialChanged(contact: Contact)
        fun onSpeedDialRemoved(contact: Contact)
    */
}

class HomeFragment(
    val homeActivityCallback: EventsPublishingCallback? = null,
    var mainActivityViewModel: MainActivityViewModel? = null
) : Fragment(), NetworkStateReceiver.NetworkStateReceiverListener, HomeFragmentInteractionCallback,
    ISpeedDialAdapter {


    fun HomeFragment() {
        // Constructor vacío
    }

    /*
    constructor() : this(null as EventsPublishingCallback, null as MainActivityViewModel)
    //   private var homeActivityCallback: EventsPublishingCallback? = null
*/
    private val commonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {
                when (intent.action) {
                    BROADCAST_ACTION_REFRESH_PANIC_BUTTON -> {
                        updatePanicButton()
                    }
                }
            }

        }
    }
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private var viewModel = HomeFragmentViewModel()


    private val locationAvalaibilityReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // TODO("Not yet implemented")
                when (intent?.action) {
                    BROADCAST_LOCATION_SERVICE_AVALAIBILITY -> {
                        updateUI()
                    }
                }
            }
        }
    }


    private var networkObserver: NetworkStatusHelper? = null
    private var hasConnectivity: Boolean = true

    //private var mPresenter: HomeFragmentPresenter = HomeFragmentPresenter(this)
    private lateinit var speedDialAdapter: SpeedDialAdapter

    var isPanicButtonBusy: Boolean = false

    private val args : HomeFragmentArgs by navArgs()

    private lateinit var binding: FragmentMainBinding
    private var recordSession: MediaRecorder? = null
    private var recordingFilename: String? = null
    private var timer: Timer? = null

    private var toPickImagePermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val imagePickerIntent =
                    Lassi(requireContext()).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(1).setGridSize(3)
                        .setMediaType(MediaType.IMAGE) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                        .setCompressionRatio(50) // compress image for single item selection (can be 0 to 100)
                        .setSupportedFileTypes(
                            "jpg", "jpeg", "png", "webp", "gif"
                        ).setMinFileSize(100) // Restrict by minimum file size
                        .setMaxFileSize(1024) //  Restrict by maximum file size
                        .setStatusBarColor(R.color.white).setToolbarResourceColor(R.color.white)
                        .setProgressBarColor(R.color.colorAccent)
                        .setPlaceHolder(R.drawable.ic_image_placeholder)
                        .setErrorDrawable(R.drawable.ic_image_placeholder)
                        .setSelectionDrawable(R.drawable.ic_checked_media)
                        .setAlertDialogNegativeButtonColor(R.color.white)
                        .setAlertDialogPositiveButtonColor(R.color.darkGray)
                        .setGalleryBackgroundColor(R.color.gray)//Customize background color of gallery (default color is white)
                        .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                        .build()


                pickImageContract?.launch(imagePickerIntent)
            } else {
                requireActivity().permissionsForImages()
            }
        }

    private var pickImageContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as java.util.ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {

                    lifecycleScope.launch(Dispatchers.IO) {

                        var localPath = selectedMedia[0].path.toString()/*
                                                var fileName =
                                                    CHAT_FILES_STORAGE_PATH + this@MessagesInEventFragment.eventKey.toString() + "/" + localPath.getJustFileName()
                        */
                        var fileName = localPath.getJustFileName()


                        try {


                            var mediaFile = requireContext().prepareMediaMessage(
                                MediaTypesEnum.IMAGE, fileName, localPath
                            )

                            if (mediaFile is MediaFile) {
                                var pp = 3

                                var panicEventKey = AppClass.instance.getPanicEventKey().toString()

                                viewModel.onNewMediaMessage(
                                    panicEventKey, mainActivityViewModel?.user?.value!!, mediaFile
                                )

                            }

                            if (mediaFile is java.lang.Exception) {
                                Toast.makeText(
                                    requireContext(), mediaFile.localizedMessage, Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (ex: Exception) {
                            Looper.prepare()
                            Toast.makeText(
                                requireContext(), ex.localizedMessage, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }


    private var toTakeVideoPermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val videoPickerIntent =
                    Lassi(requireContext()).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(5).setGridSize(3).setMediaType(MediaType.VIDEO)
                        .setMaxFileSize(1024)
                        .setCompressionRatio(10) // compress image for single item selection (can be 0 to 100)
                        .setMinTime(5) // for MediaType.VIDEO only
                        .setMaxTime(60) // for MediaType.VIDEO only
                        .setSupportedFileTypes(
                            "mp4", "mkv", "webm", "avi", "flv", "3gp"
                        ) // Filter by limited media format (Optional)
                        .setMinFileSize(100) // Restrict by minimum file size
                        .setMaxFileSize(1024) //  Restrict by maximum file size
                        .disableCrop() // to remove crop from the single image selection (crop is enabled by default for single image)
                        /*
                     * Configuration for  UI
                     */.setStatusBarColor(R.color.colorPrimaryDark)
                        .setToolbarResourceColor(R.color.colorPrimary)
                        .setProgressBarColor(R.color.colorAccent)
                        .setPlaceHolder(R.drawable.ic_image_placeholder)
                        .setErrorDrawable(R.drawable.ic_image_placeholder)
                        .setSelectionDrawable(R.drawable.ic_checked_media)
                        .setAlertDialogNegativeButtonColor(R.color.white)
                        .setAlertDialogPositiveButtonColor(R.color.colorPrimary)
                        .setGalleryBackgroundColor(R.color.gray)//Customize background color of gallery (default color is white)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                        .enableActualCircleCrop() // Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
                        .build()

                /*
             .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)

 */
                pickVideoContract?.launch(videoPickerIntent)
            } else {
                requireActivity().permissionsForVideo()
            }
        }

    private var pickVideoContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as java.util.ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        Log.d("VIDEO_FILE", selectedMedia[0].path.toString())
                        val localPath = selectedMedia[0].path.toString()
                        //   val media = MediaFile(MediaTypesEnum.VIDEO, filePath)

                        var fileName = localPath.getJustFileName()
                        try {
                            var mediaFile = requireContext().prepareMediaMessage(
                                MediaTypesEnum.VIDEO, fileName, localPath
                            )

                            when (mediaFile) {
                                is MediaFile -> {

                                    var videoDimentions =
                                        requireContext().getDimentions(Uri.parse(localPath))
                                    mediaFile.width = videoDimentions["width"]
                                    mediaFile.height = videoDimentions["height"]


                                    //---- muevo el archivo grabado al Cache
                                    val originalPath = localPath

                                    val storagePath = localPath.getJustFileName()

                                    val finalPath =
                                        requireContext().cacheDir.toString() + "/" + fileName

                                    var newFilePath = ""

                                    if (originalPath.contains(requireContext().cacheDir.toString())) {

                                        // Si el archivo fue generado en cache, lo mueve
                                        newFilePath = FileUtils().moveFile(
                                            originalPath.substringBeforeLast("/"),
                                            originalPath.getJustFileName(),
                                            finalPath.substringBeforeLast("/")
                                        )
                                    } else {
                                        // Si el archivo no estaba en Cache , lo copio
                                        newFilePath = FileUtils().copyFile(
                                            originalPath.substringBeforeLast("/"),
                                            originalPath.getJustFileName(),
                                            finalPath.substringBeforeLast("/")
                                        ).toString()
                                    }
                                    //     mediaFile.file_name = fileName

                                    var panicEventKey =
                                        AppClass.instance.getPanicEventKey().toString()

                                    viewModel.onNewMediaMessage(
                                        panicEventKey,
                                        mainActivityViewModel?.user?.value!!,
                                        mediaFile
                                    )
                                }

                                is java.lang.Exception -> {
                                }
                            }
                        } catch (ex: Exception) {
                            Toast.makeText(
                                requireContext(), ex.localizedMessage, Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                }
            }
        }


    companion object {
        private var redButtonLastTouch: Long = System.currentTimeMillis()
    }

    init {
        redButtonLastTouch = System.currentTimeMillis()
    }

    fun subscribe() {
        requireActivity().showSnackBar(
            binding.root, "Implementar en el ViewModel el metodo subscribe"
        )
//        mPresenter.subscribe()
    }

    fun unSubscribe() {/*
              requireActivity().showSnackBar(
                  binding.root,
                  "Implementar en el ViewModel el metodo unsubscribe"
              )
      */
//        mPresenter.unSubscribe()
    }

    var eventService: EventService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)



        findNavController().popBackStack(findNavController().currentDestination?.id!!, false)

        mainActivityViewModel = MainActivityViewModel.getInstance()
        mainActivityViewModel?.setUser(
            SessionForProfile.getInstance(requireContext()).getUserProfile()
        )
        if (mainActivityViewModel?.appStatus?.value != MainActivityViewModel.AppStatus.READY) {

            val mainActivity = requireActivity() as MainActivity
            mainActivity.setAppStatus(MainActivityViewModel.AppStatus.INITIALIZING)
        }
        eventService = EventService.getInstance(requireContext())

        startNetworkBroadcastReceiver(requireContext())
        registerLocationPermissionsReceiver()

        speedDialAdapter = SpeedDialAdapter(requireActivity(), this)

        val eventService = AppClass.instance.serviceLocationPointer?.ServiceLocationBinder()
     //   (requireActivity() as MainActivity).setListnerToRootView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        activity?.findViewById<View>(R.id.top_section)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        redButtonLastTouch = System.currentTimeMillis()
        setupUI()
        requireContext().broadcastMessage(null, BROADCAST_ACTION_SHOW_FOOTER_TOOLBAR)
        // setupObservers()
        return binding.root
    }

    private fun startObservers(userKey: String) {


        AppClass.instance.panic.observe(viewLifecycleOwner) { isInPanic ->
            if (isInPanic) {
                switchPanicButtonToPanic()
                var panicEventKey = AppClass.instance.getPanicEventKey().toString()
                var currentEvent = viewModel.getEventObservedKey()
                if (currentEvent == null || currentEvent != panicEventKey) {
                    viewModel.onObserveEventRequest(panicEventKey)
                }
            } else switchPanicButtonToReady()
        }


        viewModel.speedDialFlow.observe(viewLifecycleOwner) { resource ->

            val newList = resource.data

            var adapterData = speedDialAdapter.getData()

            val copyOfAdapterData = ArrayList(adapterData)
            copyOfAdapterData.forEach { oldRecord ->
                if (!newList!!.contains(oldRecord)) {
                    adapterData.remove(oldRecord)
                }
            }

            newList?.forEach { newRecord ->
                if (!adapterData.contains(newRecord)) {
                    adapterData.add(newRecord)
                }
            }

            speedDialAdapter.notifyDataSetChanged()
            binding.recyclerContacts.scrollToPosition(0)

            /*
            when (contact) {
                is SpeedDialRepository.DataEvent.OnChildAdded -> {
                    var newSpeedDialContact = contact.data
                    if (!adapterData.contains(newSpeedDialContact)) {
                        adapterData.add(0,newSpeedDialContact)
                        speedDialAdapter.notifyItemInserted(0)
                        binding.recyclerContacts.scrollToPosition(0)
                    }
                }

                is SpeedDialRepository.DataEvent.OnChildChanged -> {

                    var newSpeedDialContact = contact.data
                    var index = adapterData.indexOf(newSpeedDialContact)
                    if (index>-1) {
                        adapterData.set(index, newSpeedDialContact)
                        speedDialAdapter.notifyItemChanged(index)
                    }
                }

                is SpeedDialRepository.DataEvent.OnChildRemoved -> {
                    var newSpeedDialContact = contact.data
                    var index = adapterData.indexOf(newSpeedDialContact)
                    if (index>-1) {
                        adapterData.removeAt(index)
                        speedDialAdapter.notifyItemRemoved(index)
                    }
                }

                else -> {}
            }

    */
        }


        viewModel.viewers.observe(viewLifecycleOwner) { viewers ->

            lifecycleScope.launch {
                viewers.forEach { viewer ->

                    var extras = viewer.getExtras()
                    var userKey: String? = null
                    if (extras != null && extras.containsKey("userKey")) {
                        userKey = extras.get("userKey")
                        try {

                            /*
                                                        var storageReference = FirebaseStorage.getInstance()
                                                            .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                                                            .child(userKey!!)
                                                            .child(viewer.getImageSrc().toString()).downloadUrl.await()
                            */
                            var storageReferenceCache = FirebaseStorage.getInstance()
                                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                                .child(userKey!!).child(viewer.getImageSrc().toString())
                                .downloadUrlWithCache(requireContext())

                            viewer.setImageSrc(storageReferenceCache.toString())
                            binding.activationInstructions.visibility = View.GONE
                            binding.panicViewers.updateAvatars(viewers)
                            binding.panicEventViewersSection.visibility = View.VISIBLE
                        } catch (exception: Exception) {
                            var pp = 33
                        }
                    }
                }

                if (viewers.size > 0) {/*
                                        binding.activationInstructions.visibility = View.GONE
                                        binding.panicViewers.updateAvatars(viewers)
                                        binding.panicEventViewersSection.visibility = View.VISIBLE
                                        */
                } else {
                    binding.panicEventViewersSection.visibility = View.GONE
                    binding.activationInstructions.visibility = View.VISIBLE
                }


                requireActivity().runOnUiThread {
                    binding.panicEventViewersSection.forceLayout()
                    binding.panicEventViewersSection.invalidate()
                    view?.invalidate()
                }

            }


        }

        /*
              mainActivityViewModel.isInPanic.observe(viewLifecycleOwner) { isInPanic ->
                  if (!isInPanic)
                      switchToPanicOn()
                  else
                      switchToPanicOff()
              }
      */
        mainActivityViewModel?.isLocationAvailable?.observe(viewLifecycleOwner) { available ->
            if (available) {
                if (mainActivityViewModel?.isInPanic?.value == true) {
                    switchPanicButtonToPanic()
                } else {
                    switchPanicButtonToReady()
                    setupPanicButtonModality()
                }
            } else {
                disablePanicButton()
            }
        }

        eventService?.getResult()?.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> binding.seekCounter.progress = 0
                is Resource.Error -> binding.seekCounter.progress = 0
                else -> {}
            }
        }/*
                AppClass.instance.eventsFollowed.observe(viewLifecycleOwner) { events ->

        //            viewModel.processEventsList(events)
                }
        */

        //      viewModel.listenSpeedDialContacts(userKey)

        viewModel.sendingContent.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> {
                    requireActivity().showErrorDialog(
                        getString(R.string.error),
                        resource.message.toString(),
                        getString(R.string.close),
                        null
                    )
                }

                is Resource.Loading -> {
                    requireActivity().showLoader(R.raw.lottie_animation_nodes_of_people_neus_vich)
                }

                is Resource.Success -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        requireActivity().hideLoader()
                        requireActivity().showLoader(R.raw.lottie_done)
                        delay(1000)
                        requireActivity().hideLoader()
                    }
                }

                null -> {}
            }

        }
    }

    private fun removeObservers() {
        AppClass.instance.lastLocation.removeObservers(viewLifecycleOwner)

        AppClass.instance.panic.removeObservers(viewLifecycleOwner)


        /*
              mainActivityViewModel.isInPanic.observe(viewLifecycleOwner) { isInPanic ->
                  if (!isInPanic)
                      switchToPanicOn()
                  else
                      switchToPanicOff()
              }
      */
        mainActivityViewModel?.isLocationAvailable?.removeObservers(viewLifecycleOwner)

        eventService?.getResult()?.removeObservers(viewLifecycleOwner)

        //  AppClass.instance.eventsFollowed.removeObservers(viewLifecycleOwner)
        viewModel.speedDialUpdates.removeObservers(viewLifecycleOwner)
        viewModel.viewers.removeObservers(viewLifecycleOwner)

    }

    private fun disablePanicButton() {
        val grayScaleImage = requireContext().getBitmapFromVectorDrawable(
            R.drawable.sos_in_main_image
        )?.toGrayscale()

        binding.redButton.setImageBitmap(grayScaleImage)
        binding.redButton.setOnTouchListener(null)
        binding.redButton.setOnClickListener {
            if (!hasConnectivity) {
                if (!requireActivity().hasOpenedDialogs()) {
                    requireActivity().showErrorDialog(
                        getString(R.string.connectivity),
                        getString(R.string.no_connectivity_limitations_explanation),
                        getString(R.string.close),
                        null
                    )
                }
            } else if (!mainActivityViewModel?.isLocationAvailable?.value!!) {
                if (!requireActivity().hasOpenedDialogs()) {
                    (requireActivity() as MainActivity).requestPermissions()
                }
            }
        }
    }

    private fun switchPanicButtonToReady() {
        binding.statusTitle.text = getString(R.string.are_you_in_trouble)
        binding.panicEventViewersSection.visibility = View.GONE
        binding.panicViewers.visibility = View.GONE
        binding.seekCounter.isEnabled = true
        binding.panicMultiButton.visibility = View.GONE
        binding.redButton.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(), R.drawable.sos_in_main_image
            )
        )
        setupPanicButtonModality()
        //     viewModel.viewers.removeObservers(viewLifecycleOwner)
    }

    private fun switchPanicButtonToPanic() {

        binding.statusTitle.text = getString(R.string.panic_button_fired)
        binding.panicViewers.visibility = View.VISIBLE
        binding.panicEventViewersSection.visibility = View.VISIBLE
        binding.redButton.setOnTouchListener(null)
        binding.stopPanicButton.setOnClickListener {
            requireContext().handleTouch()
            if (!requireActivity().areLocationPermissionsGranted(true)) {
                binding.panicMultiButton.visibility = View.GONE
                (requireActivity() as MainActivity).requestPermissions()
            } else {
                var panicEventKey = AppClass.instance.getPanicEventKey()


                val validatorRequestType = PulseValidationRequest(
                    PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT, panicEventKey
                )
                mainActivityViewModel?.requestValidationDialog(validatorRequestType)/*
                                var data = HashMap<String, String>()
                                data.put("panicEventKey", panicEventKey ?: "")

                                requireContext().broadcastMessage(
                                    data,
                                    BROADCAST_ACTION_CANCEL_PANIC
                                )
                                binding.panicMultiButton.visibility = View.VISIBLE
                */
            }
        }

        binding.seekCounter.isEnabled = false


        NonUI.getInstance(requireContext()).getUser()/*
         if (AppClass.instance.isFreeUser()) {
             binding.redButton.setImageDrawable(
                 AppCompatResources.getDrawable(
                     requireContext(), R.drawable.sos_in_main_full_red
                 )
             )
         } else {
             */
        binding.redButton.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(), R.drawable.sos_in_main_multi_function
            )
        )
        // }

        binding.panicMultiButton.visibility = View.VISIBLE
        binding.stopPanicButton.visibility = View.VISIBLE
        //       if (!AppClass.instance.isFreeUser()) {

        binding.sendPictureButton.visibility = View.VISIBLE
        binding.sendVideoButton.visibility = View.VISIBLE
        binding.sendVoiceMessageButton.visibility = View.VISIBLE
        binding.messageButton.visibility = View.VISIBLE


    }


    private fun registerObservers() {

        networkObserver = NetworkStatusHelper(requireContext())
        networkObserver?.observe(this) { status ->
            when (status) {
                NetworkStatus.Available -> {
                    //                  if (hasConnectivity == false) {
                    hasConnectivity = true
                    requireActivity().showSnackBar(binding.root, "Hay conectividad")
                    updateUI()
                    //                }
                }

                NetworkStatus.Unavailable -> {
//                    if (hasConnectivity == true) {
                    hasConnectivity = false
                    requireActivity().showSnackBar(
                        binding.root, getString(R.string.no_connectivity)
                    )
                    updateUI()
                    //                  }
                }

            }
        }
    }

    private fun unregisterObservers() {
        networkObserver?.removeObserver { }
    }


    private fun setupUI() {

        var me = SessionForProfile.getInstance(requireContext()).getUserProfile()


        binding.buttonQr.setOnClickListener {
            requireContext().handleTouch()
            showQRPopup()
            /*
                        if (requireContext().loadImageFromCache("qr_code", "images") == null) {
                            prepareQrCode()
                        } else
                            findNavController().navigate(R.id.qrCodeDisplayPopup)
            */
        }


        binding.recyclerContacts.adapter = speedDialAdapter
        binding.recyclerContacts.setItemTransformer(
            ScaleTransformer.Builder().setMaxScale(1.05f).setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build()
        )

        binding.recyclerContacts.setOffscreenItems(2)
        binding.recyclerContacts.setSlideOnFling(true)
        binding.recyclerContacts.setOverScrollEnabled(true)

        binding.stopPanicButton.setOnClickListener {
            requireContext().handleTouch()
            if (context is HomeFragmentInteractionCallback) {
                //         (context as HomeFragmentInteractionCallback).onEmergencyCancelButtonPressed()
                Toast.makeText(
                    requireContext(), "Implementar Cancelacion del Evento", Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.sendPictureButton.setOnClickListener {
            requireActivity().permissionsReadWrite()
            toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        binding.sendVideoButton.setOnClickListener {
            requireActivity().permissionsReadWrite()
            toTakeVideoPermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        binding.sendVoiceMessageButton.setOnTouchListener(View.OnTouchListener { view, motionEvent -> //if Button is Pressed.! or user Id Holding Button
            recordingManagement(motionEvent)
            true
        })

    }


    private fun recordingManagement(motionEvent: MotionEvent): Boolean {
        Log.d("RECORDING ", " action = " + motionEvent.action.toString())
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("RECORDING ", "START")

                var panicEventKey = AppClass.instance.getPanicEventKey().toString()

                recordingFilename =
                    AppClass.instance.cacheDir.toString() + "/" + AppConstants.CHAT_FILES_STORAGE_PATH + panicEventKey + "/" + UUID.randomUUID()
                        .toString() + ".3gp"

                requireContext().createDirectoryStructure(recordingFilename!!)

                recordSession = MultimediaUtils(requireContext()).startRecording(
                    requireActivity(), recordingFilename!!
                )!!

                startMonitoringWave()

                var iconBitmap = requireContext().getBitmapFromVectorDrawable(
                    R.drawable.ic_recording
                )

                binding.sendVoiceMessageButton.setImageBitmap(iconBitmap)
                requireActivity().playSound(R.raw.recording_start, null, null)

            }

            MotionEvent.ACTION_UP -> {
                Log.d("RECORDING ", "END")
                try {
                    requireActivity().playSound(R.raw.recording_stop, null, null)

                    var iconBitmap = requireContext().getBitmapFromVectorDrawable(
                        R.drawable.ic_microphone
                    )
                    binding.sendVoiceMessageButton?.setImageBitmap(iconBitmap)

                    //Do Nothing
                    MultimediaUtils(requireContext()).stopRecording(
                        recordSession, recordingFilename!!
                    )

                    val callback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {

                        }
                    }


                    stopMonitoringWave()


                    val me = SessionForProfile.getInstance(requireContext()).getUserProfile()

                    /*
                                        var newFileLocation = FileUtils().moveFile(
                                            recordingFilename.toString().substringBeforeLast("/"),
                                            recordingFilename.toString().getJustFileName(),
                                            requireContext().cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH + eventKey!!
                                        )
                    */
                    Log.d("AUDIO_FILE", "2-" + recordingFilename.toString())

                    try {
                        var fileName = recordingFilename.toString().getJustFileName()
                        ///             var localFolder = recordingFilename.toString().substringBeforeLast("/").replace(requireContext().cacheDir.toString()+"/","")

                        val mediaFile = requireContext().prepareMediaMessage(
                            MediaTypesEnum.AUDIO, fileName, recordingFilename.toString()
                        )


                        when (mediaFile) {
                            is MediaFile -> {
                                mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                                mediaFile.time = Date().time

                                lifecycleScope.launch(Dispatchers.IO) {

                                    var panicEventKey =
                                        AppClass.instance.getPanicEventKey().toString()
                                    viewModel.onNewMediaMessage(
                                        panicEventKey,
                                        mainActivityViewModel?.user?.value!!,
                                        mediaFile
                                    )
                                }
                                recordSession = null
                            }

                            else -> {
                                requireActivity().showErrorDialog(mediaFile.toString())
                            }
                        }
                    } catch (ex: Exception) {
                        requireActivity().showErrorDialog(ex.localizedMessage)
                    }

                } catch (ex: Exception) {
                    recordSession = null
                }
            }
        }
        return false
    }


    private fun startMonitoringWave() {
        binding.audioRecordView.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            while (binding.audioRecordView.visibility == View.VISIBLE) {
                val currentMaxAmplitude = recordSession?.maxAmplitude ?: 0
                Log.d("WAVE", currentMaxAmplitude.toString())
                binding.audioRecordView.update(currentMaxAmplitude)   //redraw view
                delay(100)
            }
        }
    }

    private fun stopMonitoringWave() {
        binding.audioRecordView.visibility = View.INVISIBLE
        binding.audioRecordView.recreate()
    }


    var maxTouchesReached = false // Flag para evitar multiples toques
    private fun setupPanicButtonForTap() {
        binding.redButton.setOnTouchListener(null)
        binding.seekCounter.progress = 0
        binding.redButton.setOnClickListener {
            //vibrateOnTouch(this@HomeFragment.requireActivity(), true)
            if (maxTouchesReached) return@setOnClickListener
            binding.redButton.isClickable = false

            if (binding.seekCounter.isEnabled && binding.seekCounter.progress + 1 <= binding.seekCounter.max) {
                requireContext().handleTouch()
                if (!binding.seekCounter.isVisible) {
                    binding.seekCounter.visibility = View.VISIBLE
                    binding.seekCounter.progress = 0
                }
                binding.seekCounter.progress = binding.seekCounter.progress + 1

                val currentProgress: Int = binding.seekCounter.progress
                val handler = Handler(Looper.myLooper()!!)
                handler.postDelayed({
                    if (currentProgress == binding.seekCounter.progress) {
                        if (binding.seekCounter.isVisible) {
                            binding.seekCounter.visibility = View.VISIBLE
                            binding.seekCounter.progress = 0
                            maxTouchesReached = false
                            binding.redButton.isClickable = true
                        }
                    }
                }, 300)

            } else {
                maxTouchesReached = true
                Toast.makeText(
                    requireContext(), "The Button's touch is disabled", Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    // Panic button Hold

    private fun setupPanicButtonForHold() {
        binding.redButton.setOnClickListener(null)
        binding.redButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                if (binding.seekCounter.isEnabled) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onSOSActionDown()
                        return true
                    } else {
                        if (event.action == MotionEvent.ACTION_UP) {
                            onSOSActionUp()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "The Button's touch is disabled", Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        })


    }

    private fun onSOSActionUp() {
        if (binding.seekCounter.isVisible) {
            binding.seekCounter.visibility = View.INVISIBLE
        }
        Log.d("SOS_BUTTON", "ACTION_UP")
        Log.d(
            "PRESSING_BUTTON", "Canceling"
        )
        if (binding.seekCounter.progress == 0) {
            binding.seekCounter.progress = 0
        }
        timer?.cancel()
        timer?.purge()
    }

    private fun onSOSActionDown() {
        Log.d("SOS_BUTTON", "ACTION_DOWN")
        if (!binding.seekCounter.isVisible) {
            binding.seekCounter.visibility = View.VISIBLE
        }
        binding.seekCounter.progress = 0
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
        }

        timer = Timer()
        Log.d(
            "SOS_BUTTON", "CREO EL TIMER" + ""
        )
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("TIMER", "BUCLE")
                //vibrateOnTouch(this@HomeFragment.requireActivity(), true)
                requireContext().handleTouch()
                this@HomeFragment.requireActivity().runOnUiThread {
                    if (binding.seekCounter.progress < binding.seekCounter.max) {
                        binding.seekCounter.progress = binding.seekCounter.progress + 1
                    }

                }
                Log.d(
                    "SOS_BUTTON", binding.seekCounter.progress.toString()
                )
            }
        }, 0, 1000) //time out 5s
    }


    fun updateUI() {
        if (isVisible) {

            updatePanicButton()


        }
    }

    /**
     * updatePanicButton()
     *
     * Actualiza el aspecto del Panic Button dependiendo del estado y version de la APP
     *
     *
     */

    private fun updatePanicButton() {/*
               when (hasConnectivity && requireContext().areLocationPermissionsGranted(true)) {
                   false -> {
                       val grayScaleImage =
                           requireContext().getBitmapFromVectorDrawable(
                               R.drawable.sos_in_main_image
                           )?.toGrayscale()

                       binding.redButton.setImageBitmap(grayScaleImage)
                       binding.redButton.setOnTouchListener(null)
                       binding.redButton.setOnClickListener {

                           if (!hasConnectivity) {
                               requireActivity().showErrorDialog(
                                   getString(R.string.connectivity),
                                   getString(R.string.no_connectivity_limitations_explanation),
                                   getString(R.string.close),
                                   null
                               )
                           }
                       }
                   }

                   true -> {
                       if (requireActivity().checkLocationRequirementsFit()) {


                           if (mainActivityViewModel.isInPanic.value == true) {
                               /*
                                                       binding.redButton.setOnTouchListener(null)
                                                       binding.stopPanicButton.setOnClickListener {
                                                           requireContext().handleTouch()
                                                           requireContext().broadcastBLEMessage(
                                                               null,
                                                               BROADCAST_ACTION_CANCEL_PANIC
                                                           )
                                                       }
                                                       binding.panicMultiButton.visibility = View.VISIBLE

                                                       binding.seekCounter.isEnabled = false

                                                       if (AppClass.instance.isFreeUser()) {
                                                           binding.redButton.setImageDrawable(
                                                               AppCompatResources.getDrawable(
                                                                   requireContext(),
                                                                   R.drawable.sos_in_main_full_red
                                                               )
                                                           )
                                                       } else {
                                                           binding.redButton.setImageDrawable(
                                                               AppCompatResources.getDrawable(
                                                                   requireContext(),
                                                                   R.drawable.sos_in_main_multi_function
                                                               )
                                                           )
                                                       }


                                                       binding.stopPanicButton.visibility = View.VISIBLE
                                                       binding.sendPictureButton.isVisible = !AppClass.instance.isFreeUser()
                                                       binding.sendVideoButton.isVisible = !AppClass.instance.isFreeUser()
                                                       binding.sendVoiceMessageButton.isVisible = !AppClass.instance.isFreeUser()
                                                       binding.messageButton.isVisible = !AppClass.instance.isFreeUser()

                               */
                           } else {
                               /*
                                                       binding.seekCounter.isEnabled = true

                                                       binding.panicMultiButton.visibility = View.GONE
                                                       binding.redButton.setImageDrawable(
                                                           AppCompatResources.getDrawable(
                                                               requireContext(),
                                                               R.drawable.sos_in_main_image
                                                           )
                                                       )
                               */
                               //            binding.sosTextview.visibility = View.VISIBLE
                               //   binding.redButton.setColor(requireContext().getColor(R.color.colorRed))
                           }

                           setupPanicButtonModality()


                       } else {
                           binding.redButton.setImageDrawable(
                               AppCompatResources.getDrawable(
                                   requireContext(),
                                   R.drawable.sos_in_main_image
                               )
                           )
                           binding.redButton.setOnClickListener(View.OnClickListener {
                               requireActivity().requestLocationRequirements(object :
                                   LocationRequirementsCallback {
                                   override fun onRequirementsComplete() {
                                       updateUI()
                                   }
                               })
                               /*
                                                   if (requireContext().isGPSEnabled()) {
                                                       requireContext().requestPermissionsLocation()
                                                   } else {
                                                       var clickListener = object : OnConfirmationButtonsListener{
                                                           override fun onAccept() {
                                                               startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                                           }
                                                       }
                                                       requireActivity().showConfirmationDialog(
                                                           getString(R.string.warning),
                                                           getString(R.string.gps_enabled_is_required),
                                                           getString(R.string.enable),
                                                           getString(R.string.close),
                                                           clickListener
                                                       )

                                                   }

                                                   */
                           })
                       }
                   }
               }
       */
    }

    private fun setupPanicButtonModality() {
        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        binding.seekCounter.max = me.sos_invocation_count
        when (me.sos_invocation_method) {
            SOSActivationMethodsEnum.HOLD.name -> {
                val legend = String.format(
                    getString(R.string.sos_hold_activation_instruction), me.sos_invocation_count
                )
                binding.activationInstructions.text = legend
                setupPanicButtonForHold()
            }

            SOSActivationMethodsEnum.TAP.name -> {

                val legend: String = String.format(
                    getString(R.string.sos_tap_activation_instruction), me.sos_invocation_count
                )
                binding.activationInstructions.text = legend
                setupPanicButtonForTap()
            }
        }


        binding.seekCounter.setOnSeekArcChangeListener(object : SeekArc.OnSeekArcChangeListener {
            override fun onProgressChanged(counter: SeekArc?, progress: Int, p2: Boolean) {

                Log.d("_PROGRESS_", progress.toString())
                if (progress >= counter!!.max) {
                    binding.seekCounter.isEnabled = false
                    lifecycleScope.launch(Dispatchers.IO) {
/*
                        withContext(Dispatchers.Main){
                            binding.seekCounter.progress = 0
                            if (binding.seekCounter.isVisible) {
                                binding.seekCounter.visibility = View.INVISIBLE
                            }
                        }
  */
                        onEmergencyButtonPressed()
                        maxTouchesReached = false
                    }
                    /*
                    val handler = Handler()
                    handler.postDelayed({
                        binding.seekCounter.progress = 0
                        onSOSActionUp()
                        if (!binding.seekCounter.isVisible) {
                            binding.seekCounter.visibility = View.INVISIBLE
                        }
                        updateUI()
                        Log.d("SOS_BUTTON", "DISPARO EVENTO")
                        onEmergencyButtonPressed()
                        processingTap = false
//Toast.makeText(requireContext(),"Implementar el disparo del evento",Toast.LENGTH_SHORT).show()
                    }, 100)
                    */
                } else {
                    binding.redButton.isClickable = true
                    binding.seekCounter.isEnabled = true
                }
            }

            override fun onStartTrackingTouch(p0: SeekArc?) {
                //TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekArc?) {
                //    TODO("Not yet implemented")
            }
        })

    }

    private fun isPanicButtonActive(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onResume() {
        registerNetworkBroadcastReceiver(requireContext())
        super.onResume()
        AppClass.instance.setCurrentFragment(this)
        registerReceivers()
        startObservers(FirebaseAuth.getInstance().uid.toString())

        var mainActivityBindings = (requireActivity() as MainActivity).binding


//        (requireActivity() as MainActivity).setTitleBarTitle(R.string.app_long_title)

        if (findNavController().currentDestination?.id == R.id.homeFragment) {
            var appToolbar = (requireActivity() as MainActivity).appToolbar
            appToolbar.enableBackBtn(false)
            appToolbar.updateTitle(getString(R.string.app_long_title))
            mainActivityBindings.includeCustomToolbar.root.visibility = View.VISIBLE
            mainActivityBindings.bottomToolbar.visibility = View.VISIBLE
        }

        val activityRootView = (requireActivity() as MainActivity).binding.root
        val activityBindings = (requireActivity() as MainActivity).binding
        (requireActivity() as MainActivity).restoreNavigationFragment()

        try {
            if (args.firstRun)
            {
                (requireActivity() as MainActivity).setListnerToRootView()
            }
        }
        catch (ex: Exception)
        {

        }


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

    override fun onPause() {
        unregisterNetworkBroadcastReceiver(requireContext())
        unRegisterReceivers()
        removeObservers()
        super.onPause()
        //     AppClass.instance.removeViewFromStack( this)
    }

    fun getRedButtonRef(): ImageView {
        return binding.redButton
    }


    override fun onError(exception: Exception) {
        TODO("Not yet implemented")
    }


    @SuppressLint("MissingPermission")
    suspend fun onEmergencyButtonPressed() {
        //  if (SessionApp.getInstance(requireContext()).isInPanic == false) {
        binding.redButton.isClickable = false
        if (mainActivityViewModel?.isInPanic?.value == false) {

            val hasNetworkAccess = requireContext().makeNetworkRequest()

            withContext(Dispatchers.Main) {
                binding.seekCounter.progress = 0
                if (binding.seekCounter.isVisible) {
                    binding.seekCounter.visibility = View.INVISIBLE
                }

                if (requireContext().permissionsVibration()) {
                    val buzzer =
                        requireActivity().getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
                    buzzer.let {
                        val pattern = longArrayOf(0, 200, 100, 300, 100, 2000)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            //deprecated in API 26
                            buzzer.vibrate(pattern, -1)
                        }
                    }
                }

//                binding.redButton.isEnabled = false

//                binding.redButton.isEnabled = true

                mainActivityViewModel?.setButtonToPanic()

                if (hasNetworkAccess) {
                    // Actualiza la UI para mostrar que hay acceso a datos
                    preparePanicEvent()
                } else {
                    // Actualiza la UI para mostrar que no hay acceso a datos
                    val locationManager: LocationManager =
                        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

                    val locationListener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationManager.removeUpdates(this)
                            CoroutineScope(Dispatchers.IO).launch {
                                var myName =
                                    (requireActivity() as MainActivity).viewModel.user.value?.first_name + " " + (requireActivity() as MainActivity).viewModel.user.value?.last_name

                                viewModel.speedDialFlow.value?.data?.forEach { contact ->
                                    if (contact.telephone_number?.length ?: 0 >= 10) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Enviando mensaje a ${contact.display_name} - (${contact.telephone_number})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        SMSUtils.sendLocationSMS(
                                            myName, contact.telephone_number, location
                                        )
                                    }
                                }
                            }
                        }

                        override fun onLocationChanged(locations: MutableList<Location>) {
                            super.onLocationChanged(locations)
                        }

                        override fun onStatusChanged(
                            provider: String?, status: Int, extras: Bundle?
                        ) {
                            // super.onStatusChanged(provider, status, extras)
                        }

                    }
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 30, 0F, locationListener
                    )

                }

            }

            SessionApp.getInstance(requireContext()).isInPanic(true) // Mejorarlo


            var nonUI = NonUI.getInstance(requireContext())

            //         lifecycleScope.launch(Dispatchers.IO) {

            /*
                            withContext(Dispatchers.Main) {
                                if (hasNetworkAccess) {
                                    // Actualiza la UI para mostrar que hay acceso a datos
                                    preparePanicEvent()
                                } else {
                                    // Actualiza la UI para mostrar que no hay acceso a datos
                                    val locationManager: LocationManager =
                                        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

                                    val locationListener = object : LocationListener {
                                        override fun onLocationChanged(location: Location) {
                                            locationManager.removeUpdates(this)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                var myName =
                                                    (requireActivity() as MainActivity).viewModel.user.value?.first_name + " " + (requireActivity() as MainActivity).viewModel.user.value?.last_name

                                                viewModel.speedDialFlow.value?.data?.forEach { contact ->
                                                    if (contact.telephone_number?.length ?: 0 >= 10) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Enviando mensaje a ${contact.display_name} - (${contact.telephone_number})",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        SMSUtils.sendLocationSMS(
                                                            myName, contact.telephone_number, location
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        override fun onLocationChanged(locations: MutableList<Location>) {
                                            super.onLocationChanged(locations)
                                        }

                                        override fun onStatusChanged(
                                            provider: String?, status: Int, extras: Bundle?
                                        ) {
                                            // super.onStatusChanged(provider, status, extras)
                                        }

                                    }
                                    locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER, 30, 0F, locationListener
                                    )

                                }
                            }
                        */
            //           }
        } else {
            Toast.makeText(requireContext(), "You already are in PANIC!!!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun preparePanicEvent() {

        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                //             CoroutineScope(Dispatchers.IO).launch {


                val newEvent = Event()
                newEvent.author_key =
                    SessionForProfile.getInstance(requireContext()).getUserId()
                newEvent.event_type = EventTypesEnum.PANIC_BUTTON.name
                newEvent.status = EventStatusEnum.DANGER.name
                newEvent.event_location_type = EventLocationType.REALTIME.name
                newEvent.time = System.currentTimeMillis()

                val latLng = LatLng(
                    location.latitude, location.longitude
                )

                newEvent.location = EventLocation()
                newEvent.location?.latitude = latLng.latitude
                newEvent.location?.longitude = latLng.longitude
                val geoLocationAtCreation = GeoLocation()
                geoLocationAtCreation.l = ArrayList<Double>()
                (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.latitude)
                (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.longitude)
                geoLocationAtCreation.event_time = newEvent.time
                newEvent.location_at_creation = geoLocationAtCreation
                eventService?.fireEvent(newEvent)

                //               }
            }

            override fun onLocationChanged(locations: MutableList<Location>) {
                super.onLocationChanged(locations)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // super.onStatusChanged(provider, status, extras)
            }

        }

        // Comprueba si ya tienes el permiso de ubicación.
        if (ContextCompat.checkSelfPermission(
                requireContext(), ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 30, 0F, locationListener
            )
        } else requireContext().requestPermissionsLocation()
    }
//--------------------------


//    }


    private fun publishPanicEvent(event: Event, callback: OnCompleteCallback?) {
        requireActivity().showSnackBar(
            binding.root, "Implementar en el ViewModel el metodo publishPanicEvent"
        )

//        mPresenter.publishPanicEvent(event, callback)
    }


    //------------------- networkStatus
    override fun networkAvailable() {
        hasConnectivity = true
        updateUI()
    }

    override fun networkUnavailable() {
        hasConnectivity = false
        updateUI()
    }


    private fun startNetworkBroadcastReceiver(currentContext: Context) {
        networkStateReceiver = NetworkStateReceiver()
        networkStateReceiver.addListener(this)
        registerNetworkBroadcastReceiver(currentContext)
    }

    /**
     * Register the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun registerNetworkBroadcastReceiver(currentContext: Context) {

        Log.d("NetworkBroadcasReceiver - register", this.javaClass.name)
        currentContext.registerReceiver(
            networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }


    private fun registerLocationPermissionsReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_LOCATION_SERVICE_AVALAIBILITY)
        intentFilter.addAction(BROADCAST_LOCATION_SERVICE_AVALAIBILITY)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationAvalaibilityReceiver, intentFilter
        )
    }


    /**
     * Unregister the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun unregisterNetworkBroadcastReceiver(currentContext: Context) {
        Log.d("NetworkBroadcasReceiver - unregister", this.javaClass.name)
        currentContext.unregisterReceiver(networkStateReceiver)
    }


    var phoneNumberToCall: String? = null
    override fun makeAPhoneCall(phoneNumber: String) {

// verifica permisos y llama al telefono contenido en phoneNumber
        phoneNumberToCall = phoneNumber
        requireActivity().makeAPhoneCall(phoneNumber)

    }

    override fun sendSMSInvitation(contactName: String, phoneNumber: String) {
        TODO("Not yet implemented")
    }

    // Manejar la respuesta de la solicitud de permiso
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PHONE_CALL_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, realizar la llamada
                    requireActivity().makePhoneCall(phoneNumberToCall!!)
                    phoneNumberToCall = null
                } else {
                    // Permiso denegado, mostrar un mensaje o manejar de otra forma

                    AlertDialog.Builder(requireContext()).setTitle("Permiso denegado")
                        .setMessage("El permiso para realizar llamadas telefónicas ha sido denegado. Por favor, habilita el permiso en la configuración de la aplicación.")
                        .setPositiveButton("Aceptar", null).show()
                }
                return
            }

            else -> {
                // Ignorar otros casos de solicitud de permiso
            }
        }
    }


    fun registerReceivers() {

        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_REFRESH_PANIC_BUTTON)

        LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(
            commonReceiver, intentFilter
        )

        // forceLocationUpdate()
    }


    fun unRegisterReceivers() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(commonReceiver)
    }


    fun prepareQrCode() {
        Toast.makeText(
            requireContext(),
            "Generando QR Code - Modificar para que se genere al hacer el setup y se haga una sola vez.",
            Toast.LENGTH_SHORT
        ).show()
        lifecycleScope.launch(Dispatchers.IO) {

            val map: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
            map["action"] = AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP
            map["key"] = FirebaseAuth.getInstance().uid.toString()
            val dinamicLink = UIUtils.createShortDynamicLink(requireContext(), map)
            when (dinamicLink) {
                is Resource.Success -> {
                    val totalText = StringBuffer()
                    totalText.append(dinamicLink.data.toString())
                    generateQRCode(totalText.toString())

                }

                is Resource.Error -> {
                    requireActivity().showErrorDialog(dinamicLink.message.toString())
                }

                else -> {}
            }


        }

    }

    private fun generateQRCode(link: String): Bitmap {
        val data = QrData.Url(link)

        val options = QrVectorOptions.Builder()
            .setPadding(.3f)
            .setLogo(
                QrVectorLogo(
                    drawable = ContextCompat
                        .getDrawable(requireContext(), R.drawable.logo_vertical),
                    size = .25f,
                    padding = QrVectorLogoPadding.Natural(.2f),
                    shape = QrVectorLogoShape.RoundCorners(.25f)

                )
            )
            .setBackground(
                QrVectorBackground(
                    drawable = ContextCompat
                        .getDrawable(requireContext(), R.drawable.qr_frame),
                )
            )
            .setColors(
                QrVectorColors(
                    dark = Solid(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
                    ball = Solid(
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    )
                )
            )
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape
                        .RoundCorners(.5f),
                    ball = QrVectorBallShape
                        .RoundCorners(.25f),
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f),
                )
            )
            .build()

        val bitmap: Bitmap = QrCodeDrawable(data, options).toBitmap(800, 800)
        bitmap.saveImageToCache(requireContext(), "qr_code.png")
        lifecycleScope.launch(Dispatchers.Main) {
            findNavController().navigate(R.id.qrCodeDisplayPopup)
        }
        return bitmap
    }


    private lateinit var codeScanner: CodeScanner
    private lateinit var qrPopupWindow: PopupWindow
    fun showQRPopup() {

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_qr, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        qrPopupWindow = PopupWindow(popupView, width, height, true)

        val buttonQr = popupView.findViewById<ImageView>(R.id.show_qr_button)
        val buttonScanQr = popupView.findViewById<ImageView>(R.id.scan_qr_button)

        buttonQr.setOnClickListener {
            qrPopupWindow.dismiss()

            if (requireContext().loadImageFromCache("qr_code.png", "images") != null) {
                findNavController().navigate(R.id.qrCodeDisplayPopup)
            } else {
                prepareQrCode()
            }

        }

        buttonScanQr.setOnClickListener {


            qrPopupWindow.dismiss()

            findNavController().navigate(R.id.qrCodeScanningFragment)
            /*
            val scannerView = requireActivity().findViewById<CodeScannerView>(R.id.scanner_view)
            scannerView.visibility = View.VISIBLE

            if (!::codeScanner.isInitialized) {
                codeScanner = CodeScanner(requireContext(), scannerView)

                // Parameters (default values)
                codeScanner.camera =
                    CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                codeScanner.formats = listOf(BarcodeFormat.QR_CODE)
                codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
                codeScanner.isFlashEnabled = false // Whether to enable flash or not

                // Callbacks
                codeScanner.decodeCallback = DecodeCallback {

                    requireActivity().runOnUiThread {
                        scannerView.visibility = View.GONE

                        FirebaseDynamicLinks.getInstance().getDynamicLink(Uri.parse(it.text))
                            .addOnSuccessListener(
                                requireActivity()
                            ) { pendingDynamicLinkData ->
                                // Get deep link from result (may be null if no link is found)
                                var deepLink: Uri?
                                if (pendingDynamicLinkData != null) {
                                    deepLink = pendingDynamicLinkData.link

                                    val action = deepLink?.getQueryParameter("action")
                                    val key = deepLink?.getQueryParameter("key").toString()

                                    if ((key).compareTo(
                                            SessionForProfile.getInstance(requireContext()).getUserId()
                                        ) == 0
                                    ) {
                                        requireActivity().showErrorDialog(
                                            getString(
                                                R.string.error_cannot_send_it_to_you
                                            )
                                        )
                                    } else {
                                        when (action) {
                                            AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP -> {
                                                MainActivityViewModel.getInstance()
                                                    .onContactByUserKey(key)
                                            }

                                            AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL -> {

                                                requireActivity().showSnackBar(
                                                    binding.root,
                                                    "Implementar en el viewmodel onFriendshipRequestAndSpeedDialByUserKey"
                                                )

                                            }
                                        }
                                    }
                                    Toast.makeText(requireContext(), action, Toast.LENGTH_LONG).show()
                                }


                                // Handle the deep link. For example, open the linked content,
                                // or apply promotional credit to the user's account.
                                // ...

                                // ...
                            }.addOnFailureListener(
                                requireActivity()
                            ) { e -> Log.w("DYNAMIC-LINKS", "getDynamicLink:onFailure", e) }
                        /*
                                                MainActivityViewModel.getInstance().onContactByUserKey(key)
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Scan result: ${it.text}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                        */
                    }


                }
                codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS

                    requireActivity().runOnUiThread {

                        scannerView.visibility = View.GONE
                        Toast.makeText(
                            requireContext(), "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            // scannerView.setOnClickListener {
            codeScanner.startPreview()*/

        }

        val offsetX = binding.buttonQr.width - qrPopupWindow.width
        qrPopupWindow.showAsDropDown(binding.buttonQr, offsetX, 4.dp)

    }


}