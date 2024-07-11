package com.iyr.ian.apis

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.CHANNEL_ALARM_ID
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.NotificationsUtils
import java.util.Random


class NotificationsApi {


    class NotificationObject {
        var userToNotificateKey = ""
        var notificationType = -1
        var data: Any? = null
    }


    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(mContext)
    }


    fun showNotification(titleResId: Int, descriptionResId: Int, extras: Bundle = Bundle()) {

        NotificationsUtils.showNotification(
            AppClass.instance, mContext!!.getString(titleResId),
            mContext!!.getString(descriptionResId),
            extras
        )
    }

    fun showNotification(titleResId: Int, description: String, extras: Bundle = Bundle()) {

        NotificationsUtils.showNotification(
            AppClass.instance, mContext!!.getString(titleResId),
            description,
            extras
        )

    }

    fun showNotification(
        title: String?,
        description: String?,
        extras: Bundle = Bundle(),
        pendingIntent: PendingIntent? = null
    ) {

        //     if (AppClass.instance.isInForeground == false) {

        val bigText = NotificationCompat.BigTextStyle()
        bigText.setBigContentTitle(title)
        bigText.bigText(description)
        bigText.setSummaryText(description)


        //val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext!!.packageName + "/" + R.raw.policesiren)

        val hasImage = extras.containsKey("image")
        var n: NotificationCompat.Builder
        if (hasImage) {
            val imageUrl = extras.getString("image")


            val storageReference = FirebaseStorage.getInstance()
                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                .child(FirebaseAuth.getInstance().uid.toString())
                .child(imageUrl.toString())

            GlideApp.with(AppClass.instance)
                .asBitmap()
                .load(storageReference)
                .placeholder(AppClass.instance.getDrawable(R.drawable.progress_animation))
                .error(ResourcesCompat.getDrawable(AppClass.instance.resources,R.drawable.ic_error, AppClass.instance.theme))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {

//------------

                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
//--------------
                        n = NotificationCompat.Builder(
                            AppClass.instance, CHANNEL_ALARM_ID
                        )
                            .setContentTitle(title)
                            .setContentText(description)
                            .setSmallIcon(R.mipmap.ic_custom_launcher)
                            .setLargeIcon(resource)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setAutoCancel(true)
                            .setStyle(bigText)


                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            n.setSound(soundUri)
                        } else {
                            //    n.setChannelId(CHANNEL_DEFAULT_ID)
                        }


                        val notificationManager =
                            AppClass.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val random = Random()
                        val notificationId = random.nextInt(9999 - 1000) + 1000
                        //      notificationManager.notify(notificationId, n.build())

                        with(NotificationManagerCompat.from(mContext!!)) {
                            // notificationId is a unique int for each notification that you must define
                            if (ActivityCompat.checkSelfPermission(
                                    AppClass.instance,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)

                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.

  //                              ActivityCompat.requestPermissions(AppClass.instance.getCurrentActivity()!!, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATIONS_POST_PERMISSION_REQUEST_CODE)
                                return
                            }
                            notify(notificationId, n.build())
                        }

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }

                })


/*
                Glide.with(AppClass.instance)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {

//------------

                            val audioAttributes = AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
//--------------
                            n = NotificationCompat.Builder(
                                AppClass.instance, CHANNEL_ALARM_ID
                            )
                                .setContentTitle(title)
                                .setContentText(description)
                                .setSmallIcon(R.mipmap.ic_custom_launcher)
                                .setLargeIcon(resource)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setAutoCancel(true)
                                .setStyle(bigText)


                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                n.setSound(soundUri);
                            } else {
                                //    n.setChannelId(CHANNEL_DEFAULT_ID)
                            }


                            val notificationManager =
                                AppClass.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val random = Random()
                            val notificationId = random.nextInt(9999 - 1000) + 1000
                            //      notificationManager.notify(notificationId, n.build())

                            with(NotificationManagerCompat.from(mContext!!)) {
                                // notificationId is a unique int for each notification that you must define
                                notify(notificationId, n.build())
                            }

                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            TODO("Not yet implemented")
                        }

                    });
*/
            } else {

                n = NotificationCompat.Builder(
                    AppClass.instance,
                    mContext!!.getString(R.string.default_notification_channel_id)
                )
                    .setContentTitle(title)
                    .setContentText(description)
                    .setSmallIcon(R.mipmap.ic_custom_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setStyle(bigText)
                    .setContentIntent(pendingIntent)

                val notificationManager =
                    AppClass.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val random = Random()
                val notificationId = random.nextInt(9999 - 1000) + 1000
                notificationManager.notify(notificationId, n.build())

                with(NotificationManagerCompat.from(mContext!!)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(notificationId, n.build())
                }
            }
        //     }

        /*
        val resultIntent = Intent(AppClass.instance, NotificationsBroadcastReceiver::class.java)
        if (extras.containsKey("action")) {
            resultIntent.action = extras.getString("action")
             resultIntent.putExtras(extras)
        }
        val resultPendingIntent = PendingIntent.getBroadcast(
            AppClass.instance,
            1001,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

         */
        //  n.setContentIntent(resultPendingIntent)


/*
        val intent = Intent(mContext!!, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            mContext!!, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(mContext!!)
            .setChannelId( mContext!!.getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("title")
            .setContentText("body")
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
*/
    }

    /*
        fun sendNotificationViaServer(
            notificationObject: NotificationObject,
            callback: OnCompleteCallback?
        ): Task<String?>? {
            // Create the arguments to the callable function.
            val data: MutableMap<String, Any> = HashMap()

            data["notification_type"] = notificationObject.notificationType
            data["auth_token"] =
                SessionForProfile.getInstance(FacebookSdk.getApplicationContext())
                    .getProfileProperty("auth_token")

            when (notificationObject.notificationType) {
                NOTIFICATION_TYPE_USER_LEAVE_CHAT -> {
                    var dataObject = BlockedUser()
                    dataObject.user_key = (notificationObject.data as UserMinimum).user_key
                    dataObject.user_name = (notificationObject.data as UserMinimum).user_name
                    dataObject.user_image_url = (notificationObject.data as UserMinimum).user_image_url
                    data["user_to_notificate_key"] = dataObject.user_key
                    data["data"] = Gson().toJson(dataObject)
                }
            }


            return FirebaseFunctions.getInstance()
                .getHttpsCallable("sendNotificationToSingleUser")
                .call(data)
                .continueWith(Continuation<HttpsCallableResult, String?> { task -> // This continuation runs on either success or failure, but if the task
                    val result = task.result!!.data
                    callback?.onComplete(true, result.toString()).toString()
                }).addOnFailureListener(OnFailureListener { e ->
                    callback?.onError(e)

                })
        }
    */
    fun getNotificationToken(callback: OnCompleteCallback) {

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        "MESSAGING",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    task.exception?.let { callback.onError(it) }
                    return@OnCompleteListener
                } else {
                    val token = task.result
                    callback.onComplete(true, token)
                }


            })
    }


    companion object {
        fun getInstance(context: Context): NotificationsApi {
            mContext = context
            if (mInstance == null) {
                mInstance = NotificationsApi()
            }
            return mInstance as NotificationsApi
        }

        private var mContext: Context? = null
        private var mInstance: NotificationsApi? = null
    }

    init {
        mInstance = this
    }
}