package com.iyr.ian.ui.events.fragments


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.google.android.libraries.places.widget.Autocomplete
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.Constants.Companion.AUTOCOMPLETE_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.FragmentEventAditionalMediaBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.RecordingStatusEnum
import com.iyr.ian.services.eventservice.EventService
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.events.fragments.adapters.EventMediaAdapter
import com.iyr.ian.ui.events.fragments.adapters.MediaHandlingCallback
import com.iyr.ian.ui.interfaces.ErrorInterface
import com.iyr.ian.ui.interfaces.EventsPublishingCallback
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.appsInternalStorageFolder
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.multimedia.editTextDialog
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.multimedia.getDuration
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showConfirmationDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.viewmodels.EventsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID


interface EventAdditionalMediaFragmentCallback : ErrorInterface, EventsPublishingCallback

class EventAdditionalMediaFragment() : Fragment(), EventAdditionalMediaFragmentCallback,
    MediaHandlingCallback {


    val viewModel: EventsFragmentViewModel by lazy { EventsFragmentViewModel.getInstance() }

    val mainActivityViewModel: MainActivityViewModel by lazy {
        MainActivityViewModel.getInstance(
            this.requireContext(),
            FirebaseAuth.getInstance().uid.toString()
        )
    }

    private val filesCompressionData = 50
    private val filesMinimumSizeInKB = 100L
    private val filesMaximumSizeInKB = 1024L


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
            if (it.resultCode == RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {
                    val media = MediaFile(MediaTypesEnum.IMAGE, selectedMedia[0].path.toString())
                    viewModel.onImageAdded(media.file_name)
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
                        .setCompressionRatio(60) // compress image for single item selection (can be 0 to 100)
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


                pickVideoContract?.launch(videoPickerIntent)
            } else {
                requireActivity().permissionsForVideo()
            }
        }

    private var pickVideoContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {
                    val filePath = selectedMedia[0].path.toString()
                    val media = MediaFile(MediaTypesEnum.VIDEO, filePath)
                    media.duration = requireContext().getDuration(Uri.parse("file:$filePath"))
                    val dimensionsMap = requireContext().getDimentions(Uri.parse("file:$filePath"))
                    media.width = dimensionsMap["width"]!!
                    media.height = dimensionsMap["height"]!!
                 //   viewModel.onVideoAdded(media.file_name, media.duration)
                    viewModel.onVideoAdded(media)
                }
            }
        }


    private var hasConnectivity: Boolean = true
    private var isSaving = false
    private lateinit var binding: FragmentEventAditionalMediaBinding
    private lateinit var mediaRecyclerView: RecyclerView
    private var recordingFilename: String? = null
    private var recordSession: MediaRecorder? = null
    private lateinit var mediaAdapter: EventMediaAdapter

    private lateinit var eventLocationType: String
    private var eventLocation: EventLocation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventAditionalMediaBinding.inflate(inflater, container, false)


        setupMediaAdapter()
        setupRecordButton()

        binding.changeLocation.setOnClickListener {
            mainActivityViewModel.goBack()
        }

        binding.addTextButton.setOnClickListener {
            requireActivity().editTextDialog(requireActivity(), "", object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {
                    viewModel.onTextAdded(result.toString())
                }
            })
        }

        binding.addImageButton.setOnClickListener {
            toPickImagePermissionsRequest?.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        binding.recordVideoButton.setOnClickListener {
            toTakeVideoPermissionsRequest?.launch(arrayOf(Manifest.permission.CAMERA))
        }

        binding.sendButton.setOnClickListener {
            requireActivity().handleTouch()
            //         if (hasConnectivity) {
            binding.sendButton.isEnabled = false
            isSaving = true

            viewModel.onPostEventClicked()

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }


    private fun setupUI() {

    }

    private fun setupMediaAdapter() {
        mediaAdapter = EventMediaAdapter(requireContext(), this)

        binding.mediaRecyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mediaRecyclerview.adapter = mediaAdapter
    }

    private fun setupRecordButton() {
        binding.recordButton.setRecordView(binding.recordView)
        binding.recordView.setLockEnabled(true)
        binding.recordView.setRecordLockImageView(binding.recordLock)
        binding.recordButton.isListenForRecord = true
        binding.recordButton.setOnRecordClickListener {
            Toast.makeText(requireContext(), "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
            Log.d("RecordButton", "RECORD BUTTON CLICKED")
        }

        binding.recordView.cancelBounds = 8.dp.toFloat()

        binding.recordView.setSmallMicColor(Color.parseColor("#c2185b"))

        binding.recordView.setLessThanSecondAllowed(false)

        binding.recordView.setSlideToCancelText("Slide To Cancel")

        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
//                recordStart()
                mainActivityViewModel.onStartRecordingPressed()
                binding.recordView.isVisible = true
            }

            override fun onCancel() {

            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
//                recordStop()
                mainActivityViewModel.onStopRecording()


            }

            override fun onLessThanSecond() {


                disposeRecording()
            }

            override fun onLock() {
            }
        })


        binding.recordView.setOnBasketAnimationEndListener {
            Log.d(
                "RecordView", "Basket Animation Finished"
            )
        }

    }

    private fun disposeRecording() {
        try {
            stopMonitoringWave()
            showOtherSourcesButtons()
            requireActivity().playSound(R.raw.recording_stop, null, null)
            //Do Nothing
            MultimediaUtils(requireContext()).stopRecording(recordSession, recordingFilename!!)
            Toast.makeText(context, "Disposed.!", Toast.LENGTH_SHORT).show()
            FileUtils().deleteFile(recordingFilename!!.substringBeforeLast("/"))

            recordSession = null


        } catch (ex: Exception) {
            recordSession = null
        }

    }


    private fun startMonitoringWave() {
        binding.audioRecordView.visibility = VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                val currentMaxAmplitude = recordSession?.maxAmplitude ?: 0
                binding.audioRecordView.update(currentMaxAmplitude)   //redraw view
                delay(100)
            }
        }
    }

    private fun stopMonitoringWave() {
        binding.audioRecordView.visibility = INVISIBLE
        binding.audioRecordView.recreate()
    }


    private fun publishEvent() {
        viewModel.prepareToPublish(FirebaseAuth.getInstance().uid.toString())
    }


    override fun onResume() {
        super.onResume()
        val appToolbar = (requireActivity() as MainActivity).appToolbar
        appToolbar.updateTitle(getString(R.string.event_publish_additional_content_title))

        startObservers()
        measureObjects()
    }

    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    private fun measureObjects() {
        val viewTreeObserver = binding.mediaCardView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.mediaCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val mediaCardViewRect = Rect()
                    binding.mediaCardView.getLocalVisibleRect(mediaCardViewRect)

                    val recordSoundButtonArr = IntArray(2)
                    binding.recordButton.getLocationOnScreen(recordSoundButtonArr)

                    val audioRecordViewParams = binding.audioRecordView.layoutParams
                    audioRecordViewParams.width =
                        (mediaCardViewRect.width() - resources.getDimension(R.dimen.box_normal) - 20.dp).toInt()
                    binding.audioRecordView.layoutParams = audioRecordViewParams
                    binding.linearControls.invalidate()

                    val mediaRecyclerViewParams = binding.mediaRecyclerview.layoutParams
                    mediaRecyclerViewParams.height = resources.getDimension(R.dimen.box_xxxbig)
                        .toInt() + audioRecordViewParams.height

                    binding.mediaRecyclerview.layoutParams = mediaRecyclerViewParams
                    binding.mediaRecyclerview.invalidate()
                }
            })
        }
    }


    private val eventService by lazy { EventService.getInstance(requireContext()) }


    private fun startObservers() {


        viewModel.contactsGroupsListFlow.observe(this) { }
        viewModel.groupsList.observe(this) { }

        eventService.flow.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader(R.raw.lottie_bell_expansion_by_jennifer_hood)
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    // mainActivityViewModel.switchToModule(0, "home")

                    findNavController().navigate(R.id.homeFragment).apply {
                        // borrar el backstack
                        findNavController().clearBackStack(R.id.homeFragment)
                        eventService.resetFlow()
                    }


                    val eventKey = status.data?.event_key!!

                    MainActivityViewModel.getInstance().showGoToEventDialog(null, eventKey)

/*
                    findNavController().navigate(R.id.eventPublishedDoneDialog, bundle)
                    val callbackDialog: OnEventPublishedDone = object : OnEventPublishedDone {
                        override fun onBringMeToEvent() {
                            if (requireActivity() is MainActivityCallback) {
                                (requireActivity() as MainActivityCallback).goToEvent(status.data?.event_key!!)
                            }
                        }

                        override fun onRefuse() {

                        }
                    }
                    val doneDialog = EventPublishedDoneDialog(
                        requireContext(), requireActivity(), callbackDialog
                    )
                    lifecycleScope.launch(Dispatchers.Main) {
                        doneDialog.show()
                    }
                    */
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(status.message.toString())
                    eventService.resetFlow()
                    binding.sendButton.isEnabled = true
                }

                else -> {
                    var pp = 22
                }
            }
        }

        viewModel.showContactGroupSelector.observe(this) { status ->
            when (status) {
                true -> {
                    findNavController().navigate(R.id.action_eventAdditionalMediaFragment_to_networkSelectionDialog)
                    viewModel.resetShowContactGroupSelector()
                }

                false -> {
                    MainActivityViewModel.getInstance().onEventReadyToFire(viewModel.event.value!!)
                    viewModel.resetShowContactGroupSelector()
                }

                null -> {}
            }
        }

        viewModel.fixedLocation.observe(this) { location ->
            eventLocationType = viewModel.eventLocationType.value ?: "missing"
            if (eventLocationType == EventLocationType.FIXED.name) {
                binding.addressReadOnly.setText(location?.formated_address)
                binding.changeLocation.isVisible =
                    (eventLocationType == EventLocationType.FIXED.name)

            } else binding.addressReadOnly.setText(R.string.at_your_location)
        }

        viewModel.eventType.observe(this) { eventType ->
            (AppClass.instance.getCurrentActivity() as MainActivity).setTitleBarTitle(
                requireContext().getEventTypeName(eventType.toString())
            )
            binding.avatarImage.setImageDrawable(requireContext().getEventTypeDrawable(eventType.toString()))
            when (eventType.toString()) {
                EventTypesEnum.SCORT_ME.name -> {
                    binding.locationInputLayout.hint = getString(R.string.destination)
                }

                else -> {
                    binding.locationInputLayout.hint = getString(R.string.address_to_delibery) + ":"
                }
            }

        }

        viewModel.eventMediaFlow.observe(this) { mediaList ->
            mediaAdapter.setData(mediaList)
            mediaAdapter.notifyDataSetChanged()
        }

        mainActivityViewModel.newMedia.observe(this) { media ->
            if (media != null) {
                when (media.media_type) {
                    MediaTypesEnum.IMAGE -> {
                        viewModel.onImageAdded(media.file_name)
                    }

                    MediaTypesEnum.VIDEO -> {
                        viewModel.onVideoAdded(media)
                    }

                    MediaTypesEnum.AUDIO -> {
                        viewModel.onAudioAdded(media.file_name, media.duration)
                    }

                    MediaTypesEnum.TEXT -> TODO()
                }
                mainActivityViewModel.resetNewMedia()
            }
        }

        mainActivityViewModel.recordingStatus.observe(this) { status ->
            when (status) {
                RecordingStatusEnum.NONE -> {}
                RecordingStatusEnum.RECORDING -> {
                    hideOtherSourcesButtons()
                    startMonitoringWave()

                }

                RecordingStatusEnum.STOPING -> {
                    stopMonitoringWave()
                    showOtherSourcesButtons()

                }

                RecordingStatusEnum.DISPOSING -> {
                    stopMonitoringWave()
                    showOtherSourcesButtons()
                }
            }
        }
    }

    private fun stopObservers() {
        viewModel.contactsGroupsListFlow.removeObservers(this)
        viewModel.groupsList.removeObservers(this)

        eventService.flow.removeObservers(this)
        viewModel.showContactGroupSelector.removeObservers(this)
        viewModel.fixedLocation.removeObservers(this)
        viewModel.eventType.removeObservers(this)
        viewModel.eventMediaFlow.removeObservers(this)
        mainActivityViewModel.newMedia.removeObservers(this)
        mainActivityViewModel.recordingStatus.removeObservers(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === RESULT_OK) {

            /* ARREGLAR ESTO


                      if (requestCode === Picker.PICK_VIDEO_DEVICE) {
                          /*
                                     if (videoPicker == null) {
                                         videoPicker = VideoPicker(this)
                                         videoPicker.setVideoPickerCallback(this)
                                     }
                                     videoPicker.submit(attr.data)
                            */
                      } else if (requestCode === Picker.PICK_VIDEO_CAMERA) {
                          val filePath =
                              requireContext().filesDir.absolutePath + "/movies" + "/" + Uri.parse(data?.data?.encodedPath).lastPathSegment
                          val retriever = MediaMetadataRetriever()
                          retriever.setDataSource(context, Uri.parse(filePath))
                          val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                          val timeInMillisecond: Int = time.toString().toInt()
                          val duration = timeInMillisecond

                          eventsFragmentViewModel.onVideoAdded(filePath, duration)

                      } else

                          if (requestCode === Picker.PICK_IMAGE_DEVICE) {
                              val filePath = Uri.parse(data?.data?.encodedPath).toString()
                              //                   onNewImage(filePath)
                              eventsFragmentViewModel.onImageAdded(filePath)
                          } else
                          */
            if (requestCode === AUTOCOMPLETE_REQUEST_CODE) {

                val place = Autocomplete.getPlaceFromIntent(data)

                //      setCurrentLocationAsAddress(place)
                eventLocation = EventLocation()
                eventLocation!!.latitude = place.latLng.latitude
                eventLocation!!.longitude = place.latLng?.longitude
                eventLocation!!.formated_address = place.address

                viewModel.onFixedLocation(eventLocation)
                requireContext().hideKeyboard(requireView())

            }
        }
    }

    override fun onPublishEventDone(event: Event?) {
        // callback.onPublishEventDone(event)
        Toast.makeText(requireContext(), "onPublishEventDone", Toast.LENGTH_SHORT).show()
    }


    companion object {
        // TODO: Rename parameter arguments, choose names that match
    }


    private fun recordStart() {
        hideOtherSourcesButtons()
        startMonitoringWave()
        val directory = File(requireContext().filesDir.path.toString() + "/audios/")
        if (!directory.exists()) {
            directory.mkdir()
        }
        recordingFilename = directory.path + UUID.randomUUID().toString() + ".3gp"
        recordSession = MultimediaUtils(requireContext()).startRecording(
            requireActivity(), recordingFilename!!
        )!!
        requireActivity().playSound(R.raw.recording_start, null, null)
    }

    private fun hideOtherSourcesButtons() {
        lifecycleScope.launch(Dispatchers.Main) {
            hideSourceButton(binding.recordVideoButton)
            hideSourceButton(binding.addImageButton)
            hideSourceButton(binding.addTextButton)
        }
    }

    private suspend fun hideSourceButton(button: ConstraintLayout) {
        button.visibility = INVISIBLE
        delay(100)
    }

    private fun showOtherSourcesButtons() {
        lifecycleScope.launch(Dispatchers.Main) {
            showSourceButton(binding.addTextButton)
            showSourceButton(binding.addImageButton)
            showSourceButton(binding.recordVideoButton)
        }
    }

    private suspend fun showSourceButton(button: ConstraintLayout) {
        button.visibility = VISIBLE
        delay(100)
    }


    private fun recordStop() {
        try {
            stopMonitoringWave()
            showOtherSourcesButtons()
            requireActivity().playSound(R.raw.recording_stop, null, null)
            //Do Nothing
            MultimediaUtils(requireContext()).stopRecording(recordSession, recordingFilename!!)
            Toast.makeText(context, "Recorded.!", Toast.LENGTH_SHORT).show()

            FileUtils().copyFile(
                recordingFilename!!.substringBeforeLast("/"),
                recordingFilename!!.getJustFileName(),
                requireContext().appsInternalStorageFolder().path.toString()
            )

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(recordingFilename)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration

            viewModel.onAudioAdded(recordingFilename!!, duration)

            recordSession = null


        } catch (ex: Exception) {
            recordSession = null
        }
    }


    override fun onDeleteMediaButtonPressed(mediaFile: MediaFile) {
        super.onDeleteMediaButtonPressed(mediaFile)

        lifecycleScope.launch(Dispatchers.Main) {
            requireContext().handleTouch()
            val clickListener = object : IAcceptDenyDialog {
                override fun onAccept() {
                    viewModel.removeMedia(mediaFile)
                }
            }
            requireActivity().showConfirmationDialog(
                getString(R.string.eliminate),
                getString(R.string.do_you_want_to_eliminate_content),
                getString(R.string.yes),
                getString(R.string.cancel),
                clickListener
            )
        }

    }
}