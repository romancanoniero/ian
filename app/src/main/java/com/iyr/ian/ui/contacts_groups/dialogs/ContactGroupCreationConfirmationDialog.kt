package com.iyr.ian.ui.contacts_groups.dialogs


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
import com.iyr.ian.databinding.FragmentContactGroupCreationConfirmationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.capitalizeFirstAndLongWords
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.ContactsFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel

class ContactsGroupsCreationConfirmationDialog() :
    AppCompatDialogFragment() {


    private val arguments: ContactsGroupsCreationConfirmationDialogArgs by navArgs()

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

    protected lateinit var binding: FragmentContactGroupCreationConfirmationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactGroupCreationConfirmationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )


        binding.groupName.setText(arguments.additionalData)


        binding.yesButton.setOnClickListener { view ->
            requireContext().handleTouch()
            val groupName = binding.groupName.text.toString().capitalizeFirstAndLongWords()

            viewModel.insertGroup(groupName)

        }
        binding.noButton.setOnClickListener { view ->

            findNavController().popBackStack()
//            requireContext().handleTouch()
//            dismiss()
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
        viewModel.insertContactGroupAction.observe(this) { resource ->
            when (resource) {
                is Resource.Error -> {
                    requireActivity().hideLoader()
                    when (resource.message) {
                        "group_already_exists" -> {
                            requireActivity().showErrorDialog(getString(R.string.group_name_exists))
                        }

                        else -> {
                            requireActivity().showErrorDialog(resource.message.toString())
                        }
                    }
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
//                    dismiss()
                    requireActivity().hideLoader()
                    findNavController().popBackStack()
                }

                null -> {}


            }
        }
    }

    private fun stopObservers() {
        viewModel.insertContactGroupAction.removeObservers(this)
    }
}

