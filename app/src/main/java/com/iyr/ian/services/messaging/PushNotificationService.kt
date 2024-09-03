package com.iyr.ian.services.messaging

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.apis.NotificationsApi
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.EventNotificationType
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.utils.broadcastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PushNotificationService : FirebaseMessagingService() {
    private val TAG: String = "PUSH_MESSAGE_SERVICE"

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var notificationsRepository = NotificationsRepositoryImpl()
            notificationsRepository.registerNotificationsToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido: ${remoteMessage.data}")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, remoteMessage.data.toString())

        val intent = Intent("com.iyr.ian")
        val bundle = Bundle()
        val data = remoteMessage.data

        // recorre cada elemento del mapa de datos y lo agrega al bundle
        for (key in data.keys) {
            bundle.putString(key, data[key])
        }

        // recorre remoteMessage.notification y lo agrega al bundle
        remoteMessage.notification?.let {
            bundle.putString("title", it.title)
            bundle.putString("body", it.body)
            bundle.putString("titleLocKey", it.titleLocalizationKey.toString())
            bundle.putStringArray("titleLocArgs", it.titleLocalizationArgs)
            bundle.putString("bodyLocKey", it.bodyLocalizationKey.toString())
            bundle.putStringArray("bodyLocArgs", it.bodyLocalizationArgs)
            bundle.putString("image", it.imageUrl.toString())
            bundle.putString("click_action", it.clickAction)
        }
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)



