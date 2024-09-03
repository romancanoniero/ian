package com.iyr.ian.viewmodels

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.dao.repositories.EventRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventFollowersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.map.infowindow.InfoWindowData
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.lassi.domain.common.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapSituationFragmentViewModel private constructor() : ViewModel(), LifecycleObserver {

    enum class OnEventClosedActionsEnum {
        LOAD_FIRST_EVENT, GO_MAIN
    }

    companion object {
        private lateinit var instance: MapSituationFragmentViewModel

        @MainThread
        fun getInstance(): MapSituationFragmentViewModel {
            instance = if (::instance.isInitialized) instance
            else MapSituationFragmentViewModel()
            return instance
        }
    }

    val usersRepository = UsersRepositoryImpl()
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


    // ver si no se puede simplificar
    /*
      private val _currentEventKey = MutableLiveData<Resource<String>?>()
      val currentEventKey: LiveData<Resource<String>?> get() = _currentEventKey
  */

    private val _auxEventKey = SingleLiveEvent<String?>()
    val auxEventKey: LiveData<String?> = _auxEventKey
    var currentEventKey: String = _auxEventKey.value ?: ""


    private val _subscriptedEvents = MutableLiveData<EventRepository.EventDataEvent>()
    val subscriptedEvents: LiveData<EventRepository.EventDataEvent> = _subscriptedEvents


    // --- MAP
    /*
      private val _resetMap = MutableLiveData<String>()
      val resetMap: LiveData<String> = _resetMap
  */
    // --- CHAT BUTTON -------

    private val _isChatButtonVisible = MutableLiveData<Boolean?>()
    val isChatButtonVisible: LiveData<Boolean?> = _isChatButtonVisible

    fun chatOpenButtonVisibility(visible: Boolean) {
        _isChatButtonVisible.postValue(visible)
    }

    private val _isChatOpen = MutableLiveData<Boolean?>(false)
    val isChatOpen: LiveData<Boolean?> = _isChatOpen

    /**
     * Cuando la ventana de chat esta cerrada
     */
    fun onChatClosed() {
        //      _isChatButtonVisible.postValue(true)
        _isCounterVisible.postValue(true)
        _isChatOpen.postValue(false)
        messages?.value?.clear()
    }

    // --- TIME COUNTER

    private val _isCounterVisible = MutableLiveData<Boolean?>(true)
    val isCounterVisible: LiveData<Boolean?> = _isCounterVisible


    // --- CHAT
    /*
        private val _showChatFragment = MutableLiveData<Boolean?>()
        val showChatFragment: LiveData<Boolean?> = _showChatFragment
    */

    /*
        private val _messageFragmentMode = MutableLiveData<ChatWindowStatus?>()
        val messageFragmentMode: LiveData<ChatWindowStatus?> = _messageFragmentMode
    */
    // --- --- ---

    val followersList = ArrayList<EventFollower>()
    private val _followers = MutableLiveData<ArrayList<EventFollower>>(ArrayList<EventFollower>())
    val followers: LiveData<ArrayList<EventFollower>> = _followers


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


    var eventJob: Job? = null
    var eventUpdatesJob: Job? = null
    var viewersUpdatesJob: Job? = null
    var messagesFlowJob: Job? = null


    //-------  Live data relativa a los eventos.
    //---- MEnsajes
    /***
     * Ultima actualizacion del evento
     */
    var lastEventUpdate = Event()

    /***
     * LiveData que contiene la informacion del evento
     */
    private val _eventFlow = MutableLiveData<Resource<Event>>()
    val eventFlow: LiveData<Resource<Event>> get() = _eventFlow

    //---- Mensajes
//    private var messages = ArrayList<Message>() // array de mensajes recibidos
    private val _messages = MutableLiveData<ArrayList<Message>>(ArrayList<Message>())
    val messages: LiveData<ArrayList<Message>> = _messages

    /*
    private val _conversation = MutableLiveData<ArrayList<Message>>(ArrayList<Message>())
    val conversation: LiveData<ArrayList<Message>> = _conversation
*/

    private val _messageIncomming = MutableLiveData<Resource<ChatRepository.ChatDataEvent>?>()
    val messageIncomming: LiveData<Resource<ChatRepository.ChatDataEvent>?> = _messageIncomming


    private val _shimmmerVisible = MutableLiveData<Boolean?>()
    val shimmmerVisible: LiveData<Boolean?> = _shimmmerVisible

    /**
     *  Connecta al evento y a sus dependencias
     *  @param eventKey : String : Clave del evento
     */
    fun connectToEvent(eventKey: String) {
        Log.d("CHAT_FLOW", "1 - Conecto al evento = " + eventKey)
        if (eventKey == currentEventKey) return

        _shimmmerVisible.postValue(true)
        disconnectFromEvent()

        Log.d("EVENT_FLOWS", "Conecto al evento = " + eventKey)

        eventJob = viewModelScope.launch(Dispatchers.IO) {
            currentEventKey = eventKey

            //-- Escucho al evento
            launch {
                listenEventFlow()
            }
            //-- Escucho el flow de chats
            launch {
                listenChatFlow()
            }

            //-- Escucho el flow de chats
            launch {
                listenUnreadMessagesFlow()
            }

            launch {
                listenFollowersFlow()
            }

            // actualizo el _auxEventKey para el resto de los observers
            _auxEventKey.postValue(eventKey)

        }
        eventJob?.start()
    }


    private suspend fun listenEventFlow() {
        eventRepository.listenEventFlow(currentEventKey)?.collect { resource ->
            when (resource) {
                is Resource.Error -> {
                    _shimmmerVisible.postValue(false)
                    _spinner.postValue(false)
                }

                is Resource.Loading -> {
                    _spinner.postValue(true)
                }

                is Resource.Success -> {
                    _shimmmerVisible.postValue(false)

                    var event: Event? = resource.data
                    _spinner.postValue(false)

                    if (event == null) {
                        AppClass.instance.onEventRemoved(currentEventKey)
                        onDisconnectionRequested()
                        onEventClosed(currentEventKey)
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

    private suspend fun listenFollowersFlow() {
        println("Current thread: ${Thread.currentThread().name}")
        // -- Primero busco todos los registros de seguidores para luego seguir escuchandolos
  //      Log.d("FOLLOWERS", "Busco los followers del evento")
        val followersListCall = eventFollowersRepository.getEventFollowers(currentEventKey)
    //    Log.d("FOLLOWERS", "Obtuve los followers del evento")
        val followers: ArrayList<EventFollower> = _followers.value!!
        if (followersListCall is Resource.Success) {

            followersListCall.data?.forEach { follower ->
                if (followers.contains(follower) == true) {
                    return@forEach
                }
      //          Log.d("FOLLOWERS", "Agrego un follower del evento")
                _eventFollowersFlow.postValue(
                    Resource.Success(
                        EventFollowersRepository.EventFollowerDataEvent.OnChildAdded(
                            follower,
                            null
                        )
                    )
                )
                followers.add(follower)
            }
            _followers.postValue(followers)
        }
        //--- Escucho el flow de seguidores
        eventFollowersRepository.getEventFollowersFlow(currentEventKey)?.collect { update ->
            //emit(update)
            when (update) {
                is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                    _eventFollowersFlow.postValue(Resource.Error(update.exception.localizedMessage.toString()))
                }

                null -> {
                    _eventFollowersFlow.postValue(Resource.Error("null"))
                }

                else -> {
                    _eventFollowersFlow.postValue(Resource.Success(update))

                    val followers: ArrayList<EventFollower> = _followers.value!!

                    when (update) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                            val follower = update.data
                            if (followers.contains(follower) == true) {
                                return@collect
                            }
                            followers.add(follower)
                            _followers.postValue(followers)
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                            val follower = update.data
                            followers.forEachIndexed { index, eventFollower ->
                                if (eventFollower.user_key == follower.user_key) {
                                    followers.set(index, follower)
                                    _followers.postValue(followers)

                                }
                            }
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> {
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
                            val follower = update.data
                            followers.forEachIndexed { index, eventFollower ->
                                if (eventFollower.user_key == follower.user_key) {
                                    followers.removeAt(index)
                                    _followers.postValue(followers)
                                }
                            }
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                            // TODO: 10/09/2021 - Crear en mainactivity un livedata para los errores

                            MainActivityViewModel.getInstance()
                                .onError(update.exception.localizedMessage.toString())
                        }
                    }
                    //     eventFollowersConnector.emit(update)

                }
            }


        }
    }

     suspend fun listenChatFlow() {
        Log.d("CHAT_FLOW", "3 - Conecto al evento  desde ListenChatFlow= " + currentEventKey)
        chatsRepository.getChatFlow(currentEventKey)?.collect { update ->
            Log.d("EVENT_FLOWS", "empiezo a recibir chats del evento " + currentEventKey)
            var texto =
                (update.data!! as ChatRepository.ChatDataEvent.OnChildAdded).data.text
            Log.d("FLOW_VIEWMODEL", "emito el registo = ${texto}")

            val messagesArray = messages.value!!
            when (update.data) {
                is ChatRepository.ChatDataEvent.OnChildAdded -> {
                    val message = update.data.data
                    if (!messagesArray.contains(message)) {
                       if (message.action == null || (message.action!=null && message.user.id != UserViewModel.getInstance().getUser()?.user_key)) {
                           // Si el mensaje es una accion y no es del usuario actual
                           // lo agrego a la lista de mensajes
                           messagesArray.add(message)
                           _messages.postValue(messagesArray)
                       }
                    }
                }

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

    private val _unreadMessages = MutableLiveData<Long>()
    val unreadMessages: LiveData<Long> = _unreadMessages
    private suspend fun listenUnreadMessagesFlow() {
        Log.d("CHAT_FLOW_UNREADS", "3 - Conecto al evento  desde ListenChatFlow= " + currentEventKey)
        val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
        chatsRepository.unreadMessagesFlow(userKey, currentEventKey)?.collect { value ->
            Log.d("EVENT_FLOWS", "empiezo a recibir chats del evento " + currentEventKey)
            _unreadMessages.postValue(value)
            /*
                var texto =
                    (update.data!! as ChatRepository.ChatDataEvent.OnChildAdded).data.text
                Log.d("FLOW_VIEWMODEL", "emito el registo = ${texto}")

                when (update.data) {
                    is ChatRepository.ChatDataEvent.OnChildAdded -> {
                        val message = update.data.data
                        if (!messages.contains(message)) {
                            messages.add(message)
                        }
                    }

                    is ChatRepository.ChatDataEvent.OnChildChanged -> {}
                    is ChatRepository.ChatDataEvent.OnChildMoved -> {}
                    is ChatRepository.ChatDataEvent.OnChildRemoved -> {}
                    is ChatRepository.ChatDataEvent.OnError -> {}
                }

                viewModelScope.launch(Dispatchers.Main) {
                    _messageIncomming.setValue(update)
                }

             */
        }

    }



    /*
        fun onConnectToChatFlow(): LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
            liveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>(Dispatchers.IO) {
                Log.d("CHAT_FLOW", "2 - Conecto al evento = " + currentEventKey)

                chatsRepository.getChatFlow(currentEventKey)?.collect { update ->

                    var texto = (update.data!! as ChatRepository.ChatDataEvent.OnChildAdded).data.text
                    Log.d("FLOW_VIEWMODEL", "emito el registo = ${texto}")
    //                    _chatUpdatesFlow.emit(update)

                    when (update.data) {
                        is ChatRepository.ChatDataEvent.OnChildAdded -> messages?.value?.add(update.data.data)
                        is ChatRepository.ChatDataEvent.OnChildChanged -> {}
                        is ChatRepository.ChatDataEvent.OnChildMoved -> {}
                        is ChatRepository.ChatDataEvent.OnChildRemoved -> {}
                        is ChatRepository.ChatDataEvent.OnError -> {}
                        else -> {}
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        _messageIncomming.setValue(update)
                    }
                }
            }
    */





    // ---- desconexion de eventos
    private val _resetEvent = MutableLiveData<Boolean?>(false)
    val resetEvent: LiveData<Boolean?> = _resetEvent
    private fun disconnectFromEvent() {
        eventJob?.let { job ->
            Log.d("EVENT_FLOWS", "Cancelo el seguimiento al Evento = " + visibleEventKey)
            job.cancel()
            //-- Evento
            _resetEvent.postValue(true)
            _followers.postValue(ArrayList())
            //-- Chat
            messages?.value?.clear()
       //     _conversation.postValue(ArrayList<Message>())
            _messageIncomming.postValue(null)
        }
    }


    fun setEventTimeOfLastView(userKey: String, eventKey: String) {
        eventTimeOfLastViewJob?.cancel()
        eventTimeOfLastViewJob = viewModelScope.launch(Dispatchers.IO) {

            val timeInMillis =
                eventRepository.postLastTimeSeen(userKey, eventKey).data ?: "0".toLong()
        }
    }
/*
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
*/

 //   fun getMessages() = messages

    suspend fun getEventObject(eventKey: String): Event? {
        return eventRepository.getEvent(eventKey).data
    }


    fun onDisconnectionRequested() {
        if (eventUpdatesJob != null) {
            eventUpdatesJob?.cancel()
        }


        if (viewersUpdatesJob != null) {
            viewersUpdatesJob?.cancel()
        }
        messages?.value?.clear()
    }/*
        fun hideChatFragment() {
            _showChatFragment.postValue(false)
        }

        fun showChatFragment() {
            _showChatFragment.postValue(true)
        }
    */

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
     *//*
      fun onMessageRead(messageKey: String) {
          mainActivityViewModel.onMessageRead(eventKey.toString(), messageKey)
      }
  */


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

    private val _eventFollowersFlow =
        MutableLiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>()
    val eventFollowersFlow: LiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>> =
        _eventFollowersFlow
    private val eventFollowerLiveDataMerger =
        MediatorLiveData<Resource<EventFollowersRepository.EventFollowerDataEvent?>>().apply {
            addSource(eventFollowersFlow) { resource ->
                if (infoWindowKey != null) {
                    value = resource
                }
            }
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

    /*
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
    */
    fun onResetEventDone() {
        _resetEvent.postValue(false)
    }

    private val _windowInfo = MutableLiveData<HashMap<String, Any>>()
    val windowInfo: LiveData<HashMap<String, Any>> = _windowInfo
    fun onScreenDimensionsChanged(infoMap: HashMap<String, Any>) {
        _windowInfo.postValue(infoMap)
    }

    private val _infoWindowData = MutableLiveData<InfoWindowData>()
    val infoWindowData: LiveData<InfoWindowData> = _infoWindowData
    var infoWindowKey: String? = null

    fun onConnectToInfoWindowData(eventKey: String, userKey: String) {

        val followerInfo = followers.value?.find { it.user_key == userKey }
        if (followerInfo != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val newData = InfoWindowData().apply {
                    user_key = followerInfo.user_key
                    display_name = followerInfo.display_name
                    profile_image_path = followerInfo.profile_image_path
                    l = followerInfo.l
                    battery_percentage = followerInfo.battery_percentage

                    val callUser = usersRepository.getUserRemote(userKey)


                    when (callUser) {
                        is Resource.Error -> telephoneNumber = "Error"
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val userData = callUser.data
                            if (userData?.telephone_number != null) telephoneNumber =
                                userData!!.telephone_number.toString()
                            else telephoneNumber = null
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    _infoWindowData.postValue(newData)
                }
            }
        }
        eventFollowerLiveDataMerger.observeForever { resource ->
            Log.d("INFO_WINDOW", "Recibo datos del seguidor")
            when (resource) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val eventData = resource.data
                    when (eventData) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                            //        TODO()
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {

                            viewModelScope.launch(Dispatchers.IO) {
                                val followerInfo = eventData.data
                                var newData: InfoWindowData = InfoWindowData()
                                if (_infoWindowData.value?.user_key != followerInfo.user_key) {
                                    newData = InfoWindowData().apply {
                                        user_key = followerInfo.user_key
                                        display_name = followerInfo.display_name
                                        profile_image_path = followerInfo.profile_image_path
                                        l = followerInfo.l
                                        battery_percentage = followerInfo.battery_percentage
                                        val callUser = usersRepository.getUserRemote(userKey)
                                        when (callUser) {
                                            is Resource.Error -> telephoneNumber = "Error"
                                            is Resource.Loading -> {}
                                            is Resource.Success -> {
                                                val userData = callUser.data
                                                if (userData?.telephone_number != null) telephoneNumber =
                                                    userData!!.telephone_number.toString()
                                                else telephoneNumber = null
                                            }
                                        }
                                    }
                                } else {
                                    newData = _infoWindowData.value!!
                                    newData?.apply {
                                        l = followerInfo.l
                                        battery_percentage = followerInfo.battery_percentage
                                    }
                                }

                                launch(Dispatchers.Main) {
                                    _infoWindowData.postValue(newData)
                                }

                            }

                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> TODO()
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> TODO()
                        is EventFollowersRepository.EventFollowerDataEvent.OnError -> TODO()
                        null -> TODO()
                    }
                    /*
                    when (val eventData = resource.data) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> { it: EventFollower ->

                            val followerInfo = it

                            viewModelScope.launch(Dispatchers.IO) {
                                val infoWindowData = InfoWindowData().apply {
                                    user_key = followerInfo.user_key
                                    display_name = followerInfo.display_name
                                    profile_image_path = followerInfo.profile_image_path
                                    this.l = followerInfo.l

                                    val callUser = usersRepository.getUserRemote(userKey)
                                    when (callUser) {
                                        is Resource.Error -> telephoneNumber = "Error"
                                        is Resource.Loading -> {}
                                        is Resource.Success -> {
                                            val userData = callUser.data
                                            if (userData?.telephone_number != null) telephoneNumber =
                                                userData!!.telephone_number.toString()
                                            else telephoneNumber = null
                                        }
                                    }
                                }
                                _infoWindowData.postValue(infoWindowData)/*
                                    updateInfoWindowData(
                                        infoWindowData
                                    )*/


                            }

                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> { it ->

                            val data = eventData.data
                            val followerInfo = data

                            viewModelScope.launch(Dispatchers.IO) {
                                val infoWindowData = InfoWindowData().apply {
                                    user_key = followerInfo.user_key
                                    display_name = followerInfo.display_name
                                    profile_image_path = followerInfo.profile_image_path
                                    this.l = followerInfo.l

                                    val callUser = usersRepository.getUserRemote(userKey)
                                    when (callUser) {
                                        is Resource.Error -> telephoneNumber = "Error"
                                        is Resource.Loading -> {}
                                        is Resource.Success -> {
                                            val userData = callUser.data
                                            if (userData?.telephone_number != null) telephoneNumber =
                                                userData!!.telephone_number.toString()
                                            else telephoneNumber = null
                                        }
                                    }
                                }
                                _infoWindowData.postValue(infoWindowData)/*
                                updateInfoWindowData(
                                    infoWindowData
                                )*/


                            }

                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> TODO()
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> TODO()
                        is EventFollowersRepository.EventFollowerDataEvent.OnError -> TODO()
                        null -> {

                        }
                    }
                    */
                }
            }
        }
    }

    fun onMarkerClicked(markerKey: String) {
        infoWindowKey = markerKey

        viewModelScope.launch(Dispatchers.IO) {
            val followerInfo = followers.value?.find { it.user_key == markerKey }
            if (followerInfo != null) {
                val newData = InfoWindowData().apply {
                    user_key = followerInfo.user_key
                    display_name = followerInfo.display_name
                    profile_image_path = followerInfo.profile_image_path
                    l = followerInfo.l
                    battery_percentage = followerInfo.battery_percentage

                    val callUser = usersRepository.getUserRemote(markerKey)
                    when (callUser) {
                        is Resource.Error -> telephoneNumber = "Error"
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val userData = callUser.data
                            telephoneNumber = userData?.telephone_number?.toString()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    _infoWindowData.postValue(newData)
                }
            }
        }
    }


    fun resetInfoWindowData() {
        infoWindowKey = null
    }


}