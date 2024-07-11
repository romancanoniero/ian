package com.iyr.ian.utils.loaders

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class LoadersExtensions

fun Activity.showLoader() {
    if (!isLoaderVisible()) {
        val loader: LoadingDialogFragment = LoadingDialogFragment()
        loader.setLoadingMessage("")
        loader.show((this as AppCompatActivity).supportFragmentManager, "loader_frames")

    } else {
        val loader: LoadingDialogFragment =
            (this as AppCompatActivity).supportFragmentManager.findFragmentByTag("loader_frames") as LoadingDialogFragment
        loader.setLoadingMessage("")
    }
}

fun Activity.showLoader(text: String) {
    if (!isLoaderVisible()) {
        val loader: LoadingDialogFragment = LoadingDialogFragment()
        loader.setLoadingMessage(text)
        loader.show((this as AppCompatActivity).supportFragmentManager, "loader_frames")

    } else {
        val loader: LoadingDialogFragment =
            (this as AppCompatActivity).supportFragmentManager.findFragmentByTag("loader_frames") as LoadingDialogFragment
        loader.setLoadingMessage(text)
    }
}

fun Activity.showLoader(lottieAnimationID: Int, adjustType: LoaderScaleTypeEnum? = LoaderScaleTypeEnum.ORIGINAL)  {
    //Show Loader

    if (!isLoaderVisible()) {
        val loader: LoadingDialogFragment = LoadingDialogFragment()
        //loader.setLoadingMessage(text)
        loader.setAnimationResource(lottieAnimationID)
        loader.show((this as AppCompatActivity).supportFragmentManager, "loader_frames")
        loader.setAdjustType(adjustType!!)

    } else {
        val loader: LoadingDialogFragment =
            (this as AppCompatActivity).supportFragmentManager.findFragmentByTag("loader_frames") as LoadingDialogFragment
//        loader.setLoadingMessage(text)
        loader.setAnimationResource(lottieAnimationID)
        loader.setAdjustType(adjustType!!)

    }
}

fun Activity.isLoaderVisible(): Boolean {
    return (this as AppCompatActivity).supportFragmentManager.findFragmentByTag("loader_frames") != null
}

fun Activity.hideLoader() {
    //Hide Loader

    if (isLoaderVisible()) {
        val loader: LoadingDialogFragment =
            (this as AppCompatActivity).supportFragmentManager.findFragmentByTag("loader_frames") as LoadingDialogFragment
        if (loader != null) {
            loader.dismissAllowingStateLoss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        window!!.setGravity(Gravity.CENTER)
    }


}
