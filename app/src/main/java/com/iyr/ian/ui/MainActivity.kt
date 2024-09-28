package com.iyr.ian.ui


import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.SEND_SMS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iyr.fewtouchs.ui.views.home.fragments.friends.adapters.ContactListsAdapter
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_SHOW_FOOTER_TOOLBAR
import com.iyr.ian.AppConstants.Companion.BROADCAST_BLOCKED_LAYOUT_DISMISS
import com.iyr.ian.AppConstants.Companion.BROADCAST_BLOCKED_LAYOUT_REQUIRED
import com.iyr.ian.AppConstants.Companion.BROADCAST_DID_YOU_ARRIVE_REQUEST
import com.iyr.ian.AppConstants.Companion.BROADCAST_EVENT_CLOSE_TO_EXPIRE
import com.iyr.ian.AppConstants.Companion.BROADCAST_EVENT_FOLLOWED_ADDED
import com.iyr.ian.AppConstants.Companion.BROADCAST_EVENT_FOLLOWED_UPDATED
import com.iyr.ian.AppConstants.Companion.BROADCAST_PULSE_REQUIRED
import com.iyr.ian.AppConstants.Companion.DYNAMIC_LINK_ACTION_FRIENDSHIP
import com.iyr.ian.AppConstants.Companion.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL
import com.iyr.ian.AppConstants.Companion.NOTIFICATION_TYPE_NEW_MESSAGE
import com.iyr.ian.AppConstants.Companion.NOTIFICATION_TYPE_ON_NEW_MESSAGE
import com.iyr.ian.AppConstants.Companion.PERMISSIONS_REQUEST_READ_CONTACTS
import com.iyr.ian.AppConstants.ServiceCode.BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED
import com.iyr.ian.BuildConfig
import com.iyr.ian.Constants
import com.iyr.ian.Constants.Companion.MY_PERMISSION_REQUEST_SEND_SMS
import com.iyr.ian.Constants.Companion.REQUEST_CODE_RECOVER_PLAY_SERVICES
import com.iyr.ian.R
import com.iyr.ian.apis.NotificationsApi
import com.iyr.ian.app.AppClass
import com.iyr.ian.app_lifecycle_listener.AppLifecycleListener
import com.iyr.ian.callbacks.MainActivityCommonDataInterface
import com.iyr.ian.callbacks.MediaPickersInterface
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.models.EventNotificationType
import com.iyr.ian.dao.models.EventVisibilityTypes
import com.iyr.ian.dao.models.GeoLocation
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.EventsFollowedRepository
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.databinding.ActivityMainBinding
import com.iyr.ian.enums.DialogsEnum
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.enums.RecordingStatusEnum
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITag.LT
import com.iyr.ian.itag.ITag.ble
import com.iyr.ian.itag.ITagImageView
import com.iyr.ian.itag.ITagInterface
import com.iyr.ian.itag.ITagModesEnum
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.itag.StoreOpType
import com.iyr.ian.itag.TagColor
import com.iyr.ian.nonui.NonUI
import com.iyr.ian.physical_button.BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.services.bluetooth.Bluetooth
import com.iyr.ian.services.bluetooth.interfaces.Callback
import com.iyr.ian.services.falling_detection.Constants.BROADCAST_FALLING_EVENT
import com.iyr.ian.services.falling_detection.FallDetectionServiceMethod
import com.iyr.ian.services.location.LocationUpdatesService
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.services.location.ServiceLocation.ServiceLocationBinder
import com.iyr.ian.services.location.isServiceRunning
import com.iyr.ian.services.messaging.PushNotificationService
import com.iyr.ian.services.receivers.AppStatusReceiver
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.base.BlockedLayoutDialog
import com.iyr.ian.ui.base.EventCloseToExpireDialog
import com.iyr.ian.ui.base.EventCloseToExpireDialogCallback
import com.iyr.ian.ui.base.EventExtendedTimeDialog
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.PulseValidationCallback
import com.iyr.ian.ui.base.PulseValidationStatus
import com.iyr.ian.ui.base.PulseValidatorDialog
import com.iyr.ian.ui.base.ValidationPulsePayload
import com.iyr.ian.ui.callback.MainActivityCallback
import com.iyr.ian.ui.chat.MessagesInEventFragment
import com.iyr.ian.ui.dialogs.ArrivingDialog
import com.iyr.ian.ui.dialogs.LeavingTrackingConfirmationDialogCallback
import com.iyr.ian.ui.dialogs.NewEventNotificationDialog
import com.iyr.ian.ui.dialogs.OnYesNoButtonsListener
import com.iyr.ian.ui.dialogs.VideoPlayerDialog
import com.iyr.ian.ui.dialogs.iTagDialog
import com.iyr.ian.ui.events.EventsFragment
import com.iyr.ian.ui.events.fragments.adapters.MediaHandlingCallback
import com.iyr.ian.ui.friends.FriendsFragment
import com.iyr.ian.ui.interfaces.EventsPublishingCallback
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.main.HomeFragment
import com.iyr.ian.ui.main.HomeFragmentArgs
import com.iyr.ian.ui.main.HomeFragmentDirections
import com.iyr.ian.ui.map.MapSituationFragment
import com.iyr.ian.ui.map.adapters.EventsTrackingCallback
import com.iyr.ian.ui.map.models.CameraMode
import com.iyr.ian.ui.notifications.NotificationsFragment
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.ui.singletons.EventCloseToExpire
import com.iyr.ian.ui.singletons.PulseValidation
import com.iyr.ian.ui.toolbar.AppToolbar
import com.iyr.ian.utils.ActivityResultsTarget
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.KeyboardUtil
import com.iyr.ian.utils.LocationRequirementsCallback
import com.iyr.ian.utils.NotificationsUtils
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.ToolbarUtils
import com.iyr.ian.utils.UIUtils.getDensityName
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators.requestStatusConfirmationSingleton
import com.iyr.ian.utils.Validators.showIsCloseToExpireDialogSingleton
import com.iyr.ian.utils.animations.SpringAnimator
import com.iyr.ian.utils.animations.correctAnimation
import com.iyr.ian.utils.appsInternalStorageFolder
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_DEVICE_DISCOVERED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_SCAN_RESULT_UPDATED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.MY_PERMISSIONS_REQUEST_BLUETOOTH_PERMISSIONS
import com.iyr.ian.utils.bluetooth.errors.ErrorsObservable
import com.iyr.ian.utils.bluetooth.hasPermissions
import com.iyr.ian.utils.bluetooth.requirePermissionsBLE
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.clearPendingNotifications
import com.iyr.ian.utils.connectivity.NetworkStatusHelper
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.countdownanimator.CountDownAnimation
import com.iyr.ian.utils.efx.CircularRippleView
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.getSharePreferencesMessages
import com.iyr.ian.utils.hideStatusBar
import com.iyr.ian.utils.isGPSEnabled
import com.iyr.ian.utils.isGooglePlayInstalled
import com.iyr.ian.utils.keyboardheight.KeyboardHeightObserver
import com.iyr.ian.utils.keyboardheight.KeyboardHeightProvider
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.multimedia.getDuration
import com.iyr.ian.utils.multimedia.playVideo
import com.iyr.ian.utils.multimedia.showImage
import com.iyr.ian.utils.multimedia.showTextDialog
import com.iyr.ian.utils.permissions.PermissionsEnablingDialog
import com.iyr.ian.utils.permissions.PermissionsRationaleDialog
import com.iyr.ian.utils.permissionsForImages
import com.iyr.ian.utils.permissionsForSMSSend
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.permissionsReadWrite
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.px
import com.iyr.ian.utils.requestLocationRequirements
import com.iyr.ian.utils.resultTarget
import com.iyr.ian.utils.showAnimatedDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.startActivity
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.UUID


enum class ScreenModeEnum {
    NORMAL, FULLSCREEN
}

enum class KeyboardStatusEnum {
    OPEN, CLOSED
}

interface EventsNotificationCallback {
    //   fun onAgreeToAssist(eventKey: String)
    fun onDenyToAssist(event: Event)
    fun onDenyToAssist(eventKey: String)
}

interface SpeedDialActionsCallback {
    fun closeEvent(eventKey: String, securityCode: String) {}
    fun closeEvent(eventKey: String, securityCode: String, callback: OnCompleteCallback) {}
    fun onLeaveEventRequest(eventKey: String) {}
    fun onEventCloseDone(eventFollowed: EventFollowed) {}
    fun onLeaveEventRequestDone(eventKey: String) {}
    fun onGoingToHelpRequest(eventKey: String) {}
    fun onGoingToHelpRequestDone() {}
    fun onNotGoingToHelpRequest(eventKey: String) {}
}


class MainActivity : AppCompatActivity(), MainActivityCallback,
    EventsPublishingCallback, EventsTrackingCallback,
    SpeedDialActionsCallback, MediaHandlingCallback, CountDownAnimation.CountDownListener,
    PulseValidationCallback, LeavingTrackingConfirmationDialogCallback,
    //  HomeFragmentInteractionCallback,
    EventsNotificationCallback, Callback, EventCloseToExpireDialogCallback,
    MainActivityInterface, MediaPickersInterface, MainActivityCommonDataInterface,
    KeyboardHeightObserver {

    //, ViewersActionsCallback

    private var nonUI: NonUI? = null


    private lateinit var navController: NavController

    //------- ViewModel -------------
    lateinit var viewModel: MainActivityViewModel

    lateinit var appToolbar: AppToolbar

    //-------- Media recording
    private var recordSession: MediaRecorder? = null

    //------- ITag ------------------
    private var resumeCount: Int = 0
    private var sIsShown: Boolean = false


    internal class ITagServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val itagService: ITagsService.ITagBinder = service as ITagsService.ITagBinder
            itagService.removeFromForeground()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val mITagServiceConnection: ITagServiceConnection = ITagServiceConnection()
    private val disposableBag: DisposableBag = DisposableBag()
    private val mErrorListener: ErrorsObservable.IErrorListener =
        ErrorsObservable.IErrorListener { errorNotification ->
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, errorNotification.message, Toast.LENGTH_LONG
                ).show()
            }
            Log.e(LT, errorNotification.message, errorNotification.th)
        }


    private lateinit var imagePickerStartForResult: ActivityResultLauncher<Intent>
    internal lateinit var videoRecorderStartForResult: ActivityResultLauncher<Intent>

    private var locationJobServiceInitialLocationAquired: Boolean = false
    private val blockedLayoutDialog: BlockedLayoutDialog by lazy { BlockedLayoutDialog() }

    // private val eventsFollowedArray: ArrayList<EventFollowed> by lazy { ArrayList<EventFollowed>() }
    private var popupsQueue = mutableListOf<Unit>()
    private lateinit var initialLocationManagerListener: LocationListener

    val multiplePermissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->

            if (!permissionsStatusMap.containsValue(false)) {
                // Permisos concedidos
            } else {
                if (areLocationPermissionsGranted(requireBackground = true)) {
                    Log.d("PERMISSIONS", "LOCATION PERMISSION ALLOWED")
                    Log.d("LOCATION_PERMISSIONS", "PERMISO CONCEDIDO")
                    onLocationPermissionsGranted()
                } else {
                    Log.d("PERMISSIONS", "LOCATION PERMISSION NOT ALLOWED")
                    Log.d("LOCATION_PERMISSIONS", "PERMISO DENEGADO")
                    viewModel.setLocationNotAvailable()
                    onLocationPermissionsRejected()

                    if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) || shouldShowRequestPermissionRationale(
                            ACCESS_FINE_LOCATION
                        ) || shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)
                    ) {
                        val mRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            PermissionsRationaleDialog(
                                this, this, R.string.rationale_pemission_location, arrayOf(
                                    ACCESS_COARSE_LOCATION,
                                    ACCESS_FINE_LOCATION,
                                    ACCESS_BACKGROUND_LOCATION
                                ), Constants.LOCATION_REQUEST_CODE
                            )
                        } else {
                            PermissionsRationaleDialog(
                                this, this, R.string.rationale_pemission_location, arrayOf(
                                    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
                                ), Constants.LOCATION_REQUEST_CODE
                            )

                        }
                        mRationale.show()
                    } else {

                        Toast.makeText(
                            this, "Debes activar el permiso manualmente", Toast.LENGTH_LONG
                        ).show()
                        val mPermissionEnablingDialog =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                PermissionsEnablingDialog(
                                    this,
                                    this,
                                    R.string.rationale_pemission_location_manual_activation,
                                    arrayOf(
                                        ACCESS_COARSE_LOCATION,
                                        ACCESS_FINE_LOCATION,
                                        ACCESS_BACKGROUND_LOCATION
                                    ),
                                    Constants.LOCATION_REQUEST_CODE
                                )
                            } else {
                                PermissionsEnablingDialog(
                                    this,
                                    this,
                                    R.string.rationale_pemission_location_manual_activation,
                                    arrayOf(
                                        ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
                                    ),
                                    Constants.LOCATION_REQUEST_CODE
                                )
                            }
                        mPermissionEnablingDialog.show()
                    }
                }
            }
        }

    /* ------------------------------------------------------------- */

    private val filesCompressionData = 50
    private val filesMinimumSizeInKB = 100L
    private val filesMaximumSizeInKB = 1024L


    private var toPickImagePermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {


                val imagePickerIntent =
                    Lassi(this).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(1).setGridSize(3)
                        .setMediaType(MediaType.IMAGE) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                        .setCompressionRatio(filesCompressionData).setSupportedFileTypes(
                            "jpg", "jpeg", "png", "webp", "gif"
                        ).setMinFileSize(filesMinimumSizeInKB) // Restrict by minimum file size
                        .setMaxFileSize(filesMaximumSizeInKB) //  Restrict by maximum file size
                        /*
                     * Configuration for  UI
                     */.setStatusBarColor(R.color.white).setToolbarResourceColor(R.color.white)
                        .setProgressBarColor(R.color.colorAccent)
                        .setPlaceHolder(R.drawable.ic_image_placeholder)
                        .setErrorDrawable(R.drawable.ic_image_placeholder)
                        .setSelectionDrawable(R.drawable.ic_checked_media)
                        .setAlertDialogNegativeButtonColor(R.color.white)
                        .setAlertDialogPositiveButtonColor(R.color.darkGray)
                        .setGalleryBackgroundColor(R.color.gray)//Customize background color of gallery (default color is white)
                        .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)

                        .setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
//            .enableActualCircleCrop() // Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
                        .build()

