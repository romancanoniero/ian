package com.iyr.ian.ui.base


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.PULSE_VALIDATOR_RETRY_COUNT
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.animations.correctAnimation
import com.iyr.ian.utils.animations.incorrectAnimation
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showAnimatedDialog
import com.iyr.ian.utils.showErrorDialog
import com.mukesh.OtpView
import kotlin.math.floor


class ValidationPulsePayload {
    var validationType: PulseRequestTarget = PulseRequestTarget.PULSE_VALIDATION
    var key: String = ""
}

class PulseValidationRequest() {
    var validationType: PulseRequestTarget? = null
    var eventKey: String? = null

    constructor(validationType: PulseRequestTarget?, eventKey: String?) : this() {
        this.validationType = validationType
        this.eventKey = eventKey
    }
}

enum class PulseRequestTarget {
    PULSE_VALIDATION,
    VALIDATE_USER,
    VALIDATION_BEFORE_CLOSE_EVENT,
    ON_FALLING_VALIDATION
}

enum class PulseValidationStatus {
    USER_OK, USER_PULSE_SENT, USER_IN_TROUBLE, USER_NOT_RESPONSE, WRONG_PIN, PANIC
}

interface PulseValidationCallback {
    fun onValidationOK(dialog: PulseValidatorDialog, securityPIN: String) {}
    fun onSilentAlarmCode(dialog: PulseValidatorDialog, securityPIN: String) {}
    fun onNoResponse() {}
    fun onWrongCode(dialog: PulseValidatorDialog, securityPIN: String) {}
    fun onCancel(dialog: PulseValidatorDialog) {}
    // fun onKeyboardInteraction(fullfilled: Boolean)

}

