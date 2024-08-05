package com.iyr.ian.ui.contacts_groups.dialogs


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentContactGroupCreationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.capitalizeFirstAndLongWords
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.ContactsFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel

class ContactsGroupsCreationDialog :
    AppCompatDialogFragment() {

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

    private lateinit var binding: FragmentContactGroupCreationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactGroupCreationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        binding.groupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.createButton.isEnabled = binding.groupName.text.toString().length >= 4
            }
        })

        binding.createButton.setOnClickListener {
            requireContext().handleTouch()
            val groupName = binding.groupName.text.toString().capitalizeFirstAndLongWords()
            requireActivity().hideKeyboard()
            viewModel.insertGroup(groupName)

        }
        binding.cancelButton.setOnClickListener {

            requireContext().handleTouch()
            requireActivity().hideKeyboard()
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

                }

                is Resource.Success -> {
                    dismiss()
                    viewModel.resetOperations()
                }

                null -> {}


            }
        }
    }

    private fun stopObservers() {
        viewModel.insertContactGroupAction.removeObservers(this)
    }
}

