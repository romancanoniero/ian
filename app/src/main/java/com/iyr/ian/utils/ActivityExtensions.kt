package com.iyr.ian.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.core.view.WindowCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.ui.base.ActionResultDialog
import com.iyr.ian.ui.base.ConfirmationDialog
import com.iyr.ian.ui.base.SingleButtonDialog
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import java.util.Locale

class ActivityExtensions

fun Activity.showErrorDialog(message: String) {
    val looper = this.mainLooper
    looper.let {
        val dialog = SingleButtonDialog(this, this)
        dialog.setLegend(message)
        dialog.show()
    }
}

fun Activity.showErrorDialog(
    title: String?,
    message: String?,
    button1Caption: String? = null,
    callback: View.OnClickListener? = null
) {
    val dialog = SingleButtonDialog(this, this)
    title?.let { value ->
        dialog.setTitle(value)
    }

    message?.let { value ->
        dialog.setLegend(value)
    }

    button1Caption?.let { value ->
        dialog.setButton1Caption(value)
    }

    callback?.let { it ->
        dialog.setButton1Callback(it)
    }
    dialog.show()
}


fun Activity.showErrorDialog(message: String, buttonCaption: String) {
    val dialog = SingleButtonDialog(this, this)
    dialog.setLegend(message)
    dialog.setButton1Caption(buttonCaption)
    dialog.show()

}

fun Activity.showErrorDialog(message: String, callback: View.OnClickListener?) {
    this.showErrorDialog(null, message, null, callback)
}


fun Activity.showConfirmationDialog(
    title: String,
    message: String,
    afirmativeButtonCaption: String,
    negativeButtonCaption: String,
    afirmativeButtonOnClick: IAcceptDenyDialog?
) {
    val dialog = ConfirmationDialog(this, this)
    dialog.setTitle(title)
    dialog.setLegend(message)
    dialog.setButton1Caption(afirmativeButtonCaption)
    afirmativeButtonOnClick?.let { dialog.setCallback(it) }
    dialog.setButton2Caption(negativeButtonCaption)
    dialog.show()
}


fun Activity.showConfirmationDialog(
    title: String,
    message: String,
    afirmativeButtonCaption: String,
    negativeButtonCaption: String,
    afirmativeButtonOnClick: OnClickListener
) {
    val dialog = ConfirmationDialog(this, this)
    dialog.setTitle(title)
    dialog.setLegend(message)
    dialog.setButton1Caption(afirmativeButtonCaption)
    dialog.setButton1ClickListener(afirmativeButtonOnClick)

    dialog.setButton2Caption(negativeButtonCaption)
    dialog.show()
}



fun Activity.showAnimatedDialog(title: String, message: String) {
    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        .setTitleText(title)
        .setContentText(message)
        .show()
}



fun Activity.showSuccessDialog(
    title: String?,
    message: String?,
    button1Caption: String? = null,
    callback: View.OnClickListener? = null
) {
    val dialog = ActionResultDialog(this, this)
    title?.let { value ->
        dialog.setTitle(value)
    }

    message?.let { value ->
        dialog.setLegend(value)
    }

    button1Caption?.let { value ->
        dialog.setButton1Caption(value)
    }

    callback?.let { it ->
        dialog.setButton1Callback(it)
    }
    dialog.show()
}



fun Activity.startActivity(classParam: Class<*>, bundle: Bundle?) {
    val nextIntent =
        Intent(applicationContext, classParam)

    bundle?.let {
        nextIntent.putExtras(bundle)
    }
    startActivity(nextIntent)
}

fun Activity.hideStatusBar() {


    /*
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }
    */
    /*
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark

        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.black));// set status background white
    */


    if (Build.VERSION.SDK_INT in 21..29) {
        window.statusBarColor = Color.TRANSPARENT
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE


    } else if (Build.VERSION.SDK_INT >= 30) {
        window.statusBarColor = Color.TRANSPARENT
        // Making status bar overlaps with the activity
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    setLightStatusBar(window.decorView, this)

}

fun setLightStatusBar(view: View, activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = view.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        view.systemUiVisibility = flags
        activity.window.statusBarColor = activity.getColor(R.color.transparent)
    }
}


/**
 * Prepara un objeto de tipo multimedia
 * @param mediaType Tipo de multimedia
 * @param source Ruta de la multimedia incluyendo el nombre del archivo
 * @param cacheSubDirectory Subdirectorio donde se guardara la multimedia
 */

fun Context.prepareMediaObject(
    mediaType: MediaTypesEnum, source: String, cacheSubDirectory: String? = ""
): Any {

    /*
    if (fileName.compareTo(fileName.getJustFileName()) != 0) {
        throw Exception("El parametro fileName debe contener solo el nombre del archivo")
    }
    */
    val fileName = source.getJustFileName()
    try {

        val media = MediaFile(mediaType, source, fileName.toString())
        if (mediaType == MediaTypesEnum.VIDEO || mediaType == MediaTypesEnum.AUDIO || mediaType == MediaTypesEnum.IMAGE) {
            val fileExtension = media.file_name.getFileExtension(this)
            var fileUri = media.localFullPath
            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "png"
            ) {
                fileUri = "file:" + media.localFullPath
            }
            var mediaFileEncoded: String? = null
            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "png" || fileExtension?.lowercase(Locale.getDefault()) == "mp4" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "3gp"
            ) {


                mediaFileEncoded = MultimediaUtils(this).convertFileToBase64(
                    Uri.parse(
                        fileUri
                    )
                ).toString()
                media.bytesB64 = mediaFileEncoded

                cacheSubDirectory?.let { destinationPath ->

                    var destination =
                        this.cacheDir.toString() + "/" + destinationPath

                    FileUtils().copyFile(

                        source.substringBeforeLast("/"),
                        source.getJustFileName(),
                        destinationPath
                    )

                }
            }
        }

        return media
    } catch (ex: Exception) {
        return ex
    }

}

fun Context.getCacheLocation(additionalPath: String? = null): String {
    additionalPath?.let {
        return this.cacheDir.toString() + "/" + additionalPath
    }
    return this.cacheDir.toString()
}