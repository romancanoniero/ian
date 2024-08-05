package com.iyr.ian.ui.contacts_groups.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.ui.contacts_groups.ContactsGroupsFragmentCallback


interface IContactGroups {
    fun onNewListRequired()
    fun onContactListSelected(contactListKey: String)
}

class ContactsGroupsAdapter(val context: Context, val callback: ContactsGroupsFragmentCallback) :
    RecyclerView.Adapter<ContactsGroupsAdapter.ViewHolder>() {

    private var data: java.util.ArrayList<ContactGroup> = ArrayList<ContactGroup>()

    init {
    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacts_group_adapter, parent, false)
    )

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record = data[position]
        holder.listName.text = record.list_name
        holder.membersCount.text = record.members.size.toString()

        holder.container.setOnClickListener {
            callback.openGroup(record.list_key, record.list_name)
        }
    }


    fun getData(): ArrayList<ContactGroup> {
        return data
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var container: View = view.findViewById<View>(R.id.container)

        var listName: TextView = view.findViewById<TextView>(R.id.list_name)
        var membersCount: TextView = view.findViewById<TextView>(R.id.members_count)


    }


}