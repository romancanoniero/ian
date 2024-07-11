package com.iyr.ian.ui.settings.press_or_tap_setup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityPressOrTapSetupBinding
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.SettingsFragmentViewModel
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivityCallback
import com.iyr.ian.ui.setup.press_or_tap_setup.SOSActivationMethods
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.triggertrap.seekarc.SeekArc
import java.util.Timer
import java.util.TimerTask


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PressOrTapSetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PressOrTapSetupFragment(
    val mainActivityViewModel: MainActivityViewModel,
    val settingsFragmentViewModel: SettingsFragmentViewModel,
    val _interface: ISettingsFragment
) : Fragment(),
    PressOrTapSetupActivityCallback {
    private lateinit var binding: ActivityPressOrTapSetupBinding
    private var selectedToggleButton: SOSActivationMethods = SOSActivationMethods.HOLD
    private var startTimeCounter: Long = 0
    private var timer: Timer? = null

    var viewModel = PressOrTapSetupFragmentViewModel()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = ActivityPressOrTapSetupBinding.inflate(layoutInflater, container, false)
        setupUI()
        //   binding.seekCounter.progress = 0
        updateUI()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.sos_settings))
        }
        setupObservers()
    }


    override fun onPause() {
        removeObservers()
        super.onPause()
    }


    private fun setupObservers() {

        settingsFragmentViewModel.user.observe(viewLifecycleOwner) { user ->

            viewModel.setUser(user)

            var newActivationMethod =
                SOSActivationMethods.valueOf(user?.sos_invocation_method ?: "")
            if (selectedToggleButton == null || selectedToggleButton != newActivationMethod) {
                viewModel.setButtonMode(newActivationMethod)
                updateInvocationMethod(newActivationMethod)
            }
            viewModel.setCounter(user?.sos_invocation_count ?: 0)
        }

        viewModel.counter.observe(this) { value ->
            if (binding.seekCounter.progress != value) {
                binding.seekCounter.progress = value
                binding.counter.text = value.toString()
                if (value > 0) {
                    if (value >= 3 || viewModel.buttonMode.value?.name ?: "" != viewModel.getUser()?.sos_invocation_method)
                        binding.saveButton.visibility = VISIBLE
                    else
                        binding.saveButton.visibility = GONE
                    binding.saveButton.isEnabled = value > 0
                    binding.resetButton.isEnabled = value > 0
                } else {
                    binding.saveButton.visibility = GONE
                }
            }
        }

        viewModel.buttonMode.observe(this) { mode ->
            if (mode != selectedToggleButton) {
                when (mode) {
                    SOSActivationMethods.HOLD -> {

                        selectedToggleButton = SOSActivationMethods.HOLD
                        binding.toggleTapButton.isChecked = false
                        binding.buttonText.text = getString(R.string.press_here)
                        // binding.seekCounter.progress = 0
                        viewModel.setButtonMode(selectedToggleButton)
                    }

                    SOSActivationMethods.TAP -> {
                        selectedToggleButton = SOSActivationMethods.TAP
                        binding.toggleHoldButton.isChecked = false
                        binding.buttonText.text = getString(R.string.tap_here)
                        //  binding.seekCounter.progress = 0

                        viewModel.setButtonMode(selectedToggleButton)
                    }

                    null -> TODO()
                }
            }
            if (mode.name != viewModel.getUser()?.sos_invocation_method)
                binding.saveButton.visibility = VISIBLE
            else
                binding.saveButton.visibility = GONE
        }

        viewModel.savingStatus.observe(this) { status ->
            when (status) {
                is Resource.Error -> {
                    mainActivityViewModel.showError(status.message.toString())
                }

                is Resource.Loading -> {
                    mainActivityViewModel.showLoader()
                }

                is Resource.Success -> {
                    mainActivityViewModel.hideLoader()
                    viewModel.resetSavingStatus()
                    mainActivityViewModel.goBack()

                   // settingsFragmentViewModel.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
//                    mainActivityViewModel.switchToModule(SettingsFragmentsEnum.LANDING.ordinal,"landing")

                    //settingsFragmentViewModel.fragmentVisible.value = SettingsFragmentsEnum.LANDING


                }
                null ->
                {}

            }
            //       mainActivityViewModel.showLoader()
        }
    }


    private fun removeObservers() {
        settingsFragmentViewModel.user.removeObservers(this)
        viewModel.counter.removeObservers(this)
        viewModel.buttonMode.removeObservers(this)
        viewModel.savingStatus.removeObservers(this)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            context: Context,
            mainActivityViewModel: MainActivityViewModel,
            settingsFragmentViewModel: SettingsFragmentViewModel,
            _interface: ISettingsFragment
        ) =
            PressOrTapSetupFragment(mainActivityViewModel, settingsFragmentViewModel, _interface)
    }


    private fun setupUI() {

        binding.backArrows.visibility = GONE
        binding.saveButton.visibility = GONE
        binding.seekCounter.setOnSeekArcChangeListener(object : SeekArc.OnSeekArcChangeListener {
            override fun onProgressChanged(p0: SeekArc?, progress: Int, p2: Boolean) {
                if (progress != viewModel.counter.value) {
                    if (progress > 6) {
                        viewModel.resetCounter()
                    } else {
                        binding.counter.text = progress.toString()
                    }
                }

            }

            override fun onStartTrackingTouch(p0: SeekArc?) {

            }

            override fun onStopTrackingTouch(p0: SeekArc?) {

            }
        })

        binding.toggleHoldButton.setOnClickListener {
            viewModel.setButtonMode(SOSActivationMethods.HOLD)
            binding.buttonText.text = getString(R.string.press_here)
            binding.toggleHoldButton.isChecked = true
            binding.toggleTapButton.isChecked = false
            //          updateChanges()
        }

        binding.toggleTapButton.setOnClickListener {
            viewModel.setButtonMode(SOSActivationMethods.TAP)
            binding.buttonText.text = getString(R.string.tap_here)
            binding.toggleHoldButton.isChecked = false
            binding.toggleTapButton.isChecked = true
//            updateChanges()
        }


        binding.seekCounter.setOnClickListener { binding.dummyButtonImage.callOnClick() }
        binding.seekCounter.setOnTouchListener { v, event ->
            binding.dummyButtonImage.dispatchTouchEvent(event)
        }

        binding.dummyButtonImage.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                when (selectedToggleButton) {
                    SOSActivationMethods.HOLD -> {

                        if (event.action == MotionEvent.ACTION_DOWN) {

                            //    binding.seekCounter.progress = 0
                            viewModel.resetCounter()
                            if (timer != null) {
                                timer?.cancel()
                                timer?.purge()
                            }

                            timer = Timer()

                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    //vibrateOnTouch(this@PressOrTapSetupActivity.baseContext, true)
                                    requireActivity().handleTouch()

                                    requireActivity().runOnUiThread {
                                        if (binding.seekCounter.progress > 6) {
                                            binding.seekCounter.progress = 0
                                        }
                                        //                  binding.seekCounter.progress =
                                        binding.seekCounter.progress + 1

                                        viewModel.incCounter()
                                        updateChanges()

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
                                    //binding.seekCounter.progress = 0
                                    viewModel.resetCounter()
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


        binding.dummyButtonImage.setOnClickListener {
            if (selectedToggleButton == SOSActivationMethods.TAP) {
                requireActivity().handleTouch()
                if (viewModel.counter.value ?: 0 >= 6) {
                    viewModel.resetCounter()
                }
                viewModel.incCounter()
            }

        }

        binding.resetButton.setOnClickListener {
            //binding.seekCounter.progress = 0
            viewModel.resetCounter()

            //            updateChanges()
        }

        binding.saveButton.setOnClickListener {
            //mPresenter.save(selectedToggleButton, binding.seekCounter.progress)
            viewModel.onSaveClick()
        }
    }

    private fun updateChanges() {
        /*
              val me =
                  SessionForProfile.getInstance(requireContext())
                      .getUserProfile()
              me.sos_invocation_count = binding.seekCounter.progress
              SessionForProfile.getInstance(requireContext())
                  .storeUserProfile(me)
      */
        if (binding.seekCounter.progress > 0) {
            //mPresenter.save(selectedToggleButton, binding.seekCounter.progress)
        }

    }

    private fun updateUI() {
        /*
                val me =
                    SessionForProfile.getInstance(requireContext()).getUserProfile()

                binding.seekCounter.progress = me.sos_invocation_count

                updateInvocationMethod(selectedToggleButton)
        */

        binding.saveButton.isEnabled = binding.seekCounter.progress > 0
        binding.resetButton.isEnabled = binding.seekCounter.progress > 0

    }

    private fun updateInvocationMethod(mode: SOSActivationMethods) {
        when (mode) {
            SOSActivationMethods.HOLD -> {
                binding.buttonText.text = getString(R.string.press_here)
                binding.toggleHoldButton.isChecked = true
                binding.toggleTapButton.isChecked = false
            }

            SOSActivationMethods.TAP -> {
                binding.buttonText.text = getString(R.string.tap_here)
                binding.toggleHoldButton.isChecked = false
                binding.toggleTapButton.isChecked = true

            }
        }
        selectedToggleButton = mode

    }

    override fun onSaveDone(user: User) {
        // TODO("Not yet implemented")
    }

    override fun onError(exception: Exception) {
        requireActivity().showErrorDialog(exception.localizedMessage.toString())
    }

    fun save(sosInvocationMethod: SOSActivationMethods, sosInvocationCount: Int) {
        //mPresenter.save(sosInvocationMethod, sosInvocationCount)
    }
}