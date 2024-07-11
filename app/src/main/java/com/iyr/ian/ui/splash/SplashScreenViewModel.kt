package com.iyr.ian.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.models.User
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.ScreensEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.showErrorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SplashScreenViewModel(val messagingToken: String?) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _userRegistrationStatus = MutableLiveData<Resource<User?>>()
    val userRegistrationStatus: LiveData<Resource<User?>> get() = _userRegistrationStatus

    private val _screenToShow = MutableLiveData<ScreensEnum>()
    val screenToShow: LiveData<ScreensEnum> get() = _screenToShow

    private val repository = UsersRepositoryImpl()
    val usersRepository by lazy { UsersRepositoryImpl() }
    val subscriptionTypesRepository by lazy { SubscriptionTypeRepositoryImpl() }

    /***
     * Verifica si el usuario se encuentra registrado y devuelve los datos del usuario actual
     */
    fun checkIfIsUserLoggerIn() {
        _userRegistrationStatus.value = Resource.Loading<User?>()
        viewModelScope.launch(Dispatchers.Main) {
            val registerResult = repository.getUserLoginStatus()
            if (registerResult.message == null) {
                var authResponse = Resource.Success(registerResult.data!!)
                var user = repository.getUserRemote(authResponse.data?.uid.toString())
                _userRegistrationStatus.value = user
            } else {
                _userRegistrationStatus.value = Resource.Error(registerResult.message)
            }
        }
    }


    fun onAppStart() {
        _userRegistrationStatus.value = Resource.Loading<User?>()
        viewModelScope.launch(Dispatchers.Main) {
            val registerResult = repository.getUserLoginStatus()
            //    _userRegistrationStatus.value = Resource.Error(registerResult.message)
            if (registerResult.message == null) {
                var userResponse = Resource.Success(registerResult.data!!)

                var userDataRemote: User? =
                    repository.getUserRemote(userResponse.data!!.uid.toString()).data

                _user.value = userDataRemote
                //    _user.value?.user_key = userResponse.data!!.uid.toString()

                var isUserCompleted = false
                userDataRemote?.let { data ->

                    isUserCompleted =
                        !data.display_name.isNullOrEmpty() &&
                                data.image.file_name != null &&
                                data.allow_speed_dial != null &&
                                data.sos_invocation_count != null &&
                                data.sos_invocation_count >= 3 &&
                                data.sos_invocation_method != null


                    _userRegistrationStatus.postValue(Resource.Success(userDataRemote))

                    if (isUserCompleted) {
                        //                      mCallback.onOkToMainScreen(user)
                        _screenToShow.postValue(ScreensEnum.Main)
                    } else {
//                        mCallback.onProfileIncomplete(user)
                        //_screenToShow.value = ScreensEnum.setupActivity

                        //--------------

                        if (data.display_name.isNullOrBlank() || (data.telephone_number.isNullOrEmpty() && data.email_address.isEmpty())) {
                            _screenToShow.value = ScreensEnum.setupActivity
                        } else if (data.sos_invocation_count == 0 || data.sos_invocation_method.isNullOrEmpty()) {
                            _screenToShow.value = ScreensEnum.Press_or_Tap_Setup_Activity
                        } else if (data.security_code.isBlank()) {
                            _screenToShow.value = ScreensEnum.Pin_Setup_Activity
                        } else if (data.allow_speed_dial == null) {
                            _screenToShow.value = ScreensEnum.Speed_Dial_Setup_Activity
                        } else if (data.sos_invocation_count == null || data.sos_invocation_count < 3) {
                            _screenToShow.value = ScreensEnum.Press_or_Tap_Setup_Activity
                        } else {
                            _screenToShow.value = ScreensEnum.Check_Permissions
                        }
                        /*
                        else {
                                Intent(this@SplashActivity, AddContactsFromPhoneActivity::class.java)
                            }
*/
                        //-------------------
                    }

                }


            } else if (!messagingToken.isNullOrEmpty())
                _screenToShow.value = ScreensEnum.Login
            else
                _screenToShow.value = ScreensEnum.Preloader
        }
    }


    private val _progress = MutableLiveData<Int?>()
    val progress: LiveData<Int?> = _progress

    private val _progressText = MutableLiveData<String?>()
    val progressText: LiveData<String?> = _progressText

    private val _totalTasks = MutableLiveData<Int?>()
    val totalTasks: LiveData<Int?> = _totalTasks

    private val _serverTime = MutableLiveData<Long?>()
    val serverTime: LiveData<Long?> = _serverTime

    private val _readyToStart = MutableLiveData<User?>()
    val readyToStart: LiveData<User?> = _readyToStart


    fun prepareForMainScreen(user: User) {

        suspend fun getServerTime(): Resource<Long?> {
            withContext(Dispatchers.Main) {
//                _progressText.postValue("Obteniendo hora del servidor")

                withContext(Dispatchers.IO) {
                    val rootRef = FirebaseDatabase.getInstance().reference
                    rootRef.child("serverTime").setValue(ServerValue.TIMESTAMP)
                    rootRef.child("serverTime").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val serverTime = dataSnapshot.getValue(Long::class.java)
                            println("Server time: $serverTime")
                            _serverTime.postValue(serverTime)
                            Resource.Success(serverTime)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            println("Error: ${databaseError.message}")
                            Resource.Error<Long?>(databaseError.message)
                        }
                    })
                }
            }

            return Resource.Loading<Long?>(0)
        }

        suspend fun getSubscriptionPlan(): Resource<SubscriptionTypes?> {
//            withContext(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val subscriptionPlan =
                        subscriptionTypesRepository.getSubscriptionType(user.subscription_type_key)
                    if (subscriptionPlan.data != null) {
                        AppClass.instance.setUserSubscription(subscriptionPlan.data!!)
                        Resource.Success(subscriptionPlan.data)
                    } else {

                        var call = subscriptionTypesRepository.getSubscriptionTypeByAccessLevel(
                            AccessLevelsEnum.FREE
                        )
                        when (call) {
                            is Resource.Error -> {
                                AppClass.instance.getCurrentActivity()
                                    ?.showErrorDialog(call.message.toString())
                            }

                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                var subscriptionType = call.data!!
                                var subscriptionTypeKey =
                                    subscriptionType.subscription_type_key // ponerle la basica.
                                AppClass.instance.setUserSubscription(subscriptionType)
                                usersRepository.updateSubscriptionType(
                                    FirebaseAuth.getInstance().uid.toString(),
                                    subscriptionType.subscription_type_key
                                )
                                Resource.Error<SubscriptionTypes?>("Error obteniendo el tipo de subscripcion")

                            }
                        }

                    }
                }

  //          }
            return Resource.Loading<SubscriptionTypes?>(null)
        }


        suspend fun getFollowingEvents() = AppClass.instance.getEventsFollowingAll(user.user_key)

        val tasksTitles = listOf(
            "Obteniendo hora del servidor",
            "Obteniendo plan de suscripción",
            "Obteniendo Eventos actuales"
        )
        val tasks = listOf(::getServerTime, ::getSubscriptionPlan, ::getFollowingEvents)

        viewModelScope.launch(Dispatchers.IO) {
            val totalTasks = tasks.size
            _totalTasks.postValue(totalTasks)

            tasks.forEachIndexed { index, task ->
                withContext(Dispatchers.Main) {
                    _progressText.postValue(tasksTitles[index])
                }
                var result = task()
                _progress.postValue(index + 1)
            }
            _readyToStart.postValue(user)
            // Continúa aquí después de que todas las tareas hayan terminado
        }

    }
}