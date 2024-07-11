package com.iyr.ian.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.AppConstants.Companion.FIRSTNAME_MINIMUM_LENGTH
import com.iyr.ian.AppConstants.Companion.LASTNAME_MINIMUM_LENGTH
import com.iyr.ian.ui.signup.dialogs.SignupErrorDialog
import com.iyr.ian.AppConstants.Companion.PASSWORD_MINIMUM_LENGTH
import com.iyr.ian.AppConstants.Companion.USERNAME_MINIMUM_LENGTH
import com.iyr.ian.Constants.Companion.OTP_ACTION_SIGNING
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivitySignUpBinding
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.otp.OtpVerificationActivity
import com.iyr.ian.utils.NetworkUtils
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import com.iyr.ian.utils.Validators.isValidMail
import com.iyr.ian.utils.Validators.isValidPhoneNumber
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.startActivity
import java.util.*


interface SignUpActivityCallback {
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

class SignUpActivity : AppCompatActivity(), SignUpActivityCallback {

    // private lateinit var mErrorDialog: NotificationDialog
   // private lateinit var mPresenter: SignUpPresenter
    private lateinit var activitySignUpBinding: ActivitySignUpBinding
    private var buttonPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     //   mPresenter = SignUpPresenter(this, this)

        activitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        statusBarTransparent()
        /*
        activitySignUpBinding.nameET.setText("Romancito")
        activitySignUpBinding.firstName.setText("Roman Javier")
        activitySignUpBinding.lastName.setText("Canoniero")
        activitySignUpBinding.emailET.setText("+5491161274148")
        activitySignUpBinding.securityCode.setText("1234")
        */
        setupUI()
        updateUI()
    }

