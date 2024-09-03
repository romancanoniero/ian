package com.iyr.ian.ui.chat.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.R
import com.iyr.ian.dao.models.SpeedMessage
import com.iyr.ian.utils.UIUtils.handleTouch


interface SpeedMessagesCallback {
    fun onSpeedMessageClick(speedMessage: SpeedMessage)
}


class SpeedMessagesAdapter(val mActivity: Activity, val callback: SpeedMessagesCallback) :
    RecyclerView.Adapter<SpeedMessagesAdapter.UserViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    var messages = ArrayList<SpeedMessage>()

    private var areButtonsEnabled: Boolean = true



    init {

    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speed_message_adapter, parent, false)
    )

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {



        val record = messages[position]
        holder.text.isEnabled = areButtonsEnabled
        holder.text.text = mActivity.getString(record.actionTitleResId)
        holder.text.setOnClickListener {
            mActivity.handleTouch()
            callback.onSpeedMessageClick(record)
        }
    }

    fun setData(options: ArrayList<SpeedMessage>) {
        this.messages = options
    }


    fun getData(): ArrayList<SpeedMessage> {
        return messages
    }

    fun setButtonsEnabled(enabled: Boolean) {
        areButtonsEnabled = enabled
        notifyDataSetChanged()
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var text: TextView = view.findViewById<TextView>(R.id.text)
    }


}