package com.iyr.fewtouchs.ui.views.signup.phone_contacts.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.R
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.PhoneContact
import java.util.Collections


class PhoneContactsAdapter(val context: Context) :
    RecyclerView.Adapter<PhoneContactsAdapter.UserViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    private var existingContacts: ArrayList<PhoneContact> = ArrayList<PhoneContact>()
    private var contactsToShow: ArrayList<PhoneContact> = ArrayList<PhoneContact>()
    private var resultsFromSearches: ArrayList<PhoneContact> = ArrayList<PhoneContact>()
    private var compoundList: ArrayList<PhoneContact> = ArrayList<PhoneContact>()

    init {
    }

    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_contact_adapter, parent, false)
    )

    override fun getItemCount(): Int {
        return contactsToShow.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val record = contactsToShow[position]
        holder.userName.text = record.display_name

        if (record.telephone_number != null) {
            holder.phoneNumber.text = record.telephone_number
        } else
            holder.phoneNumber.isVisible = false


        if (record.email != null) {
            holder.emailAddress.text = record.email
            holder.emailAddress.isVisible = true
        } else {
            holder.emailAddress.isVisible = false
            holder.emailAddress.text = ""

        }

        holder.selector.isChecked = record.selected

        holder.selector.setOnCheckedChangeListener { _, isChecked ->
            record.selected = isChecked
        }

        if (record.image_uri != null) {
            GlideApp.with(context)
                .asBitmap()
                .load(record.image_uri)
                .placeholder(context.getDrawable(R.drawable.progress_animation))
                .error(context.getDrawable(R.drawable.ic_error))
                .into(holder.userImage)
        } else {
            Glide.with(context)
                .asBitmap()
                .load(R.drawable.ic_unknown_user)
                .into(holder.userImage)
        }


    }

    fun setData(contacts: ArrayList<PhoneContact>) {
        existingContacts = contacts
        filter()
    }

    fun addContact(contact: PhoneContact) {
        if (existingContacts.contains(contact)) return
        existingContacts.add(contact)
        existingContacts.sortBy { it.display_name }
        filter()
        val position = existingContacts.indexOf(contact)
        notifyItemInserted(position)
    }


    fun getData(): ArrayList<PhoneContact> {
        return contactsToShow
    }

    fun addFriendsFromSearch(users: ArrayList<PhoneContact>) {
        resultsFromSearches = users
        combineTables()
    }

    fun sortAlphabetically() {

        val alphabeticalOrder: Comparator<PhoneContact> =
            Comparator<PhoneContact> { c1, c2 ->
                val res = String.CASE_INSENSITIVE_ORDER.compare(
                    c1.display_name.toString(),
                    c2.display_name.toString()
                )
                res
            }
        Collections.sort(existingContacts, alphabeticalOrder)
    }


    var textFilter: String = ""
    fun filter() {
        val filteredContacts = ArrayList<PhoneContact>()

        if (textFilter.isEmpty()) {
            contactsToShow = existingContacts
            notifyDataSetChanged()
            return
        }
        else {
            existingContacts.forEach { contact ->
                if (contact.display_name!!.contains(textFilter, ignoreCase = true) == true ||
                    contact.telephone_number!!.contains(textFilter, ignoreCase = true) == true
                ) {
                    filteredContacts.add(contact)
                }
            }
            contactsToShow = filteredContacts
            notifyDataSetChanged()
        }
    }

    private fun combineTables() {

    }

    fun clearUsersFromSearchList() {
        resultsFromSearches.clear()
        combineTables()
    }

    fun selectAll() {
        existingContacts.forEach { record ->
            record.selected = true
        }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        existingContacts.forEach { record ->
            record.selected = false
        }
        notifyDataSetChanged()
    }

    fun getSelectedCound(): Int {
        var selecteds = 0
        existingContacts.forEach { record ->

            if (record.selected) selecteds++
        }
        return selecteds
    }

    fun setFilter(text: String) {
        textFilter = text
        filter()
    }

    fun getSelectedContacts(): List<PhoneContact> {
        return existingContacts.filter { it.selected }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userImage: ImageView = view.findViewById<ImageView>(R.id.user_image)
        var userName: TextView = view.findViewById<TextView>(R.id.user_name)
        var phoneNumber: TextView = view.findViewById<TextView>(R.id.phone_number)
        var emailAddress: TextView = view.findViewById<TextView>(R.id.email_address)
        var selector: CheckBox = view.findViewById<CheckBox>(R.id.selector)
    }


}