class PulseValidatorDialog(private val con: Context, private val activity: Activity) :
    AlertDialog(
        con
    ) {
    constructor(activity: Activity) : this(activity, activity)

    private var password: String = ""
    private var codeView: OtpView? = null
    private lateinit var dialogMessage: TextView
    private lateinit var dialogTitle: TextView
    private var requiresCountdown: Boolean = true
    private var auxSoundPlayer: MediaPlayer? = null
    private var remainingSeconds: Int = AppConstants.COUNTDOWN_TIMER_SECONDS
    private var countDownTimer: CountDownTimer? = null
    private var touchTimer: CountDownTimer? = null
    private var mCallback: PulseValidationCallback? = null
    private var mReason: PulseRequestTarget = PulseRequestTarget.PULSE_VALIDATION
    private var closeButton: Button?

    //  private var passwordControl: PasswordView?
    var wrongPing: Boolean = false

    private var controller: MediaController? = null
    private val mThisDialog: PulseValidatorDialog
    private var mButton1Callback: OnCompleteCallback? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private val mButton2Caption: String? = null
    var input = ""
    private var avalaibleRetriesCount = PULSE_VALIDATOR_RETRY_COUNT


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity.playSound(R.raw.plop, null, null)
        if (requiresCountdown) {
            startCountDownTimer()
        }
       // codeView?.requestFocus()
//context.showKeyboard(codeView!!)
    }

    init {
        val me = SessionForProfile.getInstance(con).getUserProfile()
        password = AppClass.instance.user.value?.security_code ?: ""

        mThisDialog = this
        val inflater = activity.layoutInflater

        mDialoglayout = inflater.inflate(R.layout.fragment_password_validation_popup, null)
        this.setView(mDialoglayout)
        dialogTitle = mDialoglayout.findViewById<TextView>(R.id.title)
        dialogMessage = mDialoglayout.findViewById<TextView>(R.id.message)
        closeButton = mDialoglayout.findViewById<Button>(R.id.close_button)
        codeView = mDialoglayout.findViewById<OtpView>(R.id.otp_view)

        codeView?.setOtpCompletionListener { text ->
            input = text
            checkPasswordInput(text)
        }

        this.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        closeButton!!.setOnClickListener { view ->
            con.handleTouch()
            dismiss()
        }
    }

    override fun dismiss() {
        activity.hideKeyboard()
        getCallback()?.onCancel(mThisDialog)
        super.dismiss()
    }

    private fun checkPasswordInput(inputText: String) {
        val c: StringBuffer = StringBuffer(password)
        val reversePassword = c.reverse().toString()
        cancelCountDownTimer()
        if (password == inputText) {
            //      Toast.makeText(con, "El usuario esta bien", Toast.LENGTH_LONG).show()
            activity.correctAnimation(codeView!!)
            dismiss()
            if (mCallback != null) {
                mCallback?.onValidationOK(mThisDialog, password)
            }
        } else
            if (reversePassword == inputText) {
                activity.correctAnimation(codeView?.rootView!!)
                if (con is PulseValidationCallback) {
                    (con as PulseValidationCallback).onWrongCode(mThisDialog, inputText)
                }
                if (mCallback != null) {
//                            mCallback?.onComplete(false, PulseValidationStatus.IN_TROUBLE)
                    dismiss()
                    mCallback?.onSilentAlarmCode(mThisDialog, inputText)

                }
                activity.showAnimatedDialog(
                    activity.getString(R.string.closing_event_title),
                    activity.getString(R.string.event_sucessfully_close)
                )

                Toast.makeText(con, "Enviar Alarma Silenciosa", Toast.LENGTH_LONG)
                    .show()
            } else {

                activity.incorrectAnimation(codeView?.rootView!!)
                avalaibleRetriesCount--
                if (avalaibleRetriesCount <= 0) {
                    wrongPing = true
                    Toast.makeText(con, "Password erroneo", Toast.LENGTH_SHORT).show()

                    activity.showErrorDialog(
                        activity.getString(R.string.error_wrong_security_code),
                        activity.getString(R.string.error_wrong_security_code_message),
                        activity.getString(R.string.close),
                        null
                    )
                    if (mCallback != null) {
                        mCallback?.onWrongCode(mThisDialog, inputText)
                    }
                    //      dismiss()
                } else {
                    codeView!!.text?.clear()
                    input = ""
                }
            }

    }


    fun show(validationType: PulseRequestTarget?, eventKey: String?) {
        this.mReason = validationType!!
        show()
    }


    override fun show() {

        // set the type of input entered, this would be for entry of a number digit keyboard
        val value =
            codeView?.setInputType(InputType.TYPE_CLASS_NUMBER )

        codeView?.requestFocus()
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        super.show()
        codeView?.text?.clear()
        input = ""
        wrongPing = false
        avalaibleRetriesCount = PULSE_VALIDATOR_RETRY_COUNT

        when (mReason) {
            PulseRequestTarget.PULSE_VALIDATION -> {
                dialogTitle.setText(R.string.verification_dialog_pulse_validation_title)
                dialogMessage.setText(R.string.verification_dialog_pulse_validation_message)
            }

            PulseRequestTarget.VALIDATE_USER -> {
                dialogTitle.setText(R.string.verification_dialog_user_validation_title)
                dialogMessage.setText(R.string.verification_dialog_user_validation_message)
            }

            PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                dialogTitle.setText(R.string.verification_dialog_close_event_title)
                dialogMessage.setText(R.string.verification_dialog_close_event_message)
            }

            PulseRequestTarget.ON_FALLING_VALIDATION -> {
                dialogTitle.setText(R.string.verification_dialog_falling_title)
                dialogMessage.setText(R.string.verification_dialog_falling_event_message)
            }
        }
        /*
                val imm: InputMethodManager? =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                */


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
                    val me = SessionForProfile.getInstance(activity).getUserProfile()
                    if (me.pulse_status == PulseValidationStatus.USER_OK.name) {
                        activity.playSound(R.raw.policesiren, null, null)
                    }

                    if (con is PulseValidationCallback) {
                        (con as PulseValidationCallback).onWrongCode(mThisDialog, "")
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

    fun getOtpControl(): OtpView? {
        return codeView
    }


//-----------------------------
}

