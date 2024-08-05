package com.iyr.ian.ui.otp


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.iyr.fewtouchs.ui.views.otp.OtpVerificationActivityViewModel
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityOtpVerificationBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.setup.SetupActivity
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showKeyboard
import com.iyr.ian.utils.showSnackBar
import java.util.concurrent.TimeUnit


class OtpVerificationFragment : Fragment(),
    NetworkStateReceiver.NetworkStateReceiverListener,
    OtpVerificationActivityCallback {

    private var storedVerificationId: String = ""
    private val viewModel: OtpVerificationActivityViewModel = OtpVerificationActivityViewModel()
    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean = true
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    //private lateinit var mPresenter: OtpVerificationPresenter
    private lateinit var binding: ActivityOtpVerificationBinding

    private val args: OtpVerificationFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startNetworkBroadcastReceiver(
            requireContext()
        )


        val action = arguments?.getString("action")
        resendToken = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("resend_token", PhoneAuthProvider.ForceResendingToken::class.java)
        } else {
            arguments?.getParcelable("resend_token")

        }
        storedVerificationId = arguments?.getString("storedVerificationId").toString()
        var phoneNumber = arguments?.getString("phone_number")

        if (resendToken == null) {
            requireActivity().showErrorDialog("Resend token is null")
        }

        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_otp_verification)

        val client = SmsRetriever.getClient(requireActivity() as MainActivity)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            // SMS Retriever ha comenzado a escuchar los mensajes SMS entrantes.
        }
        task.addOnFailureListener {
            // No se pudo iniciar SMS Retriever.
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        var iSMSRetriever = object : ISMSRetriever {
            override fun onSMSReceived(code: String) {
                // Extrae el código de verificación del mensaje SMS.
                binding.otpView.setText(code)
            }
        }

        requireContext().registerReceiver(MySMSRetriever(iSMSRetriever), intentFilter)
        setupUI()

    }


    private var originalObject: User? = null
    private lateinit var currentObject: User
    /*
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
    */


    override fun onResume() {
        registerNetworkBroadcastReceiver(requireContext())
        setupObservers()
        requireActivity().showKeyboard(binding.otpView)
        super.onResume()
    }


    override fun onPause() {
        unregisterNetworkBroadcastReceiver(requireContext())
        cancelObservers()
        super.onPause()
    }


    private fun setupObservers() {
        viewModel.currentUser.observe(this) { status ->
            when (status) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    SessionForProfile.getInstance(AppClass.instance)
                        .storeUserProfile(status.data!!)

                    if (status.data != null) {
                        when (status.data) {
                            is User -> {
                                val user: User = status.data

                                onUserAuthenticated(user)
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(status.message.toString())
                }

            }
        }
    }


    fun onUserAuthenticated(user: User) {

        val isUserCompleted =
            !user.display_name.isNullOrEmpty() && user.image.file_name != null && user.allow_speed_dial != null && user.sos_invocation_count != null && user.sos_invocation_count >= 3 && user.sos_invocation_method != null

        if (isUserCompleted) {
            goToMainScreen(user)
        } else {
            goToCompleteProfile(user)
        }
    }

    private fun goToCompleteProfile(user: User) {

        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup", true
        )

        var action: NavDirections? = null
        if (user.display_name.isNullOrBlank() || (user.telephone_number.isNullOrEmpty() && user.email_address.isEmpty())) {
            //Intent(this, SetupActivity::class.java)
            action = OtpVerificationFragmentDirections.actionOtpVerificationActivityToSetupFragment(
                bundle
            )
        } else if (user.sos_invocation_count == 0 || user.sos_invocation_method.isNullOrEmpty()) {
            //Intent(this, PressOrTapSetupActivity::class.java)
            action =
                OtpVerificationFragmentDirections.actionOtpVerificationActivityToPressOrTapSetupFragment(
                    bundle
                )
        } else if (user.security_code.isBlank()) {
            action =
                OtpVerificationFragmentDirections.actionOtpVerificationActivityToPinSetupFragment(
                    bundle
                )
        } else if (user.allow_speed_dial == null) {
            action =
                OtpVerificationFragmentDirections.actionOtpVerificationActivityToSpeedDialSetupFragment(
                    bundle
                )
        }/* else if (user.sos_invocation_count == null || user.sos_invocation_count < 3) {
                Intent(this, PressOrTapSetupActivity::class.java)
            }*/ else if (!requireActivity().areLocationPermissionsGranted()) {
//                Intent(this, LocationRequiredActivity::class.java)
//            destinationId = R.id.action_otpVerificationActivity_to_locationRequiredFragment

            action =
                OtpVerificationFragmentDirections.actionOtpVerificationActivityToLocationRequiredFragment(
                    bundle
                )
        } else {
//                Intent(this, AddContactsFromPhoneActivity::class.java)
//            destinationId = R.id.action_otpVerificationActivity_to_addContactsFromPhoneFragment
            action =
                OtpVerificationFragmentDirections.actionOtpVerificationActivityToAddContactsFromPhoneFragment(
                    bundle
                )
        }
        //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        findNavController().navigate(action)
    }

    private fun goToMainScreen(user: User) {

        val action =
            OtpVerificationFragmentDirections.actionOtpVerificationActivityToHomeFragment(user)

        findNavController().navigate(action)

        /*
                val intent = Intent(this, MainActivity::class.java)
                val bundle = Bundle()
                bundle.putString("user", Gson().toJson(user))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtras(bundle)
                startActivity(intent)
                //   finish()
        */
    }


    private fun cancelObservers() {
        viewModel.currentUser.removeObservers(this)
    }


    private fun setupUI() {


        binding.backArrows.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.otpView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remueve el listener para que no se llame más veces
                binding.otpView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val totalWidth = binding.otpView.width // obtén el ancho total disponible
                val spacing =
                    10 // define el espacio entre cada vista, ajusta este valor según tus necesidades

                // calcula el ancho disponible para cada vista, restando el espacio total (espacio * (número de vistas - 1))
                val viewWidth = (totalWidth - spacing * (6 - 1)) / 6
                binding.otpView.itemSpacing = spacing // establece el espacio entre cada vista
                binding.otpView.itemWidth = viewWidth // establece el ancho de cada vista
            }
        })
        /*
              val totalWidth = binding.otpView.width // obtén el ancho total disponible
              val spacing = 10 // define el espacio entre cada vista, ajusta este valor según tus necesidades

      // calcula el ancho disponible para cada vista, restando el espacio total (espacio * (número de vistas - 1))
              val viewWidth = (totalWidth - spacing * (6 - 1)) / 6

              binding.otpView.itemWidth = viewWidth // establece el ancho de cada vista
      // ahora puedes usar viewWidth para establecer el ancho de cada vista en tu otpView
      */
        binding.otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                updateUI()
            }
        })

        binding.resendLabel.setOnClickListener(View.OnClickListener {
            val phoneNumber: String = args.phoneNumber
                .toString()
            resendVerificationCode(phoneNumber, resendToken)
        })

        binding.verifyButton.setOnClickListener(View.OnClickListener {

            isBussy = true
            requireActivity().hideKeyboard(binding.otpView)
            if (hasConnectivity) {

                val verificationId: String = storedVerificationId
                val code: String = binding.otpView.text.toString()
                val credential = PhoneAuthProvider.getCredential(verificationId, code)


                val action =
                    OTPActionsEnum.valueOf(arguments?.getString("action").toString())
                //mPresenter.signInWithPhoneAuthCredential(credential, userDataMap)


                viewModel.onOTPIntroduced(
                    action,
                    credential,
                    storedVerificationId,
                    code
                )

            } else {
                requireActivity().showSnackBar(binding.root, getString(R.string.no_connectivity))
            }


        })

        updateUI()
    }

    private fun updateUI() {
        binding.verifyButton.isEnabled = binding.otpView.text?.length == 6
    }


    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {

        val phoneCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("PHONE NUMBER SIGNUP", "onVerificationCompleted:$credential")

                //   signInWithPhoneAuthCredential(credential)

            }


            override fun onVerificationFailed(p0: FirebaseException) {
                var pp = 333
            }

        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(phoneCallback) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    override fun onOkToMainScreen(user: User) {

        AppClass.instance.core?.onOkToMainScreen(user, args.toBundle())
        /*
        //        var bundle = Bundle()
        //        bundle.putString("data_object", Gson().toJson(user))
                hideLoader()
                SessionForProfile.getInstance(applicationContext).storeUserProfile(user)
                UsersWSClient.instance.getAuthToken(object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {

                            Toast.makeText(this@OtpVerificationActivity,"Enviar mensaje a un core para que haga lo siguiente",Toast.LENGTH_SHORT).show()
                            broadcastMessage(intent.extras,
                                AppConstants.ServiceCode.BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
                            )
                            /*
                            val intent = Intent(this@OtpVerificationActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
        */
                        }
                    }
                })

        //        startActivity(MainActivity::class.java, null)
                */
    }

    override fun onProfileIncomplete(user: User) {


        requireActivity().hideLoader()

        findNavController().navigate(
            OtpVerificationFragmentDirections.actionOtpVerificationActivityToSetupFragment(
                args.toBundle()
            )
        )
        /*
        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup",
            true
        )
        var intent = Intent(this, SetupActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        */
    }


    override fun goToCompleteUser(user: User) {

        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup",
            true
        )
        val intent = Intent(requireContext(), SetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)


    }

    override fun loginSuccessfully(user: User) {

        /*
        if (intent.hasExtra("action")) {
            when (intent.getStringExtra("action")) {
                OTP_ACTION_SIGNING -> {



                }
            }
        }

        LoginApi.getInstance(applicationContext).setLanguage(LoginStatus.LOGGED, intent.extras)
*/
        //     LoginApi.getInstance(applicationContext).setLanguage(LoginStatus.LOGGED, intent.extras)
        goToCompleteUser(user)
    }
    /*
        override fun showProgress() {
            TODO("Not yet implemented")
        }
    */


    override fun showError(exception: Exception) {
        requireActivity().hideLoader()
        var message = exception.localizedMessage.toString()
        if (exception is FirebaseAuthInvalidCredentialsException) {
            when (exception.errorCode) {
                "ERROR_INVALID_VERIFICATION_CODE" -> {
                    message = getString(R.string.error_telephone_verification_code_invalid)
                }
            }
        }
        /*
                when (errorCode) {
                    "ERROR_INVALID_VERIFICATION_CODE" -> {
                        mCallback.showError(mActivity.getString(R.string.error_invalid_verication_code))
                    }
                }
        */



        requireActivity().showErrorDialog(message,
            View.OnClickListener {
                binding.verifyButton.isEnabled = true
            })

    }


    //------------------- networkStatus
    override fun networkAvailable() {
        hasConnectivity = true

    }

    override fun networkUnavailable() {
        hasConnectivity = false

    }


    private fun startNetworkBroadcastReceiver(currentContext: Context) {
        networkStateReceiver = NetworkStateReceiver()
        networkStateReceiver.addListener(this)
        registerNetworkBroadcastReceiver(currentContext)
    }

    /**
     * Register the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun registerNetworkBroadcastReceiver(currentContext: Context) {

        Log.d("NetworkBroadcasReceiver - register", this.javaClass.name)
        currentContext.registerReceiver(
            networkStateReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    /**
     * Unregister the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun unregisterNetworkBroadcastReceiver(currentContext: Context) {
        Log.d("NetworkBroadcasReceiver - unregister", this.javaClass.name)
        currentContext.unregisterReceiver(networkStateReceiver)
    }

    interface ISMSRetriever {
        fun onSMSReceived(message: String)
    }


    class MySMSRetriever(val callback: ISMSRetriever) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Obtén el mensaje SMS del intent.
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        // Extrae el código de verificación del mensaje SMS.
                        val code = extractVerificationCode(message)
                        callback.onSMSReceived(code)
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        // Se agotó el tiempo de espera para el mensaje SMS.
                    }
                }
            }
        }

        private fun extractVerificationCode(message: String): String {
            // Aquí debes implementar la lógica para extraer el código de verificación del mensaje SMS.
            // Esto dependerá del formato de tu mensaje SMS.
            return "pp =33"

        }
    }
}
