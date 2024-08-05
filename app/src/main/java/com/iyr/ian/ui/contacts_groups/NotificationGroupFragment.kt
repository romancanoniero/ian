package com.iyr.ian.ui.contacts_groups

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseError
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnErrorCallback
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.UserInNotificationList
import com.iyr.ian.databinding.FragmentContactGroupBinding
import com.iyr.ian.ui.contacts_groups.adapters.UsersInNotificationListsAdapter
import com.iyr.ian.ui.contacts_groups.multi_select_spinner.model.KeyPairBoolData
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.utils.components.multi_select_spinner.MultiSpinnerInterface
import com.iyr.ian.utils.components.multi_select_spinner.MultiSpinnerListener
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.MainActivityViewModel


interface NotificationListFragmentCallback : OnErrorCallback {

    fun onError(error: DatabaseError) {
    }

    fun onMemberAdded(user: UserInNotificationList) {}
    fun onMemberChanged(user: UserInNotificationList) {}
    fun onMemberRemoved(user: UserInNotificationList) {}
    fun onContactAdded(contact: Contact) {}
    fun onContactChanged(contact: Contact) {}
    fun onContactRemoved(contact: Contact) {}
    fun removeMember(userKey: String)

}

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationListFragment(
    val mainActivityViewModel: MainActivityViewModel,
    _interface: ISettingsFragment
) : Fragment(),
    NotificationListFragmentCallback,
    MultiSpinnerInterface {
    private var listName: String = ""
    private lateinit var notificationListKey: String

    private lateinit var binding: FragmentContactGroupBinding
    private lateinit var adapter: UsersInNotificationListsAdapter

    //  private var presenter = ContactGroupPresenter(this)
    private val contactListArray: ArrayList<KeyPairBoolData> = ArrayList()
    private val contactFilteredArray: ArrayList<KeyPairBoolData> = ArrayList()

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
        arguments?.let { extras ->
            notificationListKey = extras.getString("list_key").toString()

            listName = extras.getString("list_name").toString()
            subscribe()
        }

        // Inflate the layout for this fragment
        binding = FragmentContactGroupBinding.inflate(layoutInflater, container, false)
        adapter = UsersInNotificationListsAdapter(requireContext(), this)
        setupUI()
        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param context Parameter 1.
         * @param _interface Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            context: Context,
            mainActivityViewModel: MainActivityViewModel,
            _interface: ISettingsFragment
        ) =
            ContactsGroupsFragment(mainActivityViewModel, _interface)
    }


    private fun setupUI() {
        filterMultiSelector()
        binding.recyclerList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerList.adapter = adapter
        binding.usersMultiSelection.isSearchEnabled = true
        binding.usersMultiSelection.setNotificationListKey(notificationListKey)


        // A text that will display in search hint.
        // A text that will display in search hint.
        binding.usersMultiSelection.setSearchHint(getString(R.string.click_to_search))

        // Set text that will display when search result not found...


        binding.usersMultiSelection.setCallbackListener(this)
        // Set text that will display when search result not found...
        binding.usersMultiSelection.setEmptyTitle(getString(R.string.no_results))

        // If you will set the limit, this button will not display automatically.

        // If you will set the limit, this button will not display automatically.
        binding.usersMultiSelection.isShowSelectAllButton = true

        //A text that will display in clear text button

        //A text that will display in clear text button
        binding.usersMultiSelection.setClearText(getString(R.string.clear))

        // Removed second parameter, position. Its not required now..
        // If you want to pass preselected items, you can do it while making listArray,
        // Pass true in setSelected of any item that you want to preselect

        // Removed second parameter, position. Its not required now..
        // If you want to pass preselected items, you can do it while making listArray,
        // Pass true in setSelected of any item that you want to preselect

        var listArray1 = ArrayList<KeyPairBoolData>()
        var amigo1 = KeyPairBoolData()
        amigo1.name = "Amigo"
        amigo1.id = 1
        amigo1.isSelected = false

        listArray1.add(amigo1)



        binding.usersMultiSelection.setItems(listArray1,
            object : MultiSpinnerListener {
                override fun onItemsSelected(selectedItems: List<KeyPairBoolData?>?) {
                    for (i in selectedItems?.indices!!) {
                        if (selectedItems[i]?.isSelected == true) {
                            Log.i(
                                "MULTISELECTOR",
                                i.toString() + " : " + selectedItems[i]?.name + " : " + selectedItems[i]?.isSelected
                            )
                        }
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(listName)
        }
        subscribe()
    }

    private fun filterMultiSelector() {
        contactFilteredArray.clear()
        contactListArray.forEach { keyPairBoolData ->
            val contact = (keyPairBoolData.`theObject` as Contact)
            if (!isAlreadyMember(contact.user_key.toString())) {
                contactFilteredArray.add(keyPairBoolData)
            }
        }
        /*
        // TODO: Agregar la funcionalidad desde la libreria

              binding.usersMultiSelection.setItems(contactFilteredArray, -1, object : SpinnerListener {
                  override fun onItemsSelected(items: MutableList<KeyPairBoolData>?) {

                      items?.forEach { newOne ->
                          if (newOne.isSelected) {
                              val contact: Contact = newOne.`theObject` as Contact
                              val newRecord = UserInNotificationList()
                              newRecord.user_key = contact.user_key.toString()
                              newRecord.display_name = contact.display_name.toString()
                              newRecord.image = contact.image
                              newRecord.have_phone = contact.have_phone
                              newRecord.telephone_number = contact.telephone_number
                              newRecord.add_to_speed_dial = contact.add_to_speed_dial
                              val data = adapter.getData()
                              if (!data.contains(newRecord)) {
                                  data.add(newRecord)
                                  adapter.notifyItemInserted(data.size - 1)
                                  filterMultiSelector()
                                  presenter.addMemberToList(notificationListKey!!, newRecord.user_key)

                              }
                          }
                      }


                  }
              })
      */
    }

    private fun isAlreadyMember(userKey: String): Boolean {
        var alreadyMember = false

        adapter.getData().forEach { record ->
            if (record.user_key
                    .compareTo(userKey) == 0
            ) {
                alreadyMember = true
                return@forEach
            }
        }
        return alreadyMember
    }

    private fun subscribe() {
        notificationListKey.let { key ->

            requireActivity().showSnackBar(
                binding.root,
                "Implementar en el ViewModel el metodo subscribe"
            )

            //presenter.subscribe(key)
        }
    }

    override fun removeMember(userKey: String) {
        var indexToRemove = -1

        adapter.getData().forEach { user ->
            if (user.user_key == userKey) {
                indexToRemove = adapter.getData().indexOf(user)
                return@forEach
            }
        }
        if (indexToRemove > -1) {
            adapter.getData().removeAt(indexToRemove)
            adapter.notifyItemRemoved(indexToRemove)
            filterMultiSelector()
        }
        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo removeMember"
        )

        //presenter.removeMember(notificationListKey!!, userKey)
    }

    override fun onMemberAdded(user: UserInNotificationList) {

        var indexOnArray = -1
        contactListArray.forEach { record ->
            indexOnArray++
            if ((record.`theObject` as Contact).user_key.toString()
                    .compareTo(user.user_key) == 0
            ) {
                return@forEach
            }
        }
        if (indexOnArray > -1) {
            contactListArray.removeAt(indexOnArray)
        }


        if (!adapter.getData().contains(user)) {
            adapter.getData().add((user))
            adapter.notifyItemInserted(adapter.getData().size - 1)
            filterMultiSelector()
        }
    }

    override fun onMemberChanged(user: UserInNotificationList) {
        val index = adapter.getData().indexOf(user)
        if (index > -1) {
            adapter.getData()[index] = user
            adapter.notifyItemChanged(index)
            filterMultiSelector()
        } else {
            onMemberAdded(user)
        }
    }

    override fun onMemberRemoved(user: UserInNotificationList) {
        val index = adapter.getData().indexOf(user)
        if (index > -1) {
            adapter.getData().removeAt(index)
            adapter.notifyItemRemoved(index)
            filterMultiSelector()
        }
    }

    override fun onContactAdded(contact: Contact) {

        if (contact.status != FriendshipStatusEnums.ACCEPTED.name)
            return

        var exists = false
        contactListArray.forEach { record ->
            if ((record.theObject as Contact).user_key.toString()
                    .compareTo(contact.user_key.toString()) == 0
            ) {
                exists = true
                return@forEach
            }
        }

        val h = KeyPairBoolData()
        h.id = System.currentTimeMillis()
        h.name = contact.display_name
        h.theObject = contact
        h.isSelected = false

        if (!exists) {
            contactListArray.add(h)
            filterMultiSelector()
        }

    }


    override fun onContactChanged(contact: Contact) {

        if (contact.status != FriendshipStatusEnums.ACCEPTED.name)
            return


        var index = -1
        contactListArray.forEach { record ->
            index++
            if ((record.theObject as Contact).user_key.toString()
                    .compareTo(contact.user_key.toString()) == 0
            ) {
                return@forEach
            }
        }
        val h = KeyPairBoolData()
        h.id = System.currentTimeMillis()
        h.name = contact.display_name
        h.theObject = contact
        h.isSelected = true

        if (index > -1) {
            contactListArray[index] = h
            filterMultiSelector()
        } else {
            onContactAdded(contact)
        }
    }

    override fun onContactRemoved(contact: Contact) {
        var index = -1
        contactListArray.forEach { record ->
            index++
            if ((record.theObject as Contact).user_key.toString()
                    .compareTo(contact.user_key.toString()) == 0
            ) {
                return@forEach
            }
        }
        if (index > -1) {
            contactListArray.removeAt(index)
            filterMultiSelector()
        }
    }


    override fun onAccept(selecteds: ArrayList<KeyPairBoolData>) {

        var usersSelected = ArrayList<UserInNotificationList>()
        selecteds.forEach { selected ->
            var newRecord = UserInNotificationList()
            newRecord.user_key = (selected.theObject as Contact).user_key.toString()
            newRecord.display_name = (selected.theObject as Contact).display_name ?: ""
            newRecord.profile_image_path = (selected.theObject as Contact).image!!.file_name
            usersSelected.add(newRecord)
        }
        //--------------------------------------------------------

        var userList = adapter.getData()
        userList.clear()

        //   userList.addAll(usersSelected)
        //   adapter.notifyDataSetChanged()


        // borro los que ya no estan seleccionados.
        var indexesToDelete = ArrayList<UserInNotificationList>()
        userList.forEach { existingUser ->

            if (!usersSelected.contains(existingUser)) {
                indexesToDelete.add(existingUser)
            }
        }

        indexesToDelete.forEach { user ->
            var index = userList.indexOf(user)
            userList.remove(user)
            adapter.notifyItemRemoved(index)
        }

        //-------------------------------
        usersSelected.forEach {
            if (!userList.contains(it)) {
                userList.add(it)
                adapter.notifyItemInserted(adapter.getData().size - 1)

            }
        }

        adapter.notifyDataSetChanged()

        //---- Actualizo
        var dataMap: HashMap<String, UserInNotificationList> =
            HashMap<String, UserInNotificationList>()
        adapter.getData().forEach { record ->
            dataMap.put(record.user_key, record)
        }


        requireActivity().showSnackBar(binding.root, "implementar updateNotificationsList")
        //ContactsWSClient.instance.updateNotificationsList(notificationListKey,dataMap)
    }
}