//                        .setCompressionRation(50) // compress image for single item selection (can be 0 to 100)


                pickImageContract?.launch(imagePickerIntent)
            } else {
                permissionsForImages()
            }
        }

    private var pickImageContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {
                    val media = MediaFile(MediaTypesEnum.IMAGE, selectedMedia[0].path.toString())
                    viewModel.onMediaPicked(
                        MediaFile(
                            MediaTypesEnum.IMAGE, selectedMedia[0].path.toString()
                        )
                    )
                }
            }
        }


    private var toTakeVideoPermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

                val videoPickerIntent =
                    Lassi(applicationContext).with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                        .setMaxCount(5).setGridSize(3)
                        .setMediaType(MediaType.VIDEO) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                        .setCompressionRatio(10) // compress image for single item selection (can be 0 to 100)
                        .setMinTime(5) // for MediaType.VIDEO only
                        .setMaxTime(60) // for MediaType.VIDEO only
                        .setSupportedFileTypes(
                            "mp4", "mkv", "webm", "avi", "flv", "3gp"
                        ) // Filter by limited media format (Optional)
                        .setMinFileSize(100) // Restrict by minimum file size
                        .setMaxFileSize(1024) //  Restrict by maximum file size
                        .disableCrop() // to remove crop from the single image selection (crop is enabled by default for single image)
                        /*
                     * Configuration for  UI
                     */.setStatusBarColor(R.color.colorPrimaryDark)
                        .setToolbarResourceColor(R.color.colorPrimary)
                        .setProgressBarColor(R.color.colorAccent)
                        .setPlaceHolder(R.drawable.ic_image_placeholder)
                        .setErrorDrawable(R.drawable.ic_image_placeholder)
                        .setSelectionDrawable(R.drawable.ic_checked_media)
                        .setAlertDialogNegativeButtonColor(R.color.white)
                        .setAlertDialogPositiveButtonColor(R.color.colorPrimary)
                        .setGalleryBackgroundColor(R.color.gray)//Customize background color of gallery (default color is white)
                        .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .setCropAspectRatio(
                            1, 1
                        ) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                        .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                        .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                        .enableActualCircleCrop() // Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
                        .build()


                pickVideoContract?.launch(videoPickerIntent)
            } else {
                permissionsForVideo()
            }
        }

    private var pickVideoContract: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                if (!selectedMedia.isNullOrEmpty()) {
                    val filePath = selectedMedia[0].path.toString()
                    val media = MediaFile(MediaTypesEnum.VIDEO, filePath)
                    media.duration = getDuration(Uri.parse("file:$filePath"))
                    val dimensionsMap = getDimentions(Uri.parse("file:$filePath"))
                    media.width = dimensionsMap["width"]!!
                    media.height = dimensionsMap["height"]!!
                    viewModel.onMediaPicked(media)
                }
            }
        }


    private var genericPermissionsRequest: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {

            } else {
                permissionsForSMSSend()
            }
        }


    fun registerAttachmentsContracts() {

    }


    fun unRegisterAttachmentsContracts() {
        toPickImagePermissionsRequest?.unregister()
        pickImageContract?.unregister()
        toTakeVideoPermissionsRequest?.unregister()
        pickVideoContract?.unregister()
    }

    //----------------------------------------------------


    private fun onLocationPermissionsRejected() {
        SessionForProfile.getInstance(this).setProfileProperty("LocationPermissionsGranted", false)
        //      AppClass.instance.getLocationService()?.stopListeningLocation()

    }

    fun requestPermissions(showRationale: Boolean = false) {
        // if (!areLocationPermissionsGranted(true)) {
        requestPermissions(
            arrayOf(
                ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION
            ), Constants.LOCATION_REQUEST_CODE
        )
        //}


        /*
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                )
                {
                    val mRationale = PermissionsRationaleDialog(
                        this, this, R.string.rationale_pemission_location, arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        Constants.LOCATION_REQUEST_CODE
                    )
                    mRationale.show()
                } else {
                    Toast.makeText(
                        this,
                        "Debes activar el permiso manualmente",
                        Toast.LENGTH_LONG
                    ).show()
                    val mPermissionEnablingDialog = PermissionsEnablingDialog(
                        this,
                        this,
                        R.string.rationale_pemission_location_manual_activation, arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        Constants.LOCATION_REQUEST_CODE
                    )
                    mPermissionEnablingDialog.show()
                }
        */
    }


    private fun onLocationPermissionsGranted() {

        viewModel.setLocationIsAvailable()

        Log.d("LOCATION_PERMISSIONS", "PERMISO OTORGADO")

        SessionForProfile.getInstance(this).setProfileProperty("LocationPermissionsGranted", true)

        if (SessionForProfile.getInstance(this).getProfileProperty("RTLocationEnabled") == null) {
            SessionForProfile.getInstance(this).setProfileProperty("RTLocationEnabled", true)
        }

        if (SessionForProfile.getInstance(this)
                .getProfileProperty("RTLocationEnabled") as Boolean
        ) {


        }
//        val intent = Intent(applicationContext, PushNotificationService::class.java)
        /*
        val intent = Intent(Constants.BROADCAST_LOCATION_SERVICE_AVALAIBILITY)
        //      intent.action = Constants.BROADCAST_LOCATION_SERVICE_AVALAIBILITY
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            intent.putExtra("location_avalaible", true)
        )
        */
    }

    var onWantToExtendCallback: EventCloseToExpireDialogCallback =
        object : EventCloseToExpireDialogCallback {
            override fun onWantToExtend(
                dialog: EventCloseToExpireDialog, eventKey: String
            ) {
                super.onWantToExtend(dialog, eventKey)

                val onCompleteCallback: OnCompleteCallback = object : OnCompleteCallback {
                    override fun onComplete(
                        success: Boolean, result: Any?
                    ) {
                        super.onComplete(success, result)
                        val dialog = EventExtendedTimeDialog(this@MainActivity)
                        dialog.show()
                    }

                }

                /*
                EventsWSClient.instance.eventExtend(
                    eventKey, FirebaseAuth.getInstance().uid.toString(), onCompleteCallback
                )
*/
                viewModel.extendEvent(eventKey)
            }
        }

    private var networkObserver: NetworkStatusHelper? = null
    private var hasConnectivity: Boolean = false
    internal var currentModuleIndex: Int = -1
    private var contactListAdapter = ContactListsAdapter(this)

    private lateinit var mHandler: IncomingMessageHandler
    private var contactListPopupView: View? = null
    private var pendingFromActivityResult: Boolean = false

    private val mHomeFragment by lazy { HomeFragment(this as EventsPublishingCallback, viewModel) }
    private val mEventsFragment by lazy {
        EventsFragment(
            this as EventsPublishingCallback, viewModel
        )
    }
    val mMapFragment by lazy { MapSituationFragment() }
    private val mFriendsFragment by lazy { FriendsFragment(this) }

    //  private val mSettingsFragment by lazy { SettingsFragment(viewModel) }
    private val mNotificationsFragment by lazy {
        NotificationsFragment()
    }

    private val gnsStatus: GnssStatus.Callback = object : GnssStatus.Callback() {
        override fun onStarted() {
            super.onStarted()
            handleOnLocationServicesStatus()
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            handleOnLocationServicesStatus()
        }
    }
    var isActivityVisible: Boolean = false
    private var appIsBussy: Boolean = false

    //    private lateinit var videoPicker: VideoPicker
    //  private lateinit var pagerAdapter: MainActivityPagerAdapter
    lateinit var binding: ActivityMainBinding
    var isBluetoothActivated = false
    private var isBluetoothScanning: Boolean = false
    private lateinit var bluetoothIntent: Intent
    private var bluetoothService: Bluetooth? = null
    private var countDownAnimation: CountDownAnimation? = null
    private val pulseValidatorDialog: PulseValidatorDialog by lazy {
        PulseValidatorDialog(
            this, this
        )
    }
    private val arrivingConfirmationDialog: ArrivingDialog by lazy { ArrivingDialog(this, this) }
    private var eventCloseToExpireDialog: PulseValidatorDialog? = null
    private var popupWindow: PopupWindow? = null

    //private lateinit var mPresenter: MainPresenter
    private var mMyCurrentLocation: LatLng? = null


    //  private val notificationsAdapter by lazy { NotificationsAdapter(this) }
    private var videoDialog: VideoPlayerDialog? = null
    //private l|ateinit var notificationDetail: EventSliderNotificationDialog

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                packageName -> {
                    val bundle = intent!!.extras!!
                    val notificationType: String = bundle.getString("notification_type")!!

                    handleForegroundMessage(bundle)
                }

                "ON_NOTIFICATION_INCOME" -> {
                    Log.d(
                        "PUSH_MESSAGE_RECEIVER", Gson().toJson(
                            intent.extras
                        )
                    )
                }

                BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED -> {
                    if (AppClass.instance.itagPressMode == ITagModesEnum.active && viewModel.isInPanic.value == false) {
                        // esto no deberia ejecutarse                onEmergencyButtonPressed()
                    }
                }

                BROADCAST_ACTION_SHOW_FOOTER_TOOLBAR -> {
                    //                   showFooterToolBar()
                }

                BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR -> {
                    hideFooterToolBar()
                }

                AppConstants.BROADCAST_ACTION_REQUEST_PIN -> {
                    //             var ppp = 33
                    var payloadAsJson = intent.getStringExtra("data")
                    var payload = Gson().fromJson<ValidationPulsePayload>(
                        payloadAsJson, ValidationPulsePayload::class.java
                    )


                    var validationCallback: PulseValidationCallback? = null
                    when (payload.validationType) {
                        PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                            // CUANDO SE QUIERE CANCELAR UN EVENTO
                            validationCallback = object : PulseValidationCallback {

                                override fun onWrongCode(
                                    dialog: PulseValidatorDialog, securityPIN: String
                                ) {
                                    super.onWrongCode(dialog, securityPIN)

                                    if (!viewModel.isFreeUser()) {
                                        showErrorDialog(
                                            getString(R.string.error_wrong_security_code),
                                            getString(R.string.error_wrong_security_code_message),
                                            getString(R.string.close),
                                            null
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Codigo Incorrecto. Hay que Notificar a todos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }


                                override fun onValidationOK(
                                    dialog: PulseValidatorDialog, code: String
                                ) {

                                    if (viewModel.isFreeUser() == false) {

                                        showLoader(getString(R.string.closing_event_wait))



                                        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {


                                            val eventRepository = EventRepositoryImpl()

                                            var call = eventRepository.closeEvent(
                                                payload.key,
                                                FirebaseAuth.getInstance().uid.toString(),
                                                code
                                            )

                                            if (call.message == null) {
                                                AppClass.instance.getCurrentActivity()
                                                    ?.runOnUiThread {
                                                        hideLoader()

                                                        broadcastMessage(
                                                            payload.key,
                                                            AppConstants.ServiceCode.BROADCAST_EVENT_CLOSE_EVENT
                                                        )

                                                        showAnimatedDialog(
                                                            getString(R.string.closing_event_title),
                                                            getString(R.string.event_sucessfully_close)
                                                        )
                                                    }
                                            } else {
                                                runOnUiThread {

                                                    hideLoader()
                                                    showErrorDialog(call.message.toString())
                                                }
                                            }
                                        }

                                        /*
                                            hacerlo como rutina suspend
                                                    EventsWSClient.instance.closeEvent(
                                                        AppClass.instance.getPanicEventKey()!!,
                                                        FirebaseAuth.getInstance().uid.toString(),
                                                        code,
                                                        object : OnCompleteCallback {
                                                            override fun onComplete(
                                                                success: Boolean,
                                                                result: Any?
                                                            ) {
                                                                context.getCurrentActivity()
                                                                    ?.hideLoader()
                                                                AppClass.instance.getPanicEvent()
                                                                    ?.let {
                                                                        //                     mainActivity.onEventCloseDone(it.event_key)
                                                                        context.broadcastMessage(
                                                                            it.event_key,
                                                                            BROADCAST_EVENT_CLOSE_EVENT
                                                                        )
                                                                    }

                                                                context.getCurrentActivity()
                                                                    ?.showAnimatedDialog(
                                                                        context.getString(R.string.closing_event_title),
                                                                        context.getString(R.string.event_sucessfully_close)
                                                                    )

                                                            }
                                                        }
                                                    )

    */
                                    }

                                    /*
                                                                      when (context.versionLevel()) {
                                                                          VersionsEnum.DISCONNECTED -> {

                                                                          }
                                                                          else -> {

                                                                              context.getCurrentActivity()
                                                                                  ?.showLoader(context.getString(R.string.closing_event_wait))

                                                                              EventsWSClient.instance.closeEvent(
                                                                                  AppClass.instance.getPanicEventKey()!!,
                                                                                  FirebaseAuth.getInstance().uid.toString(),
                                                                                  code,
                                                                                  object : OnCompleteCallback {
                                                                                      override fun onComplete(
                                                                                          success: Boolean,
                                                                                          result: Any?
                                                                                      ) {
                                                                                          context.getCurrentActivity()?.hideLoader()
                                                                                          AppClass.instance.getPanicEvent()
                                                                                              ?.let {
                                                                                                  //                     mainActivity.onEventCloseDone(it.event_key)
                                                                                                  context.broadcastMessage(
                                                                                                      it.event_key,
                                                                                                      BROADCAST_EVENT_CLOSE_EVENT
                                                                                                  )
                                                                                              }

                                                                                          context.getCurrentActivity()
                                                                                              ?.showAnimatedDialog(
                                                                                                  context.getString(R.string.closing_event_title),
                                                                                                  context.getString(R.string.event_sucessfully_close)
                                                                                              )

                                                                                      }
                                                                                  }
                                                                              )


                                                                          }
                                                                      }
                                  */
                                    SessionApp.getInstance(context).isInPanic(false)
                                    broadcastMessage(
                                        null, AppConstants.BROADCAST_ACTION_REFRESH_PANIC_BUTTON
                                    )

                                }
                            }

                            requestStatusConfirmation(
                                PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT, validationCallback
                            )

                        }

                        PulseRequestTarget.PULSE_VALIDATION -> TODO()
                        PulseRequestTarget.VALIDATE_USER -> TODO()
                        PulseRequestTarget.ON_FALLING_VALIDATION -> TODO()
                    }

                }


                BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED -> {
                    // se encanga de actualizar el estado del indicador de disponibilidad del dispositivo fisico

                    // pasarlo a un mensaje distinto que sea acerca de refrescar el tag_indicator
                    if (SessionApp.getInstance(context).isBTPanicButtonEnabled) {/*
                         if (AppClass.instance.getBLEService() != null) {
                             var bleService = AppClass.instance.getBLEService()!!
                             var devicesList = bleService.getDevicesList()
                             if (devicesList.isNotEmpty()) {
                                 binding?.itagsIndicator?.visibility = View.VISIBLE

                                 var isPulsatorConnected = false
                                 devicesList.forEach { result ->


                                     if (bleService?.isDeviceConnected(result.device) ?: false) {
                                         isPulsatorConnected = true
                                     }
                                 }

                                 if (isPulsatorConnected) {
                                     binding?.itag?.setImageDrawable(getDrawable(R.drawable.itag_red))
                                 } else {
                                     binding?.itag?.setImageDrawable(getDrawable(R.drawable.itag_white))
                                 }
                             } else {
                                 binding?.itagsIndicator?.visibility = View.GONE
                             }
                         } else {
                             binding?.itagsIndicator?.visibility = View.GONE
                         }

                         */
                    } else {
                        binding.root.findViewById<View>(R.id.itags_indicator).visibility = GONE
                    }
                }


                BROADCAST_MESSAGE_SCAN_RESULT_UPDATED -> {

                    val dataAsJson = intent.getStringExtra("data")
                    val listType = object : TypeToken<MutableList<ScanResult>>() {}.type
                    val scanResult = Gson().fromJson<MutableList<ScanResult>>(dataAsJson, listType)

                    broadcastMessage(null, BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED)/*
                        // pasarlo a un mensaje distinto que sea acerca de refrescar el tag_indicator
                       if ( SessionApp.getInstance(context).isBTPanicButtonEnabled() ) {
                           if (scanResult.isNotEmpty()) {
                               binding?.itagsIndicator?.visibility = View.VISIBLE

                               var isPulsatorConnected = false
                               scanResult.forEach { result ->
                                   if (isBleDeviceConnected(result.device.address)) {
                                       isPulsatorConnected = true
                                   }
                               }

                               if (isPulsatorConnected) {
                                   binding?.itag?.setImageDrawable(getDrawable(R.drawable.itag_red))
                               } else {
                                   binding?.itag?.setImageDrawable(getDrawable(R.drawable.itag_white))
                               }
                           } else {
                               binding?.itagsIndicator?.visibility = View.GONE
                           }
                       }
                        else
                       {
                           binding?.itagsIndicator?.visibility = View.GONE
                       }
                        */
                }

                BROADCAST_EVENT_CLOSE_TO_EXPIRE -> {
                    if (eventCloseToExpireDialog == null || eventCloseToExpireDialog?.isShowing == false) {
                        showIsCloseToExpireDialog(
                            intent.extras?.get("event_key").toString(),
                            intent.extras,
                            onWantToExtendCallback
                        )
                    }
                }

                BROADCAST_PULSE_REQUIRED -> {


                    val pulseValidationCallback = object : PulseValidationCallback {
                        override fun onValidationOK(
                            dialog: PulseValidatorDialog, securityPIN: String
                        ) {
                            super.onValidationOK(dialog, securityPIN)/*
                                                        val wsCallback = object : OnCompleteCallback {
                                                            override fun onComplete(success: Boolean, result: Any?) {
                                                                dialog.dismiss()
                                                            }

                                                            override fun onError(exception: Exception) {
                                                                this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                                            }
                                                        }


                                                        UsersWSClient.instance.updateUserStatus(
                                                            securityPIN, AppClass.instance.lastLocation, wsCallback
                                                        )
                            */

                            SmartLocation.with(context).location().oneFix().start { location ->
                                viewModel.securityPINIntroduced(
                                    securityPIN, LatLng(location.latitude, location.longitude)
                                )

                            }

                        }

                        override fun onSilentAlarmCode(
                            dialog: PulseValidatorDialog, securityPIN: String
                        ) {

                            /*
                            val wsCallback = object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    dialog.dismiss()
                                }

                                override fun onError(exception: Exception) {
                                    this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                }
                            }
                            UsersWSClient.instance.updateUserStatus(
                                securityPIN, AppClass.instance.lastLocation, wsCallback
                            )
*/
                            SmartLocation.with(context).location().oneFix().start { location ->
                                viewModel.securityPINIntroduced(
                                    securityPIN, LatLng(location.latitude, location.longitude)
                                )

                            }
                            //          viewModel.securityPINIntroduced(SessionForProfile(this@MainActivity).getUserId(),securityPIN, AppClass.instance.lastLocation)


                        }

                        override fun onNoResponse() {
                            super.onNoResponse()/*
                                 UsersWSClient.instance.updateUserStatus(
                                     "", AppClass.instance.lastLocation, null
                                 )
     */

                            SmartLocation.with(context).location().oneFix().start { location ->
                                viewModel.securityPINIntroduced(
                                    "", LatLng(location.latitude, location.longitude)
                                )

                            }
                        }

                        override fun onWrongCode(
                            dialog: PulseValidatorDialog, securityPIN: String
                        ) {
                            super.onWrongCode(dialog, securityPIN)/*
                                                        val wsCallback = object : OnCompleteCallback {
                                                            override fun onComplete(success: Boolean, result: Any?) {
                                                                dialog.dismiss()
                                                            }

                                                            override fun onError(exception: Exception) {
                                                                this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                                            }
                                                        }

                                                        UsersWSClient.instance.updateUserStatus(
                                                            securityPIN, AppClass.instance.lastLocation, wsCallback
                                                        )
                            */

                            SmartLocation.with(context).location().oneFix().start { location ->
                                viewModel.securityPINIntroduced(
                                    securityPIN, LatLng(location.latitude, location.longitude)
                                )

                            }
                        }

                    }

                    requestStatusConfirmation(
                        PulseRequestTarget.PULSE_VALIDATION, pulseValidationCallback
                    )
                }

                BROADCAST_DID_YOU_ARRIVE_REQUEST -> {
                    var eventKey = intent.extras?.get("event_key").toString()
                    if (eventExists(eventKey)) {
                        if (!arrivingConfirmationDialog.isShowing) {

                            requestArrivingConfirmation(eventKey)
                        }
                    } else {
                        Log.d("BROADCASTED_MESSAGE", "borro porque el evento ya no existe")
                    }

                }

                BROADCAST_BLOCKED_LAYOUT_REQUIRED -> {

                    showBlockedLayout()/*
                                        if (binding.blockedScreenLayout.visibility != View.VISIBLE) {
                                            binding.blockedScreenLayout.visibility = View.VISIBLE
                                        }
                    */
                }

                BROADCAST_BLOCKED_LAYOUT_DISMISS -> {

                    if (isBlockedLayoutVisible()) {
                        hideBlockedLayout()
                    }
                }


                BROADCAST_MESSAGE_BLE_DEVICE_DISCOVERED -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)
                    iTagDialog(
                        this@MainActivity, this@MainActivity
                    ).withTitle(getString(R.string.bluetooth_device_discovered_title))
                        .withDevice(device).show()

                }

                BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED -> {

                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)
                    iTagDialog(
                        this@MainActivity, this@MainActivity
                    ).withTitle(getString(R.string.bluetooth_device_connected_title))
                        .withDevice(device).show()

                }
            }

        }
    }

    /*
        private val eventExpirationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (eventCloseToExpireDialog == null || eventCloseToExpireDialog?.isShowing == false) {
                    when (intent?.action) {
                        Constants.BROADCAST_EVENT_CLOSE_TO_EXPIRE -> {
                            if (eventCloseToExpireDialog == null || eventCloseToExpireDialog?.isShowing() == false) {
                                showIsCloseToExpireDialog(
                                    intent.extras?.get("event_key").toString(),
                                    intent.extras,
                                    onWantToExtendCallback
                                )
                            }
                        }
                    }
                }
            }
        }

        private val pulseRequestReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (pulseValidatorDialog == null || pulseValidatorDialog?.isShowing == false) {

                    when (intent?.action) {
                        Constants.BROADCAST_PULSE_REQUIRED -> {

                            val pulseValidationCallback = object : PulseValidationCallback {
                                override fun onValidationOK(
                                    dialog: PulseValidatorDialog,
                                    securityPIN: String
                                ) {
                                    super.onValidationOK(dialog, securityPIN)

                                    val wsCallback = object : OnCompleteCallback {
                                        override fun onComplete(success: Boolean, result: Any?) {
                                            dialog.dismiss()
                                        }

                                        override fun onError(exception: Exception) {
                                            this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                        }
                                    }


                                    UsersWSClient.instance.updateUserStatus(
                                        securityPIN,
                                        AppClass.instance.lastLocation,
                                        wsCallback
                                    )
                                }

                                override fun onSilentAlarmCode(
                                    dialog: PulseValidatorDialog,
                                    securityPIN: String
                                ) {

                                    val wsCallback = object : OnCompleteCallback {
                                        override fun onComplete(success: Boolean, result: Any?) {
                                            dialog.dismiss()
                                        }

                                        override fun onError(exception: Exception) {
                                            this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                        }
                                    }
                                    UsersWSClient.instance.updateUserStatus(
                                        securityPIN,
                                        AppClass.instance.lastLocation,
                                        wsCallback
                                    )
                                }

                                override fun onNoResponse() {
                                    super.onNoResponse()
                                    UsersWSClient.instance.updateUserStatus(
                                        "",
                                        AppClass.instance.lastLocation,
                                        null
                                    )

                                }

                                override fun onWrongCode(
                                    dialog: PulseValidatorDialog,
                                    securityPIN: String
                                ) {
                                    super.onWrongCode(dialog, securityPIN)

                                    val wsCallback = object : OnCompleteCallback {
                                        override fun onComplete(success: Boolean, result: Any?) {
                                            dialog.dismiss()
                                        }

                                        override fun onError(exception: Exception) {
                                            this@MainActivity.showErrorDialog(exception.localizedMessage.toString())
                                        }
                                    }

                                    UsersWSClient.instance.updateUserStatus(
                                        securityPIN,
                                        AppClass.instance.lastLocation,
                                        wsCallback
                                    )

                                }

                                override fun onCancel(dialog: PulseValidatorDialog) {
                                    super.onCancel(dialog)
                                }
                            }

                            requestStatusConfirmation(
                                PulseRequestTarget.PULSE_VALIDATION,
                                pulseValidationCallback
                            )
                        }
                        else -> {
                            requestStatusConfirmation(PulseRequestTarget.VALIDATE_USER, null)
                        }
                    }
                } else {
                    Log.d(
                        "PULSE_VALIDATION",
                        "El dialogo ya se esta mostrando, deberia dejar marcado el estado del usuario y bloquear hasta que ingrese el codigo"
                    )
                }
                //     }

            }
        }

        private val arriveValidationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {


                when (intent?.action) {
                    Constants.BROADCAST_DID_YOU_ARRIVE_REQUEST -> {
                        var eventKey = intent?.extras?.get("event_key").toString()
                        if (eventExists(eventKey)) {
                            if (arrivingConfirmationDialog == null || arrivingConfirmationDialog?.isShowing == false) {

                                requestArrivingConfirmation(eventKey)
                            }
                        } else {
                            Log.d("BROADCASTED_MESSAGE", "borro porque el evento ya no existe")
                        }

                    }
                    else -> {
                        requestStatusConfirmation(PulseRequestTarget.VALIDATE_USER, null)
                    }
                }

                //     }

            }
        }
    */

    private fun eventExists(eventKey: String): Boolean {/*   var eventExists = false
           eventsFollowedArray.forEach { event ->
               if (event.event_key == eventKey) {
                   eventExists = true
                   return@forEach
               }

           }
           return eventExists*/

        return viewModel.eventExists(eventKey)
    }

    /*
        private val unreadMessagesBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {/* Toast.makeText(context, "Message is: "+ intent.getStringExtra("message"), Toast.LENGTH_LONG)
                    .show();*/
                val action = intent.action
                when (action) {
                    Constants.BROADCAST_UNREAD_MESSAGES_UPDATES -> {
                        val map = intent.getSerializableExtra("data") as HashMap<String, UnreadMessages>
                        updateUnreadMessagesIndicator(map)
                    }
                }
            }
        }
    */

    private var fragmentContainer: FragmentContainerView? = null


    override fun setToolbarTitle(title: String) {
        appToolbar.updateTitle(title)
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

    val callback = object : OnBackPressedCallback(true /* enabled by default */) {
        override fun handleOnBackPressed() {
            // Define aquí tu lógica personalizada
            var pp = 3
        }
    }


    val args: HomeFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("SCREEN_DENSITY", getDensityName(this).toString())

        hideStatusBar()

        // TODO: Borrar toda referencia a la clase AppClass es decir , a la mainActivity y reemplazar por requireActivity
        AppClass.instance.setMainActivityRef(this)

        onBackPressedDispatcher.addCallback(this, callback)

        if (BuildConfig.NAVIGATION_HOST_MODE?.toBoolean() == true) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            binding.eventsCounterText.visibility = GONE
            // Obtain reference to the NavHostFragment
            appToolbar =
                AppToolbar.Companion.Builder().withBinding(binding.includeCustomToolbar).build()

            appToolbar.onBackPressed {
                this.findNavController(R.id.nav_host_fragment).popBackStack()
            }
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            // Get the NavController
            navController = navHostFragment.navController
            val appBarConfiguration = AppBarConfiguration(navController.graph)

//            binding.toolbar.setupWithNavController(navController, appBarConfiguration)

            setContentView(binding.root)

            if (!isGooglePlayInstalled()) {
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
            } else {
                if (checkGooglePlayServices()) {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
                }
            }


            startCoreObservers()
            runServices()
            /*
            TODO: MOVER AL AREA DE INICIALIZCION DE LA APP
            getIntentData()
            val user = SessionForProfile.getInstance(this).getUserProfile()

            viewModel = MainActivityViewModel(this, user.user_key)
            viewModel.setUser(user)
            AppClass.instance.mainViewModel = viewModel
            setupUI()

            startObservers()
            runServices()
            registerReceivers()
            AppClass.instance.subscribeToUnreadMessages()
            NonUI.getInstance(applicationContext).initialize()
            setListnerToRootView()
            adjustNavHostFragmentToLimits()
*/
            return@onCreate
        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        //----------------------
        val userParam = intent.extras?.getString("user")
        val user = Gson().fromJson(userParam, User::class.java)!!
        SessionForProfile.getInstance(applicationContext).storeUserProfile(user)
        viewModel = MainActivityViewModel.getInstance(this, user.user_key)
        viewModel.setUser(user)

        //  speak("Bienvenido a IAN!!!")


        //configureCropOptions()
        //multiPickerWrapper = MultiPickerWrapper(this, cacheLocation)
        intentHandler(intent)
        AppClass.instance.setCurrentActivity(this)


        mHandler = IncomingMessageHandler()

        if (!isGooglePlayInstalled()) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        } else {
            if (checkGooglePlayServices()) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
            }
        }


        registerActivityResultContracts()
        //  startService(Intent(applicationContext, PanicButtonService::class.java))


        if (SessionApp.getInstance(this).isBTPanicButtonEnabled) {
            requirePermissionsBLE(MY_PERMISSIONS_REQUEST_BLUETOOTH_PERMISSIONS)
        }

        permissionsReadWrite()


        //    binding.root.findViewById<View>(R.id.tags_miniatures_container).removeAllViews()

        appToolbar.clearTagsMiniatures()
        KeyboardUtil(this, binding.navHostFragment)


        //    val me = SessionForProfile.getInstance(this).getUserProfile()
        // AppClass.instance.mainViewModel = viewModel
        if (FirebaseAuth.getInstance().currentUser != null) {

            if (BuildConfig.NAVIGATION_HOST_MODE?.toBoolean() == false) {
                var navHostFragment = binding.root.findViewById<View>(R.id.nav_host_fragment)
                navHostFragment.visibility = View.GONE
            }
            setContentView(binding.root)

            getIntentData()

            setupUIWhenReady()

            startCoreObservers()

            startObservers()

            runServices()
            registerReceivers()
            AppClass.instance.subscribeToUnreadMessages()

            //   AppClass.instance.fetchCurrentSubscriptionType()

            NonUI.getInstance(applicationContext).initialize()

//            setupTagsDisplay()

            setListnerToRootView()
            AppClass.instance.logged = true
        } else {
            startActivity(LoginActivity::class.java, null)
        }
        takeKeyEvents(true)
        goHome()
