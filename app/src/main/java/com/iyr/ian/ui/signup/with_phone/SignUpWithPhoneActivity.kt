package com.iyr.ian.ui.signup.with_phone

import android.content.Intent
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
import com.iyr.ian.Constants.Companion.OTP_ACTION_SIGNING
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivitySignUpWithPhoneBinding
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.otp.OtpVerificationActivity
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import com.iyr.ian.utils.Validators.isValidPhoneNumber
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.startActivity
import com.iyr.ian.viewmodels.SignUpWithPhoneActivityViewModel


interface SignUpWithPhoneActivityCallback {
    fun enableLoginButton()
    fun disableLoginButton()
    fun goBack()
    fun showError(message: String)
    fun showError(titleId: Int, messageId: Int, closeButtonText: Int)
    fun showError(titleId: Int, message: String, closeButtonText: Int)

    fun onSignUpByPhoneSuccess()
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


class SignUpWithPhoneActivity : AppCompatActivity() {

    private var buttonClicked: Boolean = false

    private val viewModel by lazy { SignUpWithPhoneActivityViewModel(this, this) }

    // private lateinit var mErrorDialog: NotificationDialog
    //  private lateinit var mPresenter: SignUpWithPhonePresenter
    private lateinit var binding: ActivitySignUpWithPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarTransparent()

        //    mPresenter = SignUpWithPhonePresenter(this, this)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up_with_phone)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        setupObservers()
    }

    override fun onPause() {
        super.onPause()
        buttonClicked = false
        cancelObservers()
    }

    private fun setupUI() {

        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //    TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //  TODO("Not yet implemented")
            }

            override fun afterTextChanged(p0: Editable?) {
                updateUI()
            }
        })


        binding.phoneNumber.setOnFocusChangeListener { view, hasFocus ->
            handleTouch()
            if (!hasFocus) {
                hideKeyboard(view)
                updateUI()
            }
        }


        binding.signupWithPhoneButton.setOnClickListener {
            buttonClicked = true
            updateUI()
            val phoneNumber = binding.phoneNumber.text.toString()
            // showSnackBar(binding.root, "Implementar en el viewmodel el medoto signUpWithPhoneNumber(phoneNumber)")
            viewModel.onSignUpWithPhoneNumber(phoneNumber);
        }
    }

    private fun updateUI() {
        val phoneNumber = binding.phoneNumber.text.toString()
        binding.signupWithPhoneButton.isEnabled = isValidPhoneNumber(phoneNumber) && !buttonClicked
    }


    fun setupObservers() {
        viewModel.viewStatus.observe(this, {
            when (it) {
                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Success -> {
                    hideLoader()
                    var response = it.data

                    when (response) {
                        is String -> {
                            showVerificationByEmailSent()
                        }

                        is HashMap<*, *> -> {

                            val hashMap = response as HashMap<String, Any>
                            val action =
                                SignUpWithPhoneActivityViewModel.SignUpWithPhoneActionsEnum.valueOf(
                                    hashMap["action"] as String
                                )
                            when (action) {
                                SignUpWithPhoneActivityViewModel.SignUpWithPhoneActionsEnum.VERIFY_OTP -> {
                                    val storedVerificationId =
                                        hashMap["storedVerificationId"] as String
                                    val resendToken =
                                        hashMap["resendToken"] as PhoneAuthProvider.ForceResendingToken
                                    requestOTPVerification(storedVerificationId, resendToken)
                                }

                                SignUpWithPhoneActivityViewModel.SignUpWithPhoneActionsEnum.IS_AUTHENTICATED -> {
                                    TODO()
                                }

                                else -> {
                                }


                            }

                        }

                        else -> {
                        }
                    }
                    //              viewModel.onUserAuthenticated(it.data!!)
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(it.message!!)
                    buttonClicked = false
                    updateUI()
                }

                else -> {
                    hideLoader()
                    buttonClicked = false
                    updateUI()
                    showErrorDialog("Error")
                }
            }
        })
    }

    fun cancelObservers() {
        viewModel.viewStatus.removeObservers(this)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }
    //-------------------------


    fun requestOTPVerification(
        storedVerificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {

        val map: HashMap<String, String> = HashMap()

        map["phone_number"] = binding.phoneNumber.text.toString()
        val nextIntent =
            Intent(this@SignUpWithPhoneActivity, OtpVerificationActivity::class.java)
        nextIntent.putExtra("action", OTP_ACTION_SIGNING)
        nextIntent.putExtra("storedVerificationId", storedVerificationId)
        nextIntent.putExtra("resend_token", resendToken)
        nextIntent.putExtra("user_data", map)
        startActivity(nextIntent)

        //    finish()
    }

    fun onSignUpWithEmailLinkDone() {
        hideLoader()
        startActivity(LoginActivity::class.java, null)
    }

    fun userAuthCreationByPhoneIsDone() {
        TODO("Not yet implemented")
    }

    fun enableLoginButton() {
        TODO("Not yet implemented")
    }

    fun disableLoginButton() {
        TODO("Not yet implemented")
    }

    fun goBack() {
        //    finish()
    }

    /*
        override fun showError(errorString: String) {
            buttonClicked = false
            showError(R.string.error, errorString, R.string.close)
        }
    */
    /*
      override fun showError(titleId: Int, messageId: Int, closeButtonText: Int) {
          buttonClicked = false

          val title = getString(titleId)
          val message = getString(messageId)
          val buttonCaption = getString(closeButtonText)
          showErrorDialog(
              title, message, buttonCaption
          ) { updateUI() }
      }

      override fun showError(titleId: Int, message: String, closeButtonText: Int) {
          showErrorDialog(getString(titleId), message, getString(closeButtonText), null)
          buttonClicked = false
      }
  */

    fun onErrorUserDoesNotExists() {

        showErrorDialog(getString(R.string.error_user_does_not_exists))
        /*
                var errorDialog =
                    NotificationDialog(
                        this,
                        this,
                        R.string.error,
                        R.string.error_user_does_not_exists,
                        R.string.close
                    )
                errorDialog.show()
          */
    }

    fun showVerificationByEmailSent() {

        showErrorDialog(
            getString(R.string.verification_mail_sent),
            getString(R.string.verification_mail_sent_description),
            getString(R.string.close)
        ) { (this@SignUpWithPhoneActivity).finish() }

    }


    fun onError(exception: Exception) {
        hideLoader()
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

}
