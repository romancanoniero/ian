package com.iyr.ian.ui.base


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentEventCloseToExpirePopupBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.millisToTimeLocale
import com.iyr.ian.utils.playSound
import com.mukesh.OtpView

interface EventCloseToExpireDialogCallback {
    fun onWantToExtend(dialog: EventCloseToExpireDialog, eventKey: String) {}
    fun onDismiss(dialog: EventCloseToExpireDialog) {}
}

class EventCloseToExpireDialog(private val con: Context, private val activity: Activity) :
    AlertDialog(
        con
    ) {
    constructor(activity: Activity) : this(activity, activity)

    var userName: String = ""

    var remainingTime: Long = 0

    private lateinit var binding: FragmentEventCloseToExpirePopupBinding
    private var eventKey: String = ""
    private var codeView: OtpView? = null
    private lateinit var dialogMessage: TextView
    private lateinit var dialogTitle: TextView
    private var requiresCountdown: Boolean = true
    private var auxSoundPlayer: MediaPlayer? = null
    private var remainingSeconds: Int = AppConstants.COUNTDOWN_TIMER_SECONDS
    private var countDownTimer: CountDownTimer? = null
    private var touchTimer: CountDownTimer? = null
    private var mCallback: EventCloseToExpireDialogCallback? = null
    private var mReason: PulseRequestTarget = PulseRequestTarget.PULSE_VALIDATION


    //  private var passwordControl: PasswordView?


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        con.playSound(R.raw.plop, null, null)
    }

    init {
        var me = SessionForProfile.getInstance(con).getUserProfile()

        //      mThisDialog = this
        val inflater = activity.layoutInflater
        binding = FragmentEventCloseToExpirePopupBinding.inflate(inflater)
        this.setView(binding.root)
        this.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.extendButton.setOnClickListener { view ->
            con.handleTouch()
            getCallback()?.onWantToExtend(this, eventKey)
            dismiss()
        }


        binding.closeButton.setOnClickListener { view ->
            con.handleTouch()
            dismiss()
        }
    }

    override fun dismiss() {
        activity.hideKeyboard()
        getCallback()?.onDismiss(this)
        super.dismiss()
    }

    fun setCallback(callback: EventCloseToExpireDialogCallback?) {
        mCallback = callback
    }

    fun getCallback(): EventCloseToExpireDialogCallback? {
        return mCallback
    }

    override fun show() {
        super.show()

        var remainingTimeAsLegend = context.millisToTimeLocale(remainingTime)
        remainingTimeAsLegend = remainingTimeAsLegend.removeSuffix("ago")
        binding.message.text = String.format(
            activity.getString(R.string.event_close_to_expire_for_viewer_message),
            userName,
            remainingTimeAsLegend
        )

    }

    fun getEventKey() = eventKey
    fun setEventKey(key: String) = key.also { this.eventKey = it }

}