//        switchToModule(IANModulesEnum.MAIN.ordinal, "Home")
        // Inicializo el array de conexines
        initITags()


        var sharedPrefereces =
            getSharePreferencesMessages("notifications") as HashMap<String, String>
        if (sharedPrefereces != null) {
            handlePushNotifications(sharedPrefereces)
        }


    }

    private fun handlePushNotifications(sharedPrefereces: HashMap<String, String>) {
        Log.d("PREFS", Gson().toJson(sharedPrefereces))


        if (sharedPrefereces.size > 0) {
            this@MainActivity.clearPendingNotifications()
            var pref: String = sharedPrefereces["notifications"].toString()
            var notificationInfo: NotificationsUtils.PushNotification =
                Gson().fromJson(pref, NotificationsUtils.PushNotification::class.java)
            when (notificationInfo.notificationType) {
                AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE -> {
                    viewModel.onNewMessageNotification(notificationInfo)
                }

                AppConstants.NOTIFICATION_TYPE_PANIC_BUTTON -> {
                    viewModel.onPanicButtonNotification(notificationInfo)
                }

                else -> {
                    var pppp = 33
                }
            }


            //     handleTouch()
            //       switchToModule(5, "notifications", true, null)

        }/*
                sharedPrefereces.forEach { key, valuesAsString ->

                    var map = Gson().fromJson<NotificationsUtils.PushNotification>(
                        valuesAsString, NotificationsUtils.PushNotification::class.java
                    )

                    Toast.makeText(
                        this@MainActivity, map.notificationType.toString(), Toast.LENGTH_SHORT
                    ).show()

                    //viewModel.onNewPendingNotification(map)


                }

         */
    }


    var keyboardHeightProvider: KeyboardHeightProvider? = null

    private fun runServices() {
        /*
                val eventService = EventService.getInstance(applicationContext)
                val intent = Intent(this, EventService::class.java)
                this.startService(intent)
                eventService.getResult().observe(this) { status ->
                    when (status) {
                        is Resource.Error -> {
                            showErrorDialog(status.message.toString())
                        }

                        is Resource.Loading -> {
                            showLoader()
                        }

                        is Resource.Success -> {

                            findNavController(R.id.nav_host_fragment).apply {
                                popBackStack(R.id.homeFragment, false)
                                navigate(R.id.homeFragment)
                            }
                            hideLoader()
        //                    viewModel.switchToModule(IANModulesEnum.MAIN.ordinal, "home")
                            // volver a homescreen y borrar el stack para que no pueda volver atras


                        }
                    }
                }
        */
        /*
                if (isServiceRunning(NewLocationJobService::class.java) == false) {
                    applicationContext.scheduleLocationUpdateJob()
                }
        */

        // --------- ITAG SERVICE
        if (SessionApp.getInstance(applicationContext).isBTPanicButtonEnabled) {
            if (isServiceRunning(ITagsService::class.java) == false) {

                ITag.initITag(applicationContext)
                AppClass.instance.initializeITags()

                /*
                                val serviceIntent = Intent(applicationContext, ITagsService::class.java)
                                startService(serviceIntent)
                                Log.d(this::class.java.name, "EMG_ Inicio servicio de ITag")
                                ITagsService.start(applicationContext) // expected to create application and thus start waytooday
                */
            } else {
                Log.d(this::class.java.name, "EMG_ El servicio de ITag estaba corriendo")
            }
        }

        if (isServiceRunning(ServiceLocation::class.java) == false) {
            Log.d(
                "MainActivity", "LOC_ El servicio de ubicación no estaba corriendo, lo creo"
            )


            // En tu actividad
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as ServiceLocationBinder
                    val locationService = binder.getService()
                    AppClass.instance.serviceLocationPointer = locationService

                    val receiver = AppStatusReceiver(locationService)
                    val filter = IntentFilter()
                    filter.addAction(AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND)
                    filter.addAction(AppConstants.BROADCAST_ACTION_ENTER_FOREGROUND)
                    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                        receiver, filter
                    )
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(Intent(AppConstants.BROADCAST_ACTION_ENTER_FOREGROUND))


                    // Ahora puedes interactuar con el servicio
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    // Maneja la desconexión del servicio aquí si es necesario
                }
            }
            val serviceIntent = Intent(applicationContext, ServiceLocation::class.java)
            startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            Log.d("MainActivity", "LOC_ El servicio de ubicación estaba corriendo")
        }
    }
//            locationService = LocationServiceManager.getInstance(requireContext()).bindLocationService()

    /*
    popo

        fun onStartApplication() {
            val me = SessionForProfile.getInstance(this).getUserProfile()
            viewModel = MainActivityViewModel(this, me.user_key)
            AppClass.instance.mainViewModel = viewModel
            if (FirebaseAuth.getInstance().currentUser != null) {
                Thread {
                    /*
                    mPresenter = MainPresenter(this, this)
                    mPresenter.subscribe()
                    */
                    showSnackBar(binding.root, "Implementar el subscribe")
                    setContentView(binding.root)

                }.start()

                getIntentData()
                setupUI()

                setupObservers()

                registerReceivers()
                AppClass.instance.subscribeToUnreadMessages()

                nonUI = NonUI.getInstance(this)
                // ITagsService.start(applicationContext)
                //    enablePanicButtonService()
                // setupTagsDisplay()
            } else {
                startActivity(LoginActivity::class.java, null)
            }
            takeKeyEvents(true)
            switchToModule(0, "Home")
            // Inicializo el array de conexines
            initITags()
        }
    */

    private val lock = Any()

    /**
     * Estos son los observers que arrancan cuando la App se ejecuta sin importar el estado de loggeo.
     */
    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
    fun startCoreObservers() {

        viewModel = MainActivityViewModel.getInstance(this)
        viewModel.appStatus.observe(this) { status ->
            when (status) {
                MainActivityViewModel.AppStatus.NOT_LOGGED -> Toast.makeText(
                    this, "NOT_LOGGED", Toast.LENGTH_SHORT
                ).show()


                MainActivityViewModel.AppStatus.INITIALIZING -> {

                    lifecycleScope.launch {
                        //       Toast.makeText(this@MainActivity, "INITIALIZING", Toast.LENGTH_SHORT).show()

                    }

                    //                val me = SessionForProfile.getInstance(this).getUserProfile()
                    //       viewModel = MainActivityViewModel(this, me.user_key)

                    getIntentData()

                    startObservers()
                    registerReceivers()

                    setupUIWhenReady()

                    AppClass.instance.subscribeToUnreadMessages()

                    nonUI = NonUI.getInstance(this)

                    takeKeyEvents(true)
                    // Inicializo el array de conexines
                    initITags()

                    setAppStatus(MainActivityViewModel.AppStatus.READY)
                }

                MainActivityViewModel.AppStatus.READY -> {
                    //              Toast.makeText(this, "READY", Toast.LENGTH_SHORT).show()
                }

                null -> TODO()
            }
        }


    }


    private val _isLocationEnabled = MutableLiveData<Boolean>()
    val isLocationEnabled: LiveData<Boolean> get() = _isLocationEnabled


    fun stopCoreObservers() {
        viewModel.appStatus.removeObservers(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
    fun startObservers() {

        /*
                viewModel.serverTime.observe(this) { time ->
                    if (time != null) {
                        setServerTime(time)
                    }
                }
        */

        viewModel.homeIconVisible.observe(this) { visible ->
            if (visible) {
                binding.actionHome.visibility = VISIBLE
            } else {
                binding.actionHome.visibility = GONE
            }
        }

        viewModel.initializationReady.observe(this) {
            //     requestLocationPermissions()
            var permissionGranted =
                SessionForProfile.getInstance(this).getProfileProperty("LocationPermissionsGranted")
            if (permissionGranted == null) {
                if (!areLocationPermissionsGranted(true)) {
                    requestPermissions()
                }
            } else if (permissionGranted == true && !areLocationPermissionsGranted(true)) {
                requestPermissions()
            }
            if (permissionGranted == false) {
                viewModel.setLocationNotAvailable()
                // requestPermissions()
            }
        }



        viewModel.userSubscriptionTypeAsFlow.observe(this) { resource ->
            when (resource) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {

                    var subscriptionType = resource.data!!
                    Toast.makeText(this, subscriptionType.toString(), Toast.LENGTH_SHORT).show()
                    //  viewModel.updateUserSubscriptionType(subscriptionType)

                    /*
                    subscriptions?.let { list ->
                        list.forEach { subscription ->
                            // comparar si la fecha de hoy en milisegundos es mayor a la fecha de subscripcion (subscripted_on) y menor a la fecha de expiracion (expires_on)
                            if (subscription.subscripted_on < System.currentTimeMillis() && subscription.expires_on > System.currentTimeMillis()) {
                                var pp = 3
                            }
                        }
                    }
*/
                }
            }
        }

        viewModel.contactRequest.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Success -> {
                    hideLoader()
                    findNavController(R.id.nav_host_fragment).navigate(
                        R.id.contactAcceptInvitationDialog,
                        bundleOf("contact" to resource.data as UserMinimum)
                    )
                }

                is Resource.Error -> {
                    hideLoader()
                    when (resource.message) {
                        "contact_not_found" -> {
                            showErrorDialog(getString(R.string.contact_not_found))
                        }

                        "already_friends" -> {
                            showErrorDialog(getString(R.string.already_friends))
                        }

                        else -> {
                            showErrorDialog(resource.message.toString())
                        }
                    }


                }
            }
        }

        viewModel.contactAcceptance.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Success -> {
                    hideLoader()
                    lifecycleScope.launch(Dispatchers.Main) {
                        findNavController(R.id.nav_host_fragment).navigate(
                            R.id.newFriendDialog,
                            bundleOf("contactsHashMap" to resource.data as HashMap<String, Contact>)
                        )
                    }
                }

                is Resource.Error -> {
                    hideLoader()
                    when (resource.message) {
                        "contact_not_found" -> {
                            showErrorDialog(getString(R.string.contact_not_found))
                        }

                        "already_friends" -> {
                            showErrorDialog(getString(R.string.already_friends))
                        }

                        else -> {
                            showErrorDialog(resource.message.toString())
                        }
                    }

                }
            }
        }

        viewModel.bluetoothStatus.observe(this) { isOn ->
            if (isOn) AppClass.instance.enableBluetoothPeripherals()
            else AppClass.instance.disableBluetoothPeripherals()
        }


        viewModel.bottomBarVisibilityStatus.observe(this) { visible ->
            if (visible != null) {
                if (visible != false) binding.bottomToolbar.visibility = VISIBLE
                else binding.bottomToolbar.visibility = INVISIBLE
                viewModel.resetBottomBarVisibilityStatus()
            }
        }

        viewModel.isLocationAvailable.observe(this) { available ->
            if (available) {
                //              onLocationPermissionsGranted()
            } else {
                onLocationPermissionsRejected()
            }

        }


        viewModel.title.observe(this) { title ->
            setTitleBarTitle(title)
        }

        viewModel.titleBarCardViewVisible.observe(this) { visible ->
            if (visible) {
                appToolbar.showTitle()
//                binding.titleBar.visibility = VISIBLE
            } else {
//                binding.titleBar.visibility = GONE
                appToolbar.hideTitle()
            }
        }




        viewModel.dialogToShow.observe(this) { dialog ->
            when (dialog.dialogToShow) {
                DialogsEnum.EVENTEXTENDEDTIMEDIALOG -> {
                    val dialog = EventExtendedTimeDialog(this@MainActivity)
                    dialog.show()
                }

                DialogsEnum.EVENT_CLOSE_SUCCESSFULLY -> {
                    var eventKey: String = dialog.data as String
                    whenEventCloseSuccessfully(eventKey)
                }

                DialogsEnum.STATUS_VALIDATION_DIALOG -> TODO()
            }
        }

        viewModel.urlImageToPreLoad.observe(this) { imageReference ->

            val requestOptions: RequestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)

            Glide.with(this).asBitmap().load(imageReference).apply(requestOptions).submit()
        }/*
                viewModel.currentModule.observe(this) { moduleIndex ->
                    switchToModule(moduleIndex, null)

                }
        */
        viewModel.goBack.observe(this) { goBack ->
            handleGoBack()
        }

        viewModel.error.observe(this) { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                showErrorDialog(message.toString())
            }
        }


        viewModel.postingEvent.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
