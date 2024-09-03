package com.iyr.ian.ui.map.infowindow

import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventFollowersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoWindowData() : EventFollower() {

    var telephoneNumber: String? = null

}

class InfoWindowViewModel private constructor() : ViewModel() {

    private lateinit var context: Context
    private val _infoWindowData = MutableLiveData<InfoWindowData>()
    val infoWindowData: LiveData<InfoWindowData> = _infoWindowData

    val eventFollowersRepository = EventFollowersRepositoryImpl()
    val usersRepository = UsersRepositoryImpl()


    companion object {
        private lateinit var instance: InfoWindowViewModel

        @MainThread
        fun getInstance(paramContext: Context): InfoWindowViewModel {
            instance = if (Companion::instance.isInitialized) instance
            else
                InfoWindowViewModel().apply {
                    context = paramContext!!
                }
            return instance
        }
    }

    fun onConnectToFollower(eventKey: String, userKey: String) {
        viewModelScope.launch(Dispatchers.IO) {

            eventFollowersRepository.followFollowerFlow(eventKey, userKey).collect {
                when (it) {
                    is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                        val followerInfo = it.data
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
                                    if (userData?.telephone_number != null)
                                        telephoneNumber = userData!!.telephone_number.toString()
                                    else
                                        telephoneNumber =
                                            context.getString(R.string.no_phone_number)
                                }
                            }
                        }
                        updateInfoWindowData(
                            infoWindowData
                        )
                    }

                    is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {

                        val pepe = 33
                        /*
                        updateInfoWindowData(
                            infoWindowData
                        )

                     */
                    }

                    is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> TODO()
                    is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                        Log.e("InfoWindowViewModel", "Error: ${it.exception.message}")
                    }

                    null -> TODO()
                    else -> {}
                }
            }
        }
    }


    fun updateInfoWindowData(data: InfoWindowData) {
        _infoWindowData.postValue(data)
    }
}