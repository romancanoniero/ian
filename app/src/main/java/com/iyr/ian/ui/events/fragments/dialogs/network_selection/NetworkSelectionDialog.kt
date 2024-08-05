package com.iyr.ian.ui.events.fragments.dialogs.network_selection


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.databinding.FragmentNetworkSelectionPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.viewmodels.EventsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel

interface OnNetworkListSelection {
    fun onSelected(listKey: String)
    fun onCanceled()

}

class NetworkSelectionDialog() :
    AppCompatDialogFragment() {

    private val viewModel by lazy { EventsFragmentViewModel.getInstance().getViewModel() }

    private lateinit var spinnerAdapter: ArrayAdapter<ContactGroup>

    private var mButton1Callback: OnCompleteCallback? = null
    //private val mDialoglayout: View

    private var notificationListKey: String? = null

    protected lateinit var binding: FragmentNetworkSelectionPopupBinding
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

        binding = FragmentNetworkSelectionPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        //    spinnerNotificationGroups = mDialoglayout.findViewById<Spinner>(R.id.groups_spinner)
        //      acceptButton = mDialoglayout.findViewById<Button>(R.id.buttonOne)

        //       val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancel_button)

        binding.radioButtonSelectAGroup.setOnClickListener { view ->
            binding.radioButtonSelectAGroup.isChecked = false
            binding.groupsSpinner.isEnabled = false
        }

        binding.radioButtonSelectAGroup.setOnClickListener { view ->
            binding.radioButtonSelectAGroup.isChecked = false
            binding.groupsSpinner.isEnabled = true
        }

        binding.radioButtonSelectAGroup.isChecked = true


        binding.buttonOne.setOnClickListener { view ->
            requireContext().handleTouch()

            var groupKey: String = ""
            if (binding.radioButtonSelectAGroup.isChecked) {

                var position = binding.groupsSpinner.selectedItemPosition
                groupKey =
                    (binding.groupsSpinner.adapter?.getItem(position) as ContactGroup).list_key
                groupKey = notificationListKey!!
            } else {
                groupKey = "all"
            }

            viewModel.onNotificationGroupSelected(groupKey)
            MainActivityViewModel.getInstance().onEventReadyToFire(viewModel.event.value!!)
            findNavController().popBackStack()
        }


        binding.cancelButton.setOnClickListener { view ->
            requireContext().handleTouch()
            findNavController().popBackStack()
        }



        return binding.root
    }

    /**
     * Prepare the spinner adapter
     */
    private fun setupGroupsAdapter(list: ArrayList<ContactGroup>) {
        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        binding.groupsSpinner.adapter = spinnerAdapter
        binding.groupsSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var position = binding.groupsSpinner.selectedItemPosition
                notificationListKey =
                    (binding.groupsSpinner.adapter?.getItem(position) as ContactGroup).list_key
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
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

            startObservers()
        }
    }


    override fun onStop() {
        super.onStop()
        stopObservers()
    }


    private fun startObservers() {
        EventsFragmentViewModel.getInstance().groupsList.observe(viewLifecycleOwner, { list ->
            list.let {
                setupGroupsAdapter(it!!)
            }
        })
    }

    private fun stopObservers() {
        EventsFragmentViewModel.getInstance().groupsList.removeObservers(viewLifecycleOwner)
    }


    /*
        private fun subscribeToNotificationLists() {
            notificationGroupsRef = FirebaseDatabase.getInstance()
                .getReference(TABLE_USERS_NOTIFICATIONS_GROUPS)
                .child(FirebaseAuth.getInstance().uid.toString())

            notificationGroupsRef.addChildEventListener(notificationGroupsListener)
        }

        private fun unsubscribeNotificationLists() {
            notificationGroupsRef.removeEventListener(notificationGroupsListener)
        }
    */

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }


}