//                    showLoader(getString(R.string.message_posting_event))
                    showLoader(R.raw.lottie_campana_emitiendo)
                }

                is Resource.Success -> {
                    hideLoader()
//                    showEventRedirectorDialog(status.data.toString())
                    viewModel.showGoToEventDialog(null, status.data.toString())

                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(status.message.toString())
                }

                else -> {}
            }
        }

        viewModel.loader.observe(this) { visible ->
            if (visible == true) {
                showLoader()
            } else {
                hideLoader()

            }
        }

        viewModel.loaderCompass.observe(this) { visible ->
            lifecycleScope.launch(Dispatchers.Main) {
                if (visible == true) {
                    showLoader(R.raw.lottie_compass_2)
                } else {
                    hideLoader()

                }
            }
        }

        viewModel.panicButtonStatus.observe(this) { status ->

            when (status) {
                is Resource.Loading -> {
                    showLoader(getString(R.string.message_posting_panic_event))
                }

                is Resource.Success -> {
                    hideLoader()

                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(status.message.toString())
                }

                else -> {}
            }

        }

        viewModel.subscribingToEventStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    showLoader(getString(R.string.subscribing_to_event))
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(status.message.toString())
                }

                is Resource.Success -> {
                    hideLoader()
//                    showEventRedirectorDialog(status.data.toString())
                    viewModel.showGoToEventDialog(null, status.data.toString())

                }
            }
        }

        viewModel.closingEventStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    showLoader(getString(R.string.closing_event_wait))
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(status.message.toString())
                }

                is Resource.Success -> {
                    hideLoader()

                }
            }
        }


        viewModel.notifications.observe(this) { notifications ->
            //  if (notifications.size > 0) appToolbar.showNotificationsBell() else appToolbar.hideNotificationsBell()
            val unreadnotifications =
                notifications.filter { notification -> notification.read == false }.size
            appToolbar.updateNotificationsBell(notifications.size, unreadnotifications)
        }

        viewModel.notificationsFlow.observe(this) { dataEvent ->
            when (dataEvent) {
                is NotificationsRepository.DataEvent.ChildAdded -> {
                    val notification = dataEvent.data
                    val event = dataEvent.data
                    val dataMap = (dataEvent.data.event_data as HashMap<String, Any>)
                    val authorKey = dataMap["user_key"].toString()

                    if (!notification.read && authorKey != UserViewModel.getInstance()
                            .getUser()?.user_key ?: ""
                    ) {
                        when (event.event_type) {
                            EventTypesEnum.SEND_POLICE.name -> {
                                playSound(R.raw.policesiren, null, null)
                            }

                            EventTypesEnum.SEND_FIREMAN.name -> {
                                playSound(R.raw.fire_truck_siren, null, null)
                            }
                        }
                    }

                }

                else -> {}
            }
        }

        viewModel.showGoToEventDialog.observe(this) { propsMap ->

            val bundle = Bundle()
            bundle.putString("event_key", propsMap?.get("event_key") as String?)
            bundle.putString("notification_key", propsMap?.get("notification_key") as String?)

            findNavController(R.id.nav_host_fragment).navigate(
                R.id.eventPublishedDoneDialog, bundle
            )

            /*
            val callbackDialog: OnEventPublishedDone = object : OnEventPublishedDone {
                override fun onBringMeToEvent() {
                    if (requireActivity() is MainActivityCallback) {
                        (requireActivity() as MainActivityCallback).goToEvent(status.data?.event_key!!)
                    }
                }

                override fun onRefuse() {

                }
            }
            val doneDialog = EventPublishedDoneDialog(
                requireContext(), requireActivity(), callbackDialog
            )
*/

        }

        viewModel.eventToOpen.observe(this) { eventKey ->

            if (eventKey != null) {
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.mapSituationFragment, bundleOf("eventKey" to eventKey)
                )
            }
        }


        AppClass.instance.getEventsFollowedFlow.observe(this) { event ->

            when (event) {
                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded -> {
                    onEventFollowedAdded(event.data)
                    if (event.data.author.author_key.toString() != UserViewModel.getInstance()
                            .getUser()?.user_key ?: ""
                    ) {
                        when (event.data.event_type) {
                            EventTypesEnum.SEND_POLICE.name -> {
                                playSound(R.raw.policesiren, null, null)
                            }

                            EventTypesEnum.SEND_FIREMAN.name -> {
                                playSound(R.raw.fire_truck_siren, null, null)
                            }
                        }
                    }
                }

                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged -> onEventFollowedChanged(
                    event.data
                )

                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildMoved -> TODO()
                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved -> onEventFollowedRemoved(
                    event.data.event_key
                )

                is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> TODO()
                null -> TODO()
            }
        }


        AppClass.instance.eventsListFlow.observe(this) { event ->
            when (event) {
                is EventsRepository.DataEvent.OnChildAdded -> {

                    onEventFollowedAdded(event.data)
                }

                is EventsRepository.DataEvent.OnChildChanged -> onEventFollowedChanged(event.data)
                is EventsRepository.DataEvent.OnChildRemoved -> onEventFollowedRemoved(event.data.event_key)
                //is EventsRepository.DataEvent.onChildMoved -> {}
                is EventsRepository.DataEvent.OnChildMoved -> {}
                is EventsRepository.DataEvent.OnError -> {
                    var pp = 3
                }

                else -> {}
            }
        }

        viewModel.validationResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Success -> {
                    hideLoader()
                    correctAnimation(binding.root)
                    findNavController(R.id.nav_host_fragment).popBackStack()
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(result.message.toString())
                }
            }
        }


        AppClass.instance.unreadMessagesFlow.observe(this) { unreadsList ->
            //--- Determino si debo excluir algunos mensajes por estar dentro de ese chat
            var visibleChatRoom: String? = null
            var fragmentActive = AppClass.instance.getActiveFragment()
            fragmentActive?.let { fragment ->
                if (fragment is MessagesInEventFragment) {
                    visibleChatRoom = (fragment as MessagesInEventFragment).getChatroomKey()
                }
            }

            var unreadMessages = 0
            unreadsList?.forEach { record ->
                if (visibleChatRoom == null || visibleChatRoom != record.chat_room_key) {
                    unreadMessages = (unreadMessages + record.qty).toInt()
                }
            }


            //   withContext(Dispatchers.Main) {
            // appToolbar.updateChatCount(unreadMessages)
            //
            /*
                if (unreadMessages > 0) {
                    binding.chatIndicator.visibility = VISIBLE
                    binding.messagesPendingCounterText.text = unreadMessages.toString()
                } else {
                    binding.chatIndicator.visibility = GONE
                }
                */
            //     }
            //         }


        }


        viewModel.pendingNotifications.observe(this) { pendingNotifications ->
            /*
            if (pendingNotifications > 0) {
                showPendingNotificationsPopups(pendingNotifications)
            } else {
                binding.notificationsIndicator.visibility = GONE
            }
            */
        }

        viewModel.refreshEventsShortcut.observe(this) { refresh ->
            if (refresh) {
                //       updateMapButton()
                viewModel.resetRefreshCommand()
            }
        }

        viewModel.enableMapIcon.observe(this) { enabled ->
            //------------
            if (enabled) binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map))
            else binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map_disabled))
        }

        viewModel.showEventsCounter.observe(this) { show ->
            binding.eventsCounterText.isVisible = show
            if (show) {
                // agregar el control de que hay actualizaciones pendientes y lo pone en rojo
                binding.eventsCounterText.background = getDrawable(R.drawable.bg_yelow_point)
            }

            /*
                        if (eventsCount > 0) {
                            if (outDatedEvents > 0) {
                                binding.eventsCounterText.text = outDatedEvents.toString()
                                binding.eventsCounterText.background = getDrawable(R.drawable.bg_red_point)
                            } else {
                                binding.eventsCounterText.text = ""
                                binding.eventsCounterText.background =
                                    getDrawable(R.drawable.bg_yelow_point)
                            }
                            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map))
                            binding.eventsCounterText.visibility = View.VISIBLE
                            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )
                        } else {
                            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map_disabled))
                            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this,
                                    R.color.gray_dark
                                )
                            )
                        }
                        binding.eventsCounterText.visibility = View.GONE
            */
            //---------------
        }

        viewModel.isInPanic.observe(this) { isInPanic ->
            /*
             binding.settings.alpha = if (isInPanic) 0.4F else 1.0F
             binding.settingsControl.isEnabled = !isInPanic
             binding.userImage.alpha = if (isInPanic) 0.4F else 1.0F
             binding.userImage.isEnabled = !isInPanic
 */
            // Obtenemos un IBinder para el servicio
            val intent = Intent(this, ServiceLocation::class.java)

            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    val serviceBinder = binder as ServiceLocation.ServiceLocationBinder
                    var locationService = serviceBinder.getService()
                    if (isInPanic) {
                        AppClass.instance.serviceLocationPointer?.setMode(ServiceLocation.Mode.ACTIVE)
                    } else AppClass.instance.serviceLocationPointer?.setMode(ServiceLocation.Mode.PASSIVE)

                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)


        }

        AppClass.instance.panic.observe(this) { inPanic ->
            viewModel.onPanicStateChanged(inPanic)
        }

        viewModel.userSubscription.observe(this) { subscriptionType ->
            AppClass.subscriptionType = subscriptionType!!.access_level
            when (subscriptionType?.access_level) {
                0 -> {
                    //             binding.actionPublish.visibility = GONE
                    //           binding.actionMap.visibility = GONE
                }

                else -> {
                    //         binding.actionPublish.visibility = VISIBLE
                    //       binding.actionMap.visibility = VISIBLE
                }
            }

        }

        viewModel.recordingStatus.observe(this) { status ->

            when (status) {
                RecordingStatusEnum.NONE -> {

                }

                RecordingStatusEnum.RECORDING -> {
                    recordStart()
                }

                RecordingStatusEnum.STOPING -> {
                    recordStop()
                }

                RecordingStatusEnum.DISPOSING -> {
                    disposeRecording()
                }
            }

        }


        viewModel.validationRequestDialog.observe(this) { validationRequest ->
            //   pulseValidatorDialog.show(validationRequest.validationType, validationRequest.eventKey)
            when (validationRequest.validationType) {
                PulseRequestTarget.PULSE_VALIDATION -> TODO()
                PulseRequestTarget.VALIDATE_USER -> TODO()
                PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                    requestStatusConfirmation(PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT,
                        object : PulseValidationCallback {
                            override fun onWrongCode(
                                dialog: PulseValidatorDialog, securityPIN: String
                            ) {
                                super.onWrongCode(dialog, securityPIN)
                                showErrorDialog(
                                    getString(R.string.error_wrong_security_code),
                                    getString(R.string.error_wrong_security_code_message),
                                    getString(R.string.close),
                                    null
                                )
                            }

                            override fun onValidationOK(
                                dialog: PulseValidatorDialog, code: String
                            ) {
                                //     showLoader(getString(R.string.closing_event_wait))
                                viewModel.onCloseEventRequest(
                                    validationRequest.eventKey!!, code
                                )
                            }
                        })

                }

                PulseRequestTarget.ON_FALLING_VALIDATION -> TODO()
                null -> TODO()
            }

        }

        viewModel.onNotificationClicked.observe(this) { resource ->

            when (resource) {
                is Resource.Success -> {
                    var myMap = resource.data
                    var notification: NotificationsUtils.PushNotification =
                        myMap!!["notification"] as NotificationsUtils.PushNotification
                    when (notification.notificationType) {
                        AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE -> {
                            var bundle = Bundle()
                            bundle.putString(
                                "action", AppConstants.NOTIFICATION_ACTION_OPEN_CHAT
                            )
                            bundle.putString("message_id", myMap!!["message_key"].toString())

                            /*
                            switchToModule(
                                IANModulesEnum.EVENTS_TRACKING.ordinal,
                                "events_tracking",
                                true,
                                notification.linkKey.toString(),
                                bundle
                            )
*/
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToMapSituationFragment(
                                    notification.linkKey.toString()
                                )
                            findNavController(R.id.nav_host_fragment).navigate(action)

                        }

                        AppConstants.NOTIFICATION_TYPE_PANIC_BUTTON -> {
                            var event = myMap!!["object"] as Event
                            lifecycleScope.launch {
                                var newNotificationsPopup = NewEventNotificationDialog(
                                    this@MainActivity, this@MainActivity, event
                                )


                                newNotificationsPopup.show()

                            }
                        }
                    }
                }

                is Resource.Error -> {
                    var error = resource.message
                    when (error) {
                        "event_does_not_exist" -> {
                            showErrorDialog(resources.getString(R.string.event_no_longer_exist))
                        }
                    }
                }

                is Resource.Loading -> {}
            }


        }


        viewModel.newEventPopupToShow.observe(this) { resource ->

            when (resource) {
                is Resource.Error -> {
                    var error = resource.message
                    when (error) {
                        "event_does_not_exist" -> {
                            showErrorDialog(resources.getString(R.string.event_no_longer_exist))
                        }
                    }
                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    var myMap = resource?.data
                    var notification = myMap!!["notification"] as EventNotificationModel
                    var event = myMap!!["object"] as Event

                    var newNotificationsPopup = NewEventNotificationDialog(
                        this@MainActivity, this@MainActivity, event
                    )

                    var callback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            super.onComplete(success, result)
                            viewModel.onAgreeToAssist(
                                notification.notification_key, event.event_key
                            )
                        }
                    }

                    newNotificationsPopup.setAcceptButtoCallback(callback)
                    newNotificationsPopup.show()

                }

                null -> {

                }
            }

        }

        AppClass.instance.user.observe(this) { user ->
            if (user != null) {
                viewModel.setUser(user)

                lifecycleScope.launch(Dispatchers.Main) {
                    //binding.userName.text = user.display_name.capitalizeWords()

                    //    appToolbar.updateTitle(user.display_name.capitalizeWords())
                    try {
                        /*
                            Log.d("STORAGEREFERENCE", "va a cargar 1")
                            var storageReference: Any? = null
                            if (user.image.file_name != null) {
                                if (!user.image.file_name!!.startsWith("http")) {

                                    // TODO: Pasarlo a Coroutina

                                    storageReference = FirebaseStorage.getInstance()
                                        .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH).child(
                                            SessionForProfile.getInstance(this@MainActivity).getUserId()
                                        ).child(user.image.file_name.toString())
                                } else {
                                    storageReference = user.image.file_name
                                }

                                try {

                                    Log.d("GLIDEAPP", "2")


                                    GlideApp.with(this@MainActivity).asBitmap().load(storageReference)
                                        .placeholder(getDrawable(R.drawable.progress_animation))
                                        .error(getDrawable(R.drawable.ic_error))
                                        .into(appToolbar.getUserAvatarRef())
                                } catch (exception: Exception) {
                                    showErrorDialog(exception.localizedMessage.toString())
                                }
                            }
                            else
                            {

                                Log.d("GLIDEAPP", "3")


                                GlideApp.with(this@MainActivity).asBitmap().load("null")
                                    .placeholder(getDrawable(R.drawable.progress_animation))
                                    .error(getDrawable(R.drawable.ic_error))
                                    .into(appToolbar.getUserAvatarRef())

                            }
                            Log.d("STORAGEREFERENCE", "--------------------va a cargar 1")
                      */
                    } catch (exception: Exception) {
                        showErrorDialog(exception.localizedMessage.toString())
                    }
                }


            }
        }

        AppClass.instance.subscriptionType.observe(this) { subscriptionType ->
            viewModel.updateUserSubscriptionType(subscriptionType.data!!)
        }

        AppClass.instance.postingPanicButtonStatus.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    //                  showLoader(R.raw.lottie_sos )

                    val rippleLoader =
                        binding.root.findViewById<CircularRippleView>(R.id.circularRippleView)
                    if (rippleLoader != null) {
                        rippleLoader.visibility = VISIBLE
                    }

                }

                is Resource.Error -> {

                    //                hideLoader()


                    val rippleLoader =
                        binding.root.findViewById<CircularRippleView>(R.id.circularRippleView)
                    if (rippleLoader != null) {
                        rippleLoader.visibility = GONE
                    }

                    showErrorDialog(status.message.toString())
                }

                is Resource.Success -> {
                    // hideLoader()

                    val rippleLoader =
                        binding.root.findViewById<CircularRippleView>(R.id.circularRippleView)
                    if (rippleLoader != null) {
                        rippleLoader.visibility = GONE
                    }


                    //Toast.makeText(this, "y ahora que hacemos???", Toast.LENGTH_SHORT).show()
                }
            }
        }



        AppClass.instance.eventsMap.observe(this) { events ->
            Log.d("EVENTS_FOLLOWED", events?.size.toString())
            //    viewModel.updateFollowedEvents(events ?: ArrayList())
            if (events?.size ?: 0 > 0) {
                binding.actionMapIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                binding.eventsCounterText.visibility = VISIBLE
                binding.eventsCounterText.text = events?.size.toString()

            } else {
                binding.eventsCounterText.visibility = GONE
                binding.actionMapIcon.imageTintList = ColorStateList.valueOf(Color.GRAY)
            }
//------------------------
            /*
                        if (AppClass.instance.getEventsCount() > 0) {


                            //    poner esto cuando se actualiza el indicador
                            if (outDatedEvents > 0) {
                                binding.eventsCounterText.text = outDatedEvents.toString()
                                binding.eventsCounterText.background = getDrawable(R.drawable.bg_red_point)
                            } else {
                                binding.eventsCounterText.text = ""
                                binding.eventsCounterText.background =
                                    getDrawable(R.drawable.bg_yelow_point)
                            }
                            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map))
                            binding.eventsCounterText.visibility = VISIBLE
                            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this, R.color.white
                                )
                            )
                        } else {
                            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map_disabled))
                            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this, R.color.gray_dark
                                )
                            )
                        }
            */

            //-------------------------------------------


        }


    }

    private fun showPendingNotificationsPopups(pendingNotifications: ArrayList<NotificationsUtils.PushNotification>?) {
//var newNotificationsPopup = NewEventNotificationDialog(pendingNotifications)


    }

    private fun whenEventCloseSuccessfully(eventKey: String) {
        try {

            updateLocationIntensity()
        } catch (ex: java.lang.Exception) {
            showErrorDialog(ex.localizedMessage.toString())
        }
        hideLoader()
        mMapFragment.resolveOnEventRemoved(eventKey)
        mHomeFragment.updateUI()

        updateUI()

        showAnimatedDialog(
            getString(R.string.closing_event_title), getString(R.string.event_sucessfully_close)
        )
    }


    /**
     * Mapeo las conexiones iniciales.
     *
     */
    private fun initITags() {

    }


    private val tagsRemembered: HashMap<String, ITagImageView> = HashMap<String, ITagImageView>()
    private val tagsAnimations: HashMap<String, Animation> = HashMap<String, Animation>()


    private fun onTagDisconnected(iTag: ITagInterface) {
        val tagsContainer = binding.root.findViewById<LinearLayout>(R.id.tags_miniatures_container)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(24.px, 24.px)
        var tagIcon = tagsRemembered.get(iTag.id())
        if (tagsRemembered.containsKey(iTag.id())) {
            tagIcon = tagsRemembered.get(iTag.id())
            tagIcon?.setImageResource(R.drawable.itag_disabled)
        } else {
            tagIcon = ITagImageView(this@MainActivity)
            tagIcon.tag = iTag
            tagIcon.setImageResource(R.drawable.itag_disabled)
            tagIcon.layoutParams = params
            tagsContainer.addView(tagIcon)
//            tagsContainer.invalidate()
            tagsRemembered.put(iTag.id(), tagIcon)

        }/*
                    val imageId: Int
                    imageId = when (itag.color()) {
                        TagColor.black -> R.drawable.itag_black
                        TagColor.red -> R.drawable.itag_red
                        TagColor.green -> R.drawable.itag_green
                        TagColor.gold -> R.drawable.itag_gold
                        TagColor.blue -> R.drawable.itag_blue
                        else -> R.drawable.itag_white
                    }
                    tagIcon?.setImageResource(imageId)
            */
    }


    private fun onTagConnecting(itag: ITagInterface) {
        val tagsContainer = binding.root.findViewById<LinearLayout>(R.id.tags_miniatures_container)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(24.px, 24.px)
        var tagIcon = tagsRemembered.get(itag.id())
        if (tagsRemembered.containsKey(itag.id())) {

            lifecycleScope.launch(Dispatchers.Main) {
                val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.bounce)
                animation.repeatCount = 3
                tagIcon?.startAnimation(animation)
            }/*
                      if (tagsAnimations.containsKey(itag.id())) {
                          var animation: Animation? = tagsAnimations.get(itag.id())

                          tagsAnimations.remove(itag.id())
                      }
          */
        } else {
            tagIcon = ITagImageView(this@MainActivity)
            tagIcon.tag = itag
            tagIcon.setImageResource(R.drawable.itag_red)
            tagIcon.layoutParams = params
            tagsContainer.addView(tagIcon)
            tagsContainer.invalidate()
            tagsRemembered.put(itag.id(), tagIcon)
        }

        val imageId: Int
        imageId = when (itag.color()) {
            TagColor.black -> R.drawable.itag_black
            TagColor.red -> R.drawable.itag_red
            TagColor.green -> R.drawable.itag_green
            TagColor.gold -> R.drawable.itag_gold
            TagColor.blue -> R.drawable.itag_blue
            else -> R.drawable.itag_white
        }
        tagIcon?.setImageResource(imageId)
        SpringAnimator(tagIcon!!).start()

    }


    private fun onTagConnected(itag: ITagInterface) {

        val tagsContainer = binding.root.findViewById<LinearLayout>(R.id.tags_miniatures_container)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(24.px, 24.px)
        var tagIcon = tagsRemembered.get(itag.id())
        if (tagsRemembered.containsKey(itag.id())) {
            if (tagsAnimations.containsKey(itag.id())) {
                var animation: Animation? = tagsAnimations.get(itag.id())
                animation?.cancel()
                tagsAnimations.remove(itag.id())
            }
        } else {
            tagIcon = ITagImageView(this@MainActivity)
            tagIcon.tag = itag
            tagIcon.setImageResource(R.drawable.itag_red)
            tagIcon.layoutParams = params
            tagsContainer.addView(tagIcon)
            tagsContainer.invalidate()
            tagsRemembered.put(itag.id(), tagIcon)
        }

        val imageId: Int
        imageId = when (itag.color()) {
            TagColor.black -> R.drawable.itag_black
            TagColor.red -> R.drawable.itag_red
            TagColor.green -> R.drawable.itag_green
            TagColor.gold -> R.drawable.itag_gold
            TagColor.blue -> R.drawable.itag_blue
            else -> R.drawable.itag_white
        }
        tagIcon?.setImageResource(imageId)

    }

    /**
     * TODO
     * Monitorea el estado de las Tags que estan registradas
     *
     */
    private fun updateTagsStatus() {

        val tagsContainer = binding.root.findViewById<View>(R.id.tags_miniatures_container)
        // tagsContainer.removeAllViews()

        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(24.px, 24.px)

//----------------
        ITag.getDisposablesConnections()
        //disposableBag.add(

        for (index in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(index)
            val connection = ble.connectionById(itag.id())
            //    if (connection.isConnected)
            //      disposableBag.add(

            when (connection.state()) {
                BLEConnectionState.disconnected -> {
                    onTagDisconnected(itag)
                }

                BLEConnectionState.connecting -> {
                    onTagConnecting(itag)
                }

                BLEConnectionState.connected -> {
                    onTagConnected(itag)
                    connection.observableState().subscribe { state: BLEConnectionState? ->
                        when (state) {
                            BLEConnectionState.disconnected -> {
                                onTagDisconnected(itag)
                            }

                            BLEConnectionState.connecting -> onTagConnecting(itag)
                            BLEConnectionState.connected -> {
                                onTagConnected(itag)
                            }

                            BLEConnectionState.disconnecting -> TODO()
                            BLEConnectionState.writting -> TODO()
                            BLEConnectionState.reading -> TODO()
                            else -> {}
                        }

                    }


                }

                BLEConnectionState.disconnecting -> {
                    TODO()
                }

                BLEConnectionState.writting -> {
                    TODO()
                }

                BLEConnectionState.reading -> {
                    TODO()
                }
            }/*
            */
            //)

            //  connection.connect()


        }


        // }

        /*
            ITag.store.observable().subscribe
            {
                event ->
                when (event.op) {
                    StoreOpType.change -> {
                        Toast.makeText(this, "Change", Toast.LENGTH_LONG).show()
                    }//setupContent()
                    StoreOpType.forget -> {
                        Toast.makeText(this, "Forget", Toast.LENGTH_LONG).show()
                    }

                    StoreOpType.remember -> {
                        Toast.makeText(this, "Remembered", Toast.LENGTH_LONG).show()
                    }
                }

                for (index in 0 until ITag.store.count()) {
                    val itag = ITag.store.byPos(index)
                    val connection = ITag.ble.connectionById(itag.id())

                    if (itag != null) {
                        var tagIcon: ITagImageView?
                        disposableBag.add(
                            connection.observableState().subscribe { state: BLEConnectionState? ->

                                    if (ITag.ble.state() == BLEState.OK) {
                                        binding.root.findViewById<View>(R.id.tags_miniatures_container).visibility = View.VISIBLE

                                        when (state) {
                                            BLEConnectionState.connected -> {


                                                onTagConnected(itag)

                                            }

                                            BLEConnectionState.connecting, BLEConnectionState.disconnecting -> {
                                                if (tagsRemembered.containsKey(itag.id())) {
                                                    tagIcon = tagsRemembered.get(itag.id())
                                                } else {
                                                    tagIcon = ITagImageView(this@MainActivity)
                                                    tagIcon?.setTag(itag)
                                                    tagIcon?.setImageResource(R.drawable.itag_red)
                                                    tagIcon?.layoutParams = params
                                                    tagsContainer.addView(
                                                        tagIcon
                                                    )
                                                    tagsRemembered.put(itag.id(), tagIcon!!)
                                                    val animation: Animation = AlphaAnimation(1f, 0f)
                                                    animation.duration = 1000
                                                    animation.interpolator = LinearInterpolator()
                                                    animation.repeatCount = Animation.INFINITE
                                                    animation.repeatMode = Animation.REVERSE
                                                    tagsAnimations.put(itag.id(), animation)
                                                    tagIcon?.startAnimation(animation)
                                                }

                                                val imageId: Int
                                                imageId = when (itag.color()) {
                                                    TagColor.black -> R.drawable.itag_black
                                                    TagColor.red -> R.drawable.itag_red
                                                    TagColor.green -> R.drawable.itag_green
                                                    TagColor.gold -> R.drawable.itag_gold
                                                    TagColor.blue -> R.drawable.itag_blue
                                                    else -> R.drawable.itag_white
                                                }
                                                tagIcon?.setImageResource(imageId)
                                            }

                                            BLEConnectionState.writting, BLEConnectionState.reading -> {/*
                                                statusDrawableId = R.drawable.bt_call
                                                statusTextId = R.string.bt_call

                                                 */
                                            }

                                            BLEConnectionState.disconnected -> {
                                                // aca remover el tagb
                                                val imageId: Int/*
                                                val imageId: Int
                                                imageId = when (itag.color()) {
                                                    TagColor.black -> R.drawable.itag_black
                                                    TagColor.red -> R.drawable.itag_red
                                                    TagColor.green -> R.drawable.itag_green
                                                    TagColor.gold -> R.drawable.itag_gold
                                                    TagColor.blue -> R.drawable.itag_blue
                                                    else -> R.drawable.itag_white
                                                }

                                                var bitmap: Bitmap =
                                                    BitmapFactory.decodeResource(getResources(), imageId)
                                                tagIcon?.setImageBitmap(toGrayscale(bitmap))

                                                 */
                                            }

                                            else -> {
                                                val pp = 3/*
                                                statusDrawableId = R.drawable.bt_disabled
                                                statusTextId = R.string.bt_disabled

                                                 */
                                            }
                                        }
                                    } else {/*
                                        statusDrawableId = R.drawable.bt_disabled
                                        statusTextId = R.string.bt_disabled

                                         */
                                        binding.root.findViewById<View>(R.id.tags_miniatures_container).visibility = View.GONE
                                    }

                                })


                        disposableBag.add(ITag.store.observable().subscribe { event: StoreOp? ->
                            var ppp = 33
                        })
                        ITag.connectAsync(connection)
                        // connection.connect()
                        //     ITag.enableReconnect(itag.id())
                        //             }
                    }
                }


            }
        */
    }

    fun requestPermissions() {
        multiplePermissionsRequest.launch(
            arrayOf(
                ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION, SEND_SMS
            )
        )
    }


    private fun intentHandler(intent: Intent) {
        when (intent.action) {
            "android.intent.action.MAIN" -> {
                //       Toast.makeText(this, intent.action, Toast.LENGTH_LONG).show()

            }

            "ACTION_SHOW_EXTEND_TIME_DIALOG" -> {
                //         checkIfLogged()
                //
                //
                //
                //
                //
                //
                //
                //     Toast.makeText(this, intent.action, Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroy() {
        AppClass.instance.isKilling = true
        super.onDestroy()
        if (UserViewModel.getInstance().getUser()?.user_key != null) {
            UserViewModel.getInstance().offLine()
        }
        keyboardHeightProvider?.close()
    }

    private fun updateLocationIntensity() {

    }


    private fun registerActivityResultContracts() {

        imagePickerStartForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val intent = result.data!!

                    when (AppClass.instance.resultTarget()) {
                        ActivityResultsTarget.MAIN_FRAGMENT -> {

                        }

                        ActivityResultsTarget.MAP_FRAGMENT -> {
                            //          mMapFragment.chatFragment?.onImageSelected(intent)
                        }

                        else -> {
                            throw (java.lang.Exception("Target desconocido"))
                        }
                    }
                }
            }

        videoRecorderStartForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data!!
                    when (AppClass.instance.resultTarget()) {
                        ActivityResultsTarget.MAIN_FRAGMENT -> {

                        }

                        ActivityResultsTarget.MAP_FRAGMENT -> {
                            //    mMapFragment.chatFragment?.onVideoRecorded(intent)
                        }

                        else -> {
                            throw (java.lang.Exception("Target desconocido"))
                        }
                    }

                    /*
                            // Agrego Mensaje Local-----------------------
                            val message = chatFragment?.prepareMessage()!!
                            val finalPath =
                                FacebookSdk.getApplicationContext().getDir(
                                    "cache",
                                    Context.MODE_PRIVATE
                                ).toString()
                            FileUtils.copyFile(
                                filePath.substringBeforeLast("/"),
                                FileUtils.getJustFileName(filePath),
                                finalPath
                            )
                            val image = com.iyr.fewtouchs.utils.chat.models.Message.Image(FileUtils.getJustFileName(filePath))
                            message?.setImage(image)

                            chatFragment?.addMessageToStart(message)
                            Log.d("MEDIA_MESSAGE_SEND", Gson().toJson(message))

                            //---------------------

                            mainActivity.getPresenter()
                                .messageFileSend(
                                    currentEvent?.event_key.toString(),
                                    message.id,
                                    mediaFile
                                )
            */

                }
            }

    }


    private fun handleIntentExtras() {

        when (intent.action) {
            "android.intent.action.MAIN" -> {

            }

            "ACTION_SHOW_EXTEND_TIME_DIALOG" -> {
                //         checkIfLogged()
                var eventKey: String? = null
                intent.extras?.let { _extras ->
                    eventKey = _extras.getString("event_key")
                }
                if (!eventKey.isNullOrEmpty()) {
                    if (!arePopupsVisibles()) {
                        intent.extras?.let { _extras ->
                            eventKey = _extras.getString("event_key").toString()
                            eventKey?.also {
                                showIsCloseToExpireDialog(
                                    eventKey!!, intent.extras, onWantToExtendCallback
                                )
                            }
                        }
                    } else {
                        if (!EventCloseToExpire.getInstance().isDialogShowing) {
                            addToPopupQueue(
                                showIsCloseToExpireDialog(
                                    eventKey!!, intent.extras, onWantToExtendCallback
                                )
                            )
                        }
                    }
                }

            }
        }


    }

    private fun addToPopupQueue(newTask: Unit) {
        popupsQueue.add(newTask)
        newTask.run { }
    }

    private fun checkAndExecuteDialogsTask() {
        //checks if there is any task in the list and is there any other         running task
        if (popupsQueue.size > 0 && !arePopupsVisibles()) {
            showPopupsAsync(popupsQueue[0]).execute()
        }
    }

    private fun showPopupsAsync(task: Unit) = object : AsyncTask<Unit, Any, Any>() {

        var currentTask: Unit? = null

        override fun doInBackground(vararg task: Unit?): Any? {
            currentTask = task[0]
            currentTask
            return null
        }

        override fun onPostExecute(result: Any?) {
            super.onPostExecute(result)
            popupsQueue.remove(currentTask)

            checkAndExecuteDialogsTask()
        }
    }


    fun showBlockedLayout() {
        if (!isBlockedLayoutVisible()) {
            blockedLayoutDialog.show(
                supportFragmentManager, "blocked_layout"
            )

        }
    }

    fun hideBlockedLayout() {
        if (isBlockedLayoutVisible()) {
            blockedLayoutDialog.dismiss()
        }
    }


    fun Activity.isBlockedLayoutVisible(): Boolean {
        return supportFragmentManager.findFragmentByTag("blocked_layout") != null
    }


    private fun showEventCloseToExpireDialog(eventKey: String, extras: Bundle?) {
        EventCloseToExpire.getInstance().show(this, eventKey, extras, null)
    }

    private fun arePopupsVisibles(): Boolean {
        return PulseValidation.getInstance().isDialogShowing || EventCloseToExpire.getInstance().isDialogShowing
    }


    override fun onStart() {
        super.onStart()/*
            binding.chatIndicator.visibility = GONE
            binding.bellControl.visibility = GONE
            binding.itagsIndicator.visibility = GONE
            */
        binding.root.findViewById<View>(R.id.tags_miniatures_container).visibility = GONE

    }

    override fun onResume() {

        super.onResume()/*
                if (AppClass.instance.logged) {



                    showFooterToolBar()

                    (application as AppClass).alreadyStarted = true
                    keyboardHeightProvider?.setKeyboardHeightObserver(this)

                    if (!hasPermissions("android.permission.SEND_SMS")) {
                        requestSMSPermissions()
                    }



                    if (!isGooglePlayInstalled()) {
                        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
                    }
                    this.isActivityVisible = true

                    updateTagsStatus()
                    //  onResumeITagService()
                    var locationPermissionGranted: Boolean? = SessionForProfile.getInstance(this)
                        .getProfileProperty("LocationPermissionsGranted") as Boolean?
                    if (locationPermissionGranted == null) {
                        if (!areLocationPermissionsGranted(true)) {
                            requestPermissions()
                        }
                    } else if (locationPermissionGranted) {
                        onLocationPermissionsGranted()
                    } else {
                        onLocationPermissionsRejected()
                    }

                    if (areLocationPermissionsGranted(true)) {
                        viewModel.setLocationIsAvailable()
                    } else {
                        viewModel.setLocationNotAvailable()
                    }


                    handleDynamicLinks()


                }
                else
                {*/


        if (UserViewModel.getInstance().getUser()?.user_key != null) {
            UserViewModel.getInstance().onLine()
            handleDynamicLinks()
        }
        //      }
    }


    private fun onSMSPermissionsGranted() {

    }

    private fun onSMSPermissionsRejected() {
    }

    val smsPermissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            //    arrayOf("android.permission.SEND_SMS"), MY_PERMISSION_REQUEST_SEND_SMS
            if (!hasPermissions("android.permission.SEND_SMS")) {
                requestPermissions(
                    arrayOf("android.permission.SEND_SMS"), MY_PERMISSION_REQUEST_SEND_SMS
                )
            } else {/*
                  onSMSPermissionsGranted()

  //                onSMSPermissionsRejected()
                  showErrorDialog(
                      getString(R.string.warning),
                      getString(R.string.permission_for_sms_mandatory),
                      getString(R.string.close),
                      null
                  )

                 */
            }
        }

    private fun requestSMSPermissions() {
        smsPermissionsRequest.launch(arrayOf(SEND_SMS))

    }

    private fun onResumeITagService() {

        ErrorsObservable.addErrorListener(mErrorListener)
        MainActivity.sIsShown = true
        //  setupContent()
        //  Waytoday.gpsLocationUpdater.addOnPermissionListener(gpsPermissionListener)
        disposableBag.add(ble.observableState().subscribe { event ->
            var pepe = 3
            //    setupContent()

        })

        disposableBag.add(ble.scanner().observableActive().subscribe { event ->
            if (BuildConfig.DEBUG) {
                Log.d(
                    "Tags",
                    "ble.scanner activeEvent=$event isScanning=" + ble.scanner().isScanning + " thread=" + Thread.currentThread().name
                )
            }
//            setupContent()
            //           setupProgressBar()
        })/*

              disposableBag.add(ble.scanner().observableTimer().subscribe { event -> setupProgressBar() })
            */

        disposableBag.add(ITag.store.observable().subscribe { event ->
            when (event.op) {

                StoreOpType.remember -> {
                    TODO()
                }

                StoreOpType.forget -> {
                    TODO()
                }

                StoreOpType.change -> {
                    TODO()
                }
            }
        })
        bindService(ITagsService.intentBind(this), mITagServiceConnection, 0)/*
                    if (Waytoday.tracker.isOn(this) && PowerManagement.needRequestIgnoreOptimization(this)) {
                        if (resumeCount++ > 1) {
                            Handler(mainLooper).post { PowerManagement.requestIgnoreOptimization(this) }
                        }
                    }
            */
    }


    override fun onPause() {
        //       onPauseItagService()
        super.onPause()
        (application as AppClass).alreadyStarted = true

        keyboardHeightProvider?.setKeyboardHeightObserver(null)
        this.isActivityVisible = false
        if (videoDialog?.isShowing == true) {
            videoDialog?.dismiss()
        }
        if (UserViewModel.getInstance().getUser()?.user_key != null) {
            UserViewModel.getInstance().offLine()
        }
        //  unregisterObservers()

    }


    private fun onPauseItagService() {
        try {
            unbindService(mITagServiceConnection)
        } catch (e: IllegalArgumentException) {
            // ignore
        }
        disposableBag.dispose()
        MainActivity.sIsShown = false
        if (ITag.store.isDisconnectAlert) {
            ITagsService.start(this)
        } else {
            ITagsService.stop(this)
        }
        ErrorsObservable.removeErrorListener(mErrorListener)
    }


    private fun getIntentData() {
// Aca no se porque

        Log.d("PUSH_MESSAGE_MAINSCREEN", "entro por MainActivity")
        intent.extras?.let { extras ->
            Log.d("PUSH_MESSAGE_MAINSCREEN", "entro por MainActivity con extras")

            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(
                    "PUSH_MESSAGE_MAINSCREEN", "informacion recibida =" + String.format(
                        "%s %s (%s)", key, value?.toString(), value?.javaClass?.name
                    )
                )
            }
        }


    }

    override fun onNoEventsToShow() {
        //  updateMapButton()
    }


    fun updateMapButton() {
        var outDatedEvents = 0
//        val eventsCount = eventsFollowedArray.size
        val eventsCount = viewModel.getEventsCount()
        //      showSnackBar(binding.root, "Arreglar esto")

        if (AppClass.instance.getEventsCount() > 0) {


            //    poner esto cuando se actualiza el indicador
            if (outDatedEvents > 0) {
                binding.eventsCounterText.text = outDatedEvents.toString()
                binding.eventsCounterText.background = getDrawable(R.drawable.bg_red_point)
            } else {
                binding.eventsCounterText.text = ""
                binding.eventsCounterText.background = getDrawable(R.drawable.bg_yelow_point)
            }
            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map))
            binding.eventsCounterText.visibility = VISIBLE
            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this, R.color.white
                )
            )
        } else {
            binding.actionMapIcon.setImageDrawable(getDrawable(R.drawable.map_disabled))
            binding.actionMapIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this, R.color.gray_dark
                )
            )
        }
        //    binding.eventsCounterText.visibility = GONE

    }


    /***
     *  Configura la UI cuando la App esta lista para ser utilizada.
     */
    @SuppressLint("MissingPermission")
    private fun setupUIWhenReady() {

        prepareBeforeStart()

        appToolbar.setBellOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                if (viewModel.notificationsList.size > 0) {
                    handleTouch()
                    findNavController(R.id.nav_host_fragment).navigate(R.id.notificationsFragment)
                }
            }
        })

        appToolbar.setSettingsOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                handleTouch()
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
            }
        })

        appToolbar.hideTagsMiniatures()

        binding.actionHome.setOnClickListener(View.OnClickListener {
            handleTouch()
            goHome()
        })

        binding.actionPublish.setOnClickListener(View.OnClickListener {
            handleTouch()
            requestLocationRequirements(object : LocationRequirementsCallback {
                override fun onRequirementsComplete() {
                    navController.navigate(R.id.eventTypeSelectorFragment)
                }
            })
        })

        binding.actionMap.setOnClickListener(View.OnClickListener {

            if (!viewModel.isFreeUser()) {

                if (viewModel.getEventsCount() > 0) {
                    handleTouch()
                    requestLocationRequirements(object : LocationRequirementsCallback {
                        override fun onRequirementsComplete() {
                            //getLastKnownLocation { location ->
                            if (AppClass.instance.lastLocation != null) {
                                var eventKey =
                                    MapSituationFragmentViewModel.getInstance().auxEventKey.value
                                if (eventKey == null) {
                                    eventKey = viewModel.getFirstEvent()?.event_key
                                }
                                if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.mapSituationFragment) {
                                    val action =
                                        HomeFragmentDirections.actionHomeFragmentToMapSituationFragment(
                                            eventKey
                                        )
                                    findNavController(R.id.nav_host_fragment).navigate(action)
                                }
                            }

                        }
                    })
                }

            }
            else
            {
                var bundle = Bundle()
                bundle.putString("go_to", SettingsFragmentsEnum.PLAN_SETTINGS.name)
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment, bundle)
            }
        })

        binding.actionFriends.setOnClickListener(View.OnClickListener {
            handleTouch()
            val action = HomeFragmentDirections.actionHomeFragmentToContactsFragment()
            findNavController(R.id.nav_host_fragment).navigate(action)
        })

        appToolbar.setSettingsOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                handleTouch()
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
            }
        })

    }

    fun goHome() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.homeFragment)
    }

    private fun prepareBeforeStart() {
        getNotificationsToken()
    }

    private fun getNotificationsToken() {
//        GlobalScope.launch(Dispatchers.IO) {
        lifecycleScope.launch(Dispatchers.IO) {

            var token = FirebaseMessaging.getInstance().token.await()
// show token in toast including Looper.prepare() and Looper.loop()
            Looper.prepare()
            Toast.makeText(this@MainActivity, token, Toast.LENGTH_LONG).show()

            var notificationsRepository = NotificationsRepositoryImpl()
            notificationsRepository.registerNotificationsToken(token)

        }
    }


    override fun onBackPressed() {
        if (BuildConfig.NAVIGATION_HOST_MODE?.toBoolean() == true) {
            if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.homeFragment) {
                if (!navController.popBackStack()) {
                    super.onBackPressed()
                }
            }
        } else {
            handleGoBack()
        }
    }

    fun getVisibleFragment(fragmentManager: FragmentManager): Fragment? {
        //     val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible) return fragment
            }
        }
        return null
    }


    suspend fun getVisibleFragmentSusp(fragmentManager: FragmentManager): Fragment? {
        //     val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible) return fragment
            }
        }
        return null
    }

    fun getVisibleFragmentClassName(fragmentManager: FragmentManager): String {
        //     val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible) return fragment.javaClass.name.substringAfterLast(
                    "."
                )
            }
        }
        return ""
    }


    override fun updateUI() {
        //   val me = SessionForProfile.getInstance(this).getUserProfile()
        //binding.userName.text = me.display_name
        // appToolbar.updateTitle(me.display_name)
        /*
                try {


                    Log.d("STORAGEREFERENCE", "va a cargar 2")

                    var storageReference: Any? = null
                    if (me.image.file_name != null) {

                        // TODO: Pasarlo a Coroutina

                        if (!me.image.file_name!!.startsWith("http")) {
                            storageReference = FirebaseStorage.getInstance()
                                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                                .child(SessionForProfile.getInstance(this).getUserId())
                                .child(me.image.file_name.toString())
                        } else {
                            storageReference = me.image.file_name
                        }
                        try {
                            Log.d("GLIDEAPP", "4")
                            GlideApp.with(this).asBitmap().load(storageReference)
                                .placeholder(getDrawable(R.drawable.progress_animation))
                                .error(getDrawable(R.drawable.ic_error)).into(appToolbar.getUserAvatarRef())

                        } catch (exception: Exception) {
                            Log.d("GLIDEAPP", "5")
                            showErrorDialog(exception.message.toString())
                        }
                    } else {
                        GlideApp.with(this).asBitmap().load("null")
                            .placeholder(getDrawable(R.drawable.progress_animation))
                            .error(getDrawable(R.drawable.ic_error)).into(appToolbar.getUserAvatarRef())

                    }
                } catch (exception: Exception) {
                    var pp = 33
                }
        */

        //   pagerAdapter.getFragmentAt(1).updateUI()
//        binding.bell.isVisible = mNotificationsFragment.getData().size > 0
        //updateBellVisibilityStatus()
        //    mEventsFragment.updateUI()
        //       updateMapButton()
        //   binding.actionHome.visibility = VISIBLE

        when (currentModuleIndex) {
            IANModulesEnum.MAIN.ordinal -> {
                binding.actionHome.visibility = GONE
            }
        }

    }

    private fun handleOnLocationServicesStatus() {/*
                    handleLocationAvailability()
              */
        handleFallinDetector()

    }


    private fun handleFallinDetector() {
        if (SessionForProfile.getInstance(this.applicationContext)
                .getProfileProperty("falling_sensor", false) as Boolean
        ) {

            if (isGPSEnabled()) {
                if (!AppClass.instance.isEmulator()) {

                    if (isServiceRunning(FallDetectionServiceMethod::class.java) == false) {
                        Toast.makeText(
                            this, "Safe riding! We track you for safety", Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, FallDetectionServiceMethod::class.java)
                        startService(intent)
                    }


                }
            } else {
                Toast.makeText(
                    this, "Show Falling detector not working", Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (isServiceRunning(FallDetectionServiceMethod::class.java)) {
                val intent = Intent(this, FallDetectionServiceMethod::class.java)
                stopService(intent)
            }
        }
    }

    /*
        override fun onError(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onImagesChosen(images: MutableList<ChosenImage>?) {
            TODO("Not yet implemented")
        }
    */
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {

        //      runOnUiThread {
        /*
                    if (multiPickerWrapper!!.onActivityResult(requestCode, resultCode, data)) {

                        when (requestCode) {
                            MY_PERMISSION_REQUEST_SEND_SMS -> {
                                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                                    broadcastMessage(null, BROADCAST_PERMISSION_SEND_SMS_GRANTED)
                                } else {
                                    broadcastMessage(null, BROADCAST_PERMISSION_SEND_SMS_DENIED)
                                }

                            }
                        }
                        return@runOnUiThread
                    }
        */
        super.onActivityResult(requestCode, resultCode, data)

        this.pendingFromActivityResult = true

        if (resultCode == RESULT_OK) {

            when (currentModuleIndex) {
                IANModulesEnum.MAIN.ordinal -> {/*
                              if (requestCode == com.kbeanie.multipicker.core.VideoPickerImpl.) {
                                  val mPaths: ArrayList<String>? =
                                      data?.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH)
                              } else*/

                    /*
                       ARREGLAR ESTO

                    if (requestCode === Picker.PICK_VIDEO_DEVICE) {

                    } else if (requestCode === Picker.PICK_IMAGE_CAMERA) {
                        //if (requestCode === com.github.dhaval2404.imagepicker.ImagePicker.REQUEST_CODE) {
                        val filePath = Uri.parse(data?.data?.encodedPath).toString()
                        uploadMediaToPanicEvent(MediaTypesEnum.IMAGE, filePath)
                    } else
                        */

                    if (requestCode === REQUEST_CODE_RECOVER_PLAY_SERVICES) {


                    } else if (resultCode === RESULT_CANCELED) {

                        if (requestCode === REQUEST_CODE_RECOVER_PLAY_SERVICES) {

                            Toast.makeText(
                                this,
                                "Google Play Services must be installed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }

                }

                IANModulesEnum.POST_EVENTS.ordinal -> {
                    if (mEventsFragment.getMediaFragment().isVisible) {
                        mEventsFragment.getMediaFragment()
                            .onActivityResult(requestCode, resultCode, data)
                    }/*
                        else
                            if (mEventsFragment.getLocationManualInputFragment()?.isVisible == true) {
                                mEventsFragment.getLocationManualInputFragment()
                                    ?.onActivityResult(requestCode, resultCode, data)
                            }

                         */
                }

                IANModulesEnum.EVENTS_TRACKING.ordinal -> {
                    mMapFragment.onActivityResult(requestCode, resultCode, data)
                }

            }
        }

//        }


    }


    /*
        private fun getFragmentVisible(): Int {
            var index = 0
            for (position in 0..pagerAdapter.count) {
                if (pagerAdapter.getFragmentAt(position).isVisible) {
                    return index
                }
                index++
            }
            return -1
        }
    */

    fun uploadMediaToPanicEvent(mediaType: MediaTypesEnum, filePath: String) {

        val eventKey = AppClass.instance.getPanicEventKey()
        if (eventKey != null) {
            var event: EventFollowed? = null

            getEventsFollowed().forEach { record ->
                if (record.author.author_key == FirebaseAuth.getInstance().uid.toString() && record.event_type == EventTypesEnum.PANIC_BUTTON.name) {
                    event = record
                    return@forEach
                }
            }


            event?.let { thisEvent ->

                val mediaFile = MediaFile()
                mediaFile.media_type = mediaType
                mediaFile.file_name = filePath
                mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                mediaFile.time = Date().time

                if (mediaFile.media_type == MediaTypesEnum.VIDEO) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(
                        this, Uri.parse(mediaFile.file_name.toString())
                    )
                    val time =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val timeInMillisec: Int = time.toString().toInt()
                    mediaFile.duration = timeInMillisec
                }

                val messageKey = StringUtils.generateRandomString()
                showSnackBar(binding.root, "Implementar en el ViewModel messageFileSend")
                //   mPresenter.messageFileSend(eventKey, messageKey, mediaFile)

            }
            // var event = getActiveEvents().
        }


    }


    @SuppressLint("MissingPermission", "ServiceCast")
    fun onEmergencyButtonPressed() {/*
                    mHomeFragment.getRedButtonRef().isEnabled = false
                    val vibratePattern = longArrayOf(0, 200, 100, 300, 100, 2000)
                    val amplitudes = intArrayOf(0, 10, 80, 255, 0, 255)
                    vibrateWithPattern(vibratePattern, amplitudes)
            */
        dispatchPanicEvent()

    }

    fun onEmergencyCancelButtonPressed() {
        //      deactivatePanicEvent()
    }/*
        fun deactivatePanicEvent() {
            requestStatusConfirmation(PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT,
                object : PulseValidationCallback {

                    override fun onWrongCode(
                        dialog: PulseValidatorDialog, securityPIN: String
                    ) {
                        super.onWrongCode(dialog, securityPIN)
                        showErrorDialog(
                            getString(R.string.error_wrong_security_code),
                            getString(R.string.error_wrong_security_code_message),
                            getString(R.string.close),
                            null
                        )
                    }

                    override fun onValidationOK(dialog: PulseValidatorDialog, code: String) {
                        showLoader(getString(R.string.closing_event_wait))
                        viewModel.onCloseEventClicked(
                            AppClass.instance.getPanicEventKey()!!,
                            code
                        )
                    }


                })
        }
    */

    /*
        private fun publishEvent(event: Event,listKey: String) {
            val event = eventsFragmentViewModel.event.value!!
            lifecycleScope.launch(Dispatchers.IO) {
                SmartLocation.with(context).location().oneFix().start { location ->
                    if (location != null) {

                        val latLng = LatLng(location.latitude, location.longitude)
                        val geoHash = GeoHash(latLng.latitude, location.longitude)

                        val geoLocationAtCreation = GeoLocation()
                        geoLocationAtCreation.l = ArrayList()
                        (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.latitude)
                        (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.longitude)

                        geoLocationAtCreation.g = geoHash.geoHashString
                        geoLocationAtCreation.event_time = Date().time

                        //   geoLocationAtCreation.event_time = thisEvent.time
                        event.location_at_creation = geoLocationAtCreation

                        if (eventLocationType.compareTo(EventLocationType.REALTIME.name) == 0) {
                            event.location = EventLocation().apply {
                                latitude = latLng.latitude
                                longitude = latLng.longitude
                            }
                        }
                        eventsFragmentViewModel.publishEvent(event)
                    }
                }
            }

        }
    */

    /*
        fun publishEvent(event: Event) {
            _postingEventStatus.postValue(Resource.Loading())
            viewModelScope.launch(Dispatchers.IO)
            {

                event.media?.forEach { media ->
                    if (media.media_type == MediaTypesEnum.VIDEO ||
                        media.media_type == MediaTypesEnum.AUDIO ||
                        media.media_type == MediaTypesEnum.IMAGE
                    ) {
                        val fileExtension = media.file_name.getFileExtension(con)
                        var fileUri = media.file_name
                        if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                            fileExtension?.lowercase(Locale.getDefault()) == "png"
                        ) {
                            fileUri = "file:" + media.file_name
                        }
                        var mediaFileEncoded: String? = null
                        if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                            fileExtension?.lowercase(Locale.getDefault()) == "png" ||
                            fileExtension?.lowercase(Locale.getDefault()) == "mp4" ||
                            fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                        ) {

                            mediaFileEncoded =
                                MultimediaUtils(AppClass.instance).convertFileToBase64(Uri.parse(fileUri))
                                    .toString()
                        }
                        media.bytesB64 = mediaFileEncoded
                    }
                }


                var call = eventsRepository.postEvent(event)
                if (call.data != null) {
                    _postingEventStatus.postValue(Resource.Success<Event?>(call.data))

                } else
                    _postingEventStatus.postValue(Resource.Error<Event?>(call.message.toString()))


                /*
                            EventsWSClient.instance.postEvent(event, object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    if (success) {
                                        if (result is Event) {
                                            callback.onPublishEventDone(result)
                                            iCallback?.onComplete(true, result)
                                        }
                                    }
                                }

                                override fun onError(exception: java.lang.Exception) {
                                    callback.onError(exception)
                                    iCallback?.onError(exception)
                                }
                            })
                  */
            }

        }

    */


    private fun dispatchPanicEvent() {
        lifecycleScope.launch(Dispatchers.IO) {
            mHomeFragment.onEmergencyButtonPressed()
        }
    }


    private fun prepareEventAfterLocationFetch(latLng: LatLng) {
        val newEvent = Event()
        newEvent.event_type = EventTypesEnum.PANIC_BUTTON.name
        newEvent.event_location_type = EventLocationType.REALTIME.name
        newEvent.status = EventStatusEnum.DANGER.name

        showSnackBar(binding.root, "Implementar el metodo onEmergencyButtonPressed ")
//        mPresenter.onEmergencyButtonPressed(newEvent)

    }

    private fun updateBellVisibilityStatus(_visible: Boolean) {
        /*
                if (_visible) binding.bellControl.visibility = VISIBLE
                     else binding.bellControl.visibility = GONE
             */

    }

    private fun updateNotificationsCounter() {

    }


    fun setTitleBarTitle(titleResId: Int) {
        setTitleBarTitle(this.getText(titleResId).toString())
    }

    fun setTitleBarTitle(title: String) {

        appToolbar.updateTitle(title)

//        binding.titleText.text = title

    }

    /*
        fun switchToModule(index: Int, tag: String?) {
            this.switchToModule(index, tag, false, null)
        }

        fun switchToModule(index: Int, tag: String?, key: String) {
            this.switchToModule(index, tag, false, key)
        }
    *//*
        public fun switchToModule(index: Int, tag: String?, force: Boolean, key: String?) {
            if (currentModuleIndex != index || force) {
                if (currentModuleIndex != -1)
                    unSubscribeInModule(currentModuleIndex)

                handleTouch()

                //     binding.pager.setCurrentItem(index, true)
                subscribeInModule(index)
                when (index) {
                    0 -> {
                        binding.titleText.visibility = View.GONE
                        binding.userNameSection.visibility = View.VISIBLE
                        binding.titleText.setText(R.string.app_long_title)
                        showactionsSections(null)
                    }
                    1 -> {
                        binding.titleText.visibility = View.VISIBLE
                        binding.userNameSection.visibility = View.GONE
                        binding.titleText.setText(R.string.select_your_event)
                        hideactionsSections()
                    }
                    2 -> {
                        binding.titleText.visibility = View.VISIBLE
                        binding.userNameSection.visibility = View.GONE
                        binding.titleText.setText(R.string.action_events_map)
                        key?.let {
                            (pagerAdapter.getFragmentAt(index) as MapSituationFragment).setEventKey(
                                it
                            )
                        }
                        hideactionsSections()
                    }
                    3 -> {
                        binding.titleText.visibility = View.VISIBLE
                        binding.userNameSection.visibility = View.GONE
                        binding.titleText.setText(R.string.action_friends_list)
                        hideactionsSections()
                    }
                    4 -> {
                        binding.titleText.visibility = View.VISIBLE
                        binding.userNameSection.visibility = View.GONE
                        binding.titleText.setText(R.string.action_settings)

                        val fragment = pagerAdapter.getFragmentAt(4) as SettingsFragment

                        fragment.selectFragment(0)

                        hideactionsSections()
                    }
                    5 -> {
                        binding.titleText.visibility = View.VISIBLE
                        binding.userNameSection.visibility = View.GONE
                        binding.titleText.setText(R.string.action_notifications)
                        hideactionsSections()
                    }
                }
                binding.pager.setCurrentItem(index, true)
                currentModuleIndex = binding.pager.currentItem
            }
        }
    */

    /**
     * Cambia a un fragmento determinado cuando la aplicacion esta operativa y loggeada
     *//*
    fun switchToFragment(fragment: Fragment, clearStack: Boolean = false) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.container, fragment
        )
        if (!clearStack) {
            transaction.addToBackStack(null)
        } else {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()

    }
    */

    /*
        fun switchToModule(
            index: Int, tag: String?, force: Boolean, key: String?, arguments: Bundle? = null
        ) {

            if (currentModuleIndex != index || force) {
                if (currentModuleIndex != -1) unSubscribeInModule(currentModuleIndex)
                currentModuleIndex = index



                lifecycleScope.launch(Dispatchers.Main) {
                    //        handleTouch()
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()


    //                binding.container.childCount
                    subscribeInModule(index)


                    when (index) {
                        IANModulesEnum.MAIN.ordinal -> {
                            appToolbar.hideTitle()
    //                        binding.titleText.visibility = GONE
                            //  binding.titleBarCardView.visibility = View.GONE
                            //                      binding.userNameSection.visibility = VISIBLE
                            //                      binding.titleText.setText(R.string.app_long_title)
                            binding.bottomToolbar.visibility = VISIBLE

    //establece un margintop para binding.container en 10dp


                            val containerLayoutParams =
                                binding.container.layoutParams as ViewGroup.MarginLayoutParams
                            val marginTop = 20.px  // convertir dp a px
                            containerLayoutParams.setMargins(
                                containerLayoutParams.leftMargin,
                                marginTop,
                                containerLayoutParams.rightMargin,
                                containerLayoutParams.bottomMargin
                            )
                            binding.container.layoutParams = containerLayoutParams


                            showactionsSections(null)

                            if (supportFragmentManager.fragments.contains(mHomeFragment)) {
                                transaction.show(mHomeFragment)
                            } else {
                                transaction.replace(
                                    R.id.container, mHomeFragment
                                )
                            }
                            binding.container.tag = mHomeFragment
                        }

                        IANModulesEnum.POST_EVENTS.ordinal -> {
                            if (isPanicButtonActive()) {
                                showErrorDialog(
                                    getString(R.string.not_possible),
                                    getString(R.string.error_panic_event_is_running_close_first),
                                    getString(R.string.close),
                                    null
                                )
                                return@launch
                            } else {
                                //binding.toolbar.visibility = VISIBLE
                                /*
                                                              binding.titleBarCardView.visibility = VISIBLE
                                                              binding.titleText.visibility = VISIBLE
                                                              binding.userNameSection.visibility = GONE
                                                              binding.titleText.setText(R.string.select_your_event)
                                  */
                                hideactionsSections()
                                transaction.replace(
                                    R.id.container, mEventsFragment
                                )
                                binding.container.tag = mEventsFragment

                            }
                        }

                        IANModulesEnum.EVENTS_TRACKING.ordinal -> {
                            //binding.toolbar.visibility = VISIBLE

                            /*
                                binding.titleBarCardView.visibility = VISIBLE
                                binding.titleText.visibility = VISIBLE
                                binding.userNameSection.visibility = GONE
                                binding.titleText.setText(R.string.action_events_map)
                                */
                            binding.bottomToolbar.visibility = GONE
                            hideactionsSections()

                            if (supportFragmentManager.fragments.contains(mMapFragment)) {
                                transaction.show(mMapFragment)
                            } else {
                                transaction.replace(
                                    R.id.container_full_screen, mMapFragment
                                )
                            }
                            key?.let {
                                //  mMapFragment.setEventKey(it)
                                mMapFragment.startObserveEvent(it)
                            }


                            binding.container.tag = mMapFragment
                        }

                        IANModulesEnum.CONTACTS.ordinal -> {/*
                                binding.titleBarCardView.visibility = VISIBLE
                                binding.titleText.visibility = VISIBLE
                                binding.userNameSection.visibility = GONE
                                binding.titleText.setText(R.string.action_friends_list)
                                */
                            hideactionsSections()


                            transaction.replace(
                                R.id.container, mFriendsFragment
                            )
                            binding.container.tag = mFriendsFragment

                        }

                        IANModulesEnum.SETTINGS.ordinal -> {/*
                                binding.titleBarCardView.visibility = VISIBLE
                                binding.titleText.visibility = VISIBLE
                                binding.userNameSection.visibility = GONE
                                binding.titleText.setText(R.string.action_settings)
        */
                            //  val fragment = pagerAdapter.getFragmentAt(4) as SettingsFragment


                            hideactionsSections()

                            transaction.replace(
                                R.id.container, mSettingsFragment
                            )

                            binding.container.tag = mSettingsFragment
                        }

                        IANModulesEnum.NOTIFICATIONS.ordinal -> {/*
                                binding.titleBarCardView.visibility = VISIBLE
                                binding.titleText.visibility = VISIBLE
                                binding.userNameSection.visibility = GONE
                                binding.titleText.setText(R.string.action_notifications)
                                */
                            hideactionsSections()

                            transaction.replace(
                                R.id.container, mNotificationsFragment
                            )
                            binding.container.tag = mNotificationsFragment

                        }
                    }

                    transaction.addToBackStack(null)
                    transaction.commit()
                    supportFragmentManager.executePendingTransactions()

                    //-----------------

                    //---------
                    when (index) {

                        IANModulesEnum.MAIN.ordinal -> {
                            //       registerAttachmentsContracts()
                            /*
                                                    pickImageContract
                            toTakeVideoPermissionsRequest
                            pickVideoContract
        */
                            //                   binding.containerFullScreen.visibility = GONE
                            //                   binding.container.visibility = VISIBLE

                            //            AppClass.instance.updateViewFromStack(IANModulesEnum.MAIN, mHomeFragment)


                        }

                        IANModulesEnum.POST_EVENTS.ordinal -> {
                            mEventsFragment.switchToFragment(
                                R.id.event_fragment_event_type_selector, arguments
                            )
                            //    binding.containerFullScreen.visibility = GONE
                            //   binding.container.visibility = VISIBLE

                            //                    AppClass.instance.updateViewFromStack(IANModulesEnum.POST_EVENTS, mEventsFragment)
                        }

                        IANModulesEnum.EVENTS_TRACKING.ordinal -> {
                            binding.containerFullScreen.visibility = VISIBLE
                            binding.container.visibility = GONE
                            if (mMapFragment.initialRenderDone) {
                                mMapFragment.onResume()
                            }
                            mMapFragment.initialRenderDone = true
                            key?.let {
                                mMapFragment.connectToChat(it)
                            }

                            arguments?.let {
                                mMapFragment.doAction(arguments)
                            }

                            //                     AppClass.instance.updateViewFromStack(IANModulesEnum.EVENTS_TRACKING, mMapFragment,key)

                        }

                        IANModulesEnum.SETTINGS.ordinal -> {
                            if (arguments == null || arguments.containsKey("go_to") == false) {
                                mSettingsFragment.goToFragment(0)
                            } else {
                                var fragmentIndex = SettingsFragmentsEnum.valueOf(
                                    arguments.getString("go_to").toString()
                                ).ordinal
                                mSettingsFragment.goToFragment(fragmentIndex)
                            }
                            //                  binding.containerFullScreen.visibility = GONE
                            //                  binding.container.visibility = VISIBLE

                            //                 AppClass.instance.updateViewFromStack(IANModulesEnum.SETTINGS, mSettingsFragment)

                        }

                        IANModulesEnum.CONTACTS.ordinal -> {
                            //                binding.containerFullScreen.visibility = GONE
                            //                binding.container.visibility = VISIBLE

                            //                  AppClass.instance.updateViewFromStack(IANModulesEnum.CONTACTS, mFriendsFragment)


                        }

                        IANModulesEnum.NOTIFICATIONS.ordinal -> {
                            //              binding.containerFullScreen.visibility = GONE
                            //                binding.container.visibility = VISIBLE

                            //         AppClass.instance.updateViewFromStack(IANModulesEnum.NOTIFICATIONS, mNotificationsFragment)

                        }
                    }


                }


                /*
                    binding.pager.setCurrentItem(index, true)
                    currentModuleIndex = binding.pager.currentItem

                     */
            }
        }
    *//*
    fun getCurrentFragment(): Fragment? {

        when (currentModuleIndex) {
            IANModulesEnum.MAIN.ordinal -> {
                return mHomeFragment
            }

            IANModulesEnum.POST_EVENTS.ordinal -> {
                return mEventsFragment

            }

            IANModulesEnum.EVENTS_TRACKING.ordinal -> {
                return mMapFragment

            }

            IANModulesEnum.CONTACTS.ordinal -> {
                return mFriendsFragment
            }

            IANModulesEnum.SETTINGS.ordinal -> {
                return mSettingsFragment
            }

            IANModulesEnum.NOTIFICATIONS.ordinal -> {
                return mNotificationsFragment
            }

            else -> {
                return null
            }

        }
    }

