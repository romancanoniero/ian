package com.iyr.ian.ui.setup.press_or_tap_setup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.iyr.ian.AppConstants.ServiceCode.MINIMUM_TAPS
import com.iyr.ian.AppConstants.ServiceCode.MINIMUM_TOUCH_TIME
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityPressOrTapSetupBinding
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.setup.pin_setup.PinSetupActivity
import com.iyr.ian.ui.setup.speed_dial_setup.SpeedDialSetupActivity
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.px
import com.iyr.ian.utils.showErrorDialog
import com.triggertrap.seekarc.SeekArc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

enum class SOSActivationMethods {
    HOLD, TAP
}


interface PressOrTapSetupActivityCallback {
    fun onSaveDone(user: User)
    fun onError(exception: Exception)

}


class PressOrTapSetupActivity : AppCompatActivity(),
    PressOrTapSetupActivityCallback {


    private lateinit var viewModel: PressOrTapSetupActivityViewModel

    private var originalObject: User? = null
    private lateinit var currentObject: User
//    private lateinit var mPresenter: PressOrTapSetupPresenter
    private lateinit var binding: ActivityPressOrTapSetupBinding
    private var selectedToggleButton: SOSActivationMethods = SOSActivationMethods.HOLD
    private var startTimeCounter: Long = 0
    private var timer: Timer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

   //     speak("Ahora debes configurar como se activará el boton de panico. El Boton de Panico se puede activar tocando la cantidad de veces que establezcas el boton de panico o manteniendo pulsado el boton durante la cantidad de segundos que establezcas.")

        viewModel = PressOrTapSetupActivityViewModel()

        binding = ActivityPressOrTapSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        statusBarTransparent()

        setupUI()
        setupObservers()
        intent.extras?.let { extras ->
            viewModel.setExtraData(extras)
        }
        //      getIntentData()
        updateUI()
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
                    onSaveDone(status.data!!)
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

                else -> {}
            }
        }


        viewModel.isFirstSetup.observe(this) { value ->
            binding.backArrows.isVisible = !value
        }

        viewModel.sosInvocationCount.observe(this) { count ->
            if (count > 6) {
                viewModel.setProgress(0)
            } else {
                viewModel.setProgress(count)
            }
            binding.seekCounter.progress = count
            binding.counter.text = count.toString()
            //     updateUI()

        }

        viewModel.sosInvocationMethod.observe(this) { method ->
            when (method?.name) {

                SOSActivationMethods.HOLD.name -> {
                    selectedToggleButton = SOSActivationMethods.HOLD
                    binding.toggleHoldButton.isChecked = true
                    binding.toggleTapButton.isChecked = false
                    binding.buttonText.text = getString(R.string.press_here)
                    turnIntoHoldMode()

                    // binding.seekCounter.progress = 0
                }

                SOSActivationMethods.TAP.name -> {
                    selectedToggleButton = SOSActivationMethods.TAP
                    binding.toggleHoldButton.isChecked = false
                    binding.toggleTapButton.isChecked = true
                    binding.buttonText.text = getString(R.string.tap_here)
                    turnIntoTapMode()
                    //   binding.seekCounter.progress = 0
                }
            }


            viewModel.saveButtonEnabled.observe(this) { enabled ->
                binding.saveButton.isEnabled = enabled
            }

            viewModel.resetButtonEnabled.observe(this) { enabled ->
                binding.resetButton.isEnabled = enabled

            }

        }
    }

    var downTime: Long = 0


    private fun turnIntoTapMode() {
        binding.seekCounter.elevation = 2.px.toFloat()
        binding.seekCounter.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // El usuario ha tocado la pantalla, registramos el tiempo
                    downTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // El usuario ha levantado el dedo de la pantalla, comparamos el tiempo transcurrido
                    val upTime = System.currentTimeMillis()
                    if (upTime - downTime < 200) { // 200ms es un tiempo común para considerar un toque rápido
                        // Aquí puedes poner el código que se debe ejecutar cuando el usuario hace un "tap" rápido
                   //     Toast.makeText(this, "Tocaste la pantalla", Toast.LENGTH_SHORT).show()

                        if (selectedToggleButton == SOSActivationMethods.TAP) {
                            handleTouch()
                            if (binding.seekCounter.progress >= 6) {
                                viewModel.setProgress(1)
                                binding.seekCounter.progress = 1
                            } else {
                                binding.seekCounter.progress = binding.seekCounter.progress + 1
                                viewModel.setProgress(binding.seekCounter.progress)
                            }
                        }




                    }
                    true
                }
                else -> false
            }
        }


        binding.seekCounter.setOnClickListener {
            if (selectedToggleButton == SOSActivationMethods.TAP) {
                handleTouch()
                if (binding.seekCounter.progress >= 6) {
                    viewModel.setProgress(1)
                    binding.seekCounter.progress = 1
                } else {
                    binding.seekCounter.progress = binding.seekCounter.progress + 1
                    viewModel.setProgress(binding.seekCounter.progress)
                }
            }
        }

    }

    private fun turnIntoHoldMode() {

        binding.seekCounter.elevation = 2.px.toFloat()
        binding.seekCounter.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                when (selectedToggleButton) {
                    SOSActivationMethods.HOLD -> {
                        if (event.action == MotionEvent.ACTION_DOWN) {

                            binding.seekCounter.progress = 0
                            if (timer != null) {
                                timer?.cancel()
                                timer?.purge()
                            }

                            timer = Timer()

                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    //vibrateOnTouch(this@PressOrTapSetupActivity.baseContext, true)
                                    handleTouch()

                                    runOnUiThread {
                                        if (binding.seekCounter.progress >= 6) {
                                            binding.seekCounter.progress = 0
                                        }
                                        binding.seekCounter.progress =
                                            binding.seekCounter.progress + 1
                                        viewModel.setProgress(binding.seekCounter.progress)
                                    }
                                    Log.d(
                                        "PRESSING_BUTTON",
                                        binding.seekCounter.progress.toString()
                                    )
                                }
                            }, 0, 1000) //time out 5s
                        } else
                            if (event.action == MotionEvent.ACTION_UP) {
                                Log.d(
                                    "PRESSING_BUTTON",
                                    "Canceling"
                                )
                                if (binding.seekCounter.progress == 0) {
                                    binding.seekCounter.progress = 0
                                    viewModel.setProgress(binding.seekCounter.progress)
                                }
                                timer?.cancel()
                                timer?.purge()
                            }
                    }

                    else -> {}
                }

                return false
            }
        })
    }


    override fun onStop() {
        super.onStop()
        timer?.cancel()
        timer?.purge()
    }
    /*
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
                binding.seekCounter.progress = currentObject.sos_invocation_count
                binding.counter.text = currentObject.sos_invocation_count.toString()
                when (currentObject.sos_invocation_method) {
                    SOSActivationMethods.HOLD.name ->
                        binding.toggleHoldButton.performClick()

                    SOSActivationMethods.TAP.name ->
                        binding.toggleTapButton.performClick()

                }
            }

        }
    */

    private fun setupUI() {


        binding.backArrows.setOnClickListener {
            onBackPressed()
        }


        binding.seekCounter.setOnSeekArcChangeListener(object : SeekArc.OnSeekArcChangeListener {
            override fun onProgressChanged(p0: SeekArc?, progress: Int, user: Boolean) {
                binding.counter.text = progress.toString()

                /*
                                if (progress >= 6) {
                                    binding.seekCounter.progress = 0
                                }
                                binding.counter.text = progress.toString()
                                updateUI()

                 */
            }

            override fun onStartTrackingTouch(p0: SeekArc?) {

            }

            override fun onStopTrackingTouch(p0: SeekArc?) {

            }
        })


        binding.toggleHoldButton.setOnClickListener {
            viewModel.setMethod(SOSActivationMethods.HOLD)

        }

        binding.toggleTapButton.setOnClickListener {
            viewModel.setMethod(SOSActivationMethods.TAP)
        }







        binding.resetButton.setOnClickListener {
            binding.seekCounter.progress = 0
            viewModel.setProgress(0)
        }

        binding.saveButton.setOnClickListener {
            //     mPresenter.save(selectedToggleButton, binding.seekCounter.progress)
            handleTouch()
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.onSaveButtonClicked()
                }
            }
        }
    }

    private fun updateUI() {

        binding.saveButton.isEnabled = if (selectedToggleButton == SOSActivationMethods.TAP)
            binding.seekCounter.progress >= MINIMUM_TAPS
        else
            binding.seekCounter.progress >= MINIMUM_TOUCH_TIME
        binding.resetButton.isEnabled = binding.seekCounter.progress > 0
    }

    /*
        fun setCounterProgress(progress : Int)
        {
            binding.seekCounter.progress  =progress

        }
    */
    /*
    fun goBack() {
        finish()
    }
*/

    override fun onBackPressed() {
        if (viewModel.isFirstSetup.value == false)
            super.onBackPressed()
    }

    override fun onSaveDone(user: User) {

        val bundle = Bundle()
        bundle.putBoolean("first_setup", false)
        bundle.putString("data_object", Gson().toJson(user))
        //       val intent = Intent(this@PressOrTapSetupActivity, LocationRequiredActivity::class.java)




        intent = if (user.security_code.isBlank()) {
            Intent(this@PressOrTapSetupActivity, PinSetupActivity::class.java)
        } else if (user.allow_speed_dial == null) {
            Intent(this@PressOrTapSetupActivity, SpeedDialSetupActivity::class.java)
        } else if (!areLocationPermissionsGranted()) {
            Intent(this@PressOrTapSetupActivity, LocationRequiredActivity::class.java)
        } else {
            Intent(this@PressOrTapSetupActivity, AddContactsFromPhoneActivity::class.java)
        }
        intent?.putExtras(bundle)
 //       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onError(exception: Exception) {
        showErrorDialog(exception.localizedMessage)
    }
}
