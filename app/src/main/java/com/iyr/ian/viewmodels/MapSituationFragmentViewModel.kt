package com.iyr.ian.viewmodels

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.dao.repositories.EventRepository
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventFollowersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.ui.chat.ChatWindowStatus
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapSituationFragmentViewModel(val mainActivityViewModel: MainActivityViewModel) : ViewModel(),
    LifecycleObserver {

    enum class OnEventClosedActionsEnum {
        LOAD_FIRST_EVENT,
        GO_MAIN
    }


    private var eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()
    private var eventRepository: EventRepositoryImpl = EventRepositoryImpl()
    private var eventFollowersRepository: EventFollowersRepositoryImpl =
        EventFollowersRepositoryImpl()
    private var chatsRepository: ChatRepositoryImpl = ChatRepositoryImpl()
    private var notificationsRepository: NotificationsRepositoryImpl = NotificationsRepositoryImpl()
    private var eventTimeOfLastViewJob: Job? = null

    //-- DATA SUPPORT
    private val _spinner = MutableLiveData<Boolean?>()
    val spinner: LiveData<Boolean?> = _spinner

    var lastEventUpdate = Event()

    private var eventKey: String? = null

    private val _currentEventKey = MutableLiveData<Resource<String>?>()
    val currentEventKey: LiveData<Resource<String>?> get() = _currentEventKey

    private val _subscriptedEvents = MutableLiveData<EventRepository.EventDataEvent>()
    val subscriptedEvents: LiveData<EventRepository.EventDataEvent> = _subscriptedEvents


    private val _eventFlow = MutableLiveData<Resource<Event>>()
    val eventFlow: LiveData<Resource<Event>> get() = _eventFlow


    /*
    private val _eventFollowersFlow = MutableLiveData<Resource<EventFollowersRepository.EventFollowerDataEvent>>()
    val eventFollowersFlow: LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent>> = _eventFollowersFlow
*/
    private var messages = ArrayList<Message>() // array de mensajes recibidos

    // --- KEYBOARD


    /*
      private val _isVirtualKeyboardClosed = MutableLiveData<Boolean?>()
      val isVirtualKeyboardClosed: LiveData<Boolean?> = _isVirtualKeyboardClosed
  *//*
    private val _isKeyboardOpen = MutableLiveData<Boolean?>()
    val isKeyboardOpen: LiveData<Boolean?> = _isKeyboardOpen
*/

    // --- MAP
    private val _resetMap = MutableLiveData<String>()
    val resetMap: LiveData<String> = _resetMap

    // --- CHAT BUTTON -------

    private val _isChatButtonVisible = MutableLiveData<Boolean?>()
    val isChatButtonVisible: LiveData<Boolean?> = _isChatButtonVisible

    fun chatOpenButtonVisibility(visible: Boolean) {
        _isChatButtonVisible.postValue(visible)
    }

    private val _isChatOpen = MutableLiveData<Boolean?>(false)
    val isChatOpen: LiveData<Boolean?> = _isChatOpen

    /**
     * Cuando la ventana de chat esta abierta
     */
    fun onChatOpened() {
//        _isChatButtonVisible.postValue(false)
        _isCounterVisible.postValue(false)
        _isChatOpen.postValue(true)
    }

    /**
     * Cuando la ventana de chat esta cerrada
     */
    fun onChatClosed() {
        //      _isChatButtonVisible.postValue(true)
        _isCounterVisible.postValue(true)
        _isChatOpen.postValue(false)
        messages.clear()
    }

    // --- TIME COUNTER

    private val _isCounterVisible = MutableLiveData<Boolean?>(true)
    val isCounterVisible: LiveData<Boolean?> = _isCounterVisible


    // --- CHAT

    private val _showChatFragment = MutableLiveData<Boolean?>()
    val showChatFragment: LiveData<Boolean?> = _showChatFragment

    private val _messageIncomming = MutableLiveData<Resource<ChatRepository.ChatDataEvent>>()
    val messageIncomming: LiveData<Resource<ChatRepository.ChatDataEvent>> = _messageIncomming


    private val _messageFragmentMode = MutableLiveData<ChatWindowStatus?>()
    val messageFragmentMode: LiveData<ChatWindowStatus?> = _messageFragmentMode

    // --- --- ---

    val followersList = ArrayList<EventFollower>()
    private val _followers = MutableLiveData<ArrayList<EventFollower>>()
    val followers: LiveData<ArrayList<EventFollower>> = _followers

    /*
    private val _viewersUpdatesFlow =
        MutableLiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>()
    val viewersUpdatesFlow: LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
        _viewersUpdatesFlow
*/


    private val _viewersUpdatesFlow =
        MutableSharedFlow<Resource<EventFollowersRepository.EventFollowerDataEvent?>>()
    val viewersUpdatesFlow: SharedFlow<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
        _viewersUpdatesFlow


    var thisEvent: Event? = null
    private val _currentEvent = MutableLiveData<EventRepository.EventDataEvent>()
    val currentEvent: LiveData<EventRepository.EventDataEvent> = _currentEvent


    private val _bottomBarVisibilityStatus = MutableLiveData<Boolean?>()
    val bottomBarVisibilityStatus: LiveData<Boolean?> = _bottomBarVisibilityStatus


    init {
    }

    private fun initMessagesFlow() {


    }


    var eventUpdatesJob: Job? = null
    var viewersUpdatesJob: Job? = null



    /**
     * Mediante esta funcion, se conecta a un evento actualizando toda la informacion relativa
     * @param eventKey : String : Clave del evento al que se quiere conectar
     */
    fun startObserveEvent(eventKey: String) {


        onCurrentEventChange(eventKey)

    }
    //---------------------
    /**
     * Forma correcta de hacerlo
     */
    fun listenEventFlow(eventKey: String) = liveData<Resource<Event>>(Dispatchers.IO) {
        eventRepository.listenEventFlow(eventKey)?.collect { update ->
            _spinner.postValue(false)
            if (update.data!!.event_key != _currentEventKey.value?.data) {
                var es_un_id_distinto = 3
                return@collect
            }
            lastEventUpdate = update.data!!
            _eventFlow.postValue(update)
        }
    }


    //--------------------


    /*
        fun funEventFollowersFlow(eventKey : String) =
            liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {

                eventFollowersRepository.getEventFollowersFlow(eventKey)?.collect { update ->
                    //emit(update)
                    when (update) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                            emit(Resource.Error(update.exception.localizedMessage.toString()))
                        }

                        null -> {
                            emit(Resource.Error("null"))
                        }

                        else -> {
                            emit(Resource.Success(update))
                        }
                    }
                }
            }
    */

    fun resetChat() {
        messages.clear()
    }

    @ExperimentalCoroutinesApi
    fun getChatFlow(eventKey: String) = liveData(Dispatchers.IO) {
        chatsRepository.getChatFlow(eventKey)?.collect { update ->
            var texto = (update.data!! as ChatRepository.ChatDataEvent.OnChildAdded).data.text
            Log.d("FLOW_VIEWMODEL", "emito el registo = ${texto}")
//                    _messageIncomming.postValue(update)
            emit(update.data)
        }
    }


    fun connectToChatRoom(eventKey: String) {

    }


    fun setEventTimeOfLastView(userKey: String, eventKey: String) {
        eventTimeOfLastViewJob?.cancel()

        eventTimeOfLastViewJob = viewModelScope.launch(Dispatchers.IO) {

            val timeInMillis =
                (eventRepository.postLastTimeSeen(userKey, eventKey).data ?: "0".toLong())/*
                mainActivityViewModel.getEventFollowedByKey(eventKey)?.last_read = timeInMillis
                Log.d("LastView", timeInMillis.toString())
    */
        }.also { eventTimeOfLastViewJob = it }

    }

    fun startListenSubscribedEvents(userKey: String) {


        viewModelScope.launch(Dispatchers.IO) {
            eventsRepository.getEvents(userKey).collect { action ->
                when (action) {
                    is EventsRepository.DataEvent.OnChildAdded -> {
                        var event = eventRepository.getEvent(action.data.event_key)
                        if (event is Resource.Success) {
                            var toReturn: Event = event.data!!
                            _subscriptedEvents.postValue(
                                EventRepository.EventDataEvent.OnChildAdded(
                                    toReturn
                                )
                            )
                        }
                    }

                    is EventsRepository.DataEvent.OnChildChanged -> {
                        var event = eventRepository.getEvent(action.data.event_key)
                        if (event is Resource.Success) {
                            var toReturn: Event = event.data!!
                            _subscriptedEvents.postValue(
                                EventRepository.EventDataEvent.OnChildChanged(
                                    toReturn
                                )
                            )
                        }

                    }

                    is EventsRepository.DataEvent.OnChildRemoved -> {
                        var event = eventRepository.getEvent(action.data.event_key)
                        if (event is Resource.Success) {
                            var toReturn: Event = event.data!!
                            _subscriptedEvents.postValue(
                                EventRepository.EventDataEvent.OnChildRemoved(
                                    toReturn.event_key
                                )
                            )
                        }
                    }

                    is EventsRepository.DataEvent.OnChildMoved -> TODO()
                    is EventsRepository.DataEvent.OnError -> TODO()
                }

            }
        }
    }

    fun getEventFollowedByKey(eventKey: String): EventFollowed? {

        mainActivityViewModel.eventsFollowed.value?.forEach { event ->
            if (event.event_key == eventKey) {
                return event
            }
        }

        return null
    }

    fun getMessages() = messages

    suspend fun getEventObject(eventKey: String): Event? {
        return eventRepository.getEvent(eventKey).data
    }

    fun getEvent(eventKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var eventsFollowed = mainActivityViewModel.eventsFollowed.value

            var event = eventRepository.getEvent(eventKey).data!!
            if (event != null) {
                withContext(Dispatchers.Main) {
                    _currentEvent.postValue(EventRepository.EventDataEvent.OnChildAdded(event))
                }
            }/*
                        if (!eventsFollowed?.contains(event)) {

                            requireActivity().runOnUiThread {
                                eventsHeaderAdapter.addEvent(event)

                            }
                        }
                        var pepe = 3*/
        }
    }

    fun onEventSelected(eventKey: String) {
        startObserveEvent(eventKey)
    }

    fun disconect() {
        if (eventUpdatesJob != null) {
            eventUpdatesJob?.cancel()
        }


        if (viewersUpdatesJob != null) {
            viewersUpdatesJob?.cancel()
        }
        messages.clear()
    }

    fun hideChatFragment() {
        _showChatFragment.postValue(false)
    }

    fun showChatFragment() {
        _showChatFragment.postValue(true)
    }


    fun onMessageModeToChatRequested() {
        _bottomBarVisibilityStatus.postValue(false)
        _messageFragmentMode.postValue(ChatWindowStatus.NORMAL)

    }

    fun onMessageModeToFullScreenRequested() {
        _bottomBarVisibilityStatus.postValue(false)

        _messageFragmentMode.postValue(ChatWindowStatus.FULLSCREEN)
    }

    fun onMessageModeToClosedRequested() {
        _bottomBarVisibilityStatus.postValue(false)
        _messageFragmentMode.postValue(ChatWindowStatus.CLOSED)
    }

    fun setMessageFragmentMode(status: ChatWindowStatus) {
        _messageFragmentMode.value = status

    }

    fun setChatWindowMode(mode: ChatWindowStatus) {
        _messageFragmentMode.value = mode
    }


    private val _resetUnreadsForRoom = MutableLiveData<String?>()
    val resetUnreadsForRoom: LiveData<String?> = _resetUnreadsForRoom

    fun resetUnreadMessages(userKey: String, eventKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Marco como leidos los mensajes
            chatsRepository.resetUnreadMessages(userKey, eventKey)
            // Elimino el registro de las notificaciones
            notificationsRepository.onAllMessagesRead(userKey, eventKey)
            _resetUnreadsForRoom.postValue(eventKey)
        }
    }

    /**
     * gestiona las acciones a realizar cuando un mensaje es leido
     * @param messageKey : String : Clave del mensaje leido
     */
    fun onMessageRead(messageKey: String) {
        mainActivityViewModel.onMessageRead(eventKey.toString(), messageKey)
    }


    /**
     * Resetea la lista de seguidores
     */
    fun resetFollowersList() {
        followersList.clear()
        _followers.postValue(followersList)
    }


    fun onFollowerAdded(follower: EventFollower) {
        if (followersList.contains(follower)) {
            return
        }
        followersList.add(follower)
        _followers.postValue(followersList)
    }

    fun onFollowerChanged(follower: EventFollower) {
        followersList.forEachIndexed { index, eventFollower ->
            if (eventFollower.user_key == follower.user_key) {
                followersList[index] = follower
                _followers.postValue(followersList)
            }
        }
    }

    fun onFollowerRemoved(follower: EventFollower) {
        followersList.forEachIndexed { index, eventFollower ->
            if (eventFollower.user_key == follower.user_key) {
                followersList.removeAt(index)
                _followers.postValue(followersList)
            }
        }
    }


    /**
     * Invierte el estado de ir yendo a asistir en un envento
     * @param userKey : String : Clave del usuario
     * @param eventKey : String : Clave del evento
     */
    fun onToggleGoingStatus(userKey: String, eventKey: String) {
        _spinner.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            var call = eventRepository.togleGoingStatus(
                userKey, eventKey
            )
            _spinner.postValue(false)
        }
    }


    /**
     * Invierte el estado de haber llamado a las autoridades en un evento
     * @param userKey : String : Clave del usuario
     * @param eventKey : String : Clave del evento
     */
    fun onToggleCallStatus(userKey: String, eventKey: String) {
        _spinner.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            var call = eventRepository.toggleCallAuthority(userKey, eventKey)
            _spinner.postValue(false)
        }
    }

    fun getFollower(authorKey: String): EventFollower? {
        followersList.forEach { follower ->
            if (follower.user_key == authorKey) {
                return follower
            }
        }
        return null
    }


    public var visibleEventKey: String = ""

    /*
    * Forma correcta de hacerlo
     */
    private val _eventFollowersConnector =
        MutableLiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>()
    val eventFollowersConnector: MutableSharedFlow<EventFollowersRepository.EventFollowerDataEvent?> =
        MutableSharedFlow<EventFollowersRepository.EventFollowerDataEvent?>()


    fun eventFollowersFlow(eventKey: String): LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
        liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {

            eventFollowersRepository.getEventFollowersFlow(eventKey)?.collect { update ->
                //emit(update)
                when (update) {
                    is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                        emit(Resource.Error(update.exception.localizedMessage.toString()))
                    }

                    null -> {
                        emit(Resource.Error("null"))
                    }

                    else -> {
                        emit(Resource.Success(update))

                        eventFollowersConnector.emit(update)

                    }
                }

                when (update) {
                    is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                        emit(Resource.Error(update.exception.localizedMessage.toString()))
                    }

                    null -> {
                        emit(Resource.Error("null"))
                    }

                    else -> {
                        emit(Resource.Success(update))

                        when (update) {
                            is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                                onFollowerAdded(update.data)
                            }

                            is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                                onFollowerChanged(update.data)
                            }

                            is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> {

                            }

                            is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
                                onFollowerRemoved(update.data)
                            }

                            is EventFollowersRepository.EventFollowerDataEvent.OnError -> {}
                        }

                        //                     eventFollowersConnector.emit(update)

                    }
                }
            }
        }

    fun onConnectToChatFlow(eventKey: String): LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
        liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {

            chatsRepository.getChatFlow(eventKey)?.collect { update ->

                var texto =
                    (update.data!! as ChatRepository.ChatDataEvent.OnChildAdded).data.text
                Log.d("FLOW_VIEWMODEL", "emito el registo = ${texto}")
//                    _chatUpdatesFlow.emit(update)

                when (update.data) {
                    is ChatRepository.ChatDataEvent.OnChildAdded -> messages.add(update.data.data)
                    is ChatRepository.ChatDataEvent.OnChildChanged -> {}
                    is ChatRepository.ChatDataEvent.OnChildMoved -> {}
                    is ChatRepository.ChatDataEvent.OnChildRemoved -> {}
                    is ChatRepository.ChatDataEvent.OnError -> {}
                }

                viewModelScope.launch(Dispatchers.Main) {
                    _messageIncomming.setValue(update)
                }
            }
        }

    /**
     * Escucha los eventos
     * @param eventKey : String : Clave del evento
     */
    fun onCurrentEventChange(eventKey: String) {

        eventUpdatesJob?.let { job ->
            Log.d("EVENT_FLOWS", "Cancelo el seguimiento al Evento = " + visibleEventKey)
            job.cancel()
        }
        viewersUpdatesJob?.let { job ->
            Log.d(
                "EVENT_FLOWS",
                "Cancelo el seguimiento a los seguidores del Evento = " + visibleEventKey
            )
            job.cancel();
        }


        eventUpdatesJob = viewModelScope.launch(Dispatchers.IO) {

            Log.d(
                "EVENT_FLOWS",
                "eventUpdatesJob - Comienzo a seguir al Evento = " + eventKey.toString()
            )

            eventRepository.listenEventFlow(eventKey)?.collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _spinner.postValue(false)
                        // _eventFlow.postValue(resource)
                    }

                    is Resource.Loading -> {
                        _spinner.postValue(true)
                    }

                    is Resource.Success -> {
                        var event: Event? = resource.data
                        _spinner.postValue(false)

                        if (event == null) {
                            AppClass.instance.onEventRemoved(eventKey)

                            disconect()
                            onEventClosed(eventKey)


                        } else {
                            lastEventUpdate = event
                            _eventFlow.postValue(resource)
                            Log.d(
                                "EVENT_FLOWS",
                                "Recibo datos del Evento = " + event.event_key.toString()
                            )
                        }
                    }
                }
            }
        }
