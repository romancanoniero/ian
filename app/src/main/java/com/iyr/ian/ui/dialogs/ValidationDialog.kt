package com.iyr.ian.ui.dialogs


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.PULSE_VALIDATOR_RETRY_COUNT
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentValidationPopupBinding
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.animations.correctAnimation
import com.iyr.ian.utils.animations.incorrectAnimation
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showAnimatedDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.UserViewModel
import java.io.Serializable
import kotlin.math.floor


class ValidationPulsePayload {
    var validationType: PulseRequestTarget = PulseRequestTarget.PULSE_VALIDATION
    var key: String = ""
}

/*
enum class PulseRequestTarget : Serializable {
    PULSE_VALIDATION,
    VALIDATE_USER,
    VALIDATION_BEFORE_CLOSE_EVENT,
    ON_FALLING_VALIDATION
}
*/
enum class PulseValidationStatus : Serializable {
    USER_OK, USER_PULSE_SENT, USER_IN_TROUBLE, USER_NOT_RESPONSE, WRONG_PIN, PANIC
}

interface PulseValidationCallback {
    fun onValidationOK(dialog: ValidatorDialog, securityPIN: String) {}
    fun onSilentAlarmCode(dialog: ValidatorDialog, securityPIN: String) {}
    fun onNoResponse() {}
    fun onWrongCode(dialog: ValidatorDialog, securityPIN: String) {}
    fun onCancel(dialog: ValidatorDialog) {}
    // fun onKeyboardInteraction(fullfilled: Boolean)

}

