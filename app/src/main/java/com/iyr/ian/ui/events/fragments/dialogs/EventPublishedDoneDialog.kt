package com.iyr.ian.ui.events.fragments.dialogs


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.UIUtils.handleTouch


interface OnEventPublishedDone {
    fun onBringMeToEvent()
    fun onRefuse()

}

class EventPublishedDoneDialog(
    mContext: Context,
    val mActivity: Activity,
    callback: OnEventPublishedDone
) :
    AlertDialog(
        mContext
    ) {
    private var acceptButton: Button?
    private var eventKey: String? = null
    private var controller: MediaController? = null
    private val mThisDialog: EventPublishedDoneDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private val mButton2Caption: String? = null


    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_event_published_done_popup, null)
        this.setView(mDialoglayout)
        acceptButton = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancel_button)

        //  textField = mDialoglayout.findViewById(R.id.text_field)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        acceptButton!!.setOnClickListener { view ->
            context.handleTouch()

            callback.onBringMeToEvent()
/*
            if (mButton1Callback != null) {
//                mButton1Callback?.onComplete(true, textField.text)

            }
            */
            dismiss()
        }
        cancelButton.setOnClickListener { view ->
            context.handleTouch()
            dismiss()
        }
    }

    fun setEventKey(key: String) {
        this.eventKey = key
    }

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }

}