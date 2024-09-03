package com.iyr.ian.ui.map.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch


interface OnYesNoButtonsListener {
    fun onYes() {
        //TODO}
    }

    fun onNo() {
        //TODO}
    }
}

class ArrivingDialog(context: Context, activity: Activity) :

    AlertDialog(context) {
    private val mThisDialog: ArrivingDialog
    private val mContext: Context
    private var mActivity: Activity
    private var mCallback: OnYesNoButtonsListener? = null
    private val mDialoglayout: View

    fun setCallback(callback: OnYesNoButtonsListener) {
        mCallback = callback
    }

    init {
        mContext = context
        mActivity = activity
        mThisDialog = this
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.dialog_infowindow_popup, null)
        this.setView(mDialoglayout)
        val yesButton = mDialoglayout.findViewById<Button>(R.id.yesButton)
        val noButton = mDialoglayout.findViewById<Button>(R.id.noButton)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        yesButton.setOnClickListener {
            context.handleTouch()
            mThisDialog.dismiss()
            if (mCallback != null) {
                mCallback!!.onYes()
            }
        }

        noButton.setOnClickListener {
            context.handleTouch()
            mThisDialog.dismiss()
            if (mCallback != null) {
                mCallback!!.onNo()
            }
        }
    }
}