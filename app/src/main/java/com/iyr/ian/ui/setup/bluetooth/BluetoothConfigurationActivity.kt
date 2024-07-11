package com.iyr.ian.ui.setup.bluetooth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityPinSetupBinding
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.showErrorDialog
import com.mukesh.OnOtpCompletionListener


interface BluetoothConfigurationActivityCallback {

    fun onError(exception: Exception)
}


class BluetoothConfigurationActivity : AppCompatActivity(), BluetoothConfigurationActivityCallback {

    private var buttonClicked: Boolean = false
    private var isFirstSetup: Boolean = true
    private var originalObject: User? = null
    private lateinit var currentObject: User
    //private lateinit var mPresenter: PinSetupPresenter
    private lateinit var binding: ActivityPinSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getIntentData()
        setupUI()
    }


    private fun getIntentData() {
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
    }


    override fun onResume() {
        super.onResume()
        buttonClicked = false
        updateUI()
    }


    fun setupUI() {

        binding.backArrows.setOnClickListener {
            onBackPressed()
        }
/*
        binding.nextButton.setOnClickListener(View.OnClickListener {
            binding.nextButton.isEnabled = false
            UIUtils.hideSoftKeyboard(binding.otpView)
            buttonClicked = true
            (this@PinSetupActivity).runOnUiThread {
                val code: String = binding.otpView.text.toString()
                mPresenter.savePin(code)
            }
        })

        */

        binding.otpView.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String?) {
                updateUI()
            }
        })
    }

    private fun updateUI() {
        binding.nextButton.isEnabled = !binding.otpView.text.toString().isBlank() &&
                binding.otpView.text.toString().length == 4 &&
                binding.otpView.text.toString() != binding.otpView.text.toString().reversed() &&
                !buttonClicked

//        binding.saveButton.isEnabled
    }

    override fun onError(exception: Exception) {
        buttonClicked = false
        hideLoader()
        showErrorDialog(exception.localizedMessage)

    }
}