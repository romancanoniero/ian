package com.iyr.ian.ui.events.fragments.adapters

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventType
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.utils.UIUtils.handleTouch

interface EventTypeSelectorCallback {
    fun onEventTypeSelected(eventType: EventTypesEnum)
}

class EventTypeAdapter(val con: Context, val callback: EventTypeSelectorCallback) :
    RecyclerView.Adapter<EventTypeAdapter.UserViewHolder>() {

    private var mContext: Context = con
    private var mList: java.util.ArrayList<EventType> = ArrayList<EventType>()


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_type_adapter, parent, false)
    )

    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val record: EventType = mList[position]



        // Calculate item width
        val displayMetrics: DisplayMetrics = mContext.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth *.348837)
        val spacing = (screenWidth - (itemWidth*2)) / 3

//        val itemWidth = (screenWidth - 3 * spacing) / 2

        // Set item width
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = itemWidth.toInt()
        holder.itemView.layoutParams = layoutParams

        Glide.with(con)
            .asBitmap()
            .load(record.imageResId)
            .into(holder.avatarImage)


        holder.textLine1.text = con.getText(record.firsLineResId)
        holder.textLine2.text = con.getText(record.secondLineResId)

        holder.container.setOnClickListener {
            callback.let { _ ->
                con.handleTouch()
                callback.onEventTypeSelected(EventTypesEnum.valueOf(record.event_type_key))
            }
        }

    }

    fun getData(): java.util.ArrayList<EventType> {
        return mList
    }

    fun setData(events: ArrayList<EventType>) {
        mList = events
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var container: View = view.findViewById<View>(R.id.container)
        var avatarImage: ImageView = view.findViewById<ImageView>(R.id.avatar_image)
        var textLine1: TextView = view.findViewById<TextView>(R.id.rotules_first_line)
        var textLine2: TextView = view.findViewById<TextView>(R.id.rotules_second_line)
    }
}