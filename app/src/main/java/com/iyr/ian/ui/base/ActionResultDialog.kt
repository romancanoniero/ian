package com.iyr.ian.ui.base

import android.animation.Animator
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
import com.airbnb.lottie.LottieAnimationView
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.playSound

class ActionResultDialog(private val mContext: Context, private val mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private val mThisDialog: ActionResultDialog
    private var mButton1Callback: View.OnClickListener? = null
    private val mButton2Callback: View.OnClickListener? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private var mButton2Caption: String? = null

    init {
        mThisDialog = this
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.action_result_popup, null)
        this.setView(mDialoglayout)
        val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.setAttributes(lp)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        buttonOne.setOnClickListener { view ->
            mActivity.handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback!!.onClick(view)
            }
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        buttonOne.visibility = View.INVISIBLE
        if (mTitle != null) {
            val title = mDialoglayout.findViewById<TextView>(R.id.title)
            title.text = mTitle
            title.visibility = View.VISIBLE
        } else {
            val title = mDialoglayout.findViewById<TextView>(R.id.title)
            title.visibility = View.VISIBLE
        }
        if (mLegend != null) {
            val legend = mDialoglayout.findViewById<TextView>(R.id.legend)
            legend.text = mLegend
            legend.setGravity(Gravity.CENTER)
            legend.visibility = View.VISIBLE
        } else {
            val legend = mDialoglayout.findViewById<TextView>(R.id.legend)
            legend.visibility = View.GONE
        }
        if (mButton1Caption != null) {
            buttonOne.text = mButton1Caption
        }
        val animation = mDialoglayout.findViewById<LottieAnimationView>(R.id.lottie_animation)
        animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                buttonOne.visibility = View.VISIBLE
                context.playSound(R.raw.bell)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        if (mButton1Callback != null) {
            buttonOne.setOnClickListener {
                mButton1Callback!!.onClick(mDialoglayout)
                dismiss()
            }
        }
        if (mButton2Caption != null) {
            val buttonTwo = mDialoglayout.findViewById<Button>(R.id.buttonTwo)
            buttonTwo.text = mButton2Caption
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

    fun setButton2Caption(text: String?) {
        mButton2Caption = text
    }

    override fun show() {
        super.show()
        val buttonTwo = mDialoglayout.findViewById<Button>(R.id.buttonTwo)
    }

    fun setButton1Callback(onClickListener: View.OnClickListener) {
        mButton1Callback = onClickListener
    }
}
