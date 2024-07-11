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
import android.widget.ImageView
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch


class ImageViewerDialog(mContext: Context, mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private var imageViewer: ImageView
    private var controller: MediaController? = null

    // private var mc: MediaController
    private val mThisDialog: ImageViewerDialog = this
    private var mButton1Callback: View.OnClickListener? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private val mButton2Caption: String? = null

    fun setImageUrl(url: String) {
        Glide
            .with(context)
            .asBitmap()
            .load(url)
            .into(imageViewer)
    }

    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_image_viewer_popup, null)
        this.setView(mDialoglayout)
        val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)

        imageViewer = mDialoglayout.findViewById(R.id.image_viewer)


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