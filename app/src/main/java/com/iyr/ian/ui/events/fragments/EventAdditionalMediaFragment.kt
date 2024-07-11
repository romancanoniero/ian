package com.iyr.ian.ui.events.fragments


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.google.android.libraries.places.widget.Autocomplete
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.Constants.Companion.AUTOCOMPLETE_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.callbacks.MediaPickersInterface
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.FragmentEventAditionalMediaBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.RecordingStatusEnum
import com.iyr.ian.services.eventservice.EventService
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.callback.MainActivityCallback
import com.iyr.ian.ui.events.EventsFragmentViewModel
import com.iyr.ian.ui.events.OnPostFragmentInteractionCallback
import com.iyr.ian.ui.events.fragments.adapters.EventMediaAdapter
import com.iyr.ian.ui.events.fragments.adapters.MediaHandlingCallback
import com.iyr.ian.ui.events.fragments.dialogs.EventPublishedDoneDialog
import com.iyr.ian.ui.events.fragments.dialogs.OnEventPublishedDone
import com.iyr.ian.ui.events.fragments.dialogs.network_selection.NetworkSelectionDialog
import com.iyr.ian.ui.events.fragments.dialogs.network_selection.OnNetworkListSelection
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
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showConfirmationDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID


interface EventAdditionalMediaFragmentCallback : ErrorInterface, EventsPublishingCallback

