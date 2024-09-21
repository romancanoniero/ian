package com.iyr.fewtouchs.ui.views.home.fragments.friends.adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.base.ConfirmationDialog
import com.iyr.ian.ui.friends.FriendsFragmentCallback
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.utils.UIUtils
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.hideKeyboard

/*
interface FriendsMainActivityInterface : FriendsMainActivityInterface {
    fun cancelInvitation(userKey: Contact)
    fun onFriendshipAccepted(eventKey: String, user: UserMinimum)
    fun contactRemove(eventKey: Contact)
    fun goToEvent(eventKey: String)
    fun contactRequestExisting(record: Contact)
    fun inviteNewFriend(message: String)
    fun resendInvitation(record: Contact)
    fun onContactRemoved(contact: Contact)
    fun contactRequestAccept(eventKey: String, user: UserMinimum)
    fun contactRequestRefuse(contact: Contact, viewPressed: View?)

}
*/
class FriendsAdapter(
    val context: Context,
    val activity: Activity,
    val callback: FriendsFragmentCallback
) :
    RecyclerView.Adapter<FriendsAdapter.UserViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()

    //  private var context: Context
    var list: java.util.ArrayList<Contact> = ArrayList()
    //  private var resultsFromSearches: ArrayList<Contact> = ArrayList<Contact>()
    //  private var compoundList: java.util.ArrayList<Contact> = ArrayList<Contact>()

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_friend_adapter, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val contact = list[position]

        if (Validators.isValidMail((contact.display_name ?: "").toString())) {
            holder.userName.text = (contact.display_name ?: "").lowercase()
        } else {
            holder.userName.text = (contact.display_name ?: "").uppercase()
        }



        if (contact.image != null) {

            // TODO: Pasarlo a Coroutina


            val storageReference = StorageRepositoryImpl().generateStorageReference("${AppConstants.PROFILE_IMAGES_STORAGE_PATH}${contact.user_key.toString()}/${contact.image?.file_name.toString()}")
            Log.d("GLIDEAPP","9")


            GlideApp.with(context)
                .asBitmap()
                .load(storageReference)
                .placeholder(AppCompatResources.getDrawable(context, R.drawable.progress_animation))
                .error(AppCompatResources.getDrawable(context, R.drawable.ic_error))
                .into(holder.userImage)

        } else {

            Glide.with(context)
                .asBitmap()
                .load(R.drawable.ic_unknown_user)
                .into(holder.userImage)
        }

        holder.switchSpeedDialSection.visibility = View.INVISIBLE
        holder.secondLine.visibility = GONE

        when (contact.status) {

            FriendshipStatusEnums.USER_NOT_FOUND.name -> {

                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_friend_send_invitation)
                    .into(holder.primaryActionButton)

                holder.primaryActionButton.setOnClickListener {

                    callback.clearSearchBox()
                    context.hideKeyboard(holder.itemView)
                    context.handleTouch()
                    // TODO : HACER QUE EL LINK DE INVITACION SE GENERE UNA SOLA VEZ AL CREAR AL USUARIO.
                    val map: HashMap<String, String> = HashMap()
                    map["action"] = AppConstants.Companion.DYNAMIC_LINK_ACTION_FRIENDSHIP
                    map["key"] = SessionForProfile.getInstance(context).getUserId()
                    UIUtils.createShortDynamicLink(context, map, object :
                        OnCompleteCallback {

                        override fun onComplete(success: Boolean, shortlink: Any?) {

                            var invitationText = String.format(
                                context.getText(R.string.app_installation_invitation_message)
                                    .toString(),
                                context.getString(R.string.app_name)
                            )

                            invitationText =
                                invitationText.plus(System.getProperty("line.separator"))
                            invitationText =
                                invitationText.plus(System.getProperty("line.separator"))
                            invitationText = invitationText.plus(shortlink)

                            callback.inviteNewFriend(invitationText)

                        }

                    })
                    //}
                }
            }
            FriendshipStatusEnums.NOT_A_FRIEND_BUT_EXISTS.name -> {

                holder.primaryActionButton.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_add_white_24dp
                    )
                )
                holder.primaryActionButton.setOnClickListener {
                    context.handleTouch()
                    callback.clearSearchBox()
                    callback.inviteExistingUser(contact)

                }

                //          holder.primaryActionText.setText(R.string.invite)

                //       holder.secondaryActionButton.visibility = View.GONE

