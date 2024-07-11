package com.iyr.ian.ui.map.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.ui.base.PulseValidationStatus
import kotlin.math.roundToInt


class UsersParticipatingAdapter(val activity: Activity) :
    RecyclerView.Adapter<UsersParticipatingAdapter.UserViewHolder>() {
    var list = ArrayList<EventFollower>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_participating_adapter, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val storageReference: StorageReference?
        val record = list[position]

        if (!record.is_author) {
            holder.aditionalText.visibility = GONE
            holder.statusIndicator.visibility = GONE
            if (record.call_time != null) {
                holder.calledIndicator.visibility = VISIBLE
            }
            if (record.going_time != null) {
                holder.goingIndicator.visibility = VISIBLE
            }
        } else {
            holder.aditionalText.visibility = VISIBLE
            holder.goingIndicator.visibility = GONE
            holder.calledIndicator.visibility = GONE
            holder.statusIndicator.visibility = VISIBLE

            when (PulseValidationStatus.valueOf(record.status)) {
                PulseValidationStatus.USER_OK -> {
                    holder.statusIndicator.text =
                        activity.getText(R.string.event_status_everything_ok)
                    holder.statusIndicator.setTextColor(activity.getColor(R.color.material_green500))
                }
                PulseValidationStatus.USER_NOT_RESPONSE -> {
                    holder.statusIndicator.text =
                        activity.getText(R.string.event_status_not_response)
                    holder.statusIndicator.setTextColor(activity.getColor(R.color.gray_dark))
                }
                PulseValidationStatus.WRONG_PIN -> {
                    holder.statusIndicator.text = activity.getText(R.string.event_status_wrong_pin)
                    holder.statusIndicator.setTextColor(activity.getColor(R.color.red))
                }
                PulseValidationStatus.USER_IN_TROUBLE -> {
                    holder.statusIndicator.text =
                        activity.getText(R.string.event_status_user_in_trouble)
                    holder.statusIndicator.setTextColor(activity.getColor(R.color.red))
                }

                else -> {

                }
            }
        }


        holder.userName.text = record.display_name.toString()
        storageReference = FirebaseStorage.getInstance()
            .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
            .child(record.user_key)
            .child(record.profile_image_path)


        if (holder.userImage.tag == null || holder.userImage.tag == storageReference.toString()) {
            GlideApp.with(activity)
                .asBitmap()
                .load(storageReference)
                .placeholder(getDrawable(activity, R.drawable.progress_animation))
                .error(getDrawable(activity, R.drawable.ic_error))
                .into(holder.userImage)
            holder.userImage.tag = storageReference
        }
        else
        {
            var pp = 3
        }


        holder.batteryStatusSection.visibility = View.INVISIBLE
        if (record.battery_level != 0.0) {
            holder.batteryLevel.progress = (record.battery_level * 100).roundToInt()
            holder.batteryStatusSection.visibility = VISIBLE
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
        var aditionalText: TextView = view.findViewById(R.id.additional_text)

        var goingIndicator: TextView = view.findViewById(R.id.going_indicator)
        var calledIndicator: TextView = view.findViewById(R.id.already_called_indictator)
        var statusIndicator: TextView = view.findViewById(R.id.status_indictator)
        var batteryLevel: ProgressBar = view.findViewById(R.id.progress_battery)
        var batteryStatusSection: ConstraintLayout = view.findViewById(R.id.battery_status_section)

    }


}