package com.iyr.ian.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.dynamiclinks.DynamicLink.AndroidParameters
import com.google.firebase.dynamiclinks.DynamicLink.IosParameters
import com.google.firebase.dynamiclinks.DynamicLink.SocialMetaTagParameters
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.iyr.ian.AppConstants.Companion.DYNAMICLINKS_URI_PREFIX
import com.iyr.ian.AppConstants.ServiceCode.DYNAMICLINKS_PAGE_LINK
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.sharing_app.SharingContents
import com.iyr.ian.utils.sharing_app.SharingTargets
import kotlinx.coroutines.tasks.await


object UIUtils {


    annotation class JvmStatic

    fun Context.isApplicationVisible(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(1)
        val taskInfo = tasks[0]
        return taskInfo.topActivity?.packageName == packageName
    }

    fun Context.getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun Activity.statusBarTransparent() {
        this.window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
            val rectangle = Rect()
            val window: Window = window
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight: Int = rectangle.top
        }
    }


    fun Context.isVibrationConfigOn(): Boolean {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        when (am!!.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                return 1 == Settings.System.getInt(
                    contentResolver,
                    Settings.System.VIBRATE_WHEN_RINGING,
                    0
                )
            }

            AudioManager.RINGER_MODE_SILENT -> {                    // code here
                return false
            }

            AudioManager.RINGER_MODE_VIBRATE -> {                     // code here
                return true
            }
        }
        return false
    }


    fun Context.handleTouch() {
        if (isVibrationConfigOn()) {
            vibrateOnTouch(this)
        }
        try {
            val mPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.sound_touch)
            mPlayer.setOnCompletionListener {
                mPlayer.reset()
                mPlayer.release()
            }
            mPlayer.prepare()
            mPlayer.start()

        } catch (ex: Exception) {


        }


    }


    @SuppressLint("MissingPermission")
    private fun vibrateOnTouch(context: Context) {
        /*
               var vibrationIsEnabled = SessionForProfile.getInstance(context)
                   .getProfileProperty("vibrations_on", false) as Boolean

               if (vibrationIsEnabled) {
       */
        val vibe: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibe.vibrate(100)
        //    }
    }

    @SuppressLint("MissingPermission")
    private fun vibrateOnTouch(context: Context, force: Boolean) {
        val vibe: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibe.vibrate(100)
    }


    private fun createDynamicLink(
        context: Context,
        dynamicLinkParameters: HashMap<String, String>
    ): Uri {
        val parameters =
            StringBuilder(DYNAMICLINKS_URI_PREFIX + DYNAMICLINKS_PAGE_LINK)
        val it: Iterator<*> = dynamicLinkParameters.entries.iterator()
        var index = 0
        while (it.hasNext()) {
            val pair =
                it.next() as Map.Entry<*, *>
            if (index == 0) {
                parameters.append("?")
            } else {
                parameters.append("&")
            }
            parameters.append(pair.key)
            parameters.append("=")
            parameters.append(pair.value)
            index++
        }

        val dynamicLinkBuilder =
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(parameters.toString()))
                .setDomainUriPrefix(DYNAMICLINKS_URI_PREFIX + DYNAMICLINKS_PAGE_LINK)

                .setAndroidParameters(
                    AndroidParameters.Builder(
                        context.packageName
                    )
                        .setMinimumVersion(1)
                        .build()
                )
                .setIosParameters(
                    IosParameters.Builder(context.packageName)
                        .setAppStoreId("123456789")
                        .setMinimumVersion("1.0")
                        .build()
                ) /*
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .setItunesConnectAnalyticsParameters(
                        new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                .setProviderToken("123456")
                                .setCampaignToken("example-promo")
                                .build())
  */
                .setSocialMetaTagParameters(
                    SocialMetaTagParameters.Builder()
                        .setTitle(context.getString(R.string.app_name))
                        .setDescription(
                            context.getString(R.string.sharing_app_social_media_description)
                        )
                        .build()
                )
        // Or buildShortDynamicLink()
        val dynamicLink = dynamicLinkBuilder.buildDynamicLink()
        return dynamicLink.uri
    }

    fun createShortDynamicLink(
        context: Context,
        dynamicLinkParameters: HashMap<String, String>,
        callback: OnCompleteCallback
    ) {
        val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLongLink(createDynamicLink(context, dynamicLinkParameters))
            .buildShortDynamicLink()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Short link created
                    val shortLink = task.result.shortLink
                    val flowchartLink = task.result.previewLink
                    callback.onComplete(true, shortLink)
                } else {
                    // Error
                    // ...
                    callback.onError(task.exception!!)
                }
            }
    }


    suspend fun createShortDynamicLink(
        context: Context,
        dynamicLinkParameters: HashMap<String, String>
    ): Resource<String?> {

        return try {
            val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(createDynamicLink(context, dynamicLinkParameters))
                .buildShortDynamicLink()
                .await()

            val shortLink = shortLinkTask.shortLink.toString()
            val flowchartLink = shortLinkTask.previewLink
            Resource.Success<String?>(shortLink.toString())
        } catch (exception: Exception) {
            Resource.Error<String?>(exception.localizedMessage.toString())
        }
        /*
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Short link created
                            val shortLink = task.result.shortLink
                            val flowchartLink = task.result.previewLink
                            callback.onComplete(true, shortLink)
                        } else {
                            // Error
                            // ...
                            callback.onError(task.exception!!)
                        }
                    }
                */
    }

    fun Context.getScreenDimensions(): Point {

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }


    fun Activity.getDensityName(context: Context): String {
        val density = context.resources.displayMetrics.density
        if (density >= 4.0) {
            return "xxxhdpi"
        }
        if (density >= 3.0) {
            return "xxhdpi"
        }
        if (density >= 2.0) {
            return "xhdpi"
        }
        if (density >= 1.5) {
            return "hdpi"
        }
        return if (density >= 1.0) {
            "mdpi"
        } else "ldpi"
    }
}


