package com.iyr.ian.ui.notifications.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.callbacks.INotifications
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.models.EventNotificationType
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.ui.callback.MainActivityCallback
import com.iyr.ian.ui.dialogs.NotificationAdapterCallback
import com.iyr.ian.ui.interfaces.FriendsMainActivityInterface
import com.iyr.ian.ui.interfaces.INotificationPopup
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getHtmlStyledText
import com.iyr.ian.utils.isGPSEnabled
import com.iyr.ian.utils.requestPermissionsLocation
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale


class NotificationsAdapter(val activity: Activity, val callback: INotifications) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
    private var mList: java.util.ArrayList<EventNotificationModel>? =
        ArrayList<EventNotificationModel>()

    init {


    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = NotificationViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_adapter, parent, false)
    )

    override fun getItemCount() = mList!!.size


    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {

        val record: EventNotificationModel = mList!![position]


        var notificationType =
            record.notification_type // i copy the value of this variable to assign manually to other variable.
        if (notificationType in listOf(
                EventNotificationType.NOTIFICATION_TYPE_SEND_POLICE.name,
                EventNotificationType.NOTIFICATION_TYPE_SEND_FIREMAN.name,
                EventNotificationType.NOTIFICATION_TYPE_SEND_AMBULANCE.name,
                EventNotificationType.NOTIFICATION_TYPE_ROBBER_ALERT.name,
                EventNotificationType.NOTIFICATION_TYPE_PERSECUTION.name,
                EventNotificationType.NOTIFICATION_TYPE_SCORT_ME.name,
                EventNotificationType.NOTIFICATION_TYPE_KID_LOST.name,
                EventNotificationType.NOTIFICATION_TYPE_PET_LOST.name,
                EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.name
            )
        ) {
            notificationType = EventNotificationType.NOTIFICATION_TYPE_EVENT_NOTIFICATION.name
        }

        val localeBylanguageTag: Locale = Locale.forLanguageTag(Locale.getDefault().toLanguageTag())
        val timeAgoLocale: TimeAgoMessages =
            TimeAgoMessages.Builder().withLocale(localeBylanguageTag).build()
        holder.timeMark.text = TimeAgo.using(record.time, timeAgoLocale)

        when (notificationType) {

            EventNotificationType.NOTIFICATION_TYPE_MESSAGE.name -> {

                var notificationRecord = record.event_data

                holder.title.text = activity.getText(R.string.new_messages)
                var userKey: String = record.event_info?.get("user_key").toString()
                val displayName: String = record.event_info?.get("user_name").toString()
               GlobalScope.launch(Dispatchers.IO) {
                activity.assignFileImageTo(
                    record.event_info?.get("profile_image_path").toString(),
                    "images",
                    holder.userImage
                )
               }
                var qty = record.qty
                var textMessage = formatRichTextMessage(
                    holder,
                    R.string.notification_unread_messages_html,
                    qty.toString(), displayName
                )
                // aca hay que ver si el evento esta en seguimiento y mostrar el icono
                // para ir o que decida si lo va a seguir o lo abandona

                holder.primaryButton.text = activity.getText(R.string.go_to_chat)
                holder.primaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()

                        var firstUnreadMessageKey =
                            (record.event_data?.get("messages") as List<HashMap<String, Any?>>).first()
                                .get("message_key").toString()

                        callback.onGoToChatPressed(
                            record.event_key,
                            firstUnreadMessageKey
                        )
                    }
                }
                holder.secondaryButton.visibility = GONE

            }

            EventNotificationType.NOTIFICATION_TYPE_ADDED_TO_SPEED_DIAL.name -> {
                holder.title.text =
                    activity.getText(R.string.notification_user_added_speed_dial_title)
                var userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String =
                    record.event_data?.get("display_name").toString()

                var userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                //   displayUserImage(userKey, userProfileFileName, holder)

                displayDrawable(R.drawable.speed_dial_image, holder)

                var textMessage = ""
                formatRichTextMessage(
                    holder,
                    R.string.notification_user_added_speed_dial_html,
                    displayName
                )


                // aca hay que ver si el evento esta en seguimiento y mostrar el icono
                // para ir o que decida si lo va a seguir o lo abandona

                holder.primaryButton.text = activity.getText(R.string.ok)
                holder.primaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()
                        callback.notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }
                holder.secondaryButton.visibility = GONE
            }


            EventNotificationType.NOTIFICATION_TYPE_USER_STATUS_OK.name -> {
                holder.title.text = activity.getText(R.string.notifications_user_status_ok_title)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String =
                    record.event_data?.get("display_name").toString()

                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)
                var textMessage = ""
                holder.redField.text = displayName
                holder.blackField.text =
                    activity.getString(R.string.notifications_user_status_ok_message)

                formatRichTextMessage(
                    holder,
                    R.string.notifications_user_status_ok_message_html,
                    displayName
                )


                // aca hay que ver si el evento esta en seguimiento y mostrar el icono
                // para ir o que decida si lo va a seguir o lo abandona


                val alreadyFollowed =
                    (activity as MainActivityCallback).isFollingEvent(record.event_key)

                if (!alreadyFollowed) {
                    holder.primaryButton.text = activity.getText(R.string.follow)
                } else {
                    holder.primaryButton.text = activity.getText(R.string.go)
                }

