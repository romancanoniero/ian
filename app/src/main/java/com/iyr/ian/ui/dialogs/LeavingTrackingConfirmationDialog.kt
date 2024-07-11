package com.iyr.ian.ui.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch

interface LeavingTrackingConfirmationDialogCallback {
    fun onInputValidationRequest()
    fun onImSafeEvent(eventKey: String? = null) {}

}

class LeavingTrackingConfirmationDialog(private val mContext: Context, mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private val mThisDialog: LeavingTrackingConfirmationDialog = this
    private var mButton1Callback: View.OnClickListener? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private val mButton2Caption: String? = null

    override fun setTitle(resId: Int) {
        mTitle = mContext.getString(resId)
    }

    fun setTitle(title: String?) {
        mTitle = title
    }

    fun setLegend(resId: Int) {
        mLegend = mContext.getString(resId)
    }

    fun setLegend(message: String?) {
        mLegend = message
    }

    fun setButton1Caption(resId: Int) {
        mButton1Caption = mContext.getString(resId)
    }

    fun setButton1Caption(text: String?) {
        mButton1Caption = text
    }


    fun setButton1Callback(onClickListener: View.OnClickListener) {
        mButton1Callback = onClickListener
    }

    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.leaving_tracking_confirmation_popup, null)
        this.setView(mDialoglayout)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val imSafeButton = mDialoglayout.findViewById<Button>(R.id.im_safe_button)
        val validateMeButton = mDialoglayout.findViewById<Button>(R.id.validate_me_button)
        val closeButton = mDialoglayout.findViewById<Button>(R.id.close_button)



        closeButton.setOnClickListener {
            context.handleTouch()
            dismiss()
        }


        imSafeButton.setOnClickListener { view ->
            context.handleTouch()
            if (mContext is LeavingTrackingConfirmationDialogCallback) {
                (mContext as LeavingTrackingConfirmationDialogCallback).onImSafeEvent()
            }
            dismiss()
        }

        validateMeButton.setOnClickListener { view ->
            context.handleTouch()
            if (mContext is LeavingTrackingConfirmationDialogCallback) {
                (mContext as LeavingTrackingConfirmationDialogCallback).onInputValidationRequest()
            }
            dismiss()
        }


    }
}