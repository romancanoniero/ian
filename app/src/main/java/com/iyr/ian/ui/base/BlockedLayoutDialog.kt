package com.iyr.ian.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.iyr.ian.AppConstants.Companion.BROADCAST_PULSE_REQUIRED
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch


class BlockedLayoutDialog : DialogFragment() {

    private lateinit var layout: View

    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(requireContext().getDrawable(android.R.color.transparent))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(requireActivity().window!!.attributes)
        lp.gravity = Gravity.CENTER
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        requireActivity().window!!.attributes = lp
        requireActivity().window!!.setBackgroundDrawable(requireContext().getDrawable(R.color.layoutBlocked))
        requireActivity().window!!.setGravity(Gravity.CENTER)
        requireActivity().window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        isCancelable = false
        layout = inflater.inflate(R.layout.fragment_layout_blocked, container, false)

        var dialogScreen = layout.findViewById<ConstraintLayout>(R.id.host)

        dialogScreen.setOnClickListener(View.OnClickListener
        {
            requireContext().handleTouch()

            val intent =
                Intent(BROADCAST_PULSE_REQUIRED)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        })

        return layout
    }


    //over rid  this due to some issues that occur when trying to show a the dialog after onSaveInstanceState



    override fun show(manager: FragmentManager, tag: String?) {

        try {
            manager.findFragmentByTag(tag)
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.commitAllowingStateLoss()


        } catch (ignored: IllegalStateException) {
            var pp = 3
        }


    }


}
