package com.iyr.ian.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.implementations.auth.firebase.AuthenticationRepositoryImpl
import com.iyr.ian.enums.UserTypesEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.login.LoginMethodsEnum
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class SignUpWithEmailActivityViewModel(
    val context: Context,
    val activity: Activity
) :
    ViewModel() {


    enum class SignUpWithPhoneActionsEnum {
        VERIFY_OTP,
        IS_AUTHENTICATED
    }

    private var isEmailValid: Boolean = false


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _viewStatus = MutableLiveData<Resource<Any>?>()
    val viewStatus: LiveData<Resource<Any>?> = _viewStatus


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

    private val _loginStatus = MutableLiveData<Resource<*>>()
    val loginStatus: LiveData<Resource<*>> get() = _loginStatus


    private val _doesPhoneNumberExists = MutableLiveData<Boolean?>()
    val doesPhoneNumberExists: LiveData<Boolean?> = _doesPhoneNumberExists

    //    val context : Context = AppClass.instance
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()


    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    private var authRepository: AuthenticationRepositoryImpl = AuthenticationRepositoryImpl()
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()


    init {
        _loginMethod.value = LoginMethodsEnum.PHONENUMBER
        _loginButtonEnabled.value = false
    }


    fun onUserAuthenticated(user: FirebaseUser) {

    }

    fun onSignUpWithPhoneNumber(phoneNumber: String) {

        _viewStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val phoneCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d("PHONE NUMBER SIGNUP", "onVerificationCompleted:$credential")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(ex: FirebaseException) {

                    //_error.postValue(ex.toString())
                    _viewStatus.postValue(Resource.Error(ex.toString()))
                    /*
                        when (ex) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                _error.postValue(ex.toString())
                            }

                            is FirebaseTooManyRequestsException -> {
                                _error.postValue(ex.toString())
                            }

                            else -> {
                                _error.postValue(ex.toString())
                            }
                        }
                        */

                    // Show a message and update the UI
                    // ...
                    //callback.onError(ex.toString())

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationId, token)

                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    // Save verification ID and resending token so we can use them later
                    val storedVerificationId = verificationId
                    val resendToken = token

                    val actionMap = HashMap<String, Any>()
                    actionMap["action"] = SignUpWithPhoneActionsEnum.VERIFY_OTP.toString()
                    actionMap["storedVerificationId"] = storedVerificationId
                    actionMap["resendToken"] = resendToken
                    _viewStatus.postValue(Resource.Success(actionMap))
                }

            }


            val options = PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(phoneCallback)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //         Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            var authToken = authRepository.getAuthToken()

                            if (authToken.data != null)
                                Log.d("TOKEN_AUTHENTICATION =", authToken.data.toString())


                            /*
                        userAuthCreationSuccess()

                        Log.d("TOKEN_AUTHENTICATION =", authToken.toString())
                        _authToken.value = authToken.data.toString()

                        val user = _currentData
                        _viewStatus.postValue(Resource.Success<User?>(user))

*/

                            var actionMap = HashMap<String, Any?>()
                            actionMap["action"] =
                                SignUpWithPhoneActionsEnum.IS_AUTHENTICATED.toString()
                            actionMap["authToken"] = authToken.data.toString()
                            actionMap["user"] = user
                            _viewStatus.postValue(Resource.Success(actionMap))

                        } catch (exception: Exception) {
                            _viewStatus.value =
                                Resource.Error(exception.localizedMessage.toString())

                        }

                    }


                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    //       Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    fun onSignUpWithEmail(emailAddress: String, password: String) {
        _viewStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {


            val call = authRepository.createUserWithEmailPassword(emailAddress, password)
            val authUser = call.data as FirebaseUser?
            when (call) {
                is Resource.Success -> {
                    val user = User(UserTypesEnum.COMMON_USER.toString())
                    user.user_key = FirebaseAuth.getInstance().uid.toString()
                    user.email_address = emailAddress

                    try {
                        var call = usersRepository.insertUser(user)

                        try {
                            var emailVerificationCall =
                                authRepository.sendEmailVerification(authUser!!)
                            _viewStatus.postValue(Resource.Success(user))

                        } catch (exception: Exception) {
                            _viewStatus.postValue(Resource.Error(exception.localizedMessage.toString()))
                        }

                    } catch (exception: Exception) {
                        _viewStatus.postValue(Resource.Error(exception.localizedMessage.toString()))
                    }
                    /*
                                        UsersWSClient.instance.createUserOnStorage(user,
                                            object : OnCompleteCallback {
                                                override fun onComplete(success: Boolean, result: Any?) {
                                                    if (success) {
                                                        SessionForProfile.getInstance(AppClass.instance)
                                                            .storeUserProfile(user)

                                                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                                            ?.addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    callback.onSignUpByEmailSuccess()
                                                                } else {

                                                                    callback.onError(task.exception!!.localizedMessage.toString())
                                                                }

                                                            }


                                                    }
                                                }

                                                override fun onError(exception: java.lang.Exception) {
                                                    super.onError(exception)
                                                    callback.onError(exception)
                                                }
                                            })




                                        _viewStatus.postValue(Resource.Success(call.data))
                    */
                }

                is Resource.Error -> {
                    _viewStatus.postValue(Resource.Error(call.message.toString()))
                }

                is Resource.Loading -> {
                    _viewStatus.postValue(Resource.Loading())
                }


                /*
                val phoneCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d("PHONE NUMBER SIGNUP", "onVerificationCompleted:$credential")
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(ex: FirebaseException) {

                        //_error.postValue(ex.toString())
                        _viewStatus.postValue(Resource.Error(ex.toString()))
                        /*
                            when (ex) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    _error.postValue(ex.toString())
                                }

                                is FirebaseTooManyRequestsException -> {
                                    _error.postValue(ex.toString())
                                }

                                else -> {
                                    _error.postValue(ex.toString())
                                }
                            }
                            */

                        // Show a message and update the UI
                        // ...
                        //callback.onError(ex.toString())

                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, token)

                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        // Save verification ID and resending token so we can use them later
                        val storedVerificationId = verificationId
                        val resendToken = token

                        val actionMap = HashMap<String, Any>()
                        actionMap["action"] = SignUpWithPhoneActivityViewModel.SignUpWithPhoneActionsEnum.VERIFY_OTP.toString()
                        actionMap["storedVerificationId"] = storedVerificationId
                        actionMap["resendToken"] = resendToken
                        _viewStatus.postValue(Resource.Success(actionMap))
                    }

                }


                val options = PhoneAuthOptions
                    .newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(phoneCallback)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

                 */
            }

        }

    }


    private val _emailAddress = MutableLiveData<String>()
    val emailAddress: LiveData<String> = _emailAddress
    fun onEmailChanged(emailAddress: String) {
        _emailAddress.value = emailAddress
        checkSignUpButtonStatus()
        isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
        _isValidEmail.value = isEmailValid
    }



    private val _password = MutableLiveData<String?>()
    val password: LiveData<String?> = _password
    fun onPasswordChanged(password: String) {
        _password.value = password
        checkSignUpButtonStatus()
    }

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String> = _confirmPassword
    fun onPasswordConfirmationChanged(password: String) {
        _confirmPassword.value = password
        checkSignUpButtonStatus()
    }


    /***
     * Determina si el boton de registro debe mostrarse habilitado o no
     */
    private fun checkSignUpButtonStatus() {
        _loginButtonEnabled.value = isEmailValid && isValidPassword.value!!
        val emailAddress = _emailAddress.value
        val password = _password.value
        val confirmPassword = _confirmPassword.value
        Validators.isValidMail(emailAddress) &&
                (password?.compareTo(confirmPassword ?: "-----") == 0 && Validators.isValidPassword(
                    password,
                    AppConstants.PASSWORD_MINIMUM_LENGTH
                ))
    }


}