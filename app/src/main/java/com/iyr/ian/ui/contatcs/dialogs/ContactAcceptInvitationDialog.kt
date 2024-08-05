package com.iyr.ian.ui.contatcs.dialogs


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
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.databinding.FragmentContactAcceptInvitationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.viewmodels.MainActivityViewModel

class ContactAcceptInvitationDialog() :
    AppCompatDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    protected lateinit var binding: FragmentContactAcceptInvitationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactAcceptInvitationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        val contact = arguments?.getSerializable("contact") as UserMinimum

        var imageBitmap = requireContext().loadImageFromCache(
            contact.image.file_name,
            "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${contact.user_key}"
        )
        if (imageBitmap == null) {
            imageBitmap =
                requireContext().resources.getDrawable(R.drawable.profile_dummy_background_pink)
                    .toBitmap()
        }
        Glide.with(requireContext())
            .load(imageBitmap)
            .into(binding.userImage)

        val message = getString(R.string.dialog_you_have_been_invited_by, contact.display_name)
        binding.message.text = message



        binding.yesButton.setOnClickListener { view ->
            requireContext().handleTouch()
            MainActivityViewModel.getInstance().acceptContactInvitation(contact.user_key)
            dismiss()
        }
        binding.noButton.setOnClickListener { view ->
            requireContext().handleTouch()
            dismiss()
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