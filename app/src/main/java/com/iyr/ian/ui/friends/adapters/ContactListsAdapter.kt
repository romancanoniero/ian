package com.iyr.fewtouchs.ui.views.home.fragments.friends.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.R
import com.iyr.ian.dao.models.ContactGroup


interface IContactLists {
    fun onNewListRequired()
    fun onContactListSelected(contactListKey: String)
}

class ContactListsAdapter(val context: Context) :
    RecyclerView.Adapter<ContactListsAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    private var data: java.util.ArrayList<ContactGroup> = ArrayList<ContactGroup>()

    init {
    }

    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_list_adapter, parent, false)
    )

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record = data[position]
        viewBinderHelper.setOpenOnlyOne(true)
        //viewBinderHelper.bind(holder.swipeContainer, record.list_key);
        holder.listName.text = record.list_name


    }


    fun getData(): ArrayList<ContactGroup> {
        return data
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        //   var swipeContainer = view.findViewById<SwipeRevealLayout>(R.id.swipeContainer)

        var listName: TextView = view.findViewById<TextView>(R.id.list_name)

        /*
              var primaryActionButton = view.findViewById<View>(R.id.primary_action_button)
              var primaryActionIcon = view.findViewById<ImageView>(R.id.primary_action_icon)
              var primaryActionText = view.findViewById<TextView>(R.id.primary_action_title)
              var secondaryActionButton = view.findViewById<View>(R.id.secondary_action_button)
              var secondaryActionImage = view.findViewById<ImageView>(R.id.secondary_action_icon)
              var secondaryActionTitle = view.findViewById<TextView>(R.id.secondary_action_title)
              var switchSpeedDial = view.findViewById<Switch>(R.id.switch_speed_dial)
              var switchSpeedDialSection = view.findViewById<LinearLayout>(R.id.switch_speed_dial_section)
      */
        //        var statusStamp = view.findViewById<TextView>(R.id.status_stamp)
        var secondLine: TextView = view.findViewById<TextView>(R.id.second_line)

    }


}