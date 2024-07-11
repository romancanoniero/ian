package com.iyr.ian.ui.splash

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySplashBinding
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.ScreensEnum
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.IAuthentication
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.preloader.PreLoaderActivity
import com.iyr.ian.ui.setup.SetupActivity
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.setup.pin_setup.PinSetupActivity
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivity
import com.iyr.ian.ui.setup.speed_dial_setup.SpeedDialSetupActivity
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.utils.NotificationsUtils
import com.iyr.ian.utils.UIUtils.getDensityName
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.connectivity.NetworkStatusHelper
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideStatusBar
import com.iyr.ian.utils.isAndroidTV
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.permissions.PermissionsEnablingDialog
import com.iyr.ian.utils.permissions.PermissionsRationaleDialog
import com.iyr.ian.utils.permissions.RationaleDialogCallback
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.storeInSharedPreferencesMessages


interface OnLoginCallback {
    fun onLoggedIn()
    fun onNotLogged()
    fun onEmailNotVerified()
}


interface SplashActivityCallback {
//    abstract val PreLoaderActivity: Unit

    fun onOkToMainScreen(user: User)
    fun onProfileIncomplete(user: User)
    fun onNotLogged()
    fun onUserDoesNotExistsAnymore() // if the user exists in the pass but was deleted
    fun onError(exception: Exception)
    // fun onSignupByEmailLinkComplete()
}

class SplashActivity : AppCompatActivity(), SplashActivityCallback, IAuthentication {

    private var networkObserver: NetworkStatusHelper? = null
    private var hasConnectivity: Boolean = true
    private lateinit var binding: ActivitySplashBinding

    // private lateinit var mPresenter: SplashPresenter
    private lateinit var mContext: Context
    private lateinit var analytics: FirebaseAnalytics

    private lateinit var viewModel: SplashScreenViewModel

    var loader = LoadingDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //   setupSystemConfigs()


        Log.d("PUSH_MESSAGE_SPLASH", "entro por SplashActivity")
        intent.extras?.let { extras ->
            Log.d("PUSH_MESSAGE_SPLASH", "entro por SplashActivity con extras")

            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(
                    "PUSH_MESSAGE_SPLASH",
                    "informacion recibida =" + String.format(
                        "%s %s (%s)",
                        key,
                        value?.toString(),
                        value?.javaClass?.name
                    )
                )
            }

            var pushNotificationInfo = NotificationsUtils.PushNotification()
            pushNotificationInfo.action = extras.getString("action") ?: "????"
            pushNotificationInfo.notificationType = extras.getString("notification_type") ?: "????"
            pushNotificationInfo.linkKey = extras.getString("eventKey") ?: "????"

            /*
                        var prefs : HashMap<String, NotificationsUtils.PushNotification> = getSharePreferencesMessages("notifications") as HashMap<String, NotificationsUtils.PushNotification>

                        if (prefs==null)
                        {
                            prefs = HashMap<String, NotificationsUtils.PushNotification>()
                        }
                        prefs.put(System.currentTimeMillis().toString(), pushNotificationInfo)

                        Log.d("PUSH_NOTIFICATIONS", prefs.toString())
            */

