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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentContactDeletionConfirmationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.ContactsFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel

class ContactsDeletionConfirmationDialog() :
    AppCompatDialogFragment() {


    private val arguments: ContactsDeletionConfirmationDialogArgs by navArgs()

    val viewModel by lazy {
        ContactsFragmentViewModel.getInstance(
            UserViewModel.getInstance().getUser()?.user_key.toString()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    protected lateinit var binding: FragmentContactDeletionConfirmationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContactDeletionConfirmationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        binding.contactName.text = if (arguments.contact.display_name != null) {
            arguments.contact.display_name
        } else if (arguments.contact.email != null) {
            arguments.contact.email
        } else if (arguments.contact.telephone_number != null) {
            arguments.contact.telephone_number
        } else
            "????"

        binding.yesButton.setOnClickListener { view ->
            requireContext().handleTouch()
            viewModel.cancelFriendship(arguments.contact.user_key.toString())
        }

        binding.noButton.setOnClickListener { view ->
            requireContext().handleTouch()
            findNavController().popBackStack()
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

        startObservers()
    }


    override fun onStop() {
        super.onStop()
        stopObservers()
    }

    private fun startObservers() {
        viewModel.contactCancelationAction.observe(this) { resource ->

            when (resource) {
                is Resource.Error -> {
                    requireActivity().showErrorDialog(resource.message.toString())
                }

                is Resource.Loading -> {
                }

                is Resource.Success -> {
                    findNavController().popBackStack()
                    viewModel.resetOperations()
                }

                null -> {
                    var tt = 0
                }
            }
        }
    }

    private fun stopObservers() {
        viewModel.contactCancelationAction.removeObservers(this)
    }
}

