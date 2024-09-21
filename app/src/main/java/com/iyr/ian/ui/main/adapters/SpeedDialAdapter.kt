package com.iyr.ian.ui.main.adapters

import android.Manifest
import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants.Companion.MY_PERMISSION_REQUEST_SEND_SMS
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.ui.main.dialogs.NewUserInvitationDialog
import com.iyr.ian.ui.views.home.fragments.main.adapters.ISpeedDialAdapter
import com.iyr.ian.utils.versionPrefix
import de.hdodenhof.circleimageview.CircleImageView


class SpeedDialAdapter(val activity: Activity, val callback: ISpeedDialAdapter) :
    RecyclerView.Adapter<SpeedDialAdapter.UserViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    var contacts: java.util.ArrayList<Contact> = ArrayList<Contact>()
    private var resultsFromSearches: ArrayList<Contact> = ArrayList<Contact>()
    private var compoundList: java.util.ArrayList<Contact> = ArrayList<Contact>()

    init {

    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): UserViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speed_dial_adapter, parent, false)


        var frame = itemView.findViewById<CardView>(R.id.frame)
        // Ajusta el ancho del ítem al 80% del ancho de la pantalla

        val displayMetrics = Resources.getSystem().displayMetrics
        val widthPx = displayMetrics.widthPixels * 0.90

        frame.layoutParams =
            RecyclerView.LayoutParams(widthPx.toInt(), RecyclerView.LayoutParams.WRAP_CONTENT)

        return UserViewHolder(itemView)


    }

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




                val storageReference = StorageRepositoryImpl().
                generateStorageReference("${AppConstants.PROFILE_IMAGES_STORAGE_PATH}${record.user_key.toString()}/${record.image!!.file_name}")

                Glide.with(activity)
                    .asBitmap()
                    .load(storageReference)
                    .placeholder(activity.getDrawable(R.drawable.progress_animation))
                    .error(activity.getDrawable(R.drawable.ic_error))
                    .into(holder.userImage)

            } catch (exception: Exception) {
                var pp = 33
            }

            holder.userName.text = (record.display_name ?: "").uppercase()
            holder.userPhoneNumber.text = record.telephone_number

            holder.frame.setOnClickListener {
                Log.d("SPEED_DIAL", "Click")
//                activity.makeAPhoneCall(record.telephone_number)
                callback.makeAPhoneCall(record.telephone_number ?: "")
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
                .load(R.drawable.sharp_add_black_36)
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
                //            Toast.makeText(activity, "Show user invitation dialog", Toast.LENGTH_SHORT).show()


                if (checkSelfPermission(
                        activity,
                        Manifest.permission.SEND_SMS
                    ) != PermissionChecker.PERMISSION_GRANTED
                ) {
                    if (shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.SEND_SMS
                        )
                    ) {
                    } else {
                        requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.SEND_SMS),
                            MY_PERMISSION_REQUEST_SEND_SMS
                        )
                    }
                    return@setOnClickListener
                }


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
        /*
                holder.frame.setOnTouchListener { view, motionEvent ->
                    when (motionEvent?.action) {
                        MotionEvent.ACTION_DOWN ->{
                            Log.d("SPEED_DIAL","Click")
                            activity.makeAPhoneCall(record.telephone_number)
                        }
                    }
                    return@setOnTouchListener view?.onTouchEvent(motionEvent) ?: true
                }

                holder.innerFrame.setOnTouchListener { view, motionEvent ->
                    (view.parent as View).onTouchEvent(motionEvent)
                }

                holder.userImage.setOnTouchListener { view, motionEvent ->
                    (view.parent as View).onTouchEvent(motionEvent)
                }

                holder.userName.setOnTouchListener { view, motionEvent ->
                    (view.parent as View).onTouchEvent(motionEvent)
                }

                holder.userPhoneNumber.setOnTouchListener { view, motionEvent ->
                    (view.parent as View).onTouchEvent(motionEvent)
                }
                holder.innerContainer.setOnTouchListener { view, motionEvent ->
                    (view.parent as View).onTouchEvent(motionEvent)
                }
        */

    }

    fun setData(contacts: ArrayList<Contact>) {
        this.contacts = contacts
    }


    fun getData(): ArrayList<Contact> {
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