//                holder.actionButtonText.setTextColor(activity.getColor(R.color.colorPrimary))
                holder.primaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()
                        if (!alreadyFollowed) {
                            (activity as NotificationAdapterCallback).onStartToFollow(record.event_key)
                        } else {
                            (activity as NotificationAdapterCallback).onAgreeToAssist(
                                record.notification_key,
                                record.event_key
                            )
                        }
                    }
                }

                holder.secondaryButton.text = activity.getText(R.string.delete)
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()
                        callback.notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }
            }

            EventNotificationType.NOTIFICATION_TYPE_USER_IN_TROUBLE.name -> {
                /*
                           when (record.event_type) {
                               EventTypes.PANIC_BUTTON.name -> {
           */
                holder.title.text = activity.getText(R.string.phone_compromised)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String =
                    record.event_data?.get("display_name").toString()
                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)


                var textMessage = ""
                /*
                  if (displayName != null) {
                      textMessage = String.format(
                          activity.getString(R.string.notification_telephone_compromised_message),
                          displayName
                      )
                  }
                  holder.message.text = textMessage
                  */
                holder.redField.text = displayName
                holder.blackField.text =
                    activity.getString(R.string.notification_telephone_compromised_message)


                formatRichTextMessage(
                    holder,
                    R.string.notification_telephone_compromised_message_html,
                    displayName
                )


                val alreadyFollowed =
                    (activity as MainActivityCallback).isFollingEvent(record.event_key)
                if (!alreadyFollowed) {
                    holder.primaryButton.text = activity.getText(R.string.follow)
                } else {
                    holder.secondaryButton.text = activity.getText(R.string.go)
                }

