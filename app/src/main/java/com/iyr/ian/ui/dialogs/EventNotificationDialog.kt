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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.utils.UIUtils.handleTouch


interface NotificationAdapterCallback {
    fun onAgreeToAssist(notificationKey: String, eventKey: String)
    fun onDenyToAssist(event: Event)
    fun onDenyToAssist(eventKey: String)
    fun onStartToFollow(eventKey: String)
    fun onNotificationDismiss(notificationKey: EventNotificationModel)
    //  fun contactRequestAccept(eventKey: String, user: UserMinimum)

}

class EventNotificationDialog(private val mContext: Context, mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private val mThisDialog: EventNotificationDialog = this
    private var mButton1Callback: View.OnClickListener? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private val mButton2Caption: String? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTitle != null) {
            val title = mDialoglayout.findViewById<TextView>(R.id.title)
            title.text = mTitle
        }
        if (mLegend != null) {
            val legend = mDialoglayout.findViewById<TextView>(R.id.legend)
            legend.text = mLegend
        }
        if (mButton1Caption != null) {
            val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
            buttonOne.text = mButton1Caption
        }
        if (mButton1Callback != null) {
            val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
            buttonOne.setOnClickListener(mButton1Callback)
        }
    }

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
        mDialoglayout = inflater.inflate(R.layout.event_notification_popup, null)
        this.setView(mDialoglayout)
        val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        buttonOne.setOnClickListener { view ->
            context.handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback!!.onClick(view)
            }
            dismiss()
        }
    }
}