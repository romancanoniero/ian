package com.iyr.ian.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants.Companion.RC_SIGN_IN_FACEBOOK
import com.iyr.ian.Constants.Companion.RC_SIGN_IN_GOOGLE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityLoginBinding
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.otp.OtpVerificationActivity
import com.iyr.ian.ui.setup.SetupActivity
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.setup.pin_setup.PinSetupActivity
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivity
import com.iyr.ian.ui.setup.speed_dial_setup.SpeedDialSetupActivity
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.ui.signup.signup_selector.SignUpSelectorActivity
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.startActivity
import com.iyr.ian.viewmodels.LoginActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


interface LoginActivityCallback {
    fun enableLoginButton()
    fun disableLoginButton()
    fun onErrorUserDoesNotExists()

    //   fun goToSignupStepFirst()
    fun getIntent(): Intent
    fun onLogedIn(user: FirebaseUser)
    fun onOkToMainScreen(user: User)
    fun onProfileIncomplete(user: User)
    fun onLoginError(exception: Exception)
    fun onError(exception: Exception)
    fun onCodeSentToPhone(storedVerificationId: String)
}


enum class ProviderType {
    BASIC, EMAIL, PHONENUMBER, GOOGLE, FACEBOOK
}


enum class LoginMethod {

    EMAIL, PHONENUMBER,

}

class LoginActivity : AppCompatActivity(), LoginActivityCallback {
    private lateinit var signInRequest: BeginSignInRequest
    private var buttonClicked: Boolean = false
    private var callbackManager: CallbackManager? = null

    // protected lateinit var mPresenter: LoginPresenter
    private var username: String = ""
    private var password: String = ""
    lateinit var binding: ActivityLoginBinding
    private var loginSelectedMethod: LoginMethod = LoginMethod.PHONENUMBER

    //   var loader = LoadingDialogFragment()
    private lateinit var viewModel: LoginActivityViewModel



    /*
        val factory = LoginActivityViewModel.LoginActivityViewModelFactory(
            this,
            object : LoginActivityViewModel.OnSignInStartedListener {
                override fun onSignInStarted(client: GoogleSignInClient?) {
                    startActivityForResult(client?.signInIntent!!, RC_SIGN_IN_GOOGLE)
                }
            })
    */