*/
    private fun handleGoBack(arguments: Bundle? = null) {
        handleTouch()

        supportFragmentManager.popBackStack()

        var currentFragment = getVisibleFragment(this@MainActivity.supportFragmentManager)
        return/*
                when (currentModuleIndex) {
                    IANModulesEnum.POST_EVENTS.ordinal -> {/* CCCC
                                            var internalFragment =
                                                getVisibleFragment(pagerAdapter.getFragmentAt(binding.pager.currentItem).childFragmentManager)
                            */
                        val internalFragment =
                            getVisibleFragment(getCurrentFragment()?.childFragmentManager!!)

                        if (internalFragment != null && internalFragment is EventTypeSelectorFragment) {
                            currentModuleIndex = 0
                            supportFragmentManager.popBackStack()
                            // CCCC binding.pager.currentItem = 0
                            showactionsSections(null)
                        } else {
                            when (internalFragment?.parentFragmentManager?.fragments?.get(0)?.javaClass) {
                                EventRealTimeTrackingFragment::class.java -> {
                                    mEventsFragment.switchToFragment(
                                        R.id.event_fragment_event_type_selector, arguments
                                    )
                                }/*
                                    EventLocationManualInputFragment::class.java -> {
                                        mEventsFragment.switchToFragment(R.id.event_fragment_location_read_only_selector)
                                    }*/
                                EventLocationReadOnlyFragment::class.java -> {


                                    val eventType = EventTypesEnum.valueOf(
                                        (internalFragment.parentFragmentManager.fragments.get(0) as EventLocationReadOnlyFragment).eventsFragmentViewModel.eventType.value.toString()
                                    )

                                    if (eventType == EventTypesEnum.KID_LOST || eventType == EventTypesEnum.PET_LOST || eventType == EventTypesEnum.SCORT_ME) {
                                        mEventsFragment.switchToFragment(
                                            R.id.event_fragment_event_type_selector,
                                            internalFragment.arguments
                                        )
                                    } else {
                                        mEventsFragment.switchToFragment(
                                            R.id.event_fragment_realtime_selector,
                                            internalFragment.arguments
                                        )
                                    }
                                }

                                EventAdditionalMediaFragment::class.java -> {

                                    var viewModel =
                                        (internalFragment as EventAdditionalMediaFragment).eventsFragmentViewModel
                                    var eventType: EventTypesEnum =
                                        EventTypesEnum.valueOf(viewModel.eventType.value!!)

                                    if (eventType == EventTypesEnum.KID_LOST || eventType == EventTypesEnum.PET_LOST || eventType == EventTypesEnum.SCORT_ME) {

                                        mEventsFragment.switchToFragment(
                                            R.id.event_fragment_location_read_only_selector,
                                            internalFragment.arguments
                                        )
                                    } else {

                                        if (mEventsFragment.getEvent().event_location_type == EventLocationType.FIXED.name) mEventsFragment.switchToFragment(
                                            R.id.event_fragment_location_read_only_selector,
                                            internalFragment.arguments
                                        )
                                        else mEventsFragment.switchToFragment(
                                            R.id.event_fragment_realtime_selector,
                                            internalFragment.arguments
                                        )
                                    }
                                }

                                else -> { // Note the block
                                    internalFragment?.parentFragmentManager?.popBackStack()

                                }
                            }

                        }
                    }

                    IANModulesEnum.SETTINGS.ordinal -> {/*
                                   var internalFragment =
                                       getVisibleFragment(pagerAdapter.getFragmentAt(IANModulesEnum.SETTINGS.ordinal).childFragmentManager)
                   */

                        when (mSettingsFragment.currentFragment) {
                            SettingsFragmentsEnum.LANDING -> {
        //                        switchToModule(0, "home")
                                goHome()
                            }

                            SettingsFragmentsEnum.PROFILE_SETTINGS -> {
                                mSettingsFragment.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
                            }

                            SettingsFragmentsEnum.SOS_SETTINGS -> {
                                val me = SessionForProfile.getInstance(this).getUserProfile()
                                if (me.sos_invocation_count >= 3) {

                                    val sosSettings: PressOrTapSetupFragment =
                                        mSettingsFragment.getFragment(SettingsFragmentsEnum.SOS_SETTINGS.ordinal)!! as PressOrTapSetupFragment

                                    sosSettings.save(
                                        SOSActivationMethods.valueOf(me.sos_invocation_method),
                                        me.sos_invocation_count
                                    )
        //                            moduleFragment.selectFragment(SettingsFragmentsEnum.LANDING.ordinal)
                                    mSettingsFragment.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
                                }
                            }

                            SettingsFragmentsEnum.NOTIFICATION_GROUPS -> {
                                mSettingsFragment.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
                            }

                            SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS -> {
                                mSettingsFragment.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
                            }

                            SettingsFragmentsEnum.NOTIFICATION_LIST -> {
                                mSettingsFragment.goToFragment(SettingsFragmentsEnum.NOTIFICATION_GROUPS.ordinal)
                            }


                            SettingsFragmentsEnum.PLAN_SETTINGS -> {
                                mSettingsFragment.goToFragment(SettingsFragmentsEnum.LANDING.ordinal)
                            }

                        }
                    }

                    else -> {
        //                switchToModule(0, "home")
                        goHome()
                    }
                }

        */
    }


    private fun subscribeInModule(moduleIndex: Int) {
        when (moduleIndex) {
            0 -> {
                //       mHomeFragment.subscribe()
            }

            1 -> {
                //removeMapOptionsButton()
                //               mEventsFragment.unSubscribe()

            }

            2 -> {
                //      mMapFragment.subscribe()
            }

            3 -> {
                //   mFriendsFragment.subscribe()
            }

            4 -> {
// SETTINGS
            }

            5 -> {
// NOTIFICATIONS
            }
        }
    }

    private fun unSubscribeInModule(moduleIndex: Int) {
        when (moduleIndex) {
            0 -> {
                mHomeFragment.unSubscribe()
            }

            1 -> {
                //removeMapOptionsButton()
                //               mEventsFragment.unSubscribe()

            }

            2 -> {
                mMapFragment.disconnectFromEvent()
            }

            3 -> {
                //   mFriendsFragment.unSubscribe()
            }

            4 -> {
// SETTINGS
            }

            5 -> {
// NOTIFICATIONS
            }
        }
    }

    private fun hideactionsSections() {
        showBackArrow()
    }


    private fun showactionsSections(onCompleteCallback: OnCompleteCallback?) {
        hideBackArrow()
    }

    private fun showBackArrow() {
        appToolbar.enableBackBtn(true)/*
            binding.backArrow.visibility = VISIBLE
            binding.actionSettings.visibility = GONE

             */
    }

    private fun hideBackArrow() {
        appToolbar.enableBackBtn(false)

        /*
        binding.backArrow.visibility = GONE
        binding.actionSettings.visibility = VISIBLE
        */
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults)

        var grantResults = grantResults
        //  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (SessionApp.getInstance(this).isBTPanicButtonEnabled) {
                    AppClass.instance.startBLEService()
                    AppClass.instance.itagPressMode = ITagModesEnum.active
                }
            }

            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                //     getContactList()
            }

            Constants.LOCATION_REQUEST_CODE -> {/*
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        val _grantResults = IntArray(3)
                                        _grantResults[0] = grantResults[0]
                                        _grantResults[1] = grantResults[1]
                                        _grantResults[2] = PackageManager.PERMISSION_GRANTED
                                        grantResults = _grantResults
                                    }
                                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                                        if (SessionForProfile.getInstance(this)
                                                ?.getProfileProperty("RTLocationEnabled") as Boolean
                                        ) {
                                            //  startListeningLocation()
                                            JobServicesUtils.scheduleJob(this, LocationUpdatesService::class.java)
                                        }
                                    } else {
                                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                        ) {
                                            val mRationale = PermissionsRationaleDialog(
                                                this, this, R.string.rationale_pemission_location, arrayOf(
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                                ),
                                                Constants.LOCATION_REQUEST_CODE
                                            )
                                            mRationale.show()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Debes activar el permiso manualmente",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val mPermissionEnablingDialog = PermissionsEnablingDialog(
                                                this,
                                                this,
                                                R.string.rationale_pemission_location_manual_activation, arrayOf(
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                                ),
                                                Constants.LOCATION_REQUEST_CODE
                                            )
                                            mPermissionEnablingDialog.show()

                                        }
                                    }
                    */
            }


        }

    }/*
            fun startListeningLocation() {
                if (!isServiceRunning(LocationService::class.java)) {
                    val locationIntent = Intent(this, LocationService::class.java)
                    startService(locationIntent)
                }
                if (AppClass.instance.getLocationService()?.isTrackingLocation == false) {
                    AppClass.instance.getLocationService()?.startListeningLocation()
                }
            }

            fun stopListeningLocation() {
                if (isServiceRunning(LocationService::class.java)) {
                    AppClass.instance.getLocationService()?.stopListeningLocation()

                }
            }
        */

    private fun showPermissionDialog() {
        // Here, thisActivity is the current activity
        val locationPermissions = arrayOf(
            ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
        )
        if (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, locationPermissions.toString()
                )
            ) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION), 400
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private fun registerReceivers() {

        val broadcastIntentFilter = IntentFilter()
        broadcastIntentFilter.addAction(packageName)
        broadcastIntentFilter.addAction(NOTIFICATION_TYPE_ON_NEW_MESSAGE)
        broadcastIntentFilter.addAction(NOTIFICATION_TYPE_NEW_MESSAGE)
        broadcastIntentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED)
        broadcastIntentFilter.addAction(BROADCAST_EVENT_CLOSE_TO_EXPIRE)
        broadcastIntentFilter.addAction(BROADCAST_DID_YOU_ARRIVE_REQUEST)
        broadcastIntentFilter.addAction(BROADCAST_PULSE_REQUIRED)
        broadcastIntentFilter.addAction(BROADCAST_BLOCKED_LAYOUT_REQUIRED)
        broadcastIntentFilter.addAction(BROADCAST_BLOCKED_LAYOUT_DISMISS)
        //       broadcastIntentFilter.addAction(Constants.BROADCAST_PANIC_BUTTON_PRESSED)
        broadcastIntentFilter.addAction(BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED)
        broadcastIntentFilter.addAction(AppConstants.BROADCAST_ACTION_REQUEST_PIN)
        broadcastIntentFilter.addAction(BROADCAST_MESSAGE_SCAN_RESULT_UPDATED)
        broadcastIntentFilter.addAction(
            BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED
        ) // Para que escuche los pedidos de actualizacion del indicador

        broadcastIntentFilter.addAction(BROADCAST_ACTION_SHOW_FOOTER_TOOLBAR) // Para que escuche los pedidos de actualizacion del indicador
        broadcastIntentFilter.addAction(BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR) // Para que escuche los pedidos de actualizacion del indicador
        broadcastIntentFilter.addAction("ON_NOTIFICATION_INCOME") // Para que escuche los pedidos de actualizacion del indicador
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            broadcastReceiver, broadcastIntentFilter
        )


        /*
                    //----------------- EXPIRATION RECEIVERS ----------------------------------------------
                    val eventExpirationIntentFilter = IntentFilter()
                    eventExpirationIntentFilter.addAction(Constants.BROADCAST_EVENT_CLOSE_TO_EXPIRE)
                    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                        eventExpirationReceiver,
                        eventExpirationIntentFilter
                    )


                    // ---------------- ARRIVE TO DESTINATION ------------------------------
                    val arriveValidationIntentFilter = IntentFilter()
                    arriveValidationIntentFilter.addAction(Constants.BROADCAST_DID_YOU_ARRIVE_REQUEST)
                    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                        arriveValidationReceiver,
                        arriveValidationIntentFilter
                    )

                    //----------------- PULSE ----------------------------------------------
                    val pulseValidationIntentFilter = IntentFilter()
                    pulseValidationIntentFilter.addAction(Constants.BROADCAST_PULSE_REQUIRED)
                    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                        pulseRequestReceiver,
                        pulseValidationIntentFilter
                    )
                    */
        //---------------- FALLING ----------------------------------------------
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_FALLING_EVENT)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : FallDetectionServiceMethod.FallingReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    super.onReceive(context, intent)
                    val data: Map<String, Any>? = Gson().fromJson(
                        intent.getStringExtra("data"), Map::class.java
                    ) as Map<String, Any>?

                    val latLngMap = data!!["location"] as Map<String, Double>
                    val latLng = LatLng(
                        latLngMap["latitude"] as Double, latLngMap["longitude"] as Double
                    )
                    val event = Event()
                    event.event_type = EventTypesEnum.FALLING_ALARM.name
                    event.visibility = EventVisibilityTypes.PENDING.name
                    event.event_location_type = EventLocationType.REALTIME.name
                    event.status = EventStatusEnum.DANGER.name
                    var me = SessionForProfile.getInstance(this@MainActivity).getUserProfile()/*
                                            event.user_key = FirebaseAuth.getInstance().uid.toString()
                                            var user = UserInEvent()
                                            user.display_name = me.display_name
                                            user.user_key = event.user_key
                                            user.image = me.image
                                            event.user = user

                                            event.time = System.currentTimeMillis()
                        */
                    event.status = PulseValidationStatus.USER_IN_TROUBLE.name

                    val locationAsCreation = GeoLocation()
                    locationAsCreation.l = listOf(latLng.latitude, latLng.longitude)
                    event.location_at_creation = locationAsCreation

                    val eventLocation = EventLocation()
                    eventLocation.latitude = latLng.latitude
                    eventLocation.longitude = latLng.longitude
                    event.location = eventLocation
                    event.location?.address_components = null

                    // Public el evento de que se cayo y q espero respuesta
                    event.status = EventStatusEnum.CONFIRMATION_REQUESTED.name
                    onPublishEvent(event, object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {

                            val event = result as Event

                            requestStatusConfirmation(PulseRequestTarget.ON_FALLING_VALIDATION,
                                object : PulseValidationCallback {
                                    override fun onSilentAlarmCode(
                                        dialog: PulseValidatorDialog, inputText: String
                                    ) {
                                        event.status = EventStatusEnum.USER_IN_TROUBLE.name

                                        viewModel.postEventClicked(event)/*
                                                                               EventsWSClient.instance.publishPostedEvent(
                                                                                   event,
                                                                                   object : OnCompleteCallback {
                                                                                       override fun onComplete(
                                                                                           success: Boolean, result: Any?
                                                                                       ) {


                                                                                       }
                                                                                   })

                                         */
                                    }

                                    override fun onNoResponse() {
                                        event.status = EventStatusEnum.CONFIRMATION_MISSING.name

                                        viewModel.postEventClicked(event)/*
                                        EventsWSClient.instance.publishPostedEvent(
                                            event,
                                            object : OnCompleteCallback {
                                                override fun onComplete(
                                                    success: Boolean, result: Any?
                                                ) {
                                                    showErrorDialog("Error")
                                                }
                                            })

                                         */
                                    }

                                    override fun onWrongCode(
                                        dialog: PulseValidatorDialog, securityPIN: String
                                    ) {
                                        super.onWrongCode(dialog, securityPIN)
                                        event.status = EventStatusEnum.DANGER.name

                                        viewModel.postEventClicked(event)

                                        /*
                                                                              EventsWSClient.instance.publishPostedEvent(
                                                                                  event,
                                                                                  object : OnCompleteCallback {
                                                                                      override fun onComplete(
                                                                                          success: Boolean, result: Any?
                                                                                      ) {

                                                                                      }
                                                                                  })
                                      */

                                    }

                                    override fun onValidationOK(
                                        dialog: PulseValidatorDialog, securityPIN: String
                                    ) {

                                        Toast.makeText(
                                            this@MainActivity,
                                            "Que bueno saber que estas bien",
                                            Toast.LENGTH_SHORT
                                        ).show()


                                        val auxEventFollowed = EventFollowed()
                                        auxEventFollowed.event_key = event.event_key
                                        auxEventFollowed.author = event.toEventFollowed().author

                                        closeEvent(event.event_key,
                                            securityPIN,
                                            object : OnCompleteCallback {

                                                override fun onComplete(
                                                    success: Boolean, result: Any?
                                                ) {
                                                    pulseValidatorDialog.dismiss()
                                                }
                                            })


                                    }
                                })


                        }
                    })


                }

            }, intentFilter
        )
        //---------------- CHATS UPDATES -----------------------------------------
        /*
                val filter = IntentFilter(Constants.BROADCAST_UNREAD_MESSAGES_UPDATES)
                registerReceiver(unreadMessagesBroadcastReceiver, filter)
        */
    }


    private fun handleDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener(
            this
        ) { pendingDynamicLinkData ->
            // Get deep link from result (may be null if no link is found)
            var deepLink: Uri? = null
            if (pendingDynamicLinkData != null) {
                deepLink = pendingDynamicLinkData.link

                val action = deepLink?.getQueryParameter("action")
                val key = deepLink?.getQueryParameter("key").toString()

                if ((key + "--").compareTo(SessionForProfile.getInstance(this).getUserId()) == 0) {
                    showErrorDialog(
                        getString(
                            R.string.error_cannot_send_it_to_you
                        )
                    )
                } else {
                    when (action) {
                        DYNAMIC_LINK_ACTION_FRIENDSHIP -> {
                            //     mPresenter.onSharingContactByUserKey(key)

                            viewModel.onContactByUserKey(key)


                        }

                        DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL -> {
//                            mPresenter.onFriendshipRequestAndSpeedDialByUserKey(key)
                            showSnackBar(
                                binding.root,
                                "Implementar en el viewmodel onFriendshipRequestAndSpeedDialByUserKey"
                            )

                        }
                    }
                }
                Toast.makeText(this, action, Toast.LENGTH_LONG).show()
            }


            // Handle the deep link. For example, open the linked content,
            // or apply promotional credit to the user's account.
            // ...

            // ...
        }.addOnFailureListener(
            this
        ) { e -> Log.w("DYNAMIC-LINKS", "getDynamicLink:onFailure", e) }
    }


    override fun onError(exception: Exception) {
        hideLoader()
        showErrorDialog(exception.localizedMessage)
    }


    override fun onEventFollowedAdded(
        eventFull: Event, eventFollowed: EventFollowed
    ) {
        SessionForProfile.getInstance(this).addEventToUser(eventFollowed)

        //  this.eventsFollowedArray.add(eventFollowed)
        mHomeFragment.updateUI()
        updateLocationIntensity()
        var intent = Intent(BROADCAST_EVENT_FOLLOWED_ADDED)
        val dataJson = Gson().toJson(eventFollowed)
        intent.putExtra("data", dataJson)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        updateUI()
    }

    fun onEventFollowedAdded(
        eventFollowed: EventFollowed
    ) {

        SessionForProfile.getInstance(this).addEventToUser(eventFollowed)

        // Reviso si es un evento de panico y de ser asi, lo marco.
        if (eventFollowed.event_type == EventTypesEnum.PANIC_BUTTON.name && eventFollowed.author.author_key == FirebaseAuth.getInstance().uid.toString()) {
            AppClass.instance.setInPanic()
        }
        //   this.eventsFollowedArray.add(eventFollowed)
        mHomeFragment.updateUI()
        updateLocationIntensity()/*
        var intent = Intent(BROADCAST_EVENT_FOLLOWED_ADDED)
        val dataJson = Gson().toJson(eventFollowed)
        intent.putExtra("data", dataJson)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

         */
        updateUI()
    }

    override fun onEventFollowedChanged(
        eventFull: Event, eventFollowed: EventFollowed
    ) {
// CCCC        mMapFragment?.onEventFollowedChanged(eventFull, eventFollowed)
        /*
         val index = this.eventsFollowedArray.indexOf(eventFollowed)
         if (index > -1) {
             this.eventsFollowedArray[index] = eventFollowed
         }
         */





        updateLocationIntensity()
//        mHomeFragment?.updateUI()
        /*
                var intent = Intent(BROADCAST_EVENT_FOLLOWED_UPDATED)
                val dataJson = Gson().toJson(eventFollowed)
                intent.putExtra("data", dataJson)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                */
        updateUI()
    }


    fun onEventFollowedChanged(
        eventFollowed: EventFollowed
    ) {/*
            val index = this.eventsFollowedArray.indexOf(eventFollowed)
            if (index > -1) {
                this.eventsFollowedArray[index] = eventFollowed
            }
            */
        updateLocationIntensity()

        var intent = Intent(BROADCAST_EVENT_FOLLOWED_UPDATED)
        val dataJson = Gson().toJson(eventFollowed)
        intent.putExtra("data", dataJson)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        updateUI()
    }


    override fun onEventFollowedRemoved(eventKey: String) {
        updateLocationIntensity()
        updateUI()
    }

    override fun isFollingEvent(eventKey: String): Boolean {
        getEventsFollowed().forEach { event ->
            if (event.event_key == eventKey) {
                return true
            }

        }

        return false
    }