//                holder.actionButtonText.setTextColor(activity.getColor(R.color.colorPrimary))
                holder.primaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()
                        if (!alreadyFollowed) {
                            (activity as NotificationAdapterCallback).onStartToFollow(record.event_key)
                        } else {
                            (activity as NotificationAdapterCallback).onAgreeToAssist(
                                record.notification_key,
                                record.event_key
                            )
                        }
                    }
                }

                holder.secondaryButton.setOnClickListener {
                    if (activity is FriendsMainActivityInterface) {
                        activity.handleTouch()
                        callback.notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }

                }

                /*
                                    }
                                }
                  */
            }

            EventNotificationType.NOTIFICATION_TYPE_EVENT_STATUS_CLOSED_OK.name -> {

                var userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //          var imageUrl: String =record.event_data?.get("image").toString()

                var title = ""
                var message = ""

                when (record.event_type) {
                    EventTypesEnum.PANIC_BUTTON.name -> {
                        title =
                            activity.getText(R.string.event_panic_button_user_safe_title).toString()
                        message =
                            activity.getText(R.string.event_panic_button_user_safe_message) as String

                        formatRichTextMessage(
                            holder,
                            R.string.event_panic_button_user_safe_message_html,
                            displayName
                        )

                        createTextObjects(message)


                    }

                    else -> {
                        title = activity.getText(R.string.event_close_successfully_title).toString()
                        message = activity.getString(R.string.event_close_successfully_message)
                        formatRichTextMessage(
                            holder,
                            R.string.event_close_successfully_message_html,
                            displayName
                        )
                    }


                }

//                holder.title.text = title
                var textMessage = ""
                if (displayName != null) {
                    //                  textMessage = message
                    holder.redField.text = displayName
                    holder.blackField.text = message

                }
                //holder.message.text = textMessage
                holder.redField.text = displayName
                holder.blackField.text = message


                /*
                              if (imageUrl != null) {
                                  Glide.with(con)
                                      .asBitmap()
                                      .load(imageUrl)
                                      .into(holder.image)
                              }
              */
                holder.primaryButton.visibility = View.VISIBLE
                holder.secondaryButton.visibility = GONE

                holder.primaryButton.text = activity.getText(R.string.delete)
                holder.primaryButton.setOnClickListener {
                    if (callback is INotificationPopup) {
                        activity.handleTouch()
                        callback.notificationsDeleteByEvent(
                            record,
                            holder.primaryButton
                        )
                    }
                }
            }

            EventNotificationType.CONTACT_REQUEST.name -> {
                holder.title.text = activity.getText(R.string.notification_contact_request_title)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)


                //            var imageUrl: String =record.event_data?.get("image").toString()
                var textMessage = ""
                if (displayName != null) {
                    /*
                                        textMessage = String.format(
                                            activity.getString(R.string.notification_contact_request_message),
                                            displayName
                                        )
                      */
                    holder.redField.text = displayName
                    holder.blackField.text =
                        activity.getString(R.string.notification_contact_request_message)

                    formatRichTextMessage(
                        holder,
                        R.string.notification_contact_request_message_html,
                        displayName
                    )
                }
                //            holder.message.text = textMessage
                /*
                                holder.primaryButton.setBackgroundColor(activity.getColor(R.color.white))
                                holder.actionButtonIcon.setImageResource(R.drawable.ic_check_mark)
                  */

                val imageUrl: String = record.event_data?.get("image_file_name").toString()
                if (imageUrl != null) {

                    displayUserImage(
                        record.event_data?.get("user_key").toString(),
                        imageUrl,
                        holder
                    )
                }


                holder.primaryButton.text = activity.getText(R.string.accept)
                holder.secondaryButton.text = activity.getText(R.string.refuse)
//                holder.actionButtonText.setTextColor(activity.getColor(R.color.colorPrimary))
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        val contact = Contact()
                        contact.user_key = userKey
                        contact.display_name = displayName
                        val image = MediaFile()
                        image.file_name = imageUrl
                        contact.image = image
                        (callback as INotifications).contactRequestAccept(contact)
                    }

                }
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }
            }

            EventNotificationType.NOTIFICATION_TYPE_EVENT_NOTIFICATION.name -> {
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                var textMessage = ""
                val imageUrl: String = record.event_data?.get("image_file_name").toString()

                displayEventAvatar(record.event_type, holder)
                holder.redField.text = displayName
                holder.blackField.text = activity.getString(R.string.requires_you_help)

                formatRichTextMessage(holder, R.string.requires_you_help_html, displayName)

                holder.primaryButton.text = activity.getText(R.string.assist)
                holder.secondaryButton.text = activity.getText(R.string.ignore)
                holder.primaryButton.visibility = View.VISIBLE
                holder.secondaryButton.visibility = View.VISIBLE

                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        val contact = Contact()
                        contact.user_key = userKey
                        contact.display_name = displayName
                        val image = MediaFile()
                        image.file_name = imageUrl
                        contact.image = image

                        var eventType = record.event_type

                        if (activity.isGPSEnabled()) {
                            if (activity.areLocationPermissionsGranted())
                                (callback as INotifications).onAgreeToAssist(
                                    record.notification_key,
                                    record.event_key
                                )
                            else
                                activity.requestPermissionsLocation()
                        } else {
                            findNavController(holder.itemView).navigate(R.id.GPSEnabledIsRequiredDialog)
                        }
                    }
                }
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }
            }

            EventNotificationType.EVENT_LEAVED.name -> {
                holder.title.text = activity.getText(R.string.event_leave)
                var userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //            var imageUrl: String =record.event_data?.get("image").toString()
                var textMessage = ""
                if (displayName != null) {
                    /*
                                      textMessage = String.format(
                                          activity.getString(R.string.item_event_leave_message),
                                          displayName
                                      )
                  */
                    holder.redField.text = displayName
                    holder.blackField.text = activity.getString(R.string.item_event_leave_message)

                    formatRichTextMessage(
                        holder,
                        R.string.item_event_leave_message_html,
                        displayName
                    )

                }
//                holder.message.text = textMessage
                holder.secondaryButton.visibility = GONE
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }
            }

            EventNotificationType.PULSE_VERIFICATION_FAILED.name -> {
                holder.title.text = activity.getText(R.string.pulse_verification_failed_title)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //          var imageUrl: String =record.event_data?.get("image").toString()

                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)


                var textMessage = ""


                if (displayName != null) {
                    /*
                    textMessage = String.format(
                        activity.getString(R.string.pulse_verification_failed_message),
                        displayName
                    )
                    */
                    holder.redField.text = displayName
                    holder.blackField.text =
                        activity.getString(R.string.pulse_verification_failed_message)

                    formatRichTextMessage(
                        holder,
                        R.string.pulse_verification_failed_message_html,
                        displayName
                    )

                }
