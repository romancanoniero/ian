package com.iyr.ian.ui.views.home.fragments.main.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.ui.main.dialogs.NewUserInvitationDialog
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.versionPrefix
import de.hdodenhof.circleimageview.CircleImageView

interface ISpeedDialAdapter {
    fun makeAPhoneCall(phoneNumber: String)
    fun sendSMSInvitation(contactName: String, phoneNumber: String)
}


class SpeedDialAdapter(val activity: Activity, val callback: ISpeedDialAdapter) :
    RecyclerView.Adapter<SpeedDialAdapter.UserViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    var contacts: java.util.ArrayList<SpeedDialContact> = ArrayList<SpeedDialContact>()

    init {

    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_speed_dial_adapter, parent, false)
    )

    override fun getItemCount(): Int {
        if (activity.versionPrefix() >= 2)
            return contacts.size
        else
            return contacts.size + 1
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        var isExistingRecord = position < contacts.size

        if (isExistingRecord) {
            val record = contacts[position]

            try {

                if (record.image!=null) {
                    val storageReference = FirebaseStorage.getInstance()
                        .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                        .child(record.user_key.toString())
                        .child(record.image!!.file_name)

                    Glide.with(activity)
                        .asBitmap()
                        .load(storageReference)
                        .placeholder(activity.getDrawable(R.drawable.progress_animation))
                        .error(activity.getDrawable(R.drawable.error_circle))
                        .into(holder.userImage)
                } else {
                    holder.userImage.circleBackgroundColor = activity.getColor(R.color.white)
                    Glide.with(activity)
                        .asBitmap()
                        .load(R.drawable.ic_unknown_user)
                        .into(holder.userImage)
                }
            } catch (exception: Exception) {
                var pp = 33
            }
            holder.userName.text = record.display_name.uppercase()
            holder.userPhoneNumber.text = record.telephone_number

            holder.frame.setOnClickListener {
                Log.d("SPEED_DIAL", "Click")
//                activity.makeAPhoneCall(record.telephone_number)
                callback.makeAPhoneCall(record.telephone_number!!)
            }


            holder.userName.setOnClickListener { view ->
                (view.parent as View).performClick()
            }
            holder.userPhoneNumber.setOnClickListener { view ->
                (view.parent as View).performClick()
            }

            holder.innerContainer.setOnClickListener { view ->
                (view.parent as View).performClick()
            }
            holder.innerFrame.setOnClickListener { view ->
                (view.parent as View).performClick()
            }
            holder.line2.visibility = View.VISIBLE
        } else {

            holder.userName.setText(R.string.add_new_contact)

            holder.userImage.circleBackgroundColor = activity.getColor(R.color.white)

            Glide.with(activity)
                .asBitmap()
                .load(R.drawable.ic_friend_add)
                .placeholder(activity.getDrawable(R.drawable.progress_animation))
                .error(activity.getDrawable(R.drawable.ic_error))
                .into(holder.userImage)
            holder.line2.visibility = View.GONE

            /*
                        holder.frame.setOnTouchListener { v, event ->
                            Toast.makeText(activity, "Show user invitation dialog", Toast.LENGTH_SHORT).show()
                            return@setOnTouchListener true
                }
              */


            holder.frame.setOnClickListener {
/*
                if (checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PermissionChecker.PERMISSION_GRANTED)
                {
                    if (shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.SEND_SMS)) {
                    } else {
                        requestPermissions(activity,
                            arrayOf(Manifest.permission.SEND_SMS),
                            MY_PERMISSION_REQUEST_SEND_SMS);
                    }
                 return@setOnClickListener
                }

 */
                activity.handleTouch()
                var invitationDialog = NewUserInvitationDialog(activity)
                invitationDialog.show()
            }
        }


        holder.line2.setOnClickListener {
            holder.frame.performClick()
        }
        holder.userImage.setOnClickListener {
            holder.frame.performClick()
        }
        holder.userName.setOnClickListener {
            holder.frame.performClick()
        }
        holder.userPhoneNumber.setOnClickListener {
            holder.frame.performClick()
        }

    }

    fun setData(speedDialContacts: ArrayList<SpeedDialContact>) {
        this.contacts = speedDialContacts
    }


    fun getData(): ArrayList<SpeedDialContact> {
        return contacts
    }


    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userImage: CircleImageView = view.findViewById<CircleImageView>(R.id.profile_image)
        var userName: TextView = view.findViewById<TextView>(R.id.user_name)
        var userPhoneNumber: TextView = view.findViewById<TextView>(R.id.user_phone_number)

        var innerFrame: LinearLayout = view.findViewById<LinearLayout>(R.id.inner_frame)
        var innerContainer: LinearLayout = view.findViewById<LinearLayout>(R.id.inner_container)
        var line2: LinearLayout = view.findViewById<LinearLayout>(R.id.line2)

        var frame: CardView = view.findViewById<CardView>(R.id.frame)!!

    }


}