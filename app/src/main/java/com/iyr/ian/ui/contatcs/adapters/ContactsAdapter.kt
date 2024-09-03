package com.iyr.ian.ui.contatcs.adapters

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.ui.contatcs.ContactsFragmentDirections
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactsAdapter(val fragment: Fragment) :
    RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {


    private val viewBinderHelper = ViewBinderHelper()
    private var groups: ArrayList<ContactGroup>? = null
    var filteredData: ArrayList<Contact> = ArrayList()
    private var items: ArrayList<Contact> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = View.inflate(parent.context, R.layout.item_contacts_contact_adapter, null)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val contact = filteredData[position]
        val friendshipStatus = FriendshipStatusEnums.valueOf(contact.status ?: "")

        when (friendshipStatus) {
            FriendshipStatusEnums.PENDING -> {
                Glide.with(holder.userImage.context)
                    .load(R.drawable.ic_sand_clock)
                    .into(holder.userImage)
            }

            FriendshipStatusEnums.ACCEPTED, FriendshipStatusEnums.NOT_A_FRIEND_BUT_EXISTS -> {
                // TODO: Poner aca la imagen del usuario


                val context = holder.userImage.context

                GlobalScope.launch(Dispatchers.IO) {
                context.assignFileImageTo(
                    contact.image?.file_name.toString(),
                    AppConstants.PROFILE_IMAGES_STORAGE_PATH + "/" + contact.user_key,
                    holder.userImage
                )
                }
/*
                Glide.with(holder.userImage.context)
                    .load(R.drawable.ic_close)
                    .into(holder.userImage)
*/
            }

            FriendshipStatusEnums.USER_NOT_FOUND -> {
                Glide.with(holder.userImage.context)
                    .load(R.drawable.ic_close)
                    .into(holder.userImage)

            }
        }


        holder.displayName.text = contact.display_name
        holder.additionalInfo.visibility = View.VISIBLE

        if (!contact.telephone_number.isNullOrEmpty())
            holder.additionalInfo.text = contact.telephone_number
        else
            if (!contact.email.isNullOrEmpty())
                holder.additionalInfo.text = contact.email
            else {
                holder.additionalInfo.visibility = View.INVISIBLE
            }


        holder.deleteButton.setOnClickListener {

            val userKey = UserViewModel.getInstance().getUser()?.user_key.toString()

            val action =
                ContactsFragmentDirections.actionContactsFragmentToContactsDeletionConfirmationDialog(
                    userKey,
                    contact
                )
            findNavController(fragment).navigate(action)
        }

        holder.cardView.setOnClickListener {

            val groupsAsJson = Gson().toJson(groups ?: ArrayList<ContactGroup>())
            val action =
                ContactsFragmentDirections.actionContactsFragmentToContactsGroupsAssignationDialog(
                    contact,
                    groupsAsJson
                )
            findNavController(fragment).navigate(action)
        }

        //---- Actualizo los chips
        holder.chipGroup.removeAllViews()
        for (item in groups ?: emptyList()) {
            if (contact.groups?.containsKey(item.list_key) ?: false) {
                val chip = Chip(fragment.requireContext())
                chip.text = item.list_name
                chip.isCheckable = true
                chip.isChecked = true
                chip.isClickable = false
                chip.setTextColor(Color.WHITE)
                chip.setCheckedIconTintResource(R.color.white)
                chip.setChipBackgroundColorResource(R.color.colorPrimary)
                chip.setCheckedIconEnabled(true)
                chip.setCheckedIconVisible(true)
                chip.setChipIconEnabled(false)
                chip.tag = item
                holder.chipGroup.addView(chip)
            }
        }

    }

    /*
        fun onBindViewHolder(holder: ContactsAdapter.MyViewHolder, contact: Contact) {

            val friendshipStatus = FriendshipStatusEnums.valueOf(contact.status ?: "")

            when (friendshipStatus) {
                FriendshipStatusEnums.PENDING -> {
                    Glide.with(holder.userImage.context)
                        .load(R.drawable.ic_sand_clock)
                        .into(holder.userImage)
                }

                FriendshipStatusEnums.ACCEPTED, FriendshipStatusEnums.NOT_A_FRIEND_BUT_EXISTS -> {
                    // TODO: Poner aca la imagen del usuario
                    Glide.with(holder.userImage.context)
                        .load(R.drawable.ic_close)
                        .into(holder.userImage)

                }

                FriendshipStatusEnums.USER_NOT_FOUND -> {
                    Glide.with(holder.userImage.context)
                        .load(R.drawable.ic_close)
                        .into(holder.userImage)

                }
            }


            holder.displayName.text = contact.display_name
            holder.additionalInfo.visibility = View.VISIBLE

            if (!contact.telephone_number.isNullOrEmpty())
                holder.additionalInfo.text = contact.telephone_number
            else
                if (!contact.email.isNullOrEmpty())
                    holder.additionalInfo.text = contact.email
                else {
                    holder.additionalInfo.visibility = View.INVISIBLE
                }


            holder.deleteButton.setOnClickListener {

                val userKey = UserViewModel.getInstance().getUser()?.user_key.toString()

                val action =
                    ContactsFragmentDirections.actionContactsFragmentToContactsDeletionConfirmationDialog(
                        userKey,
                        contact
                    )
                findNavController(fragment).navigate(action)
            }

            holder.cardView.setOnClickListener {

                val groupsAsJson = Gson().toJson(groups ?: ArrayList<ContactGroup>())
                val action =
                    ContactsFragmentDirections.actionContactsFragmentToContactsGroupsAssignationDialog(
                        contact,
                        groupsAsJson
                    )
                findNavController(fragment).navigate(action)
            }

            //---- Actualizo los chips
            holder.chipGroup.removeAllViews()
            for (item in groups ?: emptyList()) {
                if (contact.groups?.containsKey(item.list_key) ?: false) {
                    val chip = Chip(fragment.requireContext())
                    chip.text = item.list_name
                    chip.isCheckable = true
                    chip.isChecked = true
                    chip.isClickable = true
                    chip.setTextColor(Color.WHITE)
                    chip.setCheckedIconTintResource(R.color.white)
                    chip.setChipBackgroundColorResource(R.color.colorPrimary)
                    chip.setCheckedIconEnabled(true)
                    chip.setCheckedIconVisible(true)
                    chip.setChipIconEnabled(false)
                    chip.tag = item
                    holder.chipGroup.addView(chip)
                }
            }

        }
    */
    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }


    override fun getItemCount(): Int = filteredData.size

    fun getData() = items

    fun setData(data: ArrayList<Contact>) {
        items = data
        notifyDataSetChanged()
    }


    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            ArrayList(items) // if query is empty, show all items
        } else {
            items.filter {
                (it.display_name ?: "").contains(query, ignoreCase = true) ||
                        (it.telephone_number ?: "").contains(query, ignoreCase = true) ||
                        (it.email ?: "").contains(query, ignoreCase = true)
            } as ArrayList<Contact>
        }

        // ContactsFragmentViewModel.getInstance().updateVisibleContacts(items)

        notifyDataSetChanged()
    }

    fun setGroupsData(contactGroups: java.util.ArrayList<ContactGroup>) {
        this.groups = contactGroups
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var cardView: CardView = view.findViewById(R.id.card_view)
        var userImage: ImageView = view.findViewById(R.id.user_image)
        var displayName: TextView = view.findViewById(R.id.display_name)
        var additionalInfo: TextView = view.findViewById(R.id.additional_info)
        var deleteButton: ImageView = view.findViewById(R.id.delete_icon)
        var chipGroup: ChipGroup = view.findViewById(R.id.chip_group)

    }
}