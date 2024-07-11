package com.iyr.ian.ui.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.utils.UIUtils.handleTouch


interface OnConfirmationButtonsListener {
    fun onAccept() {
        //TODO}
    }

    fun onCancel() {
        //TODO}
    }
}

class ConfirmationDialog(context: Context, activity: Activity) :

    AlertDialog(context) {
    private val mThisDialog: ConfirmationDialog
    private val mContext: Context
    private var mActivity: Activity
    private var mCallback: IAcceptDenyDialog? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private var mButton2Caption: String? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTitle != null) {
            val title = mDialoglayout.findViewById<TextView>(R.id.dialogTitle)
            title.text = mTitle
        }
        if (mLegend != null) {
            val legend = mDialoglayout.findViewById<TextView>(R.id.dialogMessage)
            legend.text = mLegend
        }
        if (mButton1Caption != null) {
            val acceptButton = mDialoglayout.findViewById<Button>(R.id.acceptButton)
            acceptButton.text = mButton1Caption
        }
        if (mButton2Caption != null) {
            val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancelButton)
            cancelButton.text = mButton2Caption
        }
    }

    override fun setTitle(resId: Int) {
        mTitle = mContext.getString(resId)
    }

    fun setTitle(title: String?) {
        mTitle = title
    }

    fun setCallback(callback: IAcceptDenyDialog) {
        mCallback = callback
    }

    fun setLegend(resId: Int) {
        mLegend = mContext.getString(resId)
    }

    fun setLegend(legend: String?) {
        mLegend = legend
    }

    fun setButton1Caption(resId: Int) {
        mButton1Caption = mContext.getString(resId)
    }
    fun setButton1Caption(text: String) {
        mButton1Caption = text
    }
    fun setButton2Caption(resId: Int) {
        mButton2Caption = mContext.getString(resId)
    }
    fun setButton2Caption(text: String) {
        mButton2Caption = text
    }

    init {
        mContext = context
        mActivity = activity
        mThisDialog = this
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_confirmation_popup, null)
        this.setView(mDialoglayout)
        val acceptButton = mDialoglayout.findViewById<Button>(R.id.acceptButton)
        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancelButton)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        acceptButton.setOnClickListener {
            context.handleTouch()
            mThisDialog.dismiss()
            if (mCallback != null) {
                mCallback!!.onAccept()
            }
            dismiss()
        }

        cancelButton.setOnClickListener {
            context.handleTouch()
            mThisDialog.dismiss()
            dismiss()
            if (mCallback != null) {
                mCallback!!.onCancel()
            }
        }
    }
}