//--------------

    override fun goToEvent(eventKey: String) {
        this.onSwitchToEvent(eventKey)
    }

    override fun onPublishEventDone(event: Event?) {
        mHomeFragment.isPanicButtonBusy = false
        mHomeFragment.getRedButtonRef().isEnabled = true
        hideLoader()
        //    switchToModule(0, "home")
        goHome()
        if (event != null) {
//            showEventRedirectorDialog(event.event_key)
            viewModel.showGoToEventDialog(null, event.event_key)
        }
        appIsBussy = false
        updateUI()
    }

    /*
        private fun askForGoToEvent(event: Event) {
            var callbackDialog: OnEventPublishedDone =
                object : OnEventPublishedDone {
                    override fun onBringMeToEvent(
                        eventKey: String
                    ) {
                        goToEvent(event.event_key)
                    }
                    override fun onRefuse() {

                    }
                }
            var doneDialog = EventPublishedDoneDialog(
                this,
                this,
                callbackDialog
            )
            doneDialog.show()

        }
    */


    fun onPanicCancelRequest() {

        requestStatusConfirmationSingleton(PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT,
            object : PulseValidationCallback {
                override fun onWrongCode(
                    dialog: PulseValidatorDialog, securityPIN: String
                ) {
                    super.onWrongCode(dialog, securityPIN)



                    if (!viewModel.isFreeUser()) {
                        showErrorDialog(
                            getString(R.string.error_wrong_security_code),
                            getString(R.string.error_wrong_security_code_message),
                            getString(R.string.close),
                            null
                        )
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Codigo Incorrecto. Hay que Notificar a todos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                override fun onValidationOK(
                    dialog: PulseValidatorDialog, code: String
                ) {

                    if (!viewModel.isFreeUser()) {
                        showLoader(getString(R.string.closing_event_wait))

                        viewModel.onCloseEventRequest(
                            AppClass.instance.getPanicEventKey()!!, code
                        )/*
                                                EventsWSClient.instance.closeEvent(
                                                    AppClass.instance.getPanicEventKey()!!,
                                                    SessionForProfile.getInstance(this@onPanicCancelRequest).getUserId(),
                                                    code,
                                                    object : OnCompleteCallback {
                                                        override fun onComplete(
                                                            success: Boolean,
                                                            result: Any?
                                                        ) {
                                                            hideLoader()
                                                            AppClass.instance.getPanicEvent()
                                                                ?.let {

                                                                    Toast.makeText(applicationContext,"Hacer algo aca", Toast.LENGTH_SHORT).show()
                                                                    //    mainActivity.onEventCloseDone(it.event_key)

                                                                }

                                                            showAnimatedDialog(
                                                                getString(R.string.closing_event_title),
                                                                getString(R.string.event_sucessfully_close)
                                                            )

                                                        }
                                                    }
                                                )
                                                */
                    } else {
                        SessionApp.getInstance(applicationContext).isInPanic(false)
                        broadcastMessage(
                            null, AppConstants.BROADCAST_ACTION_REFRESH_PANIC_BUTTON
                        )
                    }
                }
            })

    }


    fun showNotificationsDialog() {/*
                    if (!notificationDetail.isShowing) {
                        //   notificationDetail.setData(notificationDetail.getData())
                        notificationDetail.show()
                    }

             */
    }

    fun onAgreeToAssist(notificationKey: String, eventKey: String) {
//        mPresenter.onAgreeToAssist(notificationKey, eventKey)
        viewModel.onAgreeToAssist(notificationKey, eventKey)
        showSnackBar(binding.root, "Implementar en el viewmodel onAgreeToAssist")

    }

    override fun onAgreeToAssistDone(eventKey: String) {
//        showEventRedirectorDialog(eventKey)
        viewModel.showGoToEventDialog(null, eventKey)
    }
    /*
        override fun onNotificationDismiss(notification: EventNotificationModel) {

        //    onNotificationRemoveInternal(notification)
    //        mPresenter.onNotificationDismiss(notification)

            showSnackBar(binding.root, "Implementar en el viewmodel onNotificationDismiss")
        }
    */

    /*
    override fun onStartToFollow(eventKey: String) {
        //   mPresenter.onStartToFollow(eventKey)
        showSnackBar(binding.root, "Implementar en el viewmodel onStartToFollow")

    }
*/
    override fun onDenyToAssist(event: Event) {
        event.event_key.let {
//            mPresenter.onDenyToAssist(it)

            showSnackBar(binding.root, "Implementar en el viewmodel onDenyToAssist")

        }
    }

    override fun onDenyToAssist(eventKey: String) {
//        mPresenter.onDenyToAssist(eventKey)

        showSnackBar(binding.root, "Implementar en el viewmodel onDenyToAssist")

    }

    private fun resolveOnEventRemoved(eventKey: String) {
        mMapFragment.resolveOnEventRemoved(eventKey)
    }


    override fun onSwitchToEvent(eventKey: String) {

//        switchToModule(2, "scort", false, eventKey)
        val bundle = bundleOf("eventKey" to eventKey)

        findNavController(R.id.nav_host_fragment).navigate(R.id.mapSituationFragment, bundle)

        // mMapFragment.selectEvent(eventKey)
    }


    override fun onViewerAdded(eventFollower: EventFollower) {
        //  TODO("Not yet implemented")
    }

    override fun onViewerChanged(eventFollower: EventFollower) {
        //   TODO("Not yet implemented")
    }

    override fun onViewerRemoved(eventFollower: EventFollower) {
        // TODO("Not yet implemented")
    }

    override fun onViwerMoved(eventFollower: EventFollower) {
        TODO("Not yet implemented")
    }

    override fun closeEvent(eventKey: String, securityCode: String) {
        showLoader()
        //mPresenter.closeEvent(eventKey, securityCode, null)

        showSnackBar(binding.root, "Implementar en el viewmodel closeEvent")

    }

    override fun closeEvent(
        eventKey: String, securityCode: String, callback: OnCompleteCallback
    ) {
        showLoader()
//        mPresenter.closeEvent(eventKey, securityCode, callback)
        viewModel.onCloseEventRequest(eventKey, securityCode)
        showSnackBar(binding.root, "Implementar en el viewmodel closeEvent")


    }


    override fun onEventCloseDone(eventKey: String) {
        try {
            //       mMapFragment.eventRemove(eventKey)
            updateLocationIntensity()
        } catch (ex: java.lang.Exception) {
            showErrorDialog(ex.localizedMessage.toString())
        }
        hideLoader()/*
                   if (eventFollowed.event_type == EventTypesEnum.SCORT_ME.name) {
                       val me = SessionForProfile.getInstance(this)!!.getUserProfile()
                       me.is_monitoring = false
                       SessionForProfile.getInstance(this).storeUserProfile(me)
                   }
                   SessionForProfile.getInstance(this).removeEventInUser(eventFollowed)
           */
        mMapFragment.resolveOnEventRemoved(eventKey)
        mHomeFragment.updateUI()

        updateUI()
        // todo : ver hacia donde lo mandas
    }


    override fun onLeaveEventRequest(eventKey: String) {
        //  mPresenter.onLeaveEventRequest(eventKey)

        showSnackBar(binding.root, "Implementar en el viewmodel onLeaveEventRequest")


    }


    override fun onLeaveEventRequestDone(eventKey: String) {
        mMapFragment.resolveOnEventRemoved(eventKey)
        updateLocationIntensity()
        Toast.makeText(
            this, "Seleccionar otro evento o mostrar un cartel y desactivar", Toast.LENGTH_LONG
        ).show()
    }/*
            override fun onGoingToHelpRequest(eventKey: String) {
                var meAsViewer =
                    mMapFragment!!.currentEvent!!.viewers?.get(FirebaseAuth.getInstance().uid.toString())
                meAsViewer!!.going_time = true
                //   mMapFragment?.configureSpeedDial(mMapFragment?.eventSelected)

                mPresenter.onGoingToHelpRequest(eventKey)

            }
            */

    override fun onGoingToHelpRequestDone() {
    }

    /*
        override fun onNotGoingToHelpRequest(eventKey: String) {
            var meAsViewer =
                mMapFragment!!.currentEvent!!.viewers?.get(FirebaseAuth.getInstance().uid.toString())
            meAsViewer!!.going_time = false
            // mMapFragment?.configureSpeedDial(mMapFragment?.eventSelected)

            mPresenter.onNotGoingToHelpRequest(eventKey)
        }
    */

    override fun onNotGoingToHelpRequestDone() {
        //   mMapFragment?.configureSpeedDial(mMapFragment?.eventSelected)
    }


    private fun PopupWindow.dimBehind() {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
    }


    override fun onPlaySoundFile(url: String) {
        MultimediaUtils(this).playSound(this, url)
    }

    override fun onPlayVideoFile(url: String) {
        videoDialog = this.playVideo(url)
    }

    override fun onShowImageFile(url: String) {
        this.showImage(url)
    }


    override fun onShowTextMessage(text: String) {
        this.showTextDialog(text)
    }


    //--- Countdown Section
    open fun initCountDownAnimation() {
//        countDownAnimation = CountDownAnimation(textView, 5)
        countDownAnimation?.setCountDownListener(this)
    }

    private fun startCountDownAnimation() {
        // Customizable animation

        // Alpha)
        // Use a set of animations
        val scaleAnimation: Animation = ScaleAnimation(
            1.0f,
            0.0f,
            1.0f,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        val alphaAnimation: Animation = AlphaAnimation(1.0f, 0.0f)
        val animationSet = AnimationSet(false)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)
        countDownAnimation?.animation = animationSet

        // Customizable start count
        countDownAnimation?.startCount = 5
        countDownAnimation?.start()
    }

    override fun onCountDownEnd(animation: CountDownAnimation?) {
        TODO("Not yet implemented")

    }


    private fun showCloseToExpireDialog(

        eventKey: String
    ) {
        AppClass.instance.getCurrentActivity().let { activity ->
            if (eventCloseToExpireDialog == null) {
                eventCloseToExpireDialog = PulseValidatorDialog(
                    this, activity!!
                )
            }
            if (!eventCloseToExpireDialog!!.isShowing) {
                eventCloseToExpireDialog?.show()
            }
        }
    }


    private fun showIsCloseToExpireDialog(
        eventKey: String, extras: Bundle?, callback: EventCloseToExpireDialogCallback?
    ) {
        showIsCloseToExpireDialogSingleton(eventKey, extras, callback)
    }


    private fun requestArrivingConfirmation(eventKey: String) {

        // Si estaba requiriendo confirmacion de estado y me registra que ya habria llegado, cierro el dialog.
        if (pulseValidatorDialog.isShowing) {
            pulseValidatorDialog.dismiss()
        }

        var callback = object : OnYesNoButtonsListener {
            override fun onYes() {
                super.onYes()
                onImSafeEvent(eventKey)
            }

        }
        arrivingConfirmationDialog.setCallback(callback)
        if (!arrivingConfirmationDialog.isShowing) {
            arrivingConfirmationDialog.show()
        }
    }


    private fun requestStatusConfirmation(
        requestType: PulseRequestTarget, callback: PulseValidationCallback?
    ) {
        AppClass.instance.getCurrentActivity().let { activity ->
            /*
              if (pulseValidatorDialog == null) {
                  pulseValidatorDialog = PulseValidatorDialog(
                      this,
                      activity!!
                  )
              }

             */
            pulseValidatorDialog.setValidationType(requestType)
            callback?.let { pulseValidatorDialog.setCallback(it) }
            if (!pulseValidatorDialog.isShowing) {
                pulseValidatorDialog.show()
            }

        }
    }


    override fun onValidationOK(dialog: PulseValidatorDialog, code: String) {

        lifecycleScope.launch(Dispatchers.Main) {
            SmartLocation.with(this@MainActivity).location().oneFix().start { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                viewModel.securityPINIntroduced(code, latLng)
            }
        }

    }

    /*
        override fun onValidationFailed(status: PulseValidationStatus) {

            var locationCallback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {
                    var latLng = result as LatLng

                    var onUpdateCallback: OnCompleteCallback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            pulseValidatorDialog?.dismiss()
                            when (status) {
                                PulseValidationStatus.USER_IN_TROUBLE -> {
                                    playSound(R.raw.intro_bell, null, null)

                                    showAnimatedDialog(
                                        getString(R.string.thaks_with_exclamation),
                                        getString(R.string.glad_that_you_are_ok)
                                    )
                                    /*
                                    AestheticDialog.Builder(
                                        this@MainActivity,
                                        DialogStyle.FLASH,
                                        DialogType.SUCCESS
                                    )
                                        .setTitle("Confirmado!")
                                        .setMessage("Gracias por avisarnos que estas bien")
                                        .setCancelable(false)
                                        .setDarkMode(false)
                                        .setDuration(2000)
                                        .setGravity(Gravity.CENTER)
                                        .setAnimation(DialogAnimation.SHRINK)
                                        .setOnClickListener(object : OnDialogClickListener {
                                            override fun onClick(dialog: AestheticDialog.Builder) {
                                                dialog.dismiss()
                                                //actions...
                                            }
                                        })
                                        .show()
        */
                                }
                                PulseValidationStatus.USER_NOT_RESPONSE -> {


                                }
                                PulseValidationStatus.WRONG_PIN -> {

                                }
                            }
                        }
                    }

                    UsersWSClient.instance.updateUserStatus(status.name, latLng, onUpdateCallback)
                }


    //        UsersApi.instance.sendPulseVerificationUnsuccessfull(this, status.name)

            }
            getUserLocationOnce(locationCallback)

        }
    */
    override fun onInputValidationRequest() {
        requestStatusConfirmation(
            PulseRequestTarget.VALIDATE_USER, null
        )
    }

    override fun onImSafeEvent(eventKey: String?) {
        pulseValidatorDialog.setCallback(null)
        val event = mMapFragment.getMyTrackingEvent()
        event?.let {
            var eventKey = it.event_key
            requestStatusConfirmation(PulseRequestTarget.VALIDATE_USER,
                object : PulseValidationCallback {

                    override fun onValidationOK(
                        dialog: PulseValidatorDialog, code: String
                    ) {

                        val securityCode = code
                        closeEvent(eventKey, securityCode)
                    }
                })
        }

        if (!eventKey.isNullOrEmpty()) {
            requestStatusConfirmation(PulseRequestTarget.VALIDATE_USER,
                object : PulseValidationCallback {

                    override fun onValidationOK(
                        dialog: PulseValidatorDialog, code: String
                    ) {

                        val securityCode = code
                        closeEvent(eventKey, securityCode)
                    }
                })

        }

    }


    override fun isUserInHidenPanic(): Boolean {
        val mineEvents = getEventsFollowed()
        var isInHiddenPanic = false
        mineEvents.forEach { event ->
            if (event.author.author_key == SessionForProfile.getInstance(this).getUserId()) {
                if (event.event_type == EventTypesEnum.PANIC_BUTTON.name && event.visibility == EventVisibilityTypes.HIDDEN_FOR_AUTHOR.name) {
                    isInHiddenPanic = true
                    return@forEach
                }
            }
        }
        return isInHiddenPanic

    }

    // Bluetooth
    override fun notifyState(state: Int) {
        Log.d("BLUETOOTH", "State = " + state.toString())
    }


    fun addButtonToToolbar(controlResId: Int, image: Any): View {
        if (getToolbarButton(controlResId) == null) {
            val newButton = ToolbarUtils().createToolbarButton(this, controlResId, image)
            // binding.rightSection.addView(newButton)
            newButton.setOnClickListener {
                //            mMapFragment.togleMapOptionsButton(newButton)
            }
            return newButton
        }
        return getToolbarButton(controlResId)!!
    }

    fun removeButtonInToolbar(controlResId: Int) {
        //binding.rightSection.removeView(getToolbarButton(controlResId))

    }

    fun getToolbarButton(controlResId: Int): View? {
        return binding.root.findViewWithTag<View>(controlResId)
    }


    inner class FadePageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.translationX = view.width * -position

            if (position <= -1.0f || position >= 1.0f) {
                view.alpha = 0.0f
                view.visibility = GONE
            } else if (position == 0.0f) {
                view.alpha = 1.0f
                view.visibility = VISIBLE
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.alpha = 1.0f - Math.abs(position)
                view.visibility = GONE
            }
        }
    }

    private fun checkGooglePlayServices(): Boolean {
        val checkGooglePlayServices =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)


        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {/*
                * google play services is missing or update is required
                *  return code could be
                * SUCCESS,
                * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
                * SERVICE_DISABLED, SERVICE_INVALID.
                */

            GoogleApiAvailability.getInstance().getErrorDialog(
                this, checkGooglePlayServices, REQUEST_CODE_RECOVER_PLAY_SERVICES
            )!!.show()
            return false
        }
        return true
    }