//                holder.message.text = textMessage
                /*
                              if (imageUrl != null) {
                                  Glide.with(con)
                                      .asBitmap()
                                      .load(imageUrl)
                                      .into(holder.image)
                              }
              */
//                holder.actionButton.visibility = View.GONE

                //            holder.primaryButton.setBackgroundColor(activity.getColor(R.color.colorRed))
                //              holder.actionButtonIcon.setImageResource(R.drawable.ic_viewers)

                holder.primaryButton.text = activity.getText(R.string.open)
                //        holder.actionButtonText.setTextColor(activity.getColor(R.color.white))
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).goToEvent(record.event_key)
                    }

                }


                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }

                }
            }

            EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.name -> {
                holder.title.text = activity.getText(R.string.event_panic_button_title)
                var userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //            var imageUrl: String =record.event_data?.get("image").toString()
                var textMessage = ""
                if (displayName != null) {
                    /*
                    textMessage = String.format(
                        activity.getString(R.string.event_panic_button_message),
                        displayName
                    )
                    */
                    holder.redField.text = displayName
                    holder.blackField.text = activity.getString(R.string.event_panic_button_message)

                    formatRichTextMessage(
                        holder,
                        R.string.event_panic_button_message_html,
                        displayName
                    )

                }
//                holder.message.text = textMessage
                holder.primaryButton.visibility = GONE
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }

                }
            }


            EventNotificationType.NOTIFICATION_TYPE_FALLING_USER.name -> {
                holder.title.text = activity.getText(R.string.event_notification_falling_title)
                var userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //            var imageUrl: String =record.event_data?.get("image").toString()
                var textMessage = ""
                if (displayName != null) {
                    /*
                                        textMessage = String.format(
                                            activity.getString(R.string.event_notification_falling_message),
                                            displayName
                                        )
                    */
                    holder.redField.text = displayName
                    holder.blackField.text =
                        activity.getString(R.string.event_notification_falling_message)

                    formatRichTextMessage(
                        holder,
                        R.string.event_notification_falling_message_html,
                        displayName
                    )
                }

//                holder.message.text = textMessage
                /*
                              if (imageUrl != null) {
                                  Glide.with(con)
                                      .asBitmap()
                                      .load(imageUrl)
                                      .into(holder.image)
                              }
              */
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationsDeleteByEvent(
                            record,
                            holder.primaryButton
                        )
                    }
                }
                holder.secondaryButton.visibility = GONE

            }

            EventNotificationType.NOTIFICATION_TYPE_NOT_RESPONSE.name -> {
                holder.title.text = activity.getText(R.string.event_notification_falling_title)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //            var imageUrl: String =record.event_data?.get("image").toString()
                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)

                var textMessage = ""
                if (displayName != null) {
                    formatRichTextMessage(
                        holder,
                        R.string.event_notification_user_not_response_html,
                        displayName
                    )
                }

                holder.primaryButton.setText(R.string.go_to_event)
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).goToEvent(record.event_key)
                    }
                }
                holder.secondaryButton.visibility = View.VISIBLE
                holder.secondaryButton.setText(R.string.ignore)
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }

            }


            EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.name -> {
                holder.title.text =
                    activity.getText(R.string.notification_event_close_to_expire_title)
                val userKey: String = record.event_data?.get("user_key").toString()
                val displayName: String = record.event_data?.get("display_name").toString()
                //            var imageUrl: String =record.event_data?.get("image").toString()
                val userProfileFileName: String =
                    record.event_data?.get("image_file_name").toString()
                displayUserImage(userKey, userProfileFileName, holder)

                var textMessage = ""
                if (displayName != null) {
                    formatRichTextMessage(
                        holder,
                        R.string.notification_event_close_to_expire_message,
                        displayName
                    )
                }

                holder.primaryButton.setText(R.string.extend_the_event)
                holder.primaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).goToEvent(record.event_key)
                    }
                }
                holder.secondaryButton.visibility = View.VISIBLE
                holder.secondaryButton.setText(R.string.ignore)
                holder.secondaryButton.setOnClickListener {
                    if (callback is INotifications) {
                        activity.handleTouch()
                        (callback as INotifications).notificationDeleteByKey(
                            record,
                            holder.secondaryButton
                        )
                    }
                }

            }
        }


    }

    private fun formatRichTextMessage(
        holder: NotificationViewHolder,
        resId: Int,
        vararg params: String
    ) {

        val dynamicStyledText = activity.getHtmlStyledText(resId, *params)
        holder.richTextView.text = dynamicStyledText

    }

    private fun displayUserImage(
        userKey: String,
        profileImageName: String,
        holder: NotificationViewHolder
    ) {
        holder.userImage.visibility = View.VISIBLE
        holder.circularBG.visibility = GONE
        holder.avatarImage.visibility = GONE

        try {


            // TODO: Pasarlo a Coroutinas

            val storageReference = FirebaseStorage.getInstance()
                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                .child(userKey)
                .child(profileImageName)


            Log.d("GLIDEAPP", "13")


            GlideApp.with(activity)
                .asBitmap()
                .load(storageReference)
                .placeholder(activity.getDrawable(R.drawable.progress_animation))
                .error(activity.getDrawable(R.drawable.ic_error))
                .into(holder.userImage)

        } catch (exception: Exception) {
            callback.onError(exception)
        }

    }


    private fun displayEventAvatar(
        eventType: String,
        holder: NotificationViewHolder
    ) {
        holder.userImage.visibility = GONE
        holder.circularBG.visibility = View.VISIBLE
        holder.avatarImage.visibility = View.VISIBLE
        holder.avatarImage.setImageDrawable(activity.getEventTypeDrawable(eventType))
    }

    private fun displayDrawable(
        resId: Int,
        holder: NotificationViewHolder
    ) {
        holder.userImage.visibility = GONE
        holder.circularBG.visibility = GONE
        holder.avatarImage.visibility = View.VISIBLE
        holder.avatarImage.setImageDrawable(activity.getDrawable(resId))
    }

    fun setData(EventNotifications: ArrayList<EventNotificationModel>?) {
        mList = EventNotifications
    }

    fun addChild(EventNotification: EventNotificationModel) {
        mList!!.add(EventNotification)
        notifyItemChanged(mList!!.size - 1)
    }


    //--- funciones del textflow
    fun formatTextFlow(holder: NotificationViewHolder, text: String, vararg params: Any) {
        val words = text.split("[ ,.;]")
        words.forEach { word ->
            Log.d("SPEELING", word)
        }
    }

    //-------------------------


    private fun createTextObjects(message: String) {
        var textObjects: ArrayList<TextView> = ArrayList<TextView>()
        var messageSplit = message.split("[ .,;]")

    }

    fun getData(): ArrayList<EventNotificationModel> {
        return mList!!
    }


    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //    var swipeContainer = view.findViewById<SwipeRevealLayout>(R.id.swipeContainer)
        //    var topLay = view.findViewById<View>(R.id.topLay)
        var title: TextView = view.findViewById<TextView>(R.id.title)
        var redField: TextView = view.findViewById<TextView>(R.id.red_field)
        var blackField: TextView = view.findViewById<TextView>(R.id.black_field)
        var userImage: ImageView = view.findViewById<ImageView>(R.id.user_image)
        var circularBG: TextView = view.findViewById<TextView>(R.id.circular_bg)
        var avatarImage: ImageView = view.findViewById<ImageView>(R.id.avatar_image)
        var timeMark: TextView = view.findViewById<TextView>(R.id.time_mark)
        var primaryButton: Button = view.findViewById<Button>(R.id.primary_button)
        var secondaryButton: Button = view.findViewById<Button>(R.id.secondary_button)
        var richTextView: TextView = view.findViewById<TextView>(R.id.rich_text_message)


    }
}