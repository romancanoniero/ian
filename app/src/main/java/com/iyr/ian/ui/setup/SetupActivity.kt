package com.iyr.ian.ui.setup


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iyr.fewtouchs.ui.views.setup.SetupActivityViewModel
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySetupBinding
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.setup.pin_setup.PinSetupActivity
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivity
import com.iyr.ian.ui.setup.speed_dial_setup.SpeedDialSetupActivity
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getCacheLocation
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.prepareMediaObject
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaFile.MEDIA_FILE_STATUS_NEW
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.utils.uploadFileToFirebaseStorage
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale


interface SetupActivityCallback {
    fun onSaveDone(user: User?)
    fun onError(exception: Exception)
}


class SetupActivity : AppCompatActivity(), SetupActivityCallback {


    private lateinit var viewModel: SetupActivityViewModel
    private var buttonClicked: Boolean = false
    private var isFirstSetup: Boolean = true

    // private lateinit var mPresenter: SetupPresenter

    //   private var originalObject: User? = null
    //   private lateinit var currentObject: User
    private lateinit var binding: ActivitySetupBinding

    private var toPickImagePermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val imagePickerIntent =
                    Lassi(this).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
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
                        .enableActualCircleCrop().setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                        .build()


                pickImageContract?.launch(imagePickerIntent)
            } else {
                permissionsForImages()
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
                            getCacheLocation(AppConstants.PROFILE_IMAGES_STORAGE_PATH + FirebaseAuth.getInstance().uid.toString())


                        var localPath = selectedMedia[0].path.toString()/*
                                                var fileName =
                                                    CHAT_FILES_STORAGE_PATH + this@MessagesInEventFragment.eventKey.toString() + "/" + localPath.getJustFileName()
                        */
                        var fileName = localPath.getJustFileName()


                        try {


                            var mediaFile =
                                prepareMediaObject(MediaTypesEnum.IMAGE, source, destinationPath)


                            if (mediaFile is MediaFile) {

                                //            viewModel.onImageChanged(mediaFile!! as MediaFile)
                                var imageBitmap = loadImageFromCache(
                                    mediaFile.file_name,
                                    "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${FirebaseAuth.getInstance().uid.toString()}"
                                )
                                if (imageBitmap?.byteCount ?: 0 > 0) {
                                    binding.userImage.setImageBitmap(imageBitmap)
                                } else {
                                    binding.userImage.setImageBitmap(imageBitmap)
                                }

                                viewModel.onImageChanged(
                                    FirebaseAuth.getInstance().uid.toString(), mediaFile
                                )

                                uploadFileToFirebaseStorage(
                                    Uri.parse("file:" + localPath),
                                    "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${FirebaseAuth.getInstance().uid.toString()}/${fileName}"
                                )


                            }

                            if (mediaFile is java.lang.Exception) {
                                Toast.makeText(
                                    this@SetupActivity,
                                    mediaFile.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (ex: Exception) {
                            Looper.prepare()
                            Toast.makeText(
                                this@SetupActivity, ex.localizedMessage, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }


    var loader = LoadingDialogFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = SetupActivityViewModel()
        intent.extras?.let { extras ->
            viewModel.setExtraData(extras)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPicker()

        //mPresenter = SetupPresenter(this, this)
        //   getIntentData()
        setupUI()
        setupObservers()
    }


    override fun onBackPressed() {
        if (viewModel.isFirstSetup.value == false) super.onBackPressed()
    }

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
                        applicationContext, "Setup Updated Successfully", Toast.LENGTH_SHORT
                    ).show()
                    onSaveDone(status.data)
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

        viewModel.diaplayName.observe(this) { text ->
            binding.displayName.setText(text)
        }

        viewModel.displayNameMinCharsRequired.observe(this) { complete ->
            if (!complete) {
                binding.displayNameMinChars.setTextColor(getColor(R.color.red))
                binding.displayNameMinChars.visibility = VISIBLE
            } else {
                binding.displayNameMinChars.visibility = GONE
                binding.displayNameMinChars.setTextColor(getColor(R.color.black))
            }
        }


        viewModel.phoneNumber.observe(this) { number ->
            binding.phoneNumber.setText(number)
        }



        viewModel.authToken.observe(this) { token ->
            SessionForProfile.getInstance(this).setProfileProperty("auth_token", token)
        }


        viewModel.image.observe(this) { resource ->

            when(resource)
            {
                is Resource.Loading -> {
                    binding.animation.visibility = VISIBLE
                }
                is Resource.Success -> {
                    binding.animation.visibility = GONE

                    var mediaFile = resource.data
                    if (mediaFile != null && !mediaFile.file_name.isNullOrEmpty()) {

                        lifecycleScope.launch(Dispatchers.Main) {

                            var subFolder =
                                AppConstants.PROFILE_IMAGES_STORAGE_PATH + FirebaseAuth.getInstance().uid.toString()

                            var finalPath = FirebaseStorage.getInstance().getReference(mediaFile.file_name)
                                .downloadUrlWithCache(
                                    AppClass.instance, subFolder
                                )

                            GlideApp.with(this@SetupActivity).asBitmap().load(finalPath)
                                .placeholder(getDrawable(R.drawable.progress_animation))
                                .error(getDrawable(R.drawable.ic_error)).into(binding.userImage)

                        }

                        binding.profileImageLayer.visibility = GONE

                    } else {
                        GlideApp.with(this).asBitmap().load(R.drawable.man_avatar_ronny_overhate)
                            .into(binding.userImage)
                        binding.profileImageLayer.visibility = VISIBLE
                    }


                }
                is Resource.Error -> {
                    binding.animation.visibility = GONE
                    showErrorDialog(resource.message.toString())
                }
            }


        }

        /*
                viewModel.saveButtonEnabled.observe(this) { enabled ->
                    binding.saveButton.isEnabled = enabled
                }
                */
    }

    private fun setupPicker() {
        configureCropOptions()
        registerActivityResultContracts()
    }

    override fun onResume() {
        super.onResume()/*
         Permiso.getInstance().setActivity(this)
         if (multiPickerWrapper!!.pickerUtilListener == null) {
             multiPickerWrapper!!.pickerUtilListener = multiPickerWrapperListener
         }

         */
        //   buttonClicked = false
        updateUI()

    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // After Activity recreate, you need to re-initialize these
        // two values to be able to re-initialize CameraImagePicker
        if (savedInstanceState != null) {/*
            if (savedInstanceState.containsKey("picker_path")) {
                multiPickerWrapper!!.pickerPath = savedInstanceState.getString("picker_path")
            }

             */
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    fun setupUI() {
        binding.userImage.setOnClickListener {
            //multiPickerWrapper?.getPermissionAndPickSingleImageAndCrop(options)


            toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))

        }
        binding.displayNameMinChars.text = String.format(
            getString(R.string.min_chars_required), AppConstants.USERNAME_MINIMUM_LENGTH
        )

        binding.phoneNumber.isEnabled = true
        val authUser = FirebaseAuth.getInstance().currentUser
        authUser?.let {
            for (userInfo in it.providerData) {
                when (userInfo.providerId) {
                    EmailAuthProvider.PROVIDER_ID -> {
                        // El usuario se autenticó con correo electrónico
                    }

                    PhoneAuthProvider.PROVIDER_ID -> {
                        binding.phoneNumber.isEnabled = false
                    }

                    GoogleAuthProvider.PROVIDER_ID -> {
                        // El usuario se autenticó con Google
                    }
                }
            }
        }

        binding.displayName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onDisplayNameChanged(s?.toString())
            }
        })

        binding.displayName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                viewModel.onDisplayNameChanged(binding.displayName.text.toString())
            }
        }

        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onPhoneNumberChanged(s?.toString())
            }
        })


        binding.firstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onFirstNameChanged(s?.toString())
            }
        })



        binding.nameMinChars.text = String.format(
            getString(R.string.min_chars_required), AppConstants.FIRSTNAME_MINIMUM_LENGTH
        )


        binding.lastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onLastNameChanged(s?.toString())
            }
        })

        binding.lastnameMinChars.text = String.format(
            getString(R.string.min_chars_required), AppConstants.LASTNAME_MINIMUM_LENGTH
        )



        binding.securityCodeInfo.setOnClickListener {
            showErrorDialog(
                getString(R.string.security_code_info_title),
                getString(R.string.security_code_info_description),
                getString(R.string.close)
            )
        }


        binding.saveButton.setOnClickListener {
            handleTouch()
            //---------------------
            val displayName: String = binding.displayName.text.toString()
            val telephoneNumber = binding.phoneNumber.text.toString()

            if (viewModel.getCurrentData().image == null) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_the_image),
                    null,
                    null
                )
                binding.userImage.requestFocus()
                return@setOnClickListener
            }


            if (displayName.length < AppConstants.USERNAME_MINIMUM_LENGTH) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_the_nickname),
                    null,
                    null
                )
                binding.displayName.requestFocus()
                return@setOnClickListener
            }

            if (telephoneNumber.isEmpty()) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_the_telephone_number),
                    null,
                    null
                )
                binding.phoneNumber.requestFocus()
                return@setOnClickListener

            }

            if (!Validators.isValidPhoneNumber(telephoneNumber)) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_correctly_the_telephone_number),
                    null,
                    null
                )
                binding.phoneNumber.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getCurrentData().first_name.isNullOrEmpty()) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_the_name),
                    null,
                    null
                )
                binding.firstName.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getCurrentData().last_name.isNullOrEmpty()) {
                showErrorDialog(
                    getString(R.string.not_filled),
                    getString(R.string.please_fullfill_the_last_name),
                    null,
                    null
                )
                binding.lastName.requestFocus()

                return@setOnClickListener
            }
            //--------------
            viewModel.onSaveButtonClicked()
            buttonClicked = true
        }


    }

    private fun updateUI() {

        var isOkDisplayName = true
        val displayName: String = binding.displayName.text.toString()
        val telephoneNumber = binding.phoneNumber.text.toString()

        isOkDisplayName = displayName.length >= AppConstants.USERNAME_MINIMUM_LENGTH
        if (!isOkDisplayName) {

            if (buttonClicked) {
                binding.displayName.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(this, R.drawable.ic_warning), null, null, null
                )

            }
        } else {
            binding.displayName.isEnabled = isFirstSetup
            binding.displayName.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null
            )
        }
        var isOkPhoneNumber = false
        if (telephoneNumber.isEmpty()) {
            binding.switchSpeedDialSection.visibility = GONE
        } else {
            isOkPhoneNumber = Validators.isValidPhoneNumber(telephoneNumber)
            if (!isOkPhoneNumber) {
                if (buttonClicked) {
                    binding.phoneNumber.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(this, R.drawable.ic_warning),
                        null,
                        null,
                        null
                    )
                }
                binding.switchSpeedDialSection.visibility = GONE
            } else {
                binding.phoneNumber.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, null, null
                )
                binding.switchSpeedDialSection.visibility = VISIBLE
            }
        }

        //      updateSaveButton()
    }

    /*
        private fun updateuserImage() {
            var storageReference: Any? = null
            storageReference = if (!currentObject.image.file_name.startsWith("http")) {
                if (currentObject.image.file_name.startsWith("file:")) {
                    currentObject.image.file_name
                } else {
                    FirebaseStorage.getInstance().getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                        .child(FirebaseAuth.getInstance().uid.toString())
                        .child(currentObject.image.file_name)
                }
            } else {
                currentObject.image.file_name
            }
            GlideApp.with(this).asBitmap().load(storageReference)
                .placeholder(getDrawable(R.drawable.progress_animation))
                .error(getDrawable(R.drawable.ic_error)).into(binding.userImage)
        }
    *//*
    private fun updateSaveButton() {
        val displayName: String = binding.displayName.text.toString()
        val telephoneNumber = binding.phoneNumber.text.toString()
        val isOkToProceed =
            currentObject.image != null &&
                    displayName.length >= AppConstants.USERNAME_MINIMUM_LENGTH &&
                    (telephoneNumber.isEmpty() || Validators.isValidPhoneNumber(telephoneNumber)) && !buttonClicked

        binding.saveButton.isEnabled = isOkToProceed
    }
*/
    private fun configureCropOptions() {/*
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
                    mediaFile.status = MEDIA_FILE_STATUS_NEW
                    mediaFile.time = Date().time
                }
            }

    }

    /*
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            /*
            if (multiPickerWrapper!!.onActivityResult(requestCode, resultCode, data)) {
                return
            }
            */
            super.onActivityResult(requestCode, resultCode, data)
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    //imgProfile.setImageURI(fileUri)

                    //You can get File object from intent
                    val file = File(fileUri.path)
                    //You can also get File Path from intent
                    var filePath: String = fileUri.path.toString()
                    val fileExtension = filePath.getFileExtension(this)
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                            Locale.getDefault()
                        ) == "jpeg"
                    ) {
                        filePath = "file:$filePath"
                    }
    /*
                    val mediaFile = MediaFile(MediaTypesEnum.IMAGE)
                    mediaFile.file_name = filePath
                    GlideApp.with(this).asBitmap().load(filePath)
                        .placeholder(getDrawable(R.drawable.progress_animation))
                        .error(getDrawable(R.drawable.ic_error)).into(binding.userImage)
    */

                    val image = MediaFile(MediaTypesEnum.IMAGE)
                    image.file_name = filePath
                    image.status = MEDIA_FILE_STATUS_NEW

    //                currentObject.image = image
                    viewModel.onImageChanged(image)
                  //  updateUI()
                }/*
                ImagePicker.RESULT_ERROR -> {
                    //         Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    when (ImagePicker.getError(data)) {
                        "Camera and Storage permissions are needed to take a picture. Please allow both the requested permissions from Settings > Permissions." -> {
                            showErrorDialog(getString(R.string.camera_and_storage_permissions_required))
                        }
                    }

                }*/
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    */

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);

        /*
         var grantResults = grantResults
         if (requestCode == 1) {
             val _grantResults = IntArray(2)
             _grantResults[0] = grantResults[0]
             _grantResults[1] = grantResults[1]
             grantResults = _grantResults
             if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                 pickImageAndCrop(CROP_SQUARED, this)
             }
         }

         */
    }


    override fun onSaveDone(user: User?) {

        SessionForProfile.getInstance(applicationContext).storeUserProfile(user)

        // Remuevo el codificado de la imagen
        user?.image?.bytesB64 = null
        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        intent =
            if (user?.sos_invocation_count == 0 || user?.sos_invocation_method.isNullOrEmpty()) {
                Intent(this@SetupActivity, PressOrTapSetupActivity::class.java)
            } else if (user?.security_code.isNullOrBlank()) {
                Intent(this@SetupActivity, PinSetupActivity::class.java)
            } else if (user?.allow_speed_dial == null) {
                Intent(this@SetupActivity, SpeedDialSetupActivity::class.java)
            } else if (!areLocationPermissionsGranted()) {
                Intent(this@SetupActivity, LocationRequiredActivity::class.java)
            } else {
                Intent(this@SetupActivity, AddContactsFromPhoneActivity::class.java)
            }

        intent?.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

    }


    override fun onError(exception: Exception) {
        hideLoader()
        showErrorDialog(exception.localizedMessage)

    }

    //------------------ Pickers--------------------------------------------
    /*
        private val options by lazy { UCrop.Options() }
        private var multiPickerWrapper: MultiPickerWrapper? = null
        private val cacheLocation = MultiPickerWrapper._CacheLocation.EXTERNAL_CACHE_DIR
        private var multiPickerWrapperListener: MultiPickerWrapper.PickerUtilListener =
            object : MultiPickerWrapper.PickerUtilListener {
                override fun onPermissionDenied() {
                    onError(getString(R.string.no_permission))
                }

                override fun onImagesChosen(list: List<ChosenImage>) {
                    var intent = Intent("Result")/*
                            intent.data = Uri.parse(list.get(0).originalPath)
                            var requestCode = Picker.PICK_IMAGE_DEVICE
                            var data = intent
                            var resultCode = Activity.RESULT_OK
                */
                    val fileUri = Uri.parse(list[0].originalPath)
                    //imgProfile.setImageURI(fileUri)

                    //You can get File object from intent
                    val file: File = File(fileUri.path)

                    //You can also get File Path from intent
                    var filePath: String = fileUri.path.toString()
                    val fileExtension = FileUtils.getFileExtension(filePath)
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                            Locale.getDefault()
                        ) == "jpeg" || fileExtension?.lowercase(Locale.getDefault()) == "png" || fileExtension?.lowercase(
                            Locale.getDefault()
                        ) == "bmp"
                    ) {
                        filePath = "file:$filePath"
                    } else {
                        showErrorDialog("Formato no soportado")
                    }
                    val mediaFile = MediaFile(MediaTypesEnum.IMAGE)
                    mediaFile.file_name = filePath

                    GlideApp.with(this@SetupActivity).asBitmap().load(filePath)
                        .placeholder(getDrawable(R.drawable.progress_animation))
                        .error(getDrawable(R.drawable.ic_error)).into(binding.userImage)

                    val image = MediaFile(MediaTypesEnum.IMAGE)
                    image.file_name = filePath
                    image.status = MEDIA_FILE_STATUS_NEW

                    viewModel.onImageChanged(image)
                    updateUI()
                }

                override fun onVideosChosen(list: List<ChosenVideo>) {
                }

                override fun onAudiosChosen(list: List<ChosenAudio>) {
                    // do something here
                    var oo = 23
                }

                override fun onFilesChosen(list: List<ChosenFile>) {
                    // do something here
                    var oo = 23
                }

                override fun onError(s: String) {
                    showErrorDialog(s)
                }
            }
    */
    private lateinit var imagePickerStartForResult: ActivityResultLauncher<Intent>

    /*
    override fun onError(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onImagesChosen(p0: MutableList<ChosenImage>?) {
      var pelotudo = 22
    }
    */
}