            storeInSharedPreferencesMessages("notifications", pushNotificationInfo)
        }





        Log.d("SCREEN_DENSITY", getDensityName(this).toString())

        // viewModel = ViewModelProvider(this).get(SplashScreenViewModel::class.java)


        var messagingToken: String? = SessionApp.getInstance(applicationContext).messagingToken
        viewModel = SplashScreenViewModel(messagingToken)

        analytics = Firebase.analytics

        hideStatusBar()
        /*
                binding = ActivitySplashBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setupUI()
        */
        mContext = this
        var senderId: String
        var linkKey: String
        var action: String

        //  mPresenter = SplashPresenter(this as AppCompatActivity, this)

        if (isAndroidTV()) {
            val nextIntent = Intent(this@SplashActivity, LoginActivity::class.java)
            nextIntent.action = intent.action
            intent.extras?.let { info ->

                nextIntent.putExtras(info)
            }
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(nextIntent)
        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) {
                var pepe = 22
            }




        when (intent.action) {
            "android.intent.action.MAIN" -> {

            }

            "ACTION_SHOW_EXTEND_TIME_DIALOG" -> {
                //         checkIfLogged()
            }

        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObservers()

        setupUI()


    }

    private fun setupObservers() {

        viewModel.userRegistrationStatus.observe(this, Observer { response ->
            when (response) {
                is Resource.Loading -> loader.show(supportFragmentManager, "loader_frames")
                is Resource.Success -> {
                    if (loader.isVisible)
                        loader.dismiss()

                    if (response.data != null) {
                        onUserAuthenticated(response.data)
                    } else {
                        showErrorDialog("Usuario Inexistente")
                    }
                }

                is Resource.Error -> {
                    loader.dismiss()
                    Toast.makeText(applicationContext, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.screenToShow.observe(this, Observer {
            loader.dismiss()
            when (it) {
                ScreensEnum.Preloader -> {
                    goToPreloader()
                }

                ScreensEnum.Main -> {

                    // reestablecer
                    viewModel.user.value?.let { user -> onOkToMainScreen(user) }

                    /*
                                        var user = viewModel.user.value!!
                                        val bundle = Bundle()
                                        bundle.putString("data_object", Gson().toJson(user))
                                        bundle.putBoolean(
                                            "first_setup",
                                            true
                                        )
                                        var intent: Intent? = null


                                        intent = Intent(this@SplashActivity, AddContactsFromPhoneActivity::class.java)


                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                        startActivity(intent)
                    */

                }

                ScreensEnum.Login -> {
                    goToLogin()
                }

                ScreensEnum.Register -> {
                }
                /*
                                ScreensEnum.setupActivity -> {
                                    var user = viewModel.user.value!!
                                    onProfileIncomplete(user)
                                }
                */
                /*
                                else -> {

                                }

                 */
                ScreensEnum.ForgotPassword -> TODO()
                ScreensEnum.setupActivity, ScreensEnum.Press_or_Tap_Setup_Activity, ScreensEnum.Pin_Setup_Activity,
                ScreensEnum.Speed_Dial_Setup_Activity, ScreensEnum.Check_Permissions -> {
                    var user = viewModel.user.value!!
                    val bundle = Bundle()
                    bundle.putString("data_object", Gson().toJson(user))
                    bundle.putBoolean(
                        "first_setup",
                        true
                    )
                    var intent: Intent? = null


                    var newClass: Class<*>? = null

//                    Intent(this@SplashActivity, newClass as Class<*>)

                    intent =
                        if (user.display_name.isNullOrBlank() || (user.telephone_number.isNullOrEmpty() && user.email_address.isEmpty())) {
                            /*
                                                        newClass = Class.forName("SetupActivity")?.newInstance() as Class<*>?
                                                        Intent(this@SplashActivity, newClass)*/
                            Intent(this@SplashActivity, SetupActivity::class.java)
                        } else if (user.sos_invocation_count == 0 || user.sos_invocation_method.isNullOrEmpty()) {
                            /*
                            newClass =
                                Class.forName("PressOrTapSetupActivity")?.newInstance() as Class<*>?
                            Intent(this@SplashActivity, PressOrTapSetupActivity::class.java)
                            */
                            Intent(this@SplashActivity, PressOrTapSetupActivity::class.java)
                        } else if (user.security_code.isBlank()) {
//                            newClass = Class.forName("PinSetupActivity")?.newInstance() as Class<*>?
                            Intent(this@SplashActivity, PinSetupActivity::class.java)
                            //Intent(this@SplashActivity, PinSetupActivity::class.java)
                        } else if (user.allow_speed_dial == null) {
                            /*
                                                        newClass =
                                                            Class.forName("SpeedDialSetupActivity")?.newInstance() as Class<*>?
                                                        Intent(this@SplashActivity, newClass)
                            */
                            Intent(this@SplashActivity, SpeedDialSetupActivity::class.java)
                        } else if (user.sos_invocation_count < 3) {
                            /*
                              newClass =
                                  Class.forName("PressOrTapSetupActivity")?.newInstance() as Class<*>?
                              Intent(this@SplashActivity, newClass)
                            */

                            Intent(this@SplashActivity, PressOrTapSetupActivity::class.java)
                        } else if (!areLocationPermissionsGranted()) { // agregar la validacion si ya decidio compartir o no la ubicacion
                            /*
                            newClass = Class.forName("LocationRequiredActivity")
                                ?.newInstance() as Class<*>?
                            Intent(this@SplashActivity, newClass)
                            */

                            Intent(this@SplashActivity, LocationRequiredActivity::class.java)
                        } else {
                            /*
                            newClass = Class.forName("AddContactsFromPhoneActivity")
                                ?.newInstance() as Class<*>?
                            Intent(this@SplashActivity, newClass)
*/
                            Intent(this@SplashActivity, AddContactsFromPhoneActivity::class.java)
                        }

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }

                //------------------------


                //-------------------


            }
        })

        viewModel.totalTasks.observe(this) {
            if (it != null) {
                if (binding.progress.visibility == View.GONE)
                    binding.progress.visibility = View.VISIBLE

                binding.progress.max = it!!
            }
        }

        viewModel.progress.observe(this) {
            if (it != null) {
                if (binding.progress.visibility == View.GONE)
                    binding.progress.visibility = View.VISIBLE
                binding.progress.progress = it!!
            }
        }

        viewModel.serverTime.observe(this) {
            if (it != null) {
                AppClass.instance.startTime = it
            }
        }

        viewModel.progressText.observe(this) { text ->
            if (text != null) {
                binding.progressDescription.visibility = View.VISIBLE
                binding.progressDescription.text = text
            } else {
                binding.progressDescription.visibility = View.GONE
            }
        }


        viewModel.readyToStart.observe(this) {
            if (it != null) {
                val intent = Intent(this, MainActivity::class.java)
                val bundle = Bundle()
                bundle.putString("user", Gson().toJson(it))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()


            }
        }
        //  viewModel.onAppStart()
    }

    override fun onResume() {
        super.onResume()
        registerObservers()
    }

    override fun onPause() {
        super.onPause()
        unregisterObservers()
    }

    private fun registerObservers() {
        /*
                networkObserver = NetworkStatusHelper(this)
                networkObserver?.observe(this) { status ->
                    when (status) {
                        NetworkStatus.Available -> {
                            if (!hasConnectivity) {
                                hasConnectivity = true
                                showSnackBar(binding.root, "Hay conectividad")
                            }
                        }

                        NetworkStatus.Unavailable -> {
                            if (hasConnectivity) {
                                hasConnectivity = false
                                showSnackBar(binding.root, getString(R.string.no_connectivity))
                            }
                        }
                    }
                }
                */

    }

    private fun unregisterObservers() {
        networkObserver?.removeObserver { }
    }

    override fun Activity.goToMainScreen(user: User) {
        // aqui se implementa la carga de las tablas necesarias para el funcionamiento de la aplicacion
        // y se llama a la pantalla principal
        viewModel.prepareForMainScreen(user)


        //
    }

    /*
            private fun goToCompleteProfile(user: User) {
                val bundle = Bundle()
                bundle.putString("data_object", Gson().toJson(user))
                bundle.putBoolean("first_setup", true)
                startActivity(SetupActivity::class.java, bundle)
            }
        */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var grantResults = grantResults
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val _grantResults = IntArray(3)
                _grantResults[0] = grantResults[0]
                _grantResults[1] = grantResults[1]
                _grantResults[2] = PackageManager.PERMISSION_GRANTED
                grantResults = _grantResults
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                //    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                //    retrieveUTCTimeAndHangleLogin()

                if (SessionForProfile.getInstance(this)
                        .getProfileProperty("RTLocationEnabled") as Boolean
                ) {
                    goToMainScreen()
                }


            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    val callback: RationaleDialogCallback = object : RationaleDialogCallback {
                        override fun onTryAgain() {

                        }

                        override fun onDeny() {
                            SessionForProfile.getInstance(this@SplashActivity)
                                .setProfileProperty("RTLocationEnabled", false)
                            goToMainScreen()

                        }
                    }

                    val mRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PermissionsRationaleDialog(
                            this,
                            this,
                            R.string.rationale_pemission_location,
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ),
                            callback,
                            Constants.LOCATION_REQUEST_CODE
                        )
                    } else {
                        PermissionsRationaleDialog(
                            this,
                            this,
                            R.string.rationale_pemission_location,
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            callback,
                            Constants.LOCATION_REQUEST_CODE
                        )
                    }
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
        }

    }

    /*
        private fun handleIntentData() {
            Log.d("EMAIL_REGISTRATION", "vERIFICANDO SI MANDA LINK")
            val emailLink = intent.data.toString()
            if (emailLink != null && !emailLink.isEmpty()) {
                Log.d("EMAIL_REGISTRATION", emailLink)
                //         Toast.makeText(this, emailLink, Toast.LENGTH_LONG).show()
                val email: String =
                    SessionForProfile.getInstance(this).getProfileProperty("PENDING_EMAIL").toString()
                mPresenter.signInWithEmailLink(email, emailLink)
            }
        }
    */
    private fun handleLinks(callback: OnCompleteCallback) {
        var senderId: String
        var linkKey: String
        var action: String
        //     Toast.makeText(this, " handleLinks ", Toast.LENGTH_LONG).show()


        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                Log.i("DYNAMIC_LINKS", intent.extras.toString())
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    senderId = deepLink?.getQueryParameter("sender_id").toString()
                    intent.putExtra("sender_id", senderId)

                    //                 Toast.makeText(this, "ref_user = " + senderId, Toast.LENGTH_LONG).show()
                } else {
                    //          Toast.makeText(this, "no tiene recomendacion ", Toast.LENGTH_LONG).show()

                }
                callback.onComplete(true, null)
            }
            .addOnFailureListener(
                this


            ) {
                Toast.makeText(
                    this@SplashActivity,
                    it.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()


            }

    }


    private fun setupUI() {
        val animation = findViewById<LottieAnimationView>(R.id.animation)
        animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                playSound(R.raw.intro_bell, null, null)
            }

            override fun onAnimationEnd(p0: Animator) {
                viewModel.onAppStart()
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}
        })


        //     setupSystemConfigs()
    }

    private fun goToPreloader() {
        val intent = Intent(this@SplashActivity, PreLoaderActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onNotLogged() {
        goToLogin()
    }

    override fun onUserDoesNotExistsAnymore() {
        SessionForProfile.getInstance(this@SplashActivity).logout()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    override fun onOkToMainScreen(user: User) {
        SessionForProfile.getInstance(applicationContext).storeUserProfile(user)
        AppClass.instance.core?.onOkToMainScreen(user, intent.extras)
        /*
                Log.d("INGRESO", "onOkToMainScreen - 1")
                NonUI(applicationContext)
                SessionForProfile.getInstance(applicationContext).storeUserProfile(user)
                // TODO SACAR ESTO y TOMARLO DESDE EL SERVIDOR
                // TODO -> arreglar esto del lado del servidor
                val auxSubscription = Subscription()
                auxSubscription.subscription_type_key = user.subscriptionTypeKey
                val dateSubscription = SimpleDateFormat("dd-mm-yyyy").parse("01-03-2022").time
                auxSubscription.subscripted_on = dateSubscription
                val dateExpiration = SimpleDateFormat("dd-mm-yyyy").parse("30-06-2022").time
                auxSubscription.expires_on = dateExpiration
                AppClass.instance.setCurrentSubscription(auxSubscription)
                val callback = object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            //        if (permissionsLocation() == true) {
                            Log.d("INGRESO", "onOkToMainScreen - 3")
                            goToMainScreen()
                        }
                    }
                }
                Log.d("INGRESO", "onOkToMainScreen - 2")
                AppClass.instance.fetchCurrentSubscriptionType(callback)
        */
//--------------------------------------------------


    }

    private fun goToMainScreen() {
        Log.d("INGRESO", "goToMainScreen - 1")
        /*

                UsersWSClient.instance.getAuthToken(object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            Log.d("INGRESO", "goToMainScreen - 2")

                            ApiNotifications.getInstance(AppClass.instance)
                                .getNotificationToken(object : OnCompleteCallback {
                                    override fun onComplete(success: Boolean, result: Any?) {
                                        if (success) {
                                            Log.d("INGRESO", "goToMainScreen - 3")

                                            val notificationToken = result as String
                                            SystemWSClient.instance.updateNotificationToken(
                                                notificationToken,
                                                object : OnCompleteCallback {
                                                    override fun onComplete(
                                                        success: Boolean,
                                                        result: Any?
                                                    ) {


                                                    }
                                                })
                                        }
                                    }
                                })

                            Log.d("INGRESO", "goToMainScreen - 4")

                            prepareForMainScreen()
                            Log.d("INGRESO", "goToMainScreen - 5")



                            broadcastMessage(intent.extras, BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY)
                            /*
                                val nextIntent = Intent(this@SplashActivity, MainActivity::class.java)
                                nextIntent.action = intent.action
                                intent.extras?.let { info ->
                                    nextIntent.putExtras(info)
                                }
                                nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                Log.d("INGRESO", "Va a llamar a MainActity")
                                startActivity(nextIntent)
            */

                        }
                    }
                })

            */
    }

    private fun prepareForMainScreen() {

        AppClass.instance.startLocationServices()

        val fallingSensor = SessionForProfile.getInstance(AppClass.instance)
            .getProfileProperty("falling_sensor", false) as Boolean
        if (fallingSensor) {
            AppClass.instance.enableFallingSensor()
        }
    }

    override fun onProfileIncomplete(user: User) {

        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup",
            true
        )
        var intent: Intent? = null