//                holder.statusStamp.visibility = View.GONE
                holder.secondLine.text = context.getText(R.string.not_in_your_network)
                holder.secondLine.visibility = View.VISIBLE
            }

            FriendshipStatusEnums.ACCEPTED.name -> {

                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_delete)
                    .into(holder.primaryActionButton)


                //               holder.primaryActionText.text = context.getText(R.string.remove)
                holder.primaryActionButton.setOnClickListener {
                    callback.clearSearchBox()
                    context.handleTouch()
                    callback.contactRemove(contact)
                }

//                holder.secondaryActionButton.visibility = View.GONE
                holder.secondLine.visibility = GONE
                if ((contact.have_phone ?: false) && !(contact.telephone_number ?: "").isEmpty()) {
                    holder.switchSpeedDialSection.visibility = View.VISIBLE
                    holder.switchSpeedDial.isChecked = contact.add_to_speed_dial ?: false
                } else
                    holder
                        .switchSpeedDialSection.visibility = View.INVISIBLE


                holder.switchSpeedDial.setOnCheckedChangeListener { _, enabled ->



                 /*

                 TODO : Arreglar esto

                    ContactsWSClient.instance.updateSpeedDialStatus(
                        contact.user_key!!,
                        enabled,
                        object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {

                            }
                        })

                    */
                }
            }
            FriendshipStatusEnums.PENDING.name -> {
                holder.switchSpeedDialSection.visibility = GONE
                if (contact.author_key != SessionForProfile.getInstance(context).getUserId()) {
                    holder.secondLine.text =
                        String.format(
                            context.getText(R.string.a_user_invites_you_to_be_friends).toString(),
                            contact.display_name
                        )

                    holder.primaryActionButton.setBackgroundColor(context.getColor(R.color.white))
                    holder.primaryActionButton.setImageResource(R.drawable.ic_question_mark)
                    holder.primaryActionButton.setOnClickListener {
                        context.handleTouch()
                        val dialog = ConfirmationDialog(context, activity)
                        dialog.setTitle(R.string.warning)
                        dialog.setLegend(
                            String.format(
                                context.getString(R.string.do_you_want_to_accept_invitation_from),
                                contact.display_name
                            )
                        )
                        dialog.setButton1Caption(R.string.accept)
                        dialog.setButton2Caption(R.string.refuse)


                        dialog.setCallback(object : IAcceptDenyDialog {
                            override fun onAccept() {
                                contact.status = FriendshipStatusEnums.ACCEPTED.toString()
                                //    callback.onContactChangedInternal(contact)
                                context.handleTouch()
                                // actualizar el estado local
                                callback.contactRequestAccept(
                                    contact
                                )

                            }
                        })
  /*
                        dialog.setCallback(object : OnConfirmationButtonsListener {
                            override fun onAccept() {

                                contact.status = FriendshipStatusEnums.ACCEPTED.toString()
                                //    callback.onContactChangedInternal(contact)
                                context.handleTouch()
                                // actualizar el estado local
                                callback.contactRequestAccept(
                                    contact
                                )

                            }

                            override fun onCancel() {
                                context.handleTouch()
                                callback.contactRefuse(contact)
                            }
                        })
*/

                        dialog.show()

                    }
/*
                    holder.secondaryActionButton.visibility = VISIBLE
                    holder.secondaryActionButton.setOnClickListener {
                        if (context is FriendsMainActivityInterface) {
                            UIUtils.vibrateOnTouch(context)

                            (context as FriendsMainActivityInterface).contactRefuse(
                                record,
                                record.notification_key
                            )


                        }

                    }
*/

                } else {
                    holder.secondLine.text = context.getText(R.string.waiting_for_acceptance)
                    holder.primaryActionButton.visibility = View.VISIBLE
                    //                holder.primaryActionText.text = context.getText(R.string.cancel)

                    holder.primaryActionButton.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_delete
                        )
                    )
                    holder.primaryActionButton.setOnClickListener {
                        context.handleTouch()
                        callback.clearSearchBox()
                        callback.cancelInvitation(contact)
                    }
                    //                holder.secondaryActionButton.visibility = View.GONE
                }
/*
                holder.statusStamp.visibility = View.VISIBLE
                holder.statusStamp.text = context.getText(R.string.pending)
                holder.statusStamp.setBackgroundColor(context.getColor(R.color.darkGray))
*/
                holder.secondLine.visibility = View.VISIBLE


            }
        }

    }

    fun setData(contacts: ArrayList<Contact>) {
        list = contacts
    }


    fun getData(): ArrayList<Contact> {
        return list
    }

    fun clearUsersFromSearchList() {
        list.clear()
        // combineTables()
    }
/*
    fun addFriendFromSearch(contact: Contact) {
        compoundList.clear()
        compoundList.add(contact)
        notifyDataSetChanged()

    }
*/

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

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