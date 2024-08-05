package com.iyr.ian.ui.setup.pin_setup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.iyr.fewtouchs.ui.views.setup.pin_setup.PinSetupActivityViewModel
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityPinSetupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.mukesh.OnOtpCompletionListener
import java.util.Locale


class PinSetupFragment : Fragment(), PinSetupActivityCallback {

    private var buttonClicked: Boolean = false
    private var isFirstSetup: Boolean = true
    private var originalObject: User? = null
    private lateinit var currentObject: User

    // private lateinit var mPresenter: PinSetupPresenter
    private lateinit var viewModel: PinSetupActivityViewModel
    private lateinit var binding: ActivityPinSetupBinding


    private val args: PinSetupFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_pin_setup)


//        setContentView(binding.root)
        //      supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = PinSetupActivityViewModel()

        //     mPresenter = PinSetupPresenter(this, this)


        //   getIntentData()
        setupUI()
        setupObservers()

        /*

        ACA

        intent.extras?.let { extras ->
            viewModel.setExtraData(extras)
        }

         */
    }

    var loader = LoadingDialogFragment()
    private fun setupObservers() {

        viewModel.viewStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Setup Updated Successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    onSaveDone(status.data!!)
                    //  onUserAuthenticated(status.data!!)
                }

                is Resource.Error -> {

                    requireActivity().hideLoader()
                    var errorMessage =
                        when (status.message.toString().lowercase(Locale.getDefault())) {
                            "the password is invalid or the user does not have a password." -> getString(
                                R.string.login_error_invalid_password_or_username
                            )

                            "there is no user record corresponding to this identifier. the user may have been deleted." -> getString(
                                R.string.login_error_user_doest_not_exists
                            )

                            else -> status.message.toString()
                        }

                    requireActivity().showErrorDialog(errorMessage)
                }
            }
        }


        viewModel.isFirstSetup.observe(this) { value ->
            binding.backArrows.isVisible = !value
        }

        viewModel.pinCode.observe(this) { code ->
            binding.otpView.setText(code!!)
        }

        viewModel.saveButtonEnabled.observe(this) { enabled ->
            binding.nextButton.isEnabled = enabled

        }
    }


    private fun getIntentData() {

        /*
              if (intent.hasExtra("data_object")) {
                  currentObject = Gson().fromJson(
                      intent.getStringExtra("data_object"),
                      User::class.java
                  )
                  originalObject = Gson().fromJson(
                      intent.getStringExtra("data_object"),
                      User::class.java
                  )
                  binding.otpView.setText(currentObject.security_code)
              }
              isFirstSetup = if (intent.hasExtra("first_setup")) {
                  intent.getBooleanExtra("first_setup", true)
              } else {
                  true
              }

         */
    }


    fun setupUI() {


        binding.nextButton.setOnClickListener(View.OnClickListener {
            requireActivity().handleTouch()
            requireActivity().hideKeyboard()
            viewModel.onSaveButtonClicked()
            /*
             binding.nextButton.isEnabled = false
             UIUtils.hideSoftKeyboard(binding.otpView)
             buttonClicked = true
             (this@PinSetupActivity).runOnUiThread {
                 val code: String = binding.otpView.text.toString()
                 mPresenter.savePin(code)
             }*/
        })

        binding.otpView.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String?) {
                requireActivity().hideKeyboard()
                viewModel.setPinCode(otp)
            }
        })

        binding.otpView.addTextChangedListener { text ->
            viewModel.setPinCode(text.toString())

        }
    }

    private fun updateUI() {
        binding.nextButton.isEnabled = !binding.otpView.text.toString().isBlank() &&
                binding.otpView.text.toString().length == 4 &&
                binding.otpView.text.toString() != binding.otpView.text.toString().reversed() &&
                !buttonClicked

//        binding.saveButton.isEnabled
    }


    override fun onSaveDone(user: User) {

        args.bundle?.let {
            var bundle = it

            bundle.putBoolean("first_setup", false)
            bundle.putString("data_object", Gson().toJson(user))
            //       val intent = Intent(this@PinSetupActivity, SpeedDialSetupActivity::class.java)

            if (user.allow_speed_dial == null) {

                //           val action = PinSetupFragmentDirections.actionPinSetupFragmentToSpeedDialSetupFragment()
                //             Intent(requireContext(), SpeedDialSetupActivity::class.java)

                val action =
                    PinSetupFragmentDirections.actionPinSetupFragmentToSpeedDialSetupFragment(bundle)
                findNavController().navigate(action)
            } else if (!requireActivity().areLocationPermissionsGranted()) {
//                Intent(requireContext(), LocationRequiredActivity::class.java)
                val action =
                    PinSetupFragmentDirections.actionPinSetupFragmentToLocationRequiredFragment(
                        bundle
                    )
                findNavController().navigate(action)
                // val action = Pin
            } else {

                val action =
                    PinSetupFragmentDirections.actionPinSetupFragmentToAddContactsFromPhoneFragment(
                        bundle
                    )
                findNavController().navigate(action)

//                Intent(requireContext(), AddContactsFromPhoneActivity::class.java)
                //       val action =

            }
            /*
                        intent?.putExtras(bundle)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
            */
        }
    }


    override fun onError(exception: Exception) {
        buttonClicked = false
        requireActivity().hideLoader()
        requireActivity().showErrorDialog(exception.localizedMessage)

    }
}