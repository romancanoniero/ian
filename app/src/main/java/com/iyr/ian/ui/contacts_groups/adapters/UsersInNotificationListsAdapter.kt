package com.iyr.ian.ui.contacts_groups.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.UserInNotificationList
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.ui.contacts_groups.NotificationListFragment
import com.iyr.ian.utils.UIUtils.handleTouch


interface IContactLists {
    fun onNewListRequired()
    fun onContactListSelected(contactListKey: String)
}

class UsersInNotificationListsAdapter(
    val context: Context,
    val callback: NotificationListFragment
) :
    RecyclerView.Adapter<UsersInNotificationListsAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    private var data: java.util.ArrayList<UserInNotificationList> =
        ArrayList<UserInNotificationList>()

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

// TODO: Pasarlo a Coroutina
        val storageReference = FirebaseStorage.getInstance()
            .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
            .child(record.user_key)
            .child(record.profile_image_path)

        try {

            Log.d("GLIDEAPP","8")


            GlideApp.with(context)
                .asBitmap()
                .load(storageReference)
                .placeholder(AppCompatResources.getDrawable(context, R.drawable.progress_animation))
                .error(AppCompatResources.getDrawable(context, R.drawable.ic_error))
                .into(holder.userImage)
        }
        catch (exception: Exception) {
            var pp = 3
        }

        holder.userName.text = record.display_name

        Glide.with(context)
            .asBitmap()
            .load(R.drawable.ic_delete)
            .into(holder.primaryActionButton)


        //               holder.primaryActionText.text = context.getText(R.string.remove)
        holder.primaryActionButton.setOnClickListener {
            //   callback.clearSearchBox()
            context.handleTouch()
            callback.removeMember(record.user_key)
        }
/*
        holder.secondLine.visibility = View.GONE
        if (record.have_phone && !record.telephone_number.isNullOrEmpty()) {
            holder.switchSpeedDialSection.visibility = View.VISIBLE
            holder.switchSpeedDial.isChecked = record.add_to_speed_dial
        } else
            holder
                .switchSpeedDialSection.visibility = View.INVISIBLE
*/

        holder.switchSpeedDial.setOnCheckedChangeListener { _, enabled ->

            Toast.makeText(context, "Implementar updateSpeedDialStatus", Toast.LENGTH_LONG).show()
           /*
            ContactsWSClient.instance.updateSpeedDialStatus(
                record.user_key,
                enabled,
                object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {

                    }
                })

            */
        }


    }


    fun getData(): ArrayList<UserInNotificationList> {
        return data
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var userImage: ImageView = view.findViewById<ImageView>(R.id.user_image)
        var userName: TextView = view.findViewById<TextView>(R.id.user_name)
        var primaryActionButton: ImageView =
            view.findViewById<ImageView>(R.id.primary_action_button)


        var switchSpeedDial: Switch = view.findViewById<Switch>(R.id.switch_speed_dial)
        var switchSpeedDialSection: LinearLayout =
            view.findViewById<LinearLayout>(R.id.speed_dial_section)
        var secondLine: TextView = view.findViewById<TextView>(R.id.second_line)

    }


}