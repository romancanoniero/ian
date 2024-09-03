package com.iyr.ian.ui.dialogs


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.UIUtils.handleTouch


class TextDialog(mContext: Context, mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private var acceptButton: Button?
    private var textField: EditText
    private var controller: MediaController? = null
    private val mThisDialog: TextDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private val mButton2Caption: String? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mButton1Callback == null) {
            acceptButton?.visibility = GONE
        }
    }


    fun setText(text: String) {
        textField.setText(text)
    }


    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.dialog_text_input, null)
        this.setView(mDialoglayout)
        acceptButton = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancel_button)

        textField = mDialoglayout.findViewById(R.id.text_field)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        acceptButton!!.setOnClickListener { view ->

            context.handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback?.onComplete(true, textField.text)
            }
            dismiss()
        }
        cancelButton.setOnClickListener { view ->
            context.handleTouch()

            dismiss()
        }
    }

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }

}