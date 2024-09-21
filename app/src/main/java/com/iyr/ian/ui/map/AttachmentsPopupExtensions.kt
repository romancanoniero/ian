package com.iyr.ian.ui.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.iyr.ian.AppConstants.Companion.CHAT_FILES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.ui.chat.MessagesInEventFragment
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.getDimentions
import com.iyr.ian.utils.permissionsForVideo
import com.iyr.ian.utils.permissionsReadWrite
import com.iyr.ian.utils.toMediaFile
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AttachmentsPopupExtensions {}

private fun MessagesInEventFragment.showAtachmentsPopupWindow(
    view: View, activity: Activity
) { //Create a View object yourself through inflater
    val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    popupView = inflater.inflate(R.layout.context_popup_attachment, null)
    popupView?.measure(
        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    )
    val attachmentButtonLocation = IntArray(2)
    view.getLocationOnScreen(attachmentButtonLocation)
    val x: Int = popupView?.measuredWidth as Int

    val popupHeight: Int = popupView?.measuredHeightAndState as Int
    //Specify the length and width through constants
    val width = LinearLayout.LayoutParams.WRAP_CONTENT
    val height = LinearLayout.LayoutParams.WRAP_CONTENT
    //Make Inactive Items Outside Of PopupWindow
    configureContextMenu(popupView!!)
    val popupWindow = PopupWindow(popupView, width, height, true)
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    val displayWidth = displayMetrics.widthPixels

    //val xAxis = displayWidth / 4 - displayWidth / 10
    val xAxis = attachmentButtonLocation[0]
    val yAxis = attachmentButtonLocation[1] - popupHeight //76.px
    popupWindow.isOutsideTouchable = true
    popupWindow.showAtLocation(
        view, Gravity.NO_GRAVITY, xAxis, yAxis
    )
    popupWindow.dimBehind()

    popupView?.setOnClickListener {
        requireContext().handleTouch()
        popupWindow.dismiss()
    }
}

private fun MessagesInEventFragment.configureContextMenu(popupView: View) {

    popupView.findViewById<LinearLayout>(R.id.attachment_photo_option).setOnClickListener {
        popupView.performClick()
        requireActivity().permissionsReadWrite()
        toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    popupView.findViewById<LinearLayout>(R.id.attachment_image_option).setOnClickListener {
        popupView.performClick()
        requireActivity().permissionsReadWrite()
        toPickImagePermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
    }


    popupView.findViewById<LinearLayout>(R.id.attachment_audio_option)
        .setOnTouchListener { _, motionEvent ->
            this.recordingManagement(motionEvent)
            return@setOnTouchListener true
        }


    popupView.findViewById<LinearLayout>(R.id.attachment_video_option).setOnClickListener {
        popupView.performClick()
        if (Build.VERSION.SDK_INT <= 32) {
            toTakeVideoPermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            toTakeVideoPermissionsRequest?.launch(arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO))
        }
    }
}

private fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.3f
    wm.updateViewLayout(container, p)
}


