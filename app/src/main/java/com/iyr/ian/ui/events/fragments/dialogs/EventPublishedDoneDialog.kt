package com.iyr.ian.ui.events.fragments.dialogs


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.databinding.FragmentEventPublishedDonePopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.viewmodels.MainActivityViewModel


interface OnEventPublishedDone {
    fun onBringMeToEvent()
    fun onRefuse()

}

class EventPublishedDoneDialog() :
    AppCompatDialogFragment() {

    private var eventKey: String? = null
    private var controller: MediaController? = null
    private val mThisDialog: EventPublishedDoneDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private lateinit var binding: FragmentEventPublishedDonePopupBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventPublishedDonePopupBinding.inflate(
            inflater,
            container,
            false
        )



        binding.buttonOne!!.setOnClickListener { view ->
            requireContext().handleTouch()
            arguments?.let { args ->

                val eventKey = (args as Bundle).getString("event_key") ?: ""

                MainActivityViewModel.getInstance().goToEvent(eventKey)

            }
            // val eventKey = "args.eventKey"
            //  MainActivityViewModel.getInstance().showGoToEventDialog(null, eventKey)
            //   callback.onBringMeToEvent()
            /*
                        if (mButton1Callback != null) {
            //                mButton1Callback?.onComplete(true, textField.text)

                        }
                        */
            dismiss()
        }

        binding.cancelButton.setOnClickListener { view ->
            requireContext().handleTouch()
            findNavController().popBackStack()
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.gravity = Gravity.CENTER

            dialog.window!!.attributes = lp
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.CENTER)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    init {

        /*
                //  textField = mDialoglayout.findViewById(R.id.text_field)

                val lp = WindowManager.LayoutParams()
                lp.copyFrom(window!!.attributes)
                lp.gravity = Gravity.CENTER
                window!!.attributes = lp
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window!!.setGravity(Gravity.CENTER)
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        */
    }

    fun setEventKey(key: String) {
        this.eventKey = key
    }

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }

}