//--------------------------- Contacts

    /*
        // MapSituationFragment Callbacks
        override fun onGoingStateChanged(
            eventKey: String, viewerKey: String, callback: OnCompleteCallback
        ) {

            val callback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {

                }

                override fun onError(exception: java.lang.Exception) {
                    super.onError(exception)
                    callback.onError(exception)
                }
            }

    //        mPresenter.onGoingStateChanged(eventKey, viewerKey, callback)

            showSnackBar(binding.root, "Implementar en el view model onGoingStateChanged")


        }
    */
    /*
        override fun onCallAuthorityStateChanged(
            eventKey: String, viewerKey: String, callback: OnCompleteCallback
        ) {
            val callback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {

                }

                override fun onError(exception: java.lang.Exception) {
                    super.onError(exception)
                    callback.onError(exception)
                }
            }

            //mPresenter.onCallAuthorityStateChanged(eventKey, viewerKey, callback)
            showSnackBar(
                binding.root, "Implementar en el viewmodel onCallAuthorityStateChanged"
            )


        }
    */
    /*
        override fun selectUserToFollow(userKey: String) {
            //     var pp = 33
            mMapFragment.getBottomSheetLayout()?.state = BottomSheetBehavior.STATE_COLLAPSED
            mMapFragment.setCameraMode(CameraModesEnum.FOLLOW_USER, userKey)
        }*/

    override fun getCameraMode(): CameraMode {
        return mMapFragment.currentCameraMode
    }/*
        fun getPresenter(): MainPresenter {
            return mPresenter
        }
    */

    private fun showContactListPopupWindow(
        view: View, activity: Activity
    ) { //Create a View object yourself through inflater
        val inflater =
            view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        contactListPopupView = inflater.inflate(R.layout.context_popup_contact_list, null)

        contactListPopupView?.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val attachmentButtonLocation = IntArray(2)
        view.getLocationOnScreen(attachmentButtonLocation)
        val x: Int = contactListPopupView?.measuredWidth as Int

        var popupHeight: Int = contactListPopupView?.measuredHeightAndState as Int


        //Specify the length and width through constants
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        //Make Inactive Items Outside Of PopupWindow
        configureContactListContextMenu(contactListPopupView!!)


        val popupWindow = PopupWindow(contactListPopupView, width, height, true)

//        val displayMetrics  = DisplayManager().getDisplay(Display.DEFAULT_DISPLAY)


        val displayMetrics = Resources.getSystem().displayMetrics

        /*
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
          */
        val displayWidth = displayMetrics.widthPixels

        //val xAxis = displayWidth / 4 - displayWidth / 10
        val xAxis = attachmentButtonLocation[0]
        val yAxis = attachmentButtonLocation[1] + 40.px //76.px
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(
            view, android.view.Gravity.NO_GRAVITY, xAxis, yAxis
        )
        popupWindow.dimBehind()

        contactListPopupView?.setOnClickListener {
            handleTouch()
            popupWindow.dismiss()
        }
    }

    private fun configureContactListContextMenu(popupView: View) {

        val recyclerContactList: RecyclerView =
            popupView.findViewById<RecyclerView>(R.id.recycler_list)

        recyclerContactList.adapter = contactListAdapter
        recyclerContactList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val auxList = ContactGroup()
        auxList.list_key = AppConstants.NEW_CONTACT_LIST_KEY
        auxList.list_name = getString(R.string.new_contact_list)

        val data = contactListAdapter.getData()
        if (!data.contains(auxList)) {
            data.add(auxList)
            contactListAdapter.notifyItemInserted(data.size - 1)
        }
    }


    fun contactRequestAccept(contact: Contact) {
        viewModel.onAcceptContactRequest(contact.user_key.toString())
        //      mFriendsFragment.contactRequestAccept(contact)
    }

    override fun onContactListAdded(
        snapshot: DataSnapshot, previousChildName: String?
    ) {
        val data = contactListAdapter.getData()
        val newList = snapshot.getValue(ContactGroup::class.java)!!
        newList.list_key = snapshot.key.toString()
        if (!data.contains(newList)) {
            data.add(0, newList)
            contactListAdapter.notifyItemInserted(0)
        }

        val auxList = ContactGroup()
        auxList.list_key = ""
        auxList.list_name = "new Contact List"
        if (!data.contains(auxList)) {
            data.add(auxList)
            contactListAdapter.notifyItemInserted(data.size - 1)
        }
    }

    override fun onContactListChanged(
        snapshot: DataSnapshot, previousChildName: String?
    ) {
        val data = contactListAdapter.getData()
        val existingList = snapshot.getValue(ContactGroup::class.java)!!
        existingList.list_key = snapshot.key.toString()
        val index = data.indexOf(existingList)
        if (index > -1) {
            data[index] = existingList
            contactListAdapter.notifyItemChanged(index)
        }

    }

    override fun onContactListRemoved(snapshot: DataSnapshot) {
        val data = contactListAdapter.getData()
        val existingList = snapshot.getValue(ContactGroup::class.java)!!
        existingList.list_key = snapshot.key.toString()
        val index = data.indexOf(existingList)
        if (index > -1) {
            data.removeAt(index)
            contactListAdapter.notifyItemRemoved(index)
        }
    }


    internal class IncomingMessageHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i("LocationUpdatesComponent", "handleMessage...$msg")
            super.handleMessage(msg)
            when (msg.what) {
                LocationUpdatesService.LOCATION_MESSAGE -> {
                    val obj: Location = msg.obj as Location
                    val currentDateTimeString: String =
                        DateFormat.getDateTimeInstance().format(Date())

                    /*
                                        locationMsg.setText(
                                            """LAT :  ${obj.getLatitude().toString()}
                    LNG : ${obj.getLongitude().toString()}

                    ${obj.toString().toString()}


                    Last updated- $currentDateTimeString"""
                                        )
                                        */
                }
            }
        }
    }
    /*
        private fun updateUnreadMessagesIndicator(map: HashMap<String, UnreadMessages>) {/*
                        if (map.size == 0) {
                            binding.chatIndicator.visibility = GONE
                        } else {
                */
            var unreadMessagesQty: Long = 0
            map.forEach { room ->
                /*
                  if (!mMapFragment.isVisible || !mMapFragment.chatFragment?.isVisible!!) {
                      unreadMessagesQty += room.value.qty
                  }

                 */
            }
            appToolbar.updateChatCount(unreadMessagesQty.toInt())
        }

    */
    //  }

    override fun recordVideo() {
        if (Build.VERSION.SDK_INT <= 32) {
            toTakeVideoPermissionsRequest?.launch(arrayOf(READ_EXTERNAL_STORAGE))
        } else {
            toTakeVideoPermissionsRequest?.launch(arrayOf(READ_MEDIA_VIDEO))
        }
    }

    override fun pickImage() {
        toPickImagePermissionsRequest?.launch(arrayOf(READ_EXTERNAL_STORAGE))
    }

    override fun takePicture() {

    }


    override fun isPanicButtonActive(): Boolean {
        var panicEventExists = false

        if (viewModel.getEvents() != null) {
            viewModel.getEvents()?.forEach { event ->
                if (event.author.author_key == SessionForProfile.getInstance(this)
                        .getUserId() && event.event_type == EventTypesEnum.PANIC_BUTTON.name && (event.visibility != EventVisibilityTypes.HIDDEN_FOR_AUTHOR.name)
                ) {
                    panicEventExists = true
                    return@forEach
                }
            }
        }
        return panicEventExists
    }


    override fun getEventsFollowed(): ArrayList<EventFollowed> {
        return viewModel.getEvents() ?: ArrayList<EventFollowed>()
    }


    fun hideFooterToolBar() {
        binding.bottomToolbar.visibility = GONE
    }

    fun showFooterToolBar() {
        binding.bottomToolbar.visibility = VISIBLE
    }


    fun showTitleBar() {
        binding.includeCustomToolbar.toolbarRootLayout.visibility = VISIBLE

        binding.toolbar.visibility = VISIBLE
    }

    fun hideTitleBar() {
        binding.includeCustomToolbar.toolbarRootLayout.visibility = GONE
        binding.toolbar.visibility = GONE
    }

    companion object {
        var sIsShown: Boolean = false
    }

    //--------------------- AUDIO SECTION
    private fun recordStart() {
        val directory = File(filesDir.path.toString() + "/audios/")
        if (!directory.exists()) {
            directory.mkdir()
        }
        val recordingFilename = directory.path + UUID.randomUUID().toString() + ".3gp"
        viewModel.setAudioRecordingFileName(recordingFilename)
        recordSession = MultimediaUtils(this).startRecording(this, recordingFilename)!!

        playSound(R.raw.recording_start, null, null)
    }


    private fun recordStop() {
        try {
            playSound(R.raw.recording_stop, null, null)
            val recordingFilename: String = viewModel._recordFile.toString()
            MultimediaUtils(this).stopRecording(recordSession, recordingFilename)
            Toast.makeText(this, "Recorded.!", Toast.LENGTH_SHORT).show()

            FileUtils().copyFile(
                recordingFilename.substringBeforeLast("/"),
                recordingFilename.getJustFileName(),
                appsInternalStorageFolder().path.toString()
            )

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(recordingFilename)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration

            val media = MediaFile(MediaTypesEnum.AUDIO, recordingFilename, duration)
            viewModel.onMediaPicked(media)
            recordSession = null


        } catch (ex: Exception) {
            recordSession = null
            showErrorDialog(ex.localizedMessage.toString())
        }
    }


    private fun disposeRecording() {
        try {
            playSound(R.raw.recording_stop, null, null)
            //Do Nothing
            val recordingFilename: String = viewModel._recordFile.toString()
            MultimediaUtils(this).stopRecording(recordSession, recordingFilename)
            Toast.makeText(this, "Disposed.!", Toast.LENGTH_SHORT).show()
            FileUtils().deleteFile(recordingFilename.substringBeforeLast("/"))
            recordSession = null
        } catch (ex: Exception) {
            recordSession = null
        }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        //     TODO("Not yet implemented")
    }


    var isOpened = false

    fun setListnerToRootView() {
        var activityRootView: View = binding.root

        //getWindow().getDecorView().findViewById(android.R.id.content)

        val screenHeight = window.decorView.context.resources.displayMetrics.heightPixels

        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                var r = Rect()
                activityRootView.getGlobalVisibleRect(r)
                /*
                                var bottomToolbarRect = Rect()
                                binding.bottomToolbar.getGlobalVisibleRect(bottomToolbarRect)
                */
                viewModel.setScreenSize(r.width(), r.height())
                /*
                                if (r.height() != bottomToolbarRect.bottom) { // 99% of the time the height diff will be due to a keyboard.
                                    Toast.makeText(this@MainActivity, "Keyboard Open", Toast.LENGTH_SHORT).show()
                                    if (!viewModel.isKeyboardOpen()) {
                                        viewModel.onKeyboardOpened()
                                    }
                                } else {
                                    viewModel.onKeyboardClosed()
                                    Toast.makeText(this@MainActivity, "Keyboard Closed", Toast.LENGTH_SHORT).show()
                                }
                                binding.navHostFragment
                */
            }
        })

    }

    fun getLayoutView(id: Int): View? {
        return binding.root.findViewById(id)
    }


    /*
    fun removeUnreadMessagesByRoomKey(chatRoomKey: String) {
        var unreadsLiveData = AppClass.instance.getUnreadMessagesExcludingSomeRoomKey(chatRoomKey)
        var unreadsList = unreadsLiveData.value
        var unreadMessages = 0
        unreadsList?.forEach { record ->
            unreadMessages = (unreadMessages + record.qty).toInt()
        }
        //   withContext(Dispatchers.Main) {

        appToolbar.updateChatCount(unreadMessages)


    }
*/
    fun adjustNavHostFragmentToLimits() {
        // binding.toolbar.height
    }


    private val _appStatus = MutableLiveData<MainActivityViewModel.AppStatus?>()
    val appStatus: LiveData<MainActivityViewModel.AppStatus?> = _appStatus

    /**
     * Set the app status
     */
    fun setAppStatus(status: MainActivityViewModel.AppStatus) {

        viewModel.setAppStatus(status)
    }

    fun checkLocationEnabled() {
        val locationManager =
            getApplication().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        _isLocationEnabled.postValue(isEnabled)
    }


    fun restoreNavigationFragment() {
        val layoutParams = binding.navHostFragment.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomToTop = R.id.bottom_toolbar
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.topToBottom = R.id.include_custom_toolbar
        layoutParams.topToTop = -1
        binding.navHostFragment.layoutParams = layoutParams
    }

    fun expandNavigationFragmentToTop() {
        val layoutParams = binding.navHostFragment.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        binding.navHostFragment.layoutParams = layoutParams
    }


    ///-----------------------Manejo de mensajes y notificaciones.
    fun handleForegroundMessage(messageInfo: Bundle) {
        // Implementa la lógica para manejar el mensaje cuando la aplicación está en primer plano
        // Puedes optar por mostrar una notificación personalizada o actualizar la interfaz de usuario directamente.

        // Check if message contains a data payload.
        val navController = findNavController(R.id.nav_host_fragment)


        var showPushPopup = true

        val notificationType = messageInfo.getString("notification_type")!!

        val eventKey = messageInfo.getString("eventKey")
        var isAMessageInCurrentEvent: Boolean = false
        var iAMInNotificationsFragment: Boolean = false

        eventKey?.let {
            // Si llega un mensaje de un evento, estoy en el mapa y el evento es el mismo que el mensaje, no muestro el popup
            isAMessageInCurrentEvent =
                notificationType.equals(EventNotificationType.NOTIFICATION_TYPE_MESSAGE.toString()) &&
                        navController.currentDestination?.id == R.id.mapSituationFragment &&
                        (MapSituationFragmentViewModel.getInstance().auxEventKey.value
                            ?: "").equals(eventKey)

        }
        // Si estoy en el fragmento de notificaciones, no muestro el popup
        iAMInNotificationsFragment =
            navController.currentDestination?.id == R.id.notificationsFragment
        if (isAMessageInCurrentEvent || iAMInNotificationsFragment) {
            showPushPopup = false
        }

        if (showPushPopup) {
            Log.d("PUSH_MESSAGE_SERVICE", messageInfo.toString())

            //      if (remoteMessage.data.isNotEmpty()) {
            var title = ""
            var body = ""
            if (messageInfo.getString("title") != null) {
                title = messageInfo.getString("title")!!
            } else {
                try {
                    title = this.getString(
                        this.resources.getIdentifier(
                            messageInfo.getString("titleLocKey"),
                            "string",
                            this.packageName
                        )
                    )
                } catch (ex: Exception) {
                    throw ex
                }
            }

            if (messageInfo.getString("body") != null) {
                body = messageInfo.getString("body")!!
            } else {
                try {
                    body = this.getString(
                        this.resources.getIdentifier(
                            messageInfo.getString("bodyLocKey"),
                            "string",
                            this.packageName
                        )
                    )

                } catch (ex: Exception) {
                    throw ex
                }
                if (messageInfo.getStringArray("bodyLocArgs") != null) {
                    val messageArgs = messageInfo.getStringArray("bodyLocArgs")
                    var values = arrayOf(messageArgs)
                    var counter = 1
                    var auxBody = body
                    messageArgs?.forEach { value ->
                        auxBody =
                            auxBody.replace("%" + counter.toString() + "$" + "s", value.toString())
                        counter++
                    }
                    body = auxBody
                }

            }

            val bundle = Bundle()
            if (bundle.getString("image") != null) {
                bundle.putString("image", bundle.getString("image"))
            }

            when (notificationType) {
                EventNotificationType.NOTIFICATION_TYPE_MESSAGE.toString() -> {
                    var showNotification = true

                    AppClass.instance.getMainActivityRef()?.let { mainActivity ->

                        if (mainActivity.currentModuleIndex == IANModulesEnum.EVENTS_TRACKING.ordinal) {
                            var currentEventKey =
                                mainActivity.mMapFragment.getEventKey().toString()
                            if (currentEventKey == messageInfo.getString("eventKey")) {
                                showNotification = false
                            }

                        }

                    }

                    // Reviso si el mensaje ya fue leido
                    if (showNotification) {
                        val broadcastIntent =
                            Intent(this, MainActivity::class.java)

                        broadcastIntent.putExtra(
                            EventNotificationType.NOTIFICATION_TYPE_MESSAGE.toString(),
                            "notification_type",
                        )

                        broadcastIntent.putExtra(
                            "display_name",
                            bundle.getString("bodyLocArgs")?.get(0).toString()
                        )
                        broadcastIntent.putExtra(
                            "text",
                            bundle.getString("bodyLocArgs")?.get(1).toString()
                        )

                        val pendingIntent = PendingIntent.getBroadcast(
                            this,
                            0,
                            broadcastIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        /*
                                                NotificationsApi.getInstance(AppClass.instance)
                                                    .showNotification(title, body, Bundle(), pendingIntent)
                        */
                    }


                }

                EventNotificationType.NOTIFICATION_TYPE_EVENT_NOTIFICATION.name.toString() -> {
                    var eventType: String = bundle.getString("event_type").toString()
                    var eventKey = messageInfo.getString("event_key").toString()

                    when (eventType) {
                        EventTypesEnum.PANIC_BUTTON.name -> {
                            var coco = 3
                        }
                    }

                }

                EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.toString() -> {

                    val broadcastIntent =
                        Intent(EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.toString())
                    broadcastIntent.putExtra(
                        "display_name", bundle.getString("user_name")
                    )

                    broadcastMessage(messageInfo, "action_type")

                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    NotificationsApi.getInstance(AppClass.instance)
                        .showNotification(title, body, Bundle(), pendingIntent)

                }

                AppConstants.NOTIFICATION_TYPE_PULSE_REQUESTED -> {

                }

                EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString() -> {
                    val broadcastIntent =
                        Intent(EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString())
                    broadcastIntent.putExtra(
                        "display_name", bundle.getString("user_name")
                    )
                    broadcastIntent.putExtra(
                        "remaining_time", bundle.getString("remaining_time").toString().toLong()
                    )

                    val pendingIntent = PendingIntent.getBroadcast(
                        this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    NotificationsApi.getInstance(AppClass.instance)
                        .showNotification(title, body, Bundle(), pendingIntent)
                }
            }

            //           }
            var pp = 32
            when (notificationType) {
                AppConstants.NOTIFICATION_TYPE_ARE_YOU_ON_DESTINATION_REQUESTED -> {
                    val broadcastIntent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    broadcastIntent.action = AppConstants.BROADCAST_DID_YOU_ARRIVE_REQUEST
                    broadcastIntent.putExtra(
                        "event_key", bundle.getString("event_key").toString()
                    )
                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        broadcastIntent
                    )
                }

                AppConstants.NOTIFICATION_TYPE_PULSE_REQUESTED -> {
                    val intent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    intent.action = AppConstants.BROADCAST_PULSE_REQUIRED
                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        intent
                    )
                }

                EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString() -> {
                    val intent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    intent.action = AppConstants.BROADCAST_EVENT_CLOSE_TO_EXPIRE
                    intent.putExtra("event_key", bundle.getString("event_key"))
                    intent.putExtra("display_name", bundle.getString("user_name"))
                    intent.putExtra(
                        "remaining_time", bundle.getString("remaining_time").toString().toLong()
                    )

                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        intent
                    )
                }


            }

            NotificationsApi.getInstance(this).showNotification(title, body, bundle, null)
        }

        //       }
        /*
                // Check if message contains a notification payload.
                bundle.getString("").let {
                    Log.d(TAG, "Message Notification Body: ${it.body}")
                }
        */

    }

}
