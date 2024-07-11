package com.iyr.ian.utils.chat.viewholders.voice

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.R
import com.iyr.ian.app.AppClass.Companion.instance
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.chat.utils.DateFormatter
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.multimedia.IMultimediaPlayer
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/*
 * Created by troy379 on 05.04.17.
 */
class CustomIncomingVoiceMessageViewHolder(itemView: View, payload: Any?) :
    IncomingTextMessageViewHolder<Message?>(itemView, payload) {
    private val tvTime: TextView
    private val tvDuration: TextView

    private val onlineIndicator: View
    private val mediaView: PlayerView
    private val chatBubble: MaterialCardView
    private val statusIcon: ImageView
    private lateinit var player: Player

    init {
        tvDuration = itemView.findViewById(R.id.duration)
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
        tvTime = itemView.findViewById(R.id.messageTime)
        mediaView = itemView.findViewById(R.id.audioView)
        chatBubble = itemView.findViewById(R.id.chat_bubble)
        statusIcon = itemView.findViewById(R.id.status)
    }

    override fun onBind(message: Message?) {
        super.onBind(message)

        player = ExoPlayer.Builder(instance).build()
        mediaView.player = player

        val fileName = message?.voice?.url.toString().getJustFileName()

        GlobalScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main){
                message?.status = MessagesStatus.DOWNLOADING
                updateFileStatus(message)
            }


            try {
                var finalPath = FirebaseStorage.getInstance()
                    .getReference(fileName)
                    .downloadUrlWithCache(
                        instance,
                        fileName.substringBeforeLast("/")
                    )

                // Build the media item.


                withContext(Dispatchers.Main) {

                    if (finalPath != null) {
                        message?.status = MessagesStatus.READY
                        updateFileStatus(message)
                    }


                    val mediaItem = MediaItem.fromUri(File(finalPath).toString())
                    // Set the media item to be played.
                    player.setMediaItem(mediaItem)
                    player.prepare()

                    tvDuration.text =
                        MultimediaUtils.getInstance(instance)?.getDurationFormated(
                            Uri.parse(finalPath)
                        )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val isOnline = true
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online)
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline)
        }

        tvTime.text = DateFormatter.format(message?.createdAt, DateFormatter.Template.TIME)
        val payload = payload as Payload
    }

    private suspend fun updateFileStatus(message: Message?) {
        when (message?.status) {
            MessagesStatus.DOWNLOADING -> {
                statusIcon.setImageDrawable(instance.getDrawable(R.drawable.baseline_cloud_download_128))
                statusIcon.visibility = View.VISIBLE

                val animation: Animation = AlphaAnimation(1f, 0f)
                animation.duration = 1000
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                animation.repeatMode = Animation.REVERSE
                statusIcon.startAnimation(animation)

                Handler(Looper.getMainLooper()).postDelayed({
                    animation.cancel()
                }, 500)


            }

            MessagesStatus.READY -> {
                statusIcon.clearAnimation()
                statusIcon.visibility = View.GONE
            }

            else -> {
                /*
                    MessagesStatus.SENDING -> TODO()
                    MessagesStatus.SENT -> TODO()
                    MessagesStatus.DELIVERED -> TODO()
                    MessagesStatus.READ -> TODO()
                    MessagesStatus.ERROR -> TODO()
                    null -> TODO()
                  */
            }
        }
    }





    interface OnClicksListener {
        fun onAvatarClick()

        //   void onPlayButtonClick(String url);
        fun onPlayButtonClick(
            url: String?,
            mpInterface: IMultimediaPlayer?,
            callback: OnCompleteCallback?
        )
    }

    class Payload {
        var clicksListener: OnClicksListener? = null
    }
}