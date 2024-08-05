package com.iyr.ian.ui.contacts_groups

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnErrorCallback
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.databinding.FragmentContactsGroupsBinding
import com.iyr.ian.ui.contacts_groups.adapters.ContactsGroupsAdapter
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.ContactsGroupsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

interface ContactsGroupsFragmentCallback : OnErrorCallback,
    OnCreationGroupCallback {
    fun onGroupAdded(snapshot: DataSnapshot, previousChildName: String?)
    fun onGroupChanged(snapshot: DataSnapshot, previousChildName: String?)
    fun onGroupRemoved(snapshot: DataSnapshot)
    fun openGroup(notificationListKey: String, listName: String)
    fun onError(error: DatabaseError) {
    }

}

interface OnCreationGroupCallback {

}


class ContactsGroupsFragment(val mainViewModel: MainActivityViewModel, private val _interface: ISettingsFragment) : Fragment(),
    OnCreationGroupCallback,
    ContactsGroupsFragmentCallback {
    private lateinit var binding: FragmentContactsGroupsBinding
    private lateinit var adapter: ContactsGroupsAdapter
  //  private var presenter = ContactsGroupsPresenter(this)



    private val viewModel = ContactsGroupsFragmentViewModel(mainViewModel.userKey!!)
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactsGroupsBinding.inflate(layoutInflater, container, false)
        adapter = ContactsGroupsAdapter(requireContext(), this)
        setupUI()
        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance(context: Context, mainViewModel: MainActivityViewModel, _interface: ISettingsFragment) =
            ContactsGroupsFragment(mainViewModel, _interface)
    }


    private fun setupUI() {


        binding.recyclerList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerList.adapter = adapter

        binding.createButton.setOnClickListener {
            //val creationDialog = ContactGroupCreationDialog(requireActivity(), this)
            //creationDialog.show()
        }

    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.emergency_contacts))
        }
        subscribe()
    }

    private fun subscribe() {
        requireActivity().showSnackBar(binding.root, "Implementar el subscribe")
//        presenter.subscribe()
    }
/*
    override fun createGroup(listName: String) {

        requireActivity().showSnackBar(binding.root, "Implementar la actualizacion de los grupos")
        /*
        val callback = object : OnCompleteCallback {
            override fun onComplete(success: Boolean, result: Any?) {
                if (success) {
                    var newList = Gson().fromJson<NotificationList>(
                        result as String,
                        NotificationList::class.java
                    )
                }
            }

            override fun onError(exception: Exception) {
                var pp = 3
            }
        }

        ContactsWSClient.instance.createContactGroup(listName, callback)
*/
        viewModel.onCreateContactGroupClicked(listName)
    }

    override fun onCanceled() {

    }
*/
    override fun openGroup(notificationListKey: String, listName: String) {

        val extras = Bundle()
        extras.putString("list_key", notificationListKey)
        extras.putString("list_name", listName)
        _interface.goToFragment(SettingsFragmentsEnum.NOTIFICATION_LIST.ordinal, extras)

    }

    override fun onGroupAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val contactGroup = snapshot.getValue(ContactGroup::class.java)!!
        if (!adapter.getData().contains(contactGroup)) {
            adapter.getData().add((contactGroup))
            adapter.notifyItemInserted(adapter.getData().size - 1)
        }
    }

    override fun onGroupChanged(snapshot: DataSnapshot, previousChildName: String?) {

        val contactGroup = snapshot.getValue(ContactGroup::class.java)!!
        val index = adapter.getData().indexOf(contactGroup)
        if (index > -1) {
            adapter.getData()[index] = contactGroup
            adapter.notifyItemChanged(index)
        } else {
            onGroupAdded(snapshot, previousChildName)
        }
    }

    override fun onGroupRemoved(snapshot: DataSnapshot) {
        val contactGroup = snapshot.getValue(ContactGroup::class.java)!!
        val index = adapter.getData().indexOf(contactGroup)
        if (index > -1) {
            adapter.getData().removeAt(index)
            adapter.notifyItemRemoved(index)
        }
    }
}