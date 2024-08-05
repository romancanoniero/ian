package com.iyr.ian.ui.base.fragments


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentQrCodeDisplayBinding
import com.iyr.ian.utils.loadImageFromCache

class QrCodeDisplayPopup() :
    AppCompatDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    protected lateinit var binding: FragmentQrCodeDisplayBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentQrCodeDisplayBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        // Inflate the layout for this fragment
        binding.qrImage.setImageBitmap(requireContext().loadImageFromCache("qr_code.png", "images"))

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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