    private fun setupUI() {

        activitySignUpBinding.backArrow.setOnClickListener {
            onBackPressed()
        }



        activitySignUpBinding.nameET.filters = arrayOf(
            InputFilter { cs, start, end, spanned, dStart, dEnd ->
                // TODO Auto-generated method stub
                if (cs == "") { // for backspace
                    return@InputFilter cs
                }
                if (cs.toString().matches(Regex("[a-zA-Z0-9_-]+"))) { // here no space character
                    cs
                } else ""
            }
        )

        activitySignUpBinding.nameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (buttonPressed) {
                    updateUI()
                }
            }
        })

        activitySignUpBinding.emailET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                //       if (buttonPressed) {
                updateUI()
                //     }
            }
        })

        activitySignUpBinding.passwordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                //       if (buttonPressed) {
                updateUI()
                //     }
            }
        })

        activitySignUpBinding.switchSignupWithEmailLink.setOnCheckedChangeListener { compoundButton, isOn ->
            if (isOn) {
                activitySignUpBinding.passwordInputSection.visibility = View.GONE
            } else {
                activitySignUpBinding.passwordInputSection.visibility = View.VISIBLE
            }
        }


        activitySignUpBinding.signupButton.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                hideKeyboard()
            }
        }

        activitySignUpBinding.signupButton.setOnClickListener {
            buttonPressed = true
            val displayName: String = activitySignUpBinding.nameET.text.toString()
            val firstName: String = activitySignUpBinding.firstName.text.toString()
            val lastName: String = activitySignUpBinding.lastName.text.toString()
            val emailOrNumber = activitySignUpBinding.emailET.text.toString()
            val password = activitySignUpBinding.passwordET.text.toString()
            val switchLoginWithEmail = activitySignUpBinding.switchSignupWithEmailLink.isChecked
            val securityCode = activitySignUpBinding.securityCode.text.toString()
            val isOktoSignup =
                displayName.length >= USERNAME_MINIMUM_LENGTH && firstName.isNotEmpty() && lastName.isNotEmpty() && emailOrNumber.length >= 12 && ((isValidMail(
                    emailOrNumber
                ) &&
                        (switchLoginWithEmail || (password.length >= PASSWORD_MINIMUM_LENGTH && StringUtils.isDigitsAndLetters(
                            password
                        ))))
                        || isValidPhoneNumber(emailOrNumber)) && securityCode.isNotEmpty()


            var errorKeys = ArrayList<String>()
            if (displayName.length < USERNAME_MINIMUM_LENGTH) {
                errorKeys.add("display_name")
            }
            if (firstName.isEmpty()) {
                errorKeys.add("first_name")
            }

            if (lastName.isEmpty()) {
                errorKeys.add("last_name")
            }

            if (emailOrNumber.isEmpty()) {
                errorKeys.add("email_or_phone_number_empty")
            } else {
                if (!isValidPhoneNumber(emailOrNumber) && !isValidMail(emailOrNumber)) {
                    errorKeys.add("email_or_phone_number_unknown_format")

                } else if (isValidMail(emailOrNumber)) {
                    if (switchLoginWithEmail == false && (password.length < PASSWORD_MINIMUM_LENGTH || StringUtils.isDigitsAndLetters(
                            password
                        ))
                    ) {
                        errorKeys.add("password_invalid")
                    }
                }
            }
            if (securityCode.isEmpty()) {
                errorKeys.add("security_code_empty")
            } else if (securityCode.reversed() == securityCode) {
                errorKeys.add("security_code_invalid")
            }



            if (errorKeys.size > 0) {
                var signupErrorDialog = SignupErrorDialog(this, errorKeys)
                signupErrorDialog.show()
            } else {
                showLoader(getString(R.string.please_wait))

                if (NetworkUtils.getConnectionType(this) != NetworkUtils.CONNECTION_TYPE_NONE) {
                    val displayName: String = activitySignUpBinding.nameET.text.toString()
                    val firstName = activitySignUpBinding.firstName.text.toString()
                    val lastName = activitySignUpBinding.lastName.text.toString()
                    val emailOrNumber = activitySignUpBinding.emailET.text.toString()
                    val password = activitySignUpBinding.passwordET.text.toString()
                    val securityCode = activitySignUpBinding.securityCode.text.toString()

                    if (isValidMail(emailOrNumber)) {
                        if (!switchLoginWithEmail) {


                            showSnackBar(activitySignUpBinding.root, "Implementar la llamada en el ViewModel al metodo createUserWithEmailPassword")

                            /*
                            mPresenter.createUserWithEmailPassword(
                                displayName,
                                firstName,
                                lastName,
                                emailOrNumber,
                                password,
                                securityCode
                            )
                            */
                        } else {

                            showSnackBar(activitySignUpBinding.root, "Implementar la llamada en el ViewModel al metodo createUserWithEmailLink")

/*
                            mPresenter.createUserWithEmailLink(
                                displayName,
                                firstName,
                                lastName,
                                emailOrNumber,
                                securityCode
                            )

 */
                        }
                    } else {

                        showSnackBar(activitySignUpBinding.root, "Implementar la llamada en el ViewModel al metodo createUserWithPhoneNumber")

/*
                        mPresenter.createUserWithPhoneNumber(
                            displayName,
                            firstName,
                            lastName,
                            emailOrNumber,
                            securityCode
                        )*/
                    }
                } else {
                    hideLoader()
                    this@SignUpActivity.showError(
                        R.string.connectivity,
                        R.string.no_connectivity,
                        R.string.close
                    )

                    activitySignUpBinding.signupButton.isEnabled = true
                }
            }
            updateUI()

        }


        activitySignUpBinding.termsOfService.setOnClickListener {
            //    startActivity(LoginActivity::class.java, null)
            goBack()
        }
    }

    private fun updateUI() {

        var isOkDisplayName = true
        var isOkEmailOrNumber = true
        var isOkPassword = true

        val fullName: String = activitySignUpBinding.nameET.text.toString()
        val firstName = activitySignUpBinding.firstName.text.toString()
        val lastName = activitySignUpBinding.lastName.text.toString()
        val emailOrNumber = activitySignUpBinding.emailET.text.toString()
        val password = activitySignUpBinding.passwordET.text.toString()
        var switchLoginWithEmail = activitySignUpBinding.switchSignupWithEmailLink.isChecked
        val securityCode = activitySignUpBinding.securityCode.text.toString()


        if (isValidPhoneNumber(emailOrNumber)) {
            activitySignUpBinding.passwordSection.visibility = View.GONE
        } else {
            activitySignUpBinding.passwordSection.visibility = View.VISIBLE
            if (activitySignUpBinding.switchSignupWithEmailLink.isChecked) {
                activitySignUpBinding.passwordInputSection.visibility = View.GONE
            } else {
                activitySignUpBinding.passwordInputSection.visibility = View.VISIBLE
            }

        }

        isOkDisplayName = fullName.length >= USERNAME_MINIMUM_LENGTH
        if (!isOkDisplayName) {
            if (buttonPressed) {
                activitySignUpBinding.nameET.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )

            }
        } else {
            activitySignUpBinding.nameET.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }

        //--------------
        val isOkFirstName = firstName.length >= FIRSTNAME_MINIMUM_LENGTH
        if (!isOkFirstName) {
            if (buttonPressed) {
                activitySignUpBinding.firstName.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )

            }
        } else {
            activitySignUpBinding.firstName.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }

        //--------------
        val isOkLastName = lastName.length >= LASTNAME_MINIMUM_LENGTH
        if (!isOkLastName) {
            if (buttonPressed) {
                activitySignUpBinding.lastName.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )

            }
        } else {
            activitySignUpBinding.lastName.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }

