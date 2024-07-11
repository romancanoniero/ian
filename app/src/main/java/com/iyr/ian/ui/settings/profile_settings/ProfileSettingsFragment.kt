package com.iyr.ian.ui.settings.profile_settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.FragmentProfileSettingsBinding
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.IProfileSettingsFragment
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.SettingsFragmentViewModel
import com.iyr.ian.ui.setup.SetupActivity
import com.iyr.ian.utils.FirebaseExtensions.getCurrentAuthenticationMethod
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.viewmodels.MainActivityViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileSettingsFragment(
    val mainActivityViewModel: MainActivityViewModel,
    val settingsFragmentViewModel: SettingsFragmentViewModel,
    var _interface: ISettingsFragment
) : Fragment(),
    IProfileSettingsFragment {
    private lateinit var binding: FragmentProfileSettingsBinding
    //private lateinit var presenter: ProfileSettingsPresenter

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var viewModel = ProfileSettingsFragmentViewModel()

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
        binding = FragmentProfileSettingsBinding.inflate(layoutInflater, container, false)
        setupControlData()
        setupUI()
        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters


        @JvmStatic
        fun newInstance(
            context: Context,
            mainActivityViewModel: MainActivityViewModel,
            settingsFragmentViewModel: SettingsFragmentViewModel,
            _interface: ISettingsFragment
        ) =
            ProfileSettingsFragment(mainActivityViewModel, settingsFragmentViewModel, _interface)
    }


    private fun setupUI() {
        binding.profileImage.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                "data_object",
                Gson().toJson(SessionForProfile.getInstance(requireContext()).getUserProfile())
            )
            bundle.putBoolean(
                "first_setup",
                false
            )
            val intent = Intent(requireContext(), SetupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        /*
                val choosenProvider: String =
                    FirebaseAuth.getInstance().getAccessToken(false).getResult().getSignInProvider()
                        .toString()
        */
        //    val usersAuthMethods = FirebaseAuth.getInstance().currentUser!!.providerData
        /*
                var includesEmailMethod = false
                usersAuthMethods.forEach { userInfo ->
                    when (userInfo.providerId) {
                        EmailAuthProvider.PROVIDER_ID -> {
                            includesEmailMethod = true
                            binding.changeEmailButton.visibility = GONE
                        }
                        PhoneAuthProvider.PROVIDER_ID -> {
                            binding.changePhoneButton.visibility = GONE
                        }
                    }
                }
        */

        binding.changeEmailButton.visibility = GONE
        binding.changePasswordButton.visibility = GONE
        binding.changePhoneButton.visibility = GONE
        when (requireContext().getCurrentAuthenticationMethod()) {
            "email" -> {
                binding.changeEmailButton.visibility = VISIBLE
                binding.changePasswordButton.visibility = VISIBLE
            }

            "phone" -> {
                binding.changePhoneButton.visibility = VISIBLE
            }
        }

        binding.displayName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //    TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //  TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onDisplayNameChanged(s.toString())
                //   updateUI()
            }
        })

        binding.saveButton.setOnClickListener {
            // presenter.saveDisplayName(binding.displayName.text.toString())
            viewModel.onSaveButtonClicked()
        }

    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.profile_settings))
        }
        setupObservers()
    //    updateUI()
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }


    private fun setupObservers() {

        viewModel.savingStatus.observe(this) { status ->
            when(status) {
                is Resource.Error -> {
                    mainActivityViewModel.showError(status.message.toString())
                }
                is Resource.Loading ->
                {
                    mainActivityViewModel.showLoader()
                }
                is Resource.Success -> {

                    mainActivityViewModel.hideLoader()
                    mainActivityViewModel.goBack()
                }
            }
  //       mainActivityViewModel.showLoader()
        }


        viewModel.displayName.observe(this) { value ->
            binding.displayName.setText(value)
            viewModel.displayName.removeObservers(this@ProfileSettingsFragment)
        }

        mainActivityViewModel.user.observe(this) { user ->
            viewModel.setUser(user)
            if (user.image.file_name != null) {
                val storageReference = FirebaseStorage.getInstance()
                    .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                    .child(FirebaseAuth.getInstance().uid.toString())
                    .child(user.image.file_name.toString())
                //  .getReference(me.image?.file_name.toString())
                GlideApp.with(requireContext())
                    .asBitmap()
                    .load(storageReference)
                    .placeholder(requireContext().getDrawable(R.drawable.progress_animation))
                    .error(requireContext().getDrawable(R.drawable.ic_error))
                    .into(binding.profileImage)

            } else {
                binding.profileImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
            }
            viewModel.setProfileImagePath(user.image.file_name)
        }
        viewModel.buttonStatus.observe(this) { status ->
            binding.saveButton.isEnabled = status
        }
    }

    private fun removeObservers() {
        viewModel.displayName.removeObservers(this)
        mainActivityViewModel.user.removeObservers(this)
        viewModel.buttonStatus.removeObservers(this)
    }

    /*
        fun updateUI() {

            val me = SessionForProfile.getInstance(requireContext()).getUserProfile()
         binding.saveButton.isEnabled = binding.displayName.text.toString()!= me.display_name &&
                    binding.displayName.text.toString().length >= USERNAME_MINIMUM_LENGTH

        }
    */
    private fun setupControlData() {
        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()

        if (me.image.file_name != null) {
            val storageReference = FirebaseStorage.getInstance()
                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                .child(FirebaseAuth.getInstance().uid.toString())
                .child(me.image.file_name.toString())
            //  .getReference(me.image?.file_name.toString())
            GlideApp.with(requireContext())
                .asBitmap()
                .load(storageReference)
                .placeholder(requireContext().getDrawable(R.drawable.progress_animation))
                .error(requireContext().getDrawable(R.drawable.ic_error))
                .into(binding.profileImage)

        } else {
            binding.profileImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
        }

        binding.displayName.setText(me.display_name)

    }

    override fun onSaveDone(user: User) {
        //     TODO("Not yet implemented")
        (AppClass.instance.getCurrentActivity() as MainActivity).onBackPressed()
    }

    override fun onError(exception: Exception) {
        //       TODO("Not yet implemented")
    }
}