class ValidatorDialog() :
    AppCompatDialogFragment() {

    private lateinit var binding: FragmentValidationPopupBinding
    private var password: String = UserViewModel.getInstance().getUser()?.security_code.toString()
    private var requiresCountdown: Boolean = true
    private var auxSoundPlayer: MediaPlayer? = null
    private var remainingSeconds: Int = AppConstants.COUNTDOWN_TIMER_SECONDS
    private var countDownTimer: CountDownTimer? = null
    private var touchTimer: CountDownTimer? = null
    private var mCallback: PulseValidationCallback? = null
    private var mReason: PulseRequestTarget = PulseRequestTarget.PULSE_VALIDATION

    //  private var passwordControl: PasswordView?
    var wrongPing: Boolean = false

    var input = ""
    private var availableRetriesCount = PULSE_VALIDATOR_RETRY_COUNT


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentValidationPopupBinding.inflate(inflater, container, false)

        binding.otpView?.setOtpCompletionListener { text ->
            input = text
            checkPasswordInput(text)
        }

        this.setCancelable(false)
        binding.closeButton.setOnClickListener { view ->
            requireContext().handleTouch()
            findNavController().popBackStack()
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        requireActivity().playSound(R.raw.plop, null, null)
        if (requiresCountdown) {
            startCountDownTimer()
        }

        binding.otpView.text?.clear()
        input = ""
        wrongPing = false
        availableRetriesCount = PULSE_VALIDATOR_RETRY_COUNT

        when (mReason) {
            PulseRequestTarget.PULSE_VALIDATION -> {
                binding.title.setText(R.string.verification_dialog_pulse_validation_title)
                binding.message.setText(R.string.verification_dialog_pulse_validation_message)
            }

            PulseRequestTarget.VALIDATE_USER -> {
                binding.title.setText(R.string.verification_dialog_user_validation_title)
                binding.message.setText(R.string.verification_dialog_user_validation_message)
            }

            PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                binding.title.setText(R.string.verification_dialog_close_event_title)
                binding.message.setText(R.string.verification_dialog_close_event_message)
            }

            PulseRequestTarget.ON_FALLING_VALIDATION -> {
                binding.title.setText(R.string.verification_dialog_falling_title)
                binding.message.setText(R.string.verification_dialog_falling_event_message)
            }
        }
        binding.otpView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.otpView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                binding.otpView.requestFocus()
                binding.otpView.performClick()
            }
        })
    }

    private fun checkPasswordInput(inputText: String) {


        val c: StringBuffer = StringBuffer(password)
        val reversePassword = c.reverse().toString()
        cancelCountDownTimer()
        if (password == inputText) {
            arguments?.putString("code", inputText)
            arguments?.putSerializable("result", PulseValidationStatus.USER_OK)
            MainActivityViewModel.getInstance().onValidationDone(arguments)
        } else
            if (reversePassword == inputText) {
                requireActivity().correctAnimation(binding.otpView.rootView!!)
                if (requireContext() is PulseValidationCallback) {
                    (requireContext() as PulseValidationCallback).onWrongCode(
                        this@ValidatorDialog,
                        inputText
                    )
                }
                if (mCallback != null) {
//                            mCallback?.onComplete(false, PulseValidationStatus.IN_TROUBLE)
                    dismiss()
                    mCallback?.onSilentAlarmCode(this@ValidatorDialog, inputText)

                }
                requireActivity().showAnimatedDialog(
                    requireContext().getString(R.string.closing_event_title),
                    requireContext().getString(R.string.event_sucessfully_close)
                )

                Toast.makeText(requireContext(), "Enviar Alarma Silenciosa", Toast.LENGTH_LONG)
                    .show()
            } else {

                requireActivity().incorrectAnimation(binding.otpView.rootView!!)
                availableRetriesCount--
                if (availableRetriesCount <= 0) {
                    wrongPing = true
                    Toast.makeText(requireContext(), "Password erroneo", Toast.LENGTH_SHORT).show()

                    requireActivity().showErrorDialog(
                        requireContext().getString(R.string.error_wrong_security_code),
                        requireContext().getString(R.string.error_wrong_security_code_message),
                        requireActivity().getString(R.string.close),
                        null
                    )
                    if (mCallback != null) {
                        mCallback?.onWrongCode(this@ValidatorDialog, inputText)
                    }
                    //      dismiss()
                } else {
                    binding.otpView.text?.clear()
                    input = ""
                }
            }

    }

    fun setValidationType(reason: PulseRequestTarget) {
        mReason = reason
        requiresCountdown = mReason == PulseRequestTarget.PULSE_VALIDATION
    }

    fun setCallback(callback: PulseValidationCallback?) {
        mCallback = callback
    }


    fun getCallback(): PulseValidationCallback? {
        return mCallback
    }


    //-----------------------------
    private fun startCountDownTimer() {

        //   playSound(R.raw.clock_toc,null,null)
        //-------------------------------------------------
        // Get a handler that can be used to post to the main thread
        val mainHandler: Handler = Handler(Looper.getMainLooper())
        mainHandler.post {
            if (auxSoundPlayer != null) {
                auxSoundPlayer?.stop()
                auxSoundPlayer?.reset()
            }
            auxSoundPlayer = MediaPlayer.create(activity, R.raw.clock_toc)

            if (auxSoundPlayer?.isPlaying == false) {
                auxSoundPlayer?.start()
            }
        }

        //-------------------------------------------------

        remainingSeconds = AppConstants.COUNTDOWN_TIMER_SECONDS
        countDownTimer = object : CountDownTimer((remainingSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val descuento = floor(((millisUntilFinished / 1000).toDouble()))
                Log.d("COUNTDOWN", descuento.toString())
                remainingSeconds = descuento.toInt()
            }

            override fun onFinish() {
                cancelCountDownTimer()
                if (remainingSeconds <= 0) {
                    Toast.makeText(
                        activity,
                        "AVISO QUE NO SE HA INGRESADO EL CODIGO DE VALIDACION",
                        Toast.LENGTH_SHORT
                    ).show()

                    var status = PulseValidationStatus.USER_NOT_RESPONSE
                    if (wrongPing) {
                        status = PulseValidationStatus.WRONG_PIN
                    }


                    val me = UserViewModel.getInstance().getUser()!!
                    if (me.pulse_status == PulseValidationStatus.USER_OK.name) {
                        requireActivity().playSound(R.raw.policesiren, null, null)
                    }

                    if (requireContext() is PulseValidationCallback) {
                        (requireContext() as PulseValidationCallback).onWrongCode(
                            this@ValidatorDialog,
                            ""
                        )
                    }

                }
            }
        }.start()
    }

    fun onKeyboardInteraction(fullfilled: Boolean) {

        Log.d("PULSEVALIDATORDIALOG", "is password fullfilled=$fullfilled")

        cancelCountDownTimer()

        touchTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                if (input.length < 4) {
                    Log.d(
                        "PULSEVALIDATORDIALOG",
                        "OnFinish = is password fullfilled=$fullfilled"
                    )

                    Log.d("PULSEVALIDATORDIALOG", "reincio el timer")

                    if (requiresCountdown) {
                        startCountDownTimer()
                    }
                }

            }
        }.start()
    }

    private fun cancelCountDownTimer() {
        countDownTimer?.cancel()
        auxSoundPlayer?.stop()
        auxSoundPlayer?.reset()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.gravity = Gravity.CENTER

            dialog.window!!.attributes = lp
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.CENTER)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

}