//        intent = Intent(this@SplashActivity, SetupActivity::class.java)

        var newClass: Class<*>? = null

        intent =
            if (user.display_name.isNullOrBlank() || (user.telephone_number.isNullOrEmpty() && user.email_address.isEmpty())) {
                newClass = Class.forName("SetupActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)
                //    Intent(this@SplashActivity, SetupActivity::class.java)
            } else if (user.sos_invocation_count == 0 || user.sos_invocation_method.isNullOrEmpty()) {
                newClass = Class.forName("PressOrTapSetupActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)
                //           Intent(this@SplashActivity, PressOrTapSetupActivity::class.java)
            } else if (user.security_code.isBlank()) {
                newClass = Class.forName("PinSetupActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)
//                Intent(this@SplashActivity, PinSetupActivity::class.java)
            } else if (user.allow_speed_dial == null) {
                newClass = Class.forName("SpeedDialSetupActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)
                //                Intent(this@SplashActivity, SpeedDialSetupActivity::class.java)
            } else if (user.sos_invocation_count == null || user.sos_invocation_count < 3) {
                newClass = Class.forName("PressOrTapSetupActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)
//                Intent(this@SplashActivity, PressOrTapSetupActivity::class.java)
            } else if (!areLocationPermissionsGranted()) {
                newClass = Class.forName("LocationRequiredActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)

                //                Intent(this@SplashActivity, LocationRequiredActivity::class.java)
            } else {
                newClass = Class.forName("AddContactsFromPhoneActivity")?.newInstance() as Class<*>?
                Intent(this@SplashActivity, newClass)

                //                Intent(this@SplashActivity, AddContactsFromPhoneActivity::class.java)
            }


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onError(exception: Exception) {

        hideLoader()
        var dialogCallback: View.OnClickListener? = null
        var errorMessage = ""
        if (exception is FirebaseAuthActionCodeException) {
            when (exception.errorCode) {
                "ERROR_INVALID_ACTION_CODE" -> {
                    errorMessage = getString(R.string.error_signin_by_email_link_invalid_link)
                    dialogCallback =
                        object : View.OnClickListener {
                            override fun onClick(p0: View?) {

                                var newClass = Class.forName("AddContactsFromPhoneActivity")
                                    ?.newInstance() as Class<*>?
                                val intent = Intent(this@SplashActivity, newClass)
                                //    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                        }
                }
            }
        }
        showErrorDialog(errorMessage, dialogCallback)
    }

    private fun goToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
    /*
        override fun onSignupByEmailLinkComplete() {
            mPresenter.onUserExistsCheckLoginFlow(FirebaseAuth.getInstance().uid.toString())
        }
    */

    private fun setupSystemConfigs() {


        val freePlan = SubscriptionTypes()
        freePlan.subscription_type_key =
            FirebaseDatabase.getInstance().getReference("subscription_types").push().key
        freePlan.internal_name = AccessLevelsEnum.FREE.name
        freePlan.work_on_location_permission_denied = false
        FirebaseDatabase.getInstance().getReference("subscription_types")
            .child(freePlan.subscription_type_key)
            .setValue(freePlan)


        val solidaryPlan = SubscriptionTypes()
        solidaryPlan.subscription_type_key =
            FirebaseDatabase.getInstance().getReference("subscription_types").push().key
        solidaryPlan.internal_name = AccessLevelsEnum.SOLIDARY.name
//        solidaryPlan.name_res_id = R.string.subscription_plan_solidary
        solidaryPlan.work_on_location_permission_denied = false
        FirebaseDatabase.getInstance().getReference("subscription_types")
            .child(solidaryPlan.subscription_type_key)
            .setValue(solidaryPlan)


        val premiumPlan = SubscriptionTypes()
        premiumPlan.subscription_type_key =
            FirebaseDatabase.getInstance().getReference("subscription_types").push().key
        premiumPlan.internal_name = AccessLevelsEnum.PREMIUM.name
        premiumPlan.work_on_location_permission_denied = true
        FirebaseDatabase.getInstance().getReference("subscription_types")
            .child(premiumPlan.subscription_type_key)
            .setValue(premiumPlan)

        // precios de los planes
        /*
                FirebaseDatabase.getInstance().getReference("subscription_plans").removeValue()
                    .addOnCompleteListener {
                        val subscriptionPlanFree = SubscriptionPlans()
                        subscriptionPlanFree.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanFree.subscription_type_key = freePlan.subscription_type_key
                        subscriptionPlanFree.description = "Gratis"
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanFree.plan_key).setValue(subscriptionPlanFree)


                        val subscriptionPlanSolidaryx1 = SubscriptionPlans()
                        subscriptionPlanSolidaryx1.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanSolidaryx1.subscription_type_key =
                            solidaryPlan.subscription_type_key
                        subscriptionPlanSolidaryx1.description = "Solidario por 1 Mes"
                        subscriptionPlanSolidaryx1.duration_in_months = 1
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanSolidaryx1.plan_key).setValue(subscriptionPlanSolidaryx1)
                        val subscriptionPlanSolidaryx3 = SubscriptionPlans()
                        subscriptionPlanSolidaryx3.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanSolidaryx3.subscription_type_key =
                            solidaryPlan.subscription_type_key
                        subscriptionPlanSolidaryx3.description = "Solidario por 3 meses"
                        subscriptionPlanSolidaryx1.duration_in_months = 3
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanSolidaryx3.plan_key).setValue(subscriptionPlanSolidaryx3)


                        val subscriptionPlanSolidaryx6 = SubscriptionPlans()
                        subscriptionPlanSolidaryx6.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanSolidaryx6.subscription_type_key =
                            solidaryPlan.subscription_type_key
                        subscriptionPlanSolidaryx6.description = "Solidario por 6 meses"
                        subscriptionPlanSolidaryx6.duration_in_months = 6
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanSolidaryx6.plan_key).setValue(subscriptionPlanSolidaryx6)

                        val subscriptionPlanSolidaryx12 = SubscriptionPlans()
                        subscriptionPlanSolidaryx12.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanSolidaryx12.subscription_type_key =
                            solidaryPlan.subscription_type_key
                        subscriptionPlanSolidaryx12.description = "Solidario por 1 año"
                        subscriptionPlanSolidaryx12.duration_in_months = 12
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanSolidaryx12.plan_key)
                            .setValue(subscriptionPlanSolidaryx12)
                        //-------------------
                        val subscriptionPlanNonDisturbx1 = SubscriptionPlans()
                        subscriptionPlanNonDisturbx1.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanNonDisturbx1.subscription_type_key =
                            premiumPlan.subscription_type_key
                        subscriptionPlanNonDisturbx1.description = "No molestar por 1 Mes"
                        subscriptionPlanNonDisturbx1.duration_in_months = 1
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanSolidaryx1.plan_key)
                            .setValue(subscriptionPlanNonDisturbx1)


                        val subscriptionPlanNonDisturbx3 = SubscriptionPlans()
                        subscriptionPlanNonDisturbx3.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanNonDisturbx3.subscription_type_key =
                            premiumPlan.subscription_type_key
                        subscriptionPlanNonDisturbx3.description = "No molestar 3 meses"
                        subscriptionPlanNonDisturbx1.duration_in_months = 3
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanNonDisturbx3.plan_key)
                            .setValue(subscriptionPlanNonDisturbx3)


                        val subscriptionPlanNonDisturbx6 = SubscriptionPlans()
                        subscriptionPlanNonDisturbx6.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanNonDisturbx6.subscription_type_key =
                            premiumPlan.subscription_type_key
                        subscriptionPlanNonDisturbx6.description = "No molestar por 6 meses"
                        subscriptionPlanNonDisturbx6.duration_in_months = 6
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanNonDisturbx6.plan_key)
                            .setValue(subscriptionPlanNonDisturbx6)

                        val subscriptionPlanNonDisturbx12 = SubscriptionPlans()
                        subscriptionPlanNonDisturbx12.plan_key =
                            FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS).push().key
                        subscriptionPlanNonDisturbx12.subscription_type_key =
                            premiumPlan.subscription_type_key
                        subscriptionPlanNonDisturbx12.description = "No molestar por 1 año"
                        subscriptionPlanNonDisturbx12.duration_in_months = 12
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_PLANS)
                            .child(subscriptionPlanNonDisturbx12.plan_key)
                            .setValue(subscriptionPlanNonDisturbx12)


                        val expiration_times = HashMap<String, Int>()

                        expiration_times[EventTypes.SEND_POLICE.name] = 180
                        expiration_times[EventTypes.SEND_FIREMAN.name] = 180
                        expiration_times[EventTypes.SEND_AMBULANCE.name] = 180
                        expiration_times[EventTypes.PERSECUTION.name] = 120
                        expiration_times[EventTypes.ROBBER_ALERT.name] = 180
                        expiration_times[EventTypes.KID_LOST.name] = 300
                        expiration_times[EventTypes.PET_LOST.name] = 420
                        expiration_times[EventTypes.PANIC_BUTTON.name] = 180
                        expiration_times[EventTypes.SCORT_ME.name] = 120
                        expiration_times[EventTypes.MECANICAL_AID.name] = 300
                        expiration_times[EventTypes.FALLING_ALARM.name] = 420

                        // configuro el sistema
                        val systemConfig = SystemConfig()
                        systemConfig.expiration_times = expiration_times
                        systemConfig.register_initial_plan_key = freePlan.subscription_type_key
                        systemConfig.register_initial_plan_duration = 90
                        FirebaseDatabase.getInstance().getReference(TABLE_CONFIG_SYSTEM)
                            .setValue(systemConfig)


                    }
        */
    }


}
