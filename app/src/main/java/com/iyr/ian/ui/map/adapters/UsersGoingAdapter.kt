package com.iyr.ian.ui.map.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import java.util.Locale


class UsersGoingAdapter(val activity: Activity) :
    RecyclerView.Adapter<UsersGoingAdapter.UserViewHolder>() {
    var list = ArrayList<EventFollower>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_user_going_adapter, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val storageReference: Any?
        val record = list[position]

        holder.userName.text = record.display_name.toString()


        try {

            storageReference = StorageRepositoryImpl().generateStorageReference("${AppConstants.PROFILE_IMAGES_STORAGE_PATH}${record.user_key}/${record.profile_image_path}")

            GlideApp.with(activity)
                .asBitmap()
                .load(storageReference)
                .placeholder(getDrawable(activity, R.drawable.progress_animation))
                .error(getDrawable(activity, R.drawable.ic_error))
                .into(holder.userImage)
        }
        catch (ex: Exception) {
            var pp = 3
        }
        if (record.call_time != null) {
            val localeByLanguageTag: Locale =
                Locale.forLanguageTag(Locale.getDefault().toLanguageTag())

            val timeAgoLocale: TimeAgoMessages =
                TimeAgoMessages.Builder().withLocale(localeByLanguageTag).build()

            holder.timeMark.visibility = View.VISIBLE
            holder.timeMark.text = TimeAgo.using(record.call_time!!, timeAgoLocale)
        } else {
            holder.timeMark.visibility = View.INVISIBLE
        }

    }

    fun setData(options: ArrayList<EventFollower>) {
        this.list = options
    }


    fun getData(): ArrayList<EventFollower> {
        return list
    }


    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.findViewById(R.id.user_name)
        var userImage: ImageView = view.findViewById(R.id.user_image)
        var timeMark: TextView = view.findViewById(R.id.time_a_go)

    }


}