/*
        if (AppClass.instance.isInForeground) {
            handleForegroundMessage(remoteMessage)
        } else {
            // La aplicación está en segundo plano o cerrada, mostrar la notificación
            showNotification(remoteMessage)
        }*/
    }


    private fun handleForegroundMessage(remoteMessage: RemoteMessage) {
        // Implementa la lógica para manejar el mensaje cuando la aplicación está en primer plano
        // Puedes optar por mostrar una notificación personalizada o actualizar la interfaz de usuario directamente.

        // Check if message contains a data payload.

        Log.d("PUSH_MESSAGE_SERVICE", remoteMessage.data.toString())

        if (remoteMessage.data.isNotEmpty()) {
            val notificationType = remoteMessage.data["notification_type"]!!
            var title = ""
            var body = ""
            if (remoteMessage.notification?.title != null) {
                title = remoteMessage.notification?.title!!
            } else {
                try {
                    title = getString(
                        applicationContext.resources.getIdentifier(
                            remoteMessage.notification?.titleLocalizationKey,
                            "string",
                            applicationContext.packageName
                        )
                    )
                } catch (ex: Exception) {
                    var oo = 3
                }

            }

            if (remoteMessage.notification?.body != null) {
                body = remoteMessage.notification?.body!!
            } else {
                body = try {
                    getString(
                        this.resources.getIdentifier(
                            remoteMessage.notification?.bodyLocalizationKey,
                            "string",
                            this.packageName
                        )
                    )

                } catch (ex: Exception) {
                    "......"
                }
                if (remoteMessage.notification?.bodyLocalizationArgs != null) {
                    val messageArgs = remoteMessage.notification?.bodyLocalizationArgs


                    var values = arrayOf(messageArgs)
                    var counter = 1
                    messageArgs?.forEach { value ->
                        body = body.replace("%$counter\$", value)
                        counter++
                    }
                }

            }

            val bundle = Bundle()
            if (remoteMessage.data["image"] != null) {
                bundle.putString("image", remoteMessage.data["image"])
            }

            //         if (!AppClass.instance.isInForeground) {

            when (notificationType) {
                EventNotificationType.NOTIFICATION_TYPE_MESSAGE.toString() -> {
                    var showNotification = true

                    AppClass.instance.getMainActivityRef()?.let { mainActivity ->

                        if (mainActivity.currentModuleIndex == IANModulesEnum.EVENTS_TRACKING.ordinal) {
                            var currentEventKey =
                                mainActivity.mMapFragment.getEventKey().toString()
                            if (currentEventKey == remoteMessage.data["eventKey"]) {
                                showNotification = false
                            }

                        }
  /*
                        mainActivity.mMapFragment.chatFragment?.let { chatFragment ->
                            if (mainActivity.mMapFragment.isVisible) {
                                var currentEventKey =
                                    mainActivity.mMapFragment.getEventKey().toString()
                                if (currentEventKey == remoteMessage.data["eventKey"]) {
                                    showNotification = false
                                }
                            }
                        }
*/
                    }

                    // Reviso si el mensaje ya fue leido



                    if (showNotification) {
                        val broadcastIntent =
                            Intent(this, MainActivity::class.java)

                        broadcastIntent.putExtra(
                            EventNotificationType.NOTIFICATION_TYPE_MESSAGE.toString(),
                            "notification_type",
                        )

                        broadcastIntent.putExtra(
                            "display_name",
                            remoteMessage.notification?.bodyLocalizationArgs?.get(0).toString()
                        )
                        broadcastIntent.putExtra(
                            "text",
                            remoteMessage.notification?.bodyLocalizationArgs?.get(1).toString()
                        )

                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext,
                            0,
                            broadcastIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )


                        NotificationsApi.getInstance(AppClass.instance)
                            .showNotification(title, body, Bundle(), pendingIntent)

                    }


                }

                EventNotificationType.NOTIFICATION_TYPE_EVENT_NOTIFICATION.name.toString() -> {
                    var pp = 33
                    var dataMap = remoteMessage.data
                    var eventType: String = dataMap.get("event_type").toString()
                    var eventKey = dataMap.get("event_key").toString()

                    when (eventType) {
                        EventTypesEnum.PANIC_BUTTON.name -> {
                            var coco = 3
                        }
                    }

                }

                EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.toString() -> {

                    val broadcastIntent =
                        Intent(EventNotificationType.NOTIFICATION_TYPE_PANIC_BUTTON.toString())
                    broadcastIntent.putExtra(
                        "display_name", remoteMessage.data["user_name"]
                    )/*
                    broadcastIntent.putExtra(
                        "remaining_time",
                        remoteMessage.data["remaining_time"].toString().toLong()
                    )
  */



                    baseContext.broadcastMessage(remoteMessage.data, "action_type")

                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    NotificationsApi.getInstance(AppClass.instance)
                        .showNotification(title, body, Bundle(), pendingIntent)

                }

                AppConstants.NOTIFICATION_TYPE_PULSE_REQUESTED -> {

                }

                EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString() -> {
                    val broadcastIntent =
                        Intent(EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString())
                    broadcastIntent.putExtra(
                        "display_name", remoteMessage.data["user_name"]
                    )
                    broadcastIntent.putExtra(
                        "remaining_time", remoteMessage.data["remaining_time"].toString().toLong()
                    )

                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    NotificationsApi.getInstance(AppClass.instance)
                        .showNotification(title, body, Bundle(), pendingIntent)
                }
            }

            //           }
            var pp = 32
            when (notificationType) {
                AppConstants.NOTIFICATION_TYPE_ARE_YOU_ON_DESTINATION_REQUESTED -> {
                    val broadcastIntent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    broadcastIntent.action = AppConstants.BROADCAST_DID_YOU_ARRIVE_REQUEST
                    broadcastIntent.putExtra(
                        "event_key", remoteMessage.data["event_key"].toString()
                    )
                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        broadcastIntent
                    )
                }

                AppConstants.NOTIFICATION_TYPE_PULSE_REQUESTED -> {
                    val intent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    intent.action = AppConstants.BROADCAST_PULSE_REQUIRED
                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        intent
                    )
                }

                EventNotificationType.NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE.toString() -> {
                    val intent = Intent(
                        AppClass.instance.applicationContext, PushNotificationService::class.java
                    )
                    intent.action = AppConstants.BROADCAST_EVENT_CLOSE_TO_EXPIRE
                    intent.putExtra("event_key", remoteMessage.data["event_key"])
                    intent.putExtra("display_name", remoteMessage.data["user_name"])
                    intent.putExtra(
                        "remaining_time", remoteMessage.data["remaining_time"].toString().toLong()
                    )

                    LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                        intent
                    )
                }


            }/*
                        when (notificationType) {
                            EventNotificationType.CONTACT_REQUEST.toString() -> {

                                val title = getString(R.string.suspicius_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.suspicius_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                            NOTIFICATION_TYPE_PULSE_REQUESTED.toString() -> {
                                val intent =
                                    Intent(AppClass.instance.applicationContext, PushNotificationService::class.java)
                                intent.action = BROADCAST_PULSE_REQUIRED
                                LocalBroadcastManager.getInstance(AppClass.instance).sendBroadcast(
                                    intent
                                    // intent.putExtra("onLocationServiceStatus", AppClass.instance.getGPSStatus())
                                )
                            }
                            EventNotificationType.NOTIFICATION_TYPE_USER_STATUS_OK.toString() -> {
                                val title = getString(R.string.notifications_user_ok_title)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.notifications_user_ok_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])
                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)

                            }
                            EventNotificationType.NOTIFICATION_TYPE_USER_IN_TROUBLE.toString() -> {
                                val title = getString(R.string.notifications_user_in_trouble_title)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.notifications_user_in_trouble_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])
                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)

                            }
                            /*
                            NOTIFICATION_TYPE_NEW_EVENT.toString() -> {
                                var title =
                                    StringUtils.getStringResourceByName(remoteMessage.data["title_loc_key"])
                                var message =
                                    StringUtils.getStringResourceByName(remoteMessage.data["body_loc_key"])
                                var messageArgs = remoteMessage.data["body_loc_args"]
                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification("Nuevo Evento", "esta llegando un evento")
                            }

                             */
                            EventNotificationType.NOTIFICATION_TYPE_SEND_POLICE.toString() -> {

                                val title = getString(R.string.police_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.police_notification_request_message),
                                    messageArgs
                                )

                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)

                            }
                            EventNotificationType.NOTIFICATION_TYPE_SEND_AMBULANCE.toString() -> {

                                val title = getString(R.string.ambulance_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.ambulance_notification_request_message),
                                    messageArgs
                                )

                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)

                            }
                            EventNotificationType.NOTIFICATION_TYPE_ROBBER_ALERT.toString() -> {

                                val title = getString(R.string.suspicius_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.suspicius_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                            EventNotificationType.NOTIFICATION_TYPE_PERSECUTION.toString() -> {

                                val title = getString(R.string.suspicius_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.suspicius_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                            EventNotificationType.NOTIFICATION_TYPE_SCORT_ME.toString() -> {

                                val title = getString(R.string.scorting_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.scorting_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                            EventNotificationType.NOTIFICATION_TYPE_KID_LOST.toString() -> {

                                val title = getString(R.string.kid_lost_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.kid_lost_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                            EventNotificationType.PANIC_BUTTON.toString() -> {

                                val title = getString(R.string.panic_button_notification_request)
                                val messageArgs = remoteMessage.data["body_loc_args"]
                                val message = String.format(
                                    getString(R.string.panic_button_notification_request_message),
                                    messageArgs
                                )
                                val bundle = Bundle()
                                bundle.putString("image", remoteMessage.data["image"])

                                ApiNotifications.getInstance(applicationContext)
                                    .showNotification(title, message, bundle)
                            }

                        }

                        */
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                //      scheduleJob()
            } else {
                // Handle message within 10 seconds
                //    handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }


    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        // Implementa la lógica para mostrar la notificación cuando la aplicación está en segundo plano
        // Utiliza el código proporcionado en el paso 5 del ejemplo anterior.

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_custom_launcher_foreground)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body).setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}