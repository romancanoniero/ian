package com.iyr.ian.ui.dialogs


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View.GONE
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.FragmentNewEventNotificationPopupBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.playSound
import kotlinx.coroutines.launch


class NewEventNotificationDialog(mContext: Context, mActivity: Activity,val event: Event) :
    AlertDialog(
        mContext
    ) {
    //private var acceptButton: Button?
    //private var description: TextView
    private var controller: MediaController? = null
    private val mThisDialog: NewEventNotificationDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private val binding: FragmentNewEventNotificationPopupBinding
    private var mTitle: String? = null
    private var mLegend: String? = null
    private val mButton2Caption: String? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mButton1Callback == null) {
            binding.buttonOne?.visibility = GONE
        }
    }


    fun setText(text: String) {
        binding.description.setText(text)
    }


    init {
        val inflater = mActivity.layoutInflater

        binding = FragmentNewEventNotificationPopupBinding.inflate(inflater, null, false)
//        mDialoglayout = inflater.inflate(R.layout.fragment_new_event_notification_popup, null)
        this.setView(binding.root)
        /*
                acceptButton = binding.buttonOne
                val cancelButton = binding.cancelButton
                description = binding.description
        */

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.buttonOne!!.setOnClickListener { view ->

            context.handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback?.onComplete(true, "textField.text")
            }
            dismiss()
        }
        binding.cancelButton.setOnClickListener { view ->
            context.handleTouch()
            dismiss()
        }

        binding.eventIcon.setImageDrawable(context.getEventTypeDrawable(event.event_type))

        lifecycleScope.launch {
            var storageReferenceCache = FirebaseStorage.getInstance()
                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                .child(event.author_key!!).child(event.author?.profile_image_path.toString())
                .downloadUrlWithCache(context)

            GlideApp.with(context)
                .asBitmap()
                .load(storageReferenceCache)
                .placeholder(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.progress_animation
                    )
                )
                .error(AppCompatResources.getDrawable(context, R.drawable.ic_error))
                .into(binding.userImage)

        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }


    override fun show() {
        super.show()

        when (event.event_type) {

            EventTypesEnum.PANIC_BUTTON.name -> {
                context.playSound(R.raw.policesiren, null, null)
            }

            EventTypesEnum.SEND_POLICE.name -> {
                context.playSound(R.raw.policesiren, null, null)
            }

            EventTypesEnum.SEND_FIREMAN.name -> {
                context.playSound(R.raw.fire_truck_siren, null, null)
            }
        }
    }
}