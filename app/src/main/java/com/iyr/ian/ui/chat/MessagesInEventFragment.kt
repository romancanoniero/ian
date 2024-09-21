package com.iyr.ian.ui.chat


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.iyr.ian.AppConstants.Companion.CHAT_FILES_STORAGE_PATH
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.MediaPickersInterface
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.SpeedMessage
import com.iyr.ian.dao.models.SpeedMessageActions
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.databinding.FragmentBottomSheetMessagesBinding
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.KeyboardStatusEnum
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.chat.adapters.SpeedMessagesAdapter
import com.iyr.ian.ui.chat.adapters.SpeedMessagesCallback
import com.iyr.ian.ui.map.recordingManagement
import com.iyr.ian.ui.map.resizeChatWindow
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.UIUtils.getStatusBarHeight
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_ACTION
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_VIDEO
import com.iyr.ian.utils.chat.ChatContentTypes.CONTENT_TYPE_VOICE
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
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.models.ViewAttributes
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.permissionsReadWrite
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.utils.toMediaFile
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageHolders.ContentChecker
import com.stfalcon.chatkit.messages.MessageInput.InputListener
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale


enum class ChatWindowStatus {
    CLOSED, NORMAL, FULLSCREEN
}


class MessagesInEventFragment : DialogFragment(), ContentChecker<Message>, SpeedMessagesCallback,
    MediaPickersInterface {

    private var recyclerViewState: Parcelable? = null
            internal var standardChatWindowHeight: Int = 0
    private val mainActivityViewModel: MainActivityViewModel by lazy { MainActivityViewModel.getInstance() }
    private val mapSituationFragmentViewModel: MapSituationFragmentViewModel by lazy { MapSituationFragmentViewModel.getInstance() }
    internal val viewModel: MessagesInEventFragmentViewModel by lazy { MessagesInEventFragmentViewModel.getInstance() }

    private val myUserKey: String = UserViewModel.getInstance().getUser()?.user_key ?: ""
    private var opennerButton: FloatingActionButton? = null
    private var mainParentView: ConstraintLayout? = null

    // private lateinit var imagePickerStartForResult: ActivityResultLauncher<Intent>
    private lateinit var speedMessagesAdapter: SpeedMessagesAdapter
    private var referenceView: View? = null
    internal var popupView: View? = null


    private var chatroomKey: String? = null
    var recordSession: MediaRecorder? = null
    var recordingFilename: String? = null

    private var adapter: MessagesListAdapter<Message>? = null

    var eventData: Event = MapSituationFragmentViewModel.getInstance().lastEventUpdate

    lateinit var binding: FragmentBottomSheetMessagesBinding

    val args: MessagesInEventFragmentArgs by navArgs()

    init {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speedMessagesAdapter = SpeedMessagesAdapter(requireActivity(), this)
        //setupSpeedMessages()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.attributes?.windowAnimations = R.style.ChatDialogAnimation

        // Cambiar la gravedad del diálogo
        val params = dialog.window?.attributes
        params?.gravity = Gravity.TOP // Cambia la gravedad a la parte superior
        val config = (args.config as HashMap<*, *>)
        val fabChatBottom = config["fab_chat_bottom"] as Int
        val toolbarBottom = config["toolbar_bottom"] as Float
        val screenHeight = resources.displayMetrics.heightPixels
        this.standardChatWindowHeight =
            (screenHeight - toolbarBottom - requireContext().getStatusBarHeight()).toInt()
        params?.height = (fabChatBottom) // Aquí puedes establecer el tamaño de la ventana
        params?.y = toolbarBottom.toInt()
        dialog.window?.attributes = params
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        initAdapter()

        binding = FragmentBottomSheetMessagesBinding.inflate(layoutInflater, container, false)

        binding.fullScreenButton.setOnClickListener {
            val screenHeight = resources.displayMetrics.heightPixels
            val rect = Rect()
            (requireActivity() as MainActivity).window.decorView.getWindowVisibleDisplayFrame(
                rect
            )
            val heightDiff = screenHeight - rect.height()

            resizeChatWindow(
                screenHeight,
                rect.height(),
                if (heightDiff > 0) KeyboardStatusEnum.OPEN else KeyboardStatusEnum.CLOSED,
                ChatWindowStatus.FULLSCREEN
            )

            binding.fullScreenButton.visibility = View.GONE
            binding.restoreScreenButton.visibility = View.VISIBLE
        }

        binding.restoreScreenButton.setOnClickListener {
            //     mapSituationFragmentViewModel.setMessageFragmentMode(ChatWindowStatus.NORMAL)
            //   viewModel.onChatWindowStatusChange(ChatWindowStatus.NORMAL)
            val screenHeight = resources.displayMetrics.heightPixels
            val rect = Rect()
            (requireActivity() as MainActivity).window.decorView.getWindowVisibleDisplayFrame(
                rect
            )
            val heightDiff = screenHeight - rect.height()

            resizeChatWindow(
                screenHeight,
                rect.height(),
                if (heightDiff > 0) KeyboardStatusEnum.OPEN else KeyboardStatusEnum.CLOSED,
                ChatWindowStatus.NORMAL
            )


            binding.fullScreenButton.visibility = View.VISIBLE
            binding.restoreScreenButton.visibility = View.GONE
        }

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.messagesList.setAdapter(adapter)

        binding.messagesList.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }

        binding.recyclerActions.adapter = speedMessagesAdapter
        binding.recyclerActions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

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
        binding.messageInputFix.setInputListener(InputListener { textMessage ->
            requireActivity().hideKeyboard()
            val me: User = UserViewModel.getInstance().getUser()!!
            viewModel.onNewMessage(
                me, textMessage.toString()
            )
            return@InputListener true
        })

        binding.messageInputFix.setAttachmentsListener {
            context?.handleTouch()
            showAtachmentsPopupWindow(binding.messageInputFix, requireActivity())
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        binding.messageInputFix.inputEditText.isEnabled = true
        MainActivityViewModel.getInstance().currentFragment = R.id.messagesInEventFragment
 binding.messagesList.layoutManager?.onRestoreInstanceState(recyclerViewState)

        startObservers()
    }

    override fun onPause() {
        super.onPause()
        MainActivityViewModel.getInstance().currentFragment = null
        binding.messageInputFix.inputEditText.isEnabled = false
        recyclerViewState = binding.messagesList.layoutManager?.onSaveInstanceState()
        stopObservers()
    }


    private var internalMessagesList: ArrayList<Message> = ArrayList()

    private var eventKey: String = ""
    private var currentAttrs: ViewAttributes? = null


    private fun startObservers() {


        val currentEventKey: String =
            mapSituationFragmentViewModel.eventFlow.value?.data?.event_key.toString()

        //     if (this.eventKey == currentEventKey) return

        this.eventKey = currentEventKey

        viewModel.sendingMessage.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    //  viewModel.onMessageSent(resource.data!!)
                    speedMessagesAdapter.setButtonsEnabled(false)
                }

                is Resource.Error -> {
                    speedMessagesAdapter.setButtonsEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        resource.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    speedMessagesAdapter.setButtonsEnabled(true)
                }

                else -> {}
            }
        }


        MapSituationFragmentViewModel.getInstance().followers.value?.forEach { follower ->
            // recorre la internalMessagesList buscando los mensajes cuyo autor sea este follower y actualiza el estado de online
            internalMessagesList.forEach { message ->
                if (message.user.id == follower.user_key) {
                    message.user.setOnline(follower.on_line ?: false)
                    adapter?.update(message)
                }

            }
        }
        // Ahora sigo escuchando
        MapSituationFragmentViewModel.getInstance().followers.observe(this) { followers ->
            val me = UserViewModel.getInstance().getUser()
            followers.forEach { follower ->
                // Actualizo el estado de going y calling
                if (follower.user_key == me?.user_key ?: false) {

                    switchCallSpeedDial(follower.call_time)
                    if (follower.call_time != null) {
                        binding.callStatus.setImageDrawable(requireContext().getDrawable(R.drawable.ic_phone_call_ok_by_smashicons))
                    } else {
                        binding.callStatus.setImageDrawable(requireContext().getDrawable(R.drawable.ic_phone_call_guess))
                    }


                    if (follower.arrival_time != null) {
                        binding.goingStatus.setImageDrawable(requireContext().getDrawable(R.drawable.ic_arrival_by_stockes_02))

                    } else if (follower.going_time != null) {
                        binding.goingStatus.setImageDrawable(requireContext().getDrawable(R.drawable.ic_man_running))
                    } else {
                        binding.goingStatus.setImageDrawable(requireContext().getDrawable(R.drawable.ic_man_standing_up))
                    }
                    //     switchArrivalSpeedDial(follower.arrival_time)


                    switchGoingSpeedDial(follower.arrival_time, follower.going_time)


                }



                internalMessagesList.forEach { message ->
                    if (message.user.id == follower.user_key) {
                        if (message.user.isOnline() != follower.on_line) {
                            message.user.setOnline(follower.on_line ?: false)
                            adapter?.update(message)
                        }
                    }
                }
            }
        }


        /***
         * Borro las notificaciones para este chat
         */
        viewModel.removeChatNotifications(eventKey)

        mapSituationFragmentViewModel.messages.value?.sortedBy { it.createdAt }
            ?.forEach { message ->
                if (!internalMessagesList.contains(message)) {
                    internalMessagesList.add(message)
                    adapter?.addToStart(message, true)
                }
            }

        mapSituationFragmentViewModel.messages.observe(this) { messages ->
            messages.forEach { message ->
                if (!internalMessagesList.contains(message)) {
                    internalMessagesList.add(message)
                    adapter?.addToStart(message, true)
                }
            }

            /*
            val messages = resource?.data
            if (messages != null) {
                internalMessagesList.clear()
                internalMessagesList.addAll(messages)
                adapter?.addToEnd(messages, true)
            }

 */
        }
        /*
                mapSituationFragmentViewModel.messages.observe(this) { messages ->
                    messages.forEach { message ->
                        if (!internalMessagesList.contains(message)) {
                            internalMessagesList.add(message)
                            adapter?.addToStart(message, true)
                        }
                    }
                }
        */

        /*
                var messages = mapSituationFragmentViewModel.getMessages()
                messages.forEach { message ->
                    if (!internalMessagesList.contains(message)) {
                        internalMessagesList.add(message)
                        adapter?.addToStart(message, true)
                    }
                }

                mapSituationFragmentViewModel.messageIncomming.observe(this) { resource ->
                    val chatEvent = resource?.data
                    handleMessageEvent(chatEvent)
                }
        */

        mapSituationFragmentViewModel.resetEvent.observe(this) {
            if (it == true) {
                internalMessagesList.clear()
                adapter?.clear()
                adapter?.notifyDataSetChanged()
            }
        }

        mapSituationFragmentViewModel.auxEventKey.observe(this) { resource ->
            /*
                  var messages = mapSituationFragmentViewModel.getMessages()
                  messages.forEach { message ->
                      if (!internalMessagesList.contains(message)) {
                          internalMessagesList.add(message)
                          adapter?.addToStart(message, true)
                      }
                  }
      */

            /*
                      when (resource) {
                          is Resource.Success -> {
                              viewModel.onConnectToEvent(resource.data!!.toString())
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
          */
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

                    MediaTypesEnum.TEXT -> {
                        TODO()
                    }
                }
                mainActivityViewModel.resetNewMedia()
            }
        }


        // Observa estado del Teclado Virutal
        mainActivityViewModel.screenAttrs.observe(this) { attrs ->
            if (attrs == currentAttrs) return@observe
            else {
                this.currentAttrs = attrs
            }

            if (attrs != null) {

                binding.cardView.post {
//                    val screenAttrs = attrs
                    val screenHeight = resources.displayMetrics.heightPixels
                    val rect = Rect()
                    (requireActivity() as MainActivity).window.decorView.getWindowVisibleDisplayFrame(
                        rect
                    )
                    val heightDiff = screenHeight - rect.height()


                    if (heightDiff > 0) { // Si la diferencia es más del 15% de la altura de la pantalla
                        // Teclado Abierto

                        resizeChatWindow(
                            screenHeight,
                            rect.height(),
                            KeyboardStatusEnum.OPEN,
                            viewModel.chatWindowStatus.value!!
                        )

                    } else {
                        // Teclado cerrado
                        binding.messageInputFix.clearFocus()

                        resizeChatWindow(
                            screenHeight,
                            rect.height(),
                            KeyboardStatusEnum.CLOSED,
                            viewModel.chatWindowStatus.value!!
                        )
                    }
                }
            }
        }

        // Observa estado de la ventana: Normal, FullScreen, Cerrado

        /*
              viewModel.chatWindowStatus.observe(this) { messageFragmentMode ->

                  val screenHeight = resources.displayMetrics.heightPixels
                  val rect = Rect()
                  (requireActivity() as MainActivity).window.decorView.getWindowVisibleDisplayFrame(
                      rect
                  )
                  val heightDiff = screenHeight - rect.height()
                  val keyboardStatus =
                      if (heightDiff > 0) KeyboardStatusEnum.OPEN else KeyboardStatusEnum.CLOSED

                  when (messageFragmentMode) {
                      ChatWindowStatus.CLOSED -> {
                          binding.cardView.visibility = View.GONE
                      }

                      ChatWindowStatus.NORMAL -> {
                          resizeChatWindow(
                              screenHeight,
                              rect.height(),
                              keyboardStatus,
                              messageFragmentMode
                          )

                      }

                      ChatWindowStatus.FULLSCREEN -> {


                          resizeChatWindow(
                              screenHeight,
                              rect.height(),
                              keyboardStatus,
                              messageFragmentMode
                          )
                      }

                      null -> {}
                  }
                  viewModel.onKeyboardStateChange(
                      mainActivityViewModel.isKeyboardOpen(),
                      messageFragmentMode
                  )
              }
      */
        viewModel.isMaximizeButtonVisible.observe(this) { isVisible ->
        }



        viewModel.messagePreview.observe(this) { event ->
            // Agrego el mensaje al chat
            handleMessageEvent(event)
            // Agrego el mensaje al chat
        }/*
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
        */


    }


    private fun stopObservers() {

        mapSituationFragmentViewModel.resetEvent.removeObservers(this)

        mapSituationFragmentViewModel.auxEventKey.removeObservers(this)

        mainActivityViewModel.newMedia.removeObservers(this)

        mainActivityViewModel.isKeyboardOpen.removeObservers(this)

        viewModel.isMaximizeButtonVisible.removeObservers(this)


        mapSituationFragmentViewModel.messages.removeObservers(this)
        viewModel.messagePreview.removeObservers(this)

        viewModel.storingMessage.removeObservers(this)


    }

    private fun removeObservers() {
        viewModel.chatRoomFlow.removeObservers(this)
        viewModel.messagePreview.removeObservers(this)
        mainActivityViewModel.isKeyboardOpen.removeObservers(this)
        mapSituationFragmentViewModel.messageIncomming.removeObservers(this)
        viewModel.storingMessage.removeObservers(this)
        viewModel.isMaximizeButtonVisible.removeObservers(this)
    }


    /*
        /***
         * Resize the chat window according several status
         */
        private fun resizeChatWindow(
            screenHeight: Int,
            visibleAreaHeight: Int,
            keyboardStatus: KeyboardStatusEnum,
            chatWindowStatus: ChatWindowStatus
        ) {
            if (viewModel.chatWindowStatus.value == chatWindowStatus && viewModel.keyboardStatus.value == keyboardStatus) return

            val heightDiff = screenHeight - visibleAreaHeight
            var newHeight = 0.0

            when (keyboardStatus) {
                KeyboardStatusEnum.OPEN -> when (chatWindowStatus) {
                    ChatWindowStatus.CLOSED -> newHeight = 0.0
                    ChatWindowStatus.NORMAL -> {
                        newHeight =
                            (standardChatWindowHeight - heightDiff + requireContext().getStatusBarHeight()).toDouble()
                        binding.cardView.radius = 20.dp()
                    }

                    ChatWindowStatus.FULLSCREEN -> {
                        newHeight =
                            (visibleAreaHeight + requireContext().getStatusBarHeight()).toDouble()
                        (binding.messageInputFix.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                            200  //(visibleAreaHeight - requireContext().getStatusBarHeight()).toDouble()
                        binding.cardView.radius = 0.0f
                    }
                }

                KeyboardStatusEnum.CLOSED -> when (chatWindowStatus) {
                    ChatWindowStatus.CLOSED -> newHeight = 0.0
                    ChatWindowStatus.NORMAL -> {
                        // ok
                        newHeight = (standardChatWindowHeight).toDouble()
                        binding.cardView.radius = 20.px.toFloat()
                    }

                    ChatWindowStatus.FULLSCREEN -> {
                        // ok
                        newHeight = (screenHeight - requireContext().getStatusBarHeight()).toDouble()
                        binding.cardView.radius = 0.toFloat()
                        (binding.messageInputFix.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                            0
                    }
                }
            }

            binding.cardView.visibility = View.GONE
            binding.cardView.layoutParams.height = newHeight.toInt()
            binding.cardView.visibility = View.VISIBLE

            viewModel.onChatWindowStatusChange(chatWindowStatus)
            viewModel.onKeyboardStatusChange(keyboardStatus)
            binding.messageInputFix.requestFocus()
        }
    */

    private fun handleMessageEvent(chatEvent: ChatRepository.ChatDataEvent?) {

        when (chatEvent) {
            is ChatRepository.ChatDataEvent.OnChildAdded -> {
                val message = chatEvent.data

                if (message.video != null) {
                    message.video.url =
                        CHAT_FILES_STORAGE_PATH+"/" + chatroomKey + "/" + message.video.url.getJustFileName()
                } else if (message.image != null) {
                    message.image.url =
                        CHAT_FILES_STORAGE_PATH +"/"+ chatroomKey + "/" + message.image.url.getJustFileName()
                } else if (message.voice != null) {
                    message.voice.url =
                        CHAT_FILES_STORAGE_PATH +"/"+ chatroomKey + "/" + message.voice.url.getJustFileName()
                }

                if (!internalMessagesList.contains(message)) {

                    internalMessagesList.add(message)

                    var addMessage = true
                    if (message.action != null) {

                        if (message.user.id.compareTo(myUserKey) == 0) {
                            addMessage = false
                        }
                    }
                    if (addMessage) {
                        if (mapSituationFragmentViewModel.isChatOpen.value == true) {
                            val eventKey =
                                mapSituationFragmentViewModel.auxEventKey.value.toString()
                            mainActivityViewModel.onMessageRead(eventKey, message.id)
                        }
                        lifecycleScope.launch(Dispatchers.Main) {
                            adapter?.addToStart(message, true)
                            adapter?.notifyItemInserted(adapter?.itemCount!! - 1)
                        }
                    }
                } else {

                    lifecycleScope.launch(Dispatchers.Main) {
                        adapter?.update(message)
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

            else -> {}
        }
    }


    override fun onStop() {
        super.onStop()
        removeObservers()
    }


    private fun switchGoingSpeedDial(arrivalTime: Long?, goingTime: Long?) {
        var pill: SpeedMessage? = null
        var addArrivalPill = false

        if (arrivalTime != null) {
            pill = SpeedMessage(
                "arrival",
                SpeedMessageActions.IM_THERE,
                R.string.not_in_place,
                R.string.not_in_place_message,
            )

            removePill("im going")
        } else {

            addArrivalPill = true

            if (goingTime == null) {
                //-----------------
                pill = SpeedMessage(
                    "im going",
                    SpeedMessageActions.GOING,
                    R.string.im_going,
                    R.string.im_going_message
                )
            } else {
                pill = SpeedMessage(
                    "im going",
                    SpeedMessageActions.GOING,
                    R.string.im_not_going,
                    R.string.im_not_going_message
                )
            }
        }

        updateSpeedPill(pill)

        if (addArrivalPill) {
            val pillArrival = SpeedMessage(
                "arrival",
                SpeedMessageActions.IM_THERE,
                R.string.im_in_place,
                R.string.im_in_place_message
            )
            updateSpeedPill(pillArrival)
        }


    }


    /*
        private fun switchArrivalSpeedDial(arrivalTime: Long?) {

            var pill: SpeedMessage? = null
            if (arrivalTime == null) {
                pill = SpeedMessage(
                    "arrival",
                    SpeedMessageActions.GOING,
                    R.string.im_going,
                    R.string.im_going_message,
                )


            } else {
                pill = SpeedMessage(
                    "arrival",
                    SpeedMessageActions.GOING,
                    R.string.im_not_going,
                    R.string.im_not_going_message
                )
            }
            updateSpeedPill(pill)

            /*
            val index = speedMessagesAdapter.getData().indexOf(pill)
            if (index == -1) {
                speedMessagesAdapter.getData().add(pill)
                speedMessagesAdapter.notifyItemInserted(speedMessagesAdapter.getData().size - 1)
            } else {
                speedMessagesAdapter.getData().set(index, pill)
                speedMessagesAdapter.notifyItemChanged(index)
            }
            */
        }
    */

    private fun switchCallSpeedDial(callTime: Long?) {
        var pill: SpeedMessage? = null
        if (callTime == null) {
            pill = SpeedMessage(
                "already called",
                SpeedMessageActions.CALLED,
                R.string.already_called,
                R.string.already_called_message
            )
        } else {
            pill = SpeedMessage(
                "already called",
                SpeedMessageActions.CALLED,
                R.string.not_called,
                R.string.not_called_message
            )
        }

        updateSpeedPill(pill)
    }

    private fun removePill(pillTag: String) {
        var index = speedMessagesAdapter.getData().indexOfFirst { it.messageTag == pillTag }
        if (index > -1) {
            speedMessagesAdapter.getData().removeAt(index)
            speedMessagesAdapter.notifyItemRemoved(index)
        }
    }


    private fun updateSpeedPill(pill: SpeedMessage) {
        val index = speedMessagesAdapter.getData().indexOf(pill)
        if (index == -1) {
            speedMessagesAdapter.getData().add(pill)
            speedMessagesAdapter.notifyItemInserted(speedMessagesAdapter.getData().size - 1)
        } else {
            speedMessagesAdapter.getData().set(index, pill)
            speedMessagesAdapter.notifyItemChanged(index)
        }
    }


    /*
        private fun setupSpeedMessages() {

            val speedMessagesList = ArrayList<SpeedMessage>()
            speedMessagesList.add(
                SpeedMessage(
                    "im going",
                    SpeedMessageActions.GOING,
                    R.string.im_going,
                    R.string.im_going_message,
                    SpeedMessageActions.GOING,
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
                    SpeedMessageActions.CALLED,
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
                    SpeedMessageActions.IM_THERE,
                    R.string.not_in_place,
                    R.string.not_in_place_message
                )
            )

            speedMessagesAdapter.setData(speedMessagesList)
        }
    */
    private fun prepareMessage(): Message {
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
        val payloadForImageMessage: CustomIncomingImageMessageViewHolder.Payload =
            CustomIncomingImageMessageViewHolder.Payload()
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


        payloadForImageMessage.lifecycleOwner = viewLifecycleOwner


        payloadForIncomingVoiceMessage.lifecycleOwner = viewLifecycleOwner
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
        payloadForOutcomingVoiceMessage.lifecycleOwner = viewLifecycleOwner

        val imageLoader = ImageLoader { imageView, url, _ ->
            val requestOptions = RequestOptions()
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            val storageReference: Any? = null
            when (resources.getResourceName(imageView!!.id).substringAfter("/")) {
                "messageUserAvatar" -> {
                    val filePath = PROFILE_IMAGES_STORAGE_PATH + "/" + url?.substringBefore("/")
                    var justFileName = (url ?: "").substringAfterLast("/")
                    lifecycleScope.launch(Dispatchers.IO) {
                        requireContext().assignFileImageTo(justFileName, filePath, imageView)
                        if (imageView.visibility != View.VISIBLE) {
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }

                "image" -> {

                    val mediaUrl = url ?: ""
                    val mediaId = url?.getJustFileName() ?: ""
                    // Mostrar imagen temporal mientras se descarga
                    //       ivVideoView.defaultArtwork = AppClass.instance.getDrawable(R.drawable.ico_cloud_upload_24)
                    GlobalScope.launch(Dispatchers.IO) {
                        MapSituationFragmentViewModel.getInstance().mediaState.collect { mediaState ->
                            val mediaPath = mediaState[mediaId]
                            if (mediaPath != null) {
                                withContext(Dispatchers.Main)
                                {
                                    GlideApp.with(requireContext())
                                        .load(File(mediaPath))
                                        .into(imageView)
                                    imageView.tag = mediaPath
                                    this@launch.cancel()
                                }

                            } else {
                                withContext(Dispatchers.Main)
                                {
                                    GlideApp.with(requireContext())
                                        .load(R.drawable.ic_placeholder_image_by_pop_vectors)
                                        .into(imageView)
                                }
                                MapSituationFragmentViewModel.getInstance()
                                    .downloadMedia(mediaUrl, mediaId)
                            }
                        }


                    }
                    /*
                    val justFileName = url?.getJustFileName().toString()
                    val filePath = url?.substringBeforeLast("/").toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        requireContext().assignFileImageTo(justFileName, filePath, imageView)
                    }

                     */
                }
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
            R.layout.item_custom_incoming_image_message,
            payloadForImageMessage
        ).setOutcomingImageConfig(
            CustomOutcomingImageMessageViewHolder::class.java,
            R.layout.item_custom_outcoming_image_message,
            payloadForImageMessage
        )


// ojo que falla y esta pesta la misma clase para ambos
        val lifecycleOwner = viewLifecycleOwner

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

        payloadForOutcomingVideoMessage.lifecycleOwner = viewLifecycleOwner

        payloadForIncomingVideoMessage.lifecycleOwner = viewLifecycleOwner

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
            view, Gravity.NO_GRAVITY, xAxis, yAxis
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
            .setOnTouchListener { _, motionEvent ->
                this.recordingManagement(motionEvent)
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

    private fun addMessageToStart(message: Message) {
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

    /*
        private fun sendTextMessage(message: Message): Boolean {
            addMessageToStart(message)
            return true
        }
    */

    private fun sendSpeedMessage(message: Message): Boolean {

        viewModel.onNewSpeedMessage(
            message
        )
        return true
    }

/*
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

        val destinationPath = requireContext().copyFile(
            filePath.substringBeforeLast("/"), filePath.getJustFileName(), finalPath
        )

        val imageFile = File(destinationPath)
        val image = Message.Image(filePath.getJustFileName())

        mapSituationFragmentViewModel.addToMediaState(imageFile.name, imageFile.absolutePath)

        message.image = image
        addMessageToStart(message)
        adapter?.notifyItemInserted(adapter?.messagesCount ?: 0 - 1)
        Log.d("MEDIA_MESSAGE_SEND", Gson().toJson(message))

        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.onNewMediaMessage(me, mediaFile, imageFile)
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
        val videoFile = File(finalPath)

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


        mapSituationFragmentViewModel.addToMediaState(videoFile.name, videoFile.absolutePath)
        addMessageToStart(message)
        adapter?.notifyItemInserted(adapter?.messagesCount ?: (0 - 1))

        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.onNewMediaMessage(me, mediaFile, videoFile)
        }
    }
*/
    /*
        fun onMessageAdded(message: Message) {
            Log.d("FLOW_ADAPTER", "Entro el mensaje  = $message")
            adapter?.addToStart(message, true)
        }
    */
    fun connectToEvent(eventKey: String) {
        Log.d("FLOW_CONNECT_MESSAGES_IN_EVENT", "Connecting to event {$eventKey}")
        //  adapter?.clear()
        //  this.eventKey = eventKey
        this.chatroomKey = eventKey
        viewModel.onConnectToEvent(eventKey)
    }


    var messagesDisplayRulesDefault: ConstraintLayout.LayoutParams? = null


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

    /*
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
    */
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

    /*
        fun enableControls() {
            binding.messageInputFix.isEnabled = true
            binding.messageInputFix.inputEditText.isEnabled = true
        }
    */
    internal var toPickImagePermissionsRequest: ActivityResultLauncher<Array<String>>? =
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

                    val imageFile = File(selectedMedia[0].path)


                    lifecycleScope.launch(Dispatchers.Default) {

                        var eventKey =
                            MapSituationFragmentViewModel.getInstance().eventFlow.value?.data?.event_key?.toString()
                                ?: ""
                        val localPath = selectedMedia[0].path.toString()


                        val mediaFile = File(localPath).toMediaFile(
                            requireContext(),
                            CHAT_FILES_STORAGE_PATH +"/" + eventKey
                        )
                        if (mediaFile != null) {
                            mapSituationFragmentViewModel.downloadMedia( mediaFile.localFullPath.replace( requireContext().cacheDir.toString()+"/" ,""), mediaFile.localFullPath.getJustFileName())

                            viewModel.onNewMediaMessage(
                                mainActivityViewModel.user.value!!, mediaFile, File(mediaFile.localFullPath)
                            )

                        }
                    }
                }
            }
        }

    internal var toTakeVideoPermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val videoPickerIntent =
                    Lassi(requireContext()).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(5).setGridSize(3).setMediaType(MediaType.VIDEO)
                        .setMaxFileSize(1024)
                        .setCompressionRatio(80) // compress image for single item selection (can be 0 to 100)
                        .setMinTime(5) // for MediaType.VIDEO only
                        .setMaxTime(60) // for MediaType.VIDEO only
                        .setSupportedFileTypes(
                            "mp4", "mkv", "webm", "avi", "flv", "3gp"
                        ) // Filter by limited media format (Optional)
                        .setMinFileSize(0) // Restrict by minimum file size
                        .setMaxFileSize(512) //  Restrict by maximum file size
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
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as java.util.ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        val videoFile = File(selectedMedia[0].path)
                        try {
                            val localPath = selectedMedia[0].path.toString()

                            val mediaFile = File(localPath).toMediaFile(
                                requireContext(),
                                CHAT_FILES_STORAGE_PATH +"/"+ eventKey
                            )

                            if (mediaFile != null) {

                                val videoDimentions =
                                    requireContext().getDimentions(Uri.parse(localPath))
                                mediaFile.width = videoDimentions["width"]
                                mediaFile.height = videoDimentions["height"]

//                                mapSituationFragmentViewModel.addToMediaState(mediaFile.localFullPath.getJustFileName(), mediaFile.localFullPath),""), mediaFile.localFullPath.getJustFileName())
                                mapSituationFragmentViewModel.downloadMedia( mediaFile.localFullPath.replace( requireContext().cacheDir.toString()+"/" ,""), mediaFile.localFullPath.getJustFileName())


                                viewModel.onNewMediaMessage(
                                    mainActivityViewModel.user.value!!, mediaFile, videoFile
                                )

                            }

                        } catch (ex: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(), ex.localizedMessage, Toast.LENGTH_LONG
                                ).show()
                            }
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

    internal fun prepareMediaMessage(
        mediaType: MediaTypesEnum, fileName: String, localFullPath: String
    ): Any {

        if (fileName.compareTo(fileName.getJustFileName()) != 0) {
            throw Exception("El parametro fileName debe contener solo el nombre del archivo")
        }
        try {

            val media = MediaFile(mediaType, localFullPath, fileName)
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
                    /*
                    mediaFileEncoded = MultimediaUtils(requireContext()).convertFileToBase64(
                        Uri.parse(
                            fileUri
                        )
                    ).toString()
*/
                    if (!localFullPath.contains(requireContext().cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH + chatroomKey.toString())) {
                        val destination =
                            requireContext().cacheDir.toString() + "/" + CHAT_FILES_STORAGE_PATH +"/"+ chatroomKey
                        FileUtils().copyFile(
                            media.localFullPath.substringBeforeLast("/"),
                            media.localFullPath.getJustFileName(),
                            destination
                        )
                    }
                }
//                media.bytesB64 = mediaFileEncoded
            }

            return media
        } catch (ex: Exception) {
            return ex
        }

    }


    /*
        // recording
        private fun startMonitoringWave() {
            var iconRecorder = popupView?.findViewById<ImageView>(R.id.icon_audio)
            val iconRecordView: AudioRecordView = popupView?.findViewById(R.id.audioRecordView)!!
            val captionRecorder: TextView = popupView?.findViewById(R.id.caption_record)!!
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

            val iconRecorder: ImageView = popupView?.findViewById(R.id.icon_audio)!!
            val captionRecorder: TextView = popupView?.findViewById(R.id.caption_record)!!
            val iconRecordView: AudioRecordView = popupView?.findViewById(R.id.audioRecordView)!!


            captionRecorder.text = requireContext().getText(R.string.voice_message)
            iconRecorder.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_audio_mic_outline
                )
            )
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

                    val iconBitmap = requireContext().getBitmapFromVectorDrawable(
                        R.drawable.ic_recording
                    )

                    val iconRecorder: ImageView = popupView?.findViewById(R.id.icon_audio)!!
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
                            val fileName = recordingFilename.toString().getJustFileName()
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
    */
}