    // En tu actividad
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                lifecycleScope.launch(Dispatchers.IO) {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.onAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Desactivo el boton back
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = LoginActivityViewModel(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Facebook Login
        //configureFacebookButton()


        setupUI()

        viewModel.onEmailChanged(binding.emailAddress.text.toString())
        viewModel.onPasswordChanged(binding.emailAddress.text.toString())
        viewModel.onPhoneNumberChanged(binding.phoneNumber.text.toString())
        binding.togglePhoneButton.callOnClick()
        updateUI()
        handleIntentData()

    }

    override fun onResume() {
        super.onResume()
        startObservers()
    }


    private fun startObservers() {

        viewModel.loginStatus.observe(this, Observer { status ->
            when (status) {
                is Resource.Loading -> {
                    showLoader()
                    Log.d("LOGIN", "Loading")
                }

                is Resource.Success -> {
                    hideLoader()

                    if (status.data != null) {
                        when (status.data) {
                            is User -> {
                                val user: User = status.data


                                onUserAuthenticated(user)
                            }

                            is Bundle -> {
                                val extras = status.data
                                val intent =
                                    Intent(this@LoginActivity, OtpVerificationActivity::class.java)
                                intent.putExtras(extras)
                                startActivity(intent)

                            }
                        }
                    }
                    //
                }

                is Resource.Error -> {
                    hideLoader()

                    var errorMessage =
                        when (status.message.toString().lowercase(Locale.getDefault())) {
                            "the password is invalid or the user does not have a password." -> getString(
                                R.string.login_error_invalid_password_or_username
                            )

                            "there is no user record corresponding to this identifier. the user may have been deleted." -> getString(
                                R.string.login_error_user_doest_not_exists
                            )

                            "phone_number_not_registered" -> getString(R.string.error_phone_number_is_not_registered)

                            else -> status.message.toString()
                        }
                    showErrorDialog(errorMessage)
                    viewModel.resetLoginStatus()
                }

                null -> {}
            }
        })


        viewModel.loginMethod.observe(this, Observer { method: LoginMethodsEnum ->
            when (method) {
                LoginMethodsEnum.EMAIL -> {
                    switchToEmailLoginMethod()
                }

                LoginMethodsEnum.PHONENUMBER -> {
                    switchToPhoneNumberLoginMethod()
                }

                LoginMethodsEnum.GOOGLE -> TODO()
                LoginMethodsEnum.FACEBOOK -> TODO()
            }

        })

        viewModel.loginButtonEnabled.observe(this, Observer { enabled ->
            binding.loginButton.isEnabled = enabled
        })

        viewModel.doesPhoneNumberExists.observe(this) { phoneNumberExists ->
/*
            if (phoneNumberExists as Boolean) {
                val phoneCallback =
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases the phone number can be instantly
                            //     verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                            //     detect the incoming verification SMS and perform verification without
                            //     user action.
                            Log.d(
                                "PHONE NUMBER SIGNUP",
                                "onVerificationCompleted:$credential"
                            )
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(ex: FirebaseException) {
                            hideLoader()
                            Log.w("PHONE NUMBER SIGNUP", "onVerificationFailed", ex)
                            if (ex is FirebaseAuthInvalidCredentialsException) {
                                // Invalid request
                                // ...
                                showErrorDialog(ex.message.toString())
                            } else if (ex is FirebaseTooManyRequestsException) {
                                // The SMS quota for the project has been exceeded
                                // ...
                                showErrorDialog(ex.message.toString())
                            } else {
                                // Show a message and update the UI
                                // ...
                                showErrorDialog(ex.message.toString())
                            }
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            super.onCodeSent(verificationId, token)
                            hideLoader()
                            // The SMS verification code has been sent to the provided phone number, we
                            // now need to ask the user to enter the code and then construct a credential
                            // by combining the code with a verification ID.
                            Log.d("SIGNIN BY PHONE", "onCodeSent:$verificationId")

                            // Save verification ID and resending token so we can use them later
                            val storedVerificationId = verificationId
                            var resendToken = token
                            onCodeSentToPhone(storedVerificationId)
                        }

                    }

                val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(viewModel.phoneNumber) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this@LoginActivity) // Activity (for callback binding)
                    .setCallbacks(phoneCallback) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            } else {
                showErrorDialog(getString(R.string.error_phone_number_is_not_registered))
            }
*/
        }

        viewModel.signInWithGoogleCall.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    var signInIntent = resource.data!!
                    //    val context : Context = AppClass.instance
                }

                is Resource.Success -> {
var pp = 33
//                    val signInIntent = resource.data!!
  //                  googleSignInLauncher.launch(signInIntent)
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(resource.message.toString())
                }
            }
        }

    }


    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    private fun stopObservers() {
        viewModel.loginStatus.removeObservers(this)


        viewModel.loginMethod.removeObservers(this)

        viewModel.loginButtonEnabled.removeObservers(this)

        viewModel.doesPhoneNumberExists.removeObservers(this)

        viewModel.signInWithGoogleCall.removeObservers(this)
    }

    /*
        private fun configureFacebookButton() {
            // Initialize Facebook Login button
            callbackManager = CallbackManager.Factory.create()
            binding.fbButton.setReadPermissions(
                "email",
                "public_profile",
                "user_friends"
            )
            binding.facebookLoginButton.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {

                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("FACEBOOK_LOGIN", "facebook:onSuccess:$loginResult")
                    mPresenter.handleFacebookAccessToken(loginResult.accessToken!!)
                }

                override fun onCancel() {
                    Log.d("FACEBOOK_LOGIN", "facebook:onCancel")
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d("FACEBOOK_LOGIN", "facebook:onError", error)
                    // ...
                    showErrorDialog(error.localizedMessage)
                }
            })
        }
    */


    private fun handleIntentData() {
        val emailLink = intent.data.toString()
        //      Toast.makeText(this, emailLink, Toast.LENGTH_LONG).show()
    }

    private fun setupUI() {

        /*
                binding.backArrows?.setOnClickListener {
                    onBackPressed()
                }
                */

        /*
                binding.togglePhoneButton?.setOnClickListener {
                    viewModel.onLoginMethodSelected(LoginMethod.PHONENUMBER)
                }
                */



        binding.togglePhoneButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                viewModel.onLoginMethodSelected(LoginMethodsEnum.PHONENUMBER)
                return true
            }
        })


        binding.toggleEmailButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                viewModel.onLoginMethodSelected(LoginMethodsEnum.EMAIL)
                return true
            }
        })/*
              binding.toggleEmailButton?.setOnClickListener {
                  viewModel.onLoginMethodSelected(LoginMethod.EMAIL)

                  loginSelectedMethod = LoginMethod.EMAIL
              }
      */
        binding.newAccount.setOnClickListener {
            val nextIntent = Intent(applicationContext, SignUpSelectorActivity::class.java)
            intent.extras?.let {
                nextIntent.putExtras(it)

            }
            startActivity(nextIntent)
        }/*
        binding.forgotPasswordLabel.setOnClickListener(View.OnClickListener {

            //            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        })
*/

        binding.emailAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(charSecuence: Editable?) {
                updateUI()
                viewModel.onEmailChanged(charSecuence?.toString() ?: "")
            }
        })

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(charSecuence: Editable?) {
                viewModel.onPasswordChanged(charSecuence?.toString() ?: "")
            }
        })

        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(charSecuence: Editable?) {
                viewModel.onPhoneNumberChanged(charSecuence?.toString() ?: "")

            }
        })

        binding.phoneNumber.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
            }
            return@setOnEditorActionListener true
        }/*
        binding.switchSignupWithEmailLink.setOnCheckedChangeListener { compoundButton, isOn ->

            if (isOn) {
                binding.passwordInputSection.visibility = View.INVISIBLE
            } else {
                binding.passwordInputSection.visibility = View.VISIBLE
            }

        }
*/

        binding.loginButton.setOnClickListener(View.OnClickListener {

            when (viewModel.loginMethod.value) {
                LoginMethodsEnum.EMAIL -> {
                    val emailAddress = binding.emailAddress.text.toString()
                    val password = binding.password.text.toString()
                    viewModel.onLoginWithEmailAndPassword(emailAddress, password)
                }

                LoginMethodsEnum.PHONENUMBER -> {
                    val phoneNumber = binding.phoneNumber.text.toString()/*s
                                        showErrorDialog(
                                            "Pendiente",
                                            "Debes implementar el metodo signInWithPhoneNumber(phoneNumber)"
                                        )
                                        */
                    //mPresenter.signInWithPhoneNumber(phoneNumber)
                    viewModel.onLoginWithPhoneNumberClicked(this, phoneNumber)
                }

                else -> {}
            }


        })

        /*
                binding.fbCard.setOnClickListener { it ->

                    mPresenter.requireAuthentication(ProviderType.FACEBOOK)

                }

        */



        binding.googleButton.setOnClickListener {
            this@LoginActivity.handleTouch()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()


             val googleSignInClient = GoogleSignIn.getClient(this, gso)


            googleSignInLauncher.launch(googleSignInClient.signInIntent)


       //     viewModel.signInWithGoogle()

            /*
                        val gso = GoogleSignInOptions
                            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
                        val signInIntent: Intent = googleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
            */
            //     mPresenter.loginWithGoogle()
        }
    }

    private fun switchToEmailLoginMethod() {
        binding.togglePhoneButton.isChecked = false
        binding.toggleEmailButton.isChecked = true
        Glide.with(this@LoginActivity).asBitmap().load(R.drawable.by_email_image)
            .into(binding.loginTypeImage)

        binding.emailAddressInputLayout.visibility = View.VISIBLE
        binding.passwordInputLayout.visibility = View.VISIBLE
        binding.phoneNumberInputLayout.visibility = View.GONE
    }

    private fun switchToPhoneNumberLoginMethod() {
        binding.toggleEmailButton.isChecked = false
        binding.togglePhoneButton.isChecked = true
        Glide.with(this@LoginActivity).asBitmap().load(R.drawable.by_phone_email)
            .into(binding.loginTypeImage)

        binding.emailAddressInputLayout.visibility = View.GONE
        binding.passwordInputLayout.visibility = View.INVISIBLE
        binding.phoneNumberInputLayout.visibility = View.VISIBLE
    }

    override fun onLogedIn(user: FirebaseUser) {

        broadcastMessage(
            intent.extras, AppConstants.ServiceCode.BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
        )


        /*
                UsersWSClient.instance.getAuthToken(object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            //   Toast.makeText(applicationContext, "Enviar mensaje para direccinar a Main Activity", Toast.LENGTH_SHORT).show()
          */
        broadcastMessage(
            intent.extras, AppConstants.ServiceCode.BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
        )

        /*
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
            *//*
              }
          }
      })
*/
//        startActivity(MainActivity::class.java, null)


    }

    @SuppressLint("MissingPermission")
    fun getDevicePhoneNumber() {/*
              telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
              var telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
              binding.emailET.setText(telephonyManager.line1Number)


         */
    }


    private fun updateUI() {/*
                var emailOrNumber = binding.emailET.text.toString()
                var password = binding.passwordET.text.toString()

                if (Validators.isValidPhoneNumber(emailOrNumber)) {
                    binding.emailET.inputType = InputType.TYPE_CLASS_PHONE
                    binding.passwordSection.visibility = View.INVISIBLE
                } else {
                    binding.emailET.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    binding.passwordSection.visibility = View.VISIBLE
                }
        */

        when (loginSelectedMethod) {
            LoginMethod.EMAIL -> {
                val emailAddress = binding.emailAddress.text.toString()
                val password = binding.password.text.toString()

                binding.loginButton.isEnabled =
                    Validators.isValidMail(emailAddress) && password.isNotEmpty() && password.length >= AppConstants.PASSWORD_MINIMUM_LENGTH && !buttonClicked


            }

            LoginMethod.PHONENUMBER -> {
                val phoneNumber = binding.phoneNumber.text.toString()
                binding.loginButton.isEnabled =
                    Validators.isValidPhoneNumber(phoneNumber) && !buttonClicked

            }
        }

    }

    /*
        override fun userAuthenticationByPhoneIsDone() {
            //  TODO("Not yet implemented")
            //       Toast.makeText(this, "logiin successfull redirect to main", Toast.LENGTH_LONG).show()
        }
    */
    override fun enableLoginButton() {
        binding.loginButton.isEnabled = true
    }

    override fun disableLoginButton() {
        binding.loginButton.isEnabled = false
    }

    fun onUserAuthenticated(user: User) {

        val isUserCompleted =
            !user.display_name.isNullOrEmpty() && user.image?.file_name != null && user.allow_speed_dial != null && user.sos_invocation_count != null && user.sos_invocation_count >= 3 && user.sos_invocation_method != null

        if (isUserCompleted) {
            goToMainScreen(user)
        } else {
            goToCompleteProfile(user)
        }
    }

    private fun goToCompleteProfile(user: User) {

        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup", true
        )
        var intent: Intent? = null
        intent =
            if (user.display_name.isNullOrBlank() || (user.telephone_number.isNullOrEmpty() && user.email_address.isEmpty())) {
                Intent(this, SetupActivity::class.java)
            } else if (user.sos_invocation_count == 0 || user.sos_invocation_method.isNullOrEmpty()) {
                Intent(this, PressOrTapSetupActivity::class.java)
            } else if (user.security_code.isBlank()) {
                Intent(this, PinSetupActivity::class.java)
            } else if (user.allow_speed_dial == null) {
                Intent(this, SpeedDialSetupActivity::class.java)
            } else if (user.sos_invocation_count == null || user.sos_invocation_count < 3) {
                Intent(this, PressOrTapSetupActivity::class.java)
            } else if (!areLocationPermissionsGranted()) {
                Intent(this, LocationRequiredActivity::class.java)
            } else {
                Intent(this, AddContactsFromPhoneActivity::class.java)
            }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun goToMainScreen(user: User) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("user", Gson().toJson(user))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }


    /*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PHONE_STATE_AND_PHONE_NUMBER -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //       Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    getDevicePhoneNumber()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) || shouldShowRequestPermissionRationale(
                            Manifest.permission.READ_PHONE_NUMBERS
                        )
                    ) {

                        var rationale = PermissionsRationaleDialog(
                            this, this, R.string.rationale_pemission_code_800, arrayOf(
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_PHONE_NUMBERS
                            ),
                            REQUEST_PHONE_STATE_AND_PHONE_NUMBER
                        )

                        rationale.show()
                    } else {
                        /*
                                              Toast.makeText(
                                                  this,
                                                  "Debes activar el permiso manualmente",
                                                  Toast.LENGTH_LONG
                                              ).show()
                      */
                        AppClass.instance.showErrorDialog("Debes activar el permiso manualmente")


                    }
                }
            }

            REQUEST_STORAGE_READ_WRITE -> {


            }

        }            if (phoneNumberExists.data != null) {

            }

    }

