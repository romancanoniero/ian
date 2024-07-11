 package com.iyr.ian.ui.setup.speed_dial_setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySpeedDialSetupBinding
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.SpeedDialSetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


 interface SpeedDialSetupActivityCallback {
    fun onSaveDone(user: User)
    fun onError(exception: Exception)
}


class SpeedDialSetupActivity : AppCompatActivity(), SpeedDialSetupActivityCallback {

    private var buttonClicked: Boolean = false
 //   private lateinit var mPresenter: SpeedDialSetupPresenter
    private lateinit var binding: ActivitySpeedDialSetupBinding

    private lateinit var viewModel: SpeedDialSetupViewModel


    override fun onBackPressed() {
        if (viewModel.isFirstSetup.value == false)
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

  //      speak("Si quieres que tus contactos te puedan agregar a su listado de discado rápido debes seleccionar la opcion SI. Igual, puedes activar esta opción más adelante.")

        viewModel = SpeedDialSetupViewModel()



        binding = ActivitySpeedDialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
   //     mPresenter = SpeedDialSetupPresenter(this, this)
        //getIntentData()
        setupUI()
        setupObservers()

        getIntentData()
        intent.extras?.let { extras ->
            viewModel.setExtraData(extras)
        }
    }


    private var originalObject: User? = null
    private lateinit var currentObject: User
    private fun getIntentData() {
        if (intent.hasExtra("data_object")) {
            currentObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
            originalObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
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

        binding.switchSpeedDial.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllowSpeedDial(isChecked)
        }

        binding.nextButton.setOnClickListener(View.OnClickListener {
            //mPresenter.save(binding.switchSpeedDial.isChecked)
            handleTouch()
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.onSaveButtonClicked()
                }
            }
        })
    }

    var loader = LoadingDialogFragment()
    private fun setupObservers() {
        viewModel.viewStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    if (!loader.isVisible) {
                        loader.show(supportFragmentManager, "loader_frames")
                    }
                }

                is Resource.Success -> {
                    loader.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "Setup Updated Successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    onSaveDone(status.data!!)
                    //  onUserAuthenticated(status.data!!)
                }

                is Resource.Error -> {
                    loader.dismiss()

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

                    showErrorDialog(errorMessage)
                }
            }
        }

        viewModel.isFirstSetup.observe(this) { value ->
            binding.backArrows.isVisible = !value
        }

        viewModel.allowToAdd.observe(this) { allowed ->
            binding.switchSpeedDial.isChecked = allowed ?: false
        }

    }

    private fun updateUI() {

    }
/*
    override val AddContactsFromPhoneActivity: Unit
        get() = TODO("Not yet implemented")
*/

    override fun onSaveDone(user: User) {

        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        //  val intent = Intent(this@SpeedDialSetupActivity, AddContactsFromPhoneActivity::class.java)


        intent = Intent(this@SpeedDialSetupActivity, AddContactsFromPhoneActivity::class.java)


        /*
        intent = if (!areLocationPermissionsGranted()) {
            Intent(this@SpeedDialSetupActivity, LocationRequiredActivity::class.java)
        } else {
            Intent(this@SpeedDialSetupActivity, AddContactsFromPhoneActivity::class.java)
        }*/
        intent?.putExtras(bundle)
    //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    override fun onError(exception: Exception) {
        buttonClicked = false
        hideLoader()
        showErrorDialog(exception.localizedMessage)
    }
}