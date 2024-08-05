package com.iyr.ian.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.implementations.auth.firebase.AuthenticationRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.login.LoginMethodsEnum
import com.iyr.ian.ui.otp.OTPActionsEnum
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getImageFromUrl
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.saveImageToCache
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

//, val listener: OnSignInStartedListener
class LoginActivityViewModel(val context: Context) : ViewModel() {

    private var isEmailValid: Boolean = false

    internal var emailAddress: String = ""
    internal var password: String = ""
    internal var phoneNumber: String = ""

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _currentUser = MutableLiveData<FirebaseUser>()
    val currentUser: LiveData<FirebaseUser> = _currentUser


    private val _loginButtonEnabled = MutableLiveData<Boolean>()
    val loginButtonEnabled: LiveData<Boolean> get() = _loginButtonEnabled

    private val _isValidPhoneNumber = MutableLiveData<Boolean>()
    val isValidPhoneNumber: LiveData<Boolean> get() = _isValidPhoneNumber

    private val _isValidPassword = MutableLiveData<Boolean>()
    val isValidPassword: LiveData<Boolean> get() = _isValidPassword

    private val _isValidEmail = MutableLiveData<Boolean>()
    val isValidEmail: LiveData<Boolean> get() = _isValidEmail

    private val _loginMethod = MutableLiveData<LoginMethodsEnum>()
    val loginMethod: LiveData<LoginMethodsEnum> get() = _loginMethod

    private val _loginStatus = MutableLiveData<Resource<*>?>()
    val loginStatus: LiveData<Resource<*>?> get() = _loginStatus


    private val _doesPhoneNumberExists = MutableLiveData<Boolean?>()
    val doesPhoneNumberExists: LiveData<Boolean?> = _doesPhoneNumberExists

    //    val context : Context = AppClass.instance
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()


    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    private var authRepository: AuthenticationRepositoryImpl = AuthenticationRepositoryImpl()
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()


    init {
        _loginMethod.value = LoginMethodsEnum.PHONENUMBER
        _loginButtonEnabled.value = false
    }

    fun onEmailChanged(emailAddress: String, refresh: Boolean = true) {

        this.emailAddress = emailAddress
        _isValidEmail.value = Validators.isValidMail(emailAddress)
        if (refresh) {
            validateFields()
        }
    }

    fun onPasswordChanged(password: String, refresh: Boolean = true) {
        this.password = password
        _isValidPassword.value =
            password.isNotEmpty() && password.length >= AppConstants.PASSWORD_MINIMUM_LENGTH
        if (refresh) {
            validateFields()
        }
    }

    fun onPhoneNumberChanged(phoneNumber: String, refresh: Boolean = true) {
        this.phoneNumber = phoneNumber
        _isValidPhoneNumber.value = Validators.isValidPhoneNumber(phoneNumber)
        if (refresh) {
            validateFields()
        }
    }

    private fun validateFields() {
        when (_loginMethod.value!!) {
            LoginMethodsEnum.EMAIL -> {
                _loginButtonEnabled.value = _isValidEmail.value!! && _isValidPassword.value!!
            }

            LoginMethodsEnum.PHONENUMBER -> {
                _loginButtonEnabled.value = _isValidPhoneNumber.value
            }

            LoginMethodsEnum.GOOGLE -> TODO()
            LoginMethodsEnum.FACEBOOK -> TODO()
        }
    }

    fun onLoginMethodSelected(loginMethod: LoginMethodsEnum) {
        _loginStatus.value = null
        when (loginMethod) {
            LoginMethodsEnum.EMAIL -> {
                onEmailChanged(this.emailAddress, false)
                onPasswordChanged(this.password, false)
            }

            LoginMethodsEnum.PHONENUMBER -> {
                onPhoneNumberChanged(this.phoneNumber, false)
            }

            LoginMethodsEnum.GOOGLE -> TODO()
            LoginMethodsEnum.FACEBOOK -> TODO()
        }
        _loginMethod.value = loginMethod
        validateFields()
    }