*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN_GOOGLE -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    lifecycleScope.launch(Dispatchers.Main) {

                        viewModel.authWithGoogle(account.idToken!!)

                    }

                    /*
                  if (account != null) {
                      val credential = GoogleAuthProvider.getCredential(account.idToken, null)


                      FirebaseAuth.getInstance().signInWithCredential(credential)
                          .addOnCompleteListener {
                              if (it.isSuccessful) {
                                  mPresenter.afterUserAuth(
                                      FirebaseAuth.getInstance().uid.toString(),
                                      task.result!!
                                  )


                                  /*
                                                                      var user = User(APPLICATION_USER_TYPE)
                                                                      user.user_key = FirebaseAuth.getInstance().uid
                                                                      user.email_address = task.result!!.email
                                                                      user.display_name = task.result!!.displayName!!.split(" ")[0]
                                                                      user.images = ArrayList<MediaFile>()
                                                                      var image = MediaFile(MediaType.IMAGE)
                                                                      image.url = task.result!!.photoUrl.toString()
                                                                      image.isFavorite = true
                                                                      user.images.add(image)

                                                                      Log.i(
                                                                          "LOGGEO",
                                                                          "voy a averiguar si el usuario existe en la tabla de usuarios"
                                                                      )
                                  */

                              } else {
                                  showErrorDialog(it.exception!!.localizedMessage)
                              }

                          }

                }
 */
                } catch (e: ApiException) {


                    when (e.statusCode) {
                        12501 -> {
                            // No hago nada porque quiere decir que el Usuario Cancelo la operacion
                        }

                        12500 -> {
                            showErrorDialog(getString(R.string.error_google_sign_in_failed))
                        }

                        else -> {
                            showErrorDialog(e.localizedMessage)
                        }
                    }

                }
            }

            RC_SIGN_IN_FACEBOOK -> {

                callbackManager!!.onActivityResult(requestCode, resultCode, data)
            }

        }

    }


    override fun onError(exception: Exception) {
        hideLoader()
        var errorMessage = exception.message.toString()
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                // Invalid request
                // ...
                //    mCallback.onError(ex)
            }

            is FirebaseTooManyRequestsException -> {
                // The SMS quota for the project has been exceeded
                // ...
                errorMessage = getString(R.string.error_too_many_tries_from_your_device)
            }

            is FirebaseFunctionsException -> {
                // The SMS quota for the project has been exceeded
                // ...
                when (exception.code.toString()) {
                    "UNAUTHENTICATED" -> {
                        errorMessage = getString(R.string.error_device_not_authenticated)

                    }
                }
            }

            else -> {
                when (exception.message) {
                    "PHONE_NUMBER_DOES_NOT_EXISTS" -> {
                        errorMessage = getString(R.string.error_phone_number_is_not_registered)

                    }

                    "EMAIL_NOT_REGISTERED" -> {
                        errorMessage = getString(R.string.error_email_not_registered)

                    }
                }
            }
        }
        showErrorDialog(errorMessage)

    }

    override fun onCodeSentToPhone(storedVerificationId: String) {
        TODO("Not yet implemented")
    }

    override fun onErrorUserDoesNotExists() {
        TODO("Not yet implemented")
    }

    override fun onOkToMainScreen(user: User) {
        AppClass.instance.core?.onOkToMainScreen(user, intent.extras)/*
                hideLoader()
                SessionForProfile.getInstance(applicationContext).storeUserProfile(user)
                UsersWSClient.instance.getAuthToken(object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            Toast.makeText(applicationContext, "Enviar mensaje para direccinar a Main Activity", Toast.LENGTH_SHORT).show()
                            broadcastMessage(intent.extras,
                                AppConstants.ServiceCode.BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
                            )
                        }
                    }
                })
        */

    }

    override fun onProfileIncomplete(user: User) {
        hideLoader()
        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup", true
        )
        startActivity(SetupActivity::class.java, bundle)
    }

    override fun onLoginError(exception: Exception) {
        hideLoader()
        var errorMessage = ""
        if (exception is FirebaseAuthInvalidUserException) {
            when (exception.errorCode) {
                "ERROR_USER_NOT_FOUND" -> {
                    errorMessage = getString(R.string.error_user_not_found)
                }
            }
        } else {
            when (exception.message.toString()) {
                "ERROR_EMAIL_NOT_VERIFIED" -> {
                    errorMessage = getString(R.string.error_email_not_verified)
                }
            }

        }
        showErrorDialog(errorMessage)
        buttonClicked = false
        updateUI()

    }


}

