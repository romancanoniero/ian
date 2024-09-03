package com.iyr.ian.ui.map.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.ui.base.PulseValidationStatus
import com.iyr.ian.utils.assignFileImageTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UsersParticipatingAdapter(val activity: Activity) :
    RecyclerView.Adapter<UsersParticipatingAdapter.UserViewHolder>() {
    var list = ArrayList<EventFollower>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_participating_adapter, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {


        val record = list[position]

        if (!record.is_author) {
            holder.additionalText.visibility = GONE
            if (record.following_start_time != null) {
                holder.followingStatus.visibility = GONE
                holder.additionalText.visibility = GONE
                holder.statusIndicator.visibility = GONE
                if (record.following_start_time != null) {
                    holder.followingStatus.text = activity.getText(R.string.following)
                    holder.followingStatus.setBackgroundColor(activity.getColor(R.color.white))
                    holder.followingStatus.setTextColor(activity.getColor(R.color.black))
                    holder.followingStatus.visibility = VISIBLE
                }
                if (record.call_time != null) {
                    holder.calledIndicator.visibility = VISIBLE
                }
                if (record.going_time != null) {
                    holder.goingIndicator.visibility = VISIBLE
                }

            } else {
                holder.statusIndicator.visibility = GONE
                holder.goingIndicator.visibility = GONE
                holder.calledIndicator.visibility = GONE
                holder.followingStatus.text = activity.getText(R.string.pending)
                holder.followingStatus.setBackgroundColor(activity.getColor(R.color.gray))
                holder.followingStatus.setTextColor(activity.getColor(R.color.gray_600))
                holder.followingStatus.visibility = VISIBLE

            }


        } else {
            holder.followingStatus.visibility = GONE
            holder.additionalText.visibility = VISIBLE
            holder.goingIndicator.visibility = GONE
            holder.calledIndicator.visibility = GONE
            //      holder.statusIndicator.visibility = VISIBLE

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

        holder.userImage.visibility = View.INVISIBLE
        GlobalScope.launch(Dispatchers.IO) {

            activity.assignFileImageTo(
                record.profile_image_path,
                "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${record.user_key}",
                holder.userImage
            )
        }
        holder.userImage.visibility = VISIBLE


        holder.batteryStatusSection.visibility = View.INVISIBLE
        if (record.battery_percentage != 0.0) {
//            holder.batteryLevel.progress = (record.battery_percentage ).roundToInt()
            // En algún lugar de tu código donde necesites actualizar el progreso
            updateProgressBarColor(holder.batteryLevel, record.battery_percentage.toInt()) // Ejemplo con un progreso del 30%
            holder.batteryStatusSection.visibility = VISIBLE
        }
    }

    fun setData(options: ArrayList<EventFollower>) {
        this.list = options
    }


    fun getData(): ArrayList<EventFollower> {
        return list
    }

    // MainActivity.kt
    fun updateProgressBarColor(progressBar: ProgressBar, progress: Int) {
        val drawable = progressBar.progressDrawable as LayerDrawable
        val progressDrawable = drawable.findDrawableByLayerId(android.R.id.progress) as ClipDrawable

        when {
            progress > 50 -> {
                progressDrawable.drawable?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
            }
            progress in 15..50 -> {
                val ratio = (progress - 15) / 35.0f
                val red = (255 * (1 - ratio)).toInt()
                val yellow = (255 * ratio).toInt()
                progressDrawable.drawable?.setColorFilter(Color.rgb(red, yellow, 0), PorterDuff.Mode.SRC_IN)
            }
            else -> {
                progressDrawable.drawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
            }
        }
        progressBar.progress = progress
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.findViewById(R.id.user_name)
        var userImage: ImageView = view.findViewById(R.id.user_image)
        var additionalText: TextView = view.findViewById(R.id.additional_text)
        var goingIndicator: TextView = view.findViewById(R.id.going_indicator)
        var calledIndicator: TextView = view.findViewById(R.id.already_called_indictator)
        var statusIndicator: TextView = view.findViewById(R.id.status_indictator)
        var followingStatus: TextView = view.findViewById(R.id.following_status)
        var batteryLevel: ProgressBar = view.findViewById(R.id.progress_battery)
        var batteryStatusSection: ConstraintLayout = view.findViewById(R.id.battery_status_section)

    }


}