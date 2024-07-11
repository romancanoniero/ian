package com.iyr.ian.ui.signup.with_email

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySignUpWithEmailBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSuccessDialog
import com.iyr.ian.utils.startActivity
import com.iyr.ian.viewmodels.SignUpWithEmailActivityViewModel


interface SignUpWithEmailActivityCallback {
    fun enableLoginButton()
    fun disableLoginButton()
    fun goBack()

    /*
    fun showError(message: String)
    fun showError(titleId: Int, messageId: Int, closeButtonText: Int)
    fun showError(titleId: Int, message: String, closeButtonText: Int)
*/
    fun onSignUpByEmailSuccess()
    fun requestOTPVerification(
        storedVerificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    )

    fun userAuthCreationByPhoneIsDone()
    fun onErrorUserDoesNotExists()
    fun showVerificationByEmailSent()
    fun onError(exception: Exception)
    fun onSignUpWithEmailLinkDone()

}

class SignUpWithEmailActivity : AppCompatActivity(), SignUpWithEmailActivityCallback {

    // private lateinit var mErrorDialog: NotificationDialog
    private var buttonClicked: Boolean = false

    // private lateinit var mPresenter: SignUpWithEmailPresenter
    private lateinit var binding: ActivitySignUpWithEmailBinding
    private var buttonPressed: Boolean = false

    private val viewModel by lazy { SignUpWithEmailActivityViewModel(this, this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //   mPresenter = SignUpWithEmailPresenter(this, this)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up_with_email)

        statusBarTransparent()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        startObservers()
    }

    override fun onPause() {
        super.onPause()
        buttonClicked = false
        stopObservers()
    }


    fun startObservers() {
        viewModel.viewStatus.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(resource.message.toString())
                    buttonClicked = false
                    updateUI()
                }

                is Resource.Success -> {
                    hideLoader()
                    val user = resource.data as User
                    SessionForProfile.getInstance(this@SignUpWithEmailActivity)
                        .storeUserProfile(user)

                    showSuccessDialog(
                        getString(R.string.verification_mail_sent),
                        getString(R.string.verification_mail_sent_description),
                        getString(R.string.close)

                    ) {
                        (this@SignUpWithEmailActivity).startActivity(
                            LoginActivity::class.java,
                            null
                        )
                        (this@SignUpWithEmailActivity).finish()
                    }

                }

                else -> {}
            }
        }
    }


    private fun stopObservers() {
        viewModel.viewStatus.removeObservers(this)
    }


    private fun setupUI() {
        binding.backArrows.setOnClickListener {
            onBackPressed()
            goBack()
        }

        binding.emailAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                updateUI()
            }
        })

        binding.confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                updateUI()
            }
        })


        binding.signupWithEmailButton.setOnClickListener {
            buttonClicked = true
            updateUI()
            val emailAddress = binding.emailAddress.text.toString()
            val password = binding.password.text.toString()

            val call = viewModel.onSignUpWithEmail(emailAddress, password);

        }


    }

    private fun updateUI() {
        val emailAddress = binding.emailAddress.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        binding.signupWithEmailButton.isEnabled = Validators.isValidMail(emailAddress) &&
                (password.compareTo(confirmPassword) == 0 && Validators.isValidPassword(
                    password,
                    AppConstants.PASSWORD_MINIMUM_LENGTH
                )) &&
                !buttonClicked
    }


    override fun requestOTPVerification(
        storedVerificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {
        //      startActivity(Intent(this, OtpVerificationActivity::class.java))

        /*
        val fullName: String = binding.nameET.text.toString()
        val emailOrNumber = binding.emailET.text.toString()
        var password = binding.passwordET.text.toString()

        val map: HashMap<String, String> = HashMap()
        map["display_name"] = fullName
        map["phone_number"] = emailOrNumber

        val nextIntent =
            Intent(this@SignUpWithEmailActivity, OtpVerificationActivity::class.java)
        intent.extras?.let {
            nextIntent.putExtras(it)
        }
        nextIntent.putExtra("action", OTP_ACTION_SIGNING)
        nextIntent.putExtra("storeVerificationId", storedVerificationId)
        nextIntent.putExtra("resendToken", resendToken)
        nextIntent.putExtra("user_data", map)
        startActivity(nextIntent)
*/

        finish()
    }

    override fun onSignUpByEmailSuccess() {
        hideLoader()
        this.startActivity(LoginActivity::class.java, null)
    }

    override fun userAuthCreationByPhoneIsDone() {
        TODO("Not yet implemented")
    }

    override fun enableLoginButton() {
        TODO("Not yet implemented")
    }

    override fun disableLoginButton() {
        TODO("Not yet implemented")
    }

    override fun goBack() {
        //      finish()
    }

    /*
        override fun showError(errorString: String) {
            buttonClicked = false
            updateUI()
            showError(R.string.error, errorString, R.string.close)
        }

        override fun showError(titleId: Int, messageId: Int, closeButtonText: Int) {

            val title = getString(titleId)
            val message = getString(messageId)
            val buttonCaption = getString(closeButtonText)
            showErrorDialog(
                title, message, buttonCaption
            ) {
                buttonClicked = false
                updateUI()
            }
        }

        override fun showError(titleId: Int, message: String, closeButtonText: Int) {
            buttonClicked = false
            updateUI()
            showErrorDialog(getString(titleId), message, getString(closeButtonText), null)
        }
    */

    override fun onErrorUserDoesNotExists() {
        buttonClicked = false
        updateUI()

        showErrorDialog(getString(R.string.error_user_does_not_exists))
    }

    override fun showVerificationByEmailSent() {
        buttonClicked = false
        updateUI()

        showErrorDialog(
            getString(R.string.verification_mail_sent),
            getString(R.string.verification_mail_sent_description),
            getString(R.string.close)
        ) { (this@SignUpWithEmailActivity).finish() }

    }


    override fun onError(exception: Exception) {
        hideLoader()
        buttonClicked = false
        updateUI()

        var message = exception.localizedMessage

        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                when (exception.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        message = getString(R.string.error_signup_email_already_in_use)

                    }
                }
            }

            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> {
                        message = getString(R.string.error_auth_provider_not_allowed)
                    }
                }
            }

            is FirebaseTooManyRequestsException -> {
                message = getString(R.string.error_too_many_requests)

            }

            is FirebaseException -> {
                when (exception.message) {
                    "An internal error has occurred. [ UNAUTHORIZED_DOMAIN:Domain not whitelisted by project ]" -> {
                        message = getString(R.string.error_domain_not_authorized)
                    }
                }
            }
        }

        showErrorDialog(message)

    }

    override fun onSignUpWithEmailLinkDone() {

    }

}