class EventAdditionalMediaFragment(
    //  private var thisEvent: Event,
    val callback: OnPostFragmentInteractionCallback,
    val eventsFragmentViewModel: EventsFragmentViewModel,
    private val mainActivityViewModel: MainActivityViewModel
) : Fragment(), EventAdditionalMediaFragmentCallback, MediaHandlingCallback {


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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaRecyclerView = view.findViewById(R.id.media_recyclerview)
        setupUI()
    }


    private fun setupUI() {
        setupMediaAdapter()

        setupRecordButton()

        binding.changeLocation.setOnClickListener {
            mainActivityViewModel.goBack()
        }

        binding.addTextButton.setOnClickListener {
            requireActivity().editTextDialog(requireActivity(), "", object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {
                    eventsFragmentViewModel.onTextAdded(result.toString())
                }
            })
        }

        binding.addImageButton.setOnClickListener {
            (requireActivity() as MediaPickersInterface).pickImage()
        }

        binding.recordVideoButton.setOnClickListener {
            (requireActivity() as MainActivity).recordVideo()
        }

        binding.sendButton.setOnClickListener {
            requireActivity().handleTouch()
            if (hasConnectivity) {
                binding.sendButton.isEnabled = false
                isSaving = true
                requireActivity().showLoader(R.raw.lottie_campana_emitiendo)
                lifecycleScope.launch(Dispatchers.IO) {

                    var notificationListResource =  eventsFragmentViewModel.getNotificationList(eventsFragmentViewModel.event.value?.author_key.toString())

                    when (notificationListResource) {
                        is Resource.Error -> {
                            requireActivity().hideLoader()
                            requireActivity().showErrorDialog(notificationListResource.message.toString())
                        }

                        is Resource.Loading -> {
                            requireActivity().showLoader("Consultando las Listas de Notificaciones")
                        }

                        is Resource.Success -> {
                            var list = notificationListResource.data!!

                            var event = eventsFragmentViewModel.event.value!!

                            if (list.size > 0) {
                                val callback: OnNetworkListSelection = object : OnNetworkListSelection {
                                    override fun onSelected(listKey: String) {
//                                publishEvent(listKey)

                                        // Register the service
                                        val eventService = EventService.getInstance(requireContext())
//                                val livePostEventData = eventService.getResult()
                                        event.group_key = listKey
                                        eventService.fireEvent(event)
                                    }

                                    override fun onCanceled() {
                                        binding.sendButton.isEnabled = true
                                    }
                                }

                                val networkSelectionDialog = NetworkSelectionDialog(
                                    requireContext(), requireActivity(), callback
                                )

                                networkSelectionDialog.show(list)
                            } else {
                                // Register the service
                                val eventService = EventService.getInstance(requireContext())
                                event.group_key = "_default"
                                eventService.fireEvent(event)
                            }

                        }
                    }



                    //---------------------
/*
                    var currentLocationResource = requireActivity().getCurrentLocation()

                    when (currentLocationResource) {
                        is Resource.Error -> {
                            requireActivity().hideLoader()
                            requireActivity().showErrorDialog(currentLocationResource.message.toString())
                        }

                        is Resource.Loading -> {
                            requireActivity().showLoader(resources.getString(R.string.please_wait))
                        }

                        is Resource.Success -> {
                            //-------
                            var currentLocation = currentLocationResource.data!!
                            val geoLocationAtCreation = GeoLocation()
                            geoLocationAtCreation.l = ArrayList<Double>()
                            (geoLocationAtCreation.l as ArrayList<Double>).add(currentLocation.latitude)
                            (geoLocationAtCreation.l as ArrayList<Double>).add(currentLocation.longitude)
                            geoLocationAtCreation.event_time = Date().time
                            eventsFragmentViewModel.event.value!!.location_at_creation =
                                geoLocationAtCreation
                            //-------
                            publishEvent()

                        }
                    }
*/
                }

            } else {
                requireActivity().showSnackBar(
                    binding.root, getString(R.string.no_connectivity)
                )
            }
        }
    }

    private fun setupMediaAdapter() {
        mediaAdapter = EventMediaAdapter(requireContext(), this)
        mediaRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mediaRecyclerView.adapter = mediaAdapter
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
                var oo = 33
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
//                recordStop()
                mainActivityViewModel.onStopRecording()


            }

            override fun onLessThanSecond() {
                var dede = 33

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
        eventsFragmentViewModel.prepareToPublish(FirebaseAuth.getInstance().uid.toString())
    }


    override fun onResume() {
        super.onResume()
        setupObservers()
        measureObjects()
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
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

    private fun setupObservers() {

        eventsFragmentViewModel.showNotificationListSelector.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader(resources.getString(R.string.please_wait))
                }


                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(status.message.toString())
                }


                is Resource.Success -> {
                    requireActivity().hideLoader()
                    val list = status.data!!
                    if (list.size > 0) {
                        val callback: OnNetworkListSelection = object : OnNetworkListSelection {
                            override fun onSelected(listKey: String) {
//                                publishEvent(listKey)

                                // Register the service
                                val eventService = EventService.getInstance(requireContext())
//                                val livePostEventData = eventService.getResult()
                                eventsFragmentViewModel.event.value!!.group_key = listKey
                                eventService.fireEvent(eventsFragmentViewModel.event.value!!)
                            }

                            override fun onCanceled() {
                                binding.sendButton.isEnabled = true
                            }
                        }

                        val networkSelectionDialog = NetworkSelectionDialog(
                            requireContext(), requireActivity(), callback
                        )

                        networkSelectionDialog.show(list)
                    } else {
                        // Register the service
                        val eventService = EventService.getInstance(requireContext())
                        eventsFragmentViewModel.event.value!!.group_key = "_default"
                        eventService.fireEvent(eventsFragmentViewModel.event.value!!)
                    }
                }
            }

        }

        eventsFragmentViewModel.postingEventStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader(R.raw.lottie_posting_event_3)
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    mainActivityViewModel.switchToModule(0, "home")

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
                    doneDialog.show()
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(status.message.toString())

                }
            }
        }

        eventsFragmentViewModel.fixedLocation.observe(this) { location ->
            eventLocationType = eventsFragmentViewModel.eventLocationType.value ?: "missing"
            if (eventLocationType == EventLocationType.FIXED.name) {
                binding.addressReadOnly.setText(location?.formated_address)
                binding.changeLocation.isVisible =
                    (eventLocationType == EventLocationType.FIXED.name)

            } else binding.addressReadOnly.setText(R.string.at_your_location)
        }

        eventsFragmentViewModel.eventType.observe(this) { eventType ->
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

        eventsFragmentViewModel.eventMediaFlow.observe(this) { mediaList ->
            mediaAdapter.setData(mediaList)
            mediaAdapter.notifyDataSetChanged()
            /*
                  when (media) {
                      is DataAdaptersSupport.AdapterEventMediaFile.ItemAdded -> {
                          val mediaFile = media.data
                          val mediaArray = mediaAdapter.getData()
                          if (!mediaArray.contains(mediaFile)) {
                              mediaArray.add(mediaFile)
                              mediaAdapter.notifyItemInserted(mediaArray.size - 1)
                          }
                      }

                      is DataAdaptersSupport.AdapterEventMediaFile.ItemChanged -> {
                      }

                      is DataAdaptersSupport.AdapterEventMediaFile.ItemMoved -> {

                      }

                      is DataAdaptersSupport.AdapterEventMediaFile.ItemRemoved -> {
                          val mediaFile = media.data
                          val mediaArray = mediaAdapter.getData()
                          val index: Int = mediaArray.indexOf(mediaFile)
                          if (index > -1) {
                              mediaArray.removeAt(index)
                              mediaAdapter.notifyItemRemoved(index)
                          }
                      }
                  }

                 */
        }

        mainActivityViewModel.newMedia.observe(this) { media ->
            if (media != null) {
                when (media.media_type) {
                    MediaTypesEnum.IMAGE -> {
                        eventsFragmentViewModel.onImageAdded(media.file_name)
                    }

                    MediaTypesEnum.VIDEO -> {
                        eventsFragmentViewModel.onVideoAdded(media.file_name, media.duration)
                    }

                    MediaTypesEnum.AUDIO -> {
                        eventsFragmentViewModel.onAudioAdded(media.file_name, media.duration)
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

    private fun removeObservers() {
        eventsFragmentViewModel.showNotificationListSelector.removeObservers(this)
        eventsFragmentViewModel.postingEventStatus.removeObservers(this)
        eventsFragmentViewModel.fixedLocation.removeObservers(this)
        eventsFragmentViewModel.eventType.removeObservers(this)
        eventsFragmentViewModel.eventMediaFlow.removeObservers(this)
        mainActivityViewModel.newMedia.removeObservers(this)
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
                eventLocation!!.longitude = place.latLng.longitude
                eventLocation!!.formated_address = place.address

                eventsFragmentViewModel.onFixedLocation(eventLocation)
                requireContext().hideKeyboard(requireView())

            }
        }
    }

    override fun onPublishEventDone(event: Event?) {
        callback.onPublishEventDone(event)
    }


    fun newInstance(
        thisEvent: Event, callback: OnPostFragmentInteractionCallback
    ): EventAdditionalMediaFragment {
        val fragment = EventAdditionalMediaFragment(
            callback, eventsFragmentViewModel, mainActivityViewModel
        )
        val args = Bundle()
        //     args.putString(ARG_PARAM1, param1)
        //     args.putString(ARG_PARAM2, param2)
        fragment.arguments = args
        return fragment
    }


    companion object {
        // TODO: Rename parameter arguments, choose names that match
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
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

            eventsFragmentViewModel.onAudioAdded(recordingFilename!!, duration)

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
                    eventsFragmentViewModel.removeMedia(mediaFile)
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