fun FragmentActivity.hasOpenedDialogs(): Boolean {
    val fragments: List<Fragment> = this.supportFragmentManager.fragments
    if (fragments != null) {
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                return true
            }
        }
    }
    return false
}

fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.3f
    wm.updateViewLayout(container, p)
}

fun Context.playSound(
    soundResId: Int
) {
    playSound(soundResId, null, null)
}

fun Context.playSound(
    soundResId: Int,
    beforePlayCallback: OnCompleteCallback?,
    afterPlayCallback: OnCompleteCallback?
) {
    // Get a handler that can be used to post to the main thread
    val mainHandler: Handler = Handler(Looper.getMainLooper())
    mainHandler.post {
        val player: MediaPlayer = MediaPlayer.create(this, soundResId)
        if (beforePlayCallback != null) {
            beforePlayCallback.onComplete(true, null)
        }
        //             player.start();

        if (afterPlayCallback != null) {
            player.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(p0: MediaPlayer?) {
                    player.release()
                    afterPlayCallback.onComplete(true, null)
                }
            })
        }
        player.start()
    }


}


fun Activity.share(message: String) {
    share(message, SharingContents(SharingTargets.GENERIC, null, null))
}

fun Activity.share(message: String, sharingInfo: SharingContents) {

    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(
        Intent.EXTRA_TEXT,
        message
    )
    when (sharingInfo.sharingMethod) {
        SharingTargets.EMAIL -> {
            sendIntent.putExtra(
                Intent.EXTRA_EMAIL,
                sharingInfo.contactAddress
            )
            sendIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                sharingInfo.title
            )
        }

        SharingTargets.SMS -> {
            sendIntent.putExtra(
                Intent.EXTRA_PHONE_NUMBER,
                sharingInfo.contactAddress
            )
        }

        else -> {}
    }
    sendIntent.type = "text/plain"
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Activity.screenHeight() = window.decorView.context.resources.displayMetrics.heightPixels


fun Activity.screenWidth() = window.decorView.context.resources.displayMetrics.widthPixels
