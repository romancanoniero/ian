package com.iyr.fewtouchs.ui.views.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.implementations.auth.firebase.AuthenticationRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.login.LoginMethod
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginActivityViewModel(val context: Context, val listener: OnSignInStartedListener) :
    ViewModel() {

    private var isEmailValid: Boolean = false

    internal var emailAddress: String = ""
    internal var password: String = ""
    internal var phoneNumber: String = ""

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

    private val _loginMethod = MutableLiveData<LoginMethod>()
    val loginMethod: LiveData<LoginMethod> get() = _loginMethod

    private val _loginStatus = MutableLiveData<Resource<User>>()
    val loginStatus: LiveData<Resource<User>> get() = _loginStatus


    //    val context : Context = AppClass.instance
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()


    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    private var authRepository: AuthenticationRepositoryImpl = AuthenticationRepositoryImpl()
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()


    init {
        _loginMethod.value = LoginMethod.PHONENUMBER
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
            LoginMethod.EMAIL -> {
                _loginButtonEnabled.value =
                    _isValidEmail.value!! && _isValidPassword.value!!
            }

            LoginMethod.PHONENUMBER -> {
                _loginButtonEnabled.value = _isValidPhoneNumber.value
            }
        }
    }

    fun onLoginMethodSelected(loginMethod: LoginMethod) {
        when (loginMethod) {
            LoginMethod.EMAIL -> {
                onEmailChanged(this.emailAddress, false)
                onPasswordChanged(this.password, false)
            }

            LoginMethod.PHONENUMBER -> {
                onPhoneNumberChanged(this.phoneNumber, false)
            }
        }
        _loginMethod.value = loginMethod
        validateFields()
    }

    fun onLoginWithEmailAndPassword(email: String, password: String) {
        _loginStatus.value = Resource.Loading<User>(null)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val loginResult = authRepository.loginWithEmailAndPassword(email, password)
                if (loginResult.data != null) {
                    _loginStatus.value = Resource.Success<User>(loginResult.data)
                } else {
                    _loginStatus.value = Resource.Error<User>(loginResult.message.toString())
                }
            } catch (exception: Exception) {
                _loginStatus.value = Resource.Error<User>(exception.localizedMessage.toString())
            }
        }
    }


    suspend fun authWithGoogle(idToken: String) {

        _loginStatus.value = Resource.Loading<User>()

        val authManager = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        val credential = GoogleAuthProvider.getCredential(idToken, null)

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
    }

    fun onSignInWithGoogle() {
        listener.onSignInStarted(googleSignInClient)
    }


    @Suppress("UNCHECKED_CAST")
    class LoginActivityViewModelFactory(
        private val context: Context,
        private val listener: OnSignInStartedListener
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginActivityViewModel::class.java)) {
                return LoginActivityViewModel(context, listener) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

    }

    interface OnSignInStartedListener {
        fun onSignInStarted(client: GoogleSignInClient?)
    }

}