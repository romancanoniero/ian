package com.iyr.ian.ui.settings.landing_fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.databinding.FragmentSettingsLandingBinding
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.services.falling_detection.FallDetectionServiceMethod
import com.iyr.ian.services.location.isServiceRunning
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.SettingsFragmentViewModel
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getCacheLocation
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.prepareMediaObject
import com.iyr.ian.utils.saveImageToCache
import com.iyr.ian.utils.showConfirmationDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.utils.uploadFileToFirebaseStorage
import com.iyr.ian.utils.versionPrefix
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsLandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsLandingFragment(
    private val mainActivityViewModel: MainActivityViewModel,
    val settingsFragmentViewModel: SettingsFragmentViewModel,
    private val _interface: ISettingsFragment
) : Fragment() {
    private lateinit var binding: FragmentSettingsLandingBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var toPickImagePermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val imagePickerIntent =
                    Lassi(requireContext()).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(1).setGridSize(3)
                        .setMediaType(MediaType.IMAGE) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                        .setCompressionRatio(50) // compress image for single item selection (can be 0 to 100)
                        .setSupportedFileTypes(
                            "jpg", "jpeg", "png", "webp", "gif"
                        ).setMinFileSize(100) // Restrict by minimum file size
                        .setMaxFileSize(640) //  Restrict by maximum file size
                        .setStatusBarColor(R.color.white).setToolbarResourceColor(R.color.white)
                        .setProgressBarColor(R.color.colorAccent)
                        .setPlaceHolder(R.drawable.ic_image_placeholder)
                        .setErrorDrawable(R.drawable.ic_image_placeholder)
                        .setSelectionDrawable(R.drawable.ic_checked_media)
                        .setAlertDialogNegativeButtonColor(R.color.white)
                        .setAlertDialogPositiveButtonColor(R.color.darkGray)
                        .setGalleryBackgroundColor(R.color.gray)//Customize background color of gallery (default color is white)
                        .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableActualCircleCrop()
                        .setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                        .build()


                pickImageContract?.launch(imagePickerIntent)
            } else {
                requireActivity().permissionsForImages()
            }
        }

    private var pickImageContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as java.util.ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {

                    lifecycleScope.launch(Dispatchers.IO) {

                        var source = selectedMedia[0].path.toString()
                        var destinationPath =
                            requireContext().getCacheLocation(AppConstants.PROFILE_IMAGES_STORAGE_PATH + FirebaseAuth.getInstance().uid.toString())

                        var localPath = selectedMedia[0].path.toString()

                        var fileName = localPath.getJustFileName()

                        try {


                            var mediaFile =
                                requireContext().prepareMediaObject(
                                    MediaTypesEnum.IMAGE,
                                    source,
                                    destinationPath
                                )


                            if (mediaFile is MediaFile) {

                                var imageBitmap = requireContext().loadImageFromCache(
                                    mediaFile.file_name,
                                    "${PROFILE_IMAGES_STORAGE_PATH}/${FirebaseAuth.getInstance().uid.toString()}"
                                )
                                if (imageBitmap?.byteCount ?: 0 > 0) {
                                    binding.userImage.setImageBitmap(imageBitmap)
                                } else {
                                    binding.userImage.setImageBitmap(imageBitmap)
                                }

                                viewModel.onImageChanged(
                                    FirebaseAuth.getInstance().uid.toString(),
                                    mediaFile
                                )

                                requireContext().uploadFileToFirebaseStorage(
                                    Uri.parse("file:" + localPath),
                                    "${PROFILE_IMAGES_STORAGE_PATH}/${FirebaseAuth.getInstance().uid.toString()}/${fileName}"
                                )



                            }

                            if (mediaFile is java.lang.Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    mediaFile.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (ex: Exception) {
                            Looper.prepare()
                            Toast.makeText(
                                requireContext(), ex.localizedMessage, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }


    private lateinit var viewModel: SettingsLandingFragmentViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)

        viewModel = SettingsLandingFragmentViewModel()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        setupPicker()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsLandingBinding.inflate(layoutInflater, container, false)
        setupUI()
        setupObservers()
        return binding.root
    }

    var loader = LoadingDialogFragment()
    private fun setupObservers() {

        viewModel.image.observe(viewLifecycleOwner) { resource ->

            when (resource)
            {
                is Resource.Loading -> {
                    binding.animation.visibility = VISIBLE
                }
                is Resource.Success -> {
                    var mediaFile = resource.data


                    if (mediaFile != null && !mediaFile.file_name.isNullOrEmpty()) {
                        var imageBitmap = requireContext().loadImageFromCache(
                            mediaFile.file_name.toString(),
                            AppConstants.PROFILE_IMAGES_STORAGE_PATH
                        )

                        if (imageBitmap != null) {
                            binding.userImage.setImageBitmap(imageBitmap)
                            return@observe
                        } else {
                            lifecycleScope.launch(Dispatchers.IO) {
                                var subFolder =
                                    AppConstants.PROFILE_IMAGES_STORAGE_PATH + FirebaseAuth.getInstance().uid.toString()

                                var finalPath = FirebaseStorage.getInstance()
                                    .getReference(mediaFile.file_name).downloadUrlWithCache(
                                        AppClass.instance, subFolder
                                    )

                                var bitmap = GlideApp.with(requireContext())
                                    .asBitmap()
                                    .load(finalPath)
                                    .submit()
                                    .get()

                                if (bitmap != null) {
                                    bitmap.saveImageToCache(
                                        requireContext(),
                                        mediaFile.file_name.toString(),
                                        AppConstants.PROFILE_IMAGES_STORAGE_PATH
                                    )
                                    binding.userImage.setImageBitmap(bitmap)
                                } else {
                                    binding.userImage.setImageDrawable(
                                        requireContext().getDrawable(
                                            R.drawable.ic_error
                                        )
                                    )
                                }

                            }
                        }
                    }


                    binding.animation.visibility = GONE


                }
                is Resource.Error -> {
                    binding.animation.visibility = GONE

                }
            }

        }

        viewModel.deletingAccountStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Loading -> {
                    loader.setLoadingMessage(getString(R.string.removing_account))
                    loader.show(parentFragmentManager, "loader_frames")
                }

                is Resource.Error -> {
                    if (loader.isVisible)
                        loader.dismiss()

                    requireActivity().showErrorDialog(status.message.toString())
                }

                is Resource.Success -> {
                    if (loader.isVisible)
                        loader.dismiss()

                    requireActivity().showSnackBar(binding.root, "Implementar el logout")

//            requireActivity().logout()
                }

                null -> {

                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.action_settings))
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param context Parameter 1.
         * @param _interface Parameter 2.
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
            SettingsLandingFragment(mainActivityViewModel, settingsFragmentViewModel, _interface)
    }


    private fun setupUI() {

        binding.userImage.setOnClickListener {
            toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.CAMERA))
        }

        binding.switchVibrations.isChecked = SessionForProfile.getInstance(requireContext())
            .getProfileProperty("vibrations_on", false) as Boolean

        binding.profileSettingsButton.setOnClickListener {
            //_interface.goToFragment(SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal)
            settingsFragmentViewModel.onProfileSettingsClick()

        }

        binding.sosSettingsButton.setOnClickListener {
            //_interface.goToFragment(SettingsFragmentsEnum.SOS_SETTINGS.ordinal)
            settingsFragmentViewModel.onSOSSettingsClick()
        }


        if (requireContext().versionPrefix() >= 2) {
            binding.myPlanLay.visibility = VISIBLE
            binding.myPlanLay.setOnClickListener {
//                _interface.goToFragment(SettingsFragmentsEnum.PLAN_SETTINGS.ordinal)
                mainActivityViewModel.onSubscriptionSettingsClick()
            }
        } else {
            binding.myPlanLay.visibility = GONE
        }

        if (requireContext().versionPrefix() >= 2) {
            binding.emergencyContactsButton.visibility = VISIBLE
            binding.emergencyContactsButton.setOnClickListener {
//                _interface.goToFragment(SettingsFragmentsEnum.NOTIFICATION_GROUPS.ordinal)
                mainActivityViewModel.onNotificationGroupsSettingsClick()


            }
        } else {
            binding.emergencyContactsButton.visibility = GONE
        }


        binding.pushButtonSetupLay.setOnClickListener {
//            _interface.goToFragment(SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS.ordinal)
//            mainActivityViewModel.onPushButtonSettingsClick()
            settingsFragmentViewModel.goToFragment(SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS.ordinal)
        }

        binding.logoutButton.setOnClickListener {
            requireActivity().handleTouch()
            requireActivity().showConfirmationDialog(
                getString(R.string.logout),
                getString(R.string.are_you_sure_you_want_to_logout),
                getString(R.string.yes),
                getString(R.string.no),
                object : IAcceptDenyDialog {
                    override fun onAccept() {
                        SessionForProfile.getInstance(requireContext()).logout()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)

                    }

                    override fun onCancel() {
                        requireActivity().hideLoader()
                        super.onCancel()
                    }
                }
            )

        }

        binding.switchUpdateMyLocation.setOnCheckedChangeListener { compoundButton, enabled ->
            AppClass.instance.setRTLocationUpdate(enabled)
        }


        if (requireContext().versionPrefix() >= 3) {
            binding.fallingDetectionLay.visibility = VISIBLE
            binding.switchFallingSensor.isChecked = SessionForProfile.getInstance(requireContext())
                .getProfileProperty("falling_sensor", false) as Boolean
            binding.switchFallingSensor.setOnCheckedChangeListener { compoundButton, enabled ->
                if (enabled)
                    AppClass.instance.enableFallingSensor()
                else
                    AppClass.instance.disableFallingSensor()
            }
        } else {
            binding.switchFallingSensor.visibility = GONE
        }
        binding.switchVibrations.setOnCheckedChangeListener { compoundButton, enabled ->
            SessionForProfile.getInstance(requireContext())
                .setProfileProperty("vibrations_on", enabled)
            if (enabled) {
                if (!requireActivity().isServiceRunning(FallDetectionServiceMethod::class.java)) {
                    requireActivity().startService(
                        Intent(
                            AppClass.instance,
                            FallDetectionServiceMethod::class.java
                        )
                    )
                }
            } else {
                if (requireActivity().isServiceRunning(FallDetectionServiceMethod::class.java)) {
                    requireActivity().stopService(
                        Intent(
                            AppClass.instance,
                            FallDetectionServiceMethod::class.java
                        )
                    )
                }

            }
        }

        binding.removeAccountButton.setOnClickListener {
            requireContext().handleTouch()
            val callback = object : IAcceptDenyDialog {
                override fun onAccept() {

                    viewModel.onDeleteAccountClick()
                    /*
                                        aca conectar el viewmodel
                                        requireActivity().showLoader(getString(R.string.removing_account))
                                        UsersWSClient.instance.deleteUserAccount(object : OnCompleteCallback {
                                            override fun onComplete(success: Boolean, result: Any?) {
                                                requireActivity().hideLoader()
                                                requireActivity().logout()
                                            }

                                            override fun onError(exception: Exception) {
                                                requireActivity().hideLoader()
                                                requireActivity().showErrorDialog(exception.message.toString())
                                            }
                                        })
                                   */

                }

                override fun onCancel() {
                    requireActivity().hideLoader()
                    super.onCancel()
                }
            }

            requireActivity().showConfirmationDialog(
                getString(R.string.eliminate_account),
                getString(R.string.eliminate_account_message),
                getString(R.string.yes),
                getString(R.string.no),
                callback
            )

        }




        binding.resetEventsButton.setOnClickListener {

            requireActivity().showSnackBar(binding.root, "Implementar binding.resetEventsButton")
            /*
                val eventsRef =
                    FirebaseDatabase.getInstance().getReference(AppConstants.TABLE_EVENTS_LOCATIONS)
                val eventsFollowedRef = FirebaseDatabase.getInstance()
                    .getReference(AppConstants.TABLE_USERS_EVENTS_FOLLOWED)
                val evetsNotificationsRef =
                    FirebaseDatabase.getInstance().getReference(AppConstants.TABLE_EVENTS_NOTIFICATIONS)
                eventsFollowedRef.removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        eventsRef.removeValue().addOnCompleteListener {
                            if (it.isSuccessful) {
                                evetsNotificationsRef.removeValue().addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        requireActivity().showAnimatedDialog(
                                            "Done",
                                            "The events was reset"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

          */
        }

        updateUI()
    }


    fun updateUI() {
        val me = SessionForProfile.getInstance(requireContext()).getUserProfile()

        if (me.image.file_name != null) {

            var imageBitmap = requireContext().loadImageFromCache(
                me.image.file_name.toString(),
                AppConstants.PROFILE_IMAGES_STORAGE_PATH
            )

            if (imageBitmap != null) {
                binding.userImage.setImageBitmap(imageBitmap)
                return
            } else {

                try {
                    val storageReference = FirebaseStorage.getInstance()
                        .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                        .child(FirebaseAuth.getInstance().uid.toString())
                        .child(me.image.file_name.toString())
                    //  .getReference(me.image?.file_name.toString())
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            imageBitmap = GlideApp.with(requireContext())
                                .asBitmap()
                                .load(storageReference)
                                .placeholder(requireContext().getDrawable(R.drawable.progress_animation))
                                .error(requireContext().getDrawable(R.drawable.ic_error))
                                .submit()
                                .get()

                            if (imageBitmap != null) {
                                imageBitmap!!.saveImageToCache(
                                    requireContext(),
                                    me.image.file_name.toString(),
                                    AppConstants.PROFILE_IMAGES_STORAGE_PATH
                                )
                                withContext(Dispatchers.Main) {
                                    binding.userImage.setImageBitmap(imageBitmap)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    binding.userImage.setImageDrawable(
                                        requireContext().getDrawable(
                                            R.drawable.ic_error
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                binding.userImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
                            }
                        }

                    }
                } catch (e: Exception) {
                    binding.userImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
                } catch (e: StorageException) {
                    binding.userImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
                }
//                    .into(binding.userImage)


            }

        } else {
            binding.userImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_error))
        }

        //  binding.userName.setText(me.display_name)

        var isRTLocationEnabled = true
        if (SessionForProfile.getInstance(requireContext())
                .getProfileProperty("RTLocationEnabled") != null
        ) {
            isRTLocationEnabled = SessionForProfile.getInstance(requireContext())
                .getProfileProperty("RTLocationEnabled") as Boolean
        }

        binding.switchUpdateMyLocation.isChecked = isRTLocationEnabled

        if (AppClass.instance.isFreeUser())
            binding.fallingDetectionLay.visibility = GONE
        else
            binding.fallingDetectionLay.visibility = VISIBLE
    }

    private fun setupPicker() {
        configureCropOptions()
        registerActivityResultContracts()
    }

    private fun configureCropOptions() {
        /*
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.gray))
        options.setCropFrameStrokeWidth(4.px)
        options.setCropGridColor(ContextCompat.getColor(this, R.color.gray))
        options.setCropGridStrokeWidth(2.px)
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.black))
        options.withAspectRatio(1F, 1F)
        options.setToolbarTitle(getString(R.string.crop))
         */
    }


    private lateinit var imagePickerStartForResult: ActivityResultLauncher<Intent>
    private fun registerActivityResultContracts() {

        imagePickerStartForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val filePath = Uri.parse(intent?.data?.encodedPath).toString()
                    //             uploadMediaToPanicEvent(MediaTypesEnum.IMAGE, filePath)

                    val mediaFile = MediaFile()
                    mediaFile.media_type = MediaTypesEnum.IMAGE
                    mediaFile.file_name = filePath
                    mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                    mediaFile.time = Date().time
                }
            }

    }

}