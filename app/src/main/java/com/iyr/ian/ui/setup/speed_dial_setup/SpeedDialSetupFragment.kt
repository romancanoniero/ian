package com.iyr.ian.ui.setup.speed_dial_setup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySpeedDialSetupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.SpeedDialSetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class SpeedDialSetupFragment : Fragment() {

    private var buttonClicked: Boolean = false

    //   private lateinit var mPresenter: SpeedDialSetupPresenter
    private lateinit var binding: ActivitySpeedDialSetupBinding

    private lateinit var viewModel: SpeedDialSetupViewModel

    private val args: SpeedDialSetupFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //      speak("Si quieres que tus contactos te puedan agregar a su listado de discado rápido debes seleccionar la opcion SI. Igual, puedes activar esta opción más adelante.")

        viewModel = SpeedDialSetupViewModel()

        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_speed_dial_setup)

        setupUI()
        startObservers()

        getIntentData()
        args.let {
            viewModel.setExtraData(it.bundle!!)
        }
    }


    private var originalObject: User? = null
    private lateinit var currentObject: User
    private fun getIntentData() {
        if (args.bundle?.containsKey("data_object") == true) {
            currentObject = Gson().fromJson(
                args.bundle?.getString("data_object"), User::class.java
            )
            originalObject = Gson().fromJson(
                args.bundle?.getString("data_object"), User::class.java
            )
        }
    }


    override fun onResume() {
        super.onResume()
        buttonClicked = false
        updateUI()
    }


    fun setupUI() {

        binding.switchSpeedDial.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllowSpeedDial(isChecked)
        }

        binding.nextButton.setOnClickListener(View.OnClickListener {
            //mPresenter.save(binding.switchSpeedDial.isChecked)
            requireActivity().handleTouch()
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.onSaveButtonClicked()
                }
            }
        })
    }

    var loader = LoadingDialogFragment()
    private fun startObservers() {
        viewModel.viewStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    loader.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Setup Updated Successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    var user = status.data!!
                    val bundle = Bundle()
                    bundle.putString("data_object", Gson().toJson(user))
                    val action =
                        SpeedDialSetupFragmentDirections.actionSpeedDialSetupFragmentToAddContactsFromPhoneFragment(
                            bundle
                        )
                    findNavController().navigate(action)

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

                    requireActivity().showErrorDialog(errorMessage)
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


}