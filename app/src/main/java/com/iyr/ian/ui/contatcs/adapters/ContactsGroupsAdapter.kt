package com.iyr.ian.ui.contatcs.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.ui.contatcs.ContactsFragmentDirections
import com.iyr.ian.viewmodels.UserViewModel

class ContactsGroupsAdapter(val fragment: Fragment) :
    RecyclerView.Adapter<ContactsGroupsAdapter.MyViewHolder>() {

    private var items: ArrayList<ContactGroup> = ArrayList()
    private var originalItems: ArrayList<ContactGroup> = ArrayList()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacts_group_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var item = items[position]

        val iconSize = fragment.getResources().getDimensionPixelSize(R.dimen.circle_5)
        val groupImage = fragment.getResources().getDrawable(R.drawable.ic_group_by_aleksei_ryazancev).toBitmap(iconSize,iconSize)
        holder.groupIcon.setImageBitmap(groupImage)

        holder.groupName.text = item.list_name ?: ""
        holder.membersCount.text = item.members.size.toString()

        holder.deleteIcon.setOnClickListener {
            val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
            val action = ContactsFragmentDirections.actionContactsFragmentToContactsGroupsDeletionConfirmationDialog(userKey, item)
            findNavController(fragment).navigate(action)
        }

    }

    override fun getItemCount(): Int = items.size

    fun getData() = items

    fun setData(data: ArrayList<ContactGroup>) {
        items = data
        originalItems = ArrayList(data)
        notifyDataSetChanged()
    }


    fun filter(query: String): ArrayList<ContactGroup> {
        items = if (query.isEmpty()) {
            ArrayList(originalItems) // if query is empty, show all items
        } else {
            originalItems.filter {
                it.list_name.contains(
                    query,
                    ignoreCase = true
                )
            } as ArrayList<ContactGroup>
        }


        // ContactsFragmentViewModel.getInstance().updateVisibleContactsGroups(items)

        //notifyDataSetChanged()
        return items
    }

    /**
     * Agrega un grupo al listado de grupos
     */
    fun addGroup(group: ContactGroup) {
        items.add(group)
        notifyItemInserted(items.size - 1)
    }


    /**
     * Remueve un grupo del listado de grupos
     */
    fun removeGroup(group: ContactGroup) {
        val index = items.indexOf(group)
        if (index > -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getAllItems(): ArrayList<ContactGroup> {
        return originalItems
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var layout: View = view.rootView
        var groupName: TextView = view.findViewById<TextView>(R.id.list_name)
        var membersCount: TextView = view.findViewById<TextView>(R.id.members_count)
        var deleteIcon: ImageView = view.findViewById<ImageView>(R.id.delete_icon)
        var groupIcon: ImageView = view.findViewById<ImageView>(R.id.group_icon)


    }
}