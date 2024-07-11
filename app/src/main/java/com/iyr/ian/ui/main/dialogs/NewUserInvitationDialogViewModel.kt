package com.iyr.ian.ui.main.dialogs

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.AppConstants.Companion.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.SMSUtils
import com.iyr.ian.utils.SMSUtils.sendSMS
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class InputStatus {
    OK,
    IS_YOUR_NUMBER,
    USER_ALREADY_REGISTERED,
    USER_DOES_NOT_EXIST

}

class NewUserInvitationDialogViewModel(val userKey : String) : ViewModel() {

    private var context: Context = AppClass.instance

    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()
    private var contactsRepository: ContactsRepositoryImpl = ContactsRepositoryImpl()

    private val _inputStatus = MutableLiveData<InputStatus>()
    val inputStatus: LiveData<InputStatus> get() = _inputStatus

    private var _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> get() = _displayName

    private var _phoneNumber = MutableLiveData<String?>()
    val phoneNumber: LiveData<String?> get() = _phoneNumber


    private val _invitationActionStatus = MutableLiveData<Resource<Boolean>>()
    val invitationActionStatus: LiveData<Resource<Boolean>> get() = _invitationActionStatus

    private val _searchByPhoneNumberStatus = MutableLiveData<Resource<ArrayList<User?>>>()
    val searchByPhoneNumberStatus: LiveData<Resource<ArrayList<User?>>> get() = _searchByPhoneNumberStatus

    private val _telephoneAddedToPendingList = MutableLiveData<Boolean>()
    val telephoneAddedToPendingList: LiveData<Boolean> get() = _telephoneAddedToPendingList


    fun onAcceptButtonClicked(userWhoInvites: User, phoneNumber: String) {

        viewModelScope.launch(Dispatchers.IO) {


            if ((userWhoInvites.telephone_number?.toString() ?: "******").contains(phoneNumber.toString()) == false) {

                _invitationActionStatus.postValue(Resource.Loading<Boolean>())
                try {
                    var call = usersRepository.listUsersByPhoneNumber(phoneNumber.toString())
                    if (call.data?.size ?: 0 >= 1) {
                        var existingUser = call.data!![0]
                        try {
                            var postCall =
                                contactsRepository.postContactInvitation(userKey, existingUser?.user_key.toString())
                            if (postCall.message == null)
                                _invitationActionStatus.postValue(Resource.Success<Boolean>(true))
                            else
                                _invitationActionStatus.postValue(Resource.Error<Boolean>(postCall.message.toString()))
                        } catch (exception: Exception) {
                            _invitationActionStatus.postValue(Resource.Error<Boolean>(exception.message.toString()))
                        }
                    } else {
                        try {
                            var pendingCall = contactsRepository.putOnPendingInvitationList(
                                userWhoInvites.user_key,
                                phoneNumber
                            )

                            Log.d("CONTACT_CONNECTION", "Voy a generar el shortlink")
                            var shortLink = SMSUtils.generateShortLink(
                                context,
                                userWhoInvites,
                                DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL
                            )

                            Log.d(
                                "CONTACT_CONNECTION",
                                "Voy a generar el shortlink = " + shortLink.data
                            )

                            var invitationMessage =
                                generateInvitationText(userWhoInvites, shortLink.data.toString())

                            if (context.sendSMS(phoneNumber, invitationMessage)) {
                                _invitationActionStatus.postValue(Resource.Success<Boolean>(true))
                            }
                        } catch (exception: Exception) {
                            _invitationActionStatus.postValue(Resource.Error<Boolean>(exception.localizedMessage.toString()))
                        }
                    }


                } catch (exception: Exception) {
                    //  _userAlreadyRegistered.postValue(false)
                    _searchByPhoneNumberStatus.postValue(
                        Resource.Error<ArrayList<User?>>(
                            exception.localizedMessage?.toString() ?: "Unknown Error"
                        )
                    )
                }
            } else {

            }

        }


    }


    /**
     * Genera el texto del mensaje de invitacion para los usuarios que no estan registrados.
     *
     * @param user
     * @return
     */
    private fun generateInvitationText(user: User, shortLink: String): String {

        var userName = ""
        if (user.first_name != null && user.last_name != null) {
            userName = user.first_name + " " + user.last_name

        } else
            if (user.first_name != null) {
                userName = user.first_name + " " + user.last_name

            } else
                userName = user.display_name

        var invitationText = String.format(
            context.getText(R.string.app_contact_request_notification_message)
                .toString(), userName
        )

        return invitationText.plus(shortLink)

    }

    fun searchByPhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchByPhoneNumberStatus.postValue(Resource.Loading<ArrayList<User?>>())
            try {
                var call = usersRepository.listUsersByPhoneNumber(phoneNumber)
                var telephoneExists: Boolean = false
                if (call.data?.size == 1) {
                    with(call.data)
                    {
                        _phoneNumber.postValue(call.data?.get(0)?.telephone_number)
                        _displayName.postValue(call.data?.get(0)?.display_name)
                        telephoneExists = true
                    }
                }
                if (call.data?.size ?: 0 >= 1)
                    _inputStatus.postValue(InputStatus.USER_ALREADY_REGISTERED)
                else
                    _inputStatus.postValue(InputStatus.USER_DOES_NOT_EXIST)


                _searchByPhoneNumberStatus.postValue(Resource.Success<ArrayList<User?>>(call.data))
                //_userAlreadyRegistered.postValue(telephoneExists)

            } catch (exception: Exception) {
                //_userAlreadyRegistered.postValue(false)
                _searchByPhoneNumberStatus.postValue(
                    Resource.Error<ArrayList<User?>>(
                        exception.localizedMessage?.toString() ?: "Unknown Error"
                    )
                )
            }
        }

    }

    fun setInputStatus(status: InputStatus) {
        _inputStatus.postValue(status)
    }


}