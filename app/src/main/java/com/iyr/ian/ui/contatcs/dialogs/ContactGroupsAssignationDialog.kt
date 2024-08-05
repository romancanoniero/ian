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
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.databinding.FragmentContactGroupAssignationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.ContactsFragmentViewModel

class ContactsGroupsAssignationDialog :
    AppCompatDialogFragment() {


    private val args: ContactsGroupsAssignationDialogArgs by navArgs()

    private lateinit var  contact : Contact

    val viewModel by lazy {
        ContactsFragmentViewModel.getInstance()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    private lateinit var binding: FragmentContactGroupAssignationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        contact = args.contact

        var groupsAsJson = args.groupsAsJson
        var groups = Gson().fromJson(groupsAsJson, Array<ContactGroup>::class.java).toList()



        binding = FragmentContactGroupAssignationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )



        createChips(groups)

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val selectedGroup = chip.tag as ContactGroup

            viewModel.selectUnselectContactGroup(contact, selectedGroup, chip.isChecked)


        }

        binding.closeButton.setOnClickListener {
            requireContext().handleTouch()
            findNavController().popBackStack()
            //val groupName = binding.groupName.text.toString().capitalizeFirstAndLongWords()

            //viewModel.insertGroup(groupName)

        }
        return binding.root
    }

    private fun createChips(groups: List<ContactGroup>) {
        binding.chipGroup.removeAllViews()
        for (item in groups) {
            val chip = Chip(requireContext())
            chip.text = item.list_name
            chip.isCheckable = true
            chip.isChecked =  contact.groups?.containsKey(item.list_key) ?: false
            chip.isClickable = true
            chip.setTextColor(Color.WHITE)
            chip.setChipBackgroundColorResource(R.color.colorPrimary)
            chip.setCheckedIconEnabled(true)
            chip.setCheckedIconVisible(true)
            chip.setChipIconEnabled(false)
            chip.tag = item

            chip.setOnClickListener {
                val selectedGroup = chip.tag as ContactGroup
                viewModel.selectUnselectContactGroup(contact, selectedGroup, chip.isChecked)
            }



            binding.chipGroup.addView(chip)
        }
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
                }

                null -> {}


            }
        }
    }

    private fun stopObservers() {
        viewModel.insertContactGroupAction.removeObservers(this)
    }
}