    fun onLoginWithEmailAndPassword(email: String, password: String) {
        _loginStatus.value = null
        _loginStatus.postValue(Resource.Loading<User>(null))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loginResult = authRepository.loginWithEmailAndPassword(email, password)
                if (loginResult.data != null) {
                    _loginStatus.postValue(Resource.Success<User>(loginResult.data))
                } else {
                    _loginStatus.postValue(Resource.Error<User>(loginResult.message.toString()))
                }
            } catch (exception: Exception) {
                _loginStatus.postValue(Resource.Error<User>(exception.localizedMessage.toString()))
            }
        }
    }

    /*
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
    */
    private val _signInWithGoogleCall = MutableLiveData<Resource<Intent?>>()
    val signInWithGoogleCall: LiveData<Resource<Intent?>> = _signInWithGoogleCall
    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        _signInWithGoogleCall.postValue(Resource.Loading<Intent?>(signInIntent))

    }


    suspend fun onAuthWithGoogle(googleSignInAccount: GoogleSignInAccount) {

        _loginStatus.postValue(Resource.Loading<User>())

        val signInIntent = googleSignInClient
        //      listener.onSignInStarted(signInIntent)

        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        try {
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val user = authResult.user
            // Handle signed in user
            var userToReturn = usersRepository.getUserRemote(user?.uid.toString())
            if (userToReturn.data != null) {
                _loginStatus.postValue(Resource.Success<User>(userToReturn.data))
            } else {
                if (googleSignInAccount.idToken != null) {
                    var newUser = User()
                    newUser.user_key = user?.uid
                    newUser.display_name = googleSignInAccount.displayName
                    newUser.first_name = googleSignInAccount.givenName
                    newUser.last_name = googleSignInAccount.familyName
                    newUser.email_address = googleSignInAccount.email.toString()
                    newUser.registrationType = googleSignInAccount.account?.type
                    newUser.user_type = "COMMON_USER"
                    if (googleSignInAccount.photoUrl != null) {

                        var fileName = googleSignInAccount.photoUrl?.lastPathSegment.toString()
                        var imageBitmap =
                            context.loadImageFromCache(fileName, PROFILE_IMAGES_STORAGE_PATH)

                        if (imageBitmap == null) {
                            var bitmap = googleSignInAccount.photoUrl?.let {
                                imageBitmap = context.getImageFromUrl(it.toString())

                                imageBitmap?.saveImageToCache(
                                    context,
                                    googleSignInAccount.photoUrl.toString(),
                                    PROFILE_IMAGES_STORAGE_PATH
                                )

                                newUser.image = MediaFile(
                                    MediaTypesEnum.IMAGE,
                                    fileName
                                )
                            }
                        }
                    }
                    usersRepository.insertUser(newUser)

                    _loginStatus.postValue(Resource.Success<User>(newUser))
                } else {
                    _loginStatus.postValue(Resource.Error<User>(userToReturn.message.toString()))
                }
            }
        } catch (e: Exception) {
            // Handle error
            _loginStatus.value = Resource.Error<User>(e.localizedMessage.toString())

        }/*
                    val authManager = FirebaseAuth.getInstance()

                    try {
                        var googleAuth =
                            authManager.signInWithCredential(credential)?.await()
                        var user = Resource.Success(googleAuth?.user!!)

                        var userToReturn = usersRepository.getUserRemote(user.data?.uid.toString())
                        if (userToReturn.data != null) {
                            _loginStatus.value = Resource.Success<User>(userToReturn.data)
                        } else {
                            _loginStatus.value = Resource.Error<User>(userToReturn.message.toString())
                        }

                    } catch (e: Exception) {
                        _loginStatus.value = Resource.Error<User>(e.localizedMessage.toString())
                    }
                    */
    }


    suspend fun authWithGoogle(idToken: String) {

        _loginStatus.value = Resource.Loading<User>()

        val signInIntent = googleSignInClient
        //      listener.onSignInStarted(signInIntent)

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val user = authResult.user
            // Handle signed in user
            var userToReturn = usersRepository.getUserRemote(user?.uid.toString())
            if (userToReturn.data != null) {
                _loginStatus.value = Resource.Success<User>(userToReturn.data)
            } else {
                _loginStatus.value = Resource.Error<User>(userToReturn.message.toString())
            }
        } catch (e: Exception) {
            // Handle error
            _loginStatus.value = Resource.Error<User>(e.localizedMessage.toString())

        }/*
                    val authManager = FirebaseAuth.getInstance()

                    try {
                        var googleAuth =
                            authManager.signInWithCredential(credential)?.await()
                        var user = Resource.Success(googleAuth?.user!!)

                        var userToReturn = usersRepository.getUserRemote(user.data?.uid.toString())
                        if (userToReturn.data != null) {
                            _loginStatus.value = Resource.Success<User>(userToReturn.data)
                        } else {
                            _loginStatus.value = Resource.Error<User>(userToReturn.message.toString())
                        }

                    } catch (e: Exception) {
                        _loginStatus.value = Resource.Error<User>(e.localizedMessage.toString())
                    }
                    */
    }

    fun onSignInWithGoogle() {
        //listener.onSignInStarted(googleSignInClient)
    }

    fun onLoginWithPhoneNumberClicked(activity: Activity, phoneNumber: String) {
        _loginStatus.postValue(Resource.Loading(null))

        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.verifyIfPhoneNumberExists(phoneNumber)
            if (result is Resource.Success) {
                val phoneNumberExists = result.data as Boolean

                if (phoneNumberExists) {
                    val phoneCallback =
                        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                CoroutineScope(coroutineContext).launch {
                                    var call = signInWithPhoneAuthCredential(credential)
                                    _loginStatus.postValue(Resource.Success(call.data!!))
                                }
                            }

                            override fun onVerificationFailed(ex: FirebaseException) {

                                Log.w("PHONE NUMBER SIGNUP", "onVerificationFailed", ex)
                                if (ex is FirebaseAuthInvalidCredentialsException) {
                                    _loginStatus.postValue(Resource.Error<User>(ex.message.toString()))
                                } else if (ex is FirebaseTooManyRequestsException) {
                                    _loginStatus.postValue(Resource.Error<User>(ex.message.toString()))
                                } else {
                                    // Show a message and update the UI
                                    // ...
                                    _loginStatus.postValue(Resource.Error<User>(ex.message.toString()))
                                }
                            }

                            override fun onCodeSent(
                                verificationId: String, token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                super.onCodeSent(verificationId, token)
                                // The SMS verification code has been sent to the provided phone number, we
                                // now need to ask the user to enter the code and then construct a credential
                                // by combining the code with a verification ID.
                                Log.d("SIGNIN BY PHONE", "onCodeSent:$verificationId")

                                // Save verification ID and resending token so we can use them later
                                val storedVerificationId = verificationId
                                var resendToken = token
                                //    CoroutineScope(coroutineContext).launch{
                                var bundle = onCodeSentToPhone(phoneNumber, storedVerificationId)
                                bundle.putParcelable("resend_token", resendToken)
                                bundle.putString("storedVerificationId", storedVerificationId)

                                _loginStatus.postValue(Resource.Success<Bundle>(bundle))
                                //      }
                            }

                        }

                    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity) // Activity (for callback binding)
                        .setCallbacks(phoneCallback) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)


                } else {

                    _loginStatus.postValue(Resource.Error<User>("phone_number_not_registered"))
                }
                //  _doesPhoneNumberExists.postValue(phoneNumberExists)

            } else {
                _error.postValue(result.message.toString())
            }
        }

    }


    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Resource<FirebaseUser?> {
        return try {
            var call = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            Resource.Success(call.user)
        } catch (exception: Exception) {
            Resource.Error(exception.message.toString())
        }
    }

    fun onCodeSentToPhone(phoneNumber: String, storedVerificationId: String): Bundle {

        val bundle = Bundle()
        val map: HashMap<String, String> = HashMap()
        map["phone_number"] = phoneNumber
        //val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java)
        bundle.putSerializable("action", OTPActionsEnum.LOGIN)
        bundle.putString("storeVerificationId", storedVerificationId)
        bundle.putSerializable("user_data", map)
        return bundle
        // startActivity(intent)
        //  finish()

    }


    fun onUserAuthenticated(user: FirebaseUser) {

    }

    fun resetLoginStatus() {
        _loginStatus.value = null
    }


    @Suppress("UNCHECKED_CAST")
    class LoginActivityViewModelFactory(
        private val context: Context, private val listener: OnSignInStartedListener
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginActivityViewModel::class.java)) {
                return LoginActivityViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

    }

    interface OnSignInStartedListener {
        fun onSignInStarted(client: GoogleSignInClient?)
    }

}