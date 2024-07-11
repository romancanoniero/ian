package com.iyr.ian.ui.chat


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iyr.ian.AppConstants.Companion.CHAT_FILES_STORAGE_PATH
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.MediaPickersInterface
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.SpeedMessage
import com.iyr.ian.dao.models.SpeedMessageActions
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.databinding.FragmentBottomSheetMessagesBinding
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.chat.adapters.SpeedMessagesAdapter
import com.iyr.ian.ui.chat.adapters.SpeedMessagesCallback
import com.iyr.ian.ui.map.event_header.EventHeaderFragment
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_ACTION
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_VIDEO
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_VOICE
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Author
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.viewholders.actions.CustomIncomingActionMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.actions.CustomOutcomingActionMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.image.CustomIncomingImageMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.image.CustomOutcomingImageMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.text.CustomIncomingTextMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.text.CustomOutcomingTextMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.video.CustomIncomingVideoMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.video.CustomOutcomingVideoMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.voice.CustomIncomingVoiceMessageViewHolder
import com.iyr.ian.utils.chat.viewholders.voice.CustomOutcomingVoiceMessageViewHolder
import com.iyr.ian.utils.copyFile
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.createDirectoryStructure
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.permissionsReadWrite
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageHolders.ContentChecker
import com.stfalcon.chatkit.messages.MessageInput.AttachmentsListener
import com.stfalcon.chatkit.messages.MessageInput.InputListener
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.visualizer.amplitude.AudioRecordView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.UUID


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


enum class ChatWindowStatus {
    CLOSED, NORMAL, FULLSCREEN
}

interface MessagesFragmentInterface {

    fun hideChatFragment()

}

/**
 * A simple [Fragment] subclass.
 * Use the [MessagesInEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


enum class ChatFragmentKeyboardStatus {
    OPEN, CLOSED
}

class MessagesInEventFragment(
    context: Context,
    val mainActivityViewModel: MainActivityViewModel,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel,
    val viewModel: MessagesInEventFragmentViewModel
) : Fragment(), DialogInterface.OnClickListener, ContentChecker<Message>, SpeedMessagesCallback,
    MediaPickersInterface {


    fun MessagesInEventFragment() {

    }

    private var opennerButton: FloatingActionButton? = null
    private var mainParentView: ConstraintLayout? = null


    private var destinationFolder = ""


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


                            var mediaFile =
                                prepareMediaMessage(MediaTypesEnum.IMAGE, fileName, localPath)

                            if (mediaFile is MediaFile) {

                                viewModel.onNewMediaMessage(
                                    mainActivityViewModel.user.value!!, mediaFile
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


                            var mediaFile =
                                prepareMediaMessage(MediaTypesEnum.VIDEO, fileName, localPath)



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

                                    Log.d("VIDEO_FILE", "voy a llamar a onSendMediaMessage")

                                    viewModel.onNewMediaMessage(
                                        mainActivityViewModel.user.value!!, mediaFile
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


    /**
     * Prepara un objeto de tipo multimedia
     * @param mediaType Tipo de multimedia
     * @param fileName Nombre del archivo
     * @param localFullPath Ruta local del archivo sin el nombre de archivo.
     */

    private fun prepareMediaMessage(
        mediaType: MediaTypesEnum, fileName: String, localFullPath: String
    ): Any {

        if (fileName.compareTo(fileName.getJustFileName()) != 0) {
            throw Exception("El parametro fileName debe contener solo el nombre del archivo")
        }/*
                if (localFullPath.substringBeforeLast("/").contains(fileName)) {
                    throw Exception("El parametro localFullPath no debe contener el nombre del archivo")
                }
        */

        try {

            val media = MediaFile(mediaType, localFullPath, fileName.toString())


            if (mediaType == MediaTypesEnum.VIDEO || mediaType == MediaTypesEnum.AUDIO || mediaType == MediaTypesEnum.IMAGE) {
                val fileExtension = media.file_name.getFileExtension(requireContext())
                var fileUri = media.localFullPath
                if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                        Locale.getDefault()
                    ) == "png"
                ) {
                    fileUri = "file:" + media.localFullPath
                }
                var mediaFileEncoded: String? = null
                if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                        Locale.getDefault()
                    ) == "png" || fileExtension?.lowercase(Locale.getDefault()) == "mp4" || fileExtension?.lowercase(
                        Locale.getDefault()
                    ) == "3gp"
                ) {


                    mediaFileEncoded = MultimediaUtils(requireContext()).convertFileToBase64(
                        Uri.parse(
                            fileUri
                        )
                    ).toString()




                    if (!localFullPath.contains(requireContext().cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH + chatroomKey.toString())) {
                        var destination =
                            requireContext().cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH + chatroomKey
                        FileUtils().copyFile(
                            media.localFullPath.substringBeforeLast("/"),
                            media.localFullPath.getJustFileName(),
                            destination
                        )
                    }
                }
                media.bytesB64 = mediaFileEncoded
            }

            return media
        } catch (ex: Exception) {
            return ex
        }

    }


    private lateinit var imagePickerStartForResult: ActivityResultLauncher<Intent>
    private lateinit var speedMessagesAdapter: SpeedMessagesAdapter
    private var referenceView: View? = null
    private var popupView: View? = null
    private var chatroomKey: String? = null
    private var recordSession: MediaRecorder? = null
    private var recordingFilename: String? = null

    //    private val timelineAdapter: TimeLineAdapter by lazy { TimeLineAdapter(context) }
    private var adapter: MessagesListAdapter<Message>? = null

    // private var chatMessages = ArrayList<Message>()
    lateinit var eventData: Event

    lateinit var binding: FragmentBottomSheetMessagesBinding


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    init {
        initAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        speedMessagesAdapter = SpeedMessagesAdapter(requireActivity(), this)
        setupSpeedMessages()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetMessagesBinding.inflate(layoutInflater, container, false)

        setupUI()
        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        @JvmStatic
        fun newInstance(
            event: Event,
            mapSituationFragmentViewModel: MapSituationFragmentViewModel,
            param1: String,
            param2: String
        ) = EventHeaderFragment(
            this as EventHeaderCallback, event, mapSituationFragmentViewModel
        ).apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // viewModel.setEventKey(mapSituationFragmentViewModel.currentEventKey.value)
        binding.messageInputFix.inputEditText.isEnabled = true
        //  AppClass.instance.addViewToStack(IANModulesEnum.CHAT, this, eventKey)

    }

    override fun onPause() {
        super.onPause()
        binding.messageInputFix.inputEditText.isEnabled = false
        //     AppClass.instance.removeViewFromStack(this)
    }


    private var internalMessagesList: ArrayList<Message> = ArrayList()

    private fun setupObservers() {

        mapSituationFragmentViewModel.currentEventKey.observe(this) { resource ->

            when (resource) {
                is Resource.Success -> {
                    viewModel.onConnectToEvent(resource.data!!.toString())
                    //   mapSituationFragmentViewModel.getMessages()
                }

                is Resource.Error -> {
                    requireActivity().showErrorDialog(resource.message.toString())
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                null -> {
                    // no hago nada
                }
            }

        }

        mainActivityViewModel.newMedia.observe(this) { media ->
            if (media != null) {
                when (media.media_type) {
                    MediaTypesEnum.IMAGE -> {
                        //      eventsFragmentViewModel.onImageAdded(media.file_name)
                    }

                    MediaTypesEnum.VIDEO -> {
                        //    eventsFragmentViewModel.onVideoAdded(media.file_name, media.duration)
                    }

                    MediaTypesEnum.AUDIO -> {
                        //  eventsFragmentViewModel.onAudioAdded(media.file_name, media.duration)
                    }

                    MediaTypesEnum.TEXT -> TODO()
                }
                mainActivityViewModel.resetNewMedia()
            }
        }

        mainActivityViewModel.isKeyboardOpen.observe(this) { isKeyboardOpen ->
            val status = mapSituationFragmentViewModel.messageFragmentMode.value
            if (mainActivityViewModel.isKeyboardOpen() != isKeyboardOpen) {
                viewModel.onKeyboardStateChange(isKeyboardOpen, status)
            }
        }

        viewModel.isMaximizeButtonVisible.observe(this) { isVisible ->
            when (isVisible) {
                null -> {}
                true -> binding.fullScreenButton.visibility = View.VISIBLE
                false -> binding.fullScreenButton.visibility = View.GONE
            }
        }


        viewModel.messagePreview.observe(this) { event ->
            // Agrego el mensaje al chat

            Log.d("VIDEO_FILE", "Observe en messagePreview")
            handleMessageEvent(event)
            // ahora termino de preparar el mensaje para enviarlo

            if (event is ChatRepository.ChatDataEvent.OnChildAdded) {
                lifecycleScope.launch(Dispatchers.IO) {
                    var compressedBase64: String? = null
                    var message: Message = event.data
                    var fileLocation: String? = null
                    if (message.image != null) {
                        fileLocation = message.image.url
                    } else if (message.video != null) {
                        fileLocation = message.video.url
                    } else if (message.voice != null) {
                        fileLocation = message.voice.url

                    }
                }
            }
            // Agrego el mensaje al chat
        }

        viewModel.storingMessage.observe(this) { _message ->

            var messages = viewModel.chatMessages

            lifecycleScope.launch(Dispatchers.Main) {
                when (_message) {
                    is Resource.Loading -> {
                        mapSituationFragmentViewModel.getMessages().forEach { message ->
                            if (message.id == _message.data) {
                                message.status = MessagesStatus.SENDING
                                adapter?.update(message)
                                return@forEach
                            }
                        }

                    }

                    is Resource.Error -> {

                        Toast.makeText(
                            requireContext(), _message.message.toString(), Toast.LENGTH_SHORT
                        ).show()
                        adapter?.deleteById(_message.data.toString())
                    }

                    is Resource.Success -> {


                        var index = -1
                        mapSituationFragmentViewModel.getMessages().forEach { message ->
                            index++
                            if (message.id == _message.data) {
                                message.status = MessagesStatus.SENT
                                adapter?.update(message)
                                adapter?.notifyItemChanged(index)
                                return@forEach
                            }
                        }

                    }
                }

            }


        }


        mapSituationFragmentViewModel.messageIncomming.observe(this) { resource ->
            var chatEvent = resource.data
            handleMessageEvent(chatEvent)
        }
    }


    fun clearMessages() {
        internalMessagesList.clear()
        adapter?.clear()
    }

    private fun removeObservers() {
        viewModel.chatRoomFlow.removeObservers(this)
        viewModel.messagePreview.removeObservers(this)
        mainActivityViewModel.isKeyboardOpen.removeObservers(this)
        mapSituationFragmentViewModel.messageIncomming.removeObservers(this)
        viewModel.storingMessage.removeObservers(this)
        viewModel.isMaximizeButtonVisible.removeObservers(this)
    }


    private fun handleMessageEvent(chatEvent: ChatRepository.ChatDataEvent?) {
        if (this.chatroomKey == null) {
            requireActivity().showErrorDialog(
                "Error de Programacion", "EventKey no ha sido establecida"
            )
            return
        }


        when (chatEvent) {
            is ChatRepository.ChatDataEvent.OnChildAdded -> {
                val _message = chatEvent.data

                if (_message.video != null) {
                    _message.video.url =
                        CHAT_FILES_STORAGE_PATH + chatroomKey + "/" + _message.video.url.getJustFileName()
                } else if (_message.image != null) {
                    _message.image.url =
                        CHAT_FILES_STORAGE_PATH + chatroomKey + "/" + _message.image.url.getJustFileName()
                } else if (_message.voice != null) {
                    _message.voice.url =
                        CHAT_FILES_STORAGE_PATH + chatroomKey + "/" + _message.voice.url.getJustFileName()
                }


                if (!internalMessagesList.contains(_message)) {

                    internalMessagesList.add(_message)

                    var addMessage = true
                    if (_message.action != null) {
                        if (_message.user.id.compareTo(FirebaseAuth.getInstance().uid.toString()) == 0) {
                            addMessage = false
                        }
                    }
                    if (addMessage) {
                        if (mapSituationFragmentViewModel.isChatOpen.value ?: false) {
                            var eventKey =
                                mapSituationFragmentViewModel.currentEventKey.value?.data.toString()
                            mainActivityViewModel.onMessageRead(eventKey, _message.id)
                        }
                        lifecycleScope.launch(Dispatchers.Main) {
                            adapter?.addToStart(_message, true)
                        }
                    }
                } else {

                    lifecycleScope.launch(Dispatchers.Main) {
                        adapter?.update(_message)
                    }
                }

            }

            is ChatRepository.ChatDataEvent.OnChildChanged -> {

                val _message = chatEvent.data
                lifecycleScope.launch(Dispatchers.Main) {
                    adapter?.update(_message)
                }
            }

            is ChatRepository.ChatDataEvent.OnChildMoved -> TODO()
            is ChatRepository.ChatDataEvent.OnChildRemoved -> TODO()
            is ChatRepository.ChatDataEvent.OnError -> TODO()
            null -> {

            }
        }
    }


    override fun onStop() {
        super.onStop()
        removeObservers()
    }

    private fun setupUI() {

        binding.messagesList.setAdapter(adapter)

        binding.messagesList.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }

        binding.recyclerActions.adapter = speedMessagesAdapter
        binding.recyclerActions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // binding.messageInputFix.inputEditText.setOnFocusChangeListener { v, hasFocus ->
        // Configuro el comportamiento del teclado
        binding.messageInputFix.inputEditText.setSingleLine()
        binding.messageInputFix.inputEditText.imeOptions = EditorInfo.IME_ACTION_SEND
        binding.messageInputFix.inputEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // Realiza tu acción aquí
                binding.messageInputFix.button.performClick()
                true
            } else {
                false
            }
        }

        //   }

        binding.messageInputFix.setInputListener(InputListener { textMessage ->
            requireActivity().hideKeyboard()
            val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
            viewModel.onNewMessage(
                me, textMessage.toString()
            )
            return@InputListener true
        })

        binding.messageInputFix.setAttachmentsListener(AttachmentsListener {
            showAtachmentsPopupWindow(binding.messageInputFix, requireActivity())
        })

    }

    private fun setupSpeedMessages() {

        val speedMessagesList = ArrayList<SpeedMessage>()
        speedMessagesList.add(
            SpeedMessage(
                "im going",
                SpeedMessageActions.GOING,
                R.string.im_going,
                R.string.im_going_message,
                SpeedMessageActions.NOT_GOING,
                R.string.im_not_going,
                R.string.im_not_going_message
            )
        )
        speedMessagesList.add(
            SpeedMessage(
                "already called",
                SpeedMessageActions.CALLED,
                R.string.already_called,
                R.string.already_called_message,
                SpeedMessageActions.NOT_CALLED,
                R.string.not_called,
                R.string.not_called_message
            )
        )
        speedMessagesList.add(
            SpeedMessage(
                "im there",
                SpeedMessageActions.IM_THERE,
                R.string.im_in_place,
                R.string.im_in_place_message,
                SpeedMessageActions.NOT_IN_THERE,
                R.string.not_in_place,
                R.string.not_in_place_message
            )
        )

        speedMessagesAdapter.setData(speedMessagesList)
    }

    fun prepareMessage(): Message {
        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        val user = Author(
            SessionForProfile.getInstance(requireContext()).getUserId(),
            me.display_name,
            SessionForProfile.getInstance(requireContext()).getUserId() + "/" + me.image.file_name,
            false
        )
        val messageKey = "epepepe"
        return Message(messageKey, user)
    }

    private fun initAdapter() {
        //We can pass any data to ViewHolder with payload
        val payloadForTextMessage: CustomIncomingTextMessageViewHolder.Payload =
            CustomIncomingTextMessageViewHolder.Payload()
        val payloadForIncomingVoiceMessage: CustomIncomingVoiceMessageViewHolder.Payload =
            CustomIncomingVoiceMessageViewHolder.Payload()
        val payloadForOutcomingVoiceMessage: CustomOutcomingVoiceMessageViewHolder.Payload =
            CustomOutcomingVoiceMessageViewHolder.Payload()
        val payloadForIncomingVideoMessage: CustomIncomingVideoMessageViewHolder.Payload =
            CustomIncomingVideoMessageViewHolder.Payload()
        val payloadForOutcomingVideoMessage: CustomOutcomingVideoMessageViewHolder.Payload =
            CustomOutcomingVideoMessageViewHolder.Payload()
        val payloadForIncomingActionMessage: CustomIncomingActionMessageViewHolder.Payload =
            CustomIncomingActionMessageViewHolder.Payload()
        val payloadForOutcomingActionMessage: CustomOutcomingActionMessageViewHolder.Payload =
            CustomOutcomingActionMessageViewHolder.Payload()

        payloadForTextMessage.avatarClickListener =
            CustomIncomingTextMessageViewHolder.OnAvatarClickListener {
                Toast.makeText(
                    requireContext(), "Text message avatar clicked", Toast.LENGTH_SHORT
                ).show()
            }


        payloadForIncomingVoiceMessage.clicksListener =
            object : CustomIncomingVoiceMessageViewHolder.OnClicksListener {

                override fun onPlayButtonClick(
                    url: String?, mpInterface: IMultimediaPlayer?, callback: OnCompleteCallback?
                ) {
                    //     TODO("Not yet implemented")
                    /*
                                        var filePath: String =
                                             CHAT_FILES_STORAGE_PATH +
                                                    eventKey + "/" + url
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            var localFileLocation =FirebaseStorageUtils().getStorageObject(filePath.toString(), url)
                                            MultimediaUtils.getInstance(requireContext())
                                                ?.playSound(requireContext(), localFileLocation.toString())
                                        }
                    *//*
                    FirebaseStorageUtils().getStorageObject(
                        url!!,
                        "",
                        object : DownloadFileListener {
                            override fun onDownloadStatusChange(
                                referenceTag: String?,
                                downloadStatus: MediaFileDownloadStatus,
                                locatUri: Uri?
                            ) {
                                if (downloadStatus == MediaFileDownloadStatus.READY) {
                                    //AppClass.instance.getCurrentMediaPlayer()
//                                    MultimediaUtils.instance?.playSound(requireContext(),locatUri.toString(), mpInterface)
                                    MultimediaUtils.getInstance(requireContext())
                                        ?.playSound(requireContext(), locatUri.toString())
//                                    MultimediaUtils.playSound(locatUri.toString(), mpInterface)
                                }
                            }

                            override fun onError(exception: Exception) {
                                onError(exception)
                            }
                        })

                    */
                }

                override fun onAvatarClick() {
                    //                    TODO("Not yet implemented")
                    Toast.makeText(requireContext(), "onAvatarClick()", Toast.LENGTH_LONG).show()
                }

            }

        payloadForOutcomingVoiceMessage.clicksListener =
            object : CustomOutcomingVoiceMessageViewHolder.OnClicksListener {
                override fun onPlayButtonClick(
                    url: String?, mpInterface: IMultimediaPlayer?, callback: OnCompleteCallback?
                ) {

                    /*
                                        FirebaseStorageUtils().getStorageObject(
                                            url!!,
                                            "",
                                            object : DownloadFileListener {
                                                override fun onDownloadStatusChange(
                                                    referenceTag: String?,
                                                    downloadStatus: MediaFileDownloadStatus,
                                                    locatUri: Uri?
                                                ) {
                                                    if (downloadStatus == MediaFileDownloadStatus.READY) {
                                                        //AppClass.instance.getCurrentMediaPlayer()
                                                        MultimediaUtils.getInstance(requireContext())
                                                            ?.playSound(requireContext(), locatUri.toString())


                                                        //MultimediaUtils.playSound(locatUri.toString(), mpInterface)
                                                    }
                                                }

                                                override fun onError(exception: Exception) {
                                                    onError(exception)
                                                }
                                            })

                     */

                    /*
                                      var filePath: String =
                                          CHAT_FILES_STORAGE_PATH +
                                                  eventKey + "/" + url
                                      lifecycleScope.launch(Dispatchers.IO) {
                                          var localFileLocation =FirebaseStorageUtils().getStorageObject(filePath.toString(), url)
                                          MultimediaUtils.getInstance(requireContext())
                                              ?.playSound(requireContext(), localFileLocation.toString())
                                      }
                  */
                }

            }


        val imageLoader: ImageLoader = object : ImageLoader {
            override fun loadImage(imageView: ImageView?, url: String?, payload: Any?) {

                val requestOptions = RequestOptions()
                requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                val storageReference: Any? = null
                when (resources.getResourceName(imageView!!.id).substringAfter("/")) {
                    "messageUserAvatar" -> {
                        val filePath = PROFILE_IMAGES_STORAGE_PATH + url

                        lifecycleScope.launch {

                            Log.d("GLIDEAPP", "6")


                            var finalPath = FirebaseStorage.getInstance()
                                .getReference(filePath.getJustFileName()).downloadUrlWithCache(
                                    AppClass.instance, filePath.substringBeforeLast("/")
                                )

                            GlideApp.with(requireContext()).setDefaultRequestOptions(requestOptions)
                                .load(finalPath)
                                .error(requireActivity().getDrawable(R.drawable.ic_error))
                                .into(imageView)

                        }
                    }

                    "image" -> {

                        var justFileName = url?.getJustFileName()

                        val filePath = "$CHAT_FILES_STORAGE_PATH$chatroomKey/$justFileName"
//                        val localPath = "$CHAT_FILES_STORAGE_PATH$eventKey"
                        //  lifecycleScope.launch(Dispatchers.IO) {

                        lifecycleScope.launch(Dispatchers.IO) {


                            var finalPath = FirebaseStorage.getInstance()
                                .getReference(filePath.getJustFileName()).downloadUrlWithCache(
                                    AppClass.instance, filePath.substringBeforeLast("/")
                                )
                            // TODO: Pasarlo a Coroutina

//                            var localPath2 = FirebaseStorageUtils().getStorageObject(filePath, "")

                            Log.d("GLIDEAPP", "7")

                            withContext(Dispatchers.Main) {
                                GlideApp.with(requireContext())
                                    .setDefaultRequestOptions(requestOptions).load(finalPath)
                                    .error(requireActivity().getDrawable(R.drawable.ic_error))
                                    .into(imageView)

                            }
                        }


                    }
                }

                Log.d("IMAGE_LOADER", storageReference.toString())
            }
        }

        val holdersConfig = MessageHolders().setIncomingTextConfig(
            CustomIncomingTextMessageViewHolder::class.java,
            R.layout.item_custom_incoming_text_message,
            payloadForTextMessage
        ).setOutcomingTextConfig(
            CustomOutcomingTextMessageViewHolder::class.java,
            R.layout.item_custom_outcoming_text_message,
            payloadForTextMessage
        ).setIncomingImageConfig(
            CustomIncomingImageMessageViewHolder::class.java,
            R.layout.item_custom_incoming_image_message
        ).setOutcomingImageConfig(
            CustomOutcomingImageMessageViewHolder::class.java,
            R.layout.item_custom_outcoming_image_message
        )


// ojo que falla y esta pesta la misma clase para ambos
        holdersConfig.registerContentType(
            CONTENT_TYPE_VOICE,
            CustomIncomingVoiceMessageViewHolder::class.java,
            payloadForIncomingVoiceMessage,
            R.layout.item_custom_incoming_voice_message,
            CustomOutcomingVoiceMessageViewHolder::class.java,
            payloadForOutcomingVoiceMessage,
            R.layout.item_custom_outcoming_voice_message,
            this
        )

        holdersConfig.registerContentType(
            CONTENT_TYPE_VIDEO,
            CustomIncomingVideoMessageViewHolder::class.java,
            payloadForIncomingVideoMessage,
            R.layout.item_custom_incoming_video_message,
            CustomOutcomingVideoMessageViewHolder::class.java,
            payloadForOutcomingVideoMessage,
            R.layout.item_custom_outcoming_video_message,
            this
        )


        holdersConfig.registerContentType(
            CONTENT_TYPE_ACTION,
            CustomIncomingActionMessageViewHolder::class.java,
            payloadForIncomingActionMessage,
            R.layout.item_custom_incoming_action_message,
            CustomOutcomingActionMessageViewHolder::class.java,
            payloadForOutcomingActionMessage,
            R.layout.item_custom_outcoming_action_message,
            this
        )



        adapter = MessagesListAdapter<Message>(
            SessionForProfile.getInstance(AppClass.instance).getUserId(), holdersConfig, imageLoader
        )

    }


    override fun onClick(p0: DialogInterface?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun hasContentFor(message: Message?, type: Byte): Boolean {
        when (type) {
            CONTENT_TYPE_VOICE -> {
                return message!!.voice != null && message.voice.url != null && message.voice.url.isNotEmpty()
            }

            CONTENT_TYPE_VIDEO -> {
                return message!!.video != null && message.video.url != null && message.video.url.isNotEmpty()
            }

            CONTENT_TYPE_ACTION -> {
                return message!!.action != null
            }
        }
        return false
    }

    fun setData(event: Event) {
        if (!::eventData.isInitialized || eventData.event_key != event.event_key) {
            eventData = event
            setChatroomKey(eventData.event_key)
        }
    }


    private fun removeMessage(index: Int, message: Message) {
        mapSituationFragmentViewModel.getMessages().removeAt(index)
        adapter?.delete(message)
    }

    private fun updateMessage(index: Int, message: Message) {
        mapSituationFragmentViewModel.getMessages()[index] = message
        adapter?.update(message)
    }


    fun setChatroomKey(eventKey: String) {
        if (this.chatroomKey != null) {
            if (this.chatroomKey != eventKey) {
                //   unsubscribe()
                viewModel.setEventKey(eventKey)
                adapter?.clear()
                mapSituationFragmentViewModel.getMessages().clear()
                internalMessagesList.clear()
            }
        }
        this.chatroomKey = eventKey
        viewModel.setEventKey(eventKey)
    }

    fun getChatroomKey(): String? {
        return this.chatroomKey
    }


    private fun showAtachmentsPopupWindow(
        view: View, activity: Activity
    ) { //Create a View object yourself through inflater
        val inflater =
            view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.context_popup_attachment, null)
        popupView?.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val attachmentButtonLocation = IntArray(2)
        view.getLocationOnScreen(attachmentButtonLocation)
        val x: Int = popupView?.measuredWidth as Int

        val popupHeight: Int = popupView?.measuredHeightAndState as Int
        //Specify the length and width through constants
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        //Make Inactive Items Outside Of PopupWindow
        configureContextMenu(popupView!!)
        val popupWindow = PopupWindow(popupView, width, height, true)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //val xAxis = displayWidth / 4 - displayWidth / 10
        val xAxis = attachmentButtonLocation[0]
        val yAxis = attachmentButtonLocation[1] - popupHeight //76.px
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(
            view, android.view.Gravity.NO_GRAVITY, xAxis, yAxis
        )
        popupWindow.dimBehind()

        popupView?.setOnClickListener {
            requireContext().handleTouch()
            popupWindow.dismiss()
        }
    }

    private fun configureContextMenu(popupView: View) {

        popupView.findViewById<LinearLayout>(R.id.attachment_photo_option).setOnClickListener {
            popupView.performClick()
            requireActivity().permissionsReadWrite()
            toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        popupView.findViewById<LinearLayout>(R.id.attachment_image_option).setOnClickListener {
            popupView.performClick()
            requireActivity().permissionsReadWrite()
            toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }


        popupView.findViewById<LinearLayout>(R.id.attachment_audio_option)
            .setOnTouchListener { view, motionEvent ->
                recordingManagement(motionEvent)
                return@setOnTouchListener true
            }


        popupView.findViewById<LinearLayout>(R.id.attachment_video_option).setOnClickListener {
            popupView.performClick()
            if (Build.VERSION.SDK_INT <= 32) {
                toTakeVideoPermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                toTakeVideoPermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO))
            }
        }
    }

    private fun startMonitoringWave() {
        var iconRecorder = popupView?.findViewById<ImageView>(R.id.icon_audio)
        var iconRecordView: AudioRecordView =
            popupView?.findViewById<AudioRecordView>(R.id.audioRecordView)!!
        var captionRecorder: TextView = popupView?.findViewById<TextView>(R.id.caption_record)!!
        captionRecorder.text = requireContext().getString(R.string.recording)
        iconRecordView.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            while (iconRecordView.visibility == View.VISIBLE) {
                val currentMaxAmplitude = recordSession?.maxAmplitude ?: 0
                Log.d("WAVE", currentMaxAmplitude.toString())
                iconRecordView.update(currentMaxAmplitude)   //redraw view
                delay(100)
            }
        }
    }

    private fun stopMonitoringWave() {

        var iconRecorder: ImageView = popupView?.findViewById<ImageView>(R.id.icon_audio)!!
        var captionRecorder: TextView = popupView?.findViewById<TextView>(R.id.caption_record)!!
        var iconRecordView: AudioRecordView =
            popupView?.findViewById<AudioRecordView>(R.id.audioRecordView)!!


        captionRecorder.text = requireContext().getText(R.string.voice_message)
        iconRecorder.setImageDrawable(requireContext().getDrawable(R.drawable.ic_audio_mic_outline))
        iconRecordView.visibility = View.INVISIBLE
        iconRecordView.recreate()
    }


    private fun recordingManagement(motionEvent: MotionEvent): Boolean {
        Log.d("RECORDING ", " action = " + motionEvent.action.toString())
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("RECORDING ", "START")

                recordingFilename =
                    AppClass.instance.cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH + chatroomKey + "/" + UUID.randomUUID()
                        .toString() + ".3gp"/*
                                val directory = File(recordingFilename)
                                if (!directory.exists()) {
                                    directory.mkdir()
                                    // If you require it to make the entire directory path including parents,
                                    // use directory.mkdirs(); here instead.
                                }
                */


                //------------------------------------------

                Log.d("AUDIO_FILE", "1-" + recordingFilename!!)

                requireContext().createDirectoryStructure(recordingFilename!!)

                recordSession = MultimediaUtils(requireContext()).startRecording(
                    requireActivity(), recordingFilename!!
                )!!

                startMonitoringWave()

                var iconBitmap = requireContext().getBitmapFromVectorDrawable(
                    R.drawable.ic_recording
                )

                var iconRecorder: ImageView = popupView?.findViewById<ImageView>(R.id.icon_audio)!!
                iconRecorder.setImageBitmap(iconBitmap)
//                    recordButton.setImageBitmap(iconBitmap)
                requireActivity().playSound(R.raw.recording_start, null, null)
                //   }

            }

            MotionEvent.ACTION_UP -> {
                Log.d("RECORDING ", "END")
                try {
                    popupView?.performClick()
                    requireActivity().playSound(R.raw.recording_stop, null, null)

                    var iconBitmap = requireContext().getBitmapFromVectorDrawable(
                        R.drawable.ic_microphone
                    )
                    //                  recordButton.setImageBitmap(iconBitmap)

                    //Do Nothing
                    MultimediaUtils(requireContext()).stopRecording(
                        recordSession, recordingFilename!!
                    )
                    //                   Toast.makeText(context, "Recorded.!", Toast.LENGTH_SHORT).show()


                    // Local Message
                    //  val message = prepareMessage()
                    /*
                                      val finalPath = requireContext().appsInternalStorageFolder().toString()

                                      requireContext().copyFile(
                                          recordingFilename!!.substringBeforeLast("/"),
                                          recordingFilename!!.getJustFileName(),
                                          finalPath
                                      )
                  *//*
                    val voice = Message.Voice(
                        FileUtils.getJustFileName(recordingFilename!!),
                        mediaFile.duration
                    )
                    message.voice = voice
                    addMessageToStart(message)
                   */
                    //-----------------

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

                        val mediaFile = prepareMediaMessage(
                            MediaTypesEnum.AUDIO, fileName, recordingFilename.toString()
                        )


                        when (mediaFile) {
                            is MediaFile -> {
                                mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                                mediaFile.time = Date().time

                                lifecycleScope.launch(Dispatchers.IO) {

                                    viewModel.onNewMediaMessage(
                                        mainActivityViewModel.user.value!!, mediaFile
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
                    //viewModel.onNewMediaMessage(me, mediaFile)

                    /*
                                      ChatWSClient.instance.messageFileSend(
                                          eventKey!!,
                                          message.id,
                                          mediaFile,
                                          callback
                                      )
                  */


                } catch (ex: Exception) {
                    recordSession = null
                }
            }
        }
        return false
    }


    private fun PopupWindow.dimBehind() {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
    }


    fun getMessagesAdapter(): MessagesListAdapter<Message>? {
        return adapter
    }

    fun addMessageToStart(message: Message) {
        adapter?.addToStart(message, true)
    }


    fun setViewReference(view: View) {
        this.referenceView = view
    }


    override fun onSpeedMessageClick(speedMessage: SpeedMessage) {
        val message = prepareMessage()
        message.action = speedMessage
        sendSpeedMessage(message)
    }


    private fun sendTextMessage(message: Message): Boolean {
        addMessageToStart(message)
        return true
    }


    private fun sendSpeedMessage(message: Message): Boolean {

        viewModel.onNewSpeedMessage(
            message
        )
        return true
    }


    override fun onImageSelected(intent: Intent) {

        val intent = intent.data
        val filePath = Uri.parse(intent?.encodedPath).toString()
        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.IMAGE
        mediaFile.file_name = filePath
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time
        // Agrego Mensaje Local-----------------------
        val message = prepareMessage()
        val finalPath = requireContext().getDir(
            "cache", Context.MODE_PRIVATE
        ).toString()
        requireContext().copyFile(
            filePath.substringBeforeLast("/"), filePath.getJustFileName(), finalPath
        )
        val image = Message.Image(filePath.getJustFileName())
        message.image = image
        addMessageToStart(message)
        adapter?.notifyItemInserted(adapter?.messagesCount ?: 0 - 1)
        Log.d("MEDIA_MESSAGE_SEND", Gson().toJson(message))

        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.onNewMediaMessage(me, mediaFile)
        }
    }

    override fun onVideoRecorded(intent: Intent) {

        val intent = intent.data!!

        val originalPath = intent.toString()

        val finalPath = requireContext().getDir(
            "cache", Context.MODE_PRIVATE
        ).toString()

        FileUtils().moveFile(
            originalPath.substringBeforeLast("/"),
            Uri.parse(intent.encodedPath).lastPathSegment!!,
            finalPath
        )

        val filePath = finalPath + "/" + Uri.parse(intent.encodedPath).lastPathSegment!!

        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.VIDEO
        mediaFile.file_name = filePath

        mediaFile.duration =
            MultimediaUtils(requireContext()).getDuration(Uri.parse("file:$filePath"))
        val dimensionsMap =
            MultimediaUtils(requireContext()).getDimentions(Uri.parse("file:$filePath"))

        mediaFile.width = dimensionsMap["width"]
        mediaFile.height = dimensionsMap["height"]
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time

        // Local Message
        val message = prepareMessage()
        val video = Message.Video(
            filePath.getJustFileName(), mediaFile.duration, mediaFile.width, mediaFile.height
        )
        message.video = video
        addMessageToStart(message)
        adapter?.notifyItemInserted(adapter?.messagesCount ?: 0 - 1)

        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.onNewMediaMessage(me, mediaFile)
        }
    }

    fun onMessageAdded(message: Message) {
        Log.d("FLOW_ADAPTER", "Entro el mensaje  = $message")
        adapter?.addToStart(message, true)
    }

    fun connectToEvent(eventKey: String) {
        Log.d("FLOW_CONNECT_MESSAGES_IN_EVENT", "Connecting to event {$eventKey}")
        //  adapter?.clear()
        //  this.eventKey = eventKey
        this.chatroomKey = eventKey
        viewModel.onConnectToEvent(eventKey)
    }


    var messagesDisplayRulesDefault: ConstraintLayout.LayoutParams? = null


    /**
     * Maximiza el fragmento y anima los botones
     */
    private fun animateButtonsForFullScreen() {
        lifecycleScope.launch(Dispatchers.Main) {
            parentFragment?.let { parent ->
                val aditionalFragmentsLayout: FrameLayout = parent

                // Obtén la instancia del ConstraintLayout padre
                val parentConstraintLayout: ConstraintLayout = mainParentView!!
                // Configura el ancho y alto del FrameLayout para que ocupe todo el ancho y alto disponible
                val layoutParams =
                    aditionalFragmentsLayout.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                aditionalFragmentsLayout.setPadding(0, 0, 0, 0)
                // Establece el margen inferior deseado (puedes ajustar esto según tus necesidades)
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = -10.dp
                layoutParams.marginEnd = 0
                aditionalFragmentsLayout.layoutParams = layoutParams

                //------- CardView -------------------
                binding.cardView.cardElevation = 0F
                binding.cardView.radius = 0F
                binding.cardView.clipToPadding = false
                binding.cardView.preventCornerOverlap = false
                binding.fullScreenButton.visibility = View.GONE


                var fullScreenButton = binding.fullScreenButton
                var topButtonsSection = binding.topButtonsSection

                // Configurar la animación de desvanecimiento
                val fadeOut = ObjectAnimator.ofFloat(fullScreenButton, "alpha", 1f, 0f)
                fadeOut.duration = 1000 // Duración de la animación en milisegundos

                // Configurar la animación de agrandar
                // Configurar la animación de agrandar
                val scaleX = ObjectAnimator.ofFloat(fullScreenButton, "scaleX", 1f, 1.2f)
                val scaleY = ObjectAnimator.ofFloat(fullScreenButton, "scaleY", 1f, 1.2f)
                scaleX.duration = 1000 // Duración de la animación en milisegundos
                scaleY.duration = 1000 // Duración de la animación en milisegundos

                // Configurar la animación de alineación a la izquierda después de la animación de agrandamiento
                val alignLeft = ObjectAnimator.ofFloat(fullScreenButton, "translationX", 0f, 0f)
                alignLeft.duration = 0 // La animación de alineación es instantánea

                // Crear un conjunto de animaciones
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(fadeOut, scaleX, scaleY, alignLeft)

                // Iniciar la animación al hacer clic en el botón (puedes cambiar este evento según tus necesidades)
                animatorSet.start()

                // Después de la animación, cambiar las restricciones para alinear a la izquierda

                mapSituationFragmentViewModel.setMessageFragmentMode(ChatWindowStatus.FULLSCREEN)


            }
        }
    }

    private var parentFragment: FrameLayout? = null
    fun setParentReference(layout: FrameLayout) {
        this.parentFragment = layout
    }

    fun setMainParentReference(layout: ConstraintLayout) {
        mainParentView = layout
    }

    fun setOpenButtonReference(fab: FloatingActionButton) {
        opennerButton = fab
    }


    fun getFragmentLayoutDefaults(): ConstraintLayout.LayoutParams? {
        return messagesDisplayRulesDefault
    }

    fun setFragmentLayoutDefaults(layoutParams: ConstraintLayout.LayoutParams) {
        messagesDisplayRulesDefault = layoutParams
    }

    fun turnActionInto(currentActionType: SpeedMessageActions, action: SpeedMessageActions) {
        var actionIndex = findActionIndex(currentActionType)
        if (actionIndex > -1) {
            when (currentActionType) {
                SpeedMessageActions.GOING -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "im not going",
                        SpeedMessageActions.NOT_GOING,
                        R.string.im_not_going,
                        R.string.im_not_going_message,
                        SpeedMessageActions.GOING,
                        R.string.im_going,
                        R.string.im_going_message
                    )
                }

                SpeedMessageActions.NOT_GOING -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "im going",
                        SpeedMessageActions.GOING,
                        R.string.im_going,
                        R.string.im_going_message,
                        SpeedMessageActions.NOT_GOING,
                        R.string.im_not_going,
                        R.string.im_not_going_message
                    )
                }

                SpeedMessageActions.CALLED -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "already called",
                        SpeedMessageActions.NOT_CALLED,
                        R.string.not_called,
                        R.string.not_called_message,
                        SpeedMessageActions.CALLED,
                        R.string.already_called,
                        R.string.already_called_message
                    )

                }

                SpeedMessageActions.NOT_CALLED -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "already called",
                        SpeedMessageActions.CALLED,
                        R.string.already_called,
                        R.string.already_called_message,
                        SpeedMessageActions.NOT_CALLED,
                        R.string.not_called,
                        R.string.not_called_message
                    )

                }

                SpeedMessageActions.IM_THERE -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "im there",
                        SpeedMessageActions.NOT_IN_THERE,
                        R.string.not_in_place,
                        R.string.not_in_place_message,
                        SpeedMessageActions.IM_THERE,
                        R.string.im_in_place,
                        R.string.im_in_place_message
                    )

                }

                SpeedMessageActions.NOT_IN_THERE -> {
                    speedMessagesAdapter.getData()[actionIndex] = SpeedMessage(
                        "im there",
                        SpeedMessageActions.IM_THERE,
                        R.string.im_in_place,
                        R.string.im_in_place_message,
                        SpeedMessageActions.NOT_IN_THERE,
                        R.string.not_in_place,
                        R.string.not_in_place_message
                    )

                }
            }
            speedMessagesAdapter.notifyItemChanged(actionIndex)
        }
    }

    private fun findActionIndex(actionType: SpeedMessageActions): Int {
        var index = -1
        speedMessagesAdapter.getData().forEach { action ->
            index++
            if (action.actionType == actionType.toString()) {
                return index
            }
        }


        return -1
    }

    fun disableControls() {
        binding.messageInputFix.inputEditText.clearFocus()
        binding.messageInputFix.inputEditText.isEnabled = false

    }


    fun enableControls() {
        binding.messageInputFix.isEnabled = true
        binding.messageInputFix.inputEditText.isEnabled = true
    }


}


