package com.iyr.ian.ui.dialogs


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController

import com.iyr.ian.databinding.FragmentGpsIsRequiredPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch


/*
interface INewUserInvitationDialog {
    in
}
*/

class GPSEnabledIsRequiredDialog() :
    AppCompatDialogFragment()  {

    private lateinit var binding: FragmentGpsIsRequiredPopupBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGpsIsRequiredPopupBinding.inflate(inflater)

        binding.activateButton.setOnClickListener {
            requireActivity().handleTouch()

            findNavController().popBackStack()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        }

        binding.closeButton.setOnClickListener {
            requireActivity().handleTouch()
            dismiss()
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
}

