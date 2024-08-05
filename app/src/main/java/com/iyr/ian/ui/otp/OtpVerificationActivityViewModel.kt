package com.iyr.fewtouchs.ui.views.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.implementations.auth.firebase.AuthenticationRepositoryImpl
import com.iyr.ian.enums.UserTypesEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.otp.OTPActionsEnum
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OtpVerificationActivityViewModel : ViewModel() {


    private var authRepository: AuthenticationRepositoryImpl = AuthenticationRepositoryImpl()
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    fun onOTPIntroduced(
        action: OTPActionsEnum,
        credential: PhoneAuthCredential,
        storedVerificationId : String,
        code : String
//        , userDataMap: HashMap<String, String>
    ) {

        viewModelScope.launch(Dispatchers.IO) {


            try {
/*
                val signInResult =
                    FirebaseAuth.getInstance().signInWithCredential(credential).await()
*/
                var  credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                val signInResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
//                Toast.makeText(this, "Verifying...", Toast.LENGTH_SHORT).show();

                when (action) {

                    OTPActionsEnum.SIGNING -> {
                        _currentUser.postValue(Resource.Loading())
                        val authToken = authRepository.getAuthToken().data
                        val user = User(UserTypesEnum.COMMON_USER.name)
                        user.user_key = signInResult.user?.uid.toString()
                        user.telephone_number = signInResult.user?.phoneNumber.toString()
                        try {
                            var call = usersRepository.insertUser(user)
                            _currentUser.postValue(Resource.Success(user))
                        } catch (exception: Exception) {
                            _currentUser.postValue(Resource.Error(exception.localizedMessage.toString()))
                        }

                        /*
                        TODO : Arreglar esto
                        UsersWSClient.instance.getAuthToken(object : OnCompleteCallback {
                            override fun onComplete(
                                success: Boolean,
                                result: Any?
                            ) {

                                SessionForProfile.getInstance(mActivity)
                                    .removeProfileProperty("PENDING_PHONE_NUMBER")
                                SessionForProfile.getInstance(mActivity)
                                    .removeProfileProperty(user.telephone_number.toString())


                                UsersWSClient.instance.createUserOnStorage(
                                    user,
                                    object : OnCompleteCallback {
                                        override fun onComplete(
                                            success: Boolean,
                                            result: Any?
                                        ) {
                                            if (success) {
                                                SessionForProfile.getInstance(AppClass.instance)
                                                    .storeUserProfile(user)
                                                mCallback.loginSuccessfully(user)
                                            } else {
                                                mCallback.onError(task.exception!!)
                                            }
                                        }

                                        override fun onError(exception: Exception) {
                                            super.onError(exception)
                                            mCallback.onError(exception)
                                        }
                                    })

                            }
                        })

                         */
                    }
                    OTPActionsEnum.LOGIN -> {
                        _currentUser.postValue(Resource.Loading())

                        val call = usersRepository.getUserRemote(signInResult.user?.uid.toString())
                        _currentUser.postValue(Resource.Success(call.data))


                    }
                }


            } catch (exception: Exception) {
                _currentUser.postValue(Resource.Error(exception.localizedMessage.toString()))
            }

            /*
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {


                                    //                    mCallback.loginSuccessfully()
                                    // ...


                                } else {
                                    // Sign in failed, display a message and update the UI
                                    Log.w(
                                        "SIGNUPPHONEVERIFICATION",
                                        "signInWithCredential:failure",
                                        task.exception
                                    )
                                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                        // The verification code entered was invalid
                                        mCallback.onError((task.exception as FirebaseAuthInvalidCredentialsException))
                                    }
                                }
            */
        }


    }

}