//------------------------
        //  viewersUpdatesJob = startViewersUpdatesJob(eventKey )

        /*
        viewModelScope.launch(Dispatchers.IO) {

        Log.d("EVENT_FLOWS","viewersUpdatesJob - Comienzo a seguir a  los seguidores del Evento = "+ eventKey.toString())
        eventFollowersFlow = liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {

                eventFollowersRepository.getEventFollowersFlow(eventKey)?.collect { update ->
                        //emit(update)
                        when (update) {
                            is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                                emit(Resource.Error(update.exception.localizedMessage.toString()))
                            }
                            null -> {
                                emit(Resource.Error("null"))
                            }

                            else -> {
                                emit(Resource.Success(update))
                            }
                        }

                        when (update) {
                            is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                                emit(Resource.Error(update.exception.localizedMessage.toString()))
                            }

                            null -> {
                                emit(Resource.Error("null"))
                            }

                            else -> {
                                emit(Resource.Success(update))
                            }
                        }
                    }
            }


        var pp = 33
    }
*/
//--------------------
        /*
              unreadMessagesFlow = lifecycleScope.launch(Dispatchers.Main) {
                  mainActivityViewModel.unreadMessagesFlow.observe(this@MapSituationFragment) { unreadMessagesList ->
                      var eventKey = viewModel.currentEventKey.value

                      if (unreadMessagesList.size == 0) {
                          binding.unreadCounterText.visibility = View.GONE
                          return@observe
                      }

                      unreadMessagesList.forEach { unreadMessages ->
                          if (unreadMessages.chat_room_key.compareTo(eventKeyShowing ?: "") == 0) {
                              if (unreadMessages.qty > 0) {
                                  binding.unreadCounterText.visibility = View.VISIBLE
                                  binding.unreadCounterText.text = unreadMessages.qty.toString()
                              } else {
                                  binding.unreadCounterText.visibility = View.GONE
                              }
                          }
                      }
                  }

              }


              jobIncommingMessages = lifecycleScope.launch(Dispatchers.Main) {
                  viewModel.messageIncomming.observe(this@MapSituationFragment) { resource ->

                      val chatEvent = resource.data

                      if (viewModel.isChatOpen.value == true) {
                          viewModel.resetUnreadMessages(
                              mainActivityViewModel.userKey.toString(), eventKeyShowing!!
                          )
                          lifecycleScope.launch(Dispatchers.Main) {
                              requireContext().playSound(R.raw.message_income)
                          }
                      }
                      when (chatEvent) {
                          is ChatRepository.ChatDataEvent.OnChildAdded -> {
                              val _message = chatEvent.data

                              var addMessage = true
                              if (!messages.contains(_message)) {


                                  if (_message.action != null) {
                                      val currentAction =
                                          SpeedMessageActions.valueOf(_message.action.actionType!!)
                                      when (currentAction) {
                                          SpeedMessageActions.GOING -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )
                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.NOT_GOING
                                              )
                                          }

                                          SpeedMessageActions.NOT_GOING -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )

                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.GOING
                                              )
                                          }

                                          SpeedMessageActions.CALLED -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )

                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.NOT_CALLED
                                              )
                                          }

                                          SpeedMessageActions.NOT_CALLED -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )

                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.CALLED
                                              )
                                          }

                                          SpeedMessageActions.IM_THERE -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )

                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.NOT_IN_THERE
                                              )
                                          }

                                          SpeedMessageActions.NOT_IN_THERE -> {
                                              _message.text = String.format(
                                                  getString(_message.action?.actionMessageResId!!),
                                                  _message.user.name
                                              )

                                              chatFragment?.turnActionInto(
                                                  currentAction, SpeedMessageActions.IM_THERE
                                              )
                                          }
                                      }

                                      if (_message.user.id.compareTo(FirebaseAuth.getInstance().uid.toString()) == 0) {
                                          addMessage = false
                                      }
                                  }

                                  if (addMessage) {

                                      if (viewModel.isChatOpen.value == false) {
                                          if (_message.createdAt.time > AppClass.instance.startTime) {
                                              lifecycleScope.launch(Dispatchers.Main) {
                                                  requireContext().playSound(R.raw.message_income)
                                              }
                                          }
                                      }


                                      if (_message.voice != null) {
                                          _message.voice.url =
                                              AppConstants.CHAT_FILES_STORAGE_PATH + eventKeyShowing + "/" + _message.voice.url.getJustFileName()

                                      }
                                      if (_message.image != null || _message.video != null || _message.voice != null) {
                                          _message.status = MessagesStatus.DOWNLOADING
                                      }
                                      messages.add(_message)
                                  }
                              }
                          }

                          is ChatRepository.ChatDataEvent.OnChildChanged -> {
                              var pp = 33
                          }

                          is ChatRepository.ChatDataEvent.OnChildMoved -> TODO()
                          is ChatRepository.ChatDataEvent.OnChildRemoved -> TODO()
                          is ChatRepository.ChatDataEvent.OnError -> TODO()
                          null -> {

                          }
                      }
                  }


              }

      */
        //------------------


        eventUpdatesJob?.start()
        viewersUpdatesJob?.start()

        visibleEventKey = eventKey

    }


    /**
     * Este metodo se ejecuta cuando se notifica que un evento ha sido cerrado
     */
    private val _onCloseEventAction = MutableLiveData<OnEventClosedActionsEnum?>(null)
    val onCloseEventAction: LiveData<OnEventClosedActionsEnum?> = _onCloseEventAction
    private fun onEventClosed(eventKey: String) {
        if (!AppClass.instance.eventsFollowed.isEmpty()) {
            _onCloseEventAction.postValue(OnEventClosedActionsEnum.LOAD_FIRST_EVENT)
        } else {
            _onCloseEventAction.postValue(OnEventClosedActionsEnum.GO_MAIN)
        }


    }


    fun startViewersUpdatesJob(eventKey: String) = viewModelScope.launch(Dispatchers.IO) {

        Log.d(
            "EVENT_FLOWS",
            "viewersUpdatesJob - Comienzo a seguir a  los seguidores del Evento = " + eventKey.toString()
        )
        /*
           eventFollowersFlow = liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {

               eventFollowersRepository.getEventFollowersFlow(eventKey)?.collect { update ->
                   //emit(update)
                   when (update) {
                       is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                           emit(Resource.Error(update.exception.localizedMessage.toString()))
                       }
                       null -> {
                           emit(Resource.Error("null"))
                       }

                       else -> {
                           emit(Resource.Success(update))
                       }
                   }

                   when (update) {
                       is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                           emit(Resource.Error(update.exception.localizedMessage.toString()))
                       }

                       null -> {
                           emit(Resource.Error("null"))
                       }

                       else -> {
                           emit(Resource.Success(update))
                       }
                   }
               }
           }
   */

        var pp = 33
    }


    /**
     * Metodo que se ejecuta cuando se notifica que hay un nuevo evento en la lista de seguidos
     * @param eventKey : String : Clave del evento
     *//*
        fun onNewEventFollowed(eventKey: String) {
    _
        }
    */
}