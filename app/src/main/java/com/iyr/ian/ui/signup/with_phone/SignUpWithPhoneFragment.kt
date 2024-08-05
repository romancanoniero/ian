package com.iyr.ian.ui.signup.with_phone

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.PhoneAuthProvider
import com.iyr.ian.Constants.Companion.OTP_ACTION_SIGNING
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivitySignUpWithPhoneBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators.isValidPhoneNumber
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.SignUpWithPhoneActivityViewModel


class SignUpWithPhoneFragment : Fragment() {

    private var buttonClicked: Boolean = false

    private val viewModel by lazy {
        SignUpWithPhoneActivityViewModel(
            requireContext(),
            requireActivity()
        )
    }

    private lateinit var binding: ActivitySignUpWithPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_sign_up_with_phone)

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
            requireActivity().handleTouch()
            if (!hasFocus) {
                requireActivity().hideKeyboard(view)
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


    fun startObservers() {


        viewModel.viewStatus.observe(this, {
            when (it) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    var response = it.data

                    when (response) {
                        is String -> {
                            requireActivity().showErrorDialog(
                                getString(R.string.verification_sms_sent),
                                getString(R.string.verification_sms_sent_description),
                                getString(R.string.close)
                            ) {  }

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
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(it.message!!)
                    buttonClicked = false
                    updateUI()
                }

                else -> {
                    requireActivity().hideLoader()
                    buttonClicked = false
                    updateUI()
                    requireActivity().showErrorDialog("Error")
                }
            }
        })
    }

    fun stopObservers() {
        viewModel.viewStatus.removeObservers(this)
    }


    fun requestOTPVerification(
        storedVerificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    ) {

        val phoneNumber = binding.phoneNumber.text.toString()
        val action =
            SignUpWithPhoneFragmentDirections.actionSignUpWithPhoneFragmentToOtpVerificationActivity(
                OTP_ACTION_SIGNING,
                storedVerificationId,
                resendToken,
                phoneNumber
            )

        findNavController().navigate(action)
        /*
        val map: HashMap<String, String> = HashMap()
        map["phone_number"] = binding.phoneNumber.text.toString()
        val nextIntent =
            Intent(this@SignUpWithPhoneActivity, OtpVerificationActivity::class.java)
        nextIntent.putExtra("action", OTP_ACTION_SIGNING)
        nextIntent.putExtra("storedVerificationId", storedVerificationId)
        nextIntent.putExtra("resend_token", resendToken)
        nextIntent.putExtra("user_data", map)
        startActivity(nextIntent)
*/
        //    finish()
    }


    fun showVerificationByEmailSent() {

        requireActivity().showErrorDialog(
            getString(R.string.verification_mail_sent),
            getString(R.string.verification_mail_sent_description),
            getString(R.string.close)
        ) {  }

    }
/*

    fun onError(exception: Exception) {
        requireActivity().hideLoader()
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
*/
}