/*
        if (!switchLoginWithEmail) {
            activitySignUpBinding.passwordSection.visibility = View.VISIBLE
        } else {
            activitySignUpBinding.passwordSection.visibility = View.GONE
        }
*/
        isOkEmailOrNumber = emailOrNumber.isNotEmpty()
        if (!isOkEmailOrNumber) {
            if (buttonPressed) {

                activitySignUpBinding.emailET.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )
            }
        } else {
            if (isValidPhoneNumber(emailOrNumber)) // si es un numero de telefono
            {
                if (buttonPressed) {
                    isOkEmailOrNumber = emailOrNumber.length >= 13
                    if (!isOkEmailOrNumber) {
                        activitySignUpBinding.emailET.setCompoundDrawablesWithIntrinsicBounds(
                            AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                            null,
                            null,
                            null
                        )
                    }
                } else {
                    activitySignUpBinding.emailET.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }
            } else {
                isOkEmailOrNumber = isValidMail(emailOrNumber)
                if (!isOkEmailOrNumber) {
                    if (buttonPressed) {

                        activitySignUpBinding.emailET.setCompoundDrawables(
                            AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                            null,
                            null,
                            null
                        )
                    }
                } else {
                    activitySignUpBinding.emailET.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }
            }
        }

        isOkPassword = isValidPhoneNumber(emailOrNumber) ||
                (password.length >= PASSWORD_MINIMUM_LENGTH &&
                        StringUtils.isDigitsAndLetters(password) &&
                        password != password.lowercase(Locale.getDefault())
                        )

        if (!isOkPassword) {
            activitySignUpBinding.passwordET

            if (buttonPressed) {

                activitySignUpBinding.passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )
            }
        } else {
            activitySignUpBinding.passwordET.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }


        //--------------
        val isOkSecurityCode =
            securityCode.length == 4 && securityCode != securityCode.reversed()
        if (!isOkSecurityCode) {
            if (buttonPressed) {
                activitySignUpBinding.securityCode.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                    null,
                    null,
                    null
                )

            }
        } else {
            activitySignUpBinding.securityCode.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }


/*
        activitySignUpBinding.signupButton.isEnabled =
            isOkDisplayName && isOkEmailOrNumber && isOkPassword
*/
/*
 */
        if (isValidPhoneNumber(emailOrNumber)) // si es un numero de telefono
        {
            /*
            activitySignUpBinding.signupButton.isEnabled =
                fullName.length >= USERNAME_MINIMUM_LENGTH &&
                        emailOrNumber.length >= 13
    */
        } else {
            /*
            activitySignUpBinding.signupButton.isEnabled =
                fullName.length >= USERNAME_MINIMUM_LENGTH &&
                        emailOrNumber.length >= 13 &&
                        isValidMail(emailOrNumber) &&
                        password.length >= PASSWORD_MINIMUM_LENGTH
                        && StringUtils.isDigitsAndLetters(password)
    */
        }

    }


    //-------------------------
    override fun onSignUpByPhoneSuccess() {

    }


    override fun requestOTPVerification(
        storedVerificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {
        //      startActivity(Intent(this, OtpVerificationActivity::class.java))
        val fullName: String = activitySignUpBinding.nameET.text.toString()
        val emailOrNumber = activitySignUpBinding.emailET.text.toString()
        var password = activitySignUpBinding.passwordET.text.toString()

        val map: HashMap<String, String> = HashMap()
        map["display_name"] = fullName
        map["phone_number"] = emailOrNumber

        val nextIntent =
            Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
        intent.extras?.let {
            nextIntent.putExtras(it)
        }
        nextIntent.putExtra("action", OTP_ACTION_SIGNING)
        nextIntent.putExtra("storeVerificationId", storedVerificationId)
        nextIntent.putExtra("resendToken", resendToken)
        nextIntent.putExtra("user_data", map)
        startActivity(nextIntent)


        finish()
    }

    override fun onSignUpWithEmailLinkDone() {
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
        finish()
    }


    override fun showError(errorString: String) {
        showError(R.string.error, errorString, R.string.close)
    }

    override fun showError(titleId: Int, messageId: Int, closeButtonText: Int) {

        val title = getString(titleId)
        val message = getString(messageId)
        val buttonCaption = getString(closeButtonText)
        showErrorDialog(
            title, message, buttonCaption
        ) { updateUI() }
    }

    override fun showError(titleId: Int, message: String, closeButtonText: Int) {
        showErrorDialog(getString(titleId), message, getString(closeButtonText), null)
    }


    override fun onErrorUserDoesNotExists() {

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

    override fun showVerificationByEmailSent() {

        showErrorDialog(
            getString(R.string.verification_mail_sent),
            getString(R.string.verification_mail_sent_description),
            getString(R.string.close)
        ) { (this@SignUpActivity).finish() }

    }


    override fun onError(